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

package uk.gov.hmrc.alcoholdutycontactpreferences.config

import uk.gov.hmrc.alcoholdutycontactpreferences.base.SpecBase

class SpecBaseWithConfigOverrides extends SpecBase {
  override def configOverrides: Map[String, Any] = Map(
    "appName"                                                        -> "appName",
    "microservice.services.subscription.protocol"                    -> "http",
    "microservice.services.subscription.host"                        -> "subscriptionhost",
    "microservice.services.subscription.port"                        -> 12345,
    "microservice.services.subscription.clientId"                    -> "subscription clientId",
    "microservice.services.subscription.secret"                      -> "subscription secret",
    "microservice.services.subscription.url.subscriptionSummary"     -> "/etmp/RESTAdapter/excise/subscriptionsummary",
    "microservice.services.email-verification.protocol"              -> "http",
    "microservice.services.email-verification.host"                  -> "emailverificationhost",
    "microservice.services.email-verification.port"                  -> 12345,
    "microservice.services.email-verification.url.getVerifiedEmails" -> "/email-verification/verification-status",
    "downstream-apis.idType"                                         -> "ZAD",
    "downstream-apis.regime"                                         -> "AD",
    "crypto.key"                                                     -> "cryptokey",
    "crypto.isEnabled"                                               -> true,
    "enrolment.serviceName"                                          -> "HMRC-AD-ORG",
    "features.email-verification-stub"                               -> false
  )
}

class SpecBaseWithEmailVerificationStubs extends SpecBase {
  override def configOverrides: Map[String, Any] = Map(
    "microservice.services.alcohol-duty-stubs.protocol"              -> "http",
    "microservice.services.alcohol-duty-stubs.host"                  -> "stubshost",
    "microservice.services.alcohol-duty-stubs.port"                  -> 54321,
    "microservice.services.email-verification.url.getVerifiedEmails" -> "/email-verification/verification-status",
    "features.email-verification-stub"                               -> true
  )
}

class AppConfigSpec extends SpecBaseWithConfigOverrides {
  "AppConfig" - {
    "must return the appName" in {
      appConfig.appName mustBe "appName"
    }

    "for subscriptions" - {
      "must return the getSubscription url" in {
        appConfig.getSubscriptionUrl(
          appaId
        ) mustBe s"http://subscriptionhost:12345/etmp/RESTAdapter/excise/subscriptionsummary/AD/ZAD/$appaId"
      }

      "must return the client id" in {
        appConfig.subscriptionClientId mustBe "subscription clientId"
      }

      "must return the secret" in {
        appConfig.subscriptionSecret mustBe "subscription secret"
      }
    }

    "for email verification" - {
      "must return the getVerifiedEmailsUrl when email verification stubs is toggled off" in {
        appConfig.getVerifiedEmailsUrl(
          credId
        ) mustBe s"http://emailverificationhost:12345/email-verification/verification-status/$credId"
      }
    }

    "must return the config relating to downstream APIs" - {
      "for idType" in {
        appConfig.idType mustBe "ZAD"
      }

      "for regime" in {
        appConfig.regime mustBe "AD"
      }
    }

    "for encryption" - {
      "must return the encryption key" in {
        appConfig.cryptoKey mustBe "cryptokey"
      }

      "must return whether encryption is enabled" in {
        appConfig.cryptoEnabled mustBe true
      }
    }

    "must return the enrolment service name" in {
      appConfig.enrolmentServiceName mustBe "HMRC-AD-ORG"
    }

    "getConfStringAndThrowIfNotFound must" - {
      "return a key if found" in {
        appConfig.getConfStringAndThrowIfNotFound("subscription.secret") mustBe "subscription secret"
      }

      "throw an exception if not found" in {
        a[RuntimeException] mustBe thrownBy(appConfig.getConfStringAndThrowIfNotFound("blah"))
      }
    }
  }
}

class AppConfigWithEmailVerificationStubsSpec extends SpecBaseWithEmailVerificationStubs {
  "for email verification" - {
    "must return the getVerifiedEmailsUrl when email verification stubs is toggled on" in {
      appConfig.getVerifiedEmailsUrl(
        credId
      ) mustBe s"http://stubshost:54321/email-verification/verification-status/$credId"
    }
  }
}
