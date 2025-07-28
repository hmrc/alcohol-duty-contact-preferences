/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.alcoholdutycontactpreferences.controllers

import com.google.inject.Inject
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.EmailVerificationConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.controllers.actions.AuthorisedAction
import uk.gov.hmrc.alcoholdutycontactpreferences.models.GetVerificationStatusResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.ExecutionContext

class EmailVerificationController @Inject() (
  cc: ControllerComponents,
  emailVerificationConnector: EmailVerificationConnector,
  authorise: AuthorisedAction
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getEmailVerification(credId: String): Action[AnyContent] = authorise.async { implicit request =>
    emailVerificationConnector.getEmailVerification(credId).value.map {
      case Left(errorResponse: ErrorResponse)                    => InternalServerError(s"Error: ${errorResponse.message}")
      case Right(successResponse: GetVerificationStatusResponse) => Ok(Json.toJson(successResponse))
    }
  }

}
