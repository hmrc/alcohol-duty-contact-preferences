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
import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubmitPreferencesConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.controllers.actions.{AuthorisedAction, CheckAppaIdAction}
import uk.gov.hmrc.alcoholdutycontactpreferences.models.PaperlessPreferenceSubmission
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.ExecutionContext

class SubmitPreferencesController @Inject() (
  cc: ControllerComponents,
  submitPreferencesConnector: SubmitPreferencesConnector,
  authorise: AuthorisedAction,
  checkAppaId: CheckAppaIdAction
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def submitContactPreferences(appaId: String): Action[JsValue] =
    (authorise(parse.json) andThen checkAppaId(appaId)).async { implicit request =>
      withJsonBody[PaperlessPreferenceSubmission] { contactPreferenceSubmission =>
        submitPreferencesConnector
          .submitContactPreferences(contactPreferenceSubmission, appaId)
          .fold(
            e => error(e),
            submissionResponse => Ok(Json.toJson(submissionResponse))
          )
      }
    }

  def error(errorResponse: ErrorResponse): Result = Result(
    header = ResponseHeader(errorResponse.statusCode),
    body = HttpEntity.Strict(ByteString(Json.toBytes(Json.toJson(errorResponse))), Some("application/json"))
  )
}
