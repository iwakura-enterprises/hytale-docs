---
name: User Interface
description: Understanding the UI
author: mayuna
sort-index: -80
---

# Voile's User Interface

Voile's interface is accessible via the `/voile` command.

Voile's UI is separated into three distinct parts:
1. (left) Documentation tree with buttons
2. (middle) Topic content
3. (right) Topic chapters

![Screenshot of Voile's interface](UI/Custom/Docs/Images/Screenshots/voile-interface-example.png)

## 1. Documentation tree with buttons

On the left side, there are buttons and a documentation tree.

Using the buttons, you may configure, navigate and search server and mod documentation. From left to right:
1. Language select
2. Interface mode
3. Previous topic
4. Next topic
5. Home
6. Full-text search toggle
7. Search bar
8. Documentation tree

![Screenshot of the language change button](UI/Custom/Docs/Images/Screenshots/locale-change-button-interface-example.png)

### 1.1. Language select button

Using the language select, it is possible to open a menu for selecting a preferred language. The menu also allows you to reset
the player's interface preferences to factory defaults.

Player's interface preferences (language select, currently open topic, interface mode etc.) are persisted to the disk
(this can be disabled in Voile's configuration).

![Screenshot of the language selection menu {300x0}](UI/Custom/Docs/Images/Screenshots/language-select-menu-example.png)

### 1.2. Interface mode button

With the interface mode button, you can switch between available interface modes. This includes all of Voile's integrations
and different views. Each interface mode has specific documentation types.

| Interface mode      | Documentation types                                     |
|---------------------|---------------------------------------------------------|
| Voile               | Server Wiki, Mod Wiki, External Mod Wiki, Internal Wiki |
| Hytale Modding Wiki | HMW's Installed Mods, HMW's Mods                        |

If no documentation types are enabled for the interface mode, it won't be accessible.

Hovering over the button shows all available interface modes. If no interface modes are available, the button is grayed
out.

### 1.3. Previous topic button

Voile holds recently visited topics. Using the previous topic button, you're able to navigate to the previously opened
topic.

Hovering over the button shows the current topic history.

### 1.4. Next topic button

Similar to the 1.3. Previous topic, this button allows you to navigate to the next topic, if you've navigated to a previously
opened topic.

The next topic resets when you open a topic. Hovering over the button shows the current topic history.

### 1.5. Home button

Opens the first found topic in the currently open interface mode.

### 1.6. Full-text search toggle button

Toggles the full-text search functionality. When enabled, the search bar will search in the topic's content.

The full-text search functionality can be disabled in Voile's configuration.

### 1.7. Search bar

The search bar, under the buttons, allows you to search in the currently visible topics in the documentation tree. Found
topics based on the search query are highlighted with a blue-ish color.

If the search query is satisfied by a child topic, all of its parent topics are in the search results as well. However,
they won't be highlighted.

### 1.8. Documentation tree

Under the buttons and search bar, there's a documentation tree. Documentation tree shows all documentations and topics
within those documentations. Documentation tree can be filtered by the search bar.

To open a topic, one must click the topic label inside the documentation tree. However, categories cannot be opened.
Categories are rendered in bold.

## 2. Topic content

In the middle, there is the currently opened topic and its content.

If an error occurs while rendering the topic's content, the error message will be shown here as well.

## 3. Topic chapters

On the right side, a tree of chapters for the currently opened topic is visible. This tree is purely visual and currently
does not have any functionality.