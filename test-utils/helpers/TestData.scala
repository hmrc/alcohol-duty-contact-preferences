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

package helpers

import generators.ModelGenerators
import play.api.libs.json.{JsObject, Json, OFormat}
import uk.gov.hmrc.alcoholdutycontactpreferences.models._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time._

trait TestData extends ModelGenerators {
  val clockMillis: Long = 1718118467838L
  val clock: Clock      = Clock.fixed(Instant.ofEpochMilli(clockMillis), ZoneId.of("UTC"))

  val dummyUUID = "01234567-89ab-cdef-0123-456789abcdef"

  val regime: String           = "AD"
  val appaId: String           = appaIdGen.sample.get
  val userId: String           = "userId"
  val userDetails: UserDetails = UserDetails(appaId, userId)
  val credId: String           = "TESTCREDID00000"

  val emailAddress                                                    = "john.doe@example.com"
  val contactPreferencesEmailSelected: SubscriptionContactPreferences =
    SubscriptionContactPreferences(true, Some(emailAddress), Some(true), Some(false))

  val userAnswers: UserAnswers = UserAnswers(
    appaId = appaId,
    userId = userId,
    subscriptionSummary = SubscriptionSummaryBackend(
      paperlessReference = true,
      emailAddress = Some(SensitiveString(emailAddress)),
      emailVerification = Some(true),
      bouncedEmail = Some(false)
    ),
    emailAddress = Some(SensitiveString(emailAddress)),
    verifiedEmailAddresses = Set(SensitiveString(emailAddress)),
    data = JsObject(Seq("contactPreferenceEmail" -> Json.toJson(true))),
    startedTime = Instant.now(clock),
    lastUpdated = Instant.now(clock)
  )

  val decryptedUA: DecryptedUA = DecryptedUA(
    appaId = appaId,
    userId = userId,
    subscriptionSummary = SubscriptionSummary(
      paperlessReference = true,
      emailAddress = Some(emailAddress),
      emailVerification = Some(true),
      bouncedEmail = Some(false)
    ),
    emailAddress = Some(emailAddress),
    verifiedEmailAddresses = Set(emailAddress),
    data = JsObject(Seq("contactPreferenceEmail" -> Json.toJson(true))),
    startedTime = Instant.now(clock),
    lastUpdated = Instant.now(clock)
  )

  val emptyUserAnswers: UserAnswers = UserAnswers(
    appaId = appaId,
    userId = userId,
    subscriptionSummary = SubscriptionSummaryBackend(
      paperlessReference = true,
      emailAddress = Some(SensitiveString(emailAddress)),
      emailVerification = Some(true),
      bouncedEmail = Some(false)
    ),
    emailAddress = None,
    verifiedEmailAddresses = Set.empty,
    startedTime = Instant.now(clock),
    lastUpdated = Instant.now(clock)
  )

  val getVerificationStatusResponse = GetVerificationStatusResponse(
    List(
      GetVerificationStatusResponseEmailAddressDetails(
        emailAddress = "john.doe@example.com",
        verified = true,
        locked = false
      ),
      GetVerificationStatusResponseEmailAddressDetails(
        emailAddress = "jane.doe@example.com",
        verified = false,
        locked = true
      ),
      GetVerificationStatusResponseEmailAddressDetails(
        emailAddress = "john.doe2@example.com",
        verified = false,
        locked = false
      ),
      GetVerificationStatusResponseEmailAddressDetails(
        emailAddress = "jane.doe2@example.com",
        verified = true,
        locked = true
      )
    )
  )

  case class DownstreamErrorDetails(code: String, message: String, logID: String)

  object DownstreamErrorDetails {
    implicit val downstreamErrorDetailsWrites: OFormat[DownstreamErrorDetails] = Json.format[DownstreamErrorDetails]
  }

  val badRequest          = DownstreamErrorDetails("400", "You messed up", "id")
  val internalServerError = DownstreamErrorDetails("500", "Computer says No!", "id")
}
