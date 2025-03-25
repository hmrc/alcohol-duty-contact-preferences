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

import play.api.libs.json.{JsPath, Json}
import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.alcoholdutycontactpreferences.crypto.NoCrypto
import uk.gov.hmrc.alcoholdutycontactpreferences.queries.{Gettable, Settable}
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.time.Instant
import scala.util.Success

class UserAnswersSpec extends SpecBase {
  val ua          = userAnswers.copy(validUntil = Some(Instant.now(clock).plusMillis(1)))
  val uaDecrypted = decryptedUA.copy(validUntil = Some(Instant.now(clock).plusMillis(1)))

  case object TestCacheable extends Gettable[String] with Settable[String] {
    override def path: JsPath = JsPath \ toString
  }

  "UserAnswers must" - {
    "when encryption is enabled" - {
      val jsonWithEncrpytion =
        s"""{"_id":"$appaId","userId":"$userId","subscriptionSummary":{"paperlessReference":true,"emailAddress":"QuEpxLZgVPo2eQybYbl9Yxq+hGWotDBesA31u/dlBBU=","emailVerification":true,"bouncedEmail":false},"emailAddress":"QuEpxLZgVPo2eQybYbl9Yxq+hGWotDBesA31u/dlBBU=","data":{"contactPreferenceEmail":true},"startedTime":{"$$date":{"$$numberLong":"1718118467838"}},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}},"validUntil":{"$$date":{"$$numberLong":"1718118467839"}}}"""

      implicit val crypto: Encrypter with Decrypter = SymmetricCryptoFactory.aesCrypto(appConfig.cryptoKey)

      "serialise to json" in {
        Json.toJson(ua).toString() mustBe jsonWithEncrpytion
      }
      "deserialise from json" in {
        Json.parse(jsonWithEncrpytion).as[UserAnswers] mustBe ua
      }
    }

    "when encryption is disabled" - {
      val jsonWithoutEncryption =
        s"""{"_id":"$appaId","userId":"$userId","subscriptionSummary":{"paperlessReference":true,"emailAddress":"\\"john.doe@example.com\\"","emailVerification":true,"bouncedEmail":false},"emailAddress":"\\"john.doe@example.com\\"","data":{"contactPreferenceEmail":true},"startedTime":{"$$date":{"$$numberLong":"1718118467838"}},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}},"validUntil":{"$$date":{"$$numberLong":"1718118467839"}}}"""

      implicit val crypto: Encrypter with Decrypter = NoCrypto

      "serialise to json" in {
        Json.toJson(ua).toString() mustBe jsonWithoutEncryption
      }
      "deserialise from json" in {
        Json.parse(jsonWithoutEncryption).as[UserAnswers] mustBe ua
      }
    }

    "set a value for a given page and get the same value" in {

      val userAnswers = emptyUserAnswers

      val expectedValue = "value"

      val updatedUserAnswers = userAnswers.set(TestCacheable, expectedValue) match {
        case Success(value) => value
        case _              => fail()
      }

      val actualValue = updatedUserAnswers.get(TestCacheable) match {
        case Some(value) => value
        case _           => fail()
      }

      expectedValue mustBe actualValue
    }

    "remove a value for a given page" in {
      val userAnswers = emptyUserAnswers.set(TestCacheable, "value").success.value

      val updatedUserAnswers = userAnswers.remove(TestCacheable) match {
        case Success(updatedUA) => updatedUA
        case _                  => fail()
      }

      val actualValueOption = updatedUserAnswers.get(TestCacheable)
      actualValueOption mustBe None
    }

    "create a UserAnswers from components" in {
      val createdUserAnswers = UserAnswers.createUserAnswers(
        userDetails,
        contactPreferencesEmailSelected,
        clock
      )
      createdUserAnswers mustBe userAnswers
    }

    "convert a DecryptedUA to a UserAnswers" in {
      UserAnswers.fromDecryptedUA(decryptedUA) mustBe userAnswers
    }
  }

  "DecryptedUA must" - {
    val json =
      s"""{"appaId":"$appaId","userId":"$userId","subscriptionSummary":{"paperlessReference":true,"emailAddress":"john.doe@example.com","emailVerification":true,"bouncedEmail":false},"emailAddress":"john.doe@example.com","data":{"contactPreferenceEmail":true},"startedTime":{"$$date":{"$$numberLong":"1718118467838"}},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}},"validUntil":{"$$date":{"$$numberLong":"1718118467839"}}}"""

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
