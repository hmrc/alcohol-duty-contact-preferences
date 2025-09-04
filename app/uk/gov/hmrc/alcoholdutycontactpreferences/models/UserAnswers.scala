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

package uk.gov.hmrc.alcoholdutycontactpreferences.models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}

case class DecryptedUA(
  appaId: String,
  userId: String,
  subscriptionSummary: SubscriptionSummary,
  emailAddress: Option[String],
  verifiedEmailAddresses: Set[String],
  data: JsObject = Json.obj(),
  startedTime: Instant,
  lastUpdated: Instant,
  validUntil: Option[Instant] = None
)

object DecryptedUA {
  def fromUA(userAnswers: UserAnswers): DecryptedUA =
    DecryptedUA(
      appaId = userAnswers.appaId,
      userId = userAnswers.userId,
      subscriptionSummary = SubscriptionSummary(
        userAnswers.subscriptionSummary.paperlessReference,
        userAnswers.subscriptionSummary.emailAddress.map(_.decryptedValue),
        userAnswers.subscriptionSummary.emailVerification,
        userAnswers.subscriptionSummary.bouncedEmail,
        userAnswers.subscriptionSummary.correspondenceAddress.decryptedValue,
        userAnswers.subscriptionSummary.countryCode.map(_.decryptedValue)
      ),
      emailAddress = userAnswers.emailAddress.map(_.decryptedValue),
      verifiedEmailAddresses = userAnswers.verifiedEmailAddresses.map(_.decryptedValue),
      data = userAnswers.data,
      startedTime = userAnswers.startedTime,
      lastUpdated = userAnswers.lastUpdated,
      validUntil = userAnswers.validUntil
    )

  implicit val format: OFormat[DecryptedUA] = (
    (__ \ "appaId").format[String] and
      (__ \ "userId").format[String] and
      (__ \ "subscriptionSummary").format[SubscriptionSummary] and
      (__ \ "emailAddress").formatNullable[String] and
      (__ \ "verifiedEmailAddresses").format[Set[String]] and
      (__ \ "data").formatWithDefault[JsObject](Json.obj()) and
      (__ \ "startedTime").format(MongoJavatimeFormats.instantFormat) and
      (__ \ "lastUpdated").format(MongoJavatimeFormats.instantFormat) and
      (__ \ "validUntil").formatNullable(MongoJavatimeFormats.instantFormat)
  )(DecryptedUA.apply, unlift(DecryptedUA.unapply))

}

case class UserAnswers(
  appaId: String,
  userId: String,
  subscriptionSummary: SubscriptionSummaryBackend,
  emailAddress: Option[SensitiveString],
  verifiedEmailAddresses: Set[SensitiveString],
  data: JsObject = Json.obj(),
  startedTime: Instant,
  lastUpdated: Instant,
  validUntil: Option[Instant] = None
)

object UserAnswers {
  def createUserAnswers(
    userDetails: UserDetails,
    contactPreferences: SubscriptionContactPreferences,
    clock: Clock
  ): UserAnswers = {
    val existingEmail: Option[SensitiveString] = contactPreferences.emailAddress.map(SensitiveString)
    val hasVerifiedAndValidEmail: Boolean      = existingEmail.nonEmpty &&
      contactPreferences.emailVerificationFlag.contains(true) && !contactPreferences.bouncedEmailFlag.contains(true)

    val correspondenceAddress: String = Seq(
      contactPreferences.addressLine1,
      contactPreferences.addressLine2,
      contactPreferences.addressLine3,
      contactPreferences.addressLine4,
      contactPreferences.postcode
    ).flatten.mkString("\n")

    UserAnswers(
      appaId = userDetails.appaId,
      userId = userDetails.userId,
      subscriptionSummary = SubscriptionSummaryBackend(
        contactPreferences.paperlessReference,
        existingEmail,
        contactPreferences.emailVerificationFlag,
        contactPreferences.bouncedEmailFlag,
        SensitiveString(correspondenceAddress),
        contactPreferences.country.map(SensitiveString)
      ),
      emailAddress = None,
      verifiedEmailAddresses = if (hasVerifiedAndValidEmail) existingEmail.toSet else Set.empty[SensitiveString],
      startedTime = Instant.now(clock),
      lastUpdated = Instant.now(clock)
    )
  }

