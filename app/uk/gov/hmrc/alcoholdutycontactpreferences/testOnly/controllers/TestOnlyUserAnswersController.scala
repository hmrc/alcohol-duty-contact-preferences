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

package uk.gov.hmrc.alcoholdutycontactpreferences.testOnly.controllers

import org.apache.pekko.util.ByteString
import play.api.Logging
import play.api.http.HttpEntity
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubscriptionConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.controllers.actions.{AuthorisedAction, CheckAppaIdAction}
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{DecryptedUA, UserAnswers, UserDetails}
import uk.gov.hmrc.alcoholdutycontactpreferences.repositories.UserAnswersRepository
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyUserAnswersController @Inject() (
  cc: ControllerComponents,
  userAnswersRepository: UserAnswersRepository,
  subscriptionConnector: SubscriptionConnector,
  authorise: AuthorisedAction,
  checkAppaId: CheckAppaIdAction,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def clearAllData: Action[AnyContent] = Action.async { _ =>
    for {
      _ <- userAnswersRepository.collection.drop().toFuture()
    } yield Ok("All data cleared")
  }

  def createAndFillUserAnswers(verified: Boolean, locked: Boolean): Action[JsValue] =
    authorise(parse.json).async { implicit request =>
      withJsonBody[UserDetails] { userDetails =>
        val appaId = userDetails.appaId

        checkAppaId(appaId).invokeBlock[JsValue](
          request,
          { implicit request =>
            val subscriptionContactPreferences = subscriptionConnector.getSubscriptionContactPreferences(appaId)
            subscriptionContactPreferences.foldF(
              err => {
                logger.warn(
                  s"Unable to get existing contact preferences for $appaId - status ${err.statusCode}"
                )
                Future.successful(error(err))
              },
              contactPreferences => {
                val emptyUserAnswers: UserAnswers = UserAnswers.createUserAnswers(
                  userDetails = userDetails,
                  contactPreferences = contactPreferences,
                  clock = clock
                )
                val submittedEmail                = (verified, locked) match {
                  case (true, false) => "john.doe@example.com"
                  case (true, true)  => "jane.doe2@example.com"
                  case _             => "jane.doe@example.com"
                }
                val userAnswers                   = emptyUserAnswers.copy(
                  emailAddress = Some(SensitiveString(submittedEmail)),
                  data = JsObject(Seq("contactPreferenceEmail" -> Json.toJson(true)))
                )
                userAnswersRepository.add(userAnswers).map(ua => Created(Json.toJson(DecryptedUA.fromUA(ua))))
              }
            )
          }
        )
      }
    }

  def error(errorResponse: ErrorResponse): Result = Result(
    header = ResponseHeader(errorResponse.statusCode),
    body = HttpEntity.Strict(ByteString(Json.toBytes(Json.toJson(errorResponse))), Some("application/json"))
  )
}
