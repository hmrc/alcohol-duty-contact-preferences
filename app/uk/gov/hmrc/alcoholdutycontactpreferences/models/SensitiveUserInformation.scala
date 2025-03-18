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
import play.api.libs.json.{Format, Json, OFormat, __}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

case class DecryptedSensitiveUserInformation(
                                     emailAddress: Option[String],
                                     emailEntered: Option[String] = None
                                   )

object DecryptedSensitiveUserInformation {
  implicit val format: OFormat[DecryptedSensitiveUserInformation] = Json.format[DecryptedSensitiveUserInformation]
}

case class SensitiveUserInformation(
                                     emailAddress: Option[SensitiveString],
                                     emailEntered: Option[SensitiveString] = None
                                   )

object SensitiveUserInformation {

  implicit def format(implicit crypto: Encrypter with Decrypter): OFormat[SensitiveUserInformation] =
    (
      (__ \ "emailAddress").formatNullable[SensitiveString] and
        (__ \ "emailEntered").formatNullable[SensitiveString]
      )(SensitiveUserInformation.apply, unlift(SensitiveUserInformation.unapply))

  implicit def sensitiveStringFormat(implicit crypto: Encrypter with Decrypter): Format[SensitiveString] =
    JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

}
