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
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubmitPreferencesConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{ErrorCodes, PaperlessPreferenceSubmittedResponse}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

// For Scala3
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.when

import scala.concurrent.Future

class SubmitPreferencesControllerSpec extends SpecBase {
  val mockSubmitPreferencesConnector: SubmitPreferencesConnector = mock[SubmitPreferencesConnector]

  val controller = new SubmitPreferencesController(
    cc,
    mockSubmitPreferencesConnector,
    fakeAuthorisedAction,
    fakeCheckAppaIdAction
  )

  "submitContactPreferences must" - {
    "return 200 OK and the submission response when successful" in {
      when(
        mockSubmitPreferencesConnector
          .submitContactPreferences(eqTo(contactPreferenceSubmissionEmail), eqTo(appaId))(any())
      ).thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      val result: Future[Result] =
        controller.submitContactPreferences(appaId)(
          fakeRequestWithJsonBody(Json.toJson(contactPreferenceSubmissionEmail))
        )

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(testSubmissionResponse)
    }

    "return 422 UNPROCESSABLE_ENTITY when the submission response could not be parsed" in {
      when(
        mockSubmitPreferencesConnector
          .submitContactPreferences(eqTo(contactPreferenceSubmissionEmail), eqTo(appaId))(any())
      ).thenReturn(EitherT.leftT[Future, PaperlessPreferenceSubmittedResponse](ErrorCodes.invalidJson))

      val result: Future[Result] =
        controller.submitContactPreferences(appaId)(
          fakeRequestWithJsonBody(Json.toJson(contactPreferenceSubmissionEmail))
        )

      status(result) mustBe UNPROCESSABLE_ENTITY
    }

    "return 400 BAD_REQUEST when there is a BAD_REQUEST" in {
      when(
        mockSubmitPreferencesConnector
          .submitContactPreferences(eqTo(contactPreferenceSubmissionEmail), eqTo(appaId))(any())
      ).thenReturn(EitherT.leftT[Future, PaperlessPreferenceSubmittedResponse](ErrorCodes.badRequest))

      val result: Future[Result] =
        controller.submitContactPreferences(appaId)(
          fakeRequestWithJsonBody(Json.toJson(contactPreferenceSubmissionEmail))
        )

      status(result) mustBe BAD_REQUEST
    }

    "return 404 NOT_FOUND when not found" in {
      when(
        mockSubmitPreferencesConnector
          .submitContactPreferences(eqTo(contactPreferenceSubmissionEmail), eqTo(appaId))(any())
      ).thenReturn(EitherT.leftT[Future, PaperlessPreferenceSubmittedResponse](ErrorCodes.entityNotFound))

      val result: Future[Result] =
        controller.submitContactPreferences(appaId)(
          fakeRequestWithJsonBody(Json.toJson(contactPreferenceSubmissionEmail))
        )

      status(result) mustBe NOT_FOUND
    }

    "return 500 INTERNAL_SERVER_ERROR when there is an unexpected response" in {
      when(
        mockSubmitPreferencesConnector
          .submitContactPreferences(eqTo(contactPreferenceSubmissionEmail), eqTo(appaId))(any())
      ).thenReturn(EitherT.leftT[Future, PaperlessPreferenceSubmittedResponse](ErrorCodes.unexpectedResponse))

      val result: Future[Result] =
        controller.submitContactPreferences(appaId)(
          fakeRequestWithJsonBody(Json.toJson(contactPreferenceSubmissionEmail))
        )

      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}
