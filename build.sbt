import sbt._
import sbt.Keys._

lazy val commonSettings = Seq(

  organization := "com.hunorkovacs",
  scalaVersion := "2.12.12",
  crossScalaVersions := Seq("2.13.6"),

  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := "stacktome",

  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  resolvers ++= Seq(
    "JCenter" at "https://jcenter.bintray.com/",
    "My Nexus" at "https://nexus.stacktome.com/repository/maven-public/"),

  credentials ++= Seq(Credentials(Path.userHome / ".ivy2" / ".credentials"))
)

lazy val koauth = (project in file(".")).settings(commonSettings ++ Seq(
  name := """koauth""",
  version := "2.1.0-SNAPSHOT-" + sys.env.getOrElse("BUILD_NUMBER", "0"), // publishing is only allowed with Jenkins, default value for local use

  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.32",
    "org.slf4j" % "slf4j-simple" % "1.7.32" % "test",
    "org.specs2" %% "specs2-core" % "4.10.6",
    "org.specs2" %% "specs2-mock" % "4.10.6",
  ),

  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := "stacktome",
  updateOptions := updateOptions.value.withGigahorse(false),
  publishTo := {
    val nexus = "https://nexus.stacktome.com"

    if (isSnapshot.value)
      Some("snapshots" at nexus + "/repository/maven-snapshots")
    else
      Some("releases" at nexus + "/repository/maven-releases")
  }
))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaServerAppPackaging)