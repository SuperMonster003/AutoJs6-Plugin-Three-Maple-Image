<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>Edit images and convert image formats</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Languages

******

The current README.md supports the following languages:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- English [en] # current
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### Introduction

******

Image Tools is an image processing plugin for the AutoJs6 file manager. Once enabled, every image file in the file manager gains two actions in its overflow menu: `Edit image` opens an editor with a canvas and a toolbar for everyday touch-ups such as cropping, rotating, color adjustments, and doodling, while `Convert image` opens a dialog that saves the image as JPEG, PNG, or WebP, optionally resizing it along the way.

The result is always saved as a new file next to the source (its name carries an `edited` or `converted` suffix). The source file stays read-only the whole time and is never modified, overwritten, or deleted. The plugin requests no storage or network permissions and can only touch the single input file and single output slot authorized by the host.

******

### Highlights

******

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

<table>
  <tr>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/file-menu-action.png?raw=true" alt="File menu actions" width="300" />
      <br />
      <sub>File menu actions</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/editor.png?raw=true" alt="Image editor" width="300" />
      <br />
      <sub>Image editor</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/converter-dialog.png?raw=true" alt="JPEG conversion options" width="300" />
      <br />
      <sub>JPEG conversion options</sub>
    </td>
  </tr>
</table>

******

### Installation and Usage

******

