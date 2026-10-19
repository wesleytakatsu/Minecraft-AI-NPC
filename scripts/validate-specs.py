#!/usr/bin/env python3
"""Validate versioned specs and local Markdown links without third-party packages."""
import json
import re
import sys
from pathlib import Path
from urllib.parse import unquote

ROOT = Path(__file__).resolve().parent.parent
SPECS = ROOT / "specs"
REQUIRED = [
    "README.md",
    "v0.1/00-PRODUCT.md",
    "v0.1/01-ARCHITECTURE.md",
    "v0.1/02-BACKEND-SPIKE.md",
    "v0.1/03-CHAT-AND-AI.md",
    "v0.1/04-MISSIONS-AND-SKILLS.md",
    "v0.1/05-PERSISTENCE-CONFIG-SECURITY.md",
    "v0.1/06-TEST-PLAN.md",
    "v0.1/07-DELIVERY-PLAN.md",
    "v0.1/08-DECISIONS.md",
    "v0.1/schemas/intent-result.schema.json",
    "v0.1/schemas/mission-snapshot.schema.json",
]

errors = []
for name in REQUIRED:
    file = SPECS / name
    if not file.is_file():
        errors.append(f"Missing spec: {file.relative_to(ROOT)}")

for file in SPECS.rglob("*.schema.json"):
    try:
        value = json.loads(file.read_text(encoding="utf-8"))
        if not isinstance(value, dict) or value.get("type") != "object":
            errors.append(f"Invalid JSON Schema object: {file.relative_to(ROOT)}")
    except (OSError, json.JSONDecodeError) as exc:
        errors.append(f"Invalid schema JSON {file.relative_to(ROOT)}: {exc}")

# Check basic links in specs Markdown. Ignore external URLs, anchors, and dynamic links.
# Reference-style and URL-escaped fragments are deliberately not fully validated.
link_re = re.compile(r"\[[^\]]+\]\(([^)]+)\)")
for file in SPECS.rglob("*.md"):
    text = file.read_text(encoding="utf-8")
    for dest in link_re.findall(text):
        dest = dest.split("#", 1)[0].split("?", 1)[0].strip("<>")
        if not dest or "://" in dest or dest.startswith(("mailto:", "#")):
            continue
        dest = unquote(dest)
        resolved = (file.parent / dest).resolve()
        if not resolved.is_relative_to(ROOT.resolve()) or not resolved.exists():
            errors.append(f"Broken spec link: {file.relative_to(ROOT)} -> {dest}")

if errors:
    print("Spec validation FAILED:")
    for item in errors:
        print(" -", item)
    sys.exit(1)

print(f"Validated {len(REQUIRED)} required spec files and all local Markdown links under specs/.")
