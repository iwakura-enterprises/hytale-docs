---
name: Changelog
description: Here you can find all changes that have been made to Voile.
author: mayuna
---

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