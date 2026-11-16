# Follow AutoJs6 settings admission fix

Investigation started on 2026-11-16; device verification completed on 2026-11-17.

## Cause and scope

QV770340J7 (Sony XQ-DQ72, Android 13 / API 33) had AutoJs6 6.8.1 (6641) installed
and enabled, and Maple 3.4.3 (209) held `org.autojs.permission.PLUGIN`. The host
settings provider existed. Its actual response to Maple was:

```text
java.lang.SecurityException: Official plugin settings access denied: PLUGIN_ENTRY_UNAVAILABLE
```

`OfficialPluginSettingsProvider.hasProtectedPluginEntry` requires an enabled,
exported `org.autojs.plugin.INFO` service protected by the plugin permission.
Maple only declared Explorer Action and Gallery services. The appearance client
mapped the provider rejection to `CONTRACT_UNAVAILABLE`, triggering the fallback
subtitle despite the host being installed.

The workspace audit covered all 59 Android plugin applications. Of the 29
applications that read the host appearance contract, only Maple and 3-Stack
Archive lacked the required INFO entry. Both are fixed. R8 Compiler and Color
Picker declare provider visibility instead of package visibility, which satisfies
their lookup needs. AutoJs4 Runtime targets API 28. Four additional plugin
directories have no Android application manifest yet: Cable Connect, Ferry Sync,
Flint Trigger and Scope Inspector. Other plugins without an appearance client do
not need a new INFO endpoint for this issue.

The new `PluginInfoService` implements `IPluginInfoProvider` and reuses
`imageViewerPluginInfo`, including the unrestricted ABI declaration. It is separate
from the existing Explorer Action service, whose Binder descriptor and actions
remain unchanged. Host signature checks and settings fallback behavior are unchanged.

## Verification

- The Python manifest regression failed before the fix with INFO service count 0,
  then passed. All 10 Python tests and generated Markdown checks pass.
- Gradle `assembleDebug`, `testDebugUnitTest`, `assembleDebugAndroidTest`,
  `lintDebug` and `appendDigestToReleasedFiles` pass with `--max-workers=2`,
  version auto-increment disabled and the repository's Temurin acceptance options.
- JVM tests: 388 passed, no failures or skipped tests.
- Debug lint: 0 errors, 132 warnings and 1 hint. This change does not address the
  wider lint warning inventory.
- The two `HostSettingsContractInstrumentationTest` cases pass on QV770340J7 with
  the signed Debug APK and `-e requireHostSettings true`.
- A framework-only instrumentation probe was run under the installed plugin UID
  against the original and final signed release APKs. Before: INFO count 0 and
  `PLUGIN_ENTRY_UNAVAILABLE`, 0/2 pass. After: INFO discovery, permission, Binder
  descriptor, installed metadata and parity with Explorer Action all pass; the
  actual host settings call also passes, 2/2 pass.
- Final snapshot: host 6.8.1 (6641), protocol 1, language/resolved language
  `zh-Hans`, night policy `LIGHT`, active `false`, primary/accent `#FFDEAD`.
- Final installed package: 3.4.4 (210), signed release, installed with `adb install -r`.
  The old and new signer certificate SHA-256 match. No app data was cleared.
- Signed APK collection and 16 KB native/ZIP alignment verification pass.

The Debug test runner depends on Kotlin runtime classes that release shrinking
may remove. Therefore the signed release was checked with a separate, read-only
framework probe, not by claiming that the Debug runner passed against the release.

The phone remained securely locked during verification. No settings-dialog visual
or click-through pass is claimed. Image editing, conversion, ColorOS activation
and other business flows were not part of this fix's device regression.

## Local artifacts

- Release: `releases/autojs6-plugin-three-maple-image-v3.4.4-fa463eaf.apk`.
- `.bak/host-settings-20261116/` retains the original APK, package states, build
  log, workspace audit JSON, Debug instrumentation results and before/after release
  probe output. The framework probe source and build script are in its `probe/`
  subdirectory. These local artifacts are ignored by Git.

The 3-Stack counterpart is 3.1.3 (134), validated on BH900ASK9E (API 28) with the
same host version and the same before/after admission result.
