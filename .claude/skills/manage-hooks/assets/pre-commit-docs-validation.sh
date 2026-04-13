#!/bin/bash
#
# Pre-commit hook for documentation integrity
# Runs when docs/ files are changed
#
# Performance: lightweight Python validation
# Installation: /manage-hooks install docs-validation
# Bypass: git commit --no-verify
#

set -e

# Check if docs/ directory has changes
if ! git diff --cached --name-only | grep -q '^docs/'; then
  exit 0
fi

echo ""
echo "Documentation changes detected"

# Run documentation validation against the current SIGNAL docs baseline
if ! python3 .claude/skills/validate-docs/scripts/validate_docs.py; then
  echo ""
  exit 1
fi

exit 0
