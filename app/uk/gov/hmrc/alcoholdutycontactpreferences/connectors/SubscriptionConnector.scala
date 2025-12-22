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
import org.apache.pekko.actor.{ActorSystem, Scheduler}
import org.apache.pekko.pattern.retry
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.alcoholdutycontactpreferences.config.{AppConfig, CircuitBreakerProvider}
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.helpers.HIPHeaders
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{ErrorCodes, SubscriptionContactPreferences, SubscriptionSummarySuccess}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class SubscriptionConnector @Inject() (
  config: AppConfig,
  headers: HIPHeaders,
  circuitBreakerProvider: CircuitBreakerProvider,
  implicit val system: ActorSystem,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  implicit val scheduler: Scheduler = system.scheduler

  def getSubscriptionContactPreferences(
    appaId: String
  )(implicit hc: HeaderCarrier): EitherT[Future, ErrorResponse, SubscriptionContactPreferences] =
    EitherT(
      retry(
        () => call(appaId),
        attempts = config.retryAttempts,
        delay = config.retryAttemptsDelay
      ).recoverWith { _ =>
        Future.successful(Left(ErrorCodes.unexpectedResponse))
      }
    )

  private def call(
    appaId: String
  )(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, SubscriptionContactPreferences]] =
    circuitBreakerProvider.get().withCircuitBreaker {
      logger.info(
        s"[SubscriptionConnector] [getSubscriptionContactPreferences] Fetching subscription summary for appaId $appaId"
      )
      httpClient
        .get(url"${config.getSubscriptionUrl(appaId)}")
        .setHeader(headers.subscriptionHeaders(): _*)
        .execute[HttpResponse]
        .flatMap { response =>
          response.status match {
            case OK                   =>
              Try {
                response.json.as[SubscriptionSummarySuccess]
              } match {
                case Success(doc) =>
                  logger.info(
                    s"[SubscriptionConnector] [getSubscriptionContactPreferences] Retrieved subscription summary success for appaId $appaId"
                  )
                  Future.successful(Right(doc.success))
                case Failure(_)   =>
                  logger.warn(
                    s"[SubscriptionConnector] [getSubscriptionContactPreferences] Unable to parse subscription summary success for appaId $appaId"
                  )
                  Future.successful(
                    Left(ErrorResponse(INTERNAL_SERVER_ERROR, "Unable to parse subscription summary success"))
                  )
              }
            case BAD_REQUEST          =>
              logger.warn(
                s"[SubscriptionConnector] [getSubscriptionContactPreferences] Bad request sent to get subscription for appaId $appaId"
              )
              Future.successful(Left(ErrorResponse(BAD_REQUEST, "Bad request")))
            case NOT_FOUND            =>
              logger.warn(
                s"[SubscriptionConnector] [getSubscriptionContactPreferences] No subscription summary found for appaId $appaId"
              )
              Future.successful(Left(ErrorResponse(NOT_FOUND, "Subscription summary not found")))
            case UNPROCESSABLE_ENTITY =>
              logger.warn(
                s"[SubscriptionConnector] [getSubscriptionContactPreferences] Subscription summary request unprocessable for appaId $appaId"
              )
              Future.successful(Left(ErrorResponse(UNPROCESSABLE_ENTITY, "Unprocessable entity")))
            case _                    =>
              logger.warn(
                s"[SubscriptionConnector] [getSubscriptionContactPreferences] An error was returned while trying to fetch subscription summary for appaId $appaId"
              )
              Future.failed(new InternalServerException(response.body))
          }
        }
    }
}
