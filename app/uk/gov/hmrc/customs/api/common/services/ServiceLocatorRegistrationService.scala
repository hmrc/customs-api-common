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

package uk.gov.hmrc.customs.api.common.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.customs.api.common.config.ServicesConfig
import uk.gov.hmrc.customs.api.common.connectors.ServiceLocatorConnector
import uk.gov.hmrc.http.HeaderCarrier

trait ServiceLocatorRegistrationService

@Singleton
class ServiceLocatorRegistrationServiceImpl @Inject()(serviceLocatorConnector: ServiceLocatorConnector,
                                                  servicesConfig: ServicesConfig) extends ServiceLocatorRegistrationService {

  val registrationEnabled: Boolean = servicesConfig.getConfBool("service-locator.enabled", defBool = true)

  if (registrationEnabled) {
    Logger.info("Registering the Service")
    serviceLocatorConnector.register(HeaderCarrier())
  } else {
    Logger.warn("Registration in Service Locator is disabled")
  }
}
