filename=examples/client/target/scala-3.4.1/client-fastopt/main.js
echo "Compiling client-fastopt/main.js..."
sleep 3
until [ -e $filename ]; do
    echo "Waiting for client-fastopt/main.js to be compiled..."
    sleep 2
done

DEV=1 sbt '~client/fastLinkJS'
