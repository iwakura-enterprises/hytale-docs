package enterprises.iwakura.docs.object;

import java.util.UUID;

import lombok.Data;

@Data
public class LastValidationEntry {

    private final UUID playerUuid;
    private final Long createdAt = System.currentTimeMillis();
    private final String dump;

}
