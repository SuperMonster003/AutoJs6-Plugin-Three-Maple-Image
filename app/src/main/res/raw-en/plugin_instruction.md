# AutoJs6 3-Maple Image

Image Viewer and Image Tools are combined in 3-Maple Image, with viewing, editing and format conversion in one app.
Language, dark mode, theme color and four launcher icon choices are available in the shared settings layout.
The application ID changes from io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools to io.github.supermonster003.autojs6.plugin.three.maple.image. Android installs this as a separate app; existing apps and data can remain, and settings are not migrated automatically.


3-Maple Image supplies the primary image action in the file manager for AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, and WEBP files.

HEIC and HEIF require Android 9 or later, while AVIF requires Android 12 or later. If the platform decoder is unavailable, the viewer shows the exact requirement instead of failing silently.

Oversized JPEG, PNG, and static HEIC/HEIF images use a bounded sampled preview and decode only visible high-resolution regions while zoomed. Tile caches are released on page changes and memory pressure.

To open only an explicit group, enter selection mode in AutoJs6, select 1 to 128 supported images from one folder, and tap `View image`. The viewer preserves selection order and left/right paging stays inside that group; every selected URI is independently validated and remains read-only.

The viewer fits the image to the screen and supports left/right same-folder paging at 1x, focal pinch zoom, panning, a double-tap toggle between fit and 2.5x around the touch point, and per-page metadata. The current multiplier appears while pinching and briefly after a double-tap. Animated GIFs loop with a pause/resume control that stays hidden for static images. The image fills the whole screen under translucent overlay bars; tap the image to hide or show them, and the title bar adds a `3 / 12` page counter when several images are open. Sharing and opening with another app are available for the originally opened image and disabled on session-only sibling pages.

`Rotate` turns only the current view and preserves an active zoom. `Reset zoom` in the top-right menu restores both the original orientation and fitted size.

Use `Details` to open a bottom sheet with the file name, MIME type, size, resolution, and the available EXIF capture time, device, exposure, and orientation. If GPS metadata exists, the viewer reports its presence but keeps the coordinates hidden.

The information bar always shows MIME type, file size, and decoded resolution. On Android 8.0 or later, it also shows decoded pixel depth (bpp) and output color space when the decoder exposes them.

Photos are automatically rotated or mirrored to their EXIF orientation. `Rotate` then adds a view-only turn on top of that corrected display.

`Print / Save PDF` in the top-right menu sends the full current image to Android's system print sheet, preserving EXIF correction and manual view rotation. Animated GIFs use the frame visible when tapped. Zoom and panning do not crop the result, and the plugin creates no temporary image or PDF file.

Host build 5276 or later is required.

Safety and privacy limits:

- The selected source uses a temporary read-only `content` URI; direct siblings are opened only through the scoped host `readSiblings` session.
- Files larger than 8 TiB are rejected.
- The plugin requests no storage or network permission.
- Permanent image editing, conversion, deletion, moving, and renaming remain host features.
