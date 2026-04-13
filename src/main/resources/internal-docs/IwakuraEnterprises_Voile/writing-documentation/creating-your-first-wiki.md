---
name: Creating your first wiki
description: Bootstrapping your knowledge
author: mayuna
sort-index: -1
---

# Big picture

Creating wikis is easy for server owners and mod developers alike. Wiki (or documentation)
is defined by a **documentation index file** and its topics are defined in **Markdown files**.

| Term                     | Description                                                               |
|--------------------------|---------------------------------------------------------------------------|
| Wiki / Documentation     | Collection of various topics. Terms are used interchangeably.             |
| Topic                    | Singular page that contains information. May contain other sub-topics.    |
| Documentation index file | Defines a documentation and its name, description, etc.                   |
| Topic front-matter       | YAML header in topic's Markdown file, defines its name, description, etc. |
| Topic identifier         | Identifier of a specific topic, used when opening topics.                 |

## Documentation index file

The documentation index file is used by Voile to create new documentation entries within its interface. It contains
various information and holds all topics. Based on the documentation type, Voile looks for it in specific places.

- Server wiki: `mods/IwakuraEnterprises_Voile/documentation/index.json`
- Mod wiki: `src/main/resources/Common/Docs/{YourModGroup}_{YourModName}.json`

! Replace the `{YourModGroup}` and `{YourModName}` with your mod's group and name respectively.
! 
! You can find these in the `src/main/resources/manifest.json` file that you've created when creating your mod.

The documentation index file follows a specific format. You can create as many documentations as you desire within one
index file. There are also some fields exclusive to index files that are loaded from mods.

```json
{
  "documentations": [
    {
      "group": "MyGroup",
      "id": "MyDocumentation",
      "name": "My lovely wiki",
      "enabled": true,
      "sortIndex": 0,
      "compatibility": {
        "mod": {
          "universalDocumentationLoader": true
        }
      }
    }
  ]
}
```

After loading the index file, Voile looks for the documentation's Markdown files in a specific folder next to the index
file. The folder's name has to be formatted in the following two ways:
- `{Group}_{id}` (e.g. `MyGroup_MyDocumentation`)
- `{id}` (e.g. `MyDocumentation`)

Voile then loads all Markdown files within the documentation's folder.

!! Voile only loads Markdown files in the folder itself. To link topics in nested folders, you need to specify them
!! using the `sub-topics` field in the topic's front-matter.

For further reading about the documentation index file, check out the **Documentation index file** topic.

<buttons>
     <button topic="documentation-index-file">Documentation index file</button>
</buttons>

## Topic file

A topic file is a simple text file with the `.md` (Markdown) file type. The topic file must contain a
YAML-formatted front-matter that defines basic information about the topic, such as its name, description and author. 
The front-matter is located at the beginning of the file surrounded by three dashes (`---`). Everything after the
front-matter is considered as topic's Markdown content.

All topics have an ID. This ID is created from topic's file name or specifically specified within topic's front-matter
using the `id` field.

```
---
name: My topic
description: My lovely topic
author: Myself
---

# This is my topic
That contains various information.
```

Topic files are located within the documentation's folder. Voile loads all topics within the folder, but not in nested
folders.

For further reading about the topic file, check out the **Topic file** topic.

<buttons>
     <button topic="topic-file">Topic file</button>
</buttons>

# Creating a server wiki

1. Open the `index.json` file located at `mods/IwakuraEnterprises_Voile/documentation/index.json`
2. Add a new documentation entry with a documentation group, ID, and a name
3. Create a folder named after the documentation group and ID next to the `index.json` file
4. Create a Markdown file within the created folder with any name
5. Run `/voile-reload` command

!! Always check console for any errors while reloading Voile.

The result may look like this:

> ### The edited `index.json` file
> 
> ```json
> {
>   "documentations": [
>     {
>       "group": "MyGroup",
>       "id": "MyDocumentation",
>       "name": "My lovely wiki"
>     }
>   ]
> }
> ```

> ### The created topic file
> 
> ```md
> ---
> name: My topic
> description: My lovely topic
> author: Myself
> ---
> 
> # This is my topic
> That contains various information.
> ```

> ### File structure
> 
> ```
> mods/IwakuraEnterprises_Voile/
>   index.json
>   MyGroup_MyDocumentation/
>     my-topic.md
> ```

![Screenshot of the example wiki {800x0}](UI/Custom/Docs/Images/Screenshots/my-wiki-my-topic-example.png)

# Creating a mod wiki

1. Create a documentation index file at `src/main/resources/Common/Docs/{YourModGroup}_{YourModName}.json` in your mod's resources
2. Add a new documentation entry with your mod's group, documentation ID and a name.
3. **Make sure to include the compatibility field** with enabled `universalDocumentationLoader` option
4. Create a folder named after your mod's group and documentation ID next to the documentation index file
5. Create a Markdown file within the created folder with any name
6. Reinstall the mod and restart the server

!! Always check console for any errors while reloading Voile.

! If you're looking for simple one-file integration, check out the **Simple mod integration** topic.

The result may look like this:

> ### The created index file
>
> ```json
> {
>   "documentations": [
>     {
>       "group": "MyGroup",
>       "id": "MyModDocumentation",
>       "name": "My Mod",
>       "compatibility": {
>         "mod": {
>            "universalDocumentationLoader": true
>         }
>       }
>     }
>   ]
> }
> ```

> ### The created topic file
>
> ```md
> ---
> name: My topic
> description: My lovely topic
> author: Myself
> ---
> 
> # This is my topic
> That contains various information.
> ```

> ### File structure
>
> ```
> src/main/java/
>   ...
> src/main/resources/
>   Common/Docs/
>     MyGroup_MyModDocumentation.json
>     MyGroup_MyModDocumentation/
>       my-topic.md
> ```

! Depending on your development environment, you may need to include your mod's JAR within the `mods` directory. If your mod is
! just loaded from a classpath and/or build directory, it may not be correctly recognized by Voile. Always try installing
! your mod with Voile integration in a production-like server environment.

!! If you have such development environment, and you're having problems, you can contact me via &9&lhttps://support.voile.dev
!! 
!! I will gladly replicate your development environment and try adding support for it in Voile.

<buttons>
    <button url="https://support.voile.dev">Support</button>
</buttons>

# Further reading

This topic shows just the bare basics of Voile. Please check out other topics that will teach you all the other features
of Voile.

<buttons>
     <button topic="tutorial-server-wiki">Server wiki tutorial</button>
     <button topic="tutorial-mod-integration">Mod integration tutorial</button>
</buttons>

<buttons>
     <button topic="documentation-index-file">Documentation index file</button>
     <button topic="topic-file">Topic file</button>
     <button topic="markdown-syntax">Markdown syntax</button>
</buttons>

<buttons>
     <button topic="simple-mod-integration">Simple mod integration</button>
</buttons>