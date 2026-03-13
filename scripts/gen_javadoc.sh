#!/usr/bin/env bash
# This script was updated with assistance from OpenAI, ChatGPT,
# "Updating a Bash script to generate Android Javadocs using Gradle", 2026-03-13.
set -e

# Go to repo root
cd "$(dirname "$0")/.."

DOC_PATH="code/app/build/reports/javadoc/index.html"

# Open existing docs only
if [ "$1" = "-o" ]; then
  open "$DOC_PATH"
  exit 0
fi

# Generate Javadoc using the Gradle task in code/app/build.gradle.kts
(
  cd code
  ./gradlew generateJavadoc
)

# Open generated docs
open "$DOC_PATH"
