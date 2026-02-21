package enterprises.iwakura.docs.object;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

import lombok.Data;

@Data
public class PlayerRuntimeImageAssets {

    private final UUID playerUuid;
    private final ConcurrentSkipListMap<Integer, UUID> indexToFileId = new ConcurrentSkipListMap<>();
    private final Set<Integer> pendingIndexes = Collections.synchronizedSet(new HashSet<>());

    /**
     * Gets the next available index to be used when registering image assets for this player. Returns null if
     * the player has used up all indexes.
     *
     * @param maxSize Maximum index
     *
     * @return Nullable available index
     */
    public Integer getNextAvailableIndex(int maxSize) {
        int expected = 1;

        synchronized (indexToFileId) {
            for (Integer key : indexToFileId.keySet()) {
                if (key == expected) {
                    expected++;
                } else if (key > expected && expected < maxSize) {
                    return expected; // Return first gap
                }
            }

            if (expected >= maxSize) {
                return null;
            } else {
                return expected;
            }
        }
    }

    public Integer getIndexByFileId(UUID id) {
        synchronized (indexToFileId) {
            for (Map.Entry<Integer, UUID> entry : indexToFileId.entrySet()) {
                if (entry.getValue().equals(id)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public void removeFileIds(List<UUID> fileIdsToRemove) {
        synchronized (indexToFileId) {
            indexToFileId.entrySet().removeIf(entry -> fileIdsToRemove.contains(entry.getValue()));
        }
    }
}
