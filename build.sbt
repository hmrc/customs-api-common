import AppDependencies._
import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.PublishingSettings._


import scala.language.postfixOps

organization := "uk.gov.hmrc"

name := "customs-api-common"

targetJvm := "jvm-1.8"

resolvers  ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)

publishArtifact in (Test, packageBin) := true
publishArtifact in (Test, packageSrc) := true
publishArtifact in (Compile, packageSrc) := true

lazy val AcceptanceTest = config("acceptance") extend Test
lazy val EndToEndTest = config("endtoend") extend Test
lazy val CdsIntegrationTest = config("it") extend Test

val testConfig = Seq(EndToEndTest, AcceptanceTest, CdsIntegrationTest, Test)

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) =>
      Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq( testAll := (test in AcceptanceTest).dependsOn((test in CdsIntegrationTest).dependsOn(test in Test)).value )

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .configs(testConfig: _*)
  .settings(
    commonSettings,
    unitTestSettings,
    integrationTestSettings,
    acceptanceTestSettings,
    endtoendTestSettings,
    playPublishingSettings,
    allTest,
    scoverageSettings
  ).settings(majorVersion := 1)
  .settings(makePublicallyAvailableOnBintray := true)

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
      testOptions in CdsIntegrationTest := Seq(Tests.Filters(Seq(onPackageName("integration"), onPackageName("acceptance")))),
      fork in CdsIntegrationTest := false,
      parallelExecution in CdsIntegrationTest := false,
      addTestReportOption(CdsIntegrationTest, "int-test-reports"),
      testGrouping in CdsIntegrationTest := forkedJvmPerTestConfig((definedTests in Test).value, "integration", "acceptance")
    )

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testTasks) ++
    Seq(
      testOptions in AcceptanceTest := Seq(Tests.Filter(onPackageName("acceptance"))),
      fork in AcceptanceTest := false,
      parallelExecution in AcceptanceTest := false,
      addTestReportOption(AcceptanceTest, "acceptance-reports")
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
  coverageMinimum := 98,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

lazy val commonSettings: Seq[Setting[_]] = scalaSettings ++
  defaultSettings() ++
  gitStampSettings

lazy val playPublishingSettings: Seq[sbt.Setting[_]] = sbtrelease.ReleasePlugin.releaseSettings ++
  Seq(credentials += SbtCredentials) ++
  publishAllArtefacts

publishArtifact in Test := true
val compileDependencies = Seq(bootstrapPlay26, xmlResolver, cats)

val testDependencies = Seq(hmrcTest, scalaTest, scalaTestPlusPlay, wireMock, mockito)

unmanagedResourceDirectories in Compile += baseDirectory.value / "public"

libraryDependencies ++= compileDependencies ++ testDependencies

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)
