package enterprises.iwakura.docs.api.hytalemodding.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HMWikiPageContentResponse extends HMWikiResponse {

    @SerializedName("content")
    private String markdownContent;
    private HMWikiPage parent;
    private List<HMWikiPage> children = new ArrayList<>();

}
