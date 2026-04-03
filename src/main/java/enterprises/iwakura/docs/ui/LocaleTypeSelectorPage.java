package enterprises.iwakura.docs.ui;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.LocaleType;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.service.ValidatorService;
import enterprises.iwakura.docs.ui.DocumentationViewerPage.PageData.InterfaceAction;
import enterprises.iwakura.docs.ui.LocaleTypeSelectorPage.PageData;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.InterfaceUtils;
import enterprises.iwakura.docs.util.Logger;
import lombok.Data;

public class LocaleTypeSelectorPage extends InteractiveCustomUIPage<PageData> {

    public static final String LANGUAGE_BUTTON_COMPONENT =
        """
        // Language item component
        Group {
            LayoutMode: Top;
            Padding: (Left: 8, Top: 8, Right: 8);

            Group {
                Padding: (Full: 2);
                OutlineColor: #203651;
                OutlineSize: 2;
                Button #{{language-button-selector}} {
                    Style: (
                        Default: (Background: {{language-button-default-background}}),
                        Hovered: (Background: {{language-button-hovered-background}}),
                        Pressed: (Background: #fcca4c(0.1)),
                    );

                    Group {
                        Padding: (Full: 6);
                        LayoutMode: Left;

                        Group {
                            Padding: (Full: 8);

                            AssetImage {
                                Anchor: (Width: 40, Height: 40);
                                AssetPath: "UI/Custom/Docs/Images/Flags/{{flag-code}}.png";
                            }
                        }

                        Group {
                            LayoutMode: Middle;

                            Group {
                                LayoutMode: Left;
                                Label {
                                    Style: (TextColor: #ffffff, RenderUppercase: true, FontSize: 16, FontName: "Secondary");
                                    Text: "{{language-english-name}}";
                                    Padding: (Right: 8);
                                }

                                Label {
                                    Style: (TextColor: #cccccc, FontSize: 16);
                                    Text: "{{language-native-name}}";
                                    Padding: (Right: 8);
                                }

                                Label {
                                    Style: (TextColor: #cccccc(0.5), FontSize: 12, VerticalAlignment: Center, FontName: "Mono");
                                    Text: "{{language-code}}";
                                }
                            }

                            Group {
                                LayoutMode: Left;

                                Label {
                                    Style: (TextColor: #afc2c3, FontSize: 14);
                                    Text: "{{number-of-localized-topics}}";
                                }
                            }
                        }
                    }
                }
            }
        }
        """;

    public static final String CONTAINER_COMPONENT =
        """
        Group {
            SceneBlur {}
            Group { Background: #000000(0.45); }

            Group {
                LayoutMode: MiddleCenter;

                Group {
                    LayoutMode: Top;
                    Background: (TexturePath: "Common/ContainerFullPatch.png", Border: 20);
                    Anchor: (Width: 500, Height: 800);
                    Padding: (Full: 16);

                    Label {
                        Style: (RenderBold: true, VerticalAlignment: Center, FontSize: 20, TextColor: #afc2c3, HorizontalAlignment: Start);
                        Padding: (Top: 8, Left: 8, Right: 8);
                        Text: "Please, select your preferred language.";
                    }

                    Group {
                        LayoutMode: Top;
                        Padding: (Full: 8);

                        TextField {{language-search-selector}} {
                            Style: InputFieldStyle();
                            PlaceholderStyle: InputFieldStyle(TextColor: #6e7da1);
                            Background: PatchStyle(TexturePath: "Common/InputBox.png", Border: 16);
                            Anchor: (Height: 40);
                            Padding: (Horizontal: 10);
                            PlaceholderText: "Search for language...";
                        }
                    }


                    Group {{language-list-selector}} {

                    }
                }
            }
        }
        """;

    public static final String MAIN_CONTENT_SELECTOR = "#Content";
    public static final String LANGUAGE_LIST_SELECTOR = "#LanguageListSelector";
    public static final String LANGUAGE_SEARCH_SELECTOR = "#LanguageSearchSelector";

    private final DocumentationViewerService documentationViewerService;
    private final DocumentationService documentationService;
    private final ValidatorService validatorService;
    private final Logger logger;
    private final boolean selectShouldOpenVoileInterface;

