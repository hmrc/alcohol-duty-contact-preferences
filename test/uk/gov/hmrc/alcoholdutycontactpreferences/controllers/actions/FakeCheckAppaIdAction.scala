/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc._
import uk.gov.hmrc.alcoholdutycontactpreferences.models.requests.IdentifierRequest

import scala.concurrent.{ExecutionContext, Future}

class FakeCheckAppaIdActionImpl private[actions] extends ActionRefiner[IdentifierRequest, IdentifierRequest] {
  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] =
    Future.successful(Right(request))

  override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}

class FakeCheckAppaIdAction extends CheckAppaIdAction()(scala.concurrent.ExecutionContext.Implicits.global) {
  override def apply(appaId: String): ActionRefiner[IdentifierRequest, IdentifierRequest] =
    new FakeCheckAppaIdActionImpl()
}