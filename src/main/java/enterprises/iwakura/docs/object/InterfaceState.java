package enterprises.iwakura.docs.object;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Holds the state for the interface. Mutable instance. Same instance should always show the same interface, regardless
 * the player.
 */
@Data
public class InterfaceState {

    /**
     * Default state w/o documentations and topic. Is used when loading default settings.
     */
    public static final InterfaceState DEFAULT_STATE = new InterfaceState();

    /**
     * The topic identifier history as topics were opened. This is used to implement the back/forward button.
     */
    private final List<String> topicIdentifierHistory = new ArrayList<>();
    /**
     * Points to specific index in the history. -1 means no topic has been yet opened. If next topic is opened and
     * the index is not at the end of the history, all topics after the index are removed and the new topic is added to
     * the end of the history, and the index is moved to the end. If back button is pressed, the index is moved back by
     * one, and if forward button is pressed, the index is moved forward by one. The topic at the index is the currently
     * opened topic.
     */
    private int topicIdentifierHistoryIndex = -1;

    private InterfaceMode mode = InterfaceMode.VOILE;
    private LocaleType preferredLocale = LocaleType.CZECH;
    private List<Documentation> documentations;
    private Topic topic;

    private String topicSearchQuery;
    private boolean fullTextSearch;

    public InterfaceState(List<Documentation> documentations, Topic topic) {
        this.documentations = documentations;
        this.topic = topic;
        pushToHistory(topic, true);
    }

    /**
     * Reserved for default state w/o active documentations and topic
     */
    private InterfaceState() {
    }

    public void pushToHistory(Topic topic, boolean preventDuplicates) {
        ensureTopicHistoryIndexInBounds();

        // Don't push to history if current topic is the current one
        if (preventDuplicates
            && topicIdentifierHistoryIndex >= 0
            && topicIdentifierHistory.get(topicIdentifierHistoryIndex).equals(topic.getTopicIdentifier())) {
            return;
        }

        // Clear history that the user went from as they are opening new topic
        if (topicIdentifierHistoryIndex < topicIdentifierHistory.size()) {
            topicIdentifierHistory.subList(topicIdentifierHistoryIndex + 1, topicIdentifierHistory.size()).clear();
        }

        // Add opened topic to history
        topicIdentifierHistory.add(topic.getTopicIdentifier());
        topicIdentifierHistoryIndex++;
    }

    /**
     * Resets topic history
     */
    public void resetHistory() {
        topicIdentifierHistoryIndex = -1;
        topicIdentifierHistory.clear();
    }

    /**
     * Checks if the user can go back in the topic history, e.g. there's a topic identifier before the current one.
     *
     * @return true if the user can go back in the topic history, false otherwise
     */
    public boolean canGoBack() {
        ensureTopicHistoryIndexInBounds();
        return topicIdentifierHistoryIndex > 0;
    }

    /**
     * Checks if the user can go forward in the topic history, e.g. there's a topic identifier after the current one.
     *
     * @return true if the user can go forward in the topic history, false otherwise
     */
    public boolean canGoForward() {
        ensureTopicHistoryIndexInBounds();
        return topicIdentifierHistoryIndex < topicIdentifierHistory.size() - 1;
    }

    /**
     * Gets the previous topic in the history. Subtracts one from the index.
     *
     * @return the previous topic identifier in the history, or null if there is no previous topic
     */
    public String getPreviousTopicAndMoveIndex() {
        if (topicIdentifierHistory.isEmpty()) {
            return null;
        } else {
            ensureTopicHistoryIndexInBounds();
            if (topicIdentifierHistoryIndex > 0) {
                topicIdentifierHistoryIndex -= 1;
                return topicIdentifierHistory.get(topicIdentifierHistoryIndex);
            } else {
                return null;
            }
        }
    }

    /**
     * Gets the next topic in the history. Adds one to the index.
     *
     * @return the next topic identifier in the history, or null if there is no next topic
     */
    public String getNextTopicAndMoveIndex() {
        if (topicIdentifierHistory.isEmpty()) {
            return null;
        } else {
            ensureTopicHistoryIndexInBounds();
            if (topicIdentifierHistoryIndex < topicIdentifierHistory.size() - 1) {
                topicIdentifierHistoryIndex += 1;
                return topicIdentifierHistory.get(topicIdentifierHistoryIndex);
            } else {
                return null;
            }
        }
    }

    public boolean hasHistory() {
        return !topicIdentifierHistory.isEmpty();
    }

    /**
     * Saves various data to the preferences, such as the last opened topic and the last search query.
     *
     * @param preferences The preferences to save to
     */
    public void saveToPreferences(InterfacePreferences preferences) {
        if (topic != null) {
            preferences.setLastOpenedTopicIdentifier(topic.getTopicIdentifier());
        }
        preferences.setLastTopicSearchQuery(topicSearchQuery);
        preferences.setFullTextSearch(fullTextSearch);
        preferences.setTopicIdentifierHistory(new ArrayList<>(topicIdentifierHistory));
        preferences.setTopicIdentifierHistoryIndex(topicIdentifierHistoryIndex);
        preferences.setLastInterfaceMode(mode);
        preferences.setPreferredLocale(preferredLocale);
    }

    /**
     * Loads various data from the preferences.
     *
     * @param interfacePreferences The preferences to load from
     */
    public void loadFromPreferences(InterfacePreferences interfacePreferences) {
        topicSearchQuery = interfacePreferences.getLastTopicSearchQuery();
        fullTextSearch = interfacePreferences.isFullTextSearch();
        mode = interfacePreferences.getLastInterfaceMode();
        preferredLocale = interfacePreferences.getPreferredLocale();
        if (interfacePreferences.getTopicIdentifierHistory() != null) {
            topicIdentifierHistory.clear();
            topicIdentifierHistory.addAll(interfacePreferences.getTopicIdentifierHistory());
            topicIdentifierHistoryIndex = interfacePreferences.getTopicIdentifierHistoryIndex();
        }
        ensureTopicHistoryIndexInBounds();
    }

    private void ensureTopicHistoryIndexInBounds() {
        if (topicIdentifierHistoryIndex < -1) {
            topicIdentifierHistoryIndex = -1;
        } else if (topicIdentifierHistoryIndex >= topicIdentifierHistory.size()) {
            topicIdentifierHistoryIndex = topicIdentifierHistory.size() - 1;
        }
    }

    @Override
    public String toString() {
        return "InterfaceState{" +
            "documentations.size=" + documentations.size() +
            ", topic=" + topic +
            ", topicSearchQuery='" + topicSearchQuery + '\'' +
            '}';
    }
}
