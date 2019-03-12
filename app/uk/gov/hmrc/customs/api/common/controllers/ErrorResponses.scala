/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.customs.api.common.controllers

import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._
import play.mvc.Http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_ACCEPTABLE, NOT_FOUND, _}

import scala.xml.NodeSeq

trait HttpStatusCodeShortDescriptions {
  // 2XX
  val OkCode = "OK"
  val CreatedCode = "CREATED"
  val AcceptedCode = "ACCEPTED"
  // 4XX
  val BadRequestCode = "BAD_REQUEST"
  val UnauthorizedCode = "UNAUTHORIZED"
  val NotFoundCode = "NOT_FOUND"
  val ForbiddenCode = "FORBIDDEN"
  val MethodNotAllowedCode = "METHOD_NOT_ALLOWED"
  val NotAcceptableCode = "ACCEPT_HEADER_INVALID"
  val UnsupportedMediaTypeCode = "UNSUPPORTED_MEDIA_TYPE"
  // 5XX
  val InternalServerErrorCode = "INTERNAL_SERVER_ERROR"
  val NotImplemented = "NOT_IMPLEMENTED"
  val BadGateway = "BAD_GATEWAY"
  val ServiceUnavailable = "SERVICE_UNAVAILABLE"
}

case class ResponseContents(code: String, message: String)

object ResponseContents {
  implicit val writes: Writes[ResponseContents] = Json.writes[ResponseContents]
}

case class ErrorResponse(httpStatusCode: Int, errorCode: String, message: String, content: ResponseContents*) extends Error {
  private lazy val errorContent = JsObject(Seq(
    "code" -> JsString(errorCode),
    "message" -> JsString(message)))

  private lazy val responseJson: JsValue = content match {
    case Seq() => errorContent
    case _ => errorContent + ("errors" -> Json.toJson(content))
  }

  lazy val JsonResult: Result = Status(httpStatusCode)(responseJson).as(ContentTypes.JSON)
  lazy val XmlResult: Result = Status(httpStatusCode)(responseXml).as(ContentTypes.XML)

  private lazy val responseXml: String = "<?xml version='1.0' encoding='UTF-8'?>\n" +
    <errorResponse>
      <code>{errorCode}</code>
      <message>{message}</message>
      {errors}
    </errorResponse>

  private val errors =
    if (content.nonEmpty) {
      <errors>
        {content.map(c =>
        <error>
          <code>{c.code}</code>
          <message>{c.message}</message>
        </error>)}
      </errors>
    }
    else {
      NodeSeq.Empty
    }

  def withErrors(contents: ResponseContents*): ErrorResponse = {
    ErrorResponse(this.httpStatusCode, this.errorCode, this.message, contents :_ *)
  }
}

object ErrorResponse extends HttpStatusCodeShortDescriptions {

  val ErrorUnauthorized = ErrorResponse(UNAUTHORIZED, UnauthorizedCode, "Bearer token is missing or not authorized")

  def errorBadRequest(errorMessage: String, errorCode: String = BadRequestCode): ErrorResponse =
    ErrorResponse(BAD_REQUEST, errorCode, errorMessage)

  val ErrorGenericBadRequest: ErrorResponse = errorBadRequest("Bad Request")

  val ErrorInvalidPayload: ErrorResponse = errorBadRequest("Invalid payload")

  val ErrorNotFound = ErrorResponse(NOT_FOUND, NotFoundCode, "Resource was not found")

  val ErrorAcceptHeaderInvalid = ErrorResponse(NOT_ACCEPTABLE, NotAcceptableCode, "The accept header is missing or invalid")

  val ErrorContentTypeHeaderInvalid = ErrorResponse(UNSUPPORTED_MEDIA_TYPE, UnsupportedMediaTypeCode, "The content type header is missing or invalid")

  def errorInternalServerError(errorMessage: String): ErrorResponse =
    ErrorResponse(INTERNAL_SERVER_ERROR, InternalServerErrorCode, errorMessage)

  val ErrorInternalServerError: ErrorResponse = errorInternalServerError("Internal server error")

}
