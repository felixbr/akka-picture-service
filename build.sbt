import sbt.Keys._

val akkaVersion = "2.4.3"

val scrimageVersion = "2.1.0"

val ammoniteVersion = "0.6.0"

val commonSettings = Seq(
  name := "akka-picture-service",
  version := "0.1",
  scalaVersion := "2.11.8"
)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
      "com.sksamuel.scrimage" %% "scrimage-core" % scrimageVersion,
      "com.sksamuel.scrimage" %% "scrimage-io-extra" % scrimageVersion,
      "com.sksamuel.scrimage" %% "scrimage-filters" % scrimageVersion,
      "com.lihaoyi" %% "ammonite-ops" % ammoniteVersion,
      "org.scalatest" %% "scalatest" % "2.2.6" % "test",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test",
      "com.lihaoyi" % "ammonite-repl" % ammoniteVersion % "test" cross CrossVersion.full
    )
  )

initialCommands in (Test, console) := """ammonite.repl.Main().run()"""

cancelable in Global := true

testOptions in Test += Tests.Argument("-oS")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  //  "-Ywarn-value-discard",
  "-Xfuture"
)