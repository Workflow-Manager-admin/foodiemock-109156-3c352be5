#!/bin/bash
cd /home/kavia/workspace/code-generation/foodiemock-109156-3c352be5/foodiemock
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

