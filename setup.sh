#!/bin/bash
# Run this once to setup gradle wrapper
echo "Setting up Gradle wrapper..."
gradle wrapper --gradle-version=8.6 --distribution-type=bin
chmod +x gradlew
echo "Done! Now run: ./gradlew assembleDebug"
