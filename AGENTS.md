# AutoJs6-Plugin-Three-Maple-Image

Application ID: `io.github.supermonster003.autojs6.plugin.three.maple.image`. Preserve existing action/category/PluginInfo identity constants.

## Shared repository standard (2026-09-13)

Read [the complete repository standard](docs/development/repository-standard.md) before changing this repository. It is part of this repository guidance. Existing product-specific constraints above remain in force.

This APK contains ABI-independent managed code; native alignment verification rejects native dependencies. No ABI splits are appropriate. Release collection is `:app:appendDigestToReleasedFiles` and verifies the exact signed APK set. Do not claim physical ColorOS activation, projection consent or host output publication was tested unless it was actually exercised.

Run `.python/check_markdown.bat`, `py -3 -m unittest discover -s .python/tests`, and the Gradle Wrapper with `--max-workers=2`. Platform acceptance: `--no-daemon -Djava.vendor="Eclipse Adoptium" -Djava.vendor.version=Temurin-21.0.12.1+1 :app:assembleDebug :app:testDebugUnitTest`. Disable version auto-increment while checking a prepared commit. Before every commit set VERSION_BUILD to `git rev-list --count HEAD` plus one.


## Three-series identity and standalone app (2026-10-04)

- Follow `../AUTOJS6_PLUGIN_THREE_SERIES_RENAME_AGENTS.md`, `../AUTOJS6_PLUGIN_STANDALONE_SETTINGS_AGENTS.md` and `../AUTOJS6_PLUGIN_BLACK_N_WHITE_ADAPTIVE_ICON_AGENTS.md`. The current user's rename/merge decisions override the earlier identity-preservation wording above.
- Product: `3-Maple Image`; repository: `AutoJs6-Plugin-Three-Maple-Image`; applicationId/namespace: `io.github.supermonster003.autojs6.plugin.three.maple.image`; plugin ID: `three-maple-image`; version: `2.0.0`. New Android identity; old apps/data are not removed or migrated automatically.
- Public capability actions, AIDL packages, transaction order and engine names describe behavior and remain compatible. Four stable launcher aliases default to Auto; settings expose language, night mode, theme color, launcher icon and bundled release history.
- Preserve both source histories: Viewer `3ec7fe5` and Tools `8a1af1e`. The merge commit count includes both parents. One Explorer Action v12 service publishes `view-image`, `view-images`, `edit-image`, `convert-image`; output operations retain the bounded v3-style output envelope within v12. Standalone editor/converter subclasses are private and use local sessions; never weaken the exported host grant validation to support local files.
- Commit `.icons/`, portable generators, icon CI, `.gitattributes` and generated resources together. Ignore only caches, local signing files and build outputs. Icon Studio drafts/backups remain outside this repository in its ignored `.studio/`.


## Icon Studio publication snapshot (2026-10-04)

- `.icons/recipe.json` and its content-addressed original assets own the current icon geometry, tone and backgrounds. Keep the portable renderer, generated resources, keep rules and icon CI in the same change.
- Use `.python/generate_icon_studio.py --check` for read-only reproduction checks. Optical size bands are advisory; retain canvas, transparency and safe-circle checks. Three uses neutral foregrounds and fixed #FAFAFA / #212121 surfaces; other plugins may use colored artwork and custom or transparent surfaces.
