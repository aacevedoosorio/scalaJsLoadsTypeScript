name := "scalaJsTest"

version := "0.1"

scalaVersion := "2.12.6"

enablePlugins(ScalaJSBundlerPlugin)

// This is an application with a main method
//scalaJSUseMainModuleInitializer := false

// Configuration on how to bundle the code, as an app or just a library
webpackBundlingMode := BundlingMode.LibraryAndApplication()

npmDependencies in Compile += "ts-loader" -> "^2.0.3"
npmDevDependencies in Compile += "webpack-merge" -> "4.1.2"

libraryDependencies ++= Seq(
  "com.lihaoyi" %%% "fastparse" % "1.0.0"
)

webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.config.js")
webpackConfigFile in fullOptJS := Some(baseDirectory.value / "prod.config.js")

skip in packageJSDependencies := false

jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()

scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

scalacOptions += "-P:scalajs:sjsDefinedByDefault"
