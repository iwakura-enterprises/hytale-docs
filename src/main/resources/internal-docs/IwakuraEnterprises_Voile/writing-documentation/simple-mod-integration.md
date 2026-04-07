---
name: Simple mod integration
description: One-file integration
author: mayuna
sort-index: 999
---

# One-file mod integration

If you don't want to create full documentation for your mod, and you need just one topic to document your mod,
you may do that using this **one-file mod integration**. All you need is to create a **single Markdown file at a specific
location within your mod's resources**.

!! One-file mod integration is limiting. Some features such as localization and relative image asset resolution may
!! not work properly. If you want to localize your mod documentation, please use the full-fledged integration using a
!! documentation index file.

<buttons>
    <button topic="tutorial-mod-integration">Mod integration tutorial</button>
    <button topic="creating-your-first-wiki">Creating your first wiki</button>
    <button topic="documentation-index-file">Documentation index file</button>
    <button topic="topic-file">Topic file</button>
</buttons>

## The Markdown file

To add one-file mod integration, create a Markdown file located in your mod's resources at

```
src/main/resources/Common/Docs/{YourModGroup}_{YourModName}.md
```

! Replace the `{YourModGroup}` and `{YourModName}` with your mod's group and name respectively.
! 
! You can find these in the `src/main/resources/manifest.json` file that you've created when creating your mod.

After creating the file, specify its front-matter and the Markdown content after it.

```md
---
name: My mod documentation
description: How to use my mod
author: Myself
---

# My mod
My mod adds ... and ...
```

Save the file, recompile the mod, restart your development server and you should be able to see documentation titled
**Various mods** under which your topic named **My mod documentation** will be shown. This documentation is shown after
all other mod documentations.

![Screenshot of the various mod documentation {800x0}](UI/Custom/Docs/Images/Screenshots/simple-mod-integration-example.png)

For further reading, please see the following topics.

<buttons>
    <button topic="topic-file">Topic file</button>
    <button topic="markdown-syntax">Markdown syntax</button>
</buttons>