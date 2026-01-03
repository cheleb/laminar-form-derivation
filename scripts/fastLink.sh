#!/usr/bin/env bash
#-- DO NOT EDIT: This file is managed by sbt-fullstack-js plugin --

if [ -e ./scripts/target/build-env.sh ]; then
 . ./scripts/target/build-env.sh
else
 echo "Error: build-env.sh not found. Please run ./scripts/setup.sc first."
 exit 1
fi

echo -n "Waiting for npm dev server to start."

until [ -e $NPM_DEV_PATH ]; do
    echo -n "."
    sleep 2
done

echo "  ✅"
echo "NPM dev server started."
echo "Waiting for client-fastopt/main.js to be generated."

until [ -e $MAIN_JS_PATH ]; do
    echo -n "."
    sleep 2
done
echo "  ✅"
echo "⏱️ Watching client-fastopt/main.js for changes..."

sbt --batch -Dsbt.supershell=false '~client/fastLinkJS'
