<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>Image viewing, editing and format conversion</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Languages

******

The current README.md supports the following languages:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-TW.md)
- English [en] # current
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ar.md)

******

### Get started

A standalone home screen opens local images, starts editing or conversion, and saves the result to a chosen destination. Language, dark mode, theme color and four launcher icon choices are available in the shared settings layout.

The application ID changes from `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools` to `io.github.supermonster003.autojs6.plugin.three.maple.image`. Android installs this as a separate app; existing apps and data can remain, and settings are not migrated automatically.

******

### Introduction

******

Image Viewer and Image Tools are combined in 3-Maple Image, with viewing, editing and format conversion in one app.

Viewing uses read-only inputs. Editing and conversion produce a separate output; the source image is preserved. Standalone file access uses the Android document picker.

******

### Highlights

******

- Open an exact selection: in AutoJs6 selection mode, choose up to 128 supported images from one folder and tap `View image`; the viewer keeps the host selection order and swipes only within that chosen group.
- Tap and browse: tapping a supported image file opens the viewer directly; at 1x, swipe left or right to move through supported images in the same folder in natural file-name order.
- Natural gestures: focal pinch zoom centered on your fingers (up to 5x), one-finger panning, double-tap to toggle a 2.5x zoom around the touch point, 90° clockwise view rotation, and tap to hide or reveal the controls for distraction-free viewing. A compact indicator shows the live multiplier while pinching and briefly after a double-tap.
- Detail-preserving large-image zoom: when oversized JPEG, PNG, or static HEIC/HEIF images exceed the device texture limit or bounded decode budget, the viewer shows a sampled preview and decodes only high-resolution tiles for the visible region while zoomed. Tile memory stays capped and is released on page changes or memory pressure.
- Key facts at a glance: the overlay title bar shows the file name plus a `3 / 12` style page counter when several images are open, while the bottom bar reports the MIME type, file size, and decoded resolution (width x height). On Android 8.0 or later, when the decoder exposes them, it also reports decoded pixel depth (bpp) and output color space.
- Details bottom sheet: tap `Details` to open a draggable sheet with the file name, MIME type, size, resolution, and the capture time, device, exposure, and orientation when EXIF is available. If GPS metadata exists, the viewer reports its presence but keeps the coordinates hidden. Photos are automatically rotated or mirrored to their EXIF orientation before any manual view rotation is applied.
- Print or save as PDF: `Print / Save PDF` in the top-right menu sends the full current image to Android's system print sheet with EXIF correction and manual view rotation preserved. Animated GIFs use the frame visible when tapped; zoom and panning do not crop the output, and the plugin creates no temporary image or PDF file.
- Common formats out of the box: the JPEG family (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF, and AVIF, 11 extensions in total, with animated GIFs looping automatically and a dedicated pause/resume control.
- Share and hand off: bring up the system share sheet with one tap, or use `Open with` for editing or annotating, with the plugin itself excluded from the app list to avoid loops.
- Works as a system image viewer: a separate Android `ACTION_VIEW` entry safely serves read-only image viewing requests from other apps.
- The editor offers crop presets; 90-degree and fine -45° to +45° rotation; horizontal and vertical flip; brightness, contrast, saturation, and color temperature; brushes and styled text, with live preview while adjusting.
- Brush types include pen, highlighter, privacy-preserving mosaic, and eraser with remembered color and width; text supports multiline content, size, color, outline, shadow, and drag placement.
- Undo and redo keep up to 8 history snapshots within a 192 MiB budget; `Restore original` is reversible, and leaving with unsaved changes requires confirmation.
- The editor save dialog can follow the source format or choose JPEG, PNG, or WebP, adjust lossy quality, and enable lossless WebP on Android 11+; the host publishes a new sibling instead of overwriting the source.
- The converter supports JPEG / PNG / WebP, quality 1-100 (default 92), target file size for JPEG and lossy WebP, lossless WebP on Android 11+, and automatic indexed PNG optimization when the image has at most 256 colors.
- Four sizing modes cover `Original`, `Percentage` (1-1000), `Custom` with aspect-ratio locking, and `Long edge` (1920 px by default and never upscales); JPEG can fill transparency with white or black.
- The dialog previews resolution and estimated size in real time. Safe EXIF preservation is off by default; when enabled it keeps bounded camera fields, normalizes orientation, and always removes GPS and embedded previews.
- Configuration changes preserve the canvas, dialog drafts, undo/redo history, and running tasks; each action still uses one read-only input and one host-owned single-use sibling-output transaction.

******

### Screenshots

******

These are real UI captures from AutoJs6 6.8.0 on an Android 13 emulator. Every image, file name, and directory shown was generated specifically for the documentation and contains no personal data.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="Single-file viewer action in the file manager" width="360" />
      <br />
      <sub>Single-file viewer action in the file manager</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="Exact group of two selected images" width="360" />
      <br />
      <sub>Exact group of two selected images</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="Main viewer and live metadata" width="360" />
      <br />
      <sub>Main viewer and live metadata</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="Immersive 2.5x zoom" width="360" />
      <br />
      <sub>Immersive 2.5x zoom</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="System share sheet" width="360" />
      <br />
      <sub>System share sheet</sub>
    </td>
  </tr>
</table>

******

### Installation and Usage

******

Before starting, confirm the following requirements:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

It takes 4 steps from installation to your first image:

1. A standalone home screen opens local images, starts editing or conversion, and saves the result to a chosen destination.
2. Open AutoJs6, enter the `Plugin center`, locate `3-Maple Image`, and enable it.
3. In the AutoJs6 file manager, locate any supported image file (such as `screenshot.png`).
4. Tap the file. The image opens in the dedicated viewer.

Inside the viewer: at 1x, swipe left or right to move to the previous or next supported image in the same folder. Pinch to zoom around your fingers (1x to 5x), drag with one finger to pan, and double-tap to switch between fit-to-screen and a 2.5x zoom centered on the touch point. The current multiplier appears while pinching and briefly after a double-tap. Tap `Rotate` for a view-only quarter turn; an active zoom is preserved, while `Reset zoom` in the top-right menu restores both the original orientation and fitted size. Animated GIFs show a floating `Pause animation` / `Resume animation` button, while static images do not. Tapping the image hides or reveals the overlay bars, and `Details` opens a bottom sheet with the file facts and EXIF fields. `Share` and `Open with` are available on the originally opened image; they are disabled on session-only sibling pages because those pages intentionally have no transferable content URI.

******

### Supported Formats

******

The viewing action in the file manager matches exactly the following extensions:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

JFIF and JPE are alias extensions of the JPEG family. HEIC and HEIF require Android 9 or later; AVIF requires Android 12 or later. The viewer also runs a tiny local decoder-capability probe and shows a specific message when the platform decoder is unavailable. The separate `ACTION_VIEW` entry accepts requests by `image/*` MIME type and is not limited to the list above. A single file may be at most 8 TiB; actual decoding support depends on the Android platform and Glide.

******

### FAQ

******

**The file menu does not show `Edit image` and `Convert image`?**

Check the following in order: the AutoJs6 version code is at least 5276; the plugin is enabled in the `Plugin center`; and the file extension or MIME type is in the supported list. If any of the three fails, the menu actions will not appear.

**Opening fails with `Unable to read image information` or the screen flashes and closes?**

Viewing uses read-only inputs. Editing and conversion produce a separate output; the source image is preserved. Standalone file access uses the Android document picker.

**Where is the result saved? Does it overwrite the original?**

Viewing supports a selected group of images. Editing and conversion handle one image at a time. From the standalone home screen, you choose where to save the result; calls from AutoJs6 create a new sibling file.

**Converting a large image reports insufficient memory or too many pixels?**

Output size is bounded three ways: no side may exceed 16384 px, the total may not exceed 40 million pixels (40 MP), and it must fit the device memory budget. If the source exceeds the limits, switch `Resize` to `Percentage`, `Long edge`, or `Custom` to shrink the output; for memory issues, closing other apps or lowering the resolution further usually resolves it.

**Why does an edited image come out at a lower resolution?**

To keep editing smooth and stable, images above the editing pixel budget (up to about 16 MP, depending on device memory) are downsampled before entering the editor, and the saved result matches the editing canvas. If you only need to change the format or size without touching pixels, use `Convert image` instead: it decodes precisely at the output size and is not subject to this budget.

**Can it process multiple images at once, or save the result to another directory?**

Viewing supports a selected group of images. Editing and conversion handle one image at a time. From the standalone home screen, you choose where to save the result; calls from AutoJs6 create a new sibling file.

******

### Security

******

The plugin is built on a deny-by-default principle. All of the following measures are always on and cannot be disabled:

- Viewing uses read-only inputs. Editing and conversion produce a separate output; the source image is preserved. Standalone file access uses the Android document picker.

******

### Plugin Interface (for Developers)

******

The host discovers and invokes the plugin with the following identities:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: three-maple-image
engine: explorer-action
variant: default
explorer action id: view-image
protocol version: 12
MIME type: Explorer: avif/bmp/gif/heic/heif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5276
```

The current implementation targets explorer-action protocol version 12: the primary action declares a single-file target, read-only access, and `readSiblings`. The host session exposes only direct siblings; the plugin filters supported readable non-link images, applies natural file-name ordering, and keeps a bounded window of at most 128 pages around the selected image. A second read-only selection-toolbar action declares multiple files without `readSiblings`; it accepts 1 to 128 supported images under one parent, preserves host selection order, and transfers only explicitly granted targets. Editing, conversion, file details, deletion, moving, and renaming remain host features; without the plugin, the host falls back to a read-only external `ACTION_VIEW` request.

******

### Roadmap

******

Completed capabilities and upcoming plans are maintained as a checkable list in ROADMAP.md. Unchecked items express intent and do not describe current abilities.

- [Open the checkable ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### Release History

******

#### v2.0.1

###### 2026/10/04

* `Improved` Plugin Center icons use the sizes, positions, light and dark artwork, and circular backgrounds adjusted in Icon Studio, retaining reproducible sources and parameters

#### v2.0.0

###### 2026/10/04

* `Hint` The application ID changes from io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools to io.github.supermonster003.autojs6.plugin.three.maple.image. Android installs this as a separate app; existing apps and data can remain, and settings are not migrated automatically
* `Added` Image Viewer and Image Tools are combined in 3-Maple Image, with viewing, editing and format conversion in one app
* `Added` A standalone home screen opens local images, starts editing or conversion, and saves the result to a chosen destination
* `Added` Language, dark mode, theme color and four launcher icon choices are available in the shared settings layout

#### v1.3.1

###### 2026/09/19

* `Fixed` SDK XML v4 parsing warnings with AGP 9.1 and APK native alignment checks incorrectly triggered by JVM unit-test assembly tasks, using shared build plugins 1.8.3
* `Improved` Raise compileSdk and targetSdk to 37 (Android 17); the plugin's behavior does not depend on the new target

##### Full history

* [CHANGELOG-en.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-en.md)

******

### Build

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Release build:

```powershell
.\gradlew.bat :app:assembleRelease
```

Build parameters come from `version.properties`. The current minimum SDK is 24 and the target SDK is 36.

******

### Resource Layout

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
docs/images/screenshots/*.png
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localizes plugin metadata and the viewer UI, while `plugin_instruction.md` provides the usage instructions shown by the host. All README and CHANGELOG files are generated from JSON sources by `.python/generate_markdown.py`: to change the docs, edit the `lang_*.json` files under `.readme` and `.changelog` and rerun the script instead of editing the generated Markdown files.

******

### Links

******

- AutoJs6 documentation: https://docs.autojs6.com
- Android secure file sharing: https://developer.android.com/training/secure-file-sharing
- Glide (image loading and rendering engine): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### Sources and acknowledgments

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
