---
name: Welcome to Voile
author: mayuna
description: the in-game wiki framework
sort-index: -1
---

![Voile Banner](UI/Custom/Docs/Images/voile_banner.png)

# <gradient data="#b293e7:#f5d7e9">Welcome to the Voile wiki</gradient>

This documentation covers everything about Voile; configuration, how to write documentation, how to use it and more.

<buttons>
  <button topic="getting-started">Getting started</button>
</buttons>

## <gradient data="#b293e7:#f5d7e9">About Voile</gradient>

Voile allows you to create stunning wikis in-game for players. This allows you to communicate important information
to the players without them needing to leave Hytale.

Whether you're a **server owner** creating a wiki for your server or you're a **developer** creating a wiki for your mod, **Voile is here for you**.

## <gradient data="#b293e7:#f5d7e9">Support / Suggestions / Planned features</gradient>

```
* Download: https://download.voile.dev
* Website: https://voile.dev
* Documentation: https://docs.voile.dev
* Support: https://support.voile.dev
* Support e-mail: mauyna@iwakura.enterprises
* Roadmap: https://youtrack.iwakura.enterprises
```

! You can click the top left Voile text to see debug information.

## How to hide this internal wiki?

1. Open `config.json` located in Voile's data directory (`mods/IwakuraEnterprises_Voile`)
2. Locate `disabledDocumentationTypes` field within the JSON
3. Add `"INTERNAL"` to the array
4. Run `/voile-reload` command

The result should look something like this:

```json
// ...
  "disabledDocumentationTypes": ["INTERNAL"],
// ...
```