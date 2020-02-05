import AppDependencies._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, targetJvm}
import uk.gov.hmrc.PublishingSettings._
import uk.gov.hmrc.gitstamp.GitStampPlugin._

import scala.language.postfixOps

organization := "uk.gov.hmrc"

name := "customs-api-common"

targetJvm := "jvm-1.8"

resolvers  ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)

publishArtifact in (Test, packageBin) := true
publishArtifact in (Test, packageSrc) := true
publishArtifact in (Compile, packageSrc) := true

lazy val ComponentTest = config("component") extend Test
lazy val EndToEndTest = config("endtoend") extend Test
lazy val CdsIntegrationTest = config("it") extend Test

val testConfig = Seq(EndToEndTest, ComponentTest, CdsIntegrationTest, Test)

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) =>
      Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq( testAll := (test in ComponentTest).dependsOn((test in CdsIntegrationTest).dependsOn(test in Test)).value )

val ScalaVer_2_12 = "2.12.10"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .configs(testConfig: _*)
  .settings(
    gitStampSettings,
    unitTestSettings,
    integrationTestSettings,
    componentTestSettings,
    endtoendTestSettings,
    playPublishingSettings,
    allTest,
    scoverageSettings
  )
  .settings(majorVersion := 1)
  .settings(makePublicallyAvailableOnBintray := true)
  .settings(crossScalaVersions := Seq("2.11.12", ScalaVer_2_12))
  .settings(scalaVersion := ScalaVer_2_12)

def onPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      testOptions in Test := Seq(Tests.Filter(onPackageName("unit"))),
      unmanagedSourceDirectories in Test := Seq((baseDirectory in Test).value / "test"),
      addTestReportOption(Test, "test-reports")
    )

lazy val integrationTestSettings =
  inConfig(CdsIntegrationTest)(Defaults.testTasks) ++
    Seq(
      testOptions in CdsIntegrationTest := Seq(Tests.Filters(Seq(onPackageName("integration"), onPackageName("component")))),
      fork in CdsIntegrationTest := false,
      parallelExecution in CdsIntegrationTest := false,
      addTestReportOption(CdsIntegrationTest, "int-test-reports"),
      testGrouping in CdsIntegrationTest := forkedJvmPerTestConfig((definedTests in Test).value, "integration", "component")
    )

lazy val componentTestSettings =
  inConfig(ComponentTest)(Defaults.testTasks) ++
    Seq(
      testOptions in ComponentTest := Seq(Tests.Filter(onPackageName("component"))),
      fork in ComponentTest := false,
      parallelExecution in ComponentTest := false,
      addTestReportOption(ComponentTest, "component-reports")
    )

lazy val endtoendTestSettings =
  inConfig(EndToEndTest)(Defaults.testTasks) ++
    Seq(
      testOptions in EndToEndTest := Seq(Tests.Filter(onPackageName("endtoend"))),
      fork in EndToEndTest := false,
      parallelExecution in EndToEndTest := false,
      addTestReportOption(EndToEndTest, "e2e-test-reports")
    )

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := "<empty>;.*(Reverse|AuthService|BuildInfo|Routes).*",
  coverageMinimum := 96,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

lazy val playPublishingSettings: Seq[sbt.Setting[_]] =
  Seq(credentials += SbtCredentials) ++
  publishAllArtefacts

publishArtifact in Test := true
val compileDependencies = Seq(bootstrapPlay26, cats)

val testDependencies = Seq(hmrcTest, scalaTest, scalaTestPlusPlay, wireMock, mockito)

unmanagedResourceDirectories in Compile += baseDirectory.value / "public"

libraryDependencies ++= compileDependencies ++ testDependencies

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)
