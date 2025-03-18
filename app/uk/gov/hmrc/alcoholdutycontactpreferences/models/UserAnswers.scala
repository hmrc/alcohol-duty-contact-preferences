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

import org.mongodb.scala.bson.ObjectId
import play.api.Configuration
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.{SensitiveBoolean, SensitiveString}
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText, Sensitive, SymmetricCryptoFactory}
import uk.gov.hmrc.mongo.play.json.formats.{MongoFormats, MongoJavatimeFormats}
import uk.gov.hmrc.crypto.json.JsonEncryption
import play.api.libs.functional.syntax._

import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

case class DecryptedUA(
                      appaId: String,
                      sensitiveString: String,
                      data: JsObject = Json.obj(),
                      lastUpdated: Instant = Instant.now,
                      )

object DecryptedUA {
  def fromUA(userAnswers: UserAnswers): DecryptedUA = {
    DecryptedUA(
      appaId = userAnswers.appaId,
      sensitiveString = userAnswers.sensitiveString.decryptedValue,
      data = userAnswers.data,
      lastUpdated = userAnswers.lastUpdated
    )
  }

  implicit val format: OFormat[DecryptedUA] = (
      (__ \ "appaId").format[String] and
        (__ \ "sensitiveString").format[String] and
        (__ \ "data").formatWithDefault[JsObject](Json.obj()) and
        (__ \ "lastUpdated").format(MongoJavatimeFormats.instantFormat)
    )(DecryptedUA.apply, unlift(DecryptedUA.unapply))

}

case class UserAnswers(
                        appaId: String,
                        groupId: String,
                        internalId: String,
                        sensitiveString: SensitiveString,
                        data: JsObject = Json.obj(),
                        //  sensitiveUserInformation: SensitiveUserInformation,
                        startedTime: Instant = Instant.now,
                        lastUpdated: Instant = Instant.now,
                        validUntil: Option[Instant] = None
                      )

object UserAnswers {
  def createUserAnswers(
                         returnAndUserDetails: ReturnAndUserDetails
                         //    ,
                         //    sensitiveUserInformation: SensitiveUserInformation
                       ): UserAnswers =
    UserAnswers(
      appaId = returnAndUserDetails.appaId,
      groupId = returnAndUserDetails.groupId,
      internalId = returnAndUserDetails.userId,
      sensitiveString = SensitiveString("test123")
      //      sensitiveUserInformation = sensitiveUserInformation
    )


  implicit def sensitiveStringFormat(implicit crypto: Encrypter with Decrypter): Format[SensitiveString] =
    JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

  implicit def format(implicit crypto: Encrypter with Decrypter): OFormat[UserAnswers] =
    (
      (__ \ "_id").format[String] and
        (__ \ "groupId").format[String] and
        (__ \ "internalId").format[String] and
        (__ \ "sensitiveString").format[SensitiveString] and
        (__ \ "data").formatWithDefault[JsObject](Json.obj()) and
        //        (__ \ "sensitiveUserInformation").format[SensitiveUserInformation] and
        (__ \ "startedTime").format(MongoJavatimeFormats.instantFormat) and
        (__ \ "lastUpdated").format(MongoJavatimeFormats.instantFormat) and
        (__ \ "validUntil").formatNullable(MongoJavatimeFormats.instantFormat)
      )(UserAnswers.apply, unlift(UserAnswers.unapply))

  //  private implicit val crypto: Encrypter with Decrypter = {
  //    val aesKey = {
  //      val aesKey = new Array[Byte](32)
  //      new SecureRandom().nextBytes(aesKey)
  //      Base64.getEncoder.encodeToString(aesKey)
  //    }
  //    val config = Configuration("crypto.key" -> aesKey)
  //    SymmetricCryptoFactory.aesGcmCryptoFromConfig("crypto", config.underlying)
  //  }
  //
  //  implicit val oif: Format[ObjectId] = MongoFormats.Implicits.objectIdFormat
  //
  //  val cryptoFormat: OFormat[Crypted] =
  //    ((__ \ "encrypted").format[Boolean]
  //      ~ (__ \ "value").format[String])((_, v) => Crypted.apply(v), c => (true, c.value))
  //
  //  def sensitiveFormat[A: Format, B <: Sensitive[A]](apply: A => B, unapply: B => A)(implicit
  //    crypto: Encrypter with Decrypter
  //  ): OFormat[B] =
  //    cryptoFormat
  //      .inmap[B](
  //        c => apply(Json.parse(crypto.decrypt(c).value).as[A]),
  //        sn => crypto.encrypt(PlainText(implicitly[Format[A]].writes(unapply(sn)).toString))
  //      )
  //
  //  implicit val ssFormat: Format[SensitiveString]  =
  //    sensitiveFormat[String, SensitiveString](SensitiveString.apply, _.decryptedValue)
  //  implicit val sbFormat: Format[SensitiveBoolean] =
  //    sensitiveFormat[Boolean, SensitiveBoolean](SensitiveBoolean.apply, _.decryptedValue)

}
