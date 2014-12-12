import sbt._
import Keys._
import sbtrelease.ReleasePlugin._
import com.typesafe.sbt.SbtScalariform._
import io.gatling.sbt.GatlingPlugin
import scalariform.formatter.preferences._

object MesosUtilsBuild extends Build {
  lazy val root = Project(
    id = "marathon-integration-tests",
    base = file("."),
    settings =
      baseSettings ++
      releaseSettings ++
      Format.settings ++
      Seq(
        libraryDependencies ++= Dependencies.root,
        resolvers += Resolver.sonatypeRepo("public")
      )
  ).enablePlugins(GatlingPlugin)

  lazy val baseSettings = Defaults.defaultSettings ++ Seq (
    organization := "mesosphere",
    scalaVersion := "2.10.4",
    scalacOptions in Compile ++=
      Seq(
        "-encoding", "UTF-8", "-target:jvm-1.6", "-deprecation",
        "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"
      )
  )

}

object Dependencies {
  import Dependency._

  val root = Seq(
    // runtime
    config % "compile",
    gatling % "compile",
    playJson % "compile",

    // test
    Test.scalatest % "test",
    Test.gatlingTest % "test"
  )
}

object Dependency {
  object V {
    // runtime deps versions
    val Config = "1.2.1"
    val Gatling = "2.0.3"
    val PlayJson = "2.4.0-M2"

    // test deps versions
    val ScalaTest = "2.2.1"
    val GatlingTest = "1.0"
  }

  val config =
    "com.typesafe" % "config" % V.Config

  val gatling =
    "io.gatling.highcharts" % "gatling-charts-highcharts" % V.Gatling

  val playJson =
    "com.typesafe.play" %% "play-json" % V.PlayJson

  object Test {
    val scalatest =
      "org.scalatest" %% "scalatest" % V.ScalaTest

    val gatlingTest =
      "io.gatling" % "test-framework" % V.GatlingTest
  }
}

object Format {
  lazy val settings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := FormattingPreferences()
      .setPreference(IndentWithTabs, false)
      .setPreference(IndentSpaces, 2)
      .setPreference(AlignParameters, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
      .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
      .setPreference(PreserveDanglingCloseParenthesis, true)
      .setPreference(CompactControlReadability, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
      .setPreference(SpaceBeforeColon, false)
      .setPreference(SpaceInsideBrackets, false)
      .setPreference(SpaceInsideParentheses, false)
      .setPreference(SpacesWithinPatternBinders, true)
      .setPreference(FormatXml, true)
    )
}

