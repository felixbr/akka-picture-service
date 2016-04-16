name := "akka-http-test"

version := "0.1"

scalaVersion := "2.12.0-M3"

val akkaVersion = "2.4.3"

val commonSettings = Seq(

)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion
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