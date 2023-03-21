//import ProjectDef._
import org.scalajs.linker.interface.ModuleSplitStyle
val scala3 = "3.2.2"

inThisBuild(
  List(
    scalaVersion := scala3,
    organization := "dev.cheleb",
    homepage := Some(url("https://github.com/cheleb/")),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
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
    sharedJvm,
    example
  )
  .settings(
    publish / skip := true
  )

lazy val server = project
  .in(file("example/server"))
  .enablePlugins(serverPlugins: _*)
  .settings(
    cancelable := true,
    fork := true,
    scalaJSProjects := Seq(example),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "0.0.5"
    )
  )
  .settings(serverSettings: _*)
  .dependsOn(sharedJvm, core)
  .settings(
    publish / skip := true
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
    name := "laminar-form-derivation",
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
      "com.softwaremill.magnolia1_3" %%% "magnolia" % "1.3.0",
      "com.raquo" %%% "laminar" % "15.0.0",
      "io.laminext" %%% "websocket" % "0.14.4",
      "be.doeraene" %%% "web-components-ui5" % "1.9.2",
      "io.github.iltotore" %%% "iron-zio-json" % "2.0.0"
    )
  )
//  .dependsOn(sharedJs)

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
  .dependsOn(core, sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("example/shared"))
  .settings(
    publish / skip := true
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
