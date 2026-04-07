---
name: Tables
description: Conveying information in tables
author: mayuna
---

# Markdown tables

Table syntax follows the standard Markdown table syntax. Tables are always rendered in the middle of the topic.
Table headers are always rendered as bold and with darker background. All text formatting inside the table cells
is supported.

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

## Text alignment

It is possible to change how the text is aligned in a table.

| Divider | Alignment                            |
|:-------:|--------------------------------------|
|  `---`  | Default, header center, content left |
| `:---`  | Header and content left              |
| `---:`  | Header and content right             |
| `:---:` | Header and content center            |

> ### Text alignment example
> 
> | Class       | Name            | Guild              | Level |
> |:------------|:----------------|:-------------------|------:|
> | Warrior     | Aldric Ironfist | Iron Vanguard      |    42 |
> | Mage        | Seraphine Ash   | Arcane Brotherhood |    38 |
> | Rogue       | Vex Shadowstep  | Shadow Syndicate   |    55 |
> | Paladin     | Orynn Lightbane | Order of the Dawn  |    29 |
> | Necromancer | Mara Duskwell   | Cult of the Grave  |    67 |
> 
> ```md
> | Class       | Name            | Guild              | Level |
> |:------------|:----------------|:-------------------|------:|
> | Warrior     | Aldric Ironfist | Iron Vanguard      |    42 |
> | Mage        | Seraphine Ash   | Arcane Brotherhood |    38 |
> | Rogue       | Vex Shadowstep  | Shadow Syndicate   |    55 |
> | Paladin     | Orynn Lightbane | Order of the Dawn  |    29 |
> | Necromancer | Mara Duskwell   | Cult of the Grave  |    67 |
> ```

!! You may encounter rendering issues when Hytale is not in the fullscreen.