package enterprises.iwakura.docs.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import app.ultradev.hytaleuiparser.Parser;
import app.ultradev.hytaleuiparser.Tokenizer;
import app.ultradev.hytaleuiparser.Validator;
import app.ultradev.hytaleuiparser.ValidatorError;
import enterprises.iwakura.docs.DocsPlugin;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.LastValidationEntry;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.ExceptionUtils;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.docs.util.ReflectionUtils;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class ValidatorService {

    public static boolean showedKytaleWarning = false;
    public static final String DUMP_DIRECTORY_NAME = "generated_ui_errors";
    public static final String PLAYER_CRASHED_HEADER =
        """
            // ======= DOCS ERROR DUMP =======
            // [!] Please, report this to mayuna@iwakura.enterprises [!]
            // [!] If possible, include the markdown file as well [!]
            //
            // == Info ==
            // DocsContext: %s
            //
            // == Notice ==
            // Player has crashed after the UI was validated! This is not good.
            //
            // You can also debug the following UI by placing it into an asset
            // pack and trying to load it up. If you enable Hytale's diagnostic
            // mode in the settings, it will say where the error is.
            //
            // TIP:
            // If you have not already, try installing Kytale for validator support.
            // Docs has support for HytaleUIParser that validates the resulting
            // generated UI. However, it is written in Kotlin and without Kytale,
            // Docs cannot use it.
            //
            // We're sorry for the inconvenience.
            """;
    public static final String VALIDATOR_ERRORS_HEADER =
        """
            // ======= DOCS ERROR DUMP =======
            // [!] Please, report this to mayuna@iwakura.enterprises [!]
            // [!] If possible, include the markdown file as well [!]
            //
            // == Info ==
            // DocsContext: %s
            //
            // == Notice ==
            // We're sorry for the inconvenience.
            //
            // There are some validation errors:
            """;
    public static final String VALIDATOR_EXCEPTION_HEADER =
        """
            // ======= DOCS ERROR DUMP =======
            // [!] Please, report this to mayuna@iwakura.enterprises [!]
            // [!] If possible, include the markdown file as well [!]
            //
            // == Info ==
            // DocsContext: %s
            //
            // == Notice ==
            // An exception has occurred while parsing & validating the
            // generated UI! This usually mean malformed UI (e.g. missing
            // semicolons, extra quotes and other stuff). If other topics
            // are opened without problem, try checking your markdown
            // file. If you think the markdown file should be properly rendered,
            // this error should indicate a bug within Docs. Please report it!
            //
            // You can also debug the following UI by placing it into an asset
            // pack and trying to load it up. If you enable Hytale's diagnostic
            // mode in the settings, it will say where the error is.
            //
            // We're sorry for the inconvenience.
            """;

    private static final List<LastValidationEntry> LAST_VALIDATION_ENTRIES = Collections.synchronizedList(
        new ArrayList<>());
    private static final Timer timer = new Timer();

    private final ConfigurationService configurationService;
    private final Logger logger;

    private Path uiDumpDirectory;

    /**
     * Initializes validator service
     *
     * @param dataDirectory Data directory
     */
    public void init(Path dataDirectory) {
        uiDumpDirectory = dataDirectory.resolve(DUMP_DIRECTORY_NAME);
        logger.info("Initializing ValidatorService in directory: %s".formatted(uiDumpDirectory));

        if (!Files.exists(uiDumpDirectory)) {
            try {
                Files.createDirectories(uiDumpDirectory);
            } catch (IOException exception) {
                throw new RuntimeException("Failed to create directory for dumping UI: %s".formatted(uiDumpDirectory),
                    exception);
            }
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                var validatorConfig = configurationService.getDocsConfig().getValidator();
                try {
                    LAST_VALIDATION_ENTRIES.removeIf(entry ->
                        System.currentTimeMillis() - entry.getCreatedAt()
                            > validatorConfig.getInMemoryDumpTimeToLiveMillis());
                } catch (Exception exception) {
                    logger.error("Failed to remove last validation entries", exception);
                }
            }
        }, 0, 1000);
    }

    /**
     * Puts validation entry for player
     *
     * @param playerRef Player ref
     * @param dump      Dump
     */
    public void putValidationEntry(PlayerRef playerRef, String dump) {
        LAST_VALIDATION_ENTRIES.removeIf(entry -> entry.getPlayerUuid().equals(playerRef.getUuid()));
        LAST_VALIDATION_ENTRIES.add(new LastValidationEntry(playerRef.getUuid(), dump));
    }

    /**
     * Validates UI in specified command builder. Returns true if the UI should be shown to the player.
     *
     * @param playerRef      Player ref
     * @param docsContext    Docs context
     * @param commandBuilder Command builder
     *
     * @return True if UI should be shown to user, false otherwise.
     */
    public boolean validateUI(PlayerRef playerRef, DocsContext docsContext, UICommandBuilder commandBuilder) {
        var validatorConfig = configurationService.getDocsConfig().getValidator();

        if (!validatorConfig.isEnabled()) {
            return true;
        }

        var ui = extractAllUI(commandBuilder);
        List<String> errors = validateIfKytaleInstalled(playerRef, docsContext, ui);

        if (errors == null) {
            // Validation resulted in an exception
            return false;
        } else if (!errors.isEmpty()) {
            logger.error("UI validation for player %s for docs context %s failed with %d errors:".formatted(
                playerRef.getUuid(), docsContext, errors.size()
            ));
            if (validatorConfig.isShowErrorsToPlayers()) {
                ChatInfo.ERROR.send(playerRef, "UI validation failed with %d errors:".formatted(errors.size()));
            }

            for (String error : errors) {
                logger.error(" - " + error);
                if (validatorConfig.isShowErrorsToPlayers()) {
                    ChatInfo.ERROR.send(playerRef, error);
                }
            }

            if (validatorConfig.isDumpInvalidUI()) {
                dumpErrorsAndUI(String.valueOf(playerRef.getUuid()), docsContext, errors, ui);
            } else {
                logger.warn("Error dumping is disabled, no dump will be made.");
            }
            return !validatorConfig.isAggressive();
        }

        putValidationEntry(playerRef, createDumpString(PLAYER_CRASHED_HEADER.formatted(docsContext), ui));
        return true;
    }

    private List<String> validateIfKytaleInstalled(PlayerRef playerRef, DocsContext docsContext, String ui) {
        if (PluginManager.get().getPlugin(PluginIdentifier.fromString("AmoAster:Kytale")) == null) {
            if (!showedKytaleWarning) {
                logger.warn("Kytale is not installed! HytaleUIParser cannot validate generated UI.");
                showedKytaleWarning = true;
            }
            return List.of();
        }

        var validatorConfig = configurationService.getDocsConfig().getValidator();
        Validator validator;
        try {
            var root = new Parser(new Tokenizer(new StringReader(ui))).finish();
            validator = new Validator(Map.of("inline.ui", root), false);
            validator.validate();
            return filterErrors(validator.getValidationErrors());
        } catch (Exception exception) {
            logger.error("Failed to parse UI by validator for docs context: %s".formatted(docsContext), exception);
            if (validatorConfig.isDumpInvalidUI()) {
                dumpString(String.valueOf(playerRef.getUuid()),
                    createDumpString("%s\n%s".formatted(VALIDATOR_EXCEPTION_HEADER.formatted(docsContext), ExceptionUtils.dumpExceptionStacktrace("// ", exception)), ui));
            } else {
                logger.warn("Error dumping is disabled, no dump will be made.");
            }
            return null;
        }
    }

    private List<String> filterErrors(List<ValidatorError> errors) {
        return errors.stream()
            .map(ValidatorError::getMessage)
            //.filter(message -> !message.startsWith("Duplicate variable"))
            .toList();
    }

    private String extractAllUI(UICommandBuilder commandBuilder) {
        StringBuilder ui = new StringBuilder();

        for (CustomUICommand command : ReflectionUtils.getCommands(commandBuilder)) {
            if (command.type == CustomUICommandType.AppendInline
                || command.type == CustomUICommandType.InsertBeforeInline) {
                ui.append("// ===========================================\n");
                ui.append("// = AppendInline/InsertBeforeInline Command =\n");
                ui.append("// Selector: ").append(command.selector).append("\n");
                ui.append("\n");
                ui.append(command.text);
            }
        }

        return ui.toString();
    }

    private void dumpErrorsAndUI(String filename, DocsContext docsContext, List<String> errors, String ui) {
        var errorsString = errors.stream()
            .map(error -> "// - " + error)
            .collect(Collectors.joining(System.lineSeparator()));

        dumpString(filename,
            createDumpString("%s\n%s".formatted(VALIDATOR_ERRORS_HEADER.formatted(docsContext), errorsString), ui));
    }

    private String createDumpString(String content, String ui) {
        return content + "\n" +
            """
                // = Generated UI =
                %s
                """.formatted(ui);
    }

    private void dumpString(String fileName, String content) {
        Path filePath;
        int fileIndex = 0;

        do {
            filePath = uiDumpDirectory.resolve(fileName + "_" + fileIndex + ".dump");
            fileIndex++;
        } while (Files.exists(filePath));

        try {
            Files.writeString(filePath, content, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
            logger.warn("Dump with error information has been made to file %s".formatted(filePath));
        } catch (IOException exception) {
            logger.error("Failed to dump errors to %s".formatted(filePath), exception);
        }
    }

    /**
     * Handles crashed player
     *
     * @param playerRef Player ref
     */
    public void handleCrashedPlayer(PlayerRef playerRef) {
        var validatorConfig = configurationService.getDocsConfig().getValidator();

        if (validatorConfig.isCheckForRecentlyCrashedPlayers()) {
            LAST_VALIDATION_ENTRIES.stream()
                .filter(x -> x.getPlayerUuid().equals(playerRef.getUuid()))
                .findAny().ifPresent(lastValidationEntry -> {
                    logger.error("It is possible that player %s has crashed due to generated UI! The last validated UI has been dumped to a file.".formatted(
                        playerRef.getUuid()
                    ));
                    dumpString(String.valueOf(playerRef.getUuid()), lastValidationEntry.getDump());
                });
        }
    }
}
