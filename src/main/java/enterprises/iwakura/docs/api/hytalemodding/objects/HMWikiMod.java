package enterprises.iwakura.docs.api.hytalemodding.objects;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class HMWikiMod {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    @SerializedName("index")
    private HMWikiPage indexPage;
    @SerializedName("created_at")
    private OffsetDateTime createdAt;
    @SerializedName("updated_at")
    private OffsetDateTime updatedAt;

}
