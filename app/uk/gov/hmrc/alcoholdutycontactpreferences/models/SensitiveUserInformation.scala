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

///*
// * Copyright 2025 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.alcoholdutycontactpreferences.models
//
//import org.mongodb.scala.bson.ObjectId
//import play.api.Configuration
//import play.api.libs.functional.syntax._
//import play.api.libs.json._
//import uk.gov.hmrc.crypto.Sensitive.SensitiveString
//import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText, Sensitive, SymmetricCryptoFactory}
//import uk.gov.hmrc.mongo.play.json.formats.MongoFormats
//
//import java.security.SecureRandom
//import java.util.Base64
//case class SensitiveUserInformation(
//  paperlessReference: Boolean,
//  emailVerification: Option[Boolean],
//  bouncedEmail: Option[Boolean],
//  emailAddress: Option[SensitiveString]
//)
//
//object SensitiveUserInformation {
//  implicit val format: Format[SensitiveUserInformation] =
//    ((__ \ "paperlessReference").format[Boolean]
//      ~ (__ \ "withEmail").format[Boolean]
//      ~ (__ \ "emailVerification").formatNullable[Boolean]
//      ~ (__ \ "bouncedEmail").formatNullable[Boolean]
//      ~ (__ \ "emailAddress").formatNullable[SensitiveString])(
//      SensitiveUserInformation.apply,
//      sui => (sui.paperlessReference, sui.withEmail, sui.emailVerification, sui.bouncedEmail, sui.emailAddress)
//    )
//
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
//  implicit val ssFormat: Format[SensitiveString] =
//    sensitiveFormat[String, SensitiveString](SensitiveString.apply, _.decryptedValue)
//
//}
