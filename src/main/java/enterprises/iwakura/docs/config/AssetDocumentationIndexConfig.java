package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AssetDocumentationIndexConfig {

    private final List<AssetDocumentationConfig> documentations = new ArrayList<>();

}
