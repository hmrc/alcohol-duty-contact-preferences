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

package uk.gov.hmrc.alcoholdutycontactpreferences.connectors

import cats.data.EitherT
import org.apache.pekko.actor.{ActorSystem, Scheduler}
import org.apache.pekko.pattern.retry
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutycontactpreferences.config.{AppConfig, CircuitBreakerProvider}
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.helpers.HIPHeaders
import uk.gov.hmrc.alcoholdutycontactpreferences.models._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class SubmitPreferencesConnector @Inject() (
  config: AppConfig,
  headers: HIPHeaders,
  circuitBreakerProvider: CircuitBreakerProvider,
  implicit val system: ActorSystem,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  implicit val scheduler: Scheduler = system.scheduler

  def submitContactPreferences(contactPreferenceSubmission: PaperlessPreferenceSubmission, appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, PaperlessPreferenceSubmittedResponse] =
    EitherT(
      retry(
        () => submitCall(contactPreferenceSubmission, appaId),
        attempts = config.retryAttemptsPost,
        delay = config.retryAttemptsDelay
      ).recoverWith { _ =>
        Future.successful(Left(ErrorCodes.unexpectedResponse))
      }
    )

  private def submitCall(contactPreferenceSubmission: PaperlessPreferenceSubmission, appaId: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[ErrorResponse, PaperlessPreferenceSubmittedResponse]] =
    circuitBreakerProvider.get().withCircuitBreaker {
      logger.info(s"Submitting contact preferences for appaId $appaId")
      httpClient
        .put(url"${config.submitPreferencesUrl(appaId)}")
        .setHeader(headers.submissionHeaders(): _*)
        .withBody(Json.toJson(contactPreferenceSubmission))
        .execute[HttpResponse]
        .flatMap {
          case response if response.status == OK                   =>
            Try(response.json.as[PaperlessPreferenceSubmittedSuccess]) match {
              case Success(submissionResponse) =>
                logger.info(s"Contact preferences submitted successfully for appaId $appaId")
                Future.successful(Right(submissionResponse.success))
              case Failure(_)                  =>
                logger.warn(s"Parsing failed for submission response for appaId $appaId")
                Future.successful(Left(ErrorCodes.unexpectedResponse))
            }
          case response if response.status == BAD_REQUEST          =>
            logger.warn(s"Bad request returned for contact preference submission for appaId $appaId")
            Future.successful(Left(ErrorCodes.badRequest))
          case response if response.status == NOT_FOUND            =>
            logger.warn(s"Not found returned for contact preference submission for appaId $appaId")
            Future.successful(Left(ErrorCodes.entityNotFound))
          case response if response.status == UNPROCESSABLE_ENTITY =>
            logger.warn(s"Unprocessable entity returned for contact preference submission for appaId $appaId")
            Future.successful(Left(ErrorCodes.invalidJson))
          case response                                            =>
            logger.warn(
              s"Received unexpected response from contact preference submission API (appaId $appaId). Status: ${response.status}"
            )
            Future.failed(new InternalServerException(response.body))
        }
    }
}
