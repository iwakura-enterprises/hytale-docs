package enterprises.iwakura.docs.api.hytalemodding.response;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import enterprises.iwakura.docs.api.hytalemodding.objects.HMWikiMod;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HMWikiModListResponse extends HMWikiResponse {

    private List<HMWikiMod> mods = new ArrayList<>();

    public static class GsonDeserializer implements JsonDeserializer<HMWikiModListResponse> {

        @Override
        public HMWikiModListResponse deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
        ) throws JsonParseException {
            HMWikiModListResponse response = new HMWikiModListResponse();
            List<HMWikiMod> mods = new ArrayList<>();

            for (JsonElement element : jsonElement.getAsJsonArray()) {
                mods.add(jsonDeserializationContext.deserialize(element, HMWikiMod.class));
            }

            response.setMods(mods);
            return response;
        }
    }
}