Before starting, confirm the following requirements:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5269
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imagetools
```

It takes 4 steps from installation to your first processed image:

1. Download and install the plugin APK. The plugin has no launcher icon; after installation it is managed entirely by AutoJs6.
2. Open AutoJs6, enter the `Plugin center`, locate `Image Tools`, and enable it.
3. In the AutoJs6 file manager, locate any image file (such as `photo.jpg`) and open its overflow menu.
4. Select `Edit image` to enter the editor, or `Convert image` to open the conversion dialog.

The editor toolbar offers `Crop`, `Rotate left`, `Rotate right`, `Fine rotation`, `Flip horizontally`, `Flip vertically`, `Brightness`, `Contrast`, `Saturation`, `Color temperature`, `Brush`, and `Text`. Crop includes free, fixed, and original-ratio presets; fine rotation covers -45° to +45°. Brush types are pen, highlighter, mosaic, and eraser; text supports multiline content, outline, and shadow. The top bar provides `Undo`, `Redo`, and `Save`, while the overflow menu provides `Restore original`. `Save` opens a dialog that can follow the source format or choose JPEG / PNG / WebP, set lossy quality, and enable lossless WebP on Android 11+. The host publishes a new sibling file with an `edited` suffix; source metadata is stripped and the original is never overwritten.

The conversion dialog offers JPEG / PNG / WebP (PNG by default), quality 1-100 (default 92), lossless WebP on Android 11+, and a `Target file size` mode that selects quality automatically for JPEG or lossy WebP. `Resize` provides `Original`, `Percentage` (1-1000), `Long edge` (1920 px by default and never upscales), and `Custom` with optional aspect-ratio locking. JPEG can fill transparency with white or black; PNG automatically uses an indexed palette when the result has at most 256 colors. `Preserve safe EXIF metadata` is off by default and always removes GPS, embedded previews, and metadata that cannot be safely inspected. The dialog previews resolution, estimated size, and suffix. `Convert` asks the host to publish a new sibling with a `converted` suffix; `Cancel` or back creates no file.

******

### Supported Formats

******

The file manager shows the plugin actions for files with the following extensions (and any `image/*` MIME type):

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

The plugin identifies images by content and does not trust extensions: whether a file opens depends on whether Android can decode it. Animated files such as GIF and animated WebP are processed as their first frame only; input and output files are each capped at 256 MiB.

******

### FAQ

******

**The file menu does not show `Edit image` and `Convert image`?**

Check the following in order: the AutoJs6 version code is at least 5269; the plugin is enabled in the `Plugin center`; and the file extension or MIME type is in the supported list. If any of the three fails, the menu actions will not appear.

**Opening fails with `Unable to read image information` or the screen flashes and closes?**

Common causes: the file is corrupted or not a real image (the plugin checks content, so renaming the extension does not help); the system cannot decode the format (for example, HEIC / HEIF is generally unsupported below Android 9); the file exceeds 256 MiB; or the call did not come from the AutoJs6 file manager. For security reasons the plugin rejects invocations from any other source.

**Where is the result saved? Does it overwrite the original?**

It never overwrites. The host publishes the result as a new file next to the source with an `edited` or `converted` suffix and resolves any name clashes automatically; the source file remains read-only to the plugin throughout.

**Converting a large image reports insufficient memory or too many pixels?**

Output size is bounded three ways: no side may exceed 16384 px, the total may not exceed 40 million pixels (40 MP), and it must fit the device memory budget. If the source exceeds the limits, switch `Resize` to `Percentage`, `Long edge`, or `Custom` to shrink the output; for memory issues, closing other apps or lowering the resolution further usually resolves it.

**Why does an edited image come out at a lower resolution?**

To keep editing smooth and stable, images above the editing pixel budget (up to about 16 MP, depending on device memory) are downsampled before entering the editor, and the saved result matches the editing canvas. If you only need to change the format or size without touching pixels, use `Convert image` instead: it decodes precisely at the output size and is not subject to this budget.

**Can it process multiple images at once, or save the result to another directory?**

Not yet. Explorer-action protocol v3 supports only single-file actions with sibling output, and the plugin cannot choose the output location by itself. Multi-select actions and additional output modes depend on future protocol versions and are tracked on the roadmap.

******

### Security

******

The plugin is built on a deny-by-default principle. All of the following measures are always on and cannot be disabled:

- The source file is strictly read-only: the plugin opens the input solely through a one-time read-only content URI granted by the host, receives no filesystem paths, and requests no storage or network permissions.
- Output goes only to the exact slot pre-created by the host, and on success the plugin returns nothing but the transaction ID; it cannot choose, create, or return any other URI.
- Every output transaction is single-use: consumed transaction IDs are recorded persistently, and replayed or duplicated requests are rejected outright.
- Every invocation is fully validated: a mismatch in protocol version, source surface, action ID, grant mode, MIME type, display name, or transaction ID aborts execution, and writable grants on the source are rejected as well.
- Input and output are each capped at 256 MiB, the output resolution at 16384 px per side and 40 MP in total, and the encoded byte count is enforced while writing.
- Freshly encoded output strips source metadata by default. Optional safe EXIF preservation uses a bounded allowlist; orientation is normalized, while GPS location, embedded previews, and metadata that cannot be safely inspected are always removed.

******

### Plugin Interface (for Developers)

******

The host discovers and invokes the plugin with the following identities:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
explorer action ids: edit-image / convert-image
input MIME types: image/*
output MIME types: image/jpeg, image/png, image/webp
required host build: 5269
```

The current implementation targets explorer-action protocol v3: two overflow menu actions for a single image file on the main file manager surface, each taking one read-only input and writing one new file through a host-owned create-sibling output transaction, returning only the transaction ID on success. Multi-select and directory-level actions depend on future protocol versions and are tracked on the roadmap.

******

### Roadmap

******

Completed capabilities and upcoming plans are maintained as a checkable list in ROADMAP.md. Unchecked items express intent and do not describe current abilities.

- [Open the checkable ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/ROADMAP.md)

******

### Release History

******

#### v1.2.0

###### 2026/09/13

* `Feature` Local release history is available from the interface, with localized text and an English fallback
* `Improvement` Release packages are checked for a complete signing configuration, exact APK contents and reproducible documentation

#### v1.1.0

###### 2026/09/12

* `Feature` Symmetric undo / redo, reversible Restore original, crop aspect-ratio presets, and -45° to +45° fine rotation without changing the output dimensions
* `Feature` Expanded drawing with Pen, Highlighter, Mosaic, and Eraser tools, plus draggable multiline text with outline, shadow, and rotation
* `Feature` Editor save choices for source format / JPEG / PNG / WebP, adjustable lossy quality, and lossless WebP on Android 11+
* `Feature` Converter lossless WebP, indexed PNG for images with up to 256 colors, target file size for JPEG / lossy WebP, and a long-edge resize mode that never upscales
* `Feature` Optional safe EXIF preservation while always removing GPS, embedded previews, and opaque metadata and normalizing orientation
* `Fix` Valid images being rejected when BitmapFactory bounds-only inspection correctly returned no bitmap
* `Fix` Unreadable editor tool labels in dark mode
* `Improvement` Expanded state restoration, memory / output-limit enforcement, and regression coverage to 23 suites / 99 tests
* `Improvement` Updated all 10-language README files and host instructions from shared sources and added three privacy-safe real-device screenshots
* `Improvement` Build verification rejects accidental native dependencies and produces a JSON report

#### v1.0.1

###### 2026/08/08

* `Fix` Plugin could not be enabled from the AutoJs6 plugin center because the service returned an empty binding (onNullBinding)
* `Improvement` Shortened the plugin name and description and unified the wording of user documentation across languages

##### Full history

* [CHANGELOG-en.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-en.md)

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
.readme/template_plugin_instruction.md
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localizes the plugin metadata and the editor and converter UI. README, CHANGELOG, and host-side `plugin_instruction.md` files are all generated from JSON sources and Markdown templates by `.python/generate_markdown.py`: to change the docs, edit the sources under `.readme` and `.changelog` and rerun the script instead of editing generated Markdown files.

******

### Links

******

- AutoJs6 documentation: https://docs.autojs6.com
- Android secure file sharing: https://developer.android.com/training/secure-file-sharing


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
