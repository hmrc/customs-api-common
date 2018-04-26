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
import play.api.Configuration
import scalaz.Validation.FlatMap._
import scalaz._
import scalaz.syntax.apply._
import scalaz.syntax.validation._

import scala.concurrent.duration.Duration

/**
  * <p>
  * We need a wrapper for ServicesConfig as the current API rely's on throwing exceptions for signalling failure -
  * this is fail fast behaviour. What we want is to aggregate errors (on application startup).
  * The `ValidationNel` class in the `scalaz` library gives a type that allows us aggregate errors. It's declaration is
  * `type ValidationNel[E, +X] = Validation[NonEmptyList[E], X]`
  * `Nel` is short for `NonEmptyList` which is simply a `List` that is guaranteed to contain at least 1 item.
  * </p>
  * Example usage:
  *
  * {{{
  * @Singleton
  * class CustomsConfigService @Inject()(configValidationNel: ConfigValidationNelAdaptor) {
  *   private val root = configValidationNel.root
  *   private val validatedCustomsEnrolmentConfig: ValidationNel[String, CustomsEnrolmentConfig] = (
  *     root.string("customs.enrolment.name") |@|
  *     root.string("customs.enrolment.eori-identifier")
  *   )(CustomsEnrolmentConfig.apply)
  * ...
  * }
  * }}}
  *
  * @param servicesConfig HMRC services config lib class
  */
@Singleton
class ConfigValidationNelAdaptor @Inject()(servicesConfig: ServicesConfig, configuration: Configuration) {

  trait ValidationNelAdaptor {
    def string(key: String): ValidationNel[String, String]
    def int(key: String): ValidationNel[String, Int]
    def boolean(key: String): ValidationNel[String, Boolean]
    def duration(key: String): ValidationNel[String, Duration]
  }

  trait RootValidationNelAdaptor extends ValidationNelAdaptor {
    def maybeString(key: String): ValidationNel[String, Option[String]]
    def stringSeq(key: String): ValidationNel[String, Seq[String]]
  }

  trait UrlNelAdaptor {
    def baseUrl: ValidationNel[String, String]
    def serviceUrl: ValidationNel[String, String]
  }

  def root: RootValidationNelAdaptor = RootConfigReader

  def service(serviceName: String): ValidationNelAdaptor with UrlNelAdaptor = ServiceConfigReader(serviceName)

  private object RootConfigReader extends RootValidationNelAdaptor {

    override def string(key: String): ValidationNel[String, String] =
      validationNel(servicesConfig.getString(key))

    override def int(key: String): ValidationNel[String, Int] =
      validationNel(servicesConfig.getInt(key))

    override def boolean(key: String): ValidationNel[String, Boolean] =
      validationNel(servicesConfig.getBoolean(key))

    override def duration(key: String): ValidationNel[String, Duration] =
      validationNel(servicesConfig.getDuration(key))

    def maybeString(key: String): ValidationNel[String, Option[String]] = {
      configuration.getString(key).successNel[String]
    }

    override def stringSeq(key: String): ValidationNel[String, Seq[String]] = {
      configuration.getStringSeq(key).getOrElse(Nil).successNel
    }
  }

  private def validationNel[T](f: => T): ValidationNel[String, T] = {
    Validation.fromTryCatchNonFatal(f).leftMap[String](e => e.getMessage).toValidationNel
  }

  private case class ServiceConfigReader(serviceName: String) extends ValidationNelAdaptor with UrlNelAdaptor {

    override def string(key: String): ValidationNel[String, String] =
      validationNel(readConfig(key, servicesConfig.getConfString))

    override def int(key: String): ValidationNel[String, Int] =
      validationNel(readConfig(key, servicesConfig.getConfInt))

    override def boolean(key: String): ValidationNel[String, Boolean] =
      validationNel(readConfig(key, servicesConfig.getConfBool))

    override def duration(key: String): ValidationNel[String, Duration] =
      validationNel(readConfig(key, servicesConfig.getConfDuration))

    override def baseUrl: ValidationNel[String, String] =
      validationNel(servicesConfig.baseUrl(serviceName))

    override def serviceUrl: ValidationNel[String, String] = {
      def url(base: String, context: String) = s"$base$context"

      val contextNel: ValidationNel[String, String] = string("context") flatMap { context =>
        lazy val failureNel = s"For service '$serviceName' context '$context' does not start with '/'".failureNel
        if (context.startsWith("/")) context.successNel else failureNel
      }

      (
        baseUrl |@| contextNel
      ) (url)
    }

    private def readConfig[T](key: String, f: (String, => T) => T) = {
      val serviceKey = serviceName + "."  + key
      f(serviceKey, throw new IllegalStateException(s"Service configuration not found for key: $serviceKey"))
    }
  }

}
