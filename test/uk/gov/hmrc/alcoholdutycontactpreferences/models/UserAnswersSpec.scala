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

import play.api.libs.json.Json
import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.time.Instant

class UserAnswersSpec extends SpecBase {
  val ua          = userAnswers.copy(validUntil = Some(Instant.now(clock).plusMillis(1)))
  val uaDecrypted = decryptedUA.copy(validUntil = Some(Instant.now(clock).plusMillis(1)))

  implicit val crypto: Encrypter with Decrypter =
    SymmetricCryptoFactory.aesCrypto(appConfig.cryptoKey)

  "UserAnswers must" - {
    val json =
      s"""{"_id":"$appaId","userId":"$userId","paperlessReference":true,"emailVerification":true,"bouncedEmail":false,"emailData":{"emailAddress":"QuEpxLZgVPo2eQybYbl9Yxq+hGWotDBesA31u/dlBBU="},"data":{"contactPreferenceEmail":true},"startedTime":{"$$date":{"$$numberLong":"1718118467838"}},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}},"validUntil":{"$$date":{"$$numberLong":"1718118467839"}}}""".stripMargin

    "serialise to json" in {
      Json.toJson(ua).toString() mustBe json
    }
    "deserialise from json" in {
      Json.parse(json).as[UserAnswers] mustBe ua
    }

    "create a UserAnswers from components" in {
      val createdUserAnswers = UserAnswers.createUserAnswers(
        userDetails,
        contactPreferencesEmailSelected,
        clock
      )
      createdUserAnswers mustBe emptyUserAnswers
    }

    "convert a DecryptedUA to a UserAnswers" in {
      UserAnswers.fromDecryptedUA(decryptedUA) mustBe userAnswers
    }
  }

  "DecryptedUA must" - {
    val json =
      s"""{"appaId":"$appaId","userId":"$userId","paperlessReference":true,"emailVerification":true,"bouncedEmail":false,"emailData":{"emailAddress":"john.doe@example.com"},"data":{"contactPreferenceEmail":true},"startedTime":{"$$date":{"$$numberLong":"1718118467838"}},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}},"validUntil":{"$$date":{"$$numberLong":"1718118467839"}}}""".stripMargin

    "serialise to json" in {
      Json.toJson(uaDecrypted).toString() mustBe json
    }
    "deserialise from json" in {
      Json.parse(json).as[DecryptedUA] mustBe uaDecrypted
    }

    "convert a UserAnswers to a DecryptedUA" in {
      DecryptedUA.fromUA(userAnswers) mustBe decryptedUA
    }
  }
}
