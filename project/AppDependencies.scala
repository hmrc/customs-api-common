import sbt._

object AppDependencies {

  private val bootstrapPlay26Version = "1.1.0"
  private val xmlResolverVersion = "1.2"
  private val catsVersion = "2.0.0"
  private val hmrcTestVersion = "3.9.0-play-26"
  private val scalaTestVersion = "3.0.8"
  private val scalatestplusVersion = "3.1.2"
  private val wireMockVersion = "2.23.2"
  private val mockitoVersion = "3.0.0"
  private val testScope = "test,it"

  val bootstrapPlay26 = "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapPlay26Version
  
  val xmlResolver = "xml-resolver" % "xml-resolver" % xmlResolverVersion

  val cats = "org.typelevel" %% "cats-core" % catsVersion

  val hmrcTest = "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % testScope

  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % testScope

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock" % wireMockVersion % testScope exclude("org.apache.httpcomponents","httpclient") exclude("org.apache.httpcomponents","httpcore")

  val mockito =  "org.mockito" % "mockito-core" % mockitoVersion % testScope
}
