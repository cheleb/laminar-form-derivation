#!/usr/bin/env bash

. ./scripts/env.sh

rm -f $NPM_DEV_STARTED

filename_lock=node_modules/.package-lock.json

cd examples/client

# Define get_mtime function based on OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    get_mtime() { stat -f %m "$1"; }
else
    # Linux and others
    get_mtime() { stat -c %Y "$1"; }
fi

if [ ! -f "$filename_lock" ]; then
    echo "First time setup: Installing npm dependencies..."
    npm i
else
    npmRefresh=3600
    filename=package.json
    age=$(($(date +%s) - $(get_mtime "$filename")))
    age_lock=$(($(date +%s) - $(get_mtime "$filename_lock")))
    if [ $age_lock -gt $age ]; then
        echo "Reinstalling npm dependencies..."
        npm i
        cd ../..
    else
        echo "Skipping npm install... $filename_lock is less than an $npmRefresh seconds old ($age)."
    fi
fi
