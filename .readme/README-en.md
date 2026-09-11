<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>File manager plugin. Edit and convert images securely</p>

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

Image Tools provides independent edit and convert actions for a single image in the file manager. It reads the source without modifying it and writes only to a host-owned output transaction.

******

### Features

******

- Edit images with crop, rotation, horizontal and vertical flip, brightness, contrast, saturation, color temperature, brush strokes, text, and undo.
- Convert to JPEG, PNG, or WebP with quality control, percentage or custom resizing, aspect-ratio locking, and JPEG background selection.
- Decode through ContentResolver and ParcelFileDescriptor without raw paths or BitmapFactory.decodeFile.
- Encode only to the exact output URI supplied by the host and report success by echoing only the output transaction ID.

******

### Supported formats

******

The plugin validates image contents through Android decoding rather than trusting an extension or declared MIME type:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### Plugin interface

******

The host discovers and executes the plugin with the following identities:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
```

Version 1 registers two protocol v3 overflow actions only on the main Explorer surface. Each action accepts one read-only source image and creates one new sibling through the host output transaction. The source is never replaced.

Host build 5269 or later is required.

******

### Security

******

The plugin requests no storage or network permission. It rejects non-v3 requests, non-main surfaces, unexpected actions, parent URIs, extra ClipData items, writable source grants, non-content URIs, malformed transaction IDs, unsupported output MIME types, and output limits above 256 MiB. The output URI is never returned to the caller.

******

### Safety limits

******

- One read-only input image and one exact host-owned output URI per action.
- Maximum declared input and encoded output size: `256 MiB`.
- Output is restricted to JPEG, PNG, or WebP.
- Image dimensions, pixel count, decode sampling, conversion memory, history storage, and encoded byte count are bounded.
- Cancellation returns `RESULT_CANCELED`; success returns only the matching transaction ID.

******

### Release history

******

# v1.0.1

###### 2026/08/08

* `Fix` Return a valid Explorer Action service binding when enabled from Plugin Center
* `Improvement` Use a shorter plugin name and description with more natural user documentation

# v1.0.0

###### 2026/08/02

* `Feature` Image Tools plugin with plugin ID `image-tools`, action IDs `edit-image` and `convert-image`, engine `explorer-action`, and variant `default`
* `Feature` Explorer Action protocol v3 overflow actions with one read-only image input and host-owned create-sibling output transactions
* `Feature` Image editor with crop, rotation, flip, brightness, contrast, saturation, color temperature, brush, text, and undo
* `Feature` JPEG, PNG, and WebP conversion with quality, resizing, aspect-ratio locking, JPEG background, and memory limits
* `Feature` ContentResolver and ParcelFileDescriptor input and output without raw paths, direct sibling writes, arbitrary result URIs, storage permission, or network permission
* `Feature` Localized plugin metadata, interface text, instructions, README files, and changelogs in 10 languages
* `Improvement` Retained editor and converter sessions across configuration changes, including canvas tools, dialog drafts, conversion options, undo history, and in-flight work
* `Improvement` Protected host output transactions with a persistent single-use claim, busy guards, cancellation-safe coroutines, and result delivery after the writer closes
* `Improvement` Hardened output MIME validation, bitmap cleanup, English resource parity, ellipsis lint handling, and release digest stream cleanup
* `Dependency` Added AndroidX ExifInterface 1.4.2 for secure image metadata parsing
* `Dependency` Added Robolectric 4.16.1 for lifecycle and persistent transaction tests

# v1.1.0

###### 2026/09/11

* `Improvement` Build verification rejects accidental native dependencies and produces a JSON report

##### For more releases

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

### Resource layout

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localizes plugin metadata and UI text. `plugin_instruction.md` provides instructions shown by the host. `.python/generate_markdown.py` generates localized README and changelog files from JSON sources.

******

### Links

******

- AutoJs6 documentation: https://docs.autojs6.com
- Android secure file sharing: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
