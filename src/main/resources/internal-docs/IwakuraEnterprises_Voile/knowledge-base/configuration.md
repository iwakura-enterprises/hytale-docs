---
name: Configuration
description: Configuring Voile
author: mayuna
sort-index: -90
---

# Configuration

The configuration for Voile is located at `mods/IwakuraEnterprises_Voile/config.json`

## Configuration file fields

!! The tables may not be rendered correctly if Hytale isn't running in the fullscreen.

### `DocsConfig` object

| Field                             | Type                                  | Default           | Description                                                                                                                                                                                      |
|-----------------------------------|---------------------------------------|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`                         | Boolean                               | `true`            | Enables/disables Voile. When disabled, Voile's commands won't do anything.                                                                                                                       |
| `outOfBoxExperience`              | Boolean                               | `true`            | Enables/disables the OOBE (the welcome message for admins after installing Voile for the first time).                                                                                            |
| `updateCheckerEnabled`            | Boolean                               | `true`            | Enables/disables update checker. New updates are shown in the top-left of Voile's interface next to the current version.                                                                         |
| `enableFullTextSearch`            | Boolean                               | `true`            | Enables/disables full text search functionality. Can be helpful if it's causing any issues.                                                                                                      |
| `persistInterfacePreferences`     | Boolean                               | `true`            | Enables/disables Voile's ability to save interface preferences to player's components for better UX.                                                                                             |
| `voileCommandRequiresPermission`  | Boolean                               | `false`           | Enables/disables permission node generation for the `/voile` command. Setting this to false makes players not need any permission to open Voile's interface. Requires restart to take effect. |
| `loadDocumentationsFromDirectory` | String                                | `"documentation"` | Changes where Voile looks for the server documentation. Requires restart to take effect.                                                                                                      |
| `defaultTopicIdentifier`          | String                                | `null`            | The default topic to open for the player when opening Voile's interface for the first time. See the **Topic identifiers** topic for more information on how to define a topic identifier.           |
| `disabledDocumentationTypes`      | List of `DocumentationType` enum      | `[]`              | Disables specified documentation types. If all documentation types for interface mode are disabled, the entire interface mode is disabled and inaccessible.                                      |
| `interfacePreferencesDefaults`    | `InterfacePreferencesDefaults` object |                   | Default interface preferences for players.                                                                                                                                                       |
| `validator`                       | `Validator` object                    |                   | Configures Voile's UI validator.                                                                                                                                                                 |
| `commandShortcuts`                | `CommandShortcuts` object             |                   | Configures Voile's command shortcuts.                                                                                                                                                            |
| `runtimeImageAssets`              | `RuntimeImageAssets` object           |                   | Configures Voile's runtime image assets.                                                                                                                                                         |
| `integration`                     | `Integration` object                  |                   | Configures Voile's integrations.                                                                                                                                                                 |
| `fileSystemCache`                 | `FileSystemCache` object              |                   | Configures Voile's file system cache.                                                                                                                                                            |
| `sentry`                          | `Sentry` object                       |                   | Configures Voile's Sentry error reporting.                                                                                                                                                       |

### `InterfacePreferencesDefaults` object

| Field           | Type                 | Description                                                                                                                                                                                                                 |
|-----------------|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `checksum`      | UUID                 | The checksum that is used to validate player's interface preferences. If this UUID does not equal to player's interface preferences, they will be reset. Changes to a random UUID when running the `/voile-reload` command. |
| `interfaceMode` | `InterfaceMode` enum | Specifies the default interface mode that is shown when a player opens Voile's interface for the first time.                                                                                                                       |
| `localeType`    | `LocaleType` enum    | Specifies server's default preferred language. If set, player's game language will be ignored.                                                                                                                              |

### `Validator` object

| Field                            | Type    | Default | Description                                                                                          |
|----------------------------------|---------|---------|------------------------------------------------------------------------------------------------------|
| `enabled`                        | Boolean | `true`  | Enables/disables the UI validator.                                                                   |
| `aggressive`                     | Boolean | `true`  | When enabled, invalid UI will not be opened for the player to prevent a potential crash.             |
| `dumpInvalidUI`                  | Boolean | `true`  | When enabled, invalid UIs are dumped into `mods/IwakuraEnterprises_Voile/generated_ui_errors`.       |
| `showErrorsToPlayers`            | Boolean | `false` | When enabled, validation errors are shown to players.                                                |
| `checkForRecentlyCrashedPlayers` | Boolean | `true`  | When enabled, checks whether a player has recently crashed after opening a UI.                       |
| `inMemoryDumpTimeToLiveMillis`   | Long    | `2000`  | The time-to-live (in milliseconds) for how long generated UI is kept in memory for dumping purposes. |

### `CommandShortcuts` object

| Field                    | Type                      | Default | Description                                                                                                                              |
|--------------------------|---------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`                | Boolean                   | `true`  | Enables/disables command shortcuts.                                                                                                      |
| `overrideHytaleCommands` | Boolean                   | `false` | When enabled, command shortcuts will be registered as their own standalone commands, ensuring Hytale's built-in commands are overridden. |
| `commands`               | List of `Command` objects | `[]`    | List of command shortcuts.                                                                                                               |

