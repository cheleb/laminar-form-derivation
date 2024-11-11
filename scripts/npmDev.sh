#!/usr/bin/env bash

. ./scripts/env.sh

if [ ! -e $BUILD_ENV_FILE ]; then
    echo "Waiting for $BUILD_ENV_FILE to be generated..."
    echo '  Import the project !!!'
    echo

    until [ -e $BUILD_ENV_FILE ]; do
        echo -n "."
        sleep 4
    done

    echo
    echo
    echo " Good job 🚀"
    echo

fi
. $BUILD_ENV_FILE

echo "Starting npm dev server for client"
echo " * SCALA_VERSION=$SCALA_VERSION"
rm -f examples/client/target/scala-$SCALA_VERSION/client-fastopt/main.js
touch $NPM_DEV_STARTED
cd examples/client
npm run dev
