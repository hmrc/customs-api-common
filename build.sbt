import AppDependencies._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, targetJvm}
import uk.gov.hmrc.gitstamp.GitStampPlugin._

import scala.language.postfixOps

organization := "uk.gov.hmrc"
name := "customs-api-common"

Test / packageBin / publishArtifact := true
Test / packageSrc / publishArtifact := true
Compile / packageSrc / publishArtifact := true

lazy val ComponentTest = config("component") extend Test
lazy val CdsIntegrationTest = config("it") extend Test

val testConfig = Seq(ComponentTest, CdsIntegrationTest, Test)

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) =>
      Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq

lazy val testAll = TaskKey[Unit]("test-all")
lazy val allTest = Seq( testAll := (ComponentTest / test).dependsOn((CdsIntegrationTest / test).dependsOn(Test / test)).value )

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .configs(testConfig: _*)
  .settings(
    scalaVersion := "2.13.10",
    targetJvm := "jvm-11",
    gitStampSettings,
    unitTestSettings,
    integrationTestSettings,
    componentTestSettings,
    allTest,
    scoverageSettings,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
  )
  .settings(majorVersion := 1)

def onPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      Test / testOptions := Seq(Tests.Filter(onPackageName("unit"))),
      Test / unmanagedSourceDirectories := Seq((Test / baseDirectory).value / "test"),
      addTestReportOption(Test, "test-reports")
    )

lazy val integrationTestSettings =
  inConfig(CdsIntegrationTest)(Defaults.testTasks) ++
    Seq(
      CdsIntegrationTest / testOptions := Seq(Tests.Filters(Seq(onPackageName("integration"), onPackageName("component")))),
      CdsIntegrationTest / parallelExecution := false,
      addTestReportOption(CdsIntegrationTest, "int-test-reports"),
      CdsIntegrationTest / testGrouping := forkedJvmPerTestConfig((Test / definedTests).value, "integration", "component")
    )

lazy val componentTestSettings =
  inConfig(ComponentTest)(Defaults.testTasks) ++
    Seq(
      ComponentTest / testOptions := Seq(Tests.Filter(onPackageName("component"))),
      ComponentTest / fork := false,
      ComponentTest / parallelExecution := false,
      addTestReportOption(ComponentTest, "component-reports")
    )

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := "<empty>;.*(Reverse|AuthService|BuildInfo|Routes).*",
  coverageMinimumStmtTotal := 90,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

val compileDependencies = Seq(bootstrapBackendPlay28, cats, flexmark)

val testDependencies = Seq(pegdown, scalaTestPlusPlay, wireMock, mockito, scalaTestPlusMockito, flexmark, jackson, bootstrapTestPlay)

Compile / unmanagedResourceDirectories += baseDirectory.value / "public"
Test / unmanagedResourceDirectories += baseDirectory.value / "test" / "resources"

libraryDependencies ++= compileDependencies ++ testDependencies

