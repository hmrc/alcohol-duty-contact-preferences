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

package uk.gov.hmrc.alcoholdutycontactpreferences.services

import cats.data.EitherT
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubmitPreferencesConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{ErrorCodes, PaperlessPreferenceSubmittedResponse}
import uk.gov.hmrc.alcoholdutycontactpreferences.repositories.UserAnswersRepository
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.concurrent.Future

class SubmitPreferencesServiceSpec extends SpecBase {
  "SubmitPreferencesService must" - {
    "submit contact preferences, clear user answers and return the submission response" in new SetUp {
      when(mockSubmitPreferencesConnector.submitContactPreferences(contactPreferenceSubmissionEmail, appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      when(mockUserAnswersRespository.clearUserAnswersById(appaId)).thenReturn(Future.unit)

      whenReady(submitPreferencesService.submitContactPreferences(contactPreferenceSubmissionEmail, appaId).value) {
        _ mustBe Right(testSubmissionResponse)
      }

      verify(mockSubmitPreferencesConnector).submitContactPreferences(contactPreferenceSubmissionEmail, appaId)
      verify(mockUserAnswersRespository).clearUserAnswersById(appaId)
    }

    "return an error from the connector if submission failed" in new SetUp {
      when(mockSubmitPreferencesConnector.submitContactPreferences(contactPreferenceSubmissionEmail, appaId))
        .thenReturn(EitherT.leftT[Future, PaperlessPreferenceSubmittedResponse](ErrorCodes.entityNotFound))

      when(mockUserAnswersRespository.clearUserAnswersById(appaId)).thenReturn(Future.unit)

      whenReady(submitPreferencesService.submitContactPreferences(contactPreferenceSubmissionEmail, appaId).value) {
        _ mustBe Left(ErrorCodes.entityNotFound)
      }

      verify(mockSubmitPreferencesConnector).submitContactPreferences(contactPreferenceSubmissionEmail, appaId)
      verify(mockUserAnswersRespository, never).clearUserAnswersById(any())
    }

    "return a failed future if the returns connector fails" in new SetUp {
      when(mockSubmitPreferencesConnector.submitContactPreferences(contactPreferenceSubmissionEmail, appaId))
        .thenReturn(EitherT.left[PaperlessPreferenceSubmittedResponse](Future.failed(new RuntimeException("Fail!"))))

      when(mockUserAnswersRespository.clearUserAnswersById(appaId)).thenReturn(Future.unit)

      whenReady(
        submitPreferencesService.submitContactPreferences(contactPreferenceSubmissionEmail, appaId).value.failed
      ) {
        _ mustBe a[RuntimeException]
      }

      verify(mockSubmitPreferencesConnector).submitContactPreferences(contactPreferenceSubmissionEmail, appaId)
      verify(mockUserAnswersRespository, never).clearUserAnswersById(any())
    }

    "return any error if the clearing user answers returns a failed future" in new SetUp {
      when(mockSubmitPreferencesConnector.submitContactPreferences(contactPreferenceSubmissionEmail, appaId))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      when(mockUserAnswersRespository.clearUserAnswersById(appaId))
        .thenReturn(Future.failed(new RuntimeException("Fail!")))

      whenReady(
        submitPreferencesService.submitContactPreferences(contactPreferenceSubmissionEmail, appaId).value.failed
      ) {
        _ mustBe a[RuntimeException]
      }

      verify(mockSubmitPreferencesConnector).submitContactPreferences(contactPreferenceSubmissionEmail, appaId)
      verify(mockUserAnswersRespository).clearUserAnswersById(appaId)
    }
  }

  class SetUp {
    val mockSubmitPreferencesConnector = mock[SubmitPreferencesConnector]
    val mockUserAnswersRespository     = mock[UserAnswersRepository]

    val submitPreferencesService = new SubmitPreferencesService(
      mockSubmitPreferencesConnector,
      mockUserAnswersRespository
    )
  }
}
