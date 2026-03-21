package enterprises.iwakura.docs.integration.hytalemodding.api.response;

import java.util.ArrayList;
import java.util.List;

import enterprises.iwakura.docs.integration.hytalemodding.api.objects.HMWikiMod;
import enterprises.iwakura.docs.integration.hytalemodding.api.objects.HMWikiPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HMWikiModResponse extends HMWikiResponse {

    private HMWikiMod mod;
    private List<HMWikiPage> pages = new ArrayList<>();

}
