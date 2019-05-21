import sbt._

object AppDependencies {

  private val bootstrapPlay25Version = "4.11.0"
  private val hmrcTestVersion = "3.8.0-play-25"
  private val scalaTestVersion = "3.0.6"
  private val scalatestplusVersion = "2.0.1"
  private val mockitoVersion = "2.27.0"
  private val wireMockVersion = "2.23.2"
  private val catsVersion = "1.6.0"
  private val testScope = "test,it"

  val bootstrapPlay25: ModuleID = "uk.gov.hmrc" %% "bootstrap-play-25" % bootstrapPlay25Version

  val xmlResolver: ModuleID = "xml-resolver" % "xml-resolver" % "1.2"

  val hmrcTest: ModuleID = "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % testScope

  val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % scalaTestVersion % testScope

  val scalaTestPlusPlay: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope

  val wireMock: ModuleID = "com.github.tomakehurst" % "wiremock" % wireMockVersion % testScope exclude("org.apache.httpcomponents","httpclient") exclude("org.apache.httpcomponents","httpcore")

  val mockito: ModuleID =  "org.mockito" % "mockito-core" % mockitoVersion % testScope

  val cats: ModuleID = "org.typelevel" %% "cats-core" % catsVersion
}
