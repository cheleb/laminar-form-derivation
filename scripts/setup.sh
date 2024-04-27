. ./scripts/env.sh

rm -f $NPM_DEV_STARTED

npmRefresh=3600
filename=examples/client/package.json
filename_lock=examples/client/node_modules/.package-lock.json
age=$(($(date +%s) - $(stat -t %s -f %m -- "$filename")))
age_lock=$(($(date +%s) - $(stat -t %s -f %m -- "$filename_lock")))
if [ ! -f "$filename_lock" ] || [ $age_lock -gt $npmRefresh ] || [ $age_lock -gt $age ]; then
    echo "Reinstalling npm dependencies..."
    if [ $age_lock -gt $age ]; then
        echo "(New dependencies)"
    else
        echo "(Refreshing dependencies: package-lock.json is older than $npmRefresh seconds ($age_lock).)"
    fi
    cd examples/client
    npm i
    cd ../..
else
    echo "Skipping npm install... $filename_lock is less than an $npmRefresh seconds old ($age)."
fi
