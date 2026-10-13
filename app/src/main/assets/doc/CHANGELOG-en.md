******

### Release History

******

## v3.1.0

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

## v3.0.0

###### 2026/10/13

* `Hint` Launched on its own, the app asks once for photo access (photos and videos on Android 13+, storage on older versions) to show your albums; if you refuse it never asks again by itself, viewing, editing and converting keep working through the system picker, and you can grant access later from the home screen or Settings > Storage permission. Opening images from AutoJs6 or other apps never asks for any permission
* `Added` The home screen becomes an album view: images on the device grouped by folder (cover, name, count), or a flat view of all folder contents; the image grid offers 3-6 columns (menu or pinch), sorting by name, date modified, date taken or size remembered per album, a fast scroller with a date hint and pull to refresh
* `Added` The viewer pages through the whole album with left and right swipes, a thumbnail strip jumps to a page on tap, and the grid returns to the last image viewed
* `Added` Rotate an image with two fingers; it snaps to the nearest quarter turn on release, changes the display only, can be undone and can be turned off in Settings > Viewer
* `Added` A new Storage permission page in Settings shows the current access, grants it again, manages selected photos on Android 14+, adds or removes authorized folders (shown as albums even without photo access) and offers the optional All files access
* `Added` The folder menu of the AutoJs6 file manager gains View as album, which shows the folder's images as a grid and opens its subfolders
* `Dependency` Added Navigation Compose 2.10.2 for navigation between the home screen and album pages

## v2.1.0

###### 2026/10/13

* `Hint` The APK now carries libandroidx.graphics.path.so, a native library bundled with Jetpack Compose (about 10 KB each for arm64-v8a, armeabi-v7a, x86_64 and x86); the plugin itself still contains no native code and remains one universal APK
* `Hint` The first few launches after installation are a little slower on older devices until the system finishes its background optimization
* `Added` The whole interface moves to Material 3 (Jetpack Compose): home, settings, about, release history, viewer, editor and converter are rebuilt, follow the theme color and dark mode, and support large fonts and right-to-left layouts
* `Added` The editor toolbar becomes grouped tool strips with icons and labels (transform, adjust, draw, text); the parameter panel follows the active tool, and the text, save and palette dialogs are rebuilt
* `Added` The converter turns from a dialog into a full page form that shows only the inputs of the current resize mode, with the output resolution, estimated size and suffix pinned at the bottom
* `Added` The viewer bottom bar now offers Details, Rotate, Edit, Share and Open with; images opened from the standalone home or from another app can go straight to the editor and save the result
* `Fixed` The in-app release history page was blank (the ## version headings of the generated file were not recognized)
* `Fixed` After switching the app language to Arabic, pages that were already open kept a left-to-right layout
* `Fixed` In light mode the status bar icons of the viewer and editor followed the page theme and turned dark, so they were invisible over the dark canvas
* `Improved` Plugin Center icons use the sizes, positions, light and dark artwork, and circular backgrounds adjusted in Icon Studio, retaining reproducible sources and parameters
* `Improved` Android App info icons share Icon Studio artwork and light/dark backgrounds while preserving transparent Plugin Center artwork and existing launcher choices
* `Improved` Icons are unified as Material 24 dp vectors; bitmap icons and unused color, style and string resources are removed
* `Improved` A built-in startup baseline profile lowers the home cold start cost of the interface migration once the system has run its background optimization
* `Dependency` Added Jetpack Compose BOM 2026.09.00 for the user interface migration
* `Dependency` Added the Glide Compose integration 1.0.0-beta10 for image loading in Compose screens
* `Dependency` Upgraded Glide 5.0.5 -> 5.0.9
* `Dependency` Upgraded the AutoJs6 Gradle Platform Versions and Native Alignment plugins 1.9.0 -> 1.9.1

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
