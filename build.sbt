//import ProjectDef._
import org.scalajs.linker.interface.ModuleSplitStyle
val scala3 = "3.2.1"

inThisBuild(
  List(
    scalaVersion := scala3,
    organization := "dev.cheleb",
//    githubOwner := "cheleb",
//    githubRepository := "laminar-form-derivation",
    startYear := Some(2022),
    licenses += ("Apache-2.0", url(
      "http://www.apache.org/licenses/LICENSE-2.0"
    )),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Xfatal-warnings"
    )
  )
)

val dev = sys.env.get("DEV").isDefined

val serverPlugins = dev match {
  case true  => Seq()
  case false => Seq(SbtWeb, SbtTwirl, JavaAppPackaging, WebScalaJSBundlerPlugin)
}

val serverSettings = dev match {
  case true => Seq()
  case false =>
    Seq(
      Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
      Assets / WebKeys.packagePrefix := "public/",
      Runtime / managedClasspath += (Assets / packageBin).value
    )
}

lazy val root = project
  .in(file("."))
  .aggregate(
    server,
    core,
    sharedJs,
    sharedJvm
  )
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val server = project
  .in(file("example/server"))
  .enablePlugins(serverPlugins: _*)
  .settings(
    cancelable := true,
    fork := true,
    scalaJSProjects := Seq(example),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
//    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "0.0.3"
    )
//    Assets / WebKeys.packagePrefix := "public/",
//    Runtime / managedClasspath += (Assets / packageBin).value
  )
  .settings(serverSettings: _*)
  .dependsOn(sharedJvm, core)
  .settings(
    publish := {},
    publishLocal := {}
  )

def scalaJSModule = dev match {
  case true  => ModuleKind.ESModule
  case false => ModuleKind.CommonJSModule
}
val usedScalacOptions = Seq(
  "-encoding",
  "utf8",
  "-unchecked",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xmax-inlines:64"
)
lazy val core = scalajsProject("core")
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(scalaJSModule)
        .withSourceMap(true)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("core")))
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.magnolia1_3" %%% "magnolia" % "1.2.5",
      "com.raquo" %%% "laminar" % "0.14.5",
      "io.laminext" %%% "websocket" % "0.14.4",
      "be.doeraene" %%% "web-components-ui5" % "1.9.0"
    )
  )
//  .settings(
//    npmDevDependencies in Compile ++= Seq("vite" -> "^2.9.9")
  // npmDependencies in Compile ++= Seq(
  //   "@ui5/webcomponents" -> "1.8.0",
  //   "@ui5/webcomponents-fiori" -> "1.8.0",
  //   "@ui5/webcomponents-icons" -> "1.8.0"
  // )
//  )
  .settings(
    publish := {},
    publishLocal := {}
  )
  .dependsOn(sharedJs)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val example = scalajsProject("example-client", Some("example/client"))
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(scalaJSModule)
        .withSourceMap(true)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("client")))
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .dependsOn(core)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("example/shared"))
  .settings(
    publish := {},
    publishLocal := {}
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

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
  case true  => ScalaJSPlugin
  case false => ScalaJSBundlerPlugin
}

def scalajsProject(projectId: String, folder: Option[String] = None): Project =
  Project(id = projectId, base = file(folder.getOrElse(projectId)))
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
