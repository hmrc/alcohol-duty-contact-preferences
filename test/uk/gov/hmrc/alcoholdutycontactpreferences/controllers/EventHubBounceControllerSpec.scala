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
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.alcoholdutycontactpreferences.models.PaperlessPreferenceSubmittedResponse
import uk.gov.hmrc.alcoholdutycontactpreferences.services.EventHubBounceService
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.Future

class EventHubBounceControllerSpec extends SpecBase {
  val mockEventHubBounceService: EventHubBounceService = mock[EventHubBounceService]

  val controller = new EventHubBounceController(cc, mockEventHubBounceService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEventHubBounceService)
  }

  "handleBouncedEmail must" - {
    "return 200 OK and the submission response when successful" in {
      when(mockEventHubBounceService.handleBouncedEmail(any())(any()))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      val result: Future[Result] =
        controller.handleBouncedEmail()(
          fakeRequestWithJsonBody(Json.toJson(emailBouncedEvent))
        )

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(testSubmissionResponse)

      verify(mockEventHubBounceService, times(1)).handleBouncedEmail(eqTo(emailBouncedEventDetails))(any())
    }

    "return 400 BAD_REQUEST when the request body cannot be parsed" in {
      when(mockEventHubBounceService.handleBouncedEmail(any())(any()))
        .thenReturn(EitherT.rightT[Future, ErrorResponse](testSubmissionResponse))

      val result: Future[Result] =
        controller.handleBouncedEmail()(
          fakeRequestWithJsonBody(Json.toJson("invalid"))
        )

      status(result)          mustBe BAD_REQUEST
      contentAsString(result) mustBe "Bounced email json body could not be parsed as EmailBouncedEvent"

      verify(mockEventHubBounceService, times(0)).handleBouncedEmail(any())(any())
    }

    "return another error from the service (400 BAD_REQUEST example)" in {
      when(mockEventHubBounceService.handleBouncedEmail(any())(any())).thenReturn(
        EitherT.leftT[Future, PaperlessPreferenceSubmittedResponse](ErrorResponse(BAD_REQUEST, "Error message"))
      )

      val result: Future[Result] =
        controller.handleBouncedEmail()(
          fakeRequestWithJsonBody(Json.toJson(emailBouncedEvent))
        )

      status(result)          mustBe BAD_REQUEST
      contentAsString(result) mustBe "Error updating contact preferences for bounced email event"

      verify(mockEventHubBounceService, times(1)).handleBouncedEmail(eqTo(emailBouncedEventDetails))(any())
    }
  }
}
