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
echo "Running unit tests..."
echo "----------------------------------------"

# Run the current backend automated test baseline
if ! ./gradlew test; then
  echo ""
  echo "Tests failed"
  echo "Fix: Run './gradlew test' to see details"
  echo ""
  exit 1
fi

echo ""
echo "All tests passed"
echo ""

exit 0
