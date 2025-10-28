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
import uk.gov.hmrc.alcoholdutycontactpreferences.config.AppConfig
import uk.gov.hmrc.crypto.AesCrypto

import org.mockito.Mockito.when

class CryptoProviderSpec extends SpecBase {
  val mockAppConfig: AppConfig = mock[AppConfig]

  val cryptoProvider = new CryptoProvider(mockAppConfig)

  "getCrypto must" - {
    "use NoCrypto when the encryption feature switch is disabled" in {
      when(mockAppConfig.cryptoEnabled) thenReturn false

      val result = cryptoProvider.getCrypto
      result.isInstanceOf[NoCrypto.type] mustBe true
    }

    "use AES Crypto when the encryption feature switch is enabled" in {
      when(mockAppConfig.cryptoEnabled) thenReturn true

      val result = cryptoProvider.getCrypto
      result.isInstanceOf[AesCrypto] mustBe true
    }
  }
}
