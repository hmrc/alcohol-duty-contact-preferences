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
import org.mockito.ArgumentMatchersSugar.eqTo
import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubmitPreferencesConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{ErrorCodes, PaperlessPreferenceSubmittedResponse}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.Future

class EventHubBounceServiceSpec extends SpecBase {
  val mockSubmitPreferencesConnector: SubmitPreferencesConnector = mock[SubmitPreferencesConnector]

  val service = new EventHubBounceService(mockSubmitPreferencesConnector, appConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubmitPreferencesConnector)
  }

  "handleBouncedEmail must" - {
    "return the submission response when the user's contact preferences are updated successfully" in {
      when(mockSubmitPreferencesConnector.submitContactPreferences(any(), any())(any()))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      whenReady(service.handleBouncedEmail(emailBouncedEventDetails).value) { result =>
        result mustBe Right(testSubmissionResponse)

        verify(mockSubmitPreferencesConnector, times(1))
          .submitContactPreferences(eqTo(contactPreferenceSubmissionBouncedEmail), eqTo(appaId))(any())
      }
    }

    "return an ErrorReponse when there is an error submitting contact preferences" in {
      when(mockSubmitPreferencesConnector.submitContactPreferences(any(), any())(any()))
        .thenReturn(EitherT.leftT[Future, PaperlessPreferenceSubmittedResponse](ErrorCodes.unexpectedResponse))

      whenReady(service.handleBouncedEmail(emailBouncedEventDetails).value) { result =>
        result mustBe Left(ErrorCodes.unexpectedResponse)

        verify(mockSubmitPreferencesConnector, times(1))
          .submitContactPreferences(eqTo(contactPreferenceSubmissionBouncedEmail), eqTo(appaId))(any())
      }
    }

    "return an ErrorReponse when the enrolment field does not start with HMRC-AD-ORG~APPAID~" in {
      when(mockSubmitPreferencesConnector.submitContactPreferences(any(), any())(any()))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      val eventDetails = emailBouncedEventDetails.copy(enrolment = "invalid")

      whenReady(service.handleBouncedEmail(eventDetails).value) { result =>
        result mustBe Left(ErrorResponse(BAD_REQUEST, "Invalid format for enrolment in bounced email event"))

        verify(mockSubmitPreferencesConnector, times(0)).submitContactPreferences(any(), any())(any())
      }
    }

    "return an ErrorReponse when the enrolment field does not contain an APPA ID in the correct format" in {
      when(mockSubmitPreferencesConnector.submitContactPreferences(any(), any())(any()))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      val eventDetails = emailBouncedEventDetails.copy(enrolment = "HMRC-AD-ORG~APPAID~A12345")

      whenReady(service.handleBouncedEmail(eventDetails).value) { result =>
        result mustBe Left(ErrorResponse(BAD_REQUEST, "Invalid format for APPA ID in bounced email event"))

        verify(mockSubmitPreferencesConnector, times(0)).submitContactPreferences(any(), any())(any())
      }
    }
  }
}
