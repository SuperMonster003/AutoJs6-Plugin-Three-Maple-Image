# Release History

## v1.2.1

###### 2026/09/19

* `Fix` SDK XML v4 parsing warnings with AGP 9.1 and APK native alignment checks incorrectly triggered by JVM unit-test assembly tasks, using shared build plugins 1.8.3
* `Improvement` Raise compileSdk and targetSdk to 37 (Android 17); the plugin's behavior does not depend on the new target

## v1.2.0

###### 2026/09/13

* `Feature` Local release history is available from the interface, with localized text and an English fallback
* `Improvement` Release packages are checked for a complete signing configuration, exact APK contents and reproducible documentation

## v1.1.0

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

## v1.0.1

###### 2026/08/08

* `Fix` Plugin could not be enabled from the AutoJs6 plugin center because the service returned an empty binding (onNullBinding)
* `Improvement` Shortened the plugin name and description and unified the wording of user documentation across languages

## v1.0.0

###### 2026/08/02

* `Feature` First release of Image Tools: two overflow menu actions, `Edit image` and `Convert image`, for a single image in the AutoJs6 file manager, with results saved as a new file next to the source while the source stays read-only
* `Feature` Editor with crop, rotation, flip, brightness, contrast, saturation, color temperature, brush, text, and up to 8 undo steps, with EXIF orientation applied automatically
* `Feature` Converter with JPEG / PNG / WebP output: adjustable quality 1-100, three resize modes (Original / Percentage / Custom), aspect-ratio locking, JPEG background color selection, and live output size estimation
* `Feature` Recognition of bmp / gif / heic / heif / jpg / jpeg / png / webp extensions and all `image/*` MIME types
* `Feature` Plugin service registered on explorer-action protocol v3: single-use read-only input paired with host-owned output transactions, with no storage or network permissions requested
* `Feature` Plugin metadata, interface, instructions, README, and changelog in 10 languages: Simplified Chinese, Traditional Chinese (Hong Kong / Taiwan), English, French, Spanish, Japanese, Korean, Russian, and Arabic
* `Improvement` Preserved editor and converter sessions across configuration changes such as screen rotation, including canvas tools, dialog drafts, conversion options, undo history, and in-flight work
* `Improvement` Guarded output writes with single-use transaction claims, action busy protection, and cancellation-safe coroutines to prevent duplicate submissions and leftover partial files
* `Improvement` Hardened output MIME validation, bitmap memory recycling, and multi-language resource consistency
* `Dependency` Appended AndroidX ExifInterface 1.4.2 for safely reading image orientation metadata
* `Dependency` Appended Robolectric 4.16.1 for unit tests of lifecycle and output transactions
