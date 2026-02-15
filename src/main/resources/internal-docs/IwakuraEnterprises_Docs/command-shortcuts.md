---
name: Command Shortcuts
description: Allows you to easily open specific topics
author: mayuna
---

# Command Shortcuts

**Command Shortcuts** is a feature in Docs that allows you to configure commands that will open specific **topics**.
This allows you to create accessible documentation for server's specific needs. One example is **/rules** command that
shows server's rules.

## Configuration

You may enable and configure Command Shortcuts in Docs' configuration file **config.json** located in
**mods/IwakuraEnterprises_Docs**.

Here you can add the command shortcuts. They are simple objects that specify the command's name
and the topic that will be opened.

```
{
  "name": "rules",
  "topicIdentifier": "MyGroup:MyDocumentation:rules"
}
```

This defines a command **/rules** that opens the **rules** topic in **MyDocumentation** within group **MyGroup**.

### Adding your first command shortcut

Let's say you have created documentation with ID **MyDocumentation** in group **MyGroup**. Let's create a
topic called **rules**:

```json
---
id: rules
name: Server Rules
description: Server rules that you must follow.
author: Server
---

# Rules

There are several rules you must follow:
1. Be kind
2. No trolling
3. Do not grief
4. Do not swear

Not following these rules might get you banned.
```

> **Note:** You may specify the topic's ID directly within the markdown's front-matter. If not specified, the file's
> name is used as an ID (e.g. **rules.md** will have ID **rules**).

> One would create this topic in directory like this ~ **mods/IwakuraEnterprises_Docs/documentation/MyDocumentation**
> 
> See **First steps** topic to see how you can create documentations.

<buttons>
  <button topic="DocsServerOwner:first-steps">First steps</button>
</buttons>

Upon reloading Docs with **/docs-reload**, you should be able to see new topic labeled **Server Rules**. However as of now,
you cannot open it with a command. Let's change that. Open Docs' configuration file **config.json** and add a command shortcut.

We must define the command's name (here **rules**) and what topic it will open. This is done using **Topic Identifier**
(see topic **Topic Identifiers** for more information).

<buttons>
  <button topic="Docs:topic-identifiers">Topic Identifiers</button>
</buttons>

```json
// (...)
"commandShortcuts": {
  "enabled": true,
  "commands": [
    {
      "name": "rules",
      "topicIdentifier": "MyGroup:MyDocumentation:rules"
    }
  ]
}
// (...)
```

**After restarting the server**, you should be able to run **/rules** command that will open the **Server Rules** topic.
