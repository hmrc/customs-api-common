/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.connectors

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import play.mvc.Http.Status.OK
import uk.gov.hmrc.customs.api.common.config.ServicesConfig
import uk.gov.hmrc.customs.api.common.connectors.ServiceLocatorConnector
import uk.gov.hmrc.customs.api.common.domain.Registration
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServiceLocatorConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {

  private val APP_URL = "http://service"
  private val APP_NAME = "service"
  private val THIRD_PARTY_API = "third-party-api"

  trait Setup {
    implicit val hc = HeaderCarrier()
    val serviceLocatorException = new RuntimeException
    val mockServicesConfig = mock[ServicesConfig]
    val mockHttpClient = mock[HttpClient]

    val connector = new ServiceLocatorConnector(mockServicesConfig, mockHttpClient) {
      override val appUrl: String = APP_URL
      override val appName: String = APP_NAME
      override val serviceUrl: String = "https://SERVICE_LOCATOR"
      override val handlerOK: () => Unit = mock[() => Unit]
      override val handlerError: Throwable => Unit = mock[Throwable => Unit]
      override val metadata = Some(Map(THIRD_PARTY_API -> true.toString))
    }
  }

  "register" should {
    "register the JSON API Definition into the Service Locator" in new Setup {

      val registration = Registration(serviceName = APP_NAME, serviceUrl = APP_URL,
        metadata = Some(Map(THIRD_PARTY_API -> "true")))

      when(mockHttpClient.POST(s"${connector.serviceUrl}/registration", registration,
        Seq(CONTENT_TYPE -> JSON))).thenReturn(Future.successful(HttpResponse(OK)))

      connector.register.futureValue shouldBe true
      verify(mockHttpClient).POST("https://SERVICE_LOCATOR/registration", registration,
        Seq(CONTENT_TYPE -> JSON))
      verify(connector.handlerOK).apply()
      verify(connector.handlerError, never).apply(serviceLocatorException)
    }

    "fail registering in service locator" in new Setup {

      val registration = Registration(serviceName = APP_NAME, serviceUrl = APP_URL,
        metadata = Some(Map(THIRD_PARTY_API -> "true")))
      when(mockHttpClient.POST(s"${connector.serviceUrl}/registration", registration,
        Seq(CONTENT_TYPE -> JSON))).thenReturn(Future.failed(serviceLocatorException))

      connector.register.futureValue shouldBe false
      verify(mockHttpClient).POST("https://SERVICE_LOCATOR/registration", registration,
        Seq(CONTENT_TYPE -> JSON))
      verify(connector.handlerOK, never).apply()
      verify(connector.handlerError).apply(serviceLocatorException)
    }

  }
}

