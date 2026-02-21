package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import enterprises.iwakura.docs.object.DocumentationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocsConfig {

    private boolean enabled = true;
    private boolean outOfBoxExperience = true;
    private boolean updateCheckerEnabled = true;
    private String loadDocumentationsFromDirectory = "documentation";
    private String defaultTopicIdentifier;
    private final List<DocumentationType> enabledTypes = new ArrayList<>(DocumentationType.ALL);
    private Validator validator = new Validator();
    private CommandShortcuts commandShortcuts = new CommandShortcuts();
    private RuntimeImageAssets runtimeImageAssets = new RuntimeImageAssets();

    @Data
    public static class Validator {

        private boolean enabled = true;
        private boolean aggressive = true;
        private boolean dumpInvalidUI = true;
        private boolean showErrorsToPlayers = false;
        private boolean checkForRecentlyCrashedPlayers = true;
        private long inMemoryDumpTimeToLiveMillis = 2000;
    }

    @Data
    public static class CommandShortcuts {

        private boolean enabled = true;
        private final List<Command> commands = new ArrayList<>();

        public Optional<String> getTopicIdentifierForCommand(String commandName) {
            return commands.stream()
                .filter(command -> command.getName().equalsIgnoreCase(commandName))
                .findFirst()
                .map(Command::getTopicIdentifier);
        }

        @Data
        public static class Command {

            private String name;
            private String topicIdentifier;
        }
    }

    @Data
    public static class RuntimeImageAssets {

        private boolean enabled = true;
        private int maxImageDownloadFileSizeKb = 1024 * 2; // 2MB
        private int inMemoryTimeToLiveSeconds = 3600; // Hour
        private int downloadedImagesTimeToLiveSeconds = 86400; // Day
    }
}
