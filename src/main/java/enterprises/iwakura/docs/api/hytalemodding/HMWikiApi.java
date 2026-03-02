package enterprises.iwakura.docs.api.hytalemodding;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;

import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiPageContentResponse;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModListResponse;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModResponse;
import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.Kirara;
import enterprises.iwakura.kirara.core.PathParameter;
import enterprises.iwakura.kirara.core.RequestHeader;
import enterprises.iwakura.kirara.gson.GsonSerializer;
import enterprises.iwakura.kirara.httpclient.HttpClientHttpCore;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class HMWikiApi extends Kirara {

    /**
     * The Voile API key for Hytale Modding Wiki.
     * <p>
     * <b>If you're reading this, please don't abuse this API key. ᗜ˰ᗜ</b>
     * <p>
     * There are rate-limits applied to the key and all you can do is read public wiki pages.
     * Thanks!
     */
    public static final String API_KEY = "086e1b4b7715c0b82eed27601362be5732f102bcd7b13a82c0c53f75b0227895";
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(Version.HTTP_1_1)
        .build();

    public HMWikiApi(Gson gson) {
        super(new HttpClientHttpCore(HTTP_CLIENT), new GsonSerializer(gson));
        setApiUrl("https://dev.wiki.hytalemodding.dev/api");
        setDefaultRequestHeaders(List.of(
            RequestHeader.of("Authorization", "Bearer " + API_KEY),
            RequestHeader.of("User-Agent", "Voile/" + enterprises.iwakura.docs.Version.VERSION)
        ));
    }

    public ApiRequest<HMWikiModListResponse> fetchModList() {
        return this.createRequest("GET", "/mods", HMWikiModListResponse.class);
    }

    public ApiRequest<HMWikiModResponse> fetchMod(UUID modId) {
        return this.createRequest("GET", "/mods/{mod-id}", HMWikiModResponse.class)
            .withPathParameter(PathParameter.of("mod-id", modId.toString()));
    }

    public ApiRequest<HMWikiPageContentResponse> fetchPageContent(UUID modId, String pageSlug) {
        return this.createRequest("GET", "/mods/{mod-id}/{page-slug}", HMWikiPageContentResponse.class)
            .withPathParameter(PathParameter.of("mod-id", modId.toString()))
            .withPathParameter(PathParameter.of("page-slug", pageSlug));
    }
}
