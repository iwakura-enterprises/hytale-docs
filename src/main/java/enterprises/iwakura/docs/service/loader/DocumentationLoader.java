package enterprises.iwakura.docs.service.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import enterprises.iwakura.docs.config.TopicConfig;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.LoaderContext;
import enterprises.iwakura.docs.service.MarkdownService;

/**
 * Interface for implementing various ways to load documentations
 */
public abstract class DocumentationLoader {

    /**
     * Loads list of documentations
     *
     * @return List of documentations
     */
    public abstract List<Documentation> load(LoaderContext loaderContext);

    public abstract String toString();

    /**
     * Checks if adding a sub-topic would create a circular reference
     *
     * @param startId     The starting topic ID
     * @param currentId   The current topic ID being checked
     * @param configMap   Map of topic configurations
     * @param visitedPath Path of visited topic IDs
     *
     * @return true if circular reference detected, false otherwise
     */
    protected boolean hasCircularReference(
        String startId,
        String currentId,
        Map<String, TopicConfig> configMap,
        List<String> visitedPath
    ) {
        visitedPath.add(currentId);

        if (currentId.equals(startId)) {
            return true;
        }

        var config = configMap.get(currentId);
        if (config == null || config.getSubTopics() == null) {
            return false;
        }

        for (var subTopicId : config.getSubTopics()) {
            if (visitedPath.contains(subTopicId)) {
                visitedPath.add(subTopicId);
                return true;
            }
            if (hasCircularReference(startId, subTopicId, configMap, new ArrayList<>(visitedPath))) {
                return true;
            }
        }

        return false;
    }
}
