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

package util

import org.mockito.Mockito
import uk.gov.hmrc.http.HeaderCarrier

object MockitoPassByNameHelper {

  sealed trait Builder {

    def withByNameParam[P](expected: P): Builder

    def withAnyHeaderCarrierParam(): Builder

    def verify(): Unit
  }

  case class PassByNameParam(clazz: Class[_], paramMatcher: AnyRef)

  /**
    * Work around for the fact that Mockito can not handle Scala pass by name parameters
    * eg `debug(msg: => String)` - see
    * [[https://stackoverflow.com/questions/2152019/how-to-mock-a-method-with-functional-arguments-in-scala
    * solution takes inspiration form this Stack overflow]]. A builder pattern is used to reduce errors when specifying
    * method signatures and parameter values. Usage:
    * {{{
    *  PassByNameVerifier(mockDeclarationLogger, "error")
    *    .withByNameParam[String](s"Call to get api subscription fields failed. url=$expectedUrl")
    *    .withByNameParam[Throwable](caught)
    *    .withAnyHeaderCarrierParam
    *    .verify()
    * }}}
    *
    */
  case class PassByNameVerifier[T](mockedInstance: T, methodName: String, params: Seq[PassByNameParam] = Seq.empty)(implicit m: Manifest[T]) extends Builder {

    def withByNameParam[P](expected: P): Builder = this.copy(params = this.params :+ PassByNameParam(classOf[() => P], genericPassByName[P](expected)))

    def withAnyHeaderCarrierParam(): Builder = this.copy(params = this.params :+ PassByNameParam(classOf[HeaderCarrier], anyHeaderCarrierMatcher))

    def verify(): Unit = {
      require(params.nonEmpty, "no parameters specified.")
      val method = m.runtimeClass.asInstanceOf[Class[T]].getMethod(methodName, params.map(param => param.clazz): _*)
      method.invoke(
        Mockito.verify(mockedInstance),
        params.map(param => param.paramMatcher): _*
      )
    }

    private lazy val anyHeaderCarrierMatcher = new HeaderCarrier() {
      override def equals(o: Any): Boolean = o.getClass == classOf[HeaderCarrier]
      override def hashCode(): Int = super.hashCode()
    }

    private def genericPassByName[P](expected: P): () => P = new (() => P) {
      override def equals(o: Any): Boolean = expected == o.asInstanceOf[() => P].apply()
      def apply(): P = throw new IllegalArgumentException("unexpected apply()")
    }
  }
}
