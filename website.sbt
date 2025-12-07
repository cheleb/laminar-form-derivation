addCommandAlias("website", "docs/mdoc; makeSite")

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
  "laminar-form-derivation-docs/target/mdoc",
  "-project",
  "Laminar Form Derivation",
  "-groups",
  "-project-version",
  sys.env.getOrElse("VERSION", version.value),
  "-revision",
  version.value,
  // "-default-templates",
  // "static-site-main",
  "-project-footer",
  s"Copyright (c) 2022-$currentYear, Olivier NOUGUIER",
  // custom::https://www.linkedin.com/in/olivier-nouguier::linkedinday.png::linkedinnight.png
  "-social-links:github::https://github.com/cheleb/laminar-form-derivation,twitter::https://twitter.com/oNouguier",
  "-Ygenerate-inkuire",
  "-skip-by-regex:facades\\..*",
  "-skip-by-regex:samples\\..*",
  "-skip-by-regex:html\\..*",
  "-snippet-compiler:compile"
)
