---
name: Configuration
description: What does this do?
author: mayuna
---

# Configuration

The configuration for Voile is located at **mods/IwakuraEnterprises_Voile/config.json**

## Configuration file

Using the configuration file, **config.json**, you're able to configure various parts of this mod.
- **enabled** - enables/disables the /voile command
- **outOfBoxExperience** - enables/disables the OOBE (welcome to Voile chat message) (automatically disabled for singleplayer)
- **updateCheckerEnabled** - enables/disables update checker
- **loadDocumentationsFromDirectory** - specifies directory from which all server documentations are loaded
- **defaultTopicIdentifier** - specifies the default topic to open. Follows the topic identifier format.
- **disabledDocumentationTypes** - specifies a list of **disabled** documentation types. Using this, you may **hide** various types of documentation.
  - Types: **SERVER**, **MOD**, **INTERNAL**
- **validator** - validator configuration (see below)
- **commandShortcuts** - command shortcuts configuration (see below)
- **runtimeImageAssets** - runtime image assets configuration (see below)
- **integration** - various integrations with Voile (see below)
- **fileSystemCache** - configuration for file system cache (see below)
- **sentry** - Voile-specific Sentry error reporting (see below)

### Validator 

Validator can validate the generated UI for the Voile's interface before it is shown to the player. This can prevent
crashes due to invalid UI. **Mod called Kytale is required for the UI validation**, as the validator (HytaleUIParser)
is written in Kotlin. However, **other parts of the validator**, such as checking for recently crashed players, **works**
even without Kytale.

- **enabled** - enables/disables the validator
- **aggressive** - if the Voile's interface should not be shown to the player if validation fails
- **dumpInvalidUI** - if invalid UIs should be dumped in directory **mods/IwakuraEnterprises_Voile/genereted_ui_errors**
- **showErrorsToPlayers** - if validation errors should be sent to player's chat
- **checkForRecentlyCrashedPlayers** - if for recently crashed player a error dump should be created. **This is highly
  recommended to be enabled on!**
- **inMemoryDumpTimeToLiveMillis** - the time to keep recently validated UI in memory. Low values may cause dumps not being created.

### Command Shortcuts

Command shortcuts allow you to create quick-and-easy commands to open various documentation topics.

- **enabled** - enables/disables command shortcuts
- **overrideHytaleCommands** - command shortcuts will be registered as their own standalone commands, this will ensure Hytale's commands are overridden.
- **commands** - contains list of command shortcuts (<gray>the following fields are for specific command shortcut</gray>)
  - **name** - name of the command shortcut. This is what the player types in chat (e.g. **rules** -> **/rules**)
  - **topicIdentifier** - the identifier for topic to be opened by the command shortcut. For more information about topic identifiers, see below.

<buttons>
  <button topic="DocsServerOwner:command-shortcuts">Command Shortcuts</button>
  <button topic="Docs:topic-identifiers">Topic Identifiers</button>
</buttons>

### Runtime Image Assets

Runtime image assets are images that are loaded from file system or online.

- **enabled** - enables/disables loading runtime image assets. Resource images will still be shown. Runtime imags
will be replaced by "Images are disabled" images.
- **maxImageDownloadFileSizeKb** - specifies the maximum file size of an online image in kilobytes.
- **inMemoryTimeToLiveSeconds** - specifies the maximum TTL in seconds for a runtime image asset to be loaded in memory.
Defaults to one hour. Lower values can make the interface feel unresponsive and laggy!

For more information about images, see the **Formatting** topic.

<buttons>
  <button topic="Docs:formatting">Formatting</button>
</buttons>

### Integration

#### Hytale Modding Wiki

Voile integration with the Hytale Modding Wiki.

- **enabled** - enables/disables the integration. If disabled, players won't be able to change the interface mode to the Hytale Modding Wiki view.
- **preLoadModsInBackground** - if the page lists should be pre-loaded in the background in 10-second intervals. This ensures
good UX when checking the wiki. If disabled, players will have to click mod's topic to load all other sub-topics.
- **urlOverride** - overrides the URL to request data from.
- **apiTokenOverride** - overrides the API bearer token when sending requests. This API token is specified **without** the Bearer prefix.


### File System Cache

File system cache allows for caching various data to the disk. This ensures quick loading times when downloading images
and other various data from the internet. The file system cache lives in **IwakuraEnterprises_Voile/cache**

- **enabled** - enables/disables the file system cache
- **cacheTypeTimeToLiveSeconds** - map of cache types with their respective TTL in seconds. When file's TTL runs out,
it is deleted from the disk. If your server has a lot of online images, increasing the TTL can yield smoother experience.
  - **IMAGE** - defaults to 86400 seconds (1 day)
  - **HYTALE_MODDING_WIKI_PAGE_CONTENT** - defaults to 86400 seconds (1 day)
  - **HYTALE_MODDING_WIKI_MOD** - defaults to 86400 seconds (1 day)
  - **HYTALE_MODDING_WIKI_MOD_LIST** - defaults to 86400 seconds (1 day)

### Sentry

Voile uses Sentry to report various errors in order for the developers to fix. The reports are completely anonymous. Same
system is used by Hypixel studios themselves in Hytale.

- **enabled** - enables/disables Voile's Sentry
- **serverId** - randomized UUID for your server (used when sending error reports to the developers of Voile)
- **dsnOverride** - allows you to override Voile's Sentry DSN

# Example configuration file

```json
{
  "enabled": true,
  "outOfBoxExperience": false,
  "updateCheckerEnabled": true,
  "loadDocumentationsFromDirectory": "documentation",
  "defaultTopicIdentifier": null,
  "disabledDocumentationTypes": ["INTERNAL"],
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
    "commands": [
      {
        "name": "help",
        "topicIdentifier": "help-topic-id"
      }
    ]
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
      "IMAGE": 86400,
      "HYTALE_MODDING_WIKI_PAGE_CONTENT": 86400,
      "HYTALE_MODDING_WIKI_MOD": 86400,
      "HYTALE_MODDING_WIKI_MOD_LIST": 86400
    }
  },
  "sentry": {
    "enabled": true,
    "serverId": "a65e0e17-95e3-4e9f-ac45-210705879306",
    "dsnOverride": null
  }
}
```