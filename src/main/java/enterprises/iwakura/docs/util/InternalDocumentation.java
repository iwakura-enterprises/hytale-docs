package enterprises.iwakura.docs.util;

import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.service.loader.DirectDocumentationLoader;
import enterprises.iwakura.docs.service.loader.DocumentationLoader;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InternalDocumentation {

    public static Documentation create() {
        var topic = Topic.builder()
            .id("markdown-stress-test")
            .name("Markdown Stress Tester")
            .description("This topic showcases the power of Docs' markdown renderer.")
            .author("mayuna, markdown file from https://github.com/mxstbr/markdown-test-file, edited by mayuna (MIT License)")
            .markdownContent(
                """
# Markdown: Syntax

*   [Overview](#overview)
    *   [Philosophy](#philosophy)
    *   [Inline HTML](#html)
    *   [Automatic Escaping for Special Characters](#autoescape)
*   [Block Elements](#block)
    *   [Paragraphs and Line Breaks](#p)
    *   [Headers](#header)
    *   [Blockquotes](#blockquote)
    *   [Lists](#list)
    *   [Code Blocks](#precode)
    *   [Horizontal Rules](#hr)
*   [Span Elements](#span)
    *   [Links](#link)
    *   [Emphasis](#em)
    *   [Code](#code)
    *   [Images](#img)
*   [Miscellaneous](#misc)
    *   [Backslash Escapes](#backslash)
    *   [Automatic Links](#autolink)


> **Note (from mayuna):** There are some limitations to the markdown renderer. URLs do not
show, some specific niches also aren't properly displayed. However, the vast majority
of the markdown is correctly rendered.

> **Note (from mayuna):** Check out <gradient data="red:yellow:green:blue:purple">color formatting</gradient>
> and button formatting at the bottom of this topic!

## Overview

### Philosophy

Readability, however, is emphasized above all else. A Markdown-formatted
document should be publishable as-is, as plain text, without looking
like it's been marked up with tags or formatting instructions. While
Markdown's syntax has been influenced by several existing text-to-HTML
filters -- including [Setext](http://docutils.sourceforge.net/mirror/setext.html), [atx](http://www.aaronsw.com/2002/atx/), [Textile](http://textism.com/tools/textile/), [reStructuredText](http://docutils.sourceforge.net/rst.html),
[Grutatext](http://www.triptico.com/software/grutatxt.html), and [EtText](http://ettext.taint.org/doc/) -- the single biggest source of
inspiration for Markdown's syntax is the format of plain text email.

Markdown is intended to be as easy-to-read and easy-to-write as is feasible.

> **Note (from mayuna):** Markdown supports up-to 6th heading. The render respects this. To
distinguish between the lower headings and normal text, their size is lot smaller than paragraphs'.

#### This

Readability, however, is emphasized above all else. A Markdown-formatted
document should be publishable as-is, as plain text, without looking
like it's been marked up with tags or formatting instructions. While
Markdown's syntax has been influenced by several existing text-to-HTML filters.

##### And that

Readability, however, is emphasized above all else. A Markdown-formatted
document should be publishable as-is, as plain text, without looking
like it's been marked up with tags or formatting instructions. While
Markdown's syntax has been influenced by several existing text-to-HTML filters.

###### Also this level

Readability, however, is emphasized above all else. A Markdown-formatted
document should be publishable as-is, as plain text, without looking
like it's been marked up with tags or formatting instructions. While
Markdown's syntax has been influenced by several existing text-to-HTML
filters.

## Block Elements

### Paragraphs and Line Breaks

A paragraph is simply one or more consecutive lines of text, separated
by one or more blank lines. (A blank line is any line that looks like a
blank line -- a line containing nothing but spaces or tabs is considered
blank.) Normal paragraphs should not be indented with spaces or tabs.

The implication of the "one or more consecutive lines of text" rule is
that Markdown supports "hard-wrapped" text paragraphs. This differs
significantly from most other text-to-HTML formatters (including Movable
Type's "Convert Line Breaks" option) which translate every line break
character in a paragraph into a `<br />` tag.

When you *do* want to insert a `<br />` break tag using Markdown, you
end a line with two or more spaces, then type return.

### Headers

Markdown supports two styles of headers, [Setext] [1] and [atx] [2].

Optionally, you may "close" atx-style headers. This is purely
cosmetic -- you can use this if you think it looks better. The
closing hashes don't even need to match the number of hashes
used to open the header. (The number of opening hashes
determines the header level.)


### Blockquotes

Markdown uses email-style `>` characters for blockquoting. If you're
familiar with quoting passages of text in an email message, then you
know how to create a blockquote in Markdown. It looks best if you hard
wrap the text and put a `>` before every line:

> This is a blockquote with two paragraphs. Lorem ipsum dolor sit amet,
> consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
> Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.
>\s
> Donec sit amet nisl. Aliquam semper ipsum sit amet velit. Suspendisse
> id sem consectetuer libero luctus adipiscing.

Markdown allows you to be lazy and only put the `>` before the first
line of a hard-wrapped paragraph:

> This is a blockquote with two paragraphs. Lorem ipsum dolor sit amet,
consectetuer adipiscing elit. Aliquam hendrerit mi posuere lectus.
Vestibulum enim wisi, viverra nec, fringilla in, laoreet vitae, risus.

> Donec sit amet nisl. Aliquam semper ipsum sit amet velit. Suspendisse
id sem consectetuer libero luctus adipiscing.

> **Note (from mayuna):** This blockquote and the two above should be merged but they are not. This
is one of the limitations.

Blockquotes can be nested (i.e. a blockquote-in-a-blockquote) by
adding additional levels of `>`:

> This is the first level of quoting.
>
> > This is nested blockquote.
> > > Third level?
> > > > Fourth level?
> > > > > Fifth level?
>
> Back to the first level.

> **Note (from mayuna):** The markdown renderer can nest unlimited number of blockquotes inside
each other. However, I have not tested if it's actually unlimited...

Blockquotes can contain other Markdown elements, including headers, lists,
and code blocks:

> ## This is a header.
>
> 1.   This is the first list item.
> 2.   This is the second list item.
>
> Here's some example code:
>
>     return shell_exec("echo $input | $markdown_script");

Any decent text editor should make email-style quoting easy. For
example, with BBEdit, you can make a selection and choose Increase
Quote Level from the Text menu.


### Lists

Markdown supports ordered (numbered) and unordered (bulleted) lists.

Unordered lists use asterisks, pluses, and hyphens -- interchangably
-- as list markers:

*   Red
*   Green
*   Blue

is equivalent to:

+   Red
+   Green
+   Blue

and:

-   Red
-   Green
-   Blue

Ordered lists use numbers followed by periods:

1.  Bird
2.  McHale
3.  Parish

It's important to note that the actual numbers you use to mark the
list have no effect on the HTML output Markdown produces. The HTML
Markdown produces from the above list is:

If you instead wrote the list in Markdown like this:

1.  Bird
1.  McHale
1.  Parish

or even:

3. Bird
1. McHale
8. Parish

you'd get the exact same HTML output. The point is, if you want to,
you can use ordinal numbers in your ordered Markdown lists, so that
the numbers in your source match the numbers in your published HTML.
But if you want to be lazy, you don't have to.

To make lists look nice, you can wrap items with hanging indents:

*   Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
    Aliquam hendrerit mi posuere lectus. Vestibulum enim wisi,
    viverra nec, fringilla in, laoreet vitae, risus.
*   Donec sit amet nisl. Aliquam semper ipsum sit amet velit.
    Suspendisse id sem consectetuer libero luctus adipiscing.

But if you want to be lazy, you don't have to:

*   Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
Aliquam hendrerit mi posuere lectus. Vestibulum enim wisi,
viverra nec, fringilla in, laoreet vitae, risus.
*   Donec sit amet nisl. Aliquam semper ipsum sit amet velit.
Suspendisse id sem consectetuer libero luctus adipiscing.

List items may consist of multiple paragraphs. Each subsequent
paragraph in a list item must be indented by either 4 spaces
or one tab:

1.  This is a list item with two paragraphs. Lorem ipsum dolor
    sit amet, consectetuer adipiscing elit. Aliquam hendrerit
    mi posuere lectus.

    Vestibulum enim wisi, viverra nec, fringilla in, laoreet
    vitae, risus. Donec sit amet nisl. Aliquam semper ipsum
    sit amet velit.

2.  Suspendisse id sem consectetuer libero luctus adipiscing.

It looks nice if you indent every line of the subsequent
paragraphs, but here again, Markdown will allow you to be
lazy:

*   This is a list item with two paragraphs.

    This is the second paragraph in the list item. You're
only required to indent the first line. Lorem ipsum dolor
sit amet, consectetuer adipiscing elit.

*   Another item in the same list.

To put a blockquote within a list item, the blockquote's `>`
delimiters need to be indented:

*   A list item with a blockquote:

    > This is a blockquote
    > inside a list item.

To put a code block within a list item, the code block needs
to be indented *twice* -- 8 spaces or two tabs:

*   A list item with a code block:

        <code goes here>

### Code Blocks

Pre-formatted code blocks are used for writing about programming or
markup source code. Rather than forming normal paragraphs, the lines
of a code block are interpreted literally. Markdown wraps a code block
in both `<pre>` and `<code>` tags.

To produce a code block in Markdown, simply indent every line of the
block by at least 4 spaces or 1 tab.

This is a normal paragraph:

    This is a code block.

Here is an example of AppleScript:

    tell application "Foo"
        beep
    end tell

A code block continues until it reaches a line that is not indented
(or the end of the article).

Within a code block, ampersands (`&`) and angle brackets (`<` and `>`)
are automatically converted into HTML entities. This makes it very
easy to include example HTML source code using Markdown -- just paste
it and indent it, and Markdown will handle the hassle of encoding the
ampersands and angle brackets. For example, this:

    <div class="footer">
        &copy; 2004 Foo Corporation
    </div>

Regular Markdown syntax is not processed within code blocks. E.g.,
asterisks are just literal asterisks within a code block. This means
it's also easy to use Markdown to write about Markdown's own syntax.

```
tell application "Foo"
    beep
end tell
```

> **Note (from mayuna):** Some of the code blocks are darker. That's because they are
`IndentedCodeBlock`s. I have chosen darker color for them; it looks better when nested inside
block quotes. Further releases might change this behaviour to only appear in these nested
block quotes.

## Span Elements

### Links

Markdown supports two style of links: *inline* and *reference*.

In both styles, the link text is delimited by [square brackets].

To create an inline link, use a set of regular parentheses immediately
after the link text's closing square bracket. Inside the parentheses,
put the URL where you want the link to point, along with an *optional*
title for the link, surrounded in quotes. For example:

This is [an example](http://example.com/) inline link.

[This link](http://example.net/) has no title attribute.

### Emphasis

Markdown treats asterisks (`*`) and underscores (`_`) as indicators of
emphasis. Text wrapped with one `*` or `_` will be wrapped with an
HTML `<em>` tag; double `*`'s or `_`'s will be wrapped with an HTML
`<strong>` tag. E.g., this input:

*single asterisks*

_single underscores_

**double asterisks**

__double underscores__

### Code

To indicate a span of code, wrap it with backtick quotes (`` ` ``).
Unlike a pre-formatted code block, a code span indicates code within a
normal paragraph. For example:

Use the `printf()` function.

## Thematic breaks

The markdown render also supports thematic breaks.

---

They are used to signify a structural shift or transition between topics.

## Colors

The markdown renderer also supports various color formatting. This is done thanks to
TaleMessage (https://github.com/InsiderAnh/TaleMessage).

### Minecraft-style colors

TaleMessage supports Minecraft-styled coloring using the ampersand (&) symbol:
- `&0` &0for Black color
- `&1` &1for Dark Blue color
- `&2` &2for Dark Green color
- `&3` &3for Dark Turquoise color
- `&4` &4for Dark Red color
- `&5` &5for Purple color
- `&6` &6for Dark Yellow color
- `&7` &7for Light Gray color
- `&8` &8for Dark Gray color
- `&9` &9for Light Blue color
- `&a` &afor Light Green color
- `&b` &bfor Light Turquoise color
- `&c` &cfor Light Red color
- `&d` &dfor Magenta color
- `&e` &efor Light Yellow color
- `&f` &ffor White color

### Inline HTML color codes

TaleMessage also supports inline HTML color codes formatted like `<red>text</red>`:
- <red>Red</red>
- <green>Green</green>
- <blue>Blue</blue>

Hexadecimal values are also supported:
- <#e5da52>Written in hex!</#e5da52>

You may even write colors in decimal values like `<255,85,85>`:
- <255,85,85>Red in RGB</255,85,85>
- <128,64,200>Custom color</128,64,200>

### Gradients

TaleMessage also adds support for gradients. However, due to markdown rendering, they are defined like this:

```
<gradient data="red:yellow:green:blue:purple">Text in gradient color!</gradient>
```

This example results in <gradient data="red:yellow:green:blue:purple">text in the rainbow gradient!</gradient>

> **Fun fact:** The render uses commands to fill in the stylized text. This single topic uses nearly **~230** commands!

## Buttons - linking other topics

Due to Hytale's limitation, there cannot be inline links to other topics. As a compromise, Docs
allows you to create buttons using HTML block.

```
<buttons>
    <button topic="my_topic">My Topic</button>
    <button topic="MyGroup:my_topic">My Topic</button>
    <button topic="MyDocumentation:my_topic">My Topic</button>
    <button topic="MyGroup:MyDocumentation:my_topic">My Topic</button>
</buttons>
```

This will create four buttons linking the same topic. As you can see, you must specify the topic's
ID. You may also specify either a documentation group or documentation id (or both!) **If there would**
**be duplicate topic IDs, the button might not link to the correct topic.** An example button is here:

<buttons>
    <button topic="IwakuraEnterprises:InternalDocs:markdown-stress-test">Markdown Stress Test</button>
</buttons>

This button links the current topic with documentation group, ID and topic ID. <gray>Clicking it won't do anything as you're currently on the topic!</gray>

> More button types might be added in future releases.
                    """)
            .build();

        var anotherTopic = Topic.builder()
            .id("another_topic")
            .name("Another topic!")
            .description("Here's another topic.")
            .author("mayuna")
            .markdownContent("Here's another topic that has **two child** topics.")
            .build()
            .addTopics(
                Topic.builder()
                    .id("another_topic_child0")
                    .name("I am a first child.")
                    .description("Yeppers! I am the first child.")
                    .author("mayuna")
                    .markdownContent("""
                        Yippe, here's link to <click data="IwakuraEnterprises:another_topic_child1">to second child</click>!
                        
                        <buttons>
                            <button topic="IwakuraEnterprises:another_topic_child1">Click me to get to second child!</button>
                            <button topic="IwakuraEnterprises:another_topic_child1">Go to "second" child</button>
                            <button topic="IwakuraEnterprises:another_topic_child1">Second child</button>
                            <button topic="IwakuraEnterprises:another_topic_child1">Second child</button>
                            <button topic="IwakuraEnterprises:another_topic_child1">Second child</button>
                            <button topic="IwakuraEnterprises:another_topic_child1">Second child</button>
                            <button topic="IwakuraEnterprises:another_topic_child1">Second child</button>
                            <button topic="IwakuraEnterprises:another_topic_child1">Second child</button>
                        </buttons>
                        
                        Yippe, here's link to <click data="IwakuraEnterprises:another_topic_child1">to second child</click>!
                        """)
                    .build(),
                Topic.builder()
                    .id("another_topic_child1")
                    .name("I am a second child.")
                    .description("Yeppers!")
                    .author("mayuna")
                    .markdownContent("""
                        Wooohoooo I am the second child. Here's link to <click data=\"another_topic_child0\">to second child</click>!
                        
                        <buttons>
                            <button topic="IwakuraEnterprises:another_topic_child0">Click me to get to first child!</button>
                            <button topic="IwakuraEnterprises:another_topic_child0">Go to first child</button>
                        </buttons>
                        """)
                    .build()
            );

        return Documentation.builder()
            .group("IwakuraEnterprises")
            .id("InternalDocs")
            .name("Docs")
            .type(DocumentationType.INTERNAL)
            .build()
            .addTopics(topic, anotherTopic);
    }

    public static DocumentationLoader createLoader() {
        return new DirectDocumentationLoader(List.of(create()));
    }
}