### `Command` object

| Field             | Type   | Description                                                                                                                 |
|-------------------|--------|-----------------------------------------------------------------------------------------------------------------------------|
| `name`            | String | The name of the command shortcut. This is what the player types in chat (e.g. `rules` -> `/rules`).                         |
| `topicIdentifier` | String | The identifier of the topic to be opened by the command shortcut. See the **Topic identifiers** topic for more information. |

### `RuntimeImageAssets` object

| Field                        | Type    | Default | Description                                                     |
|------------------------------|---------|---------|-----------------------------------------------------------------|
| `enabled`                    | Boolean | `true`  | Enables/disables runtime image assets.                          |
| `maxImageDownloadFileSizeKb` | Integer | `2048`  | Maximum allowed file size (in kilobytes) for downloaded images. |
| `inMemoryTimeToLiveSeconds`  | Integer | `3600`  | The time-to-live (in seconds) for images kept in memory.        |

### `Integration` object

| Field               | Type                       | Description                                     |
|---------------------|----------------------------|-------------------------------------------------|
| `hytaleModdingWiki` | `HytaleModdingWiki` object | Configures the Hytale Modding Wiki integration. |

### `HytaleModdingWiki` object

| Field                     | Type    | Default | Description                                                                                       |
|---------------------------|---------|---------|---------------------------------------------------------------------------------------------------|
| `enabled`                 | Boolean | `true`  | Enables/disables the Hytale Modding Wiki integration.                                             |
| `preLoadModsInBackground` | Boolean | `true`  | When enabled, mods from the Hytale Modding Wiki are pre-loaded in the background on server start. |
| `urlOverride`             | String  | `null`  | Overrides the default Hytale Modding Wiki URL.                                                    |
| `apiTokenOverride`        | String  | `null`  | Overrides the default Hytale Modding Wiki API token.                                              |

### `FileSystemCache` object

| Field                        | Type                            | Default                      | Description                                                                                                         |
|------------------------------|---------------------------------|------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `enabled`                    | Boolean                         | `true`                       | Enables/disables the file system cache.                                                                             |
| `cacheTypeTimeToLiveSeconds` | Map of `CacheFileType` and Long | *(see `CacheFileType` enum)* | A map of `CacheFileType` names to their TTL in seconds. Missing entries fall back to each type's default TTL value. |

### `Sentry` object

| Field         | Type    | Default         | Description                                                   |
|---------------|---------|-----------------|---------------------------------------------------------------|
| `enabled`     | Boolean | `true`          | Enables/disables Sentry error reporting.                      |
| `serverId`    | UUID    | *(random UUID)* | The unique identifier of this server instance used in Sentry. |
| `dsnOverride` | String  | `null`          | Overrides the default Sentry DSN.                             |

