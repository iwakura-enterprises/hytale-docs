package enterprises.iwakura.docs.object;

import java.util.List;

import lombok.Data;

/**
 * Holds the state for the interface. Mutable instance. Same instance should always show the same interface, regardless
 * the player.
 */
@Data
public class InterfaceState {

    protected List<Documentation> documentations;
    protected Topic topic;

    protected String topicSearchQuery;
    protected boolean searchActive;

    public InterfaceState(List<Documentation> documentations, Topic topic) {
        this.documentations = documentations;
        this.topic = topic;
    }

    /**
     * Saves various data to the preferences, such as the last opened topic and the last search query.
     *
     * @param preferences The preferences to save to
     */
    public void saveToPreferences(InterfacePreferences preferences) {
        preferences.setLastOpenedTopicIdentifier(topic.getTopicIdentifier());
        preferences.setLastTopicSearchQuery(topicSearchQuery);
    }

    /**
     * Loads various data from the preferences.
     *
     * @param interfacePreferences The preferences to load from
     */
    public void fromPreferences(InterfacePreferences interfacePreferences) {
        this.topicSearchQuery = interfacePreferences.getLastTopicSearchQuery();
    }

    @Override
    public String toString() {
        return "InterfaceState{" +
            "documentations.size=" + documentations.size() +
            ", topic=" + topic +
            ", topicSearchQuery='" + topicSearchQuery + '\'' +
            ", searchActive=" + searchActive +
            '}';
    }
}
