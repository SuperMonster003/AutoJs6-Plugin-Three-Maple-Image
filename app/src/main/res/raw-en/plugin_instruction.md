# Image Viewer

Image Viewer supplies the primary image action in AutoJs6 Explorer for BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, and WEBP files.

The viewer fits the image to the screen and supports focal pinch zoom, panning, double-tap reset, metadata, sharing, and opening with another app. Tap the image to hide or show the controls.

The plugin requires AutoJs6 build 5269+. It is implemented entirely on the JVM and is independent of device ABI.

Safety and privacy limits:

- The source is opened through temporary read-only `content` URI access.
- Files larger than 8 TiB are rejected.
- The plugin requests no storage or network permission.
- Image editing, conversion, deletion, moving, and renaming remain host features.