## Enums

### `DocumentationType` enum

- `SERVER` - Server documentation
- `MOD` - Mod documentation
- `EXTERNAL_MOD` - Mod documentation fetched from various integrations.
- `HYTALE_MODDING_WIKI_INSTALLED` - Installed mods that already have existing mod documentation but are on the Hytale
  Modding Wiki.
- `HYTALE_MODDING_WIKI` - Hytale Modding Wiki mods.
- `INTERNAL` - Internal Voile documentation.

### `InterfaceMode` enum

- `VOILE` - The default interface mode
- `HYTALE_MODDING_WIKI` - Hytale Modding Wiki integration interface mode

### `LocaleType` enum

```
ENGLISH
SPANISH
PORTUGUESE
FRENCH
GERMAN
ITALIAN
DUTCH
POLISH
RUSSIAN
UKRAINIAN
SWEDISH
NORWEGIAN
DANISH
FINNISH
GREEK
CZECH
SLOVAK
HUNGARIAN
ROMANIAN
BULGARIAN
CROATIAN
SERBIAN
TURKISH
CHINESE_SIMPLIFIED
CHINESE_TRADITIONAL
JAPANESE
KOREAN
VIETNAMESE
THAI
INDONESIAN
MALAY
HINDI
ARABIC
HEBREW
SWAHILI
AMHARIC
ZULU
LOLCAT
TRUE_LANGUAGE
CHAOS
```

### `CacheFileType` enum

| Value                              | Default TTL | Description                                          |
|------------------------------------|-------------|------------------------------------------------------|
| `IMAGE`                            | 24 hours    | Cached downloaded images.                            |
| `HYTALE_MODDING_WIKI_MOD_LIST`     | 24 hours    | Cached Hytale Modding Wiki mod list.                 |
| `HYTALE_MODDING_WIKI_MOD`          | 24 hours    | Cached individual Hytale Modding Wiki mod page list. |
| `HYTALE_MODDING_WIKI_PAGE_CONTENT` | 24 hours    | Cached Hytale Modding Wiki page content.             |

# Example configuration file

```
{
  "enabled": true,
  "outOfBoxExperience": false,
  "updateCheckerEnabled": true,
  "enableFullTextSearch": true,
  "persistInterfacePreferences": true,
  "voileCommandRequiresPermission": false,
  "loadDocumentationsFromDirectory": "documentation",
  "defaultTopicIdentifier": null,
  "disabledDocumentationTypes": [],
  "interfacePreferencesDefaults": {
    "checksum": "8c72167c-48d6-4f08-bccc-2f0e613fe9e3",
    "interfaceMode": null,
    "localeType": "ENGLISH"
  },
  "validator": {
    "enabled": true,
    "aggressive": true,
    "dumpInvalidUI": true,
    "showErrorsToPlayers": false,
    "checkForRecentlyCrashedPlayers": true,
    "inMemoryDumpTimeToLiveMillis": 2000
  },
  "commandShortcuts": {
    "enabled": true,
    "overrideHytaleCommands": true,
    "commands": []
  },
  "runtimeImageAssets": {
    "enabled": true,
    "maxImageDownloadFileSizeKb": 2048,
    "inMemoryTimeToLiveSeconds": 3600
  },
  "integration": {
    "hytaleModdingWiki": {
      "enabled": true,
      "preLoadModsInBackground": true,
      "urlOverride": null,
      "apiTokenOverride": null
    }
  },
  "fileSystemCache": {
    "enabled": true,
    "cacheTypeTimeToLiveSeconds": {
      "IMAGE": 100000000
    }
  },
  "sentry": {
    "enabled": true,
    "serverId": "00000000-0000-0000-0000-000000000001",
    "dsnOverride": null
  }
}
```