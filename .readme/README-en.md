<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>File manager plugin. Secure read-only image browsing with same-folder paging, zoom, metadata, printing, and sharing</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Languages

******

The current README.md supports the following languages:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- English [en] # current
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### Introduction

******

Image Viewer is an image browsing plugin for the AutoJs6 file manager. Once enabled, tapping a common image file such as JPG, PNG, GIF, or WEBP opens it in a dedicated viewer: the image fits the screen automatically, pinch to inspect details, and at 1x you can swipe left or right through supported images in the same folder. The title and bottom metadata bar update for every page, with sharing or handoff to another app available for the originally opened image.

The plugin does one thing and does it safely: read-only viewing. The originally tapped file arrives through a temporary read-only content URI, while direct siblings can only be enumerated and opened through a short-lived, host-owned readSiblings session. The plugin requests no storage or network permission, never modifies or moves a source file, and closes the host session with the viewer.

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
- A read-only security sandbox: no storage or network permissions, access to exactly one file through a temporary read-only grant, and the source file is never written.

******

### Screenshots

******

These are real UI captures from AutoJs6 6.8.0 on an Android 13 emulator. Every image, file name, and directory shown was generated specifically for the documentation and contains no personal data.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="Single-file viewer action in the file manager" width="360" />
      <br />
      <sub>Single-file viewer action in the file manager</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="Exact group of two selected images" width="360" />
      <br />
      <sub>Exact group of two selected images</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="Main viewer and live metadata" width="360" />
      <br />
      <sub>Main viewer and live metadata</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="Immersive 2.5x zoom" width="360" />
      <br />
      <sub>Immersive 2.5x zoom</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="System share sheet" width="360" />
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
plugin package: io.github.supermonster003.autojs6.plugin.imageviewer
```

It takes 4 steps from installation to your first image:

1. Download and install the plugin APK. The plugin has no launcher icon; after installation it is managed entirely by AutoJs6.
2. Open AutoJs6, enter the `Plugin center`, locate `Image Viewer`, and enable it.
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

**Tapping an image file does not open this viewer?**

Check the following in order: the AutoJs6 version code is at least 5276 (version 6.8.0 or later qualifies); the plugin is enabled in the `Plugin center`; and the file extension is in the supported list. If any of the three fails, the tap will not be served by this plugin.

**The viewer shows `The image could not be displayed`?**

Common causes: the image data is corrupted or its encoding is not supported by the current Android platform; the file was moved, renamed, or deleted at the moment of opening; or the declared file size does not match the actual size (the security checks reject such requests).

**Can I edit, crop, or permanently rotate images?**

No. This plugin focuses on read-only viewing. `Rotate` changes only the current display and never the source file. For editing, tap `Open with` to hand the image to an editor app; file operations such as deleting, moving, and renaming remain available in the AutoJs6 file manager.

**Do animated GIFs play?**

Yes. Animated GIFs are decoded by Glide and loop automatically. The viewer shows a pause/resume control only for an actual animated drawable, and that control affects display playback without modifying the source file.

**What happens when the plugin is not installed?**

The host falls back to a read-only external viewing request served by the image apps already on the device. Once this plugin is installed and enabled, taps open in the built-in viewer instead.

**Why does the plugin also register a system-level image viewing entry?**

That is the separate `ACTION_VIEW` entry, which accepts only read-only `content` URI requests of `image/*` types so that other apps can use this viewer. It is isolated from the file manager entry, goes through the same strict validation, and likewise never writes anything.

******

### Security

******

The plugin is built on a deny-by-default principle. All of the following measures are always on and cannot be disabled:

- Bounded explicit groups: multi-selection accepts 1 to 128 supported direct children of one parent. Target IDs, URIs, names, ordered ClipData, MIME types, and sizes must be unique where required and mutually consistent; every selected image is content-checked before the viewer opens.
- Zero sensitive permissions: no storage, network, or other runtime permissions, with cleartext traffic disabled; the file manager entry and the wake entry are protected by the host plugin permission and callable by the host only.
- Temporary scoped read-only access: the selected file uses a temporary content URI; direct siblings are enumerated and opened only through the v12 HOST_SESSION with an opaque target ID and validated direct relative names. The plugin receives no filesystem paths and rejects write or persistable grants.
- Entry-point validation: the action identity, protocol version, request UUID, host build, calling surface, target Bundle, URI structure, ClipData, file name, MIME type, declared size, direct-parent relationship, and host-session Binder descriptor are verified item by item; any mismatch means the request is refused.
- Content double-check: before opening, the image decode bounds are probed and the declared size is compared with the actual size, rejecting on mismatch; a single file is capped at 8 TiB.
- Isolated dual entries: the file manager entry and the external `ACTION_VIEW` entry are independent of each other; the latter accepts only read-only `content` URI image requests and passes the same content verification.
- The viewer is not exported: the rendering screen can only be started from inside the plugin, sharing and external opening forward only temporary read-only grants, and the source file is never written.

******

### Plugin Interface (for Developers)

******

The host discovers and invokes the plugin with the following identities:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
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

- [Open the checkable ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### Release History

******

#### v1.2.0

###### 2026/09/12

* `Added` Redesigned the viewer as an immersive full-bleed screen: the image now fills the whole window under the status and navigation bars, and the top and bottom bars are translucent overlays that hide or return with a single tap
* `Added` Overlay title bar with the file name, a `3 / 12` style page counter while browsing a folder or a selection, and a top-right menu hosting `Reset zoom` and `Print / Save PDF`
* `Added` Bottom action bar with icon buttons for `Details`, `Rotate`, `Share`, and `Open with`, plus a floating pause / resume button that appears only for animated GIFs
* `Added` Image details now open in a draggable bottom sheet with the file name, MIME type, size, resolution, decoded color information, and EXIF fields; swipe down, tap the image, or press back to close it
* `Improved` Edge-to-edge system bar and display cutout handling, so the layout stays fully visible on Android 15 and later instead of being covered by system bars
* `Improved` Every icon control carries an accessibility description matching its former text label
* `Improved` Build verification rejects accidental native dependencies and produces a JSON report

#### v1.1.0

###### 2026/08/31

* `Hint` AutoJs6 host build 5276 or later is required for explorer-action v12 same-folder browsing and explicit multi-selection
* `Added` Browse supported images in the same folder by swiping in natural filename order, or open an explicit selection of up to 128 images while preserving the host selection order
* `Added` Enhanced viewer gestures with focal pinch zoom, 2.5x double-tap zoom, 90-degree view rotation, a transient zoom indicator and pause/resume controls for animated GIFs
* `Added` On-demand EXIF details with automatic correction of all 8 orientation variants and hidden GPS coordinates, plus decoded pixel depth and color space when available and printing or saving the full image as PDF
* `Added` HEIC / HEIF support on Android 9 or later and AVIF support on Android 12 or later, backed by real decoder capability probes and clear unsupported-format messages
* `Added` Tiled viewing for huge JPEG / PNG and static HEIC / HEIF images, using a bounded preview and high-resolution tiles for the visible region within a capped memory budget
* `Improved` Hardened every entry path with strict explorer-action v12 request, target, content, size and direct-child validation while retaining temporary read-only access and no storage or network permissions
* `Improved` Expanded the localized interface and user documentation in 10 languages, including a five-image gallery captured from the real Android UI
* `Dependency` Added AndroidX ExifInterface 1.4.2 for read-only EXIF metadata parsing

#### v1.0.1

###### 2026/08/08

* `Fixed` Enabling the plugin in the AutoJs6 plugin center failed because the service returned an empty binding (onNullBinding)
* `Improved` Leaner plugin name and description with consistent wording across all language documents

##### Full history

* [CHANGELOG-en.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-en.md)

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
