/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.customs.api.common.http
import uk.gov.hmrc.http.{HttpErrorFunctions, HttpReads, HttpResponse}

/**
  * Template based off of the 'http-verbs' HttpErrorFunctions class but which allows easy customisation of HTTP error
  * handling
  */
trait ExtensibleHttpErrorFunctions extends HttpErrorFunctions {
  implicit val readRaw: HttpReads[HttpResponse] = new HttpReads[HttpResponse]{
    def read(method: String, url: String, response: HttpResponse) = handleResponse(method, url)(response)
  }

  private def equals2xx(status: Int): Boolean = status >= 200 && status < 300
  private def equals3xx(status: Int): Boolean = status >= 300 && status < 400
  private def equals4xx(status: Int): Boolean = status >= 400 && status < 500
  private def equals5xx(status: Int): Boolean = status >= 500 && status < 600

  def handle2xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
    super.handleResponse(httpMethod, url)(response)

  def handle3xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
    super.handleResponse(httpMethod, url)(response)

  def handle4xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
    super.handleResponse(httpMethod, url)(response)

  def handle5xxResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
    super.handleResponse(httpMethod, url)(response)

  def handleAnyOtherResponse(httpMethod: String, url: String, response: HttpResponse): HttpResponse =
    super.handleResponse(httpMethod, url)(response)

  override def handleResponse(httpMethod: String, url: String)(response: HttpResponse): HttpResponse =
    response.status match {
      case status if equals2xx(status) => handle2xxResponse(httpMethod, url, response)
      case status if equals3xx(status) => handle3xxResponse(httpMethod, url, response)
      case status if equals4xx(status) => handle4xxResponse(httpMethod, url, response)
      case status if equals5xx(status) => handle5xxResponse(httpMethod, url, response)
      case _ => handleAnyOtherResponse(httpMethod, url, response)
    }
}
