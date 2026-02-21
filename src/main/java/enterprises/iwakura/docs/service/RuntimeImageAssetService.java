package enterprises.iwakura.docs.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.setup.AssetFinalize;
import com.hypixel.hytale.protocol.packets.setup.AssetInitialize;
import com.hypixel.hytale.protocol.packets.setup.AssetPart;
import com.hypixel.hytale.protocol.packets.setup.RequestCommonAssetsRebuild;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.object.PlayerRuntimeImageAssets;
import enterprises.iwakura.docs.object.ResolvedImageCommonAsset;
import enterprises.iwakura.docs.object.RuntimeImageAsset;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class RuntimeImageAssetService {

    public static final String RUNTIME_IMAGE_RESOURCE_PATH = "UI/Custom/Docs/Images/Runtime/runtime_image_%03d.png";
    public static final String IMAGE_NOT_FOUND_PATH = "UI/Custom/Docs/Images/image_not_found.png";
    public static final String IMAGES_DISABLED_PATH = "UI/Custom/Docs/Images/images_disabled.png";
    public static final Vector2d IMAGE_NOT_FOUND_SIZE = new Vector2d(251, 64);

    public static final int RUNTIME_ASSET_COUNT = 100;
    public static final int MAX_PACKET_SIZE = 2621440;

    private static final Map<Integer, CommonAsset> COMMON_ASSET_MAP = Collections.synchronizedMap(new HashMap<>());
    private static final Map<UUID, RuntimeImageAsset> RUNTIME_IMAGE_ASSET_MAP = Collections.synchronizedMap(new HashMap<>()); // UUID is RuntimeImageAsset.id
    private static final Map<UUID, PlayerRuntimeImageAssets> PLAYER_RUNTIME_IMAGE_ASSETS_MAP = Collections.synchronizedMap(new HashMap<>()); // UUID is playerRef.uuid
    private static final Timer timer = new Timer();

    private final ConfigurationService configurationService;
    private final ImageService imageService;
    private final DocsPlugin plugin;
    private final Logger logger;

    /**
     * Initializes RuntimeImageAssetService
     */
    public void init() {
        logger.info("Initializing RuntimeImageAssetService...");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    clearOldRuntimeImageAssets();
                } catch (Exception exception) {
                    logger.error("Failed to clear old runtime image assets!", exception);
                }
            }
        }, 0, 10_000);

        for (int x = 0; x < RUNTIME_ASSET_COUNT; x++) {
            var commonAsset = CommonAssetRegistry.getByName(RUNTIME_IMAGE_RESOURCE_PATH.formatted(x));

            if (commonAsset != null) {
                COMMON_ASSET_MAP.put(x, commonAsset);
            } else {
                logger.warn("Runtime image resource for index %d was not found! (%s)".formatted(
                    x, RUNTIME_IMAGE_RESOURCE_PATH.formatted(x)
                ));
            }
        }

        logger.info("Loaded %d runtime assets".formatted(COMMON_ASSET_MAP.size()));
    }

    /**
     * Clears old runtime image assets
     */
    private void clearOldRuntimeImageAssets() {
        var docsConfig = configurationService.getDocsConfig();
        var minimumLastUsedAt = OffsetDateTime.now().minusSeconds(
            docsConfig.getRuntimeImageAssets().getInMemoryTimeToLiveSeconds()
        );

        var runtimeImageAssets = RUNTIME_IMAGE_ASSET_MAP.values();
        var fileIdsToRemove = runtimeImageAssets.stream()
            .filter(runtimeImageAsset -> runtimeImageAsset.getLastUsedAt().isBefore(minimumLastUsedAt))
            .map(RuntimeImageAsset::getId)
            .toList();

        if (!fileIdsToRemove.isEmpty()) {
            logger.info("Removing %d runtime image assets from memory".formatted(fileIdsToRemove.size()));
            fileIdsToRemove.forEach(RUNTIME_IMAGE_ASSET_MAP::remove);
            PLAYER_RUNTIME_IMAGE_ASSETS_MAP.values().forEach(playerRuntimeImageAssets ->
                playerRuntimeImageAssets.removeFileIds(fileIdsToRemove));
        }
    }

    /**
     * Clears cache for specified player
     *
     * @param playerUuid Player UUID
     */
    public void clearCacheForPlayer(UUID playerUuid) {
        logger.info("Unloading runtime images for player " + playerUuid);
        PLAYER_RUNTIME_IMAGE_ASSETS_MAP.remove(playerUuid);
    }

    /**
     * Clears runtime image asset maps
     */
    public void clearCache() {
        logger.info("Clearing RuntimeImageAssetService's cache... All players will receive all runtime image assets as if "
            + "they have never received them before.");
        RUNTIME_IMAGE_ASSET_MAP.clear();
        PLAYER_RUNTIME_IMAGE_ASSETS_MAP.clear();
    }

    /**
     * Returns a path to the image specified by the image source
     *
     * @param imageSource   Image source
     * @param topicFilePath Topic file path, used when searching for relative image sources to the topic file
     *
     * @return Nullable path to specified image
     */
    public Path getFileSystemAssetPath(String imageSource, Path topicFilePath) {
        var dataDirectory = plugin.getDataDirectory();
        // First check relative to the topic file
        if (topicFilePath != null) {
            var topicFileDirectory = topicFilePath.getParent();
            var relativeImageSourcePath = topicFileDirectory.resolve(imageSource);

            if (relativeImageSourcePath.startsWith(dataDirectory)) {
                if (Files.exists(relativeImageSourcePath)) {
                    return relativeImageSourcePath;
                }
            }
        }

        // Not found by relative path / relative path out of data directory, check
        // from root data path
        var rootImageSource = dataDirectory.resolve(imageSource);

        if (!rootImageSource.startsWith(dataDirectory)) {
            throw new SecurityException("Image path points outside data directory: %s (%s)".formatted(imageSource, rootImageSource));
        }

        if (Files.exists(rootImageSource)) {
            return rootImageSource;
        }

        return null;
    }

    /**
     * Resolves the image source for the player. Image source can link to a resource image,
     * file system image (in plugin's data directory) or online (starts with http)
     *
     * @param playerRef   Player reference. Runtime images are stored per-player
     * @param imageSource image source
     * @param topicFilePath topic path to apply with the image source when searching for file system images
     *
     * @return Never-null ResolvedImageCommonAsset, if image is not found, it references NOT FOUND image.
     */
    public ResolvedImageCommonAsset resolve(PlayerRef playerRef, String imageSource, Path topicFilePath) {
        if (!configurationService.getDocsConfig().getRuntimeImageAssets().isEnabled()) {
            var imagesDisabledCommonAsset = CommonAssetRegistry.getByName(IMAGES_DISABLED_PATH);
            if (imagesDisabledCommonAsset != null) {
                return new ResolvedImageCommonAsset(imagesDisabledCommonAsset.getName(), IMAGE_NOT_FOUND_SIZE);
            } else {
                throw new IllegalStateException("Could not found %s! Try restarting the server.".formatted(
                    IMAGES_DISABLED_PATH
                ));
            }
        }

        Vector2d imageSize = null;
        CommonAsset commonAsset = null;
        Path filePath = null;

        if (imageSource.startsWith("http")) {
            // online
            try {
                filePath = imageService.downloadImageFrom(imageSource).join();
            } catch (Exception exception) {
                logger.error("Failed to download image from " + imageSource, exception);
            }
        } else {
            // resources
            commonAsset = CommonAssetRegistry.getByName(imageSource);
            if (commonAsset != null) {
                try {
                    imageSize = imageService.getImageSize(commonAsset.getBlob().join());
                } catch (IOException exception) {
                    logger.error("Failed to read image size from resource image asset " + imageSource, exception);
                    commonAsset = null;
                }
            } else {
                // file system
                filePath = getFileSystemAssetPath(imageSource, topicFilePath);
            }
        }

        // Common asset loaded from file system; register it
        if (commonAsset == null && filePath != null) {
            var runtimeImageAsset = registerRuntimeImageAsset(filePath);
            if (runtimeImageAsset != null) {
                imageSize = runtimeImageAsset.getImageSize();
                commonAsset = registerForPlayer(playerRef, runtimeImageAsset);

                if (commonAsset == null) {
                    logger.error("Failed to register common asset %s for player %s!".formatted(
                        runtimeImageAsset.getId(), playerRef.getUuid()
                    ));
                }
            }
        }

        // Image not found
        if (commonAsset == null) {
            logger.warn("Image resource source %s not found in assets!".formatted(imageSource));
            commonAsset = CommonAssetRegistry.getByName(IMAGE_NOT_FOUND_PATH);
            if (commonAsset == null) {
                throw new IllegalStateException("Could not found %s! Try restarting the server.".formatted(
                    IMAGE_NOT_FOUND_PATH
                ));
            }
            imageSize = IMAGE_NOT_FOUND_SIZE;
        }

        return new ResolvedImageCommonAsset(commonAsset.getName(), imageSize);
    }

    /**
     * Registers runtime image asset from specified file path. If file does not exist or error occurs while
     * reading it, null is returned.
     *
     * @param filePath File path
     *
     * @return Nullable RuntimeImageAsset
     */
    public RuntimeImageAsset registerRuntimeImageAsset(Path filePath) {
        var existingRuntimeImageAsset = RUNTIME_IMAGE_ASSET_MAP.values().stream()
            .filter(asset -> asset.getFilePath().equals(filePath))
            .findFirst();

        if (existingRuntimeImageAsset.isPresent()) {
            return existingRuntimeImageAsset.get();
        }

        if (Files.exists(filePath)) {
            try {
                // FIXME: This reads the file two times, little optimization needed
                var pngFileBytes = imageService.convertToPngOrLoad(filePath);
                var imageSize = imageService.getImageSize(pngFileBytes);
                var runtimeImageAsset = new RuntimeImageAsset(filePath, imageSize, pngFileBytes);
                RUNTIME_IMAGE_ASSET_MAP.put(runtimeImageAsset.getId(), runtimeImageAsset);
                return runtimeImageAsset;
            } catch (IOException exception) {
                logger.error("Failed to register runtime image asset " + filePath, exception);
            }
        }

        return null;
    }

    /**
     * Registers the provided runtime image asset for player. Replaces existing runtime asset if it has expired.
     * @param playerRef Player reference
     * @param runtimeImageAsset Runtime Image Asset
     */
    public CommonAsset registerForPlayer(PlayerRef playerRef, RuntimeImageAsset runtimeImageAsset) {
        var playerRuntimeImageAssets = PLAYER_RUNTIME_IMAGE_ASSETS_MAP.computeIfAbsent(playerRef.getUuid(), PlayerRuntimeImageAssets::new);
        var existingIndex = playerRuntimeImageAssets.getIndexByFileId(runtimeImageAsset.getId());
        if (existingIndex != null) {
            // Use existing runtime asset that the player already has loaded
            runtimeImageAsset.setLastUsedAt(OffsetDateTime.now());
            return COMMON_ASSET_MAP.get(existingIndex);
        }

        var availableIndex = playerRuntimeImageAssets.getNextAvailableIndex(RUNTIME_ASSET_COUNT);
        if (availableIndex == null) {
            // Find the asset with the oldest lastUsedAt and replace it
            var oldestAssetEntry = playerRuntimeImageAssets.getIndexToFileId().entrySet().stream()
                .map(entry -> {
                    var asset = RUNTIME_IMAGE_ASSET_MAP.get(entry.getValue());
                    return asset != null ? Map.entry(entry.getKey(), asset) : null;
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparing(entry -> entry.getValue().getLastUsedAt()))
                .orElse(null);
            // Fallback to 0
            availableIndex = oldestAssetEntry != null ? oldestAssetEntry.getKey() : 0;
        }
        playerRuntimeImageAssets.getIndexToFileId().put(availableIndex, runtimeImageAsset.getId());
        playerRuntimeImageAssets.getPendingIndexes().add(availableIndex);
        runtimeImageAsset.setLastUsedAt(OffsetDateTime.now());
        return COMMON_ASSET_MAP.get(availableIndex);
    }

    /**
     * Checks if specified player has some pending assets to be sent
     *
     * @param playerRef Player reference
     *
     * @return True if yes, false otherwise
     */
    public boolean hasPendingAssets(PlayerRef playerRef) {
        return !PLAYER_RUNTIME_IMAGE_ASSETS_MAP.computeIfAbsent(playerRef.getUuid(), PlayerRuntimeImageAssets::new)
            .getPendingIndexes().isEmpty();
    }

    /**
     * Sends pending assets to specified player. Returns instantly if there are no pending assets.
     *
     * @param playerRef Player reference
     */
    public void sendPendingAssets(PlayerRef playerRef) {
        var playerRuntimeImageAsset = PLAYER_RUNTIME_IMAGE_ASSETS_MAP.computeIfAbsent(playerRef.getUuid(),
            PlayerRuntimeImageAssets::new);

        var pendingAssetIndexes = playerRuntimeImageAsset.getPendingIndexes();
        if (pendingAssetIndexes.isEmpty()) {
            return;
        }

        pendingAssetIndexes.forEach(index -> {
            var commonAsset = COMMON_ASSET_MAP.get(index);

            if (commonAsset != null) {
                var fileId = playerRuntimeImageAsset.getIndexToFileId().get(index);

                if (fileId != null) {
                    var runtimeImageAsset = RUNTIME_IMAGE_ASSET_MAP.get(fileId);

                    if (runtimeImageAsset != null) {
                        var fileContent = runtimeImageAsset.getData();

                        if (fileContent != null) {
                            if (fileContent.length > 0) {
                                // Use one of the common assets to send the runtime image asset
                                // Taken from CommonAssetModule#sendAssetsToPlayer()
                                byte[][] fileContentParts = ArrayUtil.split(fileContent, MAX_PACKET_SIZE);
                                ToClientPacket[] packets = new ToClientPacket[1 + fileContentParts.length];
                                packets[0] = new AssetInitialize(commonAsset.toPacket(), fileContent.length);

                                for(int packetPartIndex = 0; packetPartIndex < fileContentParts.length; ++packetPartIndex) {
                                    packets[1 + packetPartIndex] = new AssetPart(fileContentParts[packetPartIndex]);
                                }

                                playerRef.getPacketHandler().write(packets, new AssetFinalize());
                            } else {
                                logger.warn("The file content for file ID %s is empty when sending pending assets to player %s".formatted(
                                    fileId, playerRef.getUuid()
                                ));
                            }
                        } else {
                            logger.warn("The file content for file ID %s is null when sending pending assets to player %s".formatted(
                                fileId, playerRef.getUuid()
                            ));
                        }
                    } else {
                        logger.warn("Could not find runtime image asset for file ID %s for player %s as index %d when sending pending assets".formatted(
                            fileId, playerRef.getUuid(), index
                        ));
                    }
                } else {
                    logger.warn("Could not find file ID for index %d for player %s when sending pending assets".formatted(
                        index, playerRef.getUuid()
                    ));
                }
            } else {
                logger.warn("Could not find common asset for index %d when sending pending assets to player %s".formatted(
                    index, playerRef.getUuid()
                ));
            }
        });

        // TODO: Check if there's a way to rebuild only specific common assets
        playerRef.getPacketHandler().writeNoCache(new RequestCommonAssetsRebuild());
        playerRuntimeImageAsset.getPendingIndexes().clear(); // Always clear pending indexes
    }
}
