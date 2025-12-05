#!/usr/bin/env -S scala-cli -S 3

//> using scala "3.8.0-RC3"
//> using javaOptions "--sun-misc-unsafe-memory-access=allow" // Example option to set maximum heap size
//> using dep "com.lihaoyi::os-lib:0.11.6"

import os.*

println("Setup script running for Scala 3.8.0-RC3")

val exampleClient = os.pwd / "examples" / "client"
val nodeModule = exampleClient / "node_modules" / ".package-lock.json"

if (os.exists(nodeModule)) {
  println(s"Node modules already installed at: $nodeModule")
} else {
  println("Installing node modules...")
  os.proc("npm", "install").call(cwd = exampleClient)
  println("Node modules installation complete.")
}
