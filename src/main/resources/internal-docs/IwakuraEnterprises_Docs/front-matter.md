---
name: Topic front-matter
description: How topics are named
author: mayuna
---

# Topic front-matter

Voile's topics have **front-matter** that specifies metadata about for the topic. They follow YAML syntax with
precise fields that Voile reads from. Some fields are mandatory and some are not.

## Formatting

Topic's front-matter is specified at the begging of topic's file in the following format:

```md
---
name: This is topic's name
description: How are you?
author: Me & myself
---

... topic content ...
```

## Fields

Topic's front-matter have various fields.

- **name** (mandatory) - specifies the name for the topic. Names are shown in the documentation tree list and when user opens a topic.
- **description** - specifies the description for the topic. Descriptions are shown when player opens a topic.
- **author** - specifies the author for the topic. Author is shown when player opens a topic.
- **id** - specifies the ID for the topic. By default, topic's ID is same as its file name (without the .md file extension).
- **sort-index** - specifies the sort index for the topic. Sort index is used to sort topics in the documentation tree list.
Sort index is sorted from **lowest** to **highest**. That means sort index of -1 will be placed **before** sort-index of 0.
The default sort index is 0.
- **sub-topics** - specifies a list of sub-topics for the topic. You may specify topic IDs or folders in the same folder that the topic file is in.

> **Warning:** Non-server topics loaded from mod resources etc. do not support **sub-topics** field. You must specify all sub-topics in an index JSON file.

## Fully populated front-matter example

```md
---
id: my-topic-id
name: This is topic's name
description: How are you?
author: Me & myself
sort-index: -9999
sub-topics:
  - another-topic-id
  - folder-name
---

... topic content ...
```
