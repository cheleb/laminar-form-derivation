#!/usr/bin/env -S scala-cli --scala-version 3.8.0-RC5
//-- DO NOT EDIT: This file is managed by sbt-fullstack-js plugin --

// using javaOptions "--sun-misc-unsafe-memory-access=allow" // Example option to set maximum heap size
//> using dep "com.lihaoyi::os-lib:0.11.6"

import os.*

// First we remove started marked semaphor file.
// This allows to avoid confict when many (3) instances of sbt starts in the same time in the same folder:
//
// - server
// - vite init
// - fastLink
//
removeStartedMarker()

//
//Expected the project id of the ScalaJs application.
//
val app = args.headOption.getOrElse("client")

given client: Path = os.pwd / "examples" / app

if buildSbt isYoungerThan buildEnv then
  println(s"Importing project settings into build-env.sh ($buildEnv)...")
  os.proc("sbt", "projects")
    .call(
      cwd = os.pwd,
      env = Map("INIT" -> "setup"),
      stdout = os.ProcessOutput.Readlines(line => println(s"  $line"))
    )

npmCommand foreach: command =>
  println(s"✨ Installing ($command) node modules...")
  os.proc("npm", command).call(cwd = client)
  println("Node modules installation complete.")

// Utils && Helpers
def npmCommand(using client: Path): Option[String] =
  if packageLockJson.isMissing then
    println("✨	- First install")
    Some("install")
  else if packageJson isYoungerThan packageLockJson then
    println("⏫	- package.json has been modified since the last installation.")
    Some("install")
  else if nodeModule.isOlderThanAWeek then {
    print(s"\t- 🔎 Node modules already installed but old")
    println("\n\t\t- ⚠️\t Not installed recently ( > 7 days). Consider reinstalling if issues arise.")
    None
  } else if nodeModule.isMissing then {
    println("🟢 CI")
    Some("ci")
  } else {
    println("\t- ✅ npm are deps uptodate.")
    None
  }

def buildSbt                            = os.pwd / "build.sbt"
def buildEnv                            = os.pwd / "scripts" / "target" / "build-env.sh"
def devMarker                           = os.pwd / "target" / "dev-server-running.marker"
def npmDevMarker                        = os.pwd / "target" / "npm-dev-server-running.marker"
def nodeModule(using client: Path)      = client / "node_modules" / ".package-lock.json"
def packageJson(using client: Path)     = client / "package.json"
def packageLockJson(using client: Path) = client / "package-lock.json"

/** Delete semaphore files to sync multiple sbt launchs.
  */
def removeStartedMarker() =
  if os.exists(devMarker) then os.remove(devMarker)
  if os.exists(npmDevMarker) then os.remove(npmDevMarker)

import scala.math.Ordered.orderingToOrdered

extension (path: Path)
  /** True if something must be reprocessed.
    */
  infix def isYoungerThan(that: Path) =
    if os.exists(that) then
      os.stat(path).mtime > os.stat(that).mtime
    else true

  def exists: Boolean = os.exists(path)

  def isMissing: Boolean = !os.exists(path)

  def isOlderThanAWeek: Boolean =
    os.exists(path) && os.stat(path).mtime.toInstant < java.time.Instant.now().minus(
      7,
      java.time.temporal.ChronoUnit.DAYS
    )
