---
name: Server Wiki
description: Step-by-step tutorial creating server wiki
author: mayuna
sort-index: -1
---

# Server Wiki tutorial

In this tutorial, you'll learn how to:

1. Create a documentation
2. Create a topic
3. Create sub-topics for the created topic
4. Localize the topic
5. Create a command shortcut for the topic

# Step 1 - Creating a documentation

1. Navigate to server's `mods` folder and open Voile's data folder named `IwakuraEnterprises_Voile`.
2. Inside Voile's data folder, there will be a few files and folders.
    1. `config.json` used to configure Voile
    2. `cache` containing downloaded images and other data
    3. `documentation` containing server's wiki/documentation
    4. `generated_ui_errors` for any UI-related errors
3. Open the `documentation` folder. Inside this folder, you'll be able to create the server wiki.
4. Open the `index.json` file inside the folder.

This `index.json` file contains all documentation definitions for the server. Let's add a new documentation:

```json
{
  "documentations": [
    {
      "group": "MyGroup",
      "id": "MyDocumentation",
      "name": "My lovely wiki"
    }
  ]
}
```

This will create a new server wiki under the group `MyGroup`, with documentation ID `MyDocumentation` and with name
`My lovely wiki`.

! The documentation's group and ID is also used when linking topics.

# Step 2 - Creating a topic

1. Inside the `documentation` folder, next to the `index.json` file, let's create a new folder named
   `MyGroup_MyDocumentation`. This folder will contain all topics for the documentation.
2. Inside the created folder, create a new text file called `my-topic.md`

This will create a new topic within the documentation. The text file contains information about the topic and the topic's
Markdown content.

```md
---
name: My topic
description: My lovely topic
author: Myself
---

# This is my topic
That contains various information.
```

3. Save the file and head over to Hytale.
4. Run the `/voile-reload` command.

Now, after opening Voile's interface with the /voile command, you should be able to see the newly created documentation
named **My lovely wiki** with one topic.

![Screenshot of the my topic {800x0}](UI/Custom/Docs/Images/Screenshots/my-wiki-my-topic-example.png)

# Step 3 - Creating sub-topics

All files with the `.md` file type inside the `MyDocumentation` folder will be included in the **My lovely wiki**
documentation. To add a sub-topic to a topic, you need to create a new text file and specify it as a sub-topic.

1. Create a new text file named `rules.md` next to the `my-topic.md` file.

```md
---
name: Rules
description: Server's rules
author: Myself
---

# Rules
1. Do not grief
2. Be kind
3. Enjoy the game
```

2. Open the `my-topic.md` file and add `sub-topics` field inside the topic's front-matter.

```md
---
name: My topic
description: My lovely topic
author: Myself
sub-topics:
  - rules
---

# This is my topic
That contains various information.
```

This will add the **Rules** topic as a sub-topic for the **My topic** topic. Inside the sub-topics list, you specify either a topic
ID (usually based on the topic's file name) or a folder name (to include all topics inside the folder).

3. Head over to Hytale and run the `/voile-reload` command.

After opening Voile's interface, you should be able to see two topics, one named **My topic** and the second, under it, 
named **Rules**.

![Screenshot of the rules topic {800x0}](UI/Custom/Docs/Images/Screenshots/my-wiki-rules-topic-example.png)

# Step 4 - Localizing the topic

To translate a topic, create a new text file next to the topic you want to localize. Let's translate the **My topic** topic.

1. Create a new text file named `my-topic$cs.md` next to the `my-topic.md` file.

```md
---
name: Můj topic
description: Můj milovaný topic
author: Já
---

# Toto je můj topic
Který obsahuje nějaké informace.
```

This will create localized topic for topic ID `my-topic` with the language code `cs`.

2. Head over to Hytale and run the `/voile-reload` command.

After opening Voile's interface, there should not be any change. Open the language selection menu by clicking the top-left
button and select Czech.

Now you should be able to see the first topic named **Můj topic** and the topic's content should be translated as well.

! Inside the language selection menu, you can see all available languages, including their language codes in a dark
! monospaced text.

![Screenshot of the localized topic {800x0}](UI/Custom/Docs/Images/Screenshots/my-wiki-localized-topic-example.png)

# Step 5 - Creating a command shortcut

To allow players to quickly open a certain topic, you can create **command shortcuts**. For example, let's create a command
shortcut `/rules` that will open the `rules` topic.

1. Open the `config.json` inside the `IwakuraEnterprises_Voile` folder.
2. Head down and add a command shortcut to the `commandShortcuts` list.

```json
"commandShortcuts": {
  "enabled": true,
  "commands": [
    {
      "name": "rules",
      "topicIdentifier": "MyGroup:MyDocumentation:rules"
    }
  ]
}
```

This will create command `/rules` that will open the **Rules** topic by its topic identifier.

3. Restart the Hytale server.

After restarting the server, you should be able to run `/rules` command to open the **Rules** topic automatically.

# Further reading

To learn more, please see the following topics.

! You may create administrator-only documentations that will be only visible to certain players with specific permissions.
! For more information, check the **Documentation index file** and **Topic file** topics.

<buttons>
   <button topic="documentation-index-file">Documentation index file</button>
   <button topic="topic-file">Topic file</button>
   <button topic="topic-identifiers">Topic identifiers</button>
   <button topic="topic-localization">Topic localization</button>
   <button topic="markdown-syntax">Markdown syntax</button>
   <button topic="configuration">Configuration</button>
   <button topic="command-shortcuts">Command shortcuts</button>
   <button topic="interface">User Interface</button>
</buttons>