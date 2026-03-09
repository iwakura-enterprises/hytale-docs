package enterprises.iwakura.docs.api.kirara;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import enterprises.iwakura.docs.service.SentryService;
import enterprises.iwakura.kirara.gson.GsonSerializer;

public class VoileGsonSerializer extends GsonSerializer {

    public VoileGsonSerializer(Gson gson) {
        super(gson);
    }

    @Override
    public <T> T deserialize(
        Class<T> specifiedResponseClass,
        int statusCode,
        Map<String, List<String>> headers,
        byte[] body
    ) {
        if (body == null || body.length == 0) {
            return null; // Return null for empty responses
        }

        List<String> contentType = headers.get("Content-Type");

        boolean supportsAnyContentType = contentType != null && contentType.stream().anyMatch(ct -> {
            for (String supportedContentType : supportedContentTypes) {
                if (ct.contains(supportedContentType)) {
                    return true;
                }
            }
            return false;
        });

        // Treat a response w/o Content-Type as JSON
        String stringResponse = new String(body, defaultCharset);

        if (contentType == null || contentType.isEmpty() || supportsAnyContentType) {
            try {
                return gson.fromJson(stringResponse, specifiedResponseClass);
            } catch (Exception exception) {
                throw new IllegalArgumentException(
                    "Failed to deserialize response to " + specifiedResponseClass.getName() + ": " + stringResponse,
                    exception);
            }
        }

        SentryService.addAttachment(stringResponse, "response.txt");
        throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
    }
}
