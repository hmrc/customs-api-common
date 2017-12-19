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

package unit.config

import com.typesafe.config.{Config, ConfigFactory}
import uk.gov.hmrc.customs.api.common.config.ConfigValidationNelAdaptor
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration.DurationInt
import scalaz.syntax.validation._


class ConfigValidationNelAdaptorSpec extends UnitSpec with MockitoSugar with Matchers {

  private def appConfig(context: String): Config = ConfigFactory.parseString(
    s"""
      |root-string-key = root-string-value
      |root-int-key = 101
      |root-bool-key = true
      |root-duration-key = 60seconds
      |root-string-seq-key.0 = element1
      |root-string-seq-key.1 = element2
      |Test {
      |  microservice {
      |    services {
      |      email {
      |        host = localhost
      |        port = 1111
      |        context = $context
      |        string-key = string-value
      |        int-key = 101
      |        bool-key = true
      |        duration-key = 60seconds
      |      }
      |    }
      |  }
      |}
    """.stripMargin)

  private val validAppConfig: Config = appConfig("/context")
  private val prefixMissingContextConfig: Config = appConfig("context")
  private val nullContextConfig: Config = appConfig("null")

  private def testServicesConfig(configuration: Config) = new ServicesConfig {
    override val runModeConfiguration = new Configuration(configuration)
    override val mode = play.api.Mode.Test
    override def environment: Environment = mock[Environment]
  }

  private val configValidationNelAdaptor = new ConfigValidationNelAdaptor(testServicesConfig(validAppConfig), new Configuration(validAppConfig))
  private val prefixMissingContextNelAdaptor = new ConfigValidationNelAdaptor(testServicesConfig(prefixMissingContextConfig), new Configuration(prefixMissingContextConfig))
  private val nullContextNelAdaptor = new ConfigValidationNelAdaptor(testServicesConfig(nullContextConfig), new Configuration(nullContextConfig))

  "For root level config ConfigValidationNelAdaptor" should {
    "return error when key not found" in {
      configValidationNelAdaptor.root.string("ENSURE_KEY_NOT_FOUND") shouldBe "Could not find config key 'ENSURE_KEY_NOT_FOUND'".failureNel[String]
    }
    "return error when value is of wrong type" in {
      configValidationNelAdaptor.root.int("root-string-key") shouldBe "Configuration error[String: 2: root-string-key has type STRING rather than NUMBER]".failureNel[String]
    }
    "read a string" in {
      configValidationNelAdaptor.root.string("root-string-key") shouldBe "root-string-value".successNel
    }
    "read an Int" in {
      configValidationNelAdaptor.root.int("root-int-key") shouldBe 101.successNel
    }
    "read a Boolean" in {
      configValidationNelAdaptor.root.boolean("root-bool-key") shouldBe true.successNel
    }
    "read a Duration" in {
      configValidationNelAdaptor.root.duration("root-duration-key") shouldBe 60.seconds.successNel
    }
    "read a maybeString" in {
      configValidationNelAdaptor.root.maybeString("root-string-key") shouldBe Some("root-string-value").successNel
    }
    "return None when a maybeString key is not found" in {
      configValidationNelAdaptor.root.maybeString("ENSURE_KEY_NOT_FOUND") shouldBe None.successNel
    }
    "read a stringSeq" in {
      configValidationNelAdaptor.root.stringSeq("root-string-seq-key") shouldBe Seq("element1", "element2").successNel
    }
    "return Nil when a stringSeq key is not found" in {
      configValidationNelAdaptor.root.stringSeq("ENSURE_KEY_NOT_FOUND") shouldBe Nil.successNel
    }
  }

  "For service level ConfigValidationNelAdaptor" should {
    val emailNelAdaptor = configValidationNelAdaptor.service("email")

    "return error when key not found for String" in {
      emailNelAdaptor.string("ENSURE_KEY_NOT_FOUND") shouldBe "Service configuration not found for key: email.ENSURE_KEY_NOT_FOUND".failureNel
    }
    "return error when key not found for Int" in {
      emailNelAdaptor.int("ENSURE_KEY_NOT_FOUND") shouldBe "Service configuration not found for key: email.ENSURE_KEY_NOT_FOUND".failureNel
    }
    "return error when key not found for Boolean" in {
      emailNelAdaptor.boolean("ENSURE_KEY_NOT_FOUND") shouldBe "Service configuration not found for key: email.ENSURE_KEY_NOT_FOUND".failureNel
    }
    "return error when key not found for Duration" in {
      emailNelAdaptor.duration("ENSURE_KEY_NOT_FOUND") shouldBe "Service configuration not found for key: email.ENSURE_KEY_NOT_FOUND".failureNel
    }
    "return error when value is of wrong type" in {
      emailNelAdaptor.int("string-key") shouldBe "Configuration error[String: 15: Test.microservice.services.email.string-key has type STRING rather than NUMBER]".failureNel
    }
    "return error when a field is null" in {
      nullContextNelAdaptor.service("email").serviceUrl shouldBe "Configuration error[String: 14: Configuration key 'Test.microservice.services.email.context' is set to null but expected STRING]".failureNel
    }
    "return error when context does not start with a '/'" in {
      prefixMissingContextNelAdaptor.service("email").serviceUrl shouldBe "For service 'email' context 'context' does not start with '/'".failureNel
    }
    "read a string" in {
      emailNelAdaptor.string("string-key") shouldBe "string-value".successNel
    }
    "read an Int" in {
      emailNelAdaptor.int("int-key") shouldBe 101.successNel
    }
    "read a Boolean" in {
      emailNelAdaptor.boolean("bool-key") shouldBe true.successNel
    }
    "read a Duration" in {
      emailNelAdaptor.duration("duration-key") shouldBe 60.seconds.successNel
    }
    "read a base url" in {
      emailNelAdaptor.baseUrl shouldBe "http://localhost:1111".successNel
    }
    "read a service url" in {
      emailNelAdaptor.serviceUrl shouldBe "http://localhost:1111/context".successNel
    }
  }
}
