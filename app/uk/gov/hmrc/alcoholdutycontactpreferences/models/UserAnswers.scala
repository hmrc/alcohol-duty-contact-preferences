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
  paperlessReference: Boolean,
  emailVerification: Option[Boolean],
  bouncedEmail: Option[Boolean],
  decryptedSensitiveUserInformation: DecryptedSensitiveUserInformation,
  data: JsObject = Json.obj(),
  startedTime: Instant,
  lastUpdated: Instant,
  validUntil: Option[Instant] = None
)

object DecryptedUA {
  def fromUA(userAnswers: UserAnswers): DecryptedUA = {

    val sensitiveInfo = userAnswers.sensitiveUserInformation
    DecryptedUA(
      appaId = userAnswers.appaId,
      userId = userAnswers.userId,
      paperlessReference = userAnswers.paperlessReference,
      emailVerification = userAnswers.emailVerification,
      bouncedEmail = userAnswers.bouncedEmail,
      decryptedSensitiveUserInformation = DecryptedSensitiveUserInformation(
        emailAddress = sensitiveInfo.emailAddress.map(_.decryptedValue),
        emailEntered = sensitiveInfo.emailEntered.map(_.decryptedValue)
      ),
      data = userAnswers.data,
      startedTime = userAnswers.startedTime,
      lastUpdated = userAnswers.lastUpdated,
      validUntil = userAnswers.validUntil
    )
  }

  implicit val format: OFormat[DecryptedUA] = (
    (__ \ "appaId").format[String] and
      (__ \ "userId").format[String] and
      (__ \ "paperlessReference").format[Boolean] and
      (__ \ "emailVerification").formatNullable[Boolean] and
      (__ \ "bouncedEmail").formatNullable[Boolean] and
      (__ \ "decryptedSensitiveUserInformation").format[DecryptedSensitiveUserInformation] and
      (__ \ "data").formatWithDefault[JsObject](Json.obj()) and
      (__ \ "startedTime").format(MongoJavatimeFormats.instantFormat) and
      (__ \ "lastUpdated").format(MongoJavatimeFormats.instantFormat) and
      (__ \ "validUntil").formatNullable(MongoJavatimeFormats.instantFormat)
  )(DecryptedUA.apply, unlift(DecryptedUA.unapply))

}

case class UserAnswers(
  appaId: String,
  userId: String,
  paperlessReference: Boolean,
  emailVerification: Option[Boolean],
  bouncedEmail: Option[Boolean],
  sensitiveUserInformation: SensitiveUserInformation,
  //                        emailAddress: Option[SensitiveString],
  //                        emailEntered: Option[SensitiveString] = None,
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
  ): UserAnswers =
    UserAnswers(
      appaId = userDetails.appaId,
      userId = userDetails.userId,
      paperlessReference = contactPreferences.paperlessReference,
      emailVerification = contactPreferences.emailVerification,
      bouncedEmail = contactPreferences.bouncedEmail,
      sensitiveUserInformation = SensitiveUserInformation(
        emailAddress = contactPreferences.emailAddress.map(SensitiveString(_))
      ),
      startedTime = Instant.now(clock),
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock))
    )

  def fromDecryptedUA(decryptedUA: DecryptedUA): UserAnswers = {
    val decryptedSensitiveInfo = decryptedUA.decryptedSensitiveUserInformation
    UserAnswers(
      appaId = decryptedUA.appaId,
      userId = decryptedUA.userId,
      paperlessReference = decryptedUA.paperlessReference,
      emailVerification = decryptedUA.emailVerification,
      bouncedEmail = decryptedUA.bouncedEmail,
      sensitiveUserInformation = SensitiveUserInformation(
        emailAddress = decryptedSensitiveInfo.emailAddress.map(SensitiveString(_)),
        emailEntered = decryptedSensitiveInfo.emailEntered.map(SensitiveString(_))
      ),
      data = decryptedUA.data,
      startedTime = decryptedUA.startedTime,
      lastUpdated = decryptedUA.lastUpdated,
      validUntil = decryptedUA.validUntil
    )
  }

  implicit def format(implicit crypto: Encrypter with Decrypter): OFormat[UserAnswers] =
    (
      (__ \ "_id").format[String] and
        (__ \ "userId").format[String] and
        (__ \ "paperlessReference").format[Boolean] and
        (__ \ "emailVerification").formatNullable[Boolean] and
        (__ \ "bouncedEmail").formatNullable[Boolean] and
        (__ \ "sensitiveUserInformation").format[SensitiveUserInformation] and
        (__ \ "data").formatWithDefault[JsObject](Json.obj()) and
        (__ \ "startedTime").format(MongoJavatimeFormats.instantFormat) and
        (__ \ "lastUpdated").format(MongoJavatimeFormats.instantFormat) and
        (__ \ "validUntil").formatNullable(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply, unlift(UserAnswers.unapply))

}
