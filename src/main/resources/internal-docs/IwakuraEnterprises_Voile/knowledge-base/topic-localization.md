---
name: Topic localization
description: Translating topics
author: mayuna
---

# Topic localization

Topics can be easily localized into different languages. This can be done by creating a topic with the same ID and
specifying
a locale. You may specify a locale either in the topic's file name (`{topic-id}${locale}.md`) or in the topic's front-matter
(`locale-type` field).

For more information how to specify a locale, please see the **Topic file** topic.

<buttons>
    <button topic="topic-file">Topic file</button>
</buttons>

## Supported languages / locales

Voile currently supports over 40 different languages. All languages have a specific language code that can be used when
specifying the locale. For example, English has language code of `en`.

! You may view all supported languages by clicking the top-left button in the Voile's interface.
! Language code is specified next to language's name in a dark monospaced text.

![Screenshot of the language change button](UI/Custom/Docs/Images/Screenshots/locale-change-button-interface-example.png)

Here's a shortened list of supported locales. **This list is not complete, please use Voile's language select to see
all supported languages/locales**.

| Language name | Configuration value | Language code |
|---------------|---------------------|---------------|
| English       | `ENGLISH`           | `en`          |
| Spanish       | `SPANISH`           | `es`          |
| Portuguese    | `PORTUGUESE`        | `pt`          |
| French        | `FRENCH`            | `fr`          |
| German        | `GERMAN`            | `de`          |
| Italian       | `ITALIAN`           | `it`          |
| Dutch         | `DUTCH`             | `nl`          |
| Polish        | `POLISH`            | `pl`          |
| Russian       | `RUSSIAN`           | `ru`          |
| Ukrainian     | `UKRAINIAN`         | `uk`          |
| Swedish       | `SWEDISH`           | `sv`          |
| Norwegian     | `NORWEGIAN`         | `no`          |
| Danish        | `DANISH`            | `da`          |
| Finnish       | `FINNISH`           | `fi`          |
| Greek         | `GREEK`             | `el`          |
| Czech         | `CZECH`             | `cs`          |

! There are some easter-egg-like languages. Some of them are used when loading a topic that has duplicate locale.

## Selecting preferred language

You may select your preferred language using the top-left button above the documentation tree list (the one with a
country flag).

After selecting, Voile will prioritize showing topics with your preferred language. If the topic is not translated into
your preferred language, Voile will fall back to the English one and then to any other available language.

! Voile automatically recognizes your game's language and sets the appropriate language for you. However, this can be
! overridden by the server using configuration.

### Configuring server default language

In the configuration file in Voile's data folder (`mods/IwakuraEnterprises_Voile/config.json`) you may define interface
preference defaults. This includes the preferred language (under the `localeType` field). This field defines the default
preferred language for all players playing on the server. Setting this field to null will prefer the player's game language.

For more information how to change this value, see the **Configuration** topic.

<buttons>
    <button topic="configuration">Configuration</button>
</buttons>

## Topic IDs and locales

When translating a topic, you must use the same topic ID for the translated topic. This way Voile knows what topics are
translated and correctly displays them for the currently selected preferred language.

For example, you have a topic with ID `my-topic` in the file named `my-topic.md`. To create Czech translation of the topic,
you need to create file named `my-topic$cs.md` or specify the ID `my-topic` and locale type `cs` in a different file.

> ### File structure example
> ```
> documentation/
>   my-topic.md // contains english translation
>   my-topic$cs.md // contains czech translation
> ```
> 
> or even
> 
> ```
> documentation/
>   my-topic$en.md
>   my-topic$cs.md
>   my-topic$fi.md
> ```

!! The default locale for a topic is **English**. However, this can differ for server documentation topics, that have
!! the server's default preferred language as default, if set.

!! Mod topics always fall back to English, regardless of the server's default preferred language.