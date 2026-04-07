---
name: Markdown syntax
description: How to write and stylize topic
author: mayuna
sort-index: -61
sub-topics:
  - tables
  - images
---

# Markdown syntax in Voile

Voile's Markdown renderer follows the standard Markdown syntax with additional extensions (tables and admonitions).

This topic will break down all supported Markdown elements and how to use them.

# Heading

To create a heading, use the hashtag (`#`) symbol in front of a line of text. The number of hashtags determines the
level
of the heading. Higher levels produce smaller headings.

```
# Heading level 1
## Heading level 2
### Heading level 3
#### Heading level 4
##### Heading level 5
###### Heading level 6
```

Additionally, heading levels 1 and 2 have an extra line below them.

# Paragraph

The basic building block, a simple paragraph.

```
This is one paragraph.

This is a second
paragraph.

This is a third paragraph.
```

Paragraphs are separated by a blank line.

# Emphasis

You can add emphasis by making text bold or italic (or both at the same time).

## Italic

To italicize text, use only one asterisk (`*`) or underscore (`_`) symbol.

- `*hello* world` -> *hello* world
- `_hello_ world` -> _hello_ world

## Bold

To make bold text, use two asterisks (`*`) or underscores (`_`).

- `**hello** world` -> **hello** world
- `__hello__ world` -> __hello__ world

## Bold and italic

To make text bold and italicized, use three asterisks (`*`) or underscores (`_`).

- `***hello*** world` -> ***hello*** world
- `___hello___ world` -> ___hello___ world

# Blockquote

To create a blockquote, add a greater than (`>`) symbol in front of a paragraph.

You can also make a multi-line blockquote by specifying just the symbol.

```
> This is a blockquote.

> This is a
>
> blockquote.
```

> This is a blockquote.

> This is a
>
> blockquote.

Blockquotes can contain other elements, such as headings and code blocks. They can be also nested.

```
> This is first level
> > This is second level
```

> This is first level
> > This is second level

## Admonition

Admonitions are more stylized block quotes. You can add them via the exclamation mark (`!`) and an optional additional
symbol to style it.

```
! This is an information.
!v This is a success.
!! This is a warning.
!x This is an error.
```

! This is an information.
!v This is a success.
!! This is a warning.
!x This is an error.

You can also make admonitions multi-lined, same as with blockquotes, however for Voile to properly render it,
add one additional space after the `!` / `!v`, `!x`, `!!` symbols.

```
!v This is a
!v 
!v multi-lined success.
```

!v This is a
!v 
!v multi-lined success.

# List

You can organize items into ordered and unordered lists.

## Ordered list

To create an ordered list, add a number followed by a period in front of paragraphs. You can also indent the line with
four spaces to create a sub-list.

```
1. First item
2. Second item
    1. First item for the Second item
    2. Second item for the Second item
3. Third item
```

1. First item
2. Second item
    1. First item for the Second item
    2. Second item for the Second item
3. Third item

## Unordered list

To create an unordered list, add either dash (`-`), asterisk (`*`) or plus (`+`) symbol in front of paragraphs. You can
also
indent the line with four spaces to create a sub-list.

```
- First item
- Second item
    - First item for the Second item
    - Second item for the Second item
- Third item
```

- First item
- Second item
    - First item for the Second item
    - Second item for the Second item
- Third item

! You can also combine ordered and unordered lists together.

# Code

To denote a word or phrase as code, enclose it in backtick (`) symbols.

```
Hello `world`
```

Hello `world`

## Code block

You can create entire code blocks by using three backtick (```) symbols.

    ```
    Hello world
    ```

```
Hello world
```

### Indented code block

You can also indent a paragraph with four spaces to create an indented code block.

```
    This is
    indented
    code block
```

    This is
    indented
    code block

# Thematic break

You can add a thematic break using three or more asterisks (`***`), dash (`---`), or underscore (`___`) symbols on a
line
by themselves.

```
***
```

***

# Topic buttons

You can link different topics by using an HTML block `<buttons>` with the `<button>` element. The button element specifies
the topic in the `topic` data element by a **Topic identifier**.

```
<buttons>
    <button topic="IwakuraEnterprises:Voile:tables$cs">Tables</button>
</buttons>
```

To learn more about **Topic identifiers**, see its topic.

<buttons>
    <button topic="topic-identifiers">Topic identifiers</button>
</buttons>

# Table

You can add tables by specifying the table block.

```
| Column 1 | Column 2 | Column 3 |
|----------|----------|----------|
| Value 1  | Value 2  | Value 3  |
| Value a  | Value b  | Value c  |
```

| Column 1 | Column 2 | Column 3 |
|----------|----------|----------|
| Value 1  | Value 2  | Value 3  |
| Value a  | Value b  | Value c  |

For more information, please see the **Tables** topic.

<buttons>
    <button topic="tables">Tables</button>
</buttons>

# Image

You can add images by using the `![Alternative text](image-source)` syntax.

![Voile Banner](UI/Custom/Docs/Images/voile_banner.png)

For more information, please see the **Images** topic.

<buttons>
    <button topic="images">Images</button>
</buttons>

# Colors

You can color your text by using various color formatting. 

## Minecraft-style colors

Voile supports Minecraft-style coloring using the ampersand (`&`) symbol:
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

## Inline HTML color codes

Inline HTML color codes are also supported:
- `<red>Red</red>` -> <red>Red</red>
- `<green>Green</green>` -> <green>Green</green>
- `<blue>Blue</blue>` -> <blue>Blue</blue>

With hexadecimal values:
- `<#e5da52>Written in hex!</#e5da52>` -> <#e5da52>Written in hex!</#e5da52>

With decimal values:
- `<255,85,85>Red in RGB</255,85,85>` -> <255,85,85>Red in RGB</255,85,85>
- `<128,64,200>Custom color</128,64,200>` -> <128,64,200>Custom color</128,64,200>

## Gradients

Gradient colors can be achieved by specifying the `<gradient>` element
with a data element specifying the color gradient.

```
<gradient data="red:yellow:green:blue:purple">Text in gradient color!</gradient>
```

<gradient data="red:yellow:green:blue:purple">text in the rainbow gradient!</gradient>

# Unsupported elements

Currently, there are some unsupported Markdown elements.

| Element type                   | Implementation ETA                                              |
|--------------------------------|-----------------------------------------------------------------|
| URL link                       | Hytale does not provide a way to add a link to the UI.          |
| Chapter link                   | Hytale does not provide a way to scroll the UI programmatically. |
| Subscript and superscript text | Hytale does not provide an easy way how to do this.             |
| Footnotes                      | N/A                                                             |
| Definition list                | N/A                                                             |
| Task list                      | N/A                                                             |

! Element types with N/A may be implemented in the future.
