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
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{ReturnAndUserDetails, ReturnId, UserAnswers}
import uk.gov.hmrc.alcoholdutycontactpreferences.repositories.SensitiveUserAnswersRepository
import uk.gov.hmrc.crypto.Sensitive
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class UserAnswersController @Inject() (
  cc: ControllerComponents,
  sensitiveUserAnswersRepository: SensitiveUserAnswersRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def createUserAnswers(): Action[AnyContent] = Action.async { implicit request =>
    val testReturnAndUserDetails: ReturnAndUserDetails =
      ReturnAndUserDetails(testReturnId, groupId = "test1", userId = "test2")

    val userAnswers: UserAnswers = UserAnswers.createUserAnswers(
      returnAndUserDetails = testReturnAndUserDetails
//      sensitiveUserInformation = sensitiveUserInformation
    )

    println("AAAAAAAAAA")
    sensitiveUserAnswersRepository.add(userAnswers).map(_ => Ok("CreateUserAnswersSuccess"))
  }

  def getUserAnswers(): Action[AnyContent] = Action.async { implicit request =>
    sensitiveUserAnswersRepository.get(testReturnId).map { result =>
      println(s"QQQQQQ + ${result.get.sensitiveString.decryptedValue}")
      Ok("get user answers was successful")
    }
  }

  private val testReturnId =
    ReturnId(appaId = "1234567890", periodKey = "25AA")

//  val sensitiveUserInformation = SensitiveUserInformation(
//    paperlessReference = false,
//    withEmail = true,
//    emailVerification = None,
//    bouncedEmail = None,
//    emailAddress = Some(Sensitive.SensitiveString("test123"))
//  )

}
