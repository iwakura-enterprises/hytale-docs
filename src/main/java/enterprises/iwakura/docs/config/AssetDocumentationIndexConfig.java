package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.List;

import enterprises.iwakura.docs.service.loader.UniversalDocumentationLoader;
import lombok.Data;

/**
 * @deprecated in favor of {@link UniversalDocumentationLoader}'s ability to load plugin's resource files
 * as from a normal file system.
 */
@Data
@Deprecated
public class AssetDocumentationIndexConfig {

    private final List<AssetDocumentationConfig> documentations = new ArrayList<>();

}
