npmRefresh=3600
filename=examples/client/node_modules/.package-lock.json
age=$(($(date +%s) - $(stat -t %s -f %m -- "$filename")))
if [ ! -f "$filename" ] || [ $age -gt $npmRefresh ]; then
    echo "Reinstalling npm dependencies..."
    cd examples/client
    npm i
    cd ../..
else
    echo "Skipping npm install... $filename is less than an $npmRefresh seconds old ($age)."
fi
