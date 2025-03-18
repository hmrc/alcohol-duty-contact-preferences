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
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubscriptionConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.controllers.actions.{AuthorisedAction, CheckAppaIdAction}
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{DecryptedUA, ReturnAndUserDetails, UserAnswers}
import uk.gov.hmrc.alcoholdutycontactpreferences.repositories.SensitiveUserAnswersRepository
import uk.gov.hmrc.crypto.Sensitive
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersController @Inject() (
  cc: ControllerComponents,
  sensitiveUserAnswersRepository: SensitiveUserAnswersRepository,
  subscriptionConnector: SubscriptionConnector,
  authorise: AuthorisedAction,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with Logging {

  def createUserAnswers(): Action[AnyContent] = authorise.async { implicit request =>
    val testReturnAndUserDetails: ReturnAndUserDetails =
      ReturnAndUserDetails(appaId = request.appaId, userId = request.userId)

    val subscriptionContactPreferences = subscriptionConnector.getSubscriptionContactPreferences(request.appaId)
    subscriptionContactPreferences.foldF(
      err => {
        logger.warn(
          s"Unable to get existing contact preferences for ${request.appaId} - ${err.statusCode} ${err.message}"
        )
        Future.successful(error(err))
      },
      contactPreferences => {
        val userAnswers: UserAnswers = UserAnswers.createUserAnswers(
          returnAndUserDetails = testReturnAndUserDetails,
          contactPreferences = contactPreferences,
          clock = clock
        )

        println("AAAAAAAAAA")
        sensitiveUserAnswersRepository.add(userAnswers).map(ua => Created(Json.toJson(DecryptedUA.fromUA(ua))))
      }
    )
  }

  def getUserAnswers(appaId: String): Action[AnyContent] = authorise.async { implicit request =>

    sensitiveUserAnswersRepository.get(appaId).map { result =>
      println("QQQQQQ")
//      println(s"QQQQQQ + ${result.get.sensitiveUserInformation.emailAddress.get.decryptedValue}")
      result match {
        case Some(ua) => Ok(Json.toJson(DecryptedUA.fromUA(ua)))
        case None => NotFound
      }
    }
  }

//  val sensitiveUserInformation = SensitiveUserInformation(
//    paperlessReference = false,
//    withEmail = true,
//    emailVerification = None,
//    bouncedEmail = None,
//    emailAddress = Some(Sensitive.SensitiveString("test123"))
//  )

  def error(errorResponse: ErrorResponse): Result = Result(
    header = ResponseHeader(errorResponse.statusCode),
    body = HttpEntity.Strict(ByteString(Json.toBytes(Json.toJson(errorResponse))), Some("application/json"))
  )
}
