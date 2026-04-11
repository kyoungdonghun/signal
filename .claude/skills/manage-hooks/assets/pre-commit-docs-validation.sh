#!/bin/bash
#
# Pre-commit hook for documentation integrity (OPTIMIZED)
# Runs when docs/ files are changed
#
# Performance: ~0.05 seconds (경량 Python 스크립트 사용)
# Installation: /manage-hooks install docs-validation
# Bypass: git commit --no-verify
#

set -e

# Check if docs/ directory has changes
if ! git diff --cached --name-only | grep -q '^docs/'; then
  exit 0
fi

echo ""
echo "📚 Documentation changes detected"

# Run quick validation (optimized Python script)
if ! python3 .claude/scripts/quick_validate.py; then
  echo ""
  exit 1
fi

exit 0
