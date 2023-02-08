/*
 * Copyright 2023 HM Revenue & Customs
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

package unit.http

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, post, stubFor, _}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.{HeaderNames, MimeTypes}
import uk.gov.hmrc.customs.api.common.http.ExtensibleHttpErrorFunctions
import uk.gov.hmrc.http.{HttpException, HttpReads, HttpResponse}
import util.{UnitSpec, WireMockRunner}

class ExtensibleHttpErrorFunctionsSpec extends UnitSpec with MockitoSugar with WireMockRunner{

  def setupEndpointToReturn(status: Int): Unit =
    stubFor(post(urlMatching("/dummy"))
      .withHeader(HeaderNames.ACCEPT, equalTo(MimeTypes.JSON))
      .withHeader(HeaderNames.CONTENT_TYPE, equalTo(MimeTypes.JSON))
      willReturn aResponse()
      .withStatus(status))

  val exceptionError = "We got the error we expected"

  "ExtensibleHttpErrorFunctions" should {
    "override handling 200 responses " in {
      val customReads = new ExtensibleHttpErrorFunctions {
        override def handle2xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
          throw new HttpException(exceptionError, response.status)
      }

      checkErrorHandlingForStatus(200, customReads.readRaw)
      checkErrorHandlingForStatus(204, customReads.readRaw)
      checkErrorHandlingForStatus(299, customReads.readRaw)
    }

    "override handling 300 responses " in {
      val customReads = new ExtensibleHttpErrorFunctions {
        override def handle3xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
          throw new HttpException(exceptionError, response.status)
      }

      checkErrorHandlingForStatus(300, customReads.readRaw)
      checkErrorHandlingForStatus(307, customReads.readRaw)
      checkErrorHandlingForStatus(399, customReads.readRaw)
    }

    "override handling 400 responses " in {
      val customReads = new ExtensibleHttpErrorFunctions {
        override def handle4xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
          throw new HttpException(exceptionError, response.status)
      }

      checkErrorHandlingForStatus(400, customReads.readRaw)
      checkErrorHandlingForStatus(404, customReads.readRaw)
      checkErrorHandlingForStatus(499, customReads.readRaw)
    }

    "override handling 500 responses " in {
      val customReads = new ExtensibleHttpErrorFunctions {
        override def handle5xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
          throw new HttpException(exceptionError, response.status)
      }

      checkErrorHandlingForStatus(500, customReads.readRaw)
      checkErrorHandlingForStatus(504, customReads.readRaw)
      checkErrorHandlingForStatus(599, customReads.readRaw)
    }

    "override handling all other status code responses" in {
      val customReads = new ExtensibleHttpErrorFunctions {
        override def handleAnyOtherResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
          throw new HttpException(exceptionError, response.status)
      }

      checkErrorHandlingForStatus(0, customReads.readRaw)
      checkErrorHandlingForStatus(104, customReads.readRaw)
      checkErrorHandlingForStatus(600, customReads.readRaw)
      checkErrorHandlingForStatus(6000, customReads.readRaw)
    }
  }

  private def checkErrorHandlingForStatus(status: Int, readsUnderTest: HttpReads[HttpResponse]) = {
    val response = mock[HttpResponse]
    when(response.status).thenReturn(status)

    val caught = intercept[HttpException]{
      readsUnderTest.read("GET", "/dummy", response)
    }

    caught.getMessage should include(exceptionError)
  }
}
