/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.customs.api.common.connectors

import uk.gov.hmrc.customs.api.common.domain.Registration
import play.api.{Application, Logger}
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSPost

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpPost }
import uk.gov.hmrc.http.hooks.HttpHook


trait ServiceLocatorConnector {

  val appName: String
  val appUrl: String
  val serviceUrl: String
  val handlerOK: () => Unit
  val handlerError: Throwable => Unit
  val metadata: Option[Map[String, String]]
  val http: HttpPost

  def register(implicit hc: HeaderCarrier): Future[Boolean] = {
    val registration = Registration(appName, appUrl, metadata)

    http.POST(s"$serviceUrl/registration", registration, Seq(CONTENT_TYPE -> JSON)) map {
      _ =>
        handlerOK()
        true
    } recover {
      case e: Throwable =>
        handlerError(e)
        false
    }
  }
}

class ServiceLocatorConnectorImpl(val app: Application) extends ServiceLocatorConnector with ServicesConfig {
  val appName = forConfigString("appName")
  val appUrl = forConfigString("appUrl")
  val serviceUrl = baseUrl("service-locator")
  val http = new HttpPost with WSPost {
    override val hooks: Seq[HttpHook] = NoneRequired
  }
  val handlerOK = () => Logger.info("Service is registered on the service locator")
  val handlerError = (e: Throwable) => Logger.error("Service could not register on the service locator", e)
  val metadata = Some(Map("third-party-api" -> "true"))

  private def forConfigString(key: String): String = app.configuration.getString(key)
    .getOrElse(throw new RuntimeException(s"$key is not configured"))
}
