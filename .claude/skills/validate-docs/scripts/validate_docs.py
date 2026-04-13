#!/usr/bin/env python3
"""Validate documentation consistency and integrity for the current SIGNAL docs baseline."""

import sys
from pathlib import Path

class DocValidator:
    def __init__(self, project_root):
        self.project_root = Path(project_root)
        self.docs_dir = self.project_root / "docs"
        self.errors = []
        self.warnings = []

    def validate_core_docs(self):
        """Check if all current core project documents exist."""
        print("\n" + "="*60)
        print("Checking Core Project Documents...")
        print("="*60)

        required_docs = [
            "AGENTS.md",
            "CLAUDE.md",
            ".claude/config/PIPELINE.md",
            "docs/meta-layer-charter.md"
        ]

        missing = []
        for doc_path in required_docs:
            full_path = self.project_root / doc_path
            if not full_path.exists():
                missing.append(doc_path)

        if missing:
            self.errors.append(f"Missing core project documents: {', '.join(missing)}")
            print(f"FAILED: {len(missing)} documents missing")
            for doc in missing:
                print(f"  - {doc}")
            return False
        else:
            print("PASSED: All core project documents exist")
            return True

    def validate_internal_links(self):
        """Check for broken internal links in markdown files."""
        print("\n" + "="*60)
        print("Checking Internal Links...")
        print("="*60)

        md_files = list(self.project_root.rglob("*.md"))
        broken_links = []

        import re
        link_pattern = re.compile(r'\[([^\]]+)\]\(([^)]+)\)')

        for md_file in md_files:
            if 'node_modules' in str(md_file):
                continue

            try:
                content = md_file.read_text(encoding='utf-8')
                for line_num, line in enumerate(content.split('\n'), 1):
                    matches = link_pattern.findall(line)
                    for text, link in matches:
                        # Skip external links (http/https)
                        if link.startswith(('http://', 'https://', '#')):
                            continue

                        # Remove anchor if present
                        link_path = link.split('#')[0]
                        if not link_path:
                            continue

                        # Support local markdown links that include absolute paths and optional :line suffix.
                        if re.match(r'^/[A-Za-z]:/', link_path):
                            link_path = link_path[1:]
                        if re.match(r'^[A-Za-z]:/.*:\d+$', link_path):
                            link_path = re.sub(r':\d+$', '', link_path)

                        # Resolve relative path
                        if link_path.startswith('/'):
                            target = self.project_root / link_path.lstrip('/')
                        else:
                            target = (md_file.parent / link_path).resolve()

                        if not target.exists():
                            broken_links.append(
                                f"{md_file.relative_to(self.project_root)}:{line_num} → {link} (file not found)"
                            )

            except Exception as e:
                self.warnings.append(f"Could not read {md_file}: {e}")

        if broken_links:
            self.errors.extend(broken_links)
            print(f"FAILED: {len(broken_links)} broken links found")
            for link in broken_links[:5]:  # Show first 5
                print(f"  - {link}")
            if len(broken_links) > 5:
                print(f"  ... and {len(broken_links) - 5} more")
            return False
        else:
            print("PASSED: No broken internal links")
            return True

    def run(self):
        """Run all validations."""
        print("="*60)
        print("DOCUMENT VALIDATION")
        print(f"Project root: {self.project_root}")
        print("="*60)

        results = []
        results.append(self.validate_core_docs())
        results.append(self.validate_internal_links())

        # Summary
        print("\n" + "="*60)
        print("SUMMARY")
        print("="*60)

        if self.errors:
            print(f"FAILED: {len(self.errors)} errors")
            return 1
        elif self.warnings:
            print(f"WARNINGS: {len(self.warnings)} warnings")
            return 2
        else:
            print("All validations passed!")
            return 0

def main():
    project_root = Path(__file__).parent.parent.parent.parent.parent
    validator = DocValidator(project_root)
    exit_code = validator.run()
    sys.exit(exit_code)

if __name__ == "__main__":
    main()
