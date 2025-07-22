import java.nio.charset.StandardCharsets
import org.scalajs.linker.interface.ModuleSplitStyle

val scala3 = "3.3.6"

val ironVersion = "3.0.2"

val tapirVersion = "1.11.38"

val laminarVersion = "17.2.1"

inThisBuild(
  List(
    scalaVersion := scala3,
    organization := "dev.cheleb",
    homepage := Some(url("https://github.com/cheleb/")),
    publishTo := {
      val centralSnapshots =
        "https://central.sonatype.com/repository/maven-snapshots/"
      if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
      else localStaging.value
    },
    versionScheme := Some("early-semver"),
    // pgpPublicRing := file("/tmp/public.asc"),
    // pgpSecretRing := file("/tmp/secret.asc"),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/cheleb/laminar-form-derivation/"),
        "scm:git:git@github.com:cheleb/laminar-form-derivation.git"
      )
    ),
    developers := List(
      Developer(
        "cheleb",
        "Olivier NOUGUIER",
        "olivier.nouguier@gmail.com",
        url("https://github.com/cheleb")
      )
    ),
    startYear := Some(2023),
    licenses += (
      "Apache-2.0",
      url(
        "http://www.apache.org/licenses/LICENSE-2.0"
      )
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Xfatal-warnings"
    )
  )
)

lazy val generator = project
  .in(file("examples/generator"))
  .enablePlugins(SbtTwirl)
  .settings(
    libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0",
    libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.4",
    libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.17"
  )
  .settings(
    publish / skip := true
  )

val dev = sys.env.get("DEV").getOrElse("demo")

val serverPlugins = dev match {
  case "prod" =>
    Seq(SbtWeb, SbtTwirl, JavaAppPackaging, WebScalaJSBundlerPlugin)
  case _ => Seq()
}

def scalaJSModule = dev match {
  case "prod" => ModuleKind.CommonJSModule
  case _      => ModuleKind.ESModule
}

val serverSettings = dev match {
  case "prod" =>
    Seq(
      Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
      Assets / WebKeys.packagePrefix := "public/",
      Runtime / managedClasspath += (Assets / packageBin).value
    )
  case _ => Seq()
}

lazy val root = project
  .in(file("."))
  .aggregate(
    generator,
    server,
    core,
    coreSharedJs,
    coreSharedJvm,
    ui5,
    ui5_nguyenyou,
    example
  )
  .settings(
    publish / skip := true
  )

val staticGenerationSettings =
  if (dev == "prod")
    Seq(
      Assets / resourceGenerators += Def
        .taskDyn[Seq[File]] {
          val baseDir = baseDirectory.value
          val rootFolder = (Assets / resourceManaged).value / "public"
          rootFolder.mkdirs()
          (generator / Compile / runMain)
            .toTask {
              Seq(
                "samples.BuildIndex",
                "--title",
                s""""Laminar Form Derivation v ${version.value}"""",
                "--resource-managed",
                rootFolder
              ).mkString(" ", " ", "")
            }
            .map(_ => (rootFolder ** "*.html").get)
        }
        .taskValue
    )
  else
    Seq()

lazy val server = project
  .in(file("examples/server"))
  .enablePlugins(serverPlugins: _*)
  .settings(
    staticGenerationSettings
  )
  .settings(
    fork := true,
    scalaJSProjects := Seq(example),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    libraryDependencies ++= Seq(
      "io.github.iltotore" %% "iron-zio-json" % ironVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % "test"
    )
  )
  .settings(serverSettings: _*)
  .dependsOn(exampleSharedJvm, core)
  .settings(
    publish / skip := true
  )

val usedScalacOptions = Seq(
  "-encoding",
  "utf8",
  "-unchecked",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xmax-inlines:64",
  "-Wunused:all"
)
lazy val core = scalajsProject("core", false)
  .dependsOn(coreSharedJs)
  .settings(
    name := "laminar-form-derivation",
    //  scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(scalaJSModule)
        .withSourceMap(false)
        .withModuleSplitStyle(ModuleSplitStyle.SmallestModules)
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
      "com.softwaremill.magnolia1_3" %%% "magnolia" % "1.3.18",
      "com.raquo" %%% "laminar" % laminarVersion,
      // "io.laminext" %%% "websocket" % laminarVersion,
      "io.github.iltotore" %%% "iron" % ironVersion
    )
  )

lazy val coreShared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/shared"))
  .settings(
    name := "laminar-form-derivation-shared"
  )
lazy val coreSharedJvm = coreShared.jvm
lazy val coreSharedJs = coreShared.js

lazy val ui5 = scalajsProject("ui5", false)
  .settings(
    name := "laminar-form-derivation-ui5",
    //   scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(scalaJSModule)
        .withSourceMap(false)
        .withModuleSplitStyle(ModuleSplitStyle.SmallestModules)
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      "be.doeraene" %%% "web-components-ui5" % "2.0.0-RC2"
    )
  )

lazy val ui5_nguyenyou = scalajsProject("ui5-nguyenyou", false)
  .settings(
    name := "laminar-form-derivation-ui5-nguyenyou",
    //   scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(scalaJSModule)
        .withSourceMap(false)
        .withModuleSplitStyle(ModuleSplitStyle.SmallestModules)
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.nguyenyou" %%% "ui5-webcomponents-laminar" % "2.9.0"
    )
  )

lazy val example = scalajsProject("client", true)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      dev match {
        case "prod" =>
          config.withModuleKind(scalaJSModule)
        case _ =>
          config
            .withModuleKind(scalaJSModule)
            .withSourceMap(false)
            .withModuleSplitStyle(ModuleSplitStyle.SmallestModules)
      }
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .dependsOn(ui5, ui5_nguyenyou, exampleSharedJs)
  .settings(
    publish / skip := true,
    scalacOptions -= "-Xfatal-warnings" // disable fatal warnings due to spurious https://github.com/scala/scala3/issues/20741
  )

lazy val exampleShared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("examples/shared"))
  .settings(
    publish / skip := true
  )
lazy val exampleSharedJvm = exampleShared.jvm
lazy val exampleSharedJs = exampleShared.js

//Global / cancelable := true
//Global / fork := true
Test / fork := false
// loads the server project at sbt startup
//onLoad in Global := (onLoad in Global).value.andThen(state => "project server" :: state)

def nexusNpmSettings =
  sys.env
    .get("NEXUS")
    .map(url =>
      npmExtraArgs ++= Seq(
        s"--registry=$url/repository/npm-public/"
      )
    )
    .toSeq

def scalaJSPlugin = dev match {
  case "prod" => ScalaJSBundlerPlugin
  case _      => ScalaJSPlugin
}

def scalajsProject(projectId: String, sample: Boolean): Project =
  Project(
    id = projectId,
    base = file(s"${if (sample) "examples" else "modules"}/$projectId")
  )
    .enablePlugins(scalaJSPlugin)
    .settings(nexusNpmSettings)
    .settings(Test / requireJsDomEnv := true)
    .settings(
      scalacOptions := Seq(
        "-scalajs",
        "-deprecation",
        "-feature",
        "-Xfatal-warnings"
      )
    )

Global / onLoad := {
  val scalaVersionValue = (example / scalaVersion).value
  val outputFile =
    baseDirectory.value / "scripts" / "target" / "build-env.sh"
  IO.writeLines(
    outputFile,
    s"""  
  |# Generated file see build.sbt
  |SCALA_VERSION="$scalaVersionValue"
  |""".stripMargin.split("\n").toList,
    StandardCharsets.UTF_8
  )

  (Global / onLoad).value
}
