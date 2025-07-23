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

package uk.gov.hmrc.alcoholdutycontactpreferences.services

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.alcoholdutycontactpreferences.connectors.SubmitPreferencesConnector
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{PaperlessPreferenceSubmission, PaperlessPreferenceSubmittedResponse}
import uk.gov.hmrc.alcoholdutycontactpreferences.repositories.UserAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitPreferencesService @Inject() (
  submitPreferencesConnector: SubmitPreferencesConnector,
  userAnswersRepository: UserAnswersRepository
)(implicit
  ec: ExecutionContext
) extends Logging {

  def submitContactPreferences(contactPreferenceSubmission: PaperlessPreferenceSubmission, appaId: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, ErrorResponse, PaperlessPreferenceSubmittedResponse] = {
    val submissionResponseEither =
      submitPreferencesConnector.submitContactPreferences(contactPreferenceSubmission, appaId)
    EitherT(
      submissionResponseEither.value.flatMap {
        case Right(submissionResponse) =>
          for {
            _ <- userAnswersRepository.clearUserAnswersById(appaId)
          } yield Right(submissionResponse)
        case Left(errorResponse)       =>
          Future.successful(Left(errorResponse))
      }
    )
  }
}
