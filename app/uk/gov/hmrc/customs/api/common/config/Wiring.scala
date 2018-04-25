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

package uk.gov.hmrc.customs.api.common.config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook

@Singleton
class WSHttp extends uk.gov.hmrc.play.http.ws.WSHttp
  with HttpGet with HttpPut with HttpPost with HttpDelete with HttpPatch {
  val hooks: Seq[HttpHook] = NoneRequired
}

@Singleton
class ServicesConfig @Inject() (override val runModeConfiguration: Configuration,
                                environment: Environment) extends uk.gov.hmrc.play.config.ServicesConfig {
  override protected def mode = environment.mode
}
