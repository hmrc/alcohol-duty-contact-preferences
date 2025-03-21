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

import cats.data.EitherT
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubscriptionConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.repositories.{UpdateFailure, UpdateSuccess, UserAnswersRepository}
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.concurrent.Future

class UserAnswersControllerSpec extends SpecBase {
  val mockUserAnswersRepository: UserAnswersRepository = mock[UserAnswersRepository]
  val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  val controller = new UserAnswersController(
    cc,
    mockUserAnswersRepository,
    mockSubscriptionConnector,
    fakeAuthorisedAction,
    fakeCheckAppaIdAction,
    clock
  )

  "getUserAnswers must" - {
    "return 200 OK with an existing user answers when there is one for the id" in {
      when(mockUserAnswersRepository.get(ArgumentMatchers.eq(appaId)))
        .thenReturn(Future.successful(Some(emptyUserAnswers)))

      val result: Future[Result] =
        controller.getUserAnswers(appaId)(fakeRequest)

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(decryptedEmptyUA)
    }

    "return 404 NOT_FOUND when there is no user answers for the id" in {
      when(mockUserAnswersRepository.get(ArgumentMatchers.eq(appaId)))
        .thenReturn(Future.successful(None))

      val result: Future[Result] =
        controller.getUserAnswers(appaId)(fakeRequest)

      status(result) mustBe NOT_FOUND
    }
  }

  "set must" - {
    "return 200 OK with the user answers that was updated" in {
      when(mockUserAnswersRepository.set(any())).thenReturn(Future.successful(UpdateSuccess))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(decryptedEmptyUA))
        )

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(decryptedEmptyUA)
    }

    "return 404 Not Found if the repository returns an error" in {
      when(mockUserAnswersRepository.set(any())).thenReturn(Future.successful(UpdateFailure))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(decryptedEmptyUA))
        )

      status(result) mustBe NOT_FOUND
    }
  }

  "createUserAnswers must" - {
    "return 201 CREATED with the user answers that was created" in {
      when(mockUserAnswersRepository.add(any())).thenReturn(Future.successful(userAnswers))
      when(mockSubscriptionConnector.getSubscriptionContactPreferences(eqTo(appaId))(any()))
        .thenReturn(EitherT.rightT(contactPreferencesEmailSelected))

      val result: Future[Result] =
        controller.createUserAnswers()(
          fakeRequestWithJsonBody(Json.toJson(userDetails))
        )

      status(result)        mustBe CREATED
      contentAsJson(result) mustBe Json.toJson(decryptedUA)
    }

    Seq(
      ("NotFound", ErrorResponse(NOT_FOUND, "Subscription summary not found")),
      ("BadRequest", ErrorResponse(BAD_REQUEST, "Bad request")),
      (
        "InternalServerError (unable to parse)",
        ErrorResponse(INTERNAL_SERVER_ERROR, "Unable to parse subscription summary success")
      ),
      ("InternalServerError (other error)", ErrorResponse(INTERNAL_SERVER_ERROR, "An error occurred"))
    ).foreach { case (errorName, errorResponse) =>
      s"return status ${errorResponse.statusCode} if the subscription connector returns the error $errorName when getting the subscription summary" in {
        when(mockUserAnswersRepository.add(any())).thenReturn(Future.successful(userAnswers))
        when(mockSubscriptionConnector.getSubscriptionContactPreferences(eqTo(appaId))(any()))
          .thenReturn(EitherT.leftT(errorResponse))

        val result: Future[Result] =
          controller.createUserAnswers()(
            fakeRequestWithJsonBody(Json.toJson(userDetails))
          )

        status(result)        mustBe errorResponse.statusCode
        contentAsJson(result) mustBe Json.toJson(errorResponse)
      }
    }
  }
}
