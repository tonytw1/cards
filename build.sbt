name := """cards"""
organization := "uk.co.eelpieconsulting"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test

libraryDependencies += "org.jsoup" % "jsoup" % "1.18.1"
libraryDependencies += "commons-codec" % "commons-codec" % "1.17.0"
libraryDependencies += "commons-io" % "commons-io" % "2.16.1"
libraryDependencies += ws

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "uk.co.eelpieconsulting.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "uk.co.eelpieconsulting.binders._"

enablePlugins(DockerPlugin)
dockerBaseImage := "openjdk:11-jre"
dockerExposedPorts in Docker := Seq(9000)
