3-Maple Image views and edits images and converts image formats. It can be opened on its own to browse and organize the images on the device by album; tapping an image file in the AutoJs6 file explorer also opens it in 3-Maple Image, and the file menu offers editing and conversion. AutoJs6 scripts can call it through the `gallery` module.

### Getting started

1. Open 3-Maple Image and grant photo access when prompted; the home page then lists the images on the device by album. Without that access, use "Choose image" or "Choose folder" to open images through the system picker.
2. Enable 3-Maple Image in the "Plugin center" of AutoJs6. Tapping an image file in the AutoJs6 file explorer then opens it, and "Edit image" and "Convert image" in the file menu edit and convert it.

### Main features

- **Albums**: the home page lists images by album, with "Favorites" first. You can search album names and file names and change the sort order, the column count and grouping by date; long-press an album to "Pin", "Exclude" or "Hide" it.
- **Viewer**: zoom, paging, a thumbnail strip and "Rotate", with GIF animations looping. The menu at the top right offers "Slideshow", "Set as wallpaper", "Share a copy", "Print / Save PDF" and more.
- **Details**: tap "Details" to see the file name, size, resolution and EXIF capture information.
- **Organizing**: long-press in the image grid to select several images, then share, favorite, move, copy or delete them. Deleted images go to the "Recycle bin" first, and the message offers an undo.
- **Editing**: the editor tools come in six groups such as "Transform", "Adjust", "Filters" and "Text", with undo and redo. Editing and converting save a new file by default and leave the original unchanged; only with "Overwrite the original" turned on is the original replaced, and the replacement can be undone.
- **Conversion**: "Convert image" changes the format (JPEG, PNG, WebP), quality and size, and previews the output size. Select several images in an album or in the AutoJs6 file explorer to convert them in a batch.

### Using it in scripts

AutoJs6 scripts use this plugin through the `gallery` module. The example below lets the user choose up to 5 images, converts them to WebP with a long edge of 1080 pixels, and shows the results in the viewer:

```js
let images = gallery.pick({ multiple: true, maxCount: 5 });
if (images) {
    let outputs = images.map((image) => gallery.convert(image.path, { format: 'webp', longEdge: 1080 }).path);
    gallery.view(outputs);
}
```

- Every method also has an asynchronous form that returns a Promise, such as `gallery.pickAsync`. The synchronous `pick`, `edit` and `convert` can wait a long time and must not be called on the UI thread.
- Images are given by file path, and relative paths are resolved against the working directory of the script. Output is written next to the original by default (the file name gets `_converted` or `_edited`), and the `output` option sets another location.
- Picking images needs the photo access of the plugin, which the picker requests first. Picking, viewing and editing open the plugin screens, so scripts run more reliably while the AutoJs6 screen is visible.
- Call `gallery.isAvailable()` before running to check that the plugin is available. See [AutoJs6 docs > gallery](https://docs.autojs6.com/#/gallery) for all options and error codes.

### Requirements

- Android 7.0 or later. HEIC and HEIF images need Android 9 or later, and AVIF images need Android 12 or later.
- Using it in AutoJs6 (the file explorer and the `gallery` module) needs AutoJs6 6.8.0 or later with this plugin enabled in the "Plugin center"; it does not need AutoJs6 when used on its own.
- Photo access is used to browse images by album on the home page and to let scripts pick images; opening images from the AutoJs6 file explorer or other apps requests no permission.
- "All files access" and "Media management" are optional permissions that let the plugin hide albums and delete or move images without asking each time; turn them on in "Settings" > "Storage permission".

### FAQ

**Using it without photo access**

The home page shows "Choose image" and "Choose folder", so images can still be viewed, edited and converted through the system picker. Access can be granted later in "Settings" > "Storage permission"; on Android 14 and later, if only some photos are allowed, the home page shows only those photos, and "Manage selected photos" adds or removes photos.

**"Edit image" and "Convert image" missing from the file menu**

AutoJs6 6.8.1 and later put these two items in the 3-Maple Image submenu of the file menu. If they still cannot be found, check that the plugin is enabled in the "Plugin center" and that the file is in a supported image format, and update AutoJs6 to the latest version.

**Restoring deleted images**

Images deleted in an album first go to the "Recycle bin" in the menu at the top right of the home page, where they can be restored; they are removed automatically after about 30 days. Images deleted after opening them from AutoJs6 go to the AutoJs6 trash and are restored in AutoJs6.

**Showing and keeping photo location**

Coordinates are not shown by default, and images produced by editing, converting and "Share a copy" have their location removed. With "Show location" turned on in "Settings", the details show the coordinates and offer "Open in map", and "Keep location" can be checked when saving or converting. This plugin has no network permission.

See the [project page](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image) for the full feature description, supported formats and FAQ.
