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

package uk.gov.hmrc.customs.api.common.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import uk.gov.hmrc.customs.api.common.config.{ServicesConfig, WSHttp}
import uk.gov.hmrc.customs.api.common.domain.Registration
import uk.gov.hmrc.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait ServiceLocatorConnector {

  val appName: String
  val appUrl: String
  val serviceUrl: String
  val handlerOK: () => Unit
  val handlerError: Throwable => Unit
  val metadata: Option[Map[String, String]]
  val http: WSHttp

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

@Singleton
class ServiceLocatorConnectorImpl @Inject()(servicesConfig: ServicesConfig, wsHttp: WSHttp) extends ServiceLocatorConnector {

  val appName = servicesConfig.getString("appName")
  val appUrl =  servicesConfig.getString("appUrl")
  val serviceUrl = servicesConfig.baseUrl("service-locator")
  val http: WSHttp = wsHttp
  val handlerOK = () => Logger.info("Service is registered with the service locator")
  val handlerError = (e: Throwable) => Logger.error("Service could not register with the service locator", e)
  val metadata = Some(Map("third-party-api" -> "true"))

}
