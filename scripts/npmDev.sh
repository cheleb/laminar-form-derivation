cd examples/client
rm -f target/scala-3.4.1/client-fastopt/main.js
echo "Compiling client-fastopt/main.js..."
sleep 3
until [ -e target/scala-3.4.1/client-fastopt/main.js ]; do
    echo "Waiting for client-fastopt/main.js to be compiled..."
    sleep 2
done

echo "Starting npm dev server..."

sleep 2

npm run dev
