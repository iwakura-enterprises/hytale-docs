---
id: first-steps
name: First steps
description: The absolute basics of creating a documentation
author: mayuna
---

# 1. Installing Docs

> If you're reading this in Hytale, you have successfully installed Docs! **Please, move onto the next chapter.**

# 2. Understanding configuration

After you've installed Docs, there will be new directory in **mods** folder: **IwakuraEnterprises_Docs**.

In this folder you'll be able to find the configuration file, folder for storing documentations and
folder for various UI-related errors.

<buttons>
  <button topic="Docs:configuration">Configuration</button>
</buttons>

For now, let's keep all settings unchanged, in the default configuration.

# 3. Creating first documentation

You may define all server documentations at **mods/IwakuraEnterprises_Docs/documentation/index.json**. Within this file
you'll be able to add, remove, disable or enable your server-specific documentation. Let's create your first documentation:

```json
{
  "documentations": [
    {
      "group": "MyGroup",
      "id": "MyDocumentation",
      "name": "My first documentation",
      "enabled": true,
      "sortIndex": 0
    }
  ]
}
```

Let's also create directory **MyDocumentation** next to the **index.json** file you have just edited.

This will:
- Define new documentation in group **MyGroup**, with ID **MyDocumentation**, with user-friendly name **My first documentation**.
  - The ID is used as the root directory for the documentation. In this example, the directory
    **mods/IwakuraEnterprises_Docs/documentation/MyDocumentation** will contain all the markdown files for the documentation.
  - The group and ID are used when referencing specific documentation's topic. The user-friendly name is shown to the user
    in the documentation list on the left side of the interface.
- Mark the documentation as enabled. Disabled documentations are now shown to the user.
- Put the documentation first in the documentation list. **Sort indexes are sorted from the lowest to highest**. This
allows you to control the order of documentations.

# 4. Writing the documentation

Documentation's content is stored in topics. Topic is a page with name, description, author and content. It can also
contain sub-topics. In matter of a fact, you're currently reading this in Topic called **First steps**.

You may create topics by creating markdown files. Each markdown file is a topic. These terms may be used interchangeably
in this topic: *topic is markdown file ~ markdown file is topic*.

Let's create your first topic called **home.md** in folder **IwakuraEnterprises_Docs/documentation/MyDocumentation**:

```md
---
name: My first topic
description: Look ma, no hands!
author: Me, Myself & I
---

This is my first topic! This is awesome :)
```

After saving the topic, you can run **/docs-reload** command to reload the Docs' configuration and documentations. After
reopening the interface with **/docs**, you'll be able to see your first documentation on the left side! After you verify
you can see it, continue to the next chapter.

> Topic's ID is defined by the name of the markdown file. You may also define your own ID by adding **id** field.

# 5. Sub-topics (and the growing complexity)

First-level topics are nice and all, but you may also create sub-topics. This is a great way to organize topics in categories
so players can easily find what they are looking for. You can create sub-topics by defining them in topics. Let's create our
first sub-topic called **My second topic**.

> ## Creating second topic
> 
> Next to **home.md**, let's create another file called **second-topic.md**:
> 
> ```md
> ---
> name: My second topic
> description: Specifics.. specifics, specifics!
> author: Still just you.
> ---
> 
> This is my second topic! I'm doing great.
> ```

> ## Editing the first topic
> 
> To reference the second topic, let's add **sub-topics** field:
> 
> ```md
> ---
> name: My first topic
> description: Look ma, no hands!
> author: Me, Myself & I
> sub-topics:
>   - second-topic
> ---
> 
> This is my first topic! This is awesome :)
> ```

You can try and use **/docs-reload** to see your changes. You should be able to see the new topic under the first one.

# 6. Sub-topics in subfolders

Having all sub-topics on the same level is quite messy. Let's change that! For our next third topic, we will create
folder called **sub-topics** next to our two markdown files (very original, I know). Inside that folder, we will create
new markdown file called **third-topic.md**

> ## Current structure
> 
> Our documentation structure should now look something like this:
> 
> ```
> IwakuraEnterprises_Docs/documentation
>   MyDocumentation/
>     home.md
>     second-topic.md
>     sub-topics/
>       third-topic.md
> ```

> ## Contents of third topic
> 
> The third topic is in **MyDocumentation/sub-topics/third-topic.md**
> 
> ```md
> ---
> name: My third topic
> description: Who lives in a subfolder under the first topic?
> author: Despite everything, it's still you.
> ---
> 
> This is my third topic. How many will there be?!
> ```

> ## Editing the first topic
> 
> Instead of specifying the topic's ID (<gray>usually the name of the file</gray>), let's specify the subfolder itself.
> **This will include all sub-topics in that folder**. However, it will **not** include *any other* subsequent subfolders.
> 
> ```md
> ---
> name: My first topic
> description: Look ma, no hands!
> author: Me, Myself & I
> sub-topics:
>   - second-topic
>   - sub-topics
> ---
> 
> This is my first topic! This is awesome :)
> ```

After you reload the Docs, there should be now three topics. **Good job!**

# 7. What's next?

You have successfully created your first documentation. You can now apply skills you've learned to create awesome
documentations for your server! You may also see other topics that will teach you more advanced skills, such as
color formatting or adding topic-linking buttons.




