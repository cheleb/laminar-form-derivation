#!/usr/bin/env bash
#-- DO NOT EDIT: This file is managed by sbt-fullstack-js plugin --

if [ -e ./scripts/target/build-env.sh ]; then
 . ./scripts/target/build-env.sh
else
 echo "Error: build-env.sh not found. Please run ./scripts/setup.sc first."
 exit 1
fi

echo -n "Waiting for dev server to start."

until [ -e $SERVER_DEV_PATH ]; do
    echo -n "."
    sleep 2
done

echo ✅

echo "Starting npm dev server for client"
echo " * SCALA_VERSION=$SCALA_VERSION"
rm -f $MAIN_JS_PATH
touch $NPM_DEV_PATH

cd examples/client
npm run dev
