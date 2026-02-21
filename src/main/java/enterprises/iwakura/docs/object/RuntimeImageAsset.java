package enterprises.iwakura.docs.object;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.hypixel.hytale.math.vector.Vector2d;

import lombok.Data;

/**
 * Runtime asset that points to a filesystem file to preserve memory
 */
@Data
public class RuntimeImageAsset {

    private final UUID id = UUID.randomUUID();
    private final Path filePath;
    private final Vector2d imageSize;
    private final byte[] data;
    private OffsetDateTime lastUsedAt = OffsetDateTime.now();

}
