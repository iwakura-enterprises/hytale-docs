package enterprises.iwakura.docs.integration.hytalemodding.api.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class HMWikiPage {

    private UUID id;
    private String title;
    private String slug;
    private String kind;
    private List<HMWikiPage> children = new ArrayList<>();
    private HMWikiPage parent;

    public boolean isCategory() {
        return kind != null && kind.equals("category");
    }

    @Override
    public String toString() {
        return "HMWikiPage{" +
            "id=" + id +
            ", slug='" + slug + '\'' +
            '}';
    }
}
