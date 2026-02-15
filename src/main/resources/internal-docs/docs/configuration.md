---
name: Configuration
description: What does this do?
author: mayuna
---

# Configuration

The configuration for Docs is located at **mods/IwakuraEnterprises_Docs/config.json**

## Configuration file

Using the configuration file, **config.json**, you're able to configure various parts of this mod.
- **enabled** - enables/disables the /docs command
- **outOfBoxExperience** - enables/disables the OOBE (welcome to Docs chat message)
- **loadDocumentationsFromDirectory** - specifies directory from which all server documentations are loaded
- **defaultTopicIdentifier** - specifies the default topic to open. Follows the topic identifier format.
- **enabledTypes** - specifies a list of enabled documentation types. Using this, you may hide various types of documentation
  (e.g. you may hide this Internal documentation type on production servers). Available types: **SERVER**, **MOD**, **INTERNAL**
- **validator** - validator configuration (see below)
- **commandShortcuts** - command shortcuts configuration (see below)

### Validator 

Validator can validate the generated UI for the Docs' interface before it is shown to the player. This can prevent
crashes due to invalid UI. **Mod called Kytale is required for the UI validation**, as the validator (HytaleUIParser)
is written in Kotlin. However, **other parts of the validator**, such as checking for recently crashed players, **works**
even without Kytale.

- **enabled** - enables/disables the validator
- **aggressive** - if the Docs' interface should not be shown to the player if validation fails
- **dumpInvalidUI** - if invalid UIs should be dumped in directory **mods/IwakuraEnterprises_Docs/genereted_ui_errors**
- **showErrorsToPlayers** - if validation errors should be sent to player's chat
- **checkForRecentlyCrashedPlayers** - if for recently crashed player a error dump should be created. **This is highly
  recommended to be enabled on!**
- **inMemoryDumpTimeToLiveMillis** - the time to keep recently validated UI in memory. Low values may cause dumps not being created.

### Command Shortcuts

Command shortcuts allow you to create quick-and-easy commands to open various documentation topics.

- **enabled** - enables/disables command shortcuts
- **commands** - contains list of command shortcuts (<gray>the following fields are for specific command shortcut</gray>)
  - **name** - name of the command shortcut. This is what the player types in chat (e.g. **rules** -> **/rules**)
  - **topicIdentifier** - the identifier for topic to be opened by the command shortcut. For more information about topic identifiers, see below.

<buttons>
  <button topic="Docs:command-shortcuts">Command Shortcuts</button>
  <button topic="Docs:topic-identifiers">Topic Identifiers</button>
</buttons>

# Example configuration file

```json
{
  "enabled": true,
  "outOfBoxExperience": true,
  "loadDocumentationsFromDirectory": "documentation",
  "defaultTopicIdentifier": "IwakuraEnterprises:MyDocumentation:welcome",
  "enabledTypes": [
    "SERVER",
    "MOD",
    "INTERNAL"
  ],
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
    "commands": [
      {
        "name": "rules",
        "topicIdentifier": "server_rules"
      },
      {
        "name": "test",
        "topicIdentifier": "IwakuraEnterprises:MyDocumentation:test"
      }
    ]
  }
}
```