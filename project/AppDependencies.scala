import sbt._

object AppDependencies {

  private val bootstrapPlay26Version = "1.13.0"
  private val xmlResolverVersion = "1.2"
  private val catsVersion = "2.0.0"
  private val pegdownVersion = "1.6.0"
  private val scalatestplusVersion = "3.1.3"
  private val wireMockVersion = "2.26.3"
  private val mockitoVersion = "3.3.3"
  private val testScope = "test,it"

  val bootstrapPlay26 = "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapPlay26Version
  
  val xmlResolver = "xml-resolver" % "xml-resolver" % xmlResolverVersion

  val cats = "org.typelevel" %% "cats-core" % catsVersion

  val pegdown = "org.pegdown" % "pegdown" % pegdownVersion

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock" % wireMockVersion % testScope exclude("org.apache.httpcomponents","httpclient") exclude("org.apache.httpcomponents","httpcore")

  val mockito =  "org.mockito" % "mockito-core" % mockitoVersion % testScope
}
