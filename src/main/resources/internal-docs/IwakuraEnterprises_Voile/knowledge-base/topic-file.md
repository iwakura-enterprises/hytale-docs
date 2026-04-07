---
name: Topic file
description: How to declare a topic
author: mayuna
sort-index: -70
sub-topics:
  - topic-identifiers
  - topic-localization
---

# Topic file

## What is it?

A topic file is a simple text file with the `.md` (Markdown) file type. The topic file must contain a
YAML-formatted front-matter that defines basic information about the topic, such as its name, description and author.
The front-matter is located at the beginning of the file surrounded by three dashes (`---`). Everything after the
front-matter is considered as the topic's Markdown content.

All topics have an ID. This ID is created from the topic's file name or specified in the topic's front-matter
using the `id` field. The topic's ID is then used when constructing the topic's identifier. You can learn about topic's
identifiers in the **Topic identifiers** topic.

<buttons>
    <button topic="topic-identifiers">Topic identifiers</button>
</buttons>

# Format

Topic has several formats:

- Topic's file name
- YAML front-matter
- Topic's content (after the front-matter)

## Topic's file name

Topic's file name defines its ID and localization.

```
{topic-id}${locale}.md
```

| Term         | Description                                                                                                                                             |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| `{topic-id}` | **Mandatory** Topic's ID. Can be overridden with the `id` field in topic's YAML front-matter. May contain spaces. Cannot contain the dollar sign symbol. |
| `{locale}`   | Topic's locale. Defines the locale type for the topic. Can be overridden with the `locale-type` field in the topic's YAML front-matter.                      |

Topic's locale in the file name is optional. For more information regarding topic localization, see the **Topic localization** topic.

<buttons>
    <button topic="topic-localization">Topic localization</button>
</buttons>

## YAML front-matter

The topic file starts with a YAML front-matter that defines metadata for the topic, such as a name, description and author.
Some fields are mandatory while others are optional. Using the front-matter you may also define the topic's sub-topics,
allowing you to create a tree of topics. Using the front-matter, you may override some metadata that is extracted from
the topic's file name.

The front-matter is located at the beginning of the file surrounded by three dashes (`---`):

```md
---
name: My topic
description: Description for my topic
author: Myself
---
```

Everything after the front-matter is considered as Markdown content and follows the Markdown syntax.

### Full front-matter example

```md
---
id: my-topic
name: My topic
description: Description for my topic
author: Myself
locale-type: cs
sort-index: -57
category: false
sub-topics:
  - other-topic-id
  - directory-name
---
```

| Field         | Type                   | Description                                                                                                                                                         |
|---------------|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `id`          | String                 | Topic's ID. If specified, overrides the topic's ID extracted from the file name.                                                                                        |
| `name`        | String                 | **Mandatory** Topic's name. Shown in the documentation tree and when a player opens the topic.                                                                        |
| `description` | String                 | Topic's description. Shown when a player opens the topic.                                                                                                             |
| `author`      | String                 | Topic's author. Shown when a player opens the topic.                                                                                                                  |
| `sort-index`  | Integer                | Sorting index for the topic. Lower values are sorted first.                                                                                                         |
| `category`    | Boolean                | Whether the topic should be considered as a category. Category topics are rendered with a bold name and are unclickable.                                           |
| `locale-type` | String (language code) | Topic's locale. If specified, overrides the topic's locale extracted from the file name, if it was supplied.                                                            |
| `sub-topics`  | List of String         | List of sub-topics for this topic. May specify other topic IDs or a folder name to include all topics within the folder. The folder must be next to the topic file. |

!! The `sub-topics` field can reference topics only in the current documentation. Topic identifiers (not IDs) are not supported.

For more information regarding topic localization, see the **Topic localization** topic.

<buttons>
    <button topic="topic-localization">Topic localization</button>
</buttons>

## Topic's content

Everything after the front-matter is considered as Markdown content and follows the Markdown syntax. For more information
regarding the Markdown syntax, please see the **Markdown syntax** topic.

```
# This is a title
## This is a sub-title
This is a paragraph.
```

<buttons>
    <button topic="markdown-syntax">Markdown syntax</button>
</buttons>
