---
name: Changelog
description: Here you can find all changes that have been made to Voile.
author: mayuna
---

# 1.8.4

### Minor
- Fixed Hytale Modding Wiki mods not properly loading when you attempted to open non-installed mod.

# 1.8.3

### Minor
- Preloading only installed Hytale Modding Wiki mods.
- Improved exception ignoring logic for sentry.

# 1.8.2

### Minor
- Adjusted Voile's version text in the interface.
- Added Voile About page when clicking the version text in the interface.
- Added command /debug-voile that shows additional debug information useful for debugging issues.
- Hardened cache directory existence, ensuring it exists when saving/loading/refreshing files.

# 1.8.1

### Minor
- Added blur behind Voile's interface.
- Added initial admonition support, check **Formatting** topic for more info.
- Fixed code stylization in headings not showing up as text in chapter tree list.
- Improved Sentry logging.
  - Specifying server version and installed plugins.
  - Ignoring some SSL-related exceptions.

# 1.8.0

### Major
- Added initial support for Markdown tables.
  - Due to Hytale's UI limitations, the tables may render incorrectly. This is initial
  implementation that isn't perfect.
  - Currently, there's no way to manually resize table's columns.

### Minor
- Fixed text escape when exception occurs during Markdown rendering and the error has quotation marks.
- Fixed paragraph padding in multiline blockquote.
- Fixed NPE when handling page data with null action

# 1.7.3

### Minor
- Added support for webp images
- Improved Sentry logging
  - Breadcrumbs from Voile logger
  - Disabled sending default PII

# 1.7.2

### Minor
- Fixed few NPEs when opening internal not found topic, etc.
- Fixed checking for installing mods in Hytale Modding Wiki for Hytale default mods
- Added error logging when error occurs in handlePageData

# 1.7.1

### Minor
- Fixed opening default topic in the current interface mode when opening the interface for the first time.
- Fixed navigation buttons not changing to topic's interface mode based on its documentation type.

# 1.7.0

The 1.7.0 update adds brand-new integration with Hytale Modding Wiki and more UI/UX improvements.

### Major
- Added support for interface modes
  - Interface modes allows Voile to group certain documentation types within one mode, ensuring good UX.
  - Interface modes can be switched with top-left button in the interface.
- Added integration with Hytale Modding Wiki, accessible with new interface mode.
  - Added two documentation types: **HYTALE_MODDING_WIKI** and **HYTALE_MODDING_WIKI_INSTALLED**
  - The Hytale Modding Wiki integration can be disabled in the config.
  - On the first load, Voile will fetch all page in 10 second interval in the background.
- Added File System Cache
  - Images and other data can now be cached on the disk.
  - The file system cache can be reconfigured in the config.
  - By default, images and Hytale Modding Wiki content is stored for one day.

### Minor
- Updated Voile's internal docs with new content
- Fixes & text sanitization when rendering the interface
- Fixed various NPEs and UI issues when rendering invalid topics
- Fixed issue when loading config with unknown enum value in map key
- Removed `enabledTypes` config in favor of `disabledDocumentationTypes`
- Improved logic when downloading images from the internet
- Added Sentry
- Adjusted left padding for lists

# 1.6.2

### Minor
- Added ability to override Hytale commands with command shorcuts (see **Configuration** topic)
- Fixed old references to Docs
- Renamed Voile's logger to Voile instead of Docs

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