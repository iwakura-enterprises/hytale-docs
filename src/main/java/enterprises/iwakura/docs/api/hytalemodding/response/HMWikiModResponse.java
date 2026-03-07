package enterprises.iwakura.docs.api.hytalemodding.response;

import java.util.ArrayList;
import java.util.List;

import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiMod;
import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HMWikiModResponse extends HMWikiResponse {

    private HMWikiMod mod;
    private List<HMWikiPage> pages = new ArrayList<>();

}
