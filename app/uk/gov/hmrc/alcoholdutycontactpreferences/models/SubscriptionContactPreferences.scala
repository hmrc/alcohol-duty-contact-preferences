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

package uk.gov.hmrc.alcoholdutycontactpreferences.models

import play.api.libs.json._
import uk.gov.hmrc.alcoholdutycontactpreferences.models.JsonHelpers

final case class SubscriptionSummarySuccess(success: SubscriptionContactPreferences)

object SubscriptionSummarySuccess {
  implicit val subscriptionSummarySuccessFormat: OFormat[SubscriptionSummarySuccess] =
    Json.format[SubscriptionSummarySuccess]
}

final case class SubscriptionContactPreferences(
  paperlessReference: Boolean,
  emailAddress: Option[String],
  emailVerification: Option[Boolean],
  bouncedEmail: Option[Boolean]
)

object SubscriptionContactPreferences {
  import JsonHelpers.booleanReads
  import JsonHelpers.booleanWrites

  implicit val subscriptionContactPreferencesFormat: OFormat[SubscriptionContactPreferences] =
    Json.format[SubscriptionContactPreferences]
}
