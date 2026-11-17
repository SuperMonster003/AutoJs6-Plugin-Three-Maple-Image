"""Measure lossy PNG encoding on connected devices (PngLossyInstrumentationTest.measure, ROADMAP 11.4).

Usage:
    py -3 .python/png_lossy_benchmark.py <serial>... [--replace] [--keep]

Build the packages first (:app:assembleDebug :app:assembleDebugAndroidTest). For each device the script installs the
debug APK and its test APK, runs the benchmark (no UI, no permission, synthetic images of 3.1 and 12 MP) and prints
the result lines the test writes to logcat (tag MaplePng). A device that already has the plugin is skipped unless
--replace is given: installing over it stops the running plugin, and a release build has another signature. Packages
this script installed are removed again unless --keep is given.
"""
from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

PACKAGE = "io.github.supermonster003.autojs6.plugin.three.maple.image"
TEST_PACKAGE = PACKAGE + ".test"
RUNNER = "androidx.test.runner.AndroidJUnitRunner"
TEST = PACKAGE + ".PngLossyInstrumentationTest#measure"
TAG = "MaplePng"
ROOT = Path(__file__).resolve().parent.parent


def adb(serial: str, *args: str, timeout: int = 120) -> subprocess.CompletedProcess:
    return subprocess.run(["adb", "-s", serial, *args], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=timeout)


def single_apk(directory: str) -> Path:
    found = sorted((ROOT / directory).glob("*.apk"))
    if len(found) != 1:
        raise SystemExit(f"expected exactly one APK in {directory}, found {len(found)}; build the debug packages first")
    return found[0]


def installed(serial: str, package: str) -> bool:
    lines = adb(serial, "shell", "pm", "list", "packages", package).stdout.split()
    return f"package:{package}" in lines


def measure(serial: str, apks: list[Path], replace: bool, keep: bool) -> bool:
    model = adb(serial, "shell", "getprop", "ro.product.model").stdout.strip()
    api = adb(serial, "shell", "getprop", "ro.build.version.sdk").stdout.strip()
    print(f"== {serial} ({model}, API {api})")
    had_plugin = installed(serial, PACKAGE)
    if had_plugin and not replace:
        print("   skipped: the plugin is installed (pass --replace to install over it)")
        return True
    try:
        for apk in apks:
            result = adb(serial, "install", "-r", str(apk), timeout=600)
            if "Success" not in result.stdout:
                print(f"   FAILED to install {apk.name}: {(result.stdout + result.stderr).strip()[-300:]}")
                return False
        # Seconds since the epoch: a form of logcat -T that has no space to quote.
        since = adb(serial, "shell", "date", "+%s").stdout.strip() + ".000"
        run = adb(serial, "shell", "am", "instrument", "-w", "-e", "class", TEST, "-e", "pngLossyBenchmark", "true", f"{TEST_PACKAGE}/{RUNNER}", timeout=3600)
        passed = "OK (1 test)" in run.stdout
        log = adb(serial, "logcat", "-d", "-v", "brief", "-s", f"{TAG}:I", "-T", since).stdout
        lines = [line.split("): ", 1)[1].strip() for line in log.splitlines() if line.startswith(f"I/{TAG}") and "): " in line]
        for line in lines:
            print("   " + line)
        if not passed:
            print("   FAILED: " + run.stdout.strip()[-400:])
        elif not lines:
            print("   FAILED: the test passed but logged nothing")
        return passed and bool(lines)
    finally:
        if not keep:
            adb(serial, "uninstall", TEST_PACKAGE)
            if not had_plugin:
                adb(serial, "uninstall", PACKAGE)
            left = [package for package in (PACKAGE, TEST_PACKAGE) if installed(serial, package)]
            print("   cleanup: " + ("left " + ", ".join(left) if left else "both packages removed"))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n", 1)[0])
    parser.add_argument("serials", nargs="+", help="adb serials of the devices to measure, one after another")
    parser.add_argument("--replace", action="store_true", help="also measure on devices that already have the plugin")
    parser.add_argument("--keep", action="store_true", help="leave the installed packages on the device")
    arguments = parser.parse_args()
    apks = [single_apk("app/build/outputs/apk/debug"), single_apk("app/build/outputs/apk/androidTest/debug")]
    results = [measure(serial, apks, arguments.replace, arguments.keep) for serial in arguments.serials]
    return 0 if all(results) else 1


if __name__ == "__main__":
    sys.exit(main())
