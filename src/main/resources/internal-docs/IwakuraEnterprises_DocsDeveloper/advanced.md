---
name: Advanced
description: Full flavored implementation
author: mayuna
---

# Voile <3 Developers

You don't have to install any dependencies, Voile will load documentations straight from your mod's resources.

## Create documentation index file

Firstly you need to tell Voile what documentations you want to add. This is done by creating an index file in **Common/Docs**
directory in your mod's resources. Voile searches for JSON files named in format of **{ModGroup}_{ModName}.json**.

Let's say your mod has group **MyGroup** and name **MyMod**. In that case you would create index file at **Common/Docs/MyGroup_MyMod.json**.

The index file has the following structure:

```json
{
  "documentations": [
    {
      "group": "MyGroup",
      "id": "MyDocumentation",
      "name": "My mod documentation",
      "enabled": true,
      "sortIndex": 300,
      "topics": [
        {
          "file": "index.md",
          "subTopics": [
            {
              "file": "tutorial/first-steps.md"
            }
          ]
        }
      ]
    }
  ]
}
```

This will:
- Define new documentation in group **MyGroup**, with ID **MyDocumentation**, with user-friendly name **My mod documentation**.
  - **The group and ID** is used as the root directory for the documentation. In this example, the directory
    **Common/Docs/MyGroup_MyDocumentation** will contain all the Markdown files for the documentation.
  - **The group and ID** are also used when referencing specific documentation's topic. The user-friendly name is shown to the user
    in the documentation list on the left side of the interface.
- Mark the documentation as enabled. Disabled documentations are not shown to the player.
- Give a sort index to the documentation. Sort indexes are used to sort documentation list: **they are sorted from the lowest to highest**. This
  allows you to control the order of documentations.
- List of topics that the documentation will contain.
  - For example, the **index.md** topic will be loaded from **Common/Docs/MyGroup_MyDocumentation/index.md**
  - The **first-steps.md** topic will be loaded from **Common/Docs/MyGroup_MyDocumentation/tutorial/index.md**

The directory structure will look something like this:

```
Common/Docs/
  MyGroup_MyMod.json
  MyGroup_MyDocumentation/
    index.md
    tutorial/
      first-steps.md
```

## Writing topics

Topics are written in Markdown with a front-matter.

> ### Common/Docs/MyGroup_MyDocumentation/index.md
>
> ```md
> ---
> name: Home
> description: This is home for my mod documentation!
> author: Me & myself
> ---
> 
> # Home
> 
> Welcome to my mod! Here you'll be able to learn all the important stuff about it.
> ```

> ### Common/Docs/MyGroup_MyDocumentation/tutorial/first-steps.md
>
> ```md
> ---
> name: First steps
> description: Starting with my mod
> author: Me & myself
> ---
> 
> # First steps
> 
> You can start playing my mod by crafting.....
> ```

The topic's ID is derived from its file name. So **index.md** will have ID **index** and **first-steps.md** will
have ID **first-steps**. You may also use field **id** in the front-matter to specify your own ID.

> **Warning:** A topics loaded from resources do not support **sub-topics** field in front-matter. Due to technical
> limitations, **you must define all topics in the index file**, including sub-topics.

## What's next?

You can have a look at other topics included in these internal documentations. For example, learning **Topic Identifiers**
is recommended. You can also check out the **Formatting** topic for additional formatting tips
(including how to format text with colors and create clickable buttons).

<buttons>
  <button topic="Docs:topic-identifiers">Topic Identifiers</button>
  <button topic="Docs:formatting">Formatting</button>
</buttons>