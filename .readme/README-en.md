<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>Secure image viewing with zoom, metadata, sharing, and external fallback for AutoJs6 Explorer</p>

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

The AutoJs6 Image Viewer plugin provides the primary image-viewing action for supported files in AutoJs6 Explorer. It opens a temporary read-only content URI in a focused viewer without changing the source file.

******

### Features

******

- Registers a protocol v2 primary Explorer action for the eight image extensions previously viewed by the host.
- Fits the image to the screen and supports focal pinch zoom, panning, double-tap reset, and tap-to-hide controls.
- Displays the file name, MIME type, size, and decoded resolution.
- Shares the image or opens it in another compatible app while excluding this plugin from its own fallback.
- Provides an independent Android `ACTION_VIEW` gateway for read-only `content` URIs with `image/*` MIME types.

******

### Supported formats

******

The Explorer primary action matches these extensions exactly:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### Plugin interface

******

AutoJs6 discovers and executes the plugin with the following identities:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
Explorer action id: view-image
MIME type: Explorer: bmp/gif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5269
supported ABIs: unrestricted (supportedAbis = emptyArray())
```

Version 1 supplies the primary image action in the main AutoJs6 Explorer. Image editing, conversion, file information, deletion, moving, and renaming remain host features. Without the plugin, the host falls back to a read-only external `ACTION_VIEW` request.

The plugin is implemented entirely on the JVM and contains no native library. It declares `supportedAbis = emptyArray()` and is released as one ABI-independent APK. AutoJs6 host build 5269 or later is required.

******

### Security

******

The plugin requests no storage or network permission. The host grants temporary read-only access to the target content URI. The Explorer gateway verifies the exact action, URI, ClipData, file name, MIME type, declared size, and parent relationship, rejects write or persistable grants, and never writes the source. The external `ACTION_VIEW` gateway is separate, accepts only read-only `content` image URIs, and forwards only the validated target URI.

******

### Safety limits

******

- Maximum input size: `8 TiB`.
- One target file per action.
- Explorer catalog: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- External `ACTION_VIEW`: read-only `content` URIs with `image/*` MIME types.
- Actual decoding support still depends on Android and Glide.
- Image editing, conversion, deletion, moving, and renaming are outside this plugin.

******

### Release history

******

# v1.0.0

###### 2026/08/02

* `Feature` Image Viewer plugin with plugin ID `image-viewer`, action ID `view-image`, engine `explorer-action`, and variant `default`
* `Feature` Explorer Action protocol v2 primary image viewing for BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, and WEBP files
* `Feature` Fit-to-screen display with focal pinch zoom, panning, double-tap reset, and tap-to-hide controls
* `Feature` File name, MIME type, size, and decoded resolution metadata, plus sharing and safe external viewer fallback
* `Feature` Separate protected Explorer and public Android `ACTION_VIEW` gateways with temporary read-only URI access and an 8 TiB input limit
* `Feature` Pure JVM implementation with no native library, unrestricted ABIs declared by `supportedAbis = emptyArray()`, one ABI-independent APK, and required AutoJs6 host build 5269
* `Feature` Localized metadata, interface text, usage instructions, README files, and changelogs in Spanish, French, Russian, Arabic, Japanese, Korean, English, Simplified Chinese, Hong Kong Traditional Chinese, and Taiwan Traditional Chinese
* `Dependency` Added Glide version 5.0.5

##### For more releases

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
