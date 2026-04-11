#!/usr/bin/env python3
"""
Validate documentation consistency and integrity.
"""

import re
import sys
from pathlib import Path
from collections import defaultdict

class DocValidator:
    def __init__(self, project_root):
        self.project_root = Path(project_root)
        self.docs_dir = self.project_root / "docs"
        self.errors = []
        self.warnings = []

    def validate_tier0_docs(self):
        """Check if all Tier 0 documents exist."""
        print("\n" + "="*60)
        print("Checking Tier 0 Documents...")
        print("="*60)

        required_docs = [
            "docs/standards/core-principles.md",
            "docs/standards/documentation-standards.md",
            "docs/standards/development-standards.md",
            "docs/standards/prompt-caching-strategy.md"
        ]

        missing = []
        for doc_path in required_docs:
            full_path = self.project_root / doc_path
            if not full_path.exists():
                missing.append(doc_path)

        if missing:
            self.errors.append(f"Missing Tier 0 documents: {', '.join(missing)}")
            print(f"✗ FAILED: {len(missing)} documents missing")
            for doc in missing:
                print(f"  - {doc}")
            return False
        else:
            print("✓ PASSED: All Tier 0 documents exist")
            return True

    def validate_internal_links(self):
        """Check for broken internal links in markdown files."""
        print("\n" + "="*60)
        print("Checking Internal Links...")
        print("="*60)

        md_files = list(self.project_root.rglob("*.md"))
        broken_links = []

        # Pattern to match markdown links: [text](path)
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
            print(f"✗ FAILED: {len(broken_links)} broken links found")
            for link in broken_links[:5]:  # Show first 5
                print(f"  - {link}")
            if len(broken_links) > 5:
                print(f"  ... and {len(broken_links) - 5} more")
            return False
        else:
            print("✓ PASSED: No broken internal links")
            return True

    def validate_traceability_ids(self):
        """Check traceability ID format and duplicates."""
        print("\n" + "="*60)
        print("Checking Traceability IDs...")
        print("="*60)

        # Patterns for different ID types
        patterns = {
            'REQ': re.compile(r'REQ-\d{8}-\d{3}'),
            'WI': re.compile(r'WI-\d{3}'),
            'STD': re.compile(r'STD-\d{3}')
        }

        id_locations = defaultdict(list)
        md_files = list(self.project_root.rglob("*.md"))

        for md_file in md_files:
            if 'node_modules' in str(md_file):
                continue

            try:
                content = md_file.read_text(encoding='utf-8')
                for id_type, pattern in patterns.items():
                    matches = pattern.findall(content)
                    for match in matches:
                        id_locations[match].append(str(md_file.relative_to(self.project_root)))

            except Exception as e:
                self.warnings.append(f"Could not read {md_file}: {e}")

        # Check for duplicates
        duplicates = {id_: locs for id_, locs in id_locations.items() if len(set(locs)) > 1}

        if duplicates:
            self.errors.append(f"Duplicate traceability IDs found: {len(duplicates)}")
            print(f"✗ FAILED: {len(duplicates)} duplicate IDs")
            for id_, locs in list(duplicates.items())[:3]:
                print(f"  - {id_} found in: {', '.join(set(locs))}")
            return False
        else:
            print(f"✓ PASSED: {len(id_locations)} unique IDs, no duplicates")
            return True

    def validate_document_index(self):
        """Check if docs/index.md exists and lists all docs."""
        print("\n" + "="*60)
        print("Checking Document Index...")
        print("="*60)

        index_path = self.docs_dir / "index.md"
        if not index_path.exists():
            self.warnings.append("docs/index.md not found")
            print("⚠ WARNING: No document index found")
            return True

        try:
            index_content = index_path.read_text(encoding='utf-8')
            md_files = [f for f in self.docs_dir.rglob("*.md") if f != index_path]

            orphaned = []
            for md_file in md_files:
                rel_path = str(md_file.relative_to(self.project_root))
                if rel_path not in index_content and md_file.name != "index.md":
                    orphaned.append(rel_path)

            if orphaned:
                self.warnings.extend(orphaned)
                print(f"⚠ WARNING: {len(orphaned)} orphaned documents")
                for doc in orphaned[:3]:
                    print(f"  - {doc}")
                if len(orphaned) > 3:
                    print(f"  ... and {len(orphaned) - 3} more")
            else:
                print("✓ PASSED: All documents listed in index")

        except Exception as e:
            self.warnings.append(f"Could not validate index: {e}")

        return True

    def run(self):
        """Run all validations."""
        print("="*60)
        print("DOCUMENT VALIDATION")
        print(f"Project root: {self.project_root}")
        print("="*60)

        results = []
        results.append(self.validate_tier0_docs())
        results.append(self.validate_internal_links())
        results.append(self.validate_traceability_ids())
        results.append(self.validate_document_index())

        # Summary
        print("\n" + "="*60)
        print("SUMMARY")
        print("="*60)

        if self.errors:
            print(f"✗ FAILED: {len(self.errors)} errors")
            return 1
        elif self.warnings:
            print(f"⚠ WARNINGS: {len(self.warnings)} warnings")
            return 2
        else:
            print("✓ All validations passed!")
            return 0

def main():
    project_root = Path(__file__).parent.parent.parent.parent.parent
    validator = DocValidator(project_root)
    exit_code = validator.run()
    sys.exit(exit_code)

if __name__ == "__main__":
    main()
