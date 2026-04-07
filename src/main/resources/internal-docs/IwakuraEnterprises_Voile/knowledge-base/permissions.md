---
name: Permissions
description: List of all Voile permissions
author: mayuna
---

# Command Permissions

```
- iwakuraenterprises.voile.command.voile -> /voile, /docs, /wiki
- iwakuraenterprises.voile.command.voile-locale -> /voile-locale
- iwakuraenterprises.voile.command.voile-reload -> /voile-reload, /docs-reload
- iwakuraenterprises.voile.command.voile-debug -> /voile-debug
```

! Command permission for the /voile command is disabled by default. This can be changed in the configuration via the
! `voileCommandRequiresPermission` field.

<buttons>
    <button topic="configuration">Configuration</button>
</buttons>

## Command Shortcuts

Command shortcuts add extra aliases for the `/voile` command. However, when `overrideHytaleCommands` is enabled in Voile's
configuration, each command shortcut will have its own permission.

For example, the command shortcut `/rules` will have permission `iwakuraenterprises.voile.command.rules`

<buttons>
    <button topic="command-shortcuts">Command shortcuts</button>
</buttons>