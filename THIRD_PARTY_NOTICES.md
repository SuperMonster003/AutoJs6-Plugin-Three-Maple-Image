# Sources and acknowledgments

3-Maple Image combines the maintainer's Image Viewer and Image Tools projects under MPL-2.0. Their original Git histories remain reachable from the merge commit. Earlier Image Tools documentation is retained under `docs/history/image-tools/`.

- Image loading: [Glide](https://github.com/bumptech/glide), at the version pinned in `gradle/libs.versions.toml`. Retain its upstream license notices and bundled component licenses.
- Android UI, lifecycle and EXIF handling: [AndroidX](https://android.googlesource.com/platform/frameworks/support/) and [Material Components](https://github.com/material-components/material-components-android), under their respective Apache-2.0 notices.
- App appearance and optical icon geometry reuse the maintainer's existing AutoJs6 plugin implementations. The maple artwork was supplied by the maintainer; the original light/dark files are preserved in `.python/icons/`.

Dependency licenses continue to apply independently of this project's license. Rights inquiries: [RIGHTS_AND_TAKEDOWN.md](RIGHTS_AND_TAKEDOWN.md).
