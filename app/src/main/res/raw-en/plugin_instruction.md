# Image Tools

Image Tools is an image processing plugin for the AutoJs6 file manager. Once enabled, every image file in the file manager gains two actions in its overflow menu: `Edit image` opens an editor with a canvas and a toolbar for everyday touch-ups such as cropping, rotating, color adjustments, and doodling, while `Convert image` opens a dialog that saves the image as JPEG, PNG, or WebP, optionally resizing it along the way.

The result is always saved as a new file next to the source (its name carries an `edited` or `converted` suffix). The source file stays read-only the whole time and is never modified, overwritten, or deleted. The plugin requests no storage or network permissions and can only touch the single input file and single output slot authorized by the host.

## Installation and Usage

Before starting, confirm the following requirements:

```text
minimum host build: 5269
minimum android: 7.0 (API 24)
```

1. Download and install the plugin APK. The plugin has no launcher icon; after installation it is managed entirely by AutoJs6.
2. Open AutoJs6, enter the `Plugin center`, locate `Image Tools`, and enable it.
3. In the AutoJs6 file manager, locate any image file (such as `photo.jpg`) and open its overflow menu.
4. Select `Edit image` to enter the editor, or `Convert image` to open the conversion dialog.

The editor toolbar offers `Crop`, `Rotate left`, `Rotate right`, `Fine rotation`, `Flip horizontally`, `Flip vertically`, `Brightness`, `Contrast`, `Saturation`, `Color temperature`, `Brush`, and `Text`. Crop includes free, fixed, and original-ratio presets; fine rotation covers -45° to +45°. Brush types are pen, highlighter, mosaic, and eraser; text supports multiline content, outline, and shadow. The top bar provides `Undo`, `Redo`, and `Save`, while the overflow menu provides `Restore original`. `Save` opens a dialog that can follow the source format or choose JPEG / PNG / WebP, set lossy quality, and enable lossless WebP on Android 11+. The host publishes a new sibling file with an `edited` suffix; source metadata is stripped and the original is never overwritten.

The conversion dialog offers JPEG / PNG / WebP (PNG by default), quality 1-100 (default 92), lossless WebP on Android 11+, and a `Target file size` mode that selects quality automatically for JPEG or lossy WebP. `Resize` provides `Original`, `Percentage` (1-1000), `Long edge` (1920 px by default and never upscales), and `Custom` with optional aspect-ratio locking. JPEG can fill transparency with white or black; PNG automatically uses an indexed palette when the result has at most 256 colors. `Preserve safe EXIF metadata` is off by default and always removes GPS, embedded previews, and metadata that cannot be safely inspected. The dialog previews resolution, estimated size, and suffix. `Convert` asks the host to publish a new sibling with a `converted` suffix; `Cancel` or back creates no file.

## Supported Formats

The file manager shows the plugin actions for files with the following extensions (and any `image/*` MIME type):

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

The plugin identifies images by content and does not trust extensions: whether a file opens depends on whether Android can decode it. Animated files such as GIF and animated WebP are processed as their first frame only; input and output files are each capped at 256 MiB.

## Security

The plugin is built on a deny-by-default principle. All of the following measures are always on and cannot be disabled:

- The source file is strictly read-only: the plugin opens the input solely through a one-time read-only content URI granted by the host, receives no filesystem paths, and requests no storage or network permissions.
- Output goes only to the exact slot pre-created by the host, and on success the plugin returns nothing but the transaction ID; it cannot choose, create, or return any other URI.
- Every output transaction is single-use: consumed transaction IDs are recorded persistently, and replayed or duplicated requests are rejected outright.
- Every invocation is fully validated: a mismatch in protocol version, source surface, action ID, grant mode, MIME type, display name, or transaction ID aborts execution, and writable grants on the source are rejected as well.
- Input and output are each capped at 256 MiB, the output resolution at 16384 px per side and 40 MP in total, and the encoded byte count is enforced while writing.
- Freshly encoded output strips source metadata by default. Optional safe EXIF preservation uses a bounded allowlist; orientation is normalized, while GPS location, embedded previews, and metadata that cannot be safely inspected are always removed.
