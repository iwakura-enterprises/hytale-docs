package enterprises.iwakura.docs.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import com.hypixel.hytale.math.vector.Vector2d;

import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.object.CacheIndex.Entry.Type;
import enterprises.iwakura.docs.object.DownloadedFile;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class ImageService {

    private static final String IMAGE_FORMAT_PNG = "png";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private final ConfigurationService configurationService;
    private final FileSystemCacheService fileSystemCacheService;

    private final Logger logger;
    private final DocsPlugin plugin;

    public void init() {
        logger.info("Initializing ImageDownloaderService...");
    }

    public CompletableFuture<Path> downloadImageFrom(String url) {
        var maxFileSizeBytes = configurationService.getDocsConfig().getRuntimeImageAssets().getMaxImageDownloadFileSizeKb() * 1024;

        var loadedEntry = fileSystemCacheService.getByName(url, Type.IMAGE);
        if (loadedEntry.isPresent()) {
            return CompletableFuture.completedFuture(loadedEntry.get().getFilePath());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Reimplement HEAD request to check the file size
//                var headRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(url))
//                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
//                    .build();
//
//                var headResponse = HTTP_CLIENT.send(headRequest, HttpResponse.BodyHandlers.discarding());
//
//                var contentType = headResponse.headers().firstValue("Content-Type").orElse("");
//                //if (!contentType.equals("image/png")) {
//                //    throw new IllegalArgumentException("URL does not point to a PNG image. Content-Type: " +
//                contentType);
//                //}
//
//                var contentLength = headResponse.headers().firstValueAsLong("Content-Length").orElse(-1L);
//                if (contentLength > MAX_IMAGE_SIZE) {
//                    throw new IllegalArgumentException("Image size exceeds maximum allowed size of " +
//                    MAX_IMAGE_SIZE + " bytes. Actual size: " + contentLength);
//                }

                var getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
                var response = HTTP_CLIENT.send(getRequest, limitedBodyHandler(maxFileSizeBytes));
                var imageData = response.body();
                var savedEntry = fileSystemCacheService.saveByName(url, Type.IMAGE, imageData);
                return savedEntry.getFilePath();
            } catch (Exception e) {
                throw new RuntimeException("Failed to download image from: " + url, e);
            }
        });
    }

    /**
     * Reads the image and returns the image size.
     *
     * @param imageData Image Data
     *
     * @return {@link Vector2d} where x is the width and y is the height
     *
     * @throws IOException If error occurs while reading the image
     */
    public Vector2d getImageSize(byte[] imageData) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            var image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("Failed to read image data");
            }
            return new Vector2d(image.getWidth(), image.getHeight());
        }
    }

    /**
     * Converts the file to PNG if it's not. Loads the image from the file system
     *
     * @param filePath File path
     *
     * @return Image data
     *
     * @throws IOException Exception if checking the image format, converting the image or writing the converted image
     */
    public byte[] convertToPngOrLoad(Path filePath) throws IOException {
        byte[] fileData = Files.readAllBytes(filePath);

        try (var inputStream = new ByteArrayInputStream(fileData)) {
            var readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(inputStream));

            if (readers.hasNext()) {
                var reader = readers.next();
                String formatName = reader.getFormatName().toLowerCase();
                reader.dispose();

                // File is png, return the data w/o conversion
                if (IMAGE_FORMAT_PNG.equals(formatName)) {
                    return fileData;
                }
            }
        }

        // File is not png, convert, save and return
        byte[] pngData;
        try (var inputStream = new ByteArrayInputStream(fileData)) {
            var image = ImageIO.read(inputStream);

            if (image == null) {
                throw new IOException("Failed to read image data from " + filePath);
            }

            try (var baos = new ByteArrayOutputStream()) {
                if (!ImageIO.write(image, IMAGE_FORMAT_PNG, baos)) {
                    throw new IOException("Failed to write PNG image");
                }
                pngData = baos.toByteArray();
            }
        }

        Files.write(filePath, pngData);
        return pngData;
    }

    private static HttpResponse.BodyHandler<byte[]> limitedBodyHandler(long maxBytes) {
        return responseInfo -> {
            var contentLength = responseInfo.headers().firstValueAsLong("Content-Length").orElse(-1L);
            if (contentLength > maxBytes) {
                throw new RuntimeException("Content-Length " + contentLength + " exceeds limit of " + maxBytes + " bytes");
            }
            return limitedBodySubscriber(maxBytes);
        };
    }

    private static HttpResponse.BodySubscriber<byte[]> limitedBodySubscriber(long maxBytes) {
        var delegate = HttpResponse.BodySubscribers.ofByteArray();
        return new HttpResponse.BodySubscriber<>() {
            private final AtomicLong received = new AtomicLong(0);
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                delegate.onSubscribe(subscription);
            }

            @Override
            public void onNext(List<ByteBuffer> buffers) {
                long chunk = buffers.stream().mapToLong(ByteBuffer::remaining).sum();
                if (received.addAndGet(chunk) > maxBytes) {
                    subscription.cancel();
                    delegate.onError(new RuntimeException("Response body exceeds limit of " + maxBytes + " bytes"));
                    return;
                }
                delegate.onNext(buffers);
            }

            @Override
            public void onError(Throwable throwable) {
                delegate.onError(throwable);
            }

            @Override
            public void onComplete() {
                delegate.onComplete();
            }

            @Override
            public CompletableFuture<byte[]> getBody() {
                return delegate.getBody().toCompletableFuture();
            }
        };
    }
}
