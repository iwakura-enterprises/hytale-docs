package enterprises.iwakura.docs.object;

import java.util.List;

public class InternalTopic extends Topic {

    public InternalTopic(String idSuffix, String name, String description, String content) {
        super(
            "internal_" + idSuffix,
            name,
            description,
            "Voile",
            LocaleType.ENGLISH,
            0,
            false,
            content,
            new Documentation(
                "voile",
                "internal_voile_documentation",
                "Internal",
                DocumentationType.INTERNAL,
                0
            ),
            List.of()
        );
    }
}
