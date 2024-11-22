/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutycontactpreferences.models

import play.api.http.Status._
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

object ErrorCodes {
  val badRequest: ErrorResponse          = ErrorResponse(BAD_REQUEST, "Bad request made")
  val entityNotFound: ErrorResponse      = ErrorResponse(NOT_FOUND, "Entity not found")
  val invalidJson: ErrorResponse         = ErrorResponse(UNPROCESSABLE_ENTITY, "Invalid Json received")
  val unauthorisedRequest: ErrorResponse = ErrorResponse(UNAUTHORIZED, "Unauthorised request")
  val serviceUnavailable: ErrorResponse  = ErrorResponse(SERVICE_UNAVAILABLE, "Service unavailable")
  val unexpectedResponse: ErrorResponse  = ErrorResponse(INTERNAL_SERVER_ERROR, "Unexpected Response")
}