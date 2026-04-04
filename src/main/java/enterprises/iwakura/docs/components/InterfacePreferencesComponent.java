package enterprises.iwakura.docs.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.object.InterfaceMode;
import enterprises.iwakura.docs.object.LocaleType;
import enterprises.iwakura.docs.util.ListUtils;
import lombok.Data;

@Data
public class InterfacePreferencesComponent implements Component<EntityStore> {

    public static final BuilderCodec<InterfacePreferencesComponent> CODEC = BuilderCodec
        .builder(InterfacePreferencesComponent.class, InterfacePreferencesComponent::new)
        .append(new KeyedCodec<>("Checksum", Codec.UUID_STRING),
            (data, value) -> data.checksum = value,
            data -> data.checksum
        ).add()
        .append(new KeyedCodec<>("LastOpenedTopicIdentifier", Codec.STRING),
            (data, value) -> data.lastOpenedTopicIdentifier = value,
            data -> data.lastOpenedTopicIdentifier
        ).add()
        .append(new KeyedCodec<>("LastTopicSearchQuery", Codec.STRING),
            (data, value) -> data.lastTopicSearchQuery = value,
            data -> data.lastTopicSearchQuery
        ).add()
        .append(new KeyedCodec<>("FullTextSearch", Codec.BOOLEAN),
            (data, value) -> data.fullTextSearch = value,
            data -> data.fullTextSearch
        ).add()
        .append(new KeyedCodec<>("TopicIdentifierHistory", Codec.STRING_ARRAY),
            (data, value) -> data.topicIdentifierHistory = ListUtils.emptyIfNull(value),
            data -> ListUtils.emptyIfNull(data.topicIdentifierHistory).toArray(String[]::new)
        ).add()
        .append(new KeyedCodec<>("TopicIdentifierHistoryIndex", Codec.INTEGER),
            (data, value) -> data.topicIdentifierHistoryIndex = value,
            data -> data.topicIdentifierHistoryIndex
        ).add()
        .append(new KeyedCodec<>("LastInterfaceMode", Codec.STRING),
            (data, value) -> data.lastInterfaceMode = InterfaceMode.safeValueOf(value),
            data -> Optional.ofNullable(data.lastInterfaceMode).map(InterfaceMode::name).orElse(null)
        ).add()
        .append(new KeyedCodec<>("PreferredLocaleType", Codec.STRING),
            (data, value) -> data.preferredLocaleType = LocaleType.safeValueOf(value),
            data -> Optional.ofNullable(data.preferredLocaleType).map(LocaleType::name).orElse(null)
        ).add()
        .build();

    private UUID checksum;
    private String lastOpenedTopicIdentifier;
    private String lastTopicSearchQuery;
    private boolean fullTextSearch;
    private List<String> topicIdentifierHistory;
    private int topicIdentifierHistoryIndex;
    private InterfaceMode lastInterfaceMode;
    private LocaleType preferredLocaleType;

    public InterfacePreferencesComponent() {
    }

    public InterfacePreferencesComponent(InterfacePreferencesComponent other) {
        this.checksum = other.checksum;
        this.lastOpenedTopicIdentifier = other.lastOpenedTopicIdentifier;
        this.lastTopicSearchQuery = other.lastTopicSearchQuery;
        this.fullTextSearch = other.fullTextSearch;
        this.topicIdentifierHistory = other.topicIdentifierHistory;
        this.topicIdentifierHistoryIndex = other.topicIdentifierHistoryIndex;
        this.lastInterfaceMode = other.lastInterfaceMode;
        this.preferredLocaleType = other.preferredLocaleType;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new InterfacePreferencesComponent(this);
    }
}
