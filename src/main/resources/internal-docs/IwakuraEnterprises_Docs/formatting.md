---
name: Formatting
description: Various ways how to format Markdown in Voile
author: mayuna
---

# Formatting

Voile comes with powerful Markdown renderer that allows you to create complex Markdown while looking top-notch. With
nearly **all Markdown elements supported**, there are few additional that are great when creating documentations.

## Markdown Syntax

Nearly all Markdown elements are supported, incl. block quotes, code blocks and even indented code blocks. For more
information on Markdown syntax, see Markdown Cheat Sheet (https://www.markdownguide.org/cheat-sheet/).

> **Warning:** As of Voile 1.3.0, some of the elements are not yet supported:
> - Tables
> - Footnotes
> - Definition List
> - Task List
> - Subscript and superscript *(won't supported due to Hytale's font limitations)*

## Colors

The markdown renderer supports various color formatting. This is done thanks to
TaleMessage (https://github.com/InsiderAnh/TaleMessage).

### Minecraft-style colors

TaleMessage supports Minecraft-styled coloring using the ampersand (&) symbol:
- `&0` for &0Black color
- `&1` for &1Dark Blue color
- `&2` for &2Dark Green color
- `&3` for &3Dark Turquoise color
- `&4` for &4Dark Red color
- `&5` for &5Purple color
- `&6` for &6Dark Yellow color
- `&7` for &7Light Gray color
- `&8` for &8Dark Gray color
- `&9` for &9Light Blue color
- `&a` for &aLight Green color
- `&b` for &bLight Turquoise color
- `&c` for &cLight Red color
- `&d` for &dMagenta color
- `&e` for &eLight Yellow color
- `&f` for &fWhite color

### Inline HTML color codes

TaleMessage also supports inline HTML color codes formatted like `<red>text</red>`:
- `<red>Red</red>` -> <red>Red</red>
- `<green>Green</green>` -> <green>Green</green>
- `<blue>Blue</blue>` -> <blue>Blue</blue>

Hexadecimal values are also supported:
- `<#e5da52>Written in hex!</#e5da52>` -> <#e5da52>Written in hex!</#e5da52>

You may even write colors in decimal values like `<255,85,85>`:
- `<255,85,85>Red in RGB</255,85,85>` -> <255,85,85>Red in RGB</255,85,85>
- `<128,64,200>Custom color</128,64,200>` -> <128,64,200>Custom color</128,64,200>

### Gradients

TaleMessage also adds support for gradients. However,
due to the Markdown rendering, they are defined like this:

```
<gradient data="red:yellow:green:blue:purple">Text in gradient color!</gradient>
```

This example results in <gradient data="red:yellow:green:blue:purple">text in the rainbow gradient!</gradient>

## Buttons - linking other topics

Due to Hytale's limitation, there cannot be inline links to other topics. As a compromise, Voile
allows you to create buttons using HTML block.

```
<buttons>
    <button topic="my_topic">My Topic</button>
    <button topic="MyGroup:my_topic">My Topic</button>
    <button topic="MyDocumentation:my_topic">My Topic</button>
    <button topic="MyGroup:MyDocumentation:my_topic">My Topic</button>
</buttons>
```

This will create four buttons linking the same topic. As you can see, you must specify the **Topic Identifier** (see
the topic for more information).

<buttons>
    <button topic="Docs:topic-identifiers">Topic Identifiers</button>
    <button topic="Non:Existing:topic">Non-existing topic</button>
</buttons>

The second button links non-existing topic that will show error message with useful debug information.

> More button types will be added in the future releases.
