---
name: Home
description: DEVELOPERS DEVELOPERS DEVELOPERS DEVELOPERS
author: mayuna
---

# Voile for Developers

Simple or complex documentations, you don't have to install any dependencies. Voile will load documentations
straight from your mod's resources. This allows you to focus on the documentation, rather than on adding a
mod support.

There are currently **two** ways to add documentations:
1. Create simple documentation inside a single Markdown file
2. Create documentation with unlimited number of topics & sub-topics

Based on your needs, you'll have to choose. The former allows you to **quickly sketch up simple documentation**, while
the latter allows you to **create complex-yet-simple documentation with many topics**.

In this topic, we'll focus on the first way. For the second way, please see **Advanced** topic.

<buttons>
  <button topic="DocsDeveloper:advanced">Advanced</button>
</buttons>

## Simple Documentation

Simply create a Markdown file in your mod's resources at **Common/Docs/{ModGroup}_{ModName}.md** where the **ModGroup**
is your mod's group and **ModName** your mod's name. **The file name is case-sensitive.**

> **Common/Docs/MyGroup_MyMod.md**
> 
> ```md
> ---
> name: My Mod Name
> description: This is a description for my mod!
> author: Me & myself
> ---
> 
> # My Mod
> 
> My Mod adds .... and .......
> ```

**That's it.** Voile will show your Markdown file (topic) under documentation named **Various Mods**.

However, there are some limitations:
- You may not specify topic's ID.
- You may not specify sub-topics.
- You may not specify the sort index.

Topics in **Various Mods** documentation are always sorted alphabetically.

For additional information, see the following topics.

<buttons>
  <button topic="DocsDeveloper:advanced">Advanced</button>
  <button topic="Docs:formatting">Formatting</button>
  <button topic="Docs:topic-identifiers">Topic Identifiers</button>
</buttons>
