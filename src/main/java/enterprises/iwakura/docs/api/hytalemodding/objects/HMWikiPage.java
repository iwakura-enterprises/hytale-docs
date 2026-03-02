package enterprises.iwakura.docs.api.hytalemodding.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class HMWikiPage {

    private UUID id;
    private String title;
    private String slug;
    private List<HMWikiPage> children = new ArrayList<>();
    private HMWikiPage parent;
}
