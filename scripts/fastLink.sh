#!/usr/bin/env bash

. ./scripts/target/build-env.sh

echo "Waiting for npm dev server to start."
sleep 3

until [ -e $MAIN_JS_PATH ]; do
    echo -n "."
    sleep 2
done

echo "Watching client-fastopt/main.js for changes..."


DEV=1 sbt '~client/fastLinkJS'