  def fromDecryptedUA(decryptedUA: DecryptedUA): UserAnswers =
    UserAnswers(
      appaId = decryptedUA.appaId,
      userId = decryptedUA.userId,
      subscriptionSummary = SubscriptionSummaryBackend(
        decryptedUA.subscriptionSummary.paperlessReference,
        decryptedUA.subscriptionSummary.emailAddress.map(SensitiveString),
        decryptedUA.subscriptionSummary.emailVerification,
        decryptedUA.subscriptionSummary.bouncedEmail,
        SensitiveString(decryptedUA.subscriptionSummary.correspondenceAddress),
        decryptedUA.subscriptionSummary.countryCode.map(SensitiveString)
      ),
      emailAddress = decryptedUA.emailAddress.map(SensitiveString),
      verifiedEmailAddresses = decryptedUA.verifiedEmailAddresses.map(SensitiveString),
      data = decryptedUA.data,
      startedTime = decryptedUA.startedTime,
      lastUpdated = decryptedUA.lastUpdated,
      validUntil = decryptedUA.validUntil
    )

  implicit def format(implicit crypto: Encrypter with Decrypter): OFormat[UserAnswers] =
    (
      (__ \ "_id").format[String] and
        (__ \ "userId").format[String] and
        (__ \ "subscriptionSummary").format[SubscriptionSummaryBackend] and
        (__ \ "emailAddress").formatNullable[SensitiveString] and
        (__ \ "verifiedEmailAddresses").format[Set[SensitiveString]] and
        (__ \ "data").formatWithDefault[JsObject](Json.obj()) and
        (__ \ "startedTime").format(MongoJavatimeFormats.instantFormat) and
        (__ \ "lastUpdated").format(MongoJavatimeFormats.instantFormat) and
        (__ \ "validUntil").formatNullable(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply, unlift(UserAnswers.unapply))

  implicit def sensitiveStringFormat(implicit crypto: Encrypter with Decrypter): Format[SensitiveString] =
    JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)
}

case class SubscriptionSummary(
  paperlessReference: Boolean,
  emailAddress: Option[String],
  emailVerification: Option[Boolean],
  bouncedEmail: Option[Boolean],
  correspondenceAddress: String,
  countryCode: Option[String]
)

object SubscriptionSummary {
  implicit val subscriptionSummaryFormat: OFormat[SubscriptionSummary] = Json.format[SubscriptionSummary]
}

case class SubscriptionSummaryBackend(
  paperlessReference: Boolean,
  emailAddress: Option[SensitiveString],
  emailVerification: Option[Boolean],
  bouncedEmail: Option[Boolean],
  correspondenceAddress: SensitiveString,
  countryCode: Option[SensitiveString]
)

object SubscriptionSummaryBackend {

  implicit def format(implicit crypto: Encrypter with Decrypter): OFormat[SubscriptionSummaryBackend] =
    (
      (__ \ "paperlessReference").format[Boolean] and
        (__ \ "emailAddress").formatNullable[SensitiveString] and
        (__ \ "emailVerification").formatNullable[Boolean] and
        (__ \ "bouncedEmail").formatNullable[Boolean] and
        (__ \ "correspondenceAddress").format[SensitiveString] and
        (__ \ "countryCode").formatNullable[SensitiveString]
    )(SubscriptionSummaryBackend.apply, unlift(SubscriptionSummaryBackend.unapply))

  implicit def sensitiveStringFormat(implicit crypto: Encrypter with Decrypter): Format[SensitiveString] =
    JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

}
