![Docs Banner saying Docs for Hytale, Create stunning documentations in-game!](https://akasha.iwakura.enterprises/data-source/hetzner/public/logo/docs-banner.png)

# **Docs** for Hytale

Create stunning documentation right in the game with **markdown**!

***

## tl;dr

*   Create **markdown documentation** accessible in the game
*   Access simply with **/docs** command
    *   Create shortcuts, e.g. **/rules** to see rules
*   Targeted to **Server owners** and **Developers** (DocsAPI)!

***

## Screenshots

![Topic with markdown](https://akasha.iwakura.enterprises/data-source/hetzner/public/docs-topic-with-code.png)

![Complex markdown](https://akasha.iwakura.enterprises/data-source/hetzner/public/docs-complex-markdown.png)

![Developers](https://akasha.iwakura.enterprises/data-source/hetzner/public/docs-developers.png)

![Color formatting](https://akasha.iwakura.enterprises/data-source/hetzner/public/docs-color-formatting.png)

***

## Installing

After installing the mod inside the mods folder, you can use the command **/docs** to open the documentation interface. Follow the **First steps** guide to see how to create your first documentation.

## Documentation

You may find complete documentation at [Iwakura Enterprises Docs](https://docs.iwakura.enterprises/hytale-docs.html)

## Support & suggestions

You may create issues right here on CurseForge or contact me via e-mail: `mayuna@iwakura.enterprises`

If your question will provide valuable information to other users, I'll share it in FAQ section!

### Commands

*   `/docs` -> Opens the documentation interface
*   `/docs-reload` -> Reloads config & documentations
*   `..any other command` -> Opens specific topic! (see **command shortcuts** for more info)

### Permissions

*   `iwakuraenterprises.docs.command.docs` - /docs and all its command shortcuts
*   `iwakuraenterprises.docs.command.docs-reload` - /docs-reload

## Command shortcuts

Docs allows you to create **command shortcuts**. These commands allows you to open specific topics/documentations with a simple command.

For example, as a server owner, you may create command shortcut `/rules` that will open screen with rules. This can be done in **config.json** located in **mods/IwakuraEneterprises\_Docs** in the **commandShortcuts** section:

```
"commandShortcuts": {
  "enabled": true,
  "commands": [
    {
      "name": "rules",
      "topicIdentifier": "server_rules"
    }
  ]
}
```

## Future plans

*   Improvements to the UI and UX, incl. error messages
*   Add images (from asset pack and online)
*   Create and edit documentations in-game
*   Search through documentations using full-text search
*   Per-documentation themes (each mod can have its own theme)
*   …many more things!

***

![Cirno](https://akasha.iwakura.enterprises/data-source/hetzner/public/docs-cirno.png)
