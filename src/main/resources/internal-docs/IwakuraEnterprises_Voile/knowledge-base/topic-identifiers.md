---
name: Topic Identifiers
description: How topics can be identified
author: mayuna
---

# Topic Identifiers

When linking other topics, you must specify a topic. A topic doesn't have to have a unique ID across all documentations.
That's why there are topic identifiers - **a way to specify a single topic across all documentations**.

Topic identifiers are used in configuration, when linking different topics in buttons, and when opening a specific topic using
the **/voile** command.

!! When defining sub-topics in a topic, you cannot use the topic identifier. This is because you may only define
!! sub-topics in the current documentation that the topic belongs to due to technical limitations when the documentation
!! is being loaded.

## Format

Topic identifiers can have various formats, ranging from the most verbose to the simplest:

| Format                                                 | Example                               |
|--------------------------------------------------------|---------------------------------------|
| `{group}:{documentation-id}:{topic-id}${topic-locale}` | `MyGroup:MyDocumentation:my-topic$cs` |
| `{group}:{documentation-id}:{topic-id}`                | `MyGroup:MyDocumentation:my-topic`    |
| `{group}:{topic-id}`                                   | `MyGroup:my-topic`                    |
| `{documentation-id}:{topic-id}`                        | `MyDocumentation:my-topic`            |
| `{topic-id}`                                           | `my-topic`                            |

| Term                 | Description                                                                            |
|----------------------|----------------------------------------------------------------------------------------|
| `{group}`            | Documentation's group. Specified in the documentation index file.                      |
| `{documentation-id}` | Documentation's ID. Specified in the documentation index file.                         |
| `{topic-id}`         | **Mandatory** Topic's ID. Extracted from topic's file name or in topic's front-matter. |
| `{topic-locale}`     | Topic's locale. Extracted from topic's file name or in topic's front-matter.           |

! More precise identifier may solve issues with duplicate topic IDs. It is recommended to at least specify group or
! documentation ID when linking other topics.

! When opening a topic by clicking a button in topic's content, Voile first looks for topics within currently opened
! topic's documentation.
! If it can't find the specified topic, it falls back to all other visible documentations.
! 
! This way you *may* link other topics by just a topic ID that could appear in other documentations (e.g. `home`,
! `weapons`, etc.)

When linking non-existing topic using a button, Voile will generate and open "topic not found" topic that will contain
some valuable information.

<buttons>
    <button topic="non:existing:topic$cs">Non-existing topic</button>
</buttons>