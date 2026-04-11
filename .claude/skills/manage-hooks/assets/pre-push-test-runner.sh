#!/bin/bash
#
# Pre-push hook for running full test suite
# Runs before pushing to remote
#
# Installation: /manage-hooks install test-runner-push
# Bypass: git push --no-verify
#

set -e

echo ""
echo "🧪 Running full test suite..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Run full test suite including integration tests
if ! claude code /test 2>&1 | tail -20; then
  echo ""
  echo "❌ Tests failed"
  echo "💡 Fix: Run '/test' to see details"
  echo ""
  exit 1
fi

# Optional: Check test coverage
echo ""
echo "📊 Checking test coverage..."
if ! claude code /test-coverage 2>&1 | grep -E "Coverage|%"; then
  echo ""
  echo "⚠️  Coverage check failed (continuing anyway)"
fi

echo ""
echo "✅ All tests passed"
echo ""

exit 0
