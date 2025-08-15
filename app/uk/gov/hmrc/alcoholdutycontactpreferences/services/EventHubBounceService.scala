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
import play.api.Logging
import play.api.http.Status.BAD_REQUEST
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.alcoholdutycontactpreferences.config.AppConfig
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubmitPreferencesConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{EventDetails, PaperlessPreferenceSubmission, PaperlessPreferenceSubmittedResponse}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventHubBounceService @Inject() (
  submitPreferencesConnector: SubmitPreferencesConnector,
  appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  def handleBouncedEmail(
    eventDetails: EventDetails
  )(implicit hc: HeaderCarrier): EitherT[Future, ErrorResponse, PaperlessPreferenceSubmittedResponse] =
    getAppaIdFromEnrolmentString(eventDetails.enrolment) match {
      case Right(appaId) =>
        val contactPreferenceSubmission = PaperlessPreferenceSubmission(
          paperlessPreference = false,
          emailAddress = Some(eventDetails.emailAddress),
          emailVerification = Some(true),
          bouncedEmail = Some(true)
        )
        submitPreferencesConnector.submitContactPreferences(contactPreferenceSubmission, appaId)
      case Left(error)   => EitherT.leftT(error)
    }

  private def getAppaIdFromEnrolmentString(enrolment: String): Either[ErrorResponse, String] = {
    val requiredPrefix = appConfig.enrolmentServiceName + "~" + appConfig.enrolmentIdentifierKey + "~"
    if (!enrolment.startsWith(requiredPrefix)) {
      logger.warn("Invalid format for enrolment in bounced email event")
      Left(ErrorResponse(BAD_REQUEST, "Invalid format for enrolment in bounced email event"))
    } else {
      val appaId = enrolment.stripPrefix(requiredPrefix)
      if (!appaId.matches("[A-Z]{5}\\d{10}")) {
        logger.warn("Invalid format for APPA ID in bounced email event")
        Left(ErrorResponse(BAD_REQUEST, "Invalid format for APPA ID in bounced email event"))
      } else {
        Right(appaId)
      }
    }
  }

}
