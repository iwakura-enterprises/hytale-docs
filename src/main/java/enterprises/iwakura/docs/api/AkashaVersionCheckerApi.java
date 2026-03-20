package enterprises.iwakura.docs.api;

import enterprises.iwakura.kirara.core.ApiRequest;
import enterprises.iwakura.kirara.core.Kirara;
import enterprises.iwakura.kirara.core.impl.StringSerializer;
import enterprises.iwakura.kirara.httpclient.HttpClientHttpCore;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class AkashaVersionCheckerApi extends Kirara {

    public AkashaVersionCheckerApi() {
        super(new HttpClientHttpCore(), new StringSerializer());
        setApiUrl("https://akasha.iwakura.enterprises/data-source/hetzner/public/versions/voile.txt");
    }

    public ApiRequest<String> fetch() {
        return this.createRequest("GET", "", String.class);
    }
}
