# Baseline profile

`app/src/main/baseline-prof.txt` is a generated ART baseline profile. The Android Gradle plugin merges it with the
profiles embedded in the AndroidX libraries, rewrites it through the R8 mapping and packs the result into
`assets/dexopt/baseline.prof`; `androidx.profileinstaller` (a Compose dependency) installs it on first launch and the
next background dexopt compiles the listed methods ahead of time.

Why it exists: after the Compose migration (2.1.0) the standalone home cold start on a 2017 API 28 phone (Sony G8441)
was 129% of 2.0.1 even after dexopt with only the library profiles, and 147-156% on a fresh install. With this profile
the dexopt state is 107-115% on API 28 and 110% on API 35 (Xiaomi 23046RP50C); the viewer stays within 102-106% and the
editor's first frame is faster than before. The measurements are recorded in `docs/design/ui-redesign.md` section 3.5.

## Regenerate

1. Install the **debug** build (unobfuscated names) on at least one API 28-30 device and one API 31+ device, then drive
   the pages whose startup matters: home, settings, about, release history, converter (pick an image, change format and
   resize mode), viewer (external `ACTION_VIEW`), editor (adjust, brush, text). Repeat the round twice and leave the
   app in the background for about 20 seconds so ART flushes the profile.
2. Dump the profile on each device:

   ```bash
   adb shell cmd package dump-profiles --dump-classes-and-methods io.github.supermonster003.autojs6.plugin.three.maple.image
   adb pull /data/misc/profman/io.github.supermonster003.autojs6.plugin.three.maple.image-primary.prof.txt profile-<device>.txt
   ```

   API 28-30 devices do not support `--dump-classes-and-methods`; drop the flag, the script converts the resolved
   signature format it produces.
3. Merge the dumps into the source file:

   ```bash
   py -3 .python/baseline_profile.py profile-g8441.txt profile-pad.txt
   ```

4. Build the release APK, install it, launch once, force the profile install and the compilation, then measure:

   ```bash
   adb shell am broadcast -a androidx.profileinstaller.action.INSTALL_PROFILE -n io.github.supermonster003.autojs6.plugin.three.maple.image/androidx.profileinstaller.ProfileInstallReceiver
   adb shell cmd package compile -m speed-profile -f io.github.supermonster003.autojs6.plugin.three.maple.image
   adb shell am force-stop io.github.supermonster003.autojs6.plugin.three.maple.image
   adb shell am start -W -n io.github.supermonster003.autojs6.plugin.three.maple.image/.launcher.AdaptiveAutoIconAlias
   ```

   Compare the `TotalTime` medians of five or ten cold starts against the previous release built from its tag on the
   same device.

Keep the full rule set. Trimming it to the app's own packages, the plugin API, Material color utilities, Kotlin and
Glide (5,191 of 25,847 rules) brought the API 28 home cold start back to 125% of 2.0.1 in the same session, so the
AndroidX rules captured from the real app are doing work that the library profiles alone do not.

The raw device dumps are not committed; keep them under `.bak/` if a later comparison needs them.
