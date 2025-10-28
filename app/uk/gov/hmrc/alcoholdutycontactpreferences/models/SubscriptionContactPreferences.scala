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

import play.api.libs.json.*

final case class SubscriptionSummarySuccess(success: SubscriptionContactPreferences)

object SubscriptionSummarySuccess {
  implicit val subscriptionSummarySuccessFormat: OFormat[SubscriptionSummarySuccess] =
    Json.format[SubscriptionSummarySuccess]
}

final case class SubscriptionContactPreferences(
  paperlessReference: Boolean,
  emailAddress: Option[String],
  emailVerificationFlag: Option[Boolean],
  bouncedEmailFlag: Option[Boolean],
  addressLine1: Option[String],
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  postcode: Option[String],
  country: Option[String]
)

object SubscriptionContactPreferences {
  import JsonHelpers.{booleanReads, booleanWrites}

  implicit val subscriptionContactPreferencesFormat: OFormat[SubscriptionContactPreferences] =
    Json.format[SubscriptionContactPreferences]
}
