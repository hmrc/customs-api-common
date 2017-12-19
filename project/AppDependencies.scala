import sbt._

object AppDependencies {

  private val microserviceBootstrapVersion = "6.12.0"
  private val authClientVersion = "2.3.0"
  private val hmrcTestVersion = "2.3.0"
  private val scalaTestVersion = "2.2.6"
  private val scalatestplusVersion = "1.5.1"
  private val mockitoVersion = "2.6.2"
  private val pegdownVersion = "1.6.0"
  private val wireMockVersion = "2.2.2"
  private val scalazVersion = "7.2.15"
  private val testScope = "test,it"

  val xmlResolver: ModuleID = "xml-resolver" % "xml-resolver" % "1.2"

  val microserviceBootStrap: ModuleID = "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion

  val authClient: ModuleID =  "uk.gov.hmrc" %% "auth-client" % authClientVersion

  val hmrcTest: ModuleID = "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % testScope

  val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % scalaTestVersion % testScope

  val pegDown: ModuleID = "org.pegdown" % "pegdown" % pegdownVersion % testScope

  val scalaTestPlusPlay: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope

  val wireMock: ModuleID = "com.github.tomakehurst" % "wiremock" % wireMockVersion % testScope exclude("org.apache.httpcomponents","httpclient") exclude("org.apache.httpcomponents","httpcore")

  val mockito: ModuleID =  "org.mockito" % "mockito-core" % mockitoVersion % testScope

  val scalaz: ModuleID = "org.scalaz" %% "scalaz-core" % scalazVersion

}
