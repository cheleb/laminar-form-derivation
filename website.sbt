lazy val currentYear: String =
  java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString

enablePlugins(
  SiteScaladocPlugin,
  SitePreviewPlugin,
  ScalaUnidocPlugin,
  GhpagesPlugin
)

ScalaUnidoc / siteSubdirName := ""
addMappingsToSiteDir(
  ScalaUnidoc / packageDoc / mappings,
  ScalaUnidoc / siteSubdirName
)
git.remoteRepo := "git@github.com:cheleb/laminar-form-derivation.git"
ghpagesNoJekyll := true
Compile / doc / scalacOptions ++= Seq(
  "-siteroot",
  "docs",
  "-project",
  "Laminar Form Derivation",
  "-groups",
  "-project-version",
  version.value,
  "-revision",
  version.value,
  "-default-templates",
  "static-site-main",
  "-project-footer",
  s"Copyright (c) 2022-$currentYear, Olivier NOUGUIER",
  "-Ygenerate-inkuire",
  "-skip-by-regex:samples\\..*",
  "-snippet-compiler:compile"
)
