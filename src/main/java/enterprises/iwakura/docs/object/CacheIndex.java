package enterprises.iwakura.docs.object;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import enterprises.iwakura.docs.object.CacheIndex.Entry.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CacheIndex {

    private final Map<String, Entry> nameToFileId = Collections.synchronizedMap(new HashMap<>());

    public int size() {
        return nameToFileId.size();
    }

    public Optional<Entry> getEntryByName(String fileName) {
        return Optional.ofNullable(nameToFileId.get(fileName));
    }

    public Entry createEntry(String fileName, Type type) {
        return getEntryByName(fileName)
            .map(entry -> entry.updateCreatedAt(OffsetDateTime.now()))
            .orElseGet(() -> {
                var entry = new Entry(UUID.randomUUID(), type, OffsetDateTime.now());
                nameToFileId.put(fileName, entry);
                return entry;
            });
    }

    @Data
    @AllArgsConstructor
    public static class Entry {

        private final UUID fileId;
        private final Type type;
        private OffsetDateTime createdAt;

        public Entry updateCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public enum Type {
            IMAGE,
            HYTALE_MODDING_MOD_INDEX,
            HYTALE_MODDING_PAGE_CONTENT
        }
    }

    @Data
    @AllArgsConstructor
    public static class LoadedEntry<T> {

        private final Entry entry;
        private final Path filePath;
        private T data;

    }
}
