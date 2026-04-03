package enterprises.iwakura.docs.util;

import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InterfaceUtils {

    /**
     * Generates selector
     *
     * @return Selector (without hashtag)
     */
    public static String generateSelector() {
        return "Generated" + UUID.randomUUID().toString().replace("-", "");
    }
}
