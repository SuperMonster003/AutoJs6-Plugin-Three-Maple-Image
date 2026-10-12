"""Regenerate app/src/main/baseline-prof.txt from ART profiles dumped on devices.

Usage:
    py -3 .python/baseline_profile.py <dump.txt>... [--out app/src/main/baseline-prof.txt]

Each <dump.txt> is the file that `adb shell cmd package dump-profiles [--dump-classes-and-methods] <package>` writes to
/data/misc/profman/<package>-primary.prof.txt after the debug build (unobfuscated names) was driven through the app.
Dumps in the human readable rule format (API 31+, `--dump-classes-and-methods`) are taken as they are; the older
resolved-signature format (API 28-30, "void pkg.Class.method(int)") is converted to rules. Rules from every input are
merged, flags (H hot, S startup, P post-startup) are unioned, and the result is written sorted with a header.
"""
from __future__ import annotations

import argparse
import re
from pathlib import Path

PRIMITIVES = {"void": "V", "boolean": "Z", "byte": "B", "char": "C", "short": "S", "int": "I", "long": "J", "float": "F", "double": "D"}
HEADER = [
    "# Baseline profile for 3-Maple Image (generated, do not edit by hand; see docs/development/baseline-profile.md).",
    "# Source: ART profiles of the debug build after driving home, settings, about, release history, converter, viewer and",
    "# editor on real devices. AndroidX rules are kept on purpose: the libraries' embedded profiles alone left the API 28",
    "# home cold start at 129% of 2.0.1, the full set brings it to 107-115%.",
]


def descriptor(java_type: str) -> str:
    java_type = java_type.strip()
    dims = java_type.count("[]")
    java_type = java_type.replace("[]", "")
    base = PRIMITIVES.get(java_type) or "L" + java_type.replace(".", "/") + ";"
    return "[" * dims + base


def rule_from_signature(signature: str) -> str | None:
    signature = re.sub(r"\[[^\]]*\]\s*$", "", signature.strip()).rstrip(",").strip()
    match = re.match(r"^(\S+)\s+([\w.$]+)\.([\w$<>]+)\((.*)\)$", signature)
    if not match:
        return None
    ret, cls, name, args = match.groups()
    params = "".join(descriptor(a) for a in args.split(",") if a.strip())
    return "L" + cls.replace(".", "/") + ";->" + name + "(" + params + ")" + descriptor(ret)


def parse(path: Path, flags: dict[str, set[str]], classes: set[str]) -> None:
    section = None
    for raw in path.read_text(encoding="utf-8", errors="replace").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        rule = re.match(r"^([HSP]*)(L[\w/$\-]+;)(->.*)?$", line)
        if rule:
            prefix, cls, method = rule.groups()
            if method:
                flags.setdefault(cls + method, set()).update(prefix)
            else:
                classes.add(cls)
            continue
        if line.startswith("hot methods"):
            section = "H"
            continue
        if line.startswith("startup methods"):
            section = "S"
            continue
        if line.startswith("post startup methods"):
            section = "P"
            continue
        if line.startswith("classes"):
            section = "C"
            rest = line.split(":", 1)[1] if ":" in line else ""
            classes.update("L" + c.strip().replace(".", "/") + ";" for c in rest.split(",") if c.strip())
            continue
        if line.startswith("base.apk") or line.startswith("ProfileInfo"):
            section = None
            continue
        if section in ("H", "S", "P"):
            for part in re.split(r"\]\s*,\s*", line):
                converted = rule_from_signature(part + "]" if "[" in part and not part.endswith("]") else part)
                if converted:
                    flags.setdefault(converted, set()).add(section)
        elif section == "C":
            classes.update("L" + c.strip().replace(".", "/") + ";" for c in line.split(",") if c.strip())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("dumps", nargs="+", type=Path)
    parser.add_argument("--out", type=Path, default=Path("app/src/main/baseline-prof.txt"))
    args = parser.parse_args()
    flags: dict[str, set[str]] = {}
    classes: set[str] = set()
    for dump in args.dumps:
        parse(dump, flags, classes)
    lines = list(HEADER) + sorted(classes)
    lines += ["".join(flag for flag in "HSP" if flag in rule_flags) + rule for rule, rule_flags in sorted(flags.items())]
    args.out.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    app_rules = sum(1 for rule in flags if "supermonster003" in rule)
    print(f"classes {len(classes)}, methods {len(flags)}, app methods {app_rules}, written to {args.out}")


if __name__ == "__main__":
    main()
