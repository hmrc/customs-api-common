import sbt._

object AppDependencies {

  private val bootstrapBackendPlay27Version = "3.0.0"
  private val xmlResolverVersion = "1.2"
  private val catsVersion = "2.2.0"
  private val pegdownVersion = "1.6.0"
  private val scalatestplusVersion = "4.0.3"
  private val wireMockVersion = "2.27.2"
  private val mockitoVersion = "3.5.9"
  private val testScope = "test,it"

  val bootstrapBackendPlay27 = "uk.gov.hmrc" %% "bootstrap-backend-play-27" % bootstrapBackendPlay27Version
  
  val xmlResolver = "xml-resolver" % "xml-resolver" % xmlResolverVersion

  val cats = "org.typelevel" %% "cats-core" % catsVersion

  val pegdown = "org.pegdown" % "pegdown" % pegdownVersion

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock" % wireMockVersion % testScope exclude("org.apache.httpcomponents","httpclient") exclude("org.apache.httpcomponents","httpcore")

  val mockito =  "org.mockito" % "mockito-core" % mockitoVersion % testScope

  val silencerPlugin = compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.5" cross CrossVersion.full)
  val silencerLib = "com.github.ghik" % "silencer-lib" % "1.7.5" % Provided cross CrossVersion.full

}
