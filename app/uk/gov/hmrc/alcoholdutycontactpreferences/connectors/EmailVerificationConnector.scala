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

package uk.gov.hmrc.alcoholdutycontactpreferences.connectors

import cats.data.EitherT
import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, UNAUTHORIZED}
import uk.gov.hmrc.alcoholdutycontactpreferences.config.AppConfig
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.helpers.HIPHeaders
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{GetVerificationStatusResponse, SubscriptionContactPreferences, SubscriptionSummarySuccess}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class EmailVerificationConnector @Inject() (
  config: AppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def getEmailVerification(
    credId: String
  )(implicit hc: HeaderCarrier): EitherT[Future, ErrorResponse, GetVerificationStatusResponse] =
    EitherT {
      logger.info(s"Fetching email verification list for credId $credId")

      httpClient
        .get(url"${config.getVerifiedEmailsUrl(credId)}")
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
        .map {
          case Right(response) =>
            Try {
              response.json
                .as[GetVerificationStatusResponse]
            } match {
              case Success(response) =>
                logger.info(s"Retrieved email records successfully for credId $credId")
                Right(response)
              case Failure(_)        =>
                logger.warn(s"Unable to parse email records successful response for credId $credId")
                Left(ErrorResponse(INTERNAL_SERVER_ERROR, "Unable to parse email records successful response"))
            }
          case Left(error)     =>
            error.statusCode match {
              case NOT_FOUND   =>
                // TODO: Check if this is in the correct place (or in success response)
                logger.info(s"There were no email address records for credId $credId. status: ${error.statusCode}")
                Right(GetVerificationStatusResponse(emails = List.empty))
              case BAD_REQUEST =>
                logger.warn(
                  s"Invalid request for email verification list for credId $credId. status: ${error.statusCode}"
                )
                Left(ErrorResponse(INTERNAL_SERVER_ERROR, "Invalid request for email verification list"))
              case _           =>
                logger.warn(
                  s"Unexpected response for email verification list for credId: $credId. status: ${error.statusCode}"
                )
                Left(ErrorResponse(INTERNAL_SERVER_ERROR, "Unexpected response for email verification list"))
            }
        }
        .recoverWith { case e: Exception =>
          logger.warn(s"An exception was returned while trying to fetch the email verification list for credId $credId")
          Future.successful(Left(ErrorResponse(INTERNAL_SERVER_ERROR, e.getMessage)))
        }
    }

}
