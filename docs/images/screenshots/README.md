# Screenshot capture notes

These documentation screenshots are raw Android UI captures of the plugin running through the AutoJs6 file manager. The gallery content and every visible file name were created specifically for this capture set from deterministic SVG geometry. They do not contain user photos, accounts, contacts, notifications, or production data.

## Capture environment

- Host: AutoJs6 6.8.0, version code 5277
- Plugin: Image Viewer 1.0.1, version code 5
- Device: Android 13 emulator, API 33
- Capture viewport: 1080 x 2340 at 420 dpi, light mode, English locale
- Input files: `01-orbit.png`, `02-layers.jpg`, `03-spectrum.webp`, and `04-pulse.gif`
- Acquisition: raw `adb screencap` PNG output, with no crop, compositing, color adjustment, or AI-generated interface
- Metadata check: all five PNG files have no embedded profile or comment

## Scenes

| File | Verified state |
| --- | --- |
| `explorer-action.png` | Four synthetic images are recognized by AutoJs6 and expose the Image Viewer action. |
| `explorer-selection.png` | Exactly two images are selected and the read-only `View image` selection action is available. |
| `viewer-main.png` | The first image is rendered with its file name, MIME type, byte size, decoded dimensions, pixel depth, and color space. |
| `viewer-zoom-2.5x.png` | A real double-tap zoom is active and the transient `2.5x` indicator is visible. |
| `share-sheet.png` | Android's system chooser receives the temporary read-only image grant and renders the image preview. |

## SHA-256

```text
explorer-action.png    657be0a54f1f5824ad70926dd6507c09d4e11046a8f99e5db4f3b76274ac62d5
explorer-selection.png 87ef216c7f3c9904696332f6f21371dbdb0950416cee40d993ae6e69a3bc7e30
share-sheet.png         f14fc051fe8d83e85f01d05d6f8b51ae6a97b9351f160f5c5a21e66b09ef2b51
viewer-main.png         1bdfb2d10294b644f08af8d152eb6e2f9cbb6c580abee3b8b2ae0d750c1d33ec
viewer-zoom-2.5x.png    a80f665f030cb31b362aa0839c7738993e54e0819adca2fe3c582ee2b2fc8e01
```

## 3.0.0 gallery captures (2026-10-13)

- Plugin: 3-Maple Image release build of the 3.0.0 development line (`VERSION_NAME` still 2.1.0), running as the standalone app
- Device: Android 13 emulator (AVD_API_33, host GPU), 1080 x 2340 at 420 dpi, light mode, English locale, system UI demo mode (12:00, full battery, no notifications)
- Library: 52 synthetic images in four albums (`Orbits`, `Waves`, `Mosaic`, `Horizons`) drawn from deterministic geometry with PIL for this capture set, plus the earlier synthetic `Pictures/01-orbit.png`; no other media was present while capturing
- Acquisition: raw `adb exec-out screencap -p` PNG output, with no crop, compositing or color adjustment

| File | Verified state |
| --- | --- |
| `gallery-albums.png` | The album home lists the synthetic albums with cover, name and image count. |
| `gallery-media-grid.png` | The `Orbits` album opens as a four-column square grid. |
| `viewer-thumbnail-strip.png` | An image opened from the grid shows the page counter `12 / 14` and the thumbnail strip above the metadata line with the current page outlined. |
| `gallery-no-permission.png` | After photo access is denied, the home keeps `Grant photo access`, `Choose folder`, `Choose image` and the Edit / Convert tool rows. |

```text
gallery-albums.png          1a7d3c9017d898bdd5877a6d04fcb3fdd88f5e15985fe3fc6ce05b45be072352
gallery-media-grid.png      c7554e6ed609c60ad784ca67d781bb3033569690847a00bf7832338454746fda
gallery-no-permission.png   a98c9614da1da3b4fc9de9c0efe94be596f4473614306f4b5d73171b7f81f507
viewer-thumbnail-strip.png  703d23cfe71c4320fed107ef4f5e69b6bec0988ac6a5953e7b16dd0ea966a15f
```
