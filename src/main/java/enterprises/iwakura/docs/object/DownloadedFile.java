package enterprises.iwakura.docs.object;

import java.nio.file.Path;
import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class DownloadedFile {

    private final String url;
    private final Path path;
    private OffsetDateTime lastUsedAt = OffsetDateTime.now();

}
