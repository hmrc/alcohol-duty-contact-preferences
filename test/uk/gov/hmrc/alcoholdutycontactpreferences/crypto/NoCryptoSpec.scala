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

package uk.gov.hmrc.alcoholdutycontactpreferences.crypto

import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase
import uk.gov.hmrc.crypto.{Crypted, PlainBytes, PlainText}

import java.nio.charset.StandardCharsets
import java.util.Base64

class NoCryptoSpec extends SpecBase {
  "encrypt must" - {
    "wrap the PlainText value in an Crypted object" in {
      val result = NoCrypto.encrypt(PlainText("value"))
      result mustBe Crypted("value")
    }

    "wrap the PlainBytes value in an Crypted object" in {
      val result = NoCrypto.encrypt(PlainBytes(new Array[Byte](5)))
      result mustBe Crypted(new String(Base64.getEncoder.encode(new Array(5)), StandardCharsets.UTF_8))
    }
  }

  "decrypt must " - {
    "wrap the Crypted value in an PlainText object" in {
      val result = NoCrypto.decrypt(Crypted("value"))
      result mustBe PlainText("value")
    }
  }

  "decryptAsBytes must" - {
    "wrap the Crypted value in an PlainBytes object (and Base64 decode the value)" in {
      val result = NoCrypto.decryptAsBytes(Crypted("dmFsdWU="))
      result.isInstanceOf[PlainBytes] mustBe true
    }
  }
}
