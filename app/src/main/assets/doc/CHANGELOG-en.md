******

### Release history

******

# v1.0.1

###### 2026/08/08

* `Fix` Return a valid Explorer Action service binding when enabled from Plugin Center
* `Improvement` Use a shorter plugin name and description with more natural user documentation

# v1.0.0

###### 2026/08/02

* `Feature` Image Tools plugin with plugin ID `image-tools`, action IDs `edit-image` and `convert-image`, engine `explorer-action`, and variant `default`
* `Feature` Explorer Action protocol v3 overflow actions with one read-only image input and host-owned create-sibling output transactions
* `Feature` Image editor with crop, rotation, flip, brightness, contrast, saturation, color temperature, brush, text, and undo
* `Feature` JPEG, PNG, and WebP conversion with quality, resizing, aspect-ratio locking, JPEG background, and memory limits
* `Feature` ContentResolver and ParcelFileDescriptor input and output without raw paths, direct sibling writes, arbitrary result URIs, storage permission, or network permission
* `Feature` Localized plugin metadata, interface text, instructions, README files, and changelogs in 10 languages
* `Improvement` Retained editor and converter sessions across configuration changes, including canvas tools, dialog drafts, conversion options, undo history, and in-flight work
* `Improvement` Protected host output transactions with a persistent single-use claim, busy guards, cancellation-safe coroutines, and result delivery after the writer closes
* `Improvement` Hardened output MIME validation, bitmap cleanup, English resource parity, ellipsis lint handling, and release digest stream cleanup
* `Dependency` Added AndroidX ExifInterface 1.4.2 for secure image metadata parsing
* `Dependency` Added Robolectric 4.16.1 for lifecycle and persistent transaction tests

# v1.1.0

###### 2026/09/11

* `Improvement` Build verification rejects accidental native dependencies and produces a JSON report
