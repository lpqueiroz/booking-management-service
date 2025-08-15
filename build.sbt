import sbt.Keys._
import play.sbt.PlaySettings

lazy val scala213 = "2.13.16"
lazy val scala3 = "3.3.4"
lazy val testcontainersVersion = "0.41.4"

lazy val root = (project in file("."))
  .enablePlugins(PlayService, PlayLayoutPlugin, Common)
  //.enablePlugins(PlayNettyServer).disablePlugins(PlayPekkoHttpServer) // uncomment to use the Netty backend
  .settings(
    name := "play-scala-rest-api-example",
    scalaVersion := scala213,
    crossScalaVersions := Seq(scala213, scala3),
    libraryDependencies ++= Seq(
      guice,
      "org.joda" % "joda-convert" % "3.0.1",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
      "com.indoorvivants" %% "scala-uri" % "4.2.0",
      "net.codingwell" %% "scala-guice" % "6.0.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
      "org.scalatest"        %% "scalatest"         % "3.2.19" % Test,
      "com.dimafeng"         %% "testcontainers-scala-scalatest" % testcontainersVersion % Test,
      "com.dimafeng"         %% "testcontainers-scala-postgresql" % testcontainersVersion % Test,
      "org.testcontainers" % "kafka" % "1.19.0" % Test,
      "org.testcontainers" % "testcontainers" % "1.19.0" % Test,
      "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.1",
      "org.sangria-graphql" %% "sangria" % "4.0.0",
      "com.github.fd4s" %% "fs2-kafka" % "3.9.0",
      "org.typelevel" %% "cats-effect" % "3.7-4972921",
      "org.sangria-graphql" %% "sangria-play-json" % "2.0.1",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC10",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC10",
      "org.postgresql" %  "postgresql"   % "42.7.3",
      "org.flywaydb" % "flyway-core" % "9.20.0",
      "io.circe" %% "circe-core"    % "0.14.9",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser"  % "0.14.9",
      "io.circe" %% "circe-fs2"     % "0.14.1",
      "org.sangria-graphql" %% "sangria-circe" % "1.3.2"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-Werror"
    ),
    dependencyOverrides ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3"
    )

  )

lazy val gatlingVersion = "3.9.5"
lazy val gatling = (project in file("gatling"))
  .enablePlugins(GatlingPlugin)
  .settings(
    scalaVersion := scala213,
    crossScalaVersions := Seq(scala213, scala3),
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Test,
      "io.gatling" % "gatling-test-framework" % gatlingVersion % Test,
    )
  )

// Documentation for this project:
//    sbt "project docs" "~ paradox"
//    open docs/target/paradox/site/index.html
lazy val docs = (project in file("docs")).enablePlugins(ParadoxPlugin).
  settings(
    scalaVersion := scala213,
    crossScalaVersions := Seq(scala213, scala3),
    paradoxProperties += ("download_url" -> "https://example.lightbend.com/v1/download/play-samples-play-scala-rest-api-example")
  )
