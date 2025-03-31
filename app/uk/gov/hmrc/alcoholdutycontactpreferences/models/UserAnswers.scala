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
import uk.gov.hmrc.alcoholdutycontactpreferences.queries.{Gettable, Settable}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import scala.util.{Failure, Success, Try}

case class DecryptedUA(
  appaId: String,
  userId: String,
  subscriptionSummary: SubscriptionSummary,
  emailAddress: Option[String],
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
        userAnswers.subscriptionSummary.bouncedEmail
      ),
      emailAddress = userAnswers.emailAddress.map(_.decryptedValue),
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
  data: JsObject = Json.obj(),
  startedTime: Instant,
  lastUpdated: Instant,
  validUntil: Option[Instant] = None
) {
  def get[A](cacheable: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(cacheable.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {
  def createUserAnswers(
    userDetails: UserDetails,
    contactPreferences: SubscriptionContactPreferences,
    clock: Clock
  ): UserAnswers =
    UserAnswers(
      appaId = userDetails.appaId,
      userId = userDetails.userId,
      subscriptionSummary = SubscriptionSummaryBackend(
        contactPreferences.paperlessReference,
        contactPreferences.emailAddress.map(SensitiveString(_)),
        contactPreferences.emailVerification,
        contactPreferences.bouncedEmail
      ),
      emailAddress = None,
      startedTime = Instant.now(clock),
      lastUpdated = Instant.now(clock)
    )

  def fromDecryptedUA(decryptedUA: DecryptedUA): UserAnswers =
    UserAnswers(
      appaId = decryptedUA.appaId,
      userId = decryptedUA.userId,
      subscriptionSummary = SubscriptionSummaryBackend(
        decryptedUA.subscriptionSummary.paperlessReference,
        decryptedUA.subscriptionSummary.emailAddress.map(SensitiveString(_)),
        decryptedUA.subscriptionSummary.emailVerification,
        decryptedUA.subscriptionSummary.bouncedEmail
      ),
      emailAddress = decryptedUA.emailAddress.map(SensitiveString(_)),
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
  bouncedEmail: Option[Boolean]
)

object SubscriptionSummary {
  implicit val subscriptionSummaryFormat: OFormat[SubscriptionSummary] = Json.format[SubscriptionSummary]
}

case class SubscriptionSummaryBackend(
  paperlessReference: Boolean,
  emailAddress: Option[SensitiveString],
  emailVerification: Option[Boolean],
  bouncedEmail: Option[Boolean]
)

object SubscriptionSummaryBackend {

  implicit def format(implicit crypto: Encrypter with Decrypter): OFormat[SubscriptionSummaryBackend] =
    (
      (__ \ "paperlessReference").format[Boolean] and
        (__ \ "emailAddress").formatNullable[SensitiveString] and
        (__ \ "emailVerification").formatNullable[Boolean] and
        (__ \ "bouncedEmail").formatNullable[Boolean]
    )(SubscriptionSummaryBackend.apply, unlift(SubscriptionSummaryBackend.unapply))

  implicit def sensitiveStringFormat(implicit crypto: Encrypter with Decrypter): Format[SensitiveString] =
    JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

}
