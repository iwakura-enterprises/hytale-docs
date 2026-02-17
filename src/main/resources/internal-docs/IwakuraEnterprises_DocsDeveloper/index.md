---
name: Home
description: DEVELOPERS DEVELOPERS DEVELOPERS DEVELOPERS
author: mayuna
---

# Voile <3 Developers

You don't have to install any dependencies, Voile will load documentations straight from your mod's resources.

Depending on your needs, there are two ways of adding support:
1. Creating simple documentation inside single Markdown file
2. Creating documentation with unlimited number of topics & sub-topics

See the **Advanced** topic for the second way.

<buttons>
  <button topic="DocsDeveloper:advanced">Advanced</button>
</buttons>

## Simple documentation

Simply create Markdown file in your mod's resources at **Common/Docs/{ModGroup}_{ModName}.md** with the following format:

```md
---
name: My Mod Name
description: This is a description for my mod!
author: Me & myself
---

# My Mod

My Mod adds .... and .......
```

**That's it.** Voile will show your Markdown file (topic) under documentation named **Various Mods**.

However, there are some limitations:
- You may not specify topic's ID.
- You may not specify sub-topics.
- You may not specify the sort index.

Topics in **Various Mods** documentation are always sorted alphabetically.

If you're looking for more advanced support, see **Advanced** topic. For topic formatting tips, see
the **Markdown Stress Tester**.

<buttons>
  <button topic="DocsDeveloper:advanced">Advanced</button>
  <button topic="Docs:markdown-stress-test">Markdown Stress Tester</button>
</buttons>