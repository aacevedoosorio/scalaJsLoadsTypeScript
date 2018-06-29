import sbt.io

name := "scalaJsTest"

version := "0.1"

scalaVersion := "2.12.6"

enablePlugins(ScalaJSBundlerPlugin)

// This is an application with a main method
//scalaJSUseMainModuleInitializer := false

// Configuration on how to bundle the code, as an app or just a library
webpackBundlingMode := BundlingMode.LibraryOnly()

npmDependencies in Compile += "ts-loader" -> "^2.0.3"
npmDependencies in Compile += "uuid" -> "3.1.0"
npmDependencies in Compile += "stsTest" -> (baseDirectory.value / "src" / "main" / "resources" / "stsTest").getAbsolutePath
npmDevDependencies in Compile += "webpack-merge" -> "4.1.2"
npmDevDependencies in Compile += "typescript" -> "2.6.1"

libraryDependencies ++= Seq(
  "com.lihaoyi" %%% "fastparse" % "1.0.0",
  "org.scalatest" %%% "scalatest" % "3.0.0" % Test
)

webpackExtraArgs := Seq("--profile", "--progress", "true")

webpackResources := webpackResources.value +++ PathFinder(
  Seq(
    baseDirectory.value / "tsconfig.json",
    //baseDirectory.value / "src" / "main" / "resources" / "stsTest"
    //baseDirectory.value / "src" / "main" / "resources" / "other-module.js"
  )
)

webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.config.js")
webpackConfigFile in fullOptJS := Some(baseDirectory.value / "prod.config.js")

skip in packageJSDependencies := false

//jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()

scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

scalacOptions += "-P:scalajs:sjsDefinedByDefault"
