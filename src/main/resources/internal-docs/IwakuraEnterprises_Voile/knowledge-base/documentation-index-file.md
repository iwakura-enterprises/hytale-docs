---
name: Documentation index file
description: How to declare a documentation
author: mayuna
sort-index: -70
---

# Documentation index file

## What is it?

The documentation index file is used by Voile to create new documentation entries within its interface. It contains
various information and holds all topics. Based on the documentation type, Voile looks for it in specific places.

- Server wiki: `mods/IwakuraEnterprises_Voile/documentation/index.json`
- Mod wiki: `src/main/resources/Common/Docs/{YourModGroup}_{YourModName}.json`

## Format

The documentation index file follows strict JSON format.

### Full JSON example

```json
{
  "documentations": [
    {
      "group": "MyGroup",
      "id": "MyExtensiveDocumentation",
      "name": "My extensive wiki",
      "enabled": true,
      "sortIndex": 1100,
      "compatibility": {
        "mod": {
          "universalDocumentationLoader": true
        }
      }
    },
    {
      "group": "MyGroup",
      "id": "MyDocumentation",
      "name": "My lovely wiki"
    }
  ]
}
```

> ### Root documentation index file object, `DocumentationIndexConfig`
>
> | Field            | Type                          | Description                   |
> |------------------|-------------------------------|-------------------------------|
> | `documentations` | List of `DocumentationConfig` | List of documentation entries |

> ### `DocumentationConfig` object
>
> | Field           | Type                 | Description                                                          |
> |-----------------|----------------------|----------------------------------------------------------------------|
> | `group`         | String               | **Mandatory** Documentation's group                                  |
> | `id`            | String               | **Mandatory** Documentation's ID                                     |
> | `name`          | String               | **Mandatory** Documentation's name (shown in the UI)                 |
> | `enabled`       | Boolean              | Whether the documentation should be shown in the UI                 |
> | `sortIndex`     | Integer              | Sorting index for the documentation. Lower values are sorted first.  |
> | `compatibility` | `Compatibility`      | Various compatibility options                                        |
> | `topics`        | List of `TopicEntry` | **Deprecated** and available only for mod documentation index files. |

> ### `Compatibility` object
>
> | Field | Type  | Description                                  |
> |-------|-------|----------------------------------------------|
> | `mod` | `Mod` | Compatibility options for mod documentations |

> ### `Mod` object
>
> | Field                          | Type    | Description                                                                                                                                                        |
> |--------------------------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
> | `universalDocumentationLoader` | Boolean | Whether the `UniversalDocumentationLoader` can be used to load mod's topics. If disabled, uses old `ResourceDocumentationLoader` that reads only specified topics. |

> ### `TopicEntry` object
> !! This object/option is deprecated. Please, enable the `universalDocumentationLoader` within the `Compatibility` object.
> 
> | Field       | Type                 | Description                                                                 |
> |-------------|----------------------|-----------------------------------------------------------------------------|
> | `file`      | String               | File path to the topic file starting from the documentation root directory. |
> | `subTopics` | List of `TopicEntry` | Recursive reference to other topic objects.                                 |

# Documentation loaders

Voile has various documentation loaders that load different types of documentations.

## `UniversalDocumentationLoader`

!v Currently used documentation loader for the majority of the documentations that are loaded by Voile.

Used when loading server documentation and mod documentation that enables the `universalDocumentationLoader`
compatibility. Supports all features Voile provides (topic localization, relative image paths, etc.)

- Server documentations are loaded from `mods/IwakuraEnterprises_Voile/documentation/index.json`
- Mod documentations are loaded from `src/main/resources/Common/Docs/{YourModGroup}_{YourModName}.json`

! Supports topic discovery - automatically includes all topics found within the documentation's root directory. Any
! nested folders are exempt from this discovery (must be specified using the `sub-topics` field in the topic's
! front-matter).

## `ResourceMarkdownFileDocumentationLoader`

Used when loading one-file mod integration Markdown files. The Markdown files are loaded from
`src/main/resources/Common/Docs/{YourModGroup}_{YourModName}.md`

!! Does not support topic localization, relative image paths, etc.

## `DirectDocumentationLoader`

Programmatically provides documentations. Only accessible to programmatic integrations using the `VoileAPI`.

## `FileSystemDocumentationLoader`

!! Deprecated in favor of `UniversalDocumentationLoader`.

Used to load server documentations from `mods/IwakuraEnterprises_Voile/documentation` directory.

## `ResourcesDocumentationLoader`

!! Deprecated in favor of `UniversalDocumentationLoader`.
!! 
!! Does not support topic localization and relative image paths.

Used to load mod documentations from the `src/main/resources/Common/Docs/{YourModGroup}_{YourModName}.json` directory.
These documentations must specify the topic files manually, as there is no topic discovery.

## `HMWikiDocumentationLoader`

! Integration with the Hytale Modding Wiki. Learn more in the **Integrations** topic.

Loads wiki pages from Hytale Modding Wiki as Voile documentations. Currently, it does not support topic localization and
may occasionally display content incorrectly.