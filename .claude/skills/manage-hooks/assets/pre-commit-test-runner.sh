#!/bin/bash
#
# Pre-commit hook for running fast unit tests
# Runs before every commit
#
# Installation: /manage-hooks install test-runner-commit
# Bypass: git commit --no-verify
#

set -e

echo ""
echo "🧪 Running unit tests..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Run fast unit tests only
# Adjust command based on your test framework:
# - Jest: npm test -- --testPathIgnorePatterns=integration
# - Vitest: npm run test:unit
# - pytest: pytest tests/unit/

if ! claude code /test 2>&1 | tail -20; then
  echo ""
  echo "❌ Tests failed"
  echo "💡 Fix: Run '/test' to see details"
  echo ""
  exit 1
fi

echo ""
echo "✅ All tests passed"
echo ""

exit 0
