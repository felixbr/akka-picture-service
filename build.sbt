name := "akka-http-test"

version := "0.1"

scalaVersion := "2.11.8"

resolvers += "stephenjudkins-bintray" at "http://dl.bintray.com/stephenjudkins/maven"

val akkaVersion = "2.4.3"

val scrimageVersion = "2.1.0"

val commonSettings = Seq(

)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
      "ps.tricerato" %% "pureimage" % "0.1.2",
      "com.sksamuel.scrimage" %% "scrimage-core" % scrimageVersion,
      "com.sksamuel.scrimage" %% "scrimage-io-extra" % scrimageVersion,
      "com.sksamuel.scrimage" %% "scrimage-filters" % scrimageVersion,
      "com.lihaoyi" %% "ammonite-ops" % "0.5.7",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test"
    )
  )

cancelable in Global := true

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