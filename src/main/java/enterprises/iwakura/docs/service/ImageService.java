package enterprises.iwakura.docs.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import com.hypixel.hytale.math.vector.Vector2d;

import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.object.DownloadedFile;
import enterprises.iwakura.docs.object.RuntimeImageAsset;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class ImageService {

    public static final String RUNTIME_IMAGES_DIRECTORY = "runtime_images";
    public static final String IMAGE_FORMAT_PNG = "png";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static final Map<String, DownloadedFile> DOWNLOAD_URL_TO_FILE = Collections.synchronizedMap(new HashMap<>());
    private static final Timer timer = new Timer();

    private final ConfigurationService configurationService;
    private final Logger logger;
    private final DocsPlugin plugin;

    public void init() {
        logger.info("Initializing ImageDownloaderService...");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    clearOldDownloadedImages();
                } catch (Exception exception) {
                    logger.error("Failed to clear old downloaded images!", exception);
                }
            }
        }, 0, 10_000);

        var runtimeImagesDirectory = getRuntimeImagesDirectory();
        if (!Files.exists(runtimeImagesDirectory)) {
            try {
                Files.createDirectories(runtimeImagesDirectory);
            } catch (Exception exception) {
                throw new RuntimeException("Failed to create runtime images directory: " + runtimeImagesDirectory, exception);
            }
        }

        try (var filesInDirectory = Files.walk(runtimeImagesDirectory, 1)) {
            logger.info("Deleting old runtime images in " + runtimeImagesDirectory);
            filesInDirectory.filter(Files::isRegularFile).forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (Exception exception) {
                    logger.error("Failed to delete old runtime image: " + file, exception);
                }
            });
        } catch (Exception exception) {
            logger.error("Failed to clear runtime images directory: " + runtimeImagesDirectory, exception);
        }
    }

    /**
     * Clears old downloaded images from file system
     */
    private void clearOldDownloadedImages() {
        var docsConfig = configurationService.getDocsConfig();
        var minimumLastUsedAt = OffsetDateTime.now().minusSeconds(
            docsConfig.getRuntimeImageAssets().getDownloadedImagesTimeToLiveSeconds()
        );

        var runtimeImageAssets = DOWNLOAD_URL_TO_FILE.values();
        var filesToDelete = runtimeImageAssets.stream()
            .filter(runtimeImageAsset -> runtimeImageAsset.getLastUsedAt().isBefore(minimumLastUsedAt))
            .toList();

        if (!filesToDelete.isEmpty()) {
            logger.info("Deleting %d downloaded images from file system".formatted(filesToDelete.size()));
            filesToDelete.forEach(file -> DOWNLOAD_URL_TO_FILE.remove(file.getUrl()));
            filesToDelete.forEach(file -> {
                try {
                    Files.deleteIfExists(file.getPath());
                } catch (IOException exception) {
                    logger.error("Failed to remove downloaded image %s from file system!".formatted(
                        file.getPath()
                    ), exception);
                }
            });
        }
    }

    /**
     * Clears ImageService's cache
     */
    public void clearCache() {
        logger.info("Clearing ImageService's downloaded files cache...");
        DOWNLOAD_URL_TO_FILE.clear();
    }

    private Path getRuntimeImagesDirectory() {
        return plugin.getDataDirectory().resolve(RUNTIME_IMAGES_DIRECTORY);
    }

    public CompletableFuture<Path> downloadImageFrom(String url) {
        var maxFileSizeBytes = configurationService.getDocsConfig().getRuntimeImageAssets().getMaxImageDownloadFileSizeKb() * 1024;

        synchronized (DOWNLOAD_URL_TO_FILE) {
            var existingDownloadedFile = DOWNLOAD_URL_TO_FILE.get(url);
            if (existingDownloadedFile != null && Files.exists(existingDownloadedFile.getPath())) {
                existingDownloadedFile.setLastUsedAt(OffsetDateTime.now());
                return CompletableFuture.completedFuture(existingDownloadedFile.getPath());
            }
            DOWNLOAD_URL_TO_FILE.remove(url);
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
                var response = HTTP_CLIENT.send(getRequest, HttpResponse.BodyHandlers.ofInputStream());

                var filePath = getRuntimeImagesDirectory().resolve(UUID.randomUUID() + ".png");

                try (var inputStream = response.body();
                    // Creates & truncates existing files
                    var outputStream = Files.newOutputStream(filePath)) {

                    byte[] buffer = new byte[8192];
                    long totalBytesDownloaded = 0;
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytesDownloaded += bytesRead;

                        if (totalBytesDownloaded > maxFileSizeBytes) {
                            Files.deleteIfExists(filePath);
                            throw new IllegalArgumentException(
                                "Image size exceeded maximum allowed size of " + maxFileSizeBytes / 1024
                                    + " kB during download");
                        }

                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                DOWNLOAD_URL_TO_FILE.put(url, new DownloadedFile(url, filePath));
                return filePath;
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
}
