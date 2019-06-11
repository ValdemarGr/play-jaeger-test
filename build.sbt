name := """play-jaeger-test"""
organization := "test"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "io.opentracing.contrib" % "opentracing-scala-concurrent_2.12" % "0.0.4"
libraryDependencies += "io.jaegertracing" % "jaeger-client" % "0.35.5"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.47"
libraryDependencies += "io.opentracing.contrib" % "opentracing-jdbc" % "0.1.3"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0-M2"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "test.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "test.binders._"
