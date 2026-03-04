package enterprises.iwakura.docs.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.google.gson.Gson;

import dev.mayuna.mayusjsonutils.ObjectLoader;
import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.object.CacheIndex;
import enterprises.iwakura.docs.object.CacheIndex.Entry.Type;
import enterprises.iwakura.docs.object.CacheIndex.LoadedEntry;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class FileSystemCacheService {

    public static final String CACHE_DIRECTORY_NAME = "cache";
    public static final String CACHE_INDEX_FILE_NAME = "index.json";
    public static final String CACHE_FILE_FORMAT = "%s.voile";

    private static final Timer timer = new Timer();

    private final ConfigurationService configurationService;

    private final Gson gson;
    private final DocsPlugin plugin;
    private final Logger logger;

    private CacheIndex cacheIndex;

    /**
     * Initializes FileSystemCacheService
     */
    public void init() {
        logger.info("Initializing FileSystemCacheService at " + getCacheDirectory());
        reload();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    refresh(false);
                } catch (Exception exception) {
                    logger.error("Failed to refresh the cache index", exception);
                }
            }
        }, 0, 60_000 * 10); // 10 minutes
    }

    public void reload() {
        var cacheDirectory = getCacheDirectory();
        if (!Files.exists(cacheDirectory)) {
            try {
                Files.createDirectories(cacheDirectory);
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to create cache directory at " + cacheDirectory, exception);
            }
        }

        var cacheIndexPath = cacheDirectory.resolve(CACHE_INDEX_FILE_NAME);
        try {
            cacheIndex = ObjectLoader.loadOrCreateFrom(CacheIndex.class, cacheIndexPath, gson);
        } catch (IOException loadException) {
            logger.error("Failed to load cache index at %s, creating new one...".formatted(cacheIndexPath), loadException);
            cacheIndex = new CacheIndex();

            try {
                ObjectLoader.saveTo(cacheIndex, cacheIndexPath);
            } catch (IOException saveException) {
                logger.error("Failed to save cache index to %s after failing to load it!".formatted(cacheIndexPath), saveException);
            }
        }

        logger.info("Loaded %d file system cache entries".formatted(cacheIndex.size()));
        refresh(true);
    }

    /**
     * Gets the cached file by its name from the cache directory. Returning empty optional if not found. If there's a
     * file with the name but the type does not equal, empty optional is returned.
     *
     * @param fileName File name
     * @param type Type of the file
     * @param <T> Type of the file. If specifies byte array or String, it is directly returned without any serialization.
     * @return Optional of T
     */
    public <T> Optional<LoadedEntry<T>> getByName(String fileName, Type type) {
        return cacheIndex.getEntryByName(fileName)
            .filter(entry -> entry.getType() == type)
            .map(entry -> {
                var filePath = getCacheFilePath(entry.getFileId());

                if (Files.exists(filePath)) {
                    return new LoadedEntry<>(entry, filePath, null);
                } else {
                    logger.warn("File system cache index has file %s but it does not exist!".formatted(filePath));
                    cacheIndex.getNameToFileId().remove(fileName);
                }

                return null;
            });
    }

    /**
     * Loads the cached file by its name from the cache directory. Returning empty optional if not found or when it
     * could not be read. If there's a file with the name but the type does not equal, empty optional is returned.
     *
     * @param fileName File name
     * @param type Type of the file
     * @param <T> Type of the file. If specifies byte array or String, it is directly returned without any serialization.
     * @return Optional of T
     */
    public <T> Optional<LoadedEntry<T>> loadByName(String fileName, Type type, Class<T> clazz) {
        var loadedEntry = getByName(fileName, type)
            .map(entry -> {
                var filePath = entry.getFilePath();

                try {
                    var fileBytes = Files.readAllBytes(filePath);

                    if (clazz == byte[].class) {
                        entry.setData((T) fileBytes);
                    } else {
                        var string = new String(fileBytes, StandardCharsets.UTF_8);
                        if (clazz == String.class) {
                            //noinspection unchecked
                            entry.setData((T) string);
                        } else {
                            entry.setData(gson.fromJson(string, clazz));
                        }
                    }
                } catch (IOException readException) {
                    logger.error("Failed to read cache file at " + filePath, readException);
                    cacheIndex.getNameToFileId().remove(fileName);

                    try {
                        Files.deleteIfExists(filePath);
                    } catch (IOException deleteException) {
                        logger.error("Failed to delete invalid cache file at " + filePath, deleteException);
                    }
                }

                return entry;
            }).orElse(null);

        if (loadedEntry == null) {
            return Optional.empty();
        } else {
            //noinspection unchecked
            return Optional.of((LoadedEntry<T>) loadedEntry);
        }
    }

    /**
     * Saves data into cache file by the specified name and type. Updates currently existing entry, if exists. If data
     * is of byte array or string, it is written without any serialization.
     *
     * @param fileName File name
     * @param type     File type
     * @param data     Data
     * @param <T> The class of the date
     * @return The created loaded entry, without the data serialized
     */
    public <T> LoadedEntry<T> saveByName(String fileName, Type type, T data) {
        var entry = cacheIndex.createEntry(fileName, type);
        var filePath = getCacheFilePath(entry.getFileId());
        byte[] bytes;

        if (data instanceof byte[] dataBytes) {
            bytes = dataBytes;
        } else {
            String dataString;

            if (data instanceof String string) {
                dataString = string;
            } else {
                dataString = gson.toJsonTree(data).toString();
            }

            bytes = dataString.getBytes(StandardCharsets.UTF_8);
        }

        try {
            Files.write(filePath, bytes);
            return new LoadedEntry<>(entry, filePath, null);
        } catch (IOException exception) {
            cacheIndex.getNameToFileId().remove(fileName);
            throw new IllegalStateException("Failed to create cache file at %s with name %s (%s)".formatted(
                filePath, fileName, type), exception
            );
        } finally {
            saveCacheIndex();
        }
    }

    private void saveCacheIndex() {
        var indexFilePath = getCacheDirectory().resolve(CACHE_INDEX_FILE_NAME);
        try {
            ObjectLoader.saveTo(cacheIndex, indexFilePath, gson);
        } catch (IOException exception) {
            logger.error("Failed to save cache index to " + indexFilePath, exception);
        }
    }

    private void refresh(boolean verbose) {
        var config = configurationService.getDocsConfig().getFileSystemCache();
        if (verbose) {
            logger.info("Refreshing the file system cache...");
        }

        var now = OffsetDateTime.now();
        var fileNamesToRemove = new ArrayList<String>();

        cacheIndex.getNameToFileId().forEach((fileName, entry) -> {
            var filePath = getCacheFilePath(entry.getFileId());

            if (!Files.exists(filePath) || entry.getType() == null || entry.getCreatedAt() == null) {
                logger.warn("Could not find / invalid cache file " + filePath);
                fileNamesToRemove.add(fileName);
            } else if (entry.getCreatedAt().isBefore(now.minusSeconds(config.getCacheTypeTimeToLiveSeconds().get(entry.getType())))) {
                logger.warn("Removing old cache file " + filePath);
                fileNamesToRemove.add(fileName);

                try {
                    Files.deleteIfExists(filePath);
                } catch (Exception exception) {
                    logger.error("Failed to delete old cache file " + filePath, exception);
                }
            }
        });

        if (!fileNamesToRemove.isEmpty()) {
            logger.warn("Found %d old / invalid file system cache entries".formatted(fileNamesToRemove.size()));
            fileNamesToRemove.forEach(name -> cacheIndex.getNameToFileId().remove(name));
        }

        // TODO: Check for dangling files

        if (verbose || !fileNamesToRemove.isEmpty()) {
            logger.info("Total file system cache size: " + cacheIndex.size());
        }
    }

    private Path getCacheDirectory() {
        return plugin.getDataDirectory().resolve(CACHE_DIRECTORY_NAME);
    }

    private Path getCacheFilePath(UUID id) {
        return getCacheDirectory().resolve(CACHE_FILE_FORMAT.formatted(id));
    }
}
