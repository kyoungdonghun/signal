#!/usr/bin/env python3
"""
Lint all Markdown, JSON, and Python files in the project.
"""

import subprocess
import sys
from pathlib import Path

def run_command(cmd, description):
    """Run a command and return success status and output."""
    print(f"\n{'='*60}")
    print(f"Checking {description}...")
    print(f"{'='*60}")

    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            shell=isinstance(cmd, str)
        )

        if result.returncode == 0:
            print(f"✓ {description}: PASSED")
            return True, result.stdout
        else:
            print(f"✗ {description}: FAILED")
            if result.stdout:
                print(result.stdout)
            if result.stderr:
                print(result.stderr)
            return False, result.stderr

    except FileNotFoundError:
        print(f"⚠ Tool not found for {description}")
        return None, f"Tool not installed"

def main():
    project_root = Path(__file__).parent.parent.parent.parent.parent
    print(f"Project root: {project_root}")

    results = {}

    # Markdown lint
    md_result, md_output = run_command(
        f"markdownlint '**/*.md' --ignore node_modules",
        "Markdown files"
    )
    results['markdown'] = (md_result, md_output)

    # JSON lint
    json_files = list(project_root.rglob("*.json"))
    json_passed = True
    json_errors = []

    for json_file in json_files:
        if 'node_modules' in str(json_file):
            continue
        jq_result, jq_output = run_command(
            f"jq empty {json_file}",
            f"JSON: {json_file.name}"
        )
        if jq_result is False:
            json_passed = False
            json_errors.append(f"{json_file}: {jq_output}")

    results['json'] = (json_passed, '\n'.join(json_errors) if json_errors else "All JSON files valid")

    # Python lint
    py_result, py_output = run_command(
        "ruff check .",
        "Python files"
    )
    results['python'] = (py_result, py_output)

    # Summary
    print(f"\n{'='*60}")
    print("SUMMARY")
    print(f"{'='*60}")

    all_passed = all(r[0] is True for r in results.values())

    for name, (passed, output) in results.items():
        status = "✓ PASSED" if passed else ("✗ FAILED" if passed is False else "⚠ SKIPPED")
        print(f"{name.capitalize()}: {status}")

    if all_passed:
        print("\n✓ All lint checks passed!")
        sys.exit(0)
    else:
        print("\n✗ Some lint checks failed. See details above.")
        sys.exit(1)

if __name__ == "__main__":
    main()
