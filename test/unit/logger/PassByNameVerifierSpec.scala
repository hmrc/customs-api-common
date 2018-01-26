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

package unit.logger

import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import util.MockitoPassByNameHelper.PassByNameVerifier

class PassByNameVerifierSpec extends UnitSpec with MockitoSugar with Matchers {

  private trait LoggerToMock {
    def error(msg: => String, e: => Throwable): Unit
    def error(msg: => String)(implicit hc: HeaderCarrier): Unit
  }

  private trait SetUp {
    val mockLogger = mock[LoggerToMock]
    val expectedException = new RuntimeException("expectedException")
    val notExpectedException = new RuntimeException("notExpectedException")
  }

  "PassByNameVerifier" can {
    "in happy path" should {
      "verify String and Throwable pass by name parameters" in new SetUp {
        val passByName = PassByNameVerifier(mockLogger, "error")
          .withByNameParam[String]("ERROR")
          .withByNameParam[Throwable](expectedException)

        mockLogger.error("ERROR", expectedException)

        passByName.verify()
      }

      "verify String pass by name parameters and implicit Header Carrier" in new SetUp {
        val passByName = PassByNameVerifier(mockLogger, "error")
          .withByNameParam[String]("ERROR")
          .withAnyHeaderCarrierParam()
        implicit val hc = HeaderCarrier()

        mockLogger.error("ERROR")

        passByName.verify()
      }
    }

    "in un-happy path" should {
      "verify there were zero interactions with this mock" in new SetUp {
        val passByName = PassByNameVerifier(mockLogger, "error")
          .withByNameParam[String]("ERROR")
          .withByNameParam[Throwable](expectedException)

        val caught = intercept[Throwable](passByName.verify())
        caught.getCause.getMessage should include("there were zero interactions with this mock")
      }

      "verify there IllegalArgumentException thrown when verifying with empty parameters" in new SetUp {
        val passByName = PassByNameVerifier(mockLogger, "error")

        mockLogger.error("ERROR", expectedException)

        val caught = intercept[Throwable](passByName.verify())

        caught.getMessage should include("no parameters specified.")
      }

      "verify wrong parameter type specification" in new SetUp {
        val wrongParamType: Int = 1
        val passByName = PassByNameVerifier(mockLogger, "error")
          .withByNameParam[Int](wrongParamType)
          .withByNameParam[Throwable](expectedException)

        mockLogger.error("ERROR", expectedException)

        val caught = intercept[Throwable](passByName.verify())
        verifyInternalError(caught)
      }

      "verify String parameter value matching errors" in new SetUp {
        val passByName = PassByNameVerifier(mockLogger, "error")
          .withByNameParam[String]("ERROR")
          .withByNameParam[Throwable](expectedException)

        mockLogger.error("VALUE_DOES_NOT_MATCH", expectedException)

        val caught = intercept[Throwable](passByName.verify())
        verifyInternalError(caught)
      }

      "verify Throwable parameter value matching errors" in new SetUp {
        val passByName = PassByNameVerifier(mockLogger, "error")
          .withByNameParam[String]("ERROR")
          .withByNameParam[Throwable](expectedException)

        mockLogger.error("Error", notExpectedException)

        val caught = intercept[Throwable](passByName.verify())
        verifyInternalError(caught)
      }
    }
  }

  private def verifyInternalError(caught: Throwable) = {
    caught.getCause.getMessage should include("Malformed class name")
  }
}
