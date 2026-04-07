---
name: Images
description: Conveying information via images
author: mayuna
---

# Images

Image syntax follows the standard Markdown image syntax with additional features. Images are always rendered in the
middle of the topic.

Runtime images can be disabled in Voile's configuration (see the **Configuration** topic).

```
![Alternative text {resize-hint}](image-source)
```

## Locations

An image can be sourced from multiple locations.

| Type                     | Location                                                                  |
|--------------------------|---------------------------------------------------------------------------|
| Mod resource asset image | Images located in the `Common/` resource.                                 |
| File system image        | Images located in Voile's data folder (`mods/IwakuraEnterprises_Voile/`). |
| Online image             | Any online image that can be directly downloaded.                         |

Images loaded from the file system or from the Internet are considered runtime images.

!! Online images should be used only by server documentations when needed. They worsen the UX due to downloading them
!! from the Internet.
!! Online images are then cached for one day (configurable).
!! 
!! Online images are downloaded into `mods/IwakuraEnterprises_Voile/runtime_images/`

!! When loading runtime images, Voile may send the `RequestCommonAssetsRebuild` packet. This packet asks the client to
!! rebuild currently loaded common assets and may freeze the client for a short while, depending on the amount of the
!! assets loaded by Voile and other mods.
!! 
!! This rebuild happens only once per runtime image that needs to be loaded into the player's game.

<buttons>
    <button topic="configuration">Configuration</button>
</buttons>

If image loading fails, the image is not found, or runtime images are disabled, these images will be shown:

![Voile Image Not Found](UI/Custom/Docs/Images/image_not_found.png)
![Voile Images are disabled](UI/Custom/Docs/Images/images_disabled.png)

## Mod resource asset image

Topics from mod integrations should prefer including required images within their mod's `Common/` assets. Then they can
be easily sourced by specifying the fully qualified or relative path starting from the `Common/` folder.

```
![Voile Banner](UI/Custom/Docs/Images/voile_banner.png)
```

![Voile Banner](UI/Custom/Docs/Images/voile_banner.png)

!! Note that the image source does not specify the `Common/` folder.

! You can also specify images relative to the topic file.
! ```
! ![Some example image](my-example-image.png)
! ```
! with the following file structure
! ```
! Common/
!     Docs/{DocumentationGroup}_{DocumentationId}/
!         my-topic.md
!         my-example-image.png
! ```

## Resizing (resize hint)

Images shown in the UI can be resized by specifying `{resize-hint}` within the alternative text block. This hint is
optional. If not specified, Voile will render the image with its original resolution.

```
![Alternative text {300x200}](image-source)
```

| Format      | Result                                                               |
|-------------|----------------------------------------------------------------------|
| `{300x200}` | Resizes image to 300 (width) x 200 (height).                         |
| `{300x0}`   | Resizes image to 300 (width) while respecting image's aspect ratio.  |
| `{0x200}`   | Resizes image to 200 (height) while respecting image's aspect ratio. |

!! Maximum width of an image is 900. If it's too wide, it will be resized while respecting image's aspect ratio.

## Multiple images

It is possible to specify multiple images next to each other.

```
![Image 1](image-source) ![Image 2](image-source)

![Image 1](image-source)
![Image 2](image-source)
```

![Voile Banner {300x0}](UI/Custom/Docs/Images/voile_banner.png) ![Voile Banner {300x0}](UI/Custom/Docs/Images/voile_banner.png)

## Supported image file types

Numerous image file formats are supported. If the image is not a PNG, it will get automatically converted.

Supported formats:
- `.png`
- `.jpg`, `.jpeg`
- `.webp`
- `.tiff`, `.tif`
- `.bmp`
- `.gif` (not animated, only the first frame)
- `.wbmp`
