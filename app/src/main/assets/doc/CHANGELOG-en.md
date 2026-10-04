******

### Release History

******

## v2.0.0

###### 2026/10/04

* `Hint` The application ID changes from io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools to io.github.supermonster003.autojs6.plugin.three.maple.image. Android installs this as a separate app; existing apps and data can remain, and settings are not migrated automatically
* `Added` Image Viewer and Image Tools are combined in 3-Maple Image, with viewing, editing and format conversion in one app
* `Added` A standalone home screen opens local images, starts editing or conversion, and saves the result to a chosen destination
* `Added` Language, dark mode, theme color and four launcher icon choices are available in the shared settings layout

## v1.3.1

###### 2026/09/19

* `Fixed` SDK XML v4 parsing warnings with AGP 9.1 and APK native alignment checks incorrectly triggered by JVM unit-test assembly tasks, using shared build plugins 1.8.3
* `Improved` Raise compileSdk and targetSdk to 37 (Android 17); the plugin's behavior does not depend on the new target

## v1.3.0

###### 2026/09/13

* `Added` Local release history is available from the interface, with localized text and an English fallback
* `Improved` Release packages are checked for a complete signing configuration, exact APK contents and reproducible documentation

## v1.2.0

###### 2026/09/12

* `Added` Redesigned the viewer as an immersive full-bleed screen: the image now fills the whole window under the status and navigation bars, and the top and bottom bars are translucent overlays that hide or return with a single tap
* `Added` Overlay title bar with the file name, a `3 / 12` style page counter while browsing a folder or a selection, and a top-right menu hosting `Reset zoom` and `Print / Save PDF`
* `Added` Bottom action bar with icon buttons for `Details`, `Rotate`, `Share`, and `Open with`, plus a floating pause / resume button that appears only for animated GIFs
* `Added` Image details now open in a draggable bottom sheet with the file name, MIME type, size, resolution, decoded color information, and EXIF fields; swipe down, tap the image, or press back to close it
* `Improved` Edge-to-edge system bar and display cutout handling, so the layout stays fully visible on Android 15 and later instead of being covered by system bars
* `Improved` Every icon control carries an accessibility description matching its former text label
* `Improved` Build verification rejects accidental native dependencies and produces a JSON report

## v1.1.0

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

## v1.0.1

###### 2026/08/08

* `Fixed` Enabling the plugin in the AutoJs6 plugin center failed because the service returned an empty binding (onNullBinding)
* `Improved` Leaner plugin name and description with consistent wording across all language documents

## v1.0.0

###### 2026/08/02

* `Added` First release of Image Viewer: a `View image` primary action for the AutoJs6 file manager, opening supported image files in a dedicated viewer with a single tap
* `Added` Support for 8 extensions, BMP / GIF / JFIF / JPE / JPEG / JPG / PNG / WEBP, decoded by Android and Glide, with animated GIFs playing automatically
* `Added` Viewer with fit-to-screen display, focal pinch zoom up to 5x, one-finger panning, double-tap restore, and tap-to-hide controls
* `Added` File name in the title bar, MIME type, file size, and decoded resolution in the information bar, plus `Reset zoom`, `Share`, and `Open with another app` actions (excluding the plugin itself)
* `Added` Isolated file manager and external `ACTION_VIEW` entries, both gated by temporary read-only content URI grants and item-by-item validation, with an 8 TiB per-file cap and no storage or network permissions
* `Added` Plugin service registered on explorer-action protocol version 2, requiring host build 5269 or later
* `Added` Plugin metadata, UI, instructions, and documents in Simplified Chinese, Traditional Chinese (Hong Kong / Taiwan), English, French, Spanish, Japanese, Korean, Russian, and Arabic
* `Dependency` Introduced Glide 5.0.5 as the image decoding and rendering engine
