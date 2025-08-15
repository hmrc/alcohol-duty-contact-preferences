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

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.alcoholdutycontactpreferences.base.ISpecBase

class EventHubBounceIntegrationSpec extends ISpecBase {
  val submitPreferencesUrl = config.submitPreferencesUrl(appaId)

  "The event hub bounced email endpoint must" - {
    "return 200 OK and the submission response when successful" in {
      stubPut(
        submitPreferencesUrl,
        OK,
        Json.toJson(contactPreferenceSubmissionBouncedEmail).toString,
        Json.toJson(testSubmissionSuccess).toString
      )

      val response = callRoute(
        FakeRequest("POST", routes.EventHubBounceController.handleBouncedEmail().url)
          .withBody(Json.toJson(emailBouncedEvent))
      )

      status(response)        mustBe OK
      contentAsJson(response) mustBe Json.toJson(testSubmissionResponse)

      verifyPut(submitPreferencesUrl)
    }

    "return 500 INTERNAL_SERVER_ERROR if the request body cannot be parsed" in {
      val response = callRoute(
        FakeRequest("POST", routes.EventHubBounceController.handleBouncedEmail().url)
          .withBody(Json.toJson("invalid"))
      )

      status(response) mustBe INTERNAL_SERVER_ERROR
    }

    "return 400 BAD_REQUEST if the enrolment field does not start with HMRC-AD-ORG~APPAID~" in {
      val event = emailBouncedEvent.copy(event = emailBouncedEventDetails.copy(enrolment = "invalid"))

      val response = callRoute(
        FakeRequest("POST", routes.EventHubBounceController.handleBouncedEmail().url)
          .withBody(Json.toJson(event))
      )

      status(response) mustBe BAD_REQUEST
    }

    "return 400 BAD_REQUEST if the enrolment field does not contain an APPA ID in the correct format" in {
      val event = emailBouncedEvent.copy(event = emailBouncedEventDetails.copy(enrolment = "HMRC-AD-ORG~APPAID~A12345"))

      val response = callRoute(
        FakeRequest("POST", routes.EventHubBounceController.handleBouncedEmail().url)
          .withBody(Json.toJson(event))
      )

      status(response) mustBe BAD_REQUEST
    }

    "return 500 INTERNAL_SERVER_ERROR if the submission response cannot be parsed" in {
      stubPut(
        submitPreferencesUrl,
        OK,
        Json.toJson(contactPreferenceSubmissionBouncedEmail).toString,
        "invalid"
      )

      val response = callRoute(
        FakeRequest("POST", routes.EventHubBounceController.handleBouncedEmail().url)
          .withBody(Json.toJson(emailBouncedEvent))
      )

      status(response) mustBe INTERNAL_SERVER_ERROR
      verifyPut(submitPreferencesUrl)
    }

    Seq(BAD_REQUEST, NOT_FOUND, UNPROCESSABLE_ENTITY).foreach { statusCode =>
      s"return $statusCode if the submit preferences API returns this error code" in {
        stubPut(
          submitPreferencesUrl,
          statusCode,
          Json.toJson(contactPreferenceSubmissionBouncedEmail).toString,
          ""
        )

        val response = callRoute(
          FakeRequest("POST", routes.EventHubBounceController.handleBouncedEmail().url)
            .withBody(Json.toJson(emailBouncedEvent))
        )

        status(response) mustBe statusCode
        verifyPut(submitPreferencesUrl)
      }
    }

    "return 500 INTERNAL_SERVER_ERROR if the submit preferences API returns another error" in {
      stubPut(
        submitPreferencesUrl,
        BAD_GATEWAY,
        Json.toJson(contactPreferenceSubmissionBouncedEmail).toString,
        ""
      )

      val response = callRoute(
        FakeRequest("POST", routes.EventHubBounceController.handleBouncedEmail().url)
          .withBody(Json.toJson(emailBouncedEvent))
      )

      status(response) mustBe INTERNAL_SERVER_ERROR
      verifyPut(submitPreferencesUrl)
    }
  }
}
