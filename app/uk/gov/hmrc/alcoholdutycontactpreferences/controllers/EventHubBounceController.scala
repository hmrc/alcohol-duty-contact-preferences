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

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.alcoholdutycontactpreferences.models.EmailBouncedEvent
import uk.gov.hmrc.alcoholdutycontactpreferences.services.EventHubBounceService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventHubBounceController @Inject() (
  cc: ControllerComponents,
  eventHubBounceService: EventHubBounceService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def handleBouncedEmail(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[EmailBouncedEvent]
      .fold(
        invalid = error => {
          logger.warn(s"Bounced email json body could not be parsed as EmailBouncedEvent")
          Future.successful(BadRequest("Bounced email json body could not be parsed as EmailBouncedEvent"))
        },
        valid = event => {
          logger.info(
            s"Bounced email event received. eventId: ${event.eventId}, subject: ${event.subject}, timestamp: ${event.timestamp}"
          )
          eventHubBounceService.handleBouncedEmail(event.event).value.map {
            case Right(submissionResponse) =>
              logger.info("Successfully updated contact preferences for bounced email event.")
              Ok(Json.toJson(submissionResponse))
            case Left(error)               =>
              logger.warn(
                s"Error updating contact preferences for bounced email event. eventID: ${event.eventId}, status: ${error.statusCode}"
              )
              Status(error.statusCode)("Error updating contact preferences for bounced email event")
          }
        }
      )
  }

}
