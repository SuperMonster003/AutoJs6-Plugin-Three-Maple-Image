"""Install a release APK on connected devices and check that it starts (release gate P3: install and cold start).

Usage:
    py -3 .python/release_smoke.py <apk> <serial>... [--from <older apk>] [--restore]

For each device: optionally install <older apk> first and start it once (the upgrade path), install <apk> over
whatever is there, compare the installed version with the APK's, cold start the launcher activity and check after a
few seconds that the process is alive and logged no fatal exception. One line per device: versions, start time,
pass or fail. With --restore a device that did not have the plugin before is left without it again; a device that
had it keeps the new package (Android cannot downgrade without wiping data), so --restore never touches those.

The script taps nothing. A first start may show the system's photo permission dialog; it is left alone (and goes
away with --restore). New screens and features are checked by the instrumentation tests on a debug build and by the
host's GalleryScriptApiTest against the release package.
"""
from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
import time
from pathlib import Path

PACKAGE = "io.github.supermonster003.autojs6.plugin.three.maple.image"


def adb(serial: str, *args: str, timeout: int = 180) -> str:
    result = subprocess.run(["adb", "-s", serial, *args], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=timeout)
    return (result.stdout + result.stderr).strip()


def installed_version(serial: str) -> tuple[str, str] | None:
    dump = adb(serial, "shell", "dumpsys", "package", PACKAGE)
    name = re.search(r"versionName=(\S+)", dump)
    code = re.search(r"versionCode=(\d+)", dump)
    return (name.group(1), code.group(1)) if name and code else None


def apk_version(apk: Path) -> tuple[str, str]:
    """Version of an APK file, read with aapt2 from the newest installed build tools."""
    sdk = Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT") or "")
    tools = sorted((sdk / "build-tools").glob("*/aapt2*"), key=lambda path: [int(part) for part in re.findall(r"\d+", path.parent.name)])
    if not tools:
        raise SystemExit("aapt2 not found; set ANDROID_HOME")
    badging = subprocess.run([str(tools[-1]), "dump", "badging", str(apk)], capture_output=True, text=True, encoding="utf-8", errors="replace").stdout
    match = re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", badging)
    if not match or match.group(1) != PACKAGE:
        raise SystemExit(f"{apk.name} is not a package of {PACKAGE}")
    return match.group(3), match.group(2)


def install(serial: str, apk: Path) -> str | None:
    output = adb(serial, "install", "-r", str(apk), timeout=600)
    return None if "Success" in output else output[-300:]


def start(serial: str) -> tuple[bool, str]:
    """Cold start through the launcher intent; returns whether it is alive without a crash, and a short detail."""
    adb(serial, "shell", "am", "force-stop", PACKAGE)
    since = adb(serial, "shell", "date", "+%s") + ".000"
    component = adb(serial, "shell", "cmd", "package", "resolve-activity", "--brief", "-c", "android.intent.category.LAUNCHER", PACKAGE).splitlines()[-1].strip()
    if "/" not in component:
        return False, "no launcher activity resolves"
    # A fresh task: a permission dialog left over from an earlier start would otherwise come back to the front alone.
    output = adb(serial, "shell", "am", "start", "-W", "--activity-clear-task", "-n", component)
    total = re.search(r"TotalTime: (\d+)", output)
    wait = re.search(r"WaitTime: (\d+)", output)
    activity = re.search(r"Activity: (\S+)/", output)
    time.sleep(4)
    alive = bool(adb(serial, "shell", "pidof", PACKAGE))
    crash = adb(serial, "logcat", "-d", "-v", "brief", "-s", "AndroidRuntime:E", "-T", since)
    crashed = PACKAGE in crash and "FATAL EXCEPTION" in crash
    if activity and activity.group(1) != PACKAGE and wait:
        # A first start that asks for photo access reports the system dialog; its TotalTime means nothing.
        detail = f"start {wait.group(1)} ms (wait time; a dialog of {activity.group(1)} is on top)"
    else:
        detail = f"start {total.group(1)} ms" if total else "start time unknown"
    if crashed:
        detail += "; FATAL EXCEPTION: " + " | ".join(line.split("): ", 1)[-1] for line in crash.splitlines() if "): " in line)[:300]
    elif not alive:
        detail += "; the process is gone"
    return alive and not crashed, detail


def smoke(serial: str, apk: Path, older: Path | None, restore: bool, expected: tuple[str, str]) -> bool:
    model = adb(serial, "shell", "getprop", "ro.product.model")
    api = adb(serial, "shell", "getprop", "ro.build.version.sdk")
    before = installed_version(serial)
    steps: list[str] = []
    passed = True
    try:
        if older is not None:
            failure = install(serial, older)
            if failure:
                print(f"{serial} ({model}, API {api}): FAILED to install {older.name}: {failure}")
                return False
            ok, detail = start(serial)
            older_version = installed_version(serial) or ("?", "?")
            steps.append(f"{older_version[0]} ({older_version[1]}) {detail}")
            passed = passed and ok
        failure = install(serial, apk)
        if failure:
            print(f"{serial} ({model}, API {api}): FAILED to install {apk.name}: {failure}")
            return False
        after = installed_version(serial)
        if after != expected:
            steps.append(f"installed version {after} differs from the APK's {expected}")
            passed = False
        ok, detail = start(serial)
        steps.append(f"{expected[0]} ({expected[1]}) {detail}")
        passed = passed and ok
    finally:
        adb(serial, "shell", "am", "force-stop", PACKAGE)
        kept = "kept"
        if restore and before is None:
            adb(serial, "uninstall", PACKAGE)
            kept = "removed again" if installed_version(serial) is None else "COULD NOT BE REMOVED"
    was = f"{before[0]} ({before[1]})" if before else "not installed"
    print(f"{serial} ({model}, API {api}): {'pass' if passed else 'FAIL'}; before: {was}; " + "; ".join(steps) + f"; package {kept}")
    return passed


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n", 1)[0])
    parser.add_argument("apk", type=Path, help="the release APK to check")
    parser.add_argument("serials", nargs="+", help="adb serials, one device after another")
    parser.add_argument("--from", dest="older", type=Path, help="install this older release first and start it once")
    parser.add_argument("--restore", action="store_true", help="uninstall again on devices that did not have the plugin")
    arguments = parser.parse_args()
    expected = apk_version(arguments.apk)
    if arguments.older is not None:
        apk_version(arguments.older)
    results = [smoke(serial, arguments.apk, arguments.older, arguments.restore, expected) for serial in arguments.serials]
    return 0 if all(results) else 1


if __name__ == "__main__":
    sys.exit(main())
