name := "pg"

version := "0.0.15"

scalaVersion := "3.1.3"

enablePlugins(ScalaNativePlugin)

nativeLinkStubs := true

nativeMode := "debug"

nativeLinkingOptions := Seq(s"-L${baseDirectory.value}/native-lib")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials",
)

organization := "io.github.spritzsn"

githubOwner := "spritzsn"

githubRepository := name.value

Global / onChangedBuildSource := ReloadOnSourceChanges

resolvers += Resolver.githubPackages("edadma")

licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/spritzsn/" + name.value))

//libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.13" % "test"

libraryDependencies ++= Seq(
  "io.github.spritzsn" %%% "async" % "0.0.9",
)

libraryDependencies ++= Seq(
  "io.github.edadma" %%% "libpq" % "0.0.5",
  "io.github.edadma" %%% "table" % "1.0.3",
)

libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.4.0"

publishMavenStyle := true

Test / publishArtifact := false
