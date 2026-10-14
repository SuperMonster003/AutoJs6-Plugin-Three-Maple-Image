# Sources and acknowledgments

3-Maple Image combines the maintainer's Image Viewer and Image Tools projects under MPL-2.0. Their original Git histories remain reachable from the merge commit. Earlier Image Tools documentation is retained under `docs/history/image-tools/`.

- Image loading: [Glide](https://github.com/bumptech/glide), at the version pinned in `gradle/libs.versions.toml`. Retain its upstream license notices and bundled component licenses.
- Android UI, lifecycle and EXIF handling: [AndroidX](https://android.googlesource.com/platform/frameworks/support/) and [Material Components](https://github.com/material-components/material-components-android), under their respective Apache-2.0 notices.
- Compose user interface: [Jetpack Compose](https://developer.android.com/jetpack/compose) (AndroidX, Apache-2.0) at the BOM version pinned in `gradle/libs.versions.toml`. Its `ui-graphics` artifact depends on `androidx.graphics:graphics-path`, whose `libandroidx.graphics.path.so` is the only native library in the APK (Apache-2.0).
- Icons: the vector drawables under `app/src/main/res/drawable` are [Material Design Icons](https://github.com/google/material-design-icons) (Google, Apache-2.0), recolored through theme tints and otherwise unchanged. Exception: the drawables whose file starts with the comment "Drawn for 3-Maple Image" (`ic_filter_24`, `ic_exposure_24`, `ic_highlights_24`, `ic_shadows_24`, `ic_sharpen_24`, `ic_vignette_24`, `ic_resize_24`, `ic_annotate_24`, `ic_shape_rectangle_24`, `ic_shape_ellipse_24`, `ic_shape_arrow_24`, `ic_shape_line_24`) are simple line icons made for this plugin and share its license.
- Compose image loading: the [Glide Compose integration](https://github.com/bumptech/glide/tree/master/integration/compose), under Glide's BSD-2-Clause / Apache-2.0 notices, at the version pinned in `gradle/libs.versions.toml`.
- App appearance and optical icon geometry reuse the maintainer's existing AutoJs6 plugin implementations. The maple artwork was supplied by the maintainer; the original light/dark files are preserved in `.python/icons/`.

Dependency licenses continue to apply independently of this project's license. Rights inquiries: [RIGHTS_AND_TAKEDOWN.md](RIGHTS_AND_TAKEDOWN.md).