    public LocaleTypeSelectorPage(@NonNull PlayerRef playerRef,
        DocumentationViewerService documentationViewerService,
        DocumentationService documentationService,
        ValidatorService validatorService,
        Logger logger,
        boolean selectShouldOpenVoileInterface
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, PageData.CODEC);
        this.documentationViewerService = documentationViewerService;
        this.documentationService = documentationService;
        this.validatorService = validatorService;
        this.logger = logger;
        this.selectShouldOpenVoileInterface = selectShouldOpenVoileInterface;
    }

    @Override
    public void build(
        @NonNull Ref<EntityStore> ref,
        @NonNull UICommandBuilder commandBuilder,
        @NonNull UIEventBuilder eventBuilder,
        @NonNull Store<EntityStore> store
    ) {
        // Create dummy docsContext just for the validator for now
        var docsContext = DocsContext.empty(playerRef);
        docsContext.getCommandBuilder().append("Docs/Pages/LocaleTypeSelectorPage.ui");

        // Append whole container with language buttons
        docsContext.getCommandBuilder().appendInline(
            MAIN_CONTENT_SELECTOR,
            CONTAINER_COMPONENT
                .replace("{{language-list-selector}}", LANGUAGE_LIST_SELECTOR)
                .replace("{{language-search-selector}}", LANGUAGE_SEARCH_SELECTOR)
        );
        clearAndAppendInline(
            docsContext,
            documentationViewerService.getInterfacePreferences(playerRef.getUuid(), null).getPreferredLocaleType(),
            null
        );

        // Searching!
        docsContext.getEventBuilder().addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            LANGUAGE_SEARCH_SELECTOR,
            new EventData()
                .append(PageData.INTERFACE_ACTION_FIELD, InterfaceAction.SEARCH)
                .append(PageData.SEARCH_QUERY_FIELD, LANGUAGE_SEARCH_SELECTOR + ".Value"),
            false
        );

        // Validate before showing to the user
        if (validatorService.validateUI(playerRef, docsContext, docsContext.getCommandBuilder())) {
            docsContext.mergeInto(commandBuilder, eventBuilder);
        } else {
            ChatInfo.ERROR.send(playerRef, "The generated UI for language selector is invalid. See console for more information.");
            var player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                // FIXME: Actually does not close the page as we are opening it.
                player.getPageManager().setPage(ref, store, Page.None);
            }
        }
    }

    public void clearAndAppendInline(
        DocsContext docsContext,
        LocaleType selectedLanguage,
        String languageSearchQuery
    ) {
        final var ui = new StringBuilder("""
            Group {
                LayoutMode: TopScrolling;
                Anchor: (Height: 660);

                ScrollbarStyle: (
                    Spacing: 6,
                    Size: 6,
                    Background: (TexturePath: "Common/Scrollbar.png", Border: 3),
                    Handle: (TexturePath: "Common/ScrollbarHandle.png", Border: 3),
                    HoveredHandle: (TexturePath: "Common/ScrollbarHandleHovered.png", Border: 3),
                    DraggedHandle: (TexturePath: "Common/ScrollbarHandleDragged.png", Border: 3)
                );
            """);

        // Append language buttons
        AtomicInteger lastNumberOfLocalizedTopics = new AtomicInteger();
        LocaleType.ALL.stream()
            .filter(localeType ->
                localeType == selectedLanguage
                || languageSearchQuery == null
                || localeType.matchesSearch(languageSearchQuery))
            // Sorts languages based on the selected language (always on top) and then by the number Of localized topics
            .sorted(Comparator.<LocaleType, Boolean>comparing(localeType -> localeType != selectedLanguage)
                .thenComparingInt(localeType -> -documentationService.getNumberOfLocalizedTopics(localeType)))
            .forEach(localeType -> {
                var localeTypeMatches = localeType == selectedLanguage;
                var numberOfLocalizedTopics = documentationService.getNumberOfLocalizedTopics(localeType);
                var languageButtonSelector = InterfaceUtils.generateSelector();

                if (numberOfLocalizedTopics == 0 && lastNumberOfLocalizedTopics.get() > 0) {
                    ui.append("Group { Padding: (Top: 16); }");
                }
                lastNumberOfLocalizedTopics.set(numberOfLocalizedTopics);

                ui.append(
                    LANGUAGE_BUTTON_COMPONENT
                        .replace("{{language-button-default-background}}", localeTypeMatches ? "#fcca4c(0.3)": "#000000(0.0)")
                        .replace("{{language-button-hovered-background}}", localeTypeMatches ? "#fcca4c(0.2)": "#000000(0.3)")
                        .replace("{{language-button-selector}}", languageButtonSelector)
                        .replace("{{flag-code}}", localeType.getCode())
                        .replace("{{language-english-name}}", localeType.getEnglishName())
                        .replace("{{language-native-name}}", localeType.getNativeName())
                        .replace("{{language-code}}", localeType.getCode())
                        .replace("{{number-of-localized-topics}}", numberOfLocalizedTopics > 0 ? " ~%d localized topic(s)".formatted(numberOfLocalizedTopics) : "No localized topics")
                );

                docsContext.getEventBuilder().addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#" + languageButtonSelector,
                    new EventData()
                        .append(PageData.INTERFACE_ACTION_FIELD, PageData.InterfaceAction.SELECT_LOCALE_TYPE)
                        .append(PageData.SELECTED_LOCALE_TYPE_FIELD, localeType.name())
                );
            });

        ui.append("}");
        docsContext.getCommandBuilder().clear(LANGUAGE_LIST_SELECTOR);
        docsContext.getCommandBuilder().appendInline(LANGUAGE_LIST_SELECTOR, ui.toString());
    }

    @Override
    public void handleDataEvent(
        @NonNull Ref<EntityStore> ref,
        @NonNull Store<EntityStore> store,
        LocaleTypeSelectorPage.@NonNull PageData data
    ) {
        var player = store.getComponent(ref, Player.getComponentType());
        var action = data.getInterfaceAction();
        var interfacePreferences = documentationViewerService.getInterfacePreferences(playerRef.getUuid(), null);

        if (action == null) {
            logger.warn("Received page data without action! " + data);
            return;
        }

        if (player == null) {
            logger.warn("Player component is null on received LocaleTypeSelectorPage event! " + data);
            return;
        }

        switch (action) {
            case SEARCH -> {
                var docsContext = DocsContext.empty(playerRef);
                clearAndAppendInline(
                    docsContext,
                    interfacePreferences.getPreferredLocaleType(),
                    data.getSearchQuery()
                );

                if (!validatorService.validateUI(playerRef, docsContext, docsContext.getCommandBuilder())) {
                    ChatInfo.ERROR.send(playerRef, "The generated UI for language selector is invalid. See console for more information.");
                    player.getPageManager().setPage(ref, store, Page.None);
                    return;
                }

                sendUpdate(docsContext.getCommandBuilder(), docsContext.getEventBuilder(), false);
            }
            case SELECT_LOCALE_TYPE -> {
                logger.info("Player %s changed their preferred locale type to %s".formatted(
                    playerRef.getUsername(), data.getLocaleType()
                ));
                interfacePreferences.setPreferredLocaleType(data.getLocaleType());
                if (selectShouldOpenVoileInterface) {
                    // Opens last visited topic based on the interface preferences
                    documentationViewerService.openFor(playerRef, Optional.empty(), false);
                } else {
                    // Closes this interface
                    player.getPageManager().setPage(ref, store, Page.None);
                    ChatInfo.SUCCESS.send(playerRef, "Changed Voile's preferred language to &e" + data.getLocaleType().getEnglishName());
                }
            }
            case RESET_INTERFACE_PREFERENCES -> {
            }
        }
    }

    @Data
    public static class PageData {

        public static final String INTERFACE_ACTION_FIELD = "InterfaceAction";
        public static final String SELECTED_LOCALE_TYPE_FIELD = "SelectedLocaleType";
        public static final String SEARCH_QUERY_FIELD = "@SearchQuery";

        public static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(
                PageData.class, PageData::new)
            .append(new KeyedCodec<>(INTERFACE_ACTION_FIELD, Codec.STRING), PageData::setInterfaceActionValue, PageData::getInterfaceActionValue).add()
            .append(new KeyedCodec<>(SELECTED_LOCALE_TYPE_FIELD, Codec.STRING), PageData::setSelectedLocaleTypeValue, PageData::getSelectedLocaleTypeValue).add()
            .append(new KeyedCodec<>(SEARCH_QUERY_FIELD, Codec.STRING), PageData::setSearchQuery, PageData::getSearchQuery).add()
            .build();

        private String selectedLocaleTypeValue;
        private String interfaceActionValue;
        private String searchQuery;

        public PageData.InterfaceAction getInterfaceAction() {
            return interfaceActionValue != null ? PageData.InterfaceAction.valueOf(interfaceActionValue) : null;
        }

        public LocaleType getLocaleType() {
            return selectedLocaleTypeValue != null ? LocaleType.valueOf(selectedLocaleTypeValue) : null;
        }

        public enum InterfaceAction {
            SEARCH,
            SELECT_LOCALE_TYPE,
            RESET_INTERFACE_PREFERENCES;
        }
    }
}
