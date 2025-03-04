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

package uk.gov.hmrc.alcoholdutycontactpreferences.testOnly.controllers

import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, BaseController, ControllerComponents}
import uk.gov.hmrc.alcoholdutycontactpreferences.models.Event

import javax.inject.Inject
import scala.concurrent.Future

class EventHubBounceController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController
    with Logging {

  def handleBouncedEmail(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body
      .validate[Event]
      .fold(
        invalid = _ => {
          logger.warn("Bounced email json body could not be parsed as Event")
          Future.successful(InternalServerError("Bounced email json body could not be parsed as Event"))
        },
        valid = event => {
          logger.info(
            s"Bounced email received successfully. eventId: ${event.eventId}," +
              s" subject: ${event.subject}, groupId: ${event.groupId}, timestamp: ${event.timestamp}, event: ${event.event}"
          )
          Future.successful(Ok("Bounced email received successfully"))
        }
      )
  }

}
