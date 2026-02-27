#!/usr/bin/env bash

# We gotta hop out one dir to repo root
cd "$(dirname "%0")/.."

# If -o flag is passed just open the java doc in browser for convenience.
if [ "$1" = "-o" ]; then
  open javadoc/index.html
  exit 0
fi

# Currently only builds javadocs for model and util packages, i'll edit the script as more packages are added.
/Applications/Android\ Studio.app/Contents/jbr/Contents/Home/bin/javadoc \
  -protected -splitindex \
  -d javadoc \
  -sourcepath code/app/src/main/java \
  -subpackages com.example.zephyrevents.model:com.example.zephyrevents.util

# Opens java doc in browser after building.
open javadoc/index.html
