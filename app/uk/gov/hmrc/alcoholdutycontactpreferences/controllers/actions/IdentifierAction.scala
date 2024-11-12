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

package uk.gov.hmrc.alcoholdutycontactpreferences.controllers.actions

import com.google.inject.Inject
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.alcoholdutycontactpreferences.config.AppConfig
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, groupIdentifier, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: AppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  private def predicate: Predicate =
    AuthProviders(GovernmentGateway) and
      Enrolment(config.enrolmentServiceName) and
      CredentialStrength(strong) and
      Organisation and
      ConfidenceLevel.L50

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(predicate).retrieve(internalId and groupIdentifier and allEnrolments) {
      case optInternalId ~ optGroupId ~ enrolments =>
        val internalId: String = getOrElseFailWithUnauthorised(optInternalId, "Unable to retrieve internalId")
        val groupId: String = getOrElseFailWithUnauthorised(optGroupId, "Unable to retrieve groupIdentifier")
        val appaId = getAppaId(enrolments)
        block(IdentifierRequest(request, appaId, groupId, internalId))
    } recover {
      case e: AuthorisationException =>
        logger.debug("Got AuthorisationException:", e)
        Unauthorized(
          Json.toJson(
            ErrorResponse(
              UNAUTHORIZED,
              e.reason
            )
          )
        )
      case e: UnauthorizedException =>
        logger.debug("Got UnauthorizedException:", e)
        Unauthorized(
          Json.toJson(
            ErrorResponse(
              UNAUTHORIZED,
              e.message
            )
          )
        )
    }
  }

  private def getAppaId(enrolments: Enrolments): String = {
    val adrEnrolments: Enrolment  = getOrElseFailWithUnauthorised(
      enrolments.enrolments.find(_.key == config.enrolmentServiceName),
      s"Unable to retrieve enrolment: ${config.enrolmentServiceName}"
    )
    val key = config.enrolmentIdentifierKey

    val appaIdOpt: Option[String] =
      adrEnrolments.getIdentifier(config.enrolmentIdentifierKey).map(_.value)
    getOrElseFailWithUnauthorised(appaIdOpt, s"Unable to retrieve $key from enrolments")
  }

  private def getOrElseFailWithUnauthorised[T](maybeAppId: Option[T], failureMessage: String): T =
    maybeAppId.getOrElse {
      logger.warn(s"Authorised Action failed with error: $failureMessage")
      throw new UnauthorizedException(failureMessage)
    }

}
