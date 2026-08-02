# Image Tools

Image Tools supplies `Edit image` and `Convert image` overflow actions in AutoJs6 Explorer.

The editor supports crop, rotation, horizontal and vertical flip, brightness, contrast, saturation, color temperature, brush strokes, text, and undo. The converter supports JPEG, PNG, and WebP output, quality control, percentage or custom resizing, aspect-ratio locking, and JPEG background selection.

The plugin requires AutoJs6 build 5269+. It is implemented entirely on the JVM and is independent of device ABI.

Safety and privacy limits:

- The source is opened only through exact read-only `content` URI access.
- Output is encoded only to the exact host-owned output transaction URI.
- The plugin never writes beside the source and never returns an arbitrary URI.
- Encoded output is limited to JPEG, PNG, or WebP and at most 256 MiB.
- The plugin requests no storage or network permission.
