#!/usr/bin/env bash

. ./scripts/env.sh

until [ -e $NPM_DEV_STARTED ]; do
    echo "Waiting for npm dev server to start..."
    sleep 2
done

until [ -e $BUILD_ENV_FILE ]; do
    echo "Waiting for $BUILD_ENV_FILE to be generated..."
    sleep 2
done

. $BUILD_ENV_FILE

filename=examples/client/target/scala-$SCALA_VERSION/client-fastopt/main.js

echo "Waiting for client-fastopt/main.js to be compiled..."
until [ -e $filename ]; do
    sleep 1
    echo -n "."
done

echo "Watching client-fastopt/main.js for changes..."

sleep 3

DEV=1 sbt --client '~client/fastLinkJS'
