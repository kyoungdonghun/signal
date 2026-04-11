#!/bin/bash
#
# Commit-msg hook for validating commit message format
# Runs after commit message is entered
#
# Installation: /manage-hooks install commit-msg-format
# Bypass: git commit --no-verify
#

COMMIT_MSG_FILE=$1
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

echo ""
echo "📝 Validating commit message format..."

# Skip merge commits
if echo "$COMMIT_MSG" | grep -qE "^Merge (branch|pull request)"; then
  echo "✅ Merge commit - skipping validation"
  exit 0
fi

# Get first line (subject)
SUBJECT=$(echo "$COMMIT_MSG" | head -n 1)

# Check subject line length (recommended < 72 characters)
SUBJECT_LENGTH=${#SUBJECT}
if [ $SUBJECT_LENGTH -gt 72 ]; then
  echo "❌ Subject line too long: $SUBJECT_LENGTH characters (max 72)"
  echo "   Subject: $SUBJECT"
  echo ""
  exit 1
fi

# Check if subject line is empty
if [ -z "$SUBJECT" ]; then
  echo "❌ Commit message cannot be empty"
  echo ""
  exit 1
fi

# Optional: Enforce conventional commit format
# Uncomment to enable: type(scope): subject
#
# if ! echo "$SUBJECT" | grep -qE "^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert)(\(.+\))?: .+"; then
#   echo "❌ Subject line must follow format: type(scope): subject"
#   echo "   Valid types: feat, fix, docs, style, refactor, test, chore, perf, ci, build, revert"
#   echo "   Example: feat(auth): add login validation"
#   echo ""
#   echo "   Your subject: $SUBJECT"
#   echo ""
#   exit 1
# fi

echo "✅ Commit message format valid"
echo ""

exit 0
