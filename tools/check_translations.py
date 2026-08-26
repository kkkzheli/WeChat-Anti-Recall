#!/usr/bin/env python3
"""Verify trilingual (zh / en / zh-rTW) string coverage for WeChat-Anti-Recall.

Checks:
  1. All three locale files have identical key sets (parity).
  2. Every R.string.* referenced in Kotlin exists in all three locales.
  3. No hardcoded user-facing text literals remain in UI code
     (Chinese literals, Text("...") / contentDescription literals).

Exit code is non-zero when any issue is found, so it can gate a build or push.
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
RES = REPO / "app" / "src" / "main" / "res"
SRC = REPO / "app" / "src" / "main" / "java"
LOCALES = {
    "zh": RES / "values" / "strings.xml",
    "en": RES / "values-en" / "strings.xml",
    "zh-rTW": RES / "values-zh-rTW" / "strings.xml",
}

problems = []


def parse_keys(xml_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    keys = set()
    for child in root:
        if child.tag in ("string", "string-array") and child.get("name"):
            keys.add(child.get("name"))
        elif child.tag == "item":  # stray items without parent array
            continue
    return keys


def kotlin_files():
    for p in SRC.rglob("*.kt"):
        yield p


def rstring_references():
    refs = set()
    for p in kotlin_files():
        text = p.read_text(encoding="utf-8")
        for m in re.finditer(r"R\.string\.([A-Za-z0-9_]+)", text):
            refs.add(m.group(1))
    return refs


def hardcoded_literals():
    findings = []
    for p in kotlin_files():
        rel = p.relative_to(SRC)
        # Only UI-facing files are subject to localization
        if "ui" not in str(rel):
            continue
        for lineno, line in enumerate(p.read_text(encoding="utf-8").splitlines(), 1):
            stripped = line.strip()
            if stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*"):
                continue
            # Chinese literal in a quoted string
            if re.search(r'"[^"]*[一-鿿][^"]*"', line) and "R.string" not in line:
                findings.append(f"{rel}:{lineno}: Chinese literal: {stripped[:80]}")
            # Text( text = "..." literal that is not a resource
            # and not a dynamic template (e.g. "${...getDisplayName(Locale)}")
            if re.search(r'text\s*=\s*"[^"]+[A-Za-z一-鿿][^"]*"', line) and "stringResource" not in line and "${" not in line:
                findings.append(f"{rel}:{lineno}: hardcoded text literal: {stripped[:80]}")
            # contentDescription literal
            if re.search(r'contentDescription\s*=\s*"', line) and "stringResource" not in line:
                findings.append(f"{rel}:{lineno}: hardcoded contentDescription: {stripped[:80]}")
    return findings


def main():
    key_sets = {}
    for lang, path in LOCALES.items():
        if not path.exists():
            problems.append(f"MISSING file: {path}")
            continue
        key_sets[lang] = parse_keys(path)

    if not key_sets:
        print("FATAL: no locale files parsed")
        return 1

    base = key_sets["zh"]
    for lang, keys in key_sets.items():
        missing = base - keys
        extra = keys - base
        for k in sorted(missing):
            problems.append(f"[{lang}] missing key: {k}")
        for k in sorted(extra):
            problems.append(f"[{lang}] extra key (not in zh): {k}")

    refs = rstring_references()
    for k in sorted(refs):
        for lang, keys in key_sets.items():
            if k not in keys:
                problems.append(f"R.string.{k} referenced in Kotlin but missing in [{lang}]")

    for finding in hardcoded_literals():
        problems.append(f"HARDCODED: {finding}")

    if problems:
        print(f"TRANSLATION CHECK FAILED — {len(problems)} issue(s):")
        for p in problems:
            print(f"  - {p}")
        return 1

    zh_count = len(key_sets["zh"])
    print(f"TRANSLATION CHECK OK — {zh_count} keys, parity across zh/en/zh-rTW, "
          f"{len(refs)} R.string references resolved, no hardcoded literals.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
