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

- Album home: launched on its own, the app opens an album grid of the images on the device (cover, name, count), with authorized folders as their own group; `Show all folder contents` in the menu opens a flat view. The media grid offers 3-6 columns (menu or pinch), sorting by name, date modified, date taken, size or random order with optional groups by day or by month (the group title stays at the top), for every album or with `This album only` for the current one, a fast scroller with a date bubble, and pull to refresh; the album list sorts by name, date modified, date taken or number of images.
- Paged viewer with a thumbnail strip: images opened from the grid swipe through the whole album, the strip highlights the current page and jumps on tap, and the grid scrolls back to the last image viewed. Rotate the picture with two fingers; it snaps to the nearest quarter turn on release (display only; turn it off in `Settings` > `Viewer` > `Two-finger rotation`).
- Multi-select and organizing: long-press an image in the grid to start selecting, tap to add or remove, and keep your finger down after the long press to drag over a range (the grid scrolls near the top or bottom edge); `Select all` and `Invert selection` are there too. Share, favorite, move, copy or delete many images at once, or rename one and view its details. Move and copy offer your albums or a new album, with recently used ones first; when a name is already taken you are asked once (keep both, skip or replace), and larger batches show progress and can be cancelled.
- Recycle bin, undo and favorites: deleted images go to a recycle bin first (the system one on Android 11+, the app's own on Android 7-10, kept for 30 days), and a bar at the bottom offers undo after deleting, moving or renaming; `Recycle bin` in the home's top-right menu restores, deletes for good or empties it. Favorited images gather in the `Favorites` album at the front of the album list; in the viewer opened from an album, the heart in the top bar favorites the current image and `Delete` in the bottom bar deletes it.
- Organizing albums and search: long-press an album in the album list to select albums, then `Pin` them (after `Favorites`), `Exclude` them (gone from this gallery only, the files stay as they are), `Hide` them (a .nomedia file in the folder hides them from other galleries too; needs `All files access`), delete whole albums (to the recycle bin, with undo) or rename one; `Excluded folders` and `Hidden folders` in `Settings` list them and bring them back. Search on the home screen looks at album names and file names, and an opened image pages through the results; inside an album the search filters by file name.
- Batch conversion: select images in an album and choose `More` > `Convert` to convert them with one set of settings (format, quality, original size, percentage or long edge, clockwise rotation, JPEG background, whether to keep safe EXIF); new images are saved in the same album as `<name>_converted` and never replace the originals, a running batch can be cancelled, and images that could not be converted are listed with the reason and can be retried. In the AutoJs6 file manager, `More` > `Convert images` in the selection bar does the same for selected images (needs an AutoJs6 version with that button) and saves the new files only when every image converts.
- Permissions only when needed: the standalone home asks for photo access once, on its first launch, and never again by itself; without it, `Choose image`, `Choose folder`, `Edit image` and `Convert image` keep working through the system picker. Android 14+ supports access to selected photos only (`Manage selected photos`). `Settings` > `Storage permission` grants access again, manages authorized folders (shown as albums even without photo access) and the optional `All files access`. Opening images from AutoJs6 or other apps never asks for a permission.
- View as album: in the folder menu of the AutoJs6 file manager, choose `View as album` to see the folder's images as a grid and open its subfolders, read-only through the host session, with no permission request and no absolute paths shown.
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
- The converter page previews the output resolution, estimated size and suffix in real time at the bottom. Safe EXIF preservation is off by default; when enabled it keeps bounded camera fields, normalizes orientation, and always removes GPS and embedded previews.
- Configuration changes preserve the canvas, dialog drafts, undo/redo history, and running tasks; each action still uses one read-only input and one host-owned single-use sibling-output transaction.

******

### Screenshots

******

These are real UI captures of the standalone app and of AutoJs6 6.8.0 on an Android 13 emulator. Every image, file name, and directory shown was generated specifically for the documentation and contains no personal data.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-albums.png?raw=true" alt="Album home" width="360" />
      <br />
      <sub>Album home</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-media-grid.png?raw=true" alt="Image grid of an album" width="360" />
      <br />
      <sub>Image grid of an album</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-thumbnail-strip.png?raw=true" alt="Paged viewer with the thumbnail strip" width="360" />
      <br />
      <sub>Paged viewer with the thumbnail strip</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-no-permission.png?raw=true" alt="Home without photo access" width="360" />
      <br />
      <sub>Home without photo access</sub>
    </td>
  </tr>
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

1. Launch 3-Maple Image on its own to browse the device's albums (photo access is requested once on the first launch); the menu at the top right still opens a chosen image for viewing, editing or conversion and saves the result to a destination you pick.
2. Open AutoJs6, enter the `Plugin center`, locate `3-Maple Image`, and enable it.
3. In the AutoJs6 file manager, locate any supported image file (such as `screenshot.png`).
4. Tap the file. The image opens in the dedicated viewer. Choose `View as album` in a folder's menu to see the images of that folder as a grid.

Inside the viewer: at 1x, swipe left or right to move to the previous or next supported image in the same folder. Pinch to zoom around your fingers (1x to 5x), drag with one finger to pan, and double-tap to switch between fit-to-screen and a 2.5x zoom centered on the touch point. The current multiplier appears while pinching and briefly after a double-tap. Tap `Rotate` for a view-only quarter turn; an active zoom is preserved, while `Reset zoom` in the top-right menu restores both the original orientation and fitted size. Animated GIFs show a floating `Pause animation` / `Resume animation` button, while static images do not. Tapping the image hides or reveals the overlay bars, and `Details` opens a bottom sheet with the file facts and EXIF fields. `Share` and `Open with` are available on the originally opened image; they are disabled on session-only sibling pages because those pages intentionally have no transferable content URI. Images opened from the standalone home or from another app also offer `Edit`, which opens the editor and lets you save the result; host entries and sibling pages do not. For images opened from an album, the heart in the top bar favorites the current image, `Delete` in the bottom bar moves it to the recycle bin (with undo), and `Open with`, `Rename`, `Move` and `Copy` are in the top-right menu.

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

**Why does the standalone app ask for photo access, and does it work if I refuse?**

The album home needs the list of images on the device, so the first standalone launch asks once (photos and videos on Android 13+, storage on older versions) and never again by itself. Without access, `Choose image`, `Choose folder`, `Edit image` and `Convert image` still work through the system picker; you can grant access later from the home button or `Settings` > `Storage permission`, and when the system stops showing the dialog the button becomes `Open app settings`. Opening images from AutoJs6 or other apps never asks for any permission.

**On Android 14+ I allowed only some photos. Why are other albums missing?**

Limited access only exposes the photos picked in the system photo picker, so only their albums appear. Tap `Manage selected photos` at the top of the home screen to change the selection, or grant full access again in `Settings` > `Storage permission`.

**What is `All files access` for? Do I need it?**

No. It is an optional advanced switch that opens the system settings page; browsing albums only needs photo access. With it on, deleting, moving and renaming images saved by other apps no longer asks the system for confirmation batch by batch (on Android 12+ you can instead turn on just `Media management` in `Settings` > `Storage permission`); hiding albums (writing .nomedia into their folders) needs it. It can be turned off at any time in the system settings.

**Where do deleted images go? Why does the system ask for confirmation when I delete or move?**

Deleted images go to the recycle bin first and can be restored from `Recycle bin` in the home's top-right menu. Android 11+ uses the system recycle bin, which the system empties after about 30 days; Android 7-10 uses the app's own recycle bin, which keeps images for 30 days, uses at most a tenth of the free space (up to 2 GiB) and is removed together with the app. With `Recycle bin` turned off in `Settings`, deleting asks first and cannot be undone. On Android 11+ changing images saved by other apps needs the system's confirmation, once per batch; with `Media management` (Android 12+) or `All files access` turned on it is no longer asked each time.

**What is the difference between `Exclude` and `Hide`?**

`Exclude` only affects this gallery: the folder's images no longer appear in the album list, `All images`, `Favorites` or search, nothing on the device changes, and `Settings` > `Excluded folders` brings them back. `Hide` puts a .nomedia file into the folder, so the system and other gallery apps stop showing its images too (nothing is deleted); on Android 11+ it needs `All files access` first, and `Settings` > `Hidden folders` shows them again. Neither changes what the AutoJs6 file manager shows.

**The file menu does not show `Edit image` and `Convert image`?**

Check the following in order: the AutoJs6 version code is at least 5276; the plugin is enabled in the `Plugin center`; and the file extension or MIME type is in the supported list. If any of the three fails, the menu actions will not appear.

**Opening fails with `Unable to read image information` or the screen flashes and closes?**

The file cannot be read right now: it may have been moved or deleted, the app that shared it may have withdrawn access, or its content does not match the extension (for example a damaged file). Open it again from its current location, or share it again from the original app.

**Where is the result saved? Does it overwrite the original?**

The original is never overwritten. From AutoJs6, the result is saved as a new file next to the source; from the standalone home screen, you choose where to save it after editing or converting.

**Converting a large image reports insufficient memory or too many pixels?**

Output size is bounded three ways: no side may exceed 16384 px, the total may not exceed 40 million pixels (40 MP), and it must fit the device memory budget. If the source exceeds the limits, switch `Resize` to `Percentage`, `Long edge`, or `Custom` to shrink the output; for memory issues, closing other apps or lowering the resolution further usually resolves it.

**Why does an edited image come out at a lower resolution?**

To keep editing smooth and stable, images above the editing pixel budget (up to about 16 MP, depending on device memory) are downsampled before entering the editor, and the saved result matches the editing canvas. If you only need to change the format or size without touching pixels, use `Convert image` instead: it decodes precisely at the output size and is not subject to this budget.

**Can it process multiple images at once, or save the result to another directory?**

Yes, conversion works in batches: select images in an album and choose `More` > `Convert`, and the new images are saved in that album; in the AutoJs6 file manager, select images and choose `More` > `Convert images` (needs an AutoJs6 version with that button), and the new images are saved next to the originals. Editing handles one image at a time. When you edit or convert one image from the standalone home screen, you choose where to save the result; calls from AutoJs6 create a new sibling file.

******

### Security

******

The plugin is built on a deny-by-default principle. All of the following measures are always on and cannot be disabled:

- Viewing uses read-only inputs. Editing and conversion produce a separate output; the source image is preserved. Standalone file access uses the Android document picker.
- Opening images from AutoJs6 (`View image`, `View as album`) or from other apps never requests a runtime permission; the standalone home asks for photo access at most once by itself, and `All files access` and `Media management` can only be turned on by you in the system settings.
- The plugin does not declare the network permission (`INTERNET`): albums, thumbnails and image details are read on the device and never leave it.
- Deleting, moving, renaming and hiding only happen after you choose them in an album or the viewer; images opened from AutoJs6 or other apps do not offer these actions.

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

The current implementation targets explorer-action protocol version 12: the primary action declares a single-file target, read-only access, and `readSiblings`. The host session exposes only direct siblings; the plugin filters supported readable non-link images, applies natural file-name ordering, and keeps a bounded window of at most 128 pages around the selected image. A second read-only selection-toolbar action declares multiple files without `readSiblings`; it accepts 1 to 128 supported images under one parent, preserves host selection order, and transfers only explicitly granted targets. Editing, conversion, file details, deletion, moving, and renaming remain host features; without the plugin, the host falls back to a read-only external `ACTION_VIEW` request. A third selection toolbar action, `convert-images`, declares multiple files with `ACCESS_CREATE_IN_PARENT`: each image becomes a new file in the source directory, is read back, and the files are published together as one output batch that rolls back completely if any image fails.

******

### Roadmap

******

Completed capabilities and upcoming plans are maintained as a checkable list in ROADMAP.md. Unchecked items express intent and do not describe current abilities.

- [Open the checkable ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### Release History

******

#### v3.1.2

###### 2026/10/14

* `Hint` Batch conversion from the AutoJs6 file manager needs an AutoJs6 version with a More button in its selection bar, which the released AutoJs6 versions do not have yet; until then, convert in batches from the albums of this plugin
* `Added` Batch conversion: select images in an album and choose More > Convert to convert them all with one set of settings (format, quality, original size / percentage / long edge, clockwise rotation, JPEG background for transparency, whether to keep safe EXIF data)
* `Added` Batch-converted images are saved next to their originals as <name>_converted and never replace them; albums that Android does not let apps add images to (such as Download) save into a folder of the same name under Pictures
* `Added` A batch shows its progress and can be cancelled at any time (the image being converted is saved completely or not at all); images that could not be converted are listed with the reason and can be retried with the same settings
* `Added` In the AutoJs6 file manager, Convert images works on selected images: the new images are saved next to the originals, and only when every image converts, so a failure leaves no new file
* `Fixed` After opening an image from an album, the next or previous image sometimes kept loading, and Print stayed unavailable

#### v3.1.1

###### 2026/10/14

* `Hint` Hiding an album puts a .nomedia file into its folder, so other gallery apps no longer show its images either (nothing is deleted); on Android 11 and later it needs All files access first. To leave an album out of this gallery only, use Exclude
* `Hint` Exclude only affects this gallery: excluded albums disappear from the album list, all images, favorites and search, the files on the device stay as they are, and Settings > Excluded folders brings them back
* `Added` Multi-select in the album list: long-press an album to pin, exclude, hide, delete whole albums (to the recycle bin, with undo) or rename an album
* `Added` Pinned albums come right after Favorites, before the other albums, with a pin mark
* `Added` Settings > Hidden folders lists hidden folders and shows them again, and can list them at the end of the album list (browse and share only)
* `Added` Sorting moves to a dialog: random order and groups by day or by month (the group title stays at the top), optionally for the current album only; the album list sorts by name, date modified, date taken or number of images and remembers it
* `Added` Search: the home screen searches album names and file names, and an opened image pages through the results; inside an album the search filters by file name
* `Fixed` Moving an image from a storage card into an album on the internal storage (or the other way round) left it on its old storage, in another album with the same name, and copies always went to the internal storage; both now land in the chosen album
* `Fixed` On Android 7-10 deleting, moving or renaming images on a storage card could leave copies behind or create stray folders on the internal storage; these versions do not let apps change a storage card, so such images now stay as they are with a message

#### v3.1.0

###### 2026/10/14

* `Hint` Deleted images go to a recycle bin first: on Android 11+ the system's (other gallery apps see it too), cleared by the system after about 30 days; on Android 7-10 the app's own, kept for 30 days and using at most a tenth of the free space (up to 2 GiB), removed together with the app. It can be switched off in Settings > Recycle bin; deleting then asks first and cannot be undone
* `Hint` On Android 11+ the system confirms each batch that deletes, moves or renames images saved by other apps; turning on Media management (Android 12+) in Settings > Storage permission removes these confirmations
* `Added` The image grid supports multi-select: long press an image to start, tap to add or remove, keep the finger down after the long press and drag to select a range (the grid scrolls near the top or bottom), select all or invert; share, favorite, move, copy or delete several images at once, rename one or show details
* `Added` A deletion can be undone from the bar at the bottom; the home screen menu gains Recycle bin, which shows the days left for each image and restores, deletes forever or empties
* `Added` Move and copy offer an album or a new album in Pictures, with recently used albums first; when the target already has images with the same names they ask once: keep both (a number is added to the new name), skip, or replace (the existing images go to the recycle bin); batches show progress and can be cancelled; moving and renaming can be undone
* `Added` Favorites: favorite images are gathered in the Favorites album at the top of the album list; on Android 11+ Sync system favorites in Settings lets other gallery apps see them
* `Added` Opened from an album, the viewer gains a heart button in the top bar, and Delete replaces Open with in the bottom bar; Open with, Rename, Move and Copy are in the top right menu
* `Added` Images in authorized folders can be deleted (for good, after a confirmation, without the recycle bin) and renamed
* `Dependency` Added Room 2.8.1 to keep the favorites and the Android 7-10 recycle bin records

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
