package enterprises.iwakura.docs.object;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import enterprises.iwakura.docs.object.CacheIndex.Entry.CacheFileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
public class CacheIndex {

    private final Map<String, Entry> nameToFileId = Collections.synchronizedMap(new HashMap<>());

    public int size() {
        return nameToFileId.size();
    }

    public Optional<Entry> getEntryByName(String fileName) {
        return Optional.ofNullable(nameToFileId.get(fileName));
    }

    public Entry createEntry(String fileName, CacheFileType cacheFileType) {
        return getEntryByName(fileName)
            .map(entry -> entry.updateCreatedAt(OffsetDateTime.now()))
            .orElseGet(() -> {
                var entry = new Entry(UUID.randomUUID(), cacheFileType, OffsetDateTime.now());
                nameToFileId.put(fileName, entry);
                return entry;
            });
    }

    @Data
    @AllArgsConstructor
    public static class Entry {

        private final UUID fileId;
        private final CacheFileType cacheFileType;
        private OffsetDateTime createdAt;

        public Entry updateCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Getter
        @RequiredArgsConstructor
        public enum CacheFileType {
            IMAGE(86400L),
            HYTALE_MODDING_WIKI_MOD_LIST(86400L),
            HYTALE_MODDING_WIKI_MOD(86400L),
            HYTALE_MODDING_WIKI_PAGE_CONTENT(86400L);

            private final Long defaultTtlSeconds;
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
