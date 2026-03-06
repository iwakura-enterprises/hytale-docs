package enterprises.iwakura.docs.api.hytalemodding;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;

import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiPageContentResponse;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModListResponse;
import enterprises.iwakura.docs.api.hytalemodding.response.HMWikiModResponse;
import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.Kirara;
import enterprises.iwakura.kirara.core.PathParameter;
import enterprises.iwakura.kirara.core.RequestHeader;
import enterprises.iwakura.kirara.gson.GsonSerializer;
import enterprises.iwakura.kirara.httpclient.HttpClientHttpCore;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class HMWikiApi extends Kirara {

    public static final String DEFAULT_URL = "https://wiki.hytalemodding.dev/api";

    /**
     * The Voile API key for Hytale Modding Wiki.
     * <p>
     * <b>If you're reading this, please don't abuse this API key. ᗜ˰ᗜ</b>
     * <p>
     * There are rate-limits applied to the key and all you can do is read public wiki pages.
     * Thanks!
     */
    public static final String DEFAULT_API_KEY = "cab23f56c4f65cda8078b8c284ed860ca78c188add76fa0bc3d88b034c8fda2f";
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(Version.HTTP_1_1)
        .build();

    private final ConfigurationService configurationService;
    private final Logger logger;

    public HMWikiApi(Gson gson, ConfigurationService configurationService, Logger logger) {
        super(new HttpClientHttpCore(HTTP_CLIENT), new GsonSerializer(gson));
        this.configurationService = configurationService;
        this.logger = logger;
    }

    public void init() {
        var config = configurationService.getDocsConfig().getIntegration().getHytaleModdingWiki();
        var apiUrl = Optional.ofNullable(config.getUrlOverride()).orElse(DEFAULT_URL);
        var apiToken = Optional.ofNullable(config.getApiTokenOverride()).orElse(DEFAULT_API_KEY);

        if (!apiUrl.equals(DEFAULT_URL)) {
            logger.warn("Using URL override for Hytale Modding Wiki API: " + apiUrl);
        }

        if (!apiToken.equals(DEFAULT_API_KEY)) {
            logger.warn("Using API token override for Hytale Modding Wiki API");
        }

        setApiUrl(apiUrl);
        setDefaultRequestHeaders(List.of(
            RequestHeader.of("Authorization", "Bearer " + apiToken),
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
