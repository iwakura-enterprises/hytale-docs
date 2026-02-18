---
name: Topic Identifiers
description: How topics can be identified
author: mayuna
---

# Topic Identifiers

When linking other topics, you must somehow specify a topic. Topic doesn't have to have unique ID across all documentations.
That's why there are topic identifiers - **a way to specify single topic across all documentations**

Topic identifiers are used in configuration, when linking different topics in buttons, and when opening specific topic using
**/voile** command.

> **Warning:** When defining sub-topics in a topic, you cannot use topic identifier. This is because you may only define
> sub-topics in the current documentation that the topic belongs to. **You cannot link different topics from different
> documentations due to technical limitations.**

## Format

Topic identifiers can have 4 formats:
1. **Group:DocumentationId:topic_id**
2. **Group:topic_id**
3. **DocumentationId:topic_id**
4. **topic_id**

The most command ways to specify topic is with second and third option. The fourth option leaves you open to duplicate
topic IDs across different documentations.

### Example

Let's say there are two documentations:
1. **MyGroup** owning documentation **MyDocumentation** with topic **home**
2. **OtherGroup** owning documentation **OtherDocumentation** with topic **home**

Both of these documentation define topic **home**. Thus, specifying the topic just by its ID is insufficient. If we
wanted to specify **home** topic in the second documentation, we can do it in these three ways:
1. **OtherGroup:OtherDocumentation:home**
2. **OtherGroup:home**
3. **OtherDocumentation:home**

