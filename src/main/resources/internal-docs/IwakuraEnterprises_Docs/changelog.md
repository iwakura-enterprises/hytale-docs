---
name: Changelog
description: Here you can find all changes that have been made to Voile.
author: mayuna
---

# 1.6.1

### Minor
- Fixed partially cut topic in the topic tree list on specific screen resolutions.

# 1.6.0

### Major
- Added Back / Forward / Home navigation buttons
- Code blocks are now rendered inside the `CodeEditor` elements. This enables you to select and copy text inside them.
  - There have been some visual and logic issues with these `CodeEditor` elements. The most worrying one is that upon
    selecting one-liner text using a double click *may* crash your client. Thus, in future releases if there
    would be any severe problems, this new change might get reverted.

### Minor
- Improved tooltip textures for images
- Fixed scroll reset on topic tree list when opening new topic
- Fixed ordered/bullet lists' bottom padding in a block quote when there are no other elements under them
- Fixed opening a topic using a topic button that references a topic with just an ID that can be found in different
documentations. Now it prefers the currently opened documentation and then searches in the other documentations.
- Added `InterfaceState` that allows to remember certain interface state that can be later loaded
- Added `/wiki` command alias
- Various fixes and improvements to the rendering and feel of Voile

# 1.5.0

### Major
- Added basic topic search. In future releases, full-text search will be added. (see VOILE-6)

# 1.4.2

### Minor
- Strip any formatting for entries in topic chapter tree. In future releases,
it might be possible to have stylized text there as well. (see VOILE-24)

# 1.4.1

### Minor
- Headings now support text formatting.
- Added support for anonymous telemetry using HStats.

# 1.4.0

### Major
- **Added support for images.** See **Formatting** topic for more information.
- The interface is now rendered in specialized render threads.

### Minor
- Voile now remembers the last topic you've opened. When opening its interface again, it will display the last seen topic.
- Improved the clarity of logs when (re)loading documentations.
- Voile's **manifest.json** now properly specifies the supported server version.
- Fixes and improvements.

# 1.3.2

### Minor
- Fixed an issue when loading resources from other mods. The JVM might load the resources from
filesystem, instead of a JAR. (<gray>Thank you FonnyFofo from WanMine!</gray>)

# 1.3.1

### Minor
- Improved **Internal Docs** for Voile
- Voile's interface now can be opened with **/voile** (with backwards compatible **/docs**)
- Voile can be now be reloaded with **/voile-reload** (with backwards compatible **/docs-reload**)

# 1.3.0

### Added
- Support for simple Markdown documentation, see topic **Voile for Developers -> Home** for more info.
- Update checker. You'll be able to see if there's a new version available next to the current version (<gray>top left</gray>).
- Check whenever the mod is in single-player, in which case the OOBE will be disabled.

# 1.2.0

### Major
- Rebranded to **Voile** due to bad naming. When searching for "hytale docs", there will be little to no chance to get this
mod in the search results. Also, Voile pays tribute to Patchouli, a great Minecraft mod adding documentation capabilities
as well!

# 1.1.0

### Added
- Improved developer API by allowing mods to just create assets that will be loaded by Docs.
This makes it super easy to add support for Docs!

# 1.0.0

The first release of Docs!

### Added
- The Docs' interface with powerful markdown renderer.
- Documentation system allowing users and developers to create documentations with ease.
- Command shortcuts allowing server owners to define easily accessible documentation.
- API for developers (accessible via class DocsAPI)
- Basic documentation for users and developers. This documentation will be extended in the future releases.

### Fixed
- Docs... not.. existing? They exist now :P

### Removed
- Removed Herobrine