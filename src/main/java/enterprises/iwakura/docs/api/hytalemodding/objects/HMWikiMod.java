package enterprises.iwakura.docs.api.hytalemodding.objects;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class HMWikiMod {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private User author;
    @SerializedName("index")
    private HMWikiPage indexPage;
    @SerializedName("created_at")
    private OffsetDateTime createdAt;
    @SerializedName("updated_at")
    private OffsetDateTime updatedAt;

    @Override
    public String toString() {
        return "HMWikiMod{" +
            "id=" + id +
            ", slug='" + slug + '\'' +
            '}';
    }

    @Data
    public static class User {

        private String name;
        private String username;
        @SerializedName("avatar_url")
        private String avatarUrl;
    }
}
