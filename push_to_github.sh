#!/bin/bash
# Run from Termux to push SubLingo to GitHub

REPO_NAME="sublingo-app"
GITHUB_USER="elephantcos-cloud"

echo "==============================="
echo "  SubLingo - GitHub Push Script"
echo "==============================="

# Initialize git
git init
git add .
git commit -m "Initial commit: SubLingo subtitle translator app"

# Set remote
git remote add origin https://github.com/$GITHUB_USER/$REPO_NAME.git 2>/dev/null || git remote set-url origin https://github.com/$GITHUB_USER/$REPO_NAME.git

# Push
git branch -M main
git push -u origin main

echo ""
echo "Push complete! GitHub Actions will now build the APK."
echo "Check: https://github.com/$GITHUB_USER/$REPO_NAME/actions"
