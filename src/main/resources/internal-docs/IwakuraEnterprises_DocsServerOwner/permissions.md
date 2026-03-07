---
name: Permissions
description: List of all Voile permissions
author: mayuna
---

# Command Permissions

- `iwakuraenterprises.voile.command.voile` -> /voile, /docs, /wiki
- `iwakuraenterprises.voile.command.voile-reload` -> /voile-reload, /docs-reload

## Command Shortcuts

Command shortcuts add extra aliases for the /voile command. However, when you enable **overrideHytaleCommands** in the
config each command shortcut will have its own permission. This is due to them being registered as standalone commands
rather than alias.

For example, command shortcut **/rules** will have permission `iwakuraenterprises.voile.command.rules`