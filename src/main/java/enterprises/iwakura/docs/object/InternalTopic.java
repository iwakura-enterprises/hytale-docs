package enterprises.iwakura.docs.object;

public class InternalTopic extends Topic {

    public InternalTopic(String idSuffix, String name, String description, String content) {
        super(
            "internal_" + idSuffix,
            name,
            description,
            "Voile",
            0,
            content,
            null,
            null,
            null
        );
    }
}
