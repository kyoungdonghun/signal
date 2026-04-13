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
echo "Running full test suite..."
echo "----------------------------------------"

# Run backend tests
if ! ./gradlew test; then
  echo ""
  echo "Backend tests failed"
  echo "Fix: Run './gradlew test' to see details"
  echo ""
  exit 1
fi

# Run frontend production build
echo ""
echo "Running frontend build..."
if ! (cd frontend && npm run build); then
  echo ""
  echo "Frontend build failed"
  echo "Fix: Run 'cd frontend && npm run build' to see details"
  echo ""
  exit 1
fi

echo ""
echo "Backend tests and frontend build passed"
echo ""

exit 0
