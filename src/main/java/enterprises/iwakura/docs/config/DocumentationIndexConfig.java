package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class DocumentationIndexConfig {

    private final List<DocumentationConfig> documentations = new ArrayList<>();

}
