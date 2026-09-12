# Screenshot provenance

The screenshots in this directory document the real AutoJs6 file-manager flow for Image Tools. They are original Android screen captures: no crop, resize, compositing, retouching, or AI image generation was applied.

## Capture environment

- Device: physical Sony Android device (`1096 x 2560`, density `420`)
- Android: 13
- Locale: `en-US`
- Host: AutoJs6 `6.8.0` (`versionCode 5276`, development build)
- Plugin: Image Tools `1.0.1` (`versionCode 5`, debug build from this repository)
- Capture command: Android `screencap -p`

## Synthetic fixture

Every screen uses repository-owned synthetic content with no personal data:

- Editable source: [`docs/fixtures/image-tools-showcase.svg`](../../fixtures/image-tools-showcase.svg)
- Device input: [`docs/fixtures/image-tools-showcase.png`](../../fixtures/image-tools-showcase.png)

The PNG is a deterministic mechanical render of the SVG at `1600 x 1200`; it was not generated from a photograph or user file.

## Assets

| File | State shown |
|---|---|
| `file-menu-action.png` | AutoJs6 image-file overflow menu with the two Image Tools actions |
| `editor.png` | Loaded image editor, canvas, save/undo controls, and scrollable tool bar |
| `converter-dialog.png` | JPEG conversion controls, including quality, target-size, resize, background, and safe EXIF options |
| `conversion-result.png` | Pending: successful host publication beside the source |

## Pending result capture

The result screenshot is deliberately absent. On AutoJs6 `6.8.0` development builds 5276 and 5277, tested on physical Android 12/13 devices and an Android 13 emulator, the plugin writes and closes a valid JPEG but the host rolls the output transaction back during final publication.

The captured transaction evidence reached `COMMITTING` with detected MIME type `image/jpeg`, a verified snapshot SHA-256, a `1600 x 1200` native-decodable image, and `186,913` encoded bytes. This places the failure after plugin encoding and host image validation, but before the new sibling file is durably published. A success screenshot will be added only after the host publication path works end to end; no result image is simulated here.

## Refresh procedure

1. Render `image-tools-showcase.svg` to an opaque, stripped, 8-bit sRGB PNG at exactly `1600 x 1200`.
2. Push the PNG to a dedicated synthetic folder under the AutoJs6 Scripts directory.
3. Set both AutoJs6 and Image Tools to `en-US`, enable the plugin, and exercise the overflow-menu action on a physical device.
4. Capture each state with `screencap -p` and pull the unmodified PNG into this directory.
5. Verify dimensions, color space, bit depth, visible content, and the absence of personal data before regenerating the README files.
