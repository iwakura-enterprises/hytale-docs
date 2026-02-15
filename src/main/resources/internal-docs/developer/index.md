---
name: Home
description: DEVELOPERS DEVELOPERS DEVELOPERS DEVELOPERS
author: mayuna
---

# Docs <3 Developers

You may register documentations right from mods. This is done using the **DocsAPI** class.

## Installing Docs dependency

> TBD when Docs will be released on CurseForge.
> 
> Docs is a standalone plugin, so you **should not** shadow it in your plugin.

## Using DocsAPI

In your **setup** method, check if **Docs** is loaded and if so, register your documentation:

```java
public void setup() {
    // Reference to your JavaPlugin
    JavaPlugin myPlugin = this;

    // Gets the Docs' JavaPlugin instance
    var docsPlugin = PluginManager.get().getPlugin(
        PluginIdentifier.fromString("IwakuraEnterprises:Docs")
    );

    // Is Docs' installed?
    if (docsPlugin != null) {
        // Defines a documentation
        var documentation = Documentation.builder()
            .type(DocumentationType.MOD)
            .group("MyGroup")
            .id("MyModDocumentation")
            .name("My mod documentation")
            .build();

        // Defines a topic
        var topic = Topic.builder()
            .id("my_topic")
            .name("My topic")
            .description("This is my mod topic!")
            .author("the hacker known as 4chan")
            .markdownContent(
                """
                # This is markdown!
                
                You can use markdown formatting here.
                """
            )
            .build();

        // Adds the topic to the documentation
        documentation.addTopics(topic);

        // Registers the documentation
        DocsAPI.get().register(myPlugin, documentation);
    } else {
        // Docs not installed
    }
}
```

The **DocsAPI** has more ways to register your documentation. For example using **DocumentationLoaders**.
- **DirectDocumentationLoader** - Directly returns **List** of **Documentation** objects
- **ResourcesDocumentationLoader** - Reads your plugin's resources for
  documentations. Please, **read the class' Javadocs** for more information, as it is quite complex.

You can reload loaded documentations using **/docs-reload** command.

## Inspiration

You may find this documentation on GitHub: https://github.com/iwakura-enterprises/hytale-docs
(<gray>non-clickable sadly! There should be a link to source on CurseForge's page.</gray>)