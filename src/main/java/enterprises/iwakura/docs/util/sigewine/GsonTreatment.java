package enterprises.iwakura.docs.util.sigewine;

import java.lang.reflect.Type;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import enterprises.iwakura.sigewine.core.annotations.Bean;

public class GsonTreatment {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
            .create();
    }

    public static class LocalDateSerializer implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
        ) throws JsonParseException {
            return LocalDate.parse(jsonElement.getAsString());
        }

        @Override
        public JsonElement serialize(
            LocalDate localDate,
            Type type,
            JsonSerializationContext jsonSerializationContext
        ) {
            return jsonSerializationContext.serialize(localDate.toString());
        }
    }
}
