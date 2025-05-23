/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  val appName: String = config.get[String]("appName")

  private val subscriptionHost: String                  = servicesConfig.baseUrl("subscription")
  lazy val subscriptionClientId: String                 = getConfStringAndThrowIfNotFound("subscription.clientId")
  lazy val subscriptionSecret: String                   = getConfStringAndThrowIfNotFound("subscription.secret")
  private lazy val subscriptionGetSubscriptionUrlPrefix = getConfStringAndThrowIfNotFound(
    "subscription.url.subscriptionSummary"
  )

  private val emailVerificationHost: String                 = servicesConfig.baseUrl("email-verification")
  private lazy val emailVerificationGetVerifiedEmailsPrefix = getConfStringAndThrowIfNotFound(
    "email-verification.url.getVerifiedEmails"
  )

  val idType: String = config.get[String]("downstream-apis.idType")
  val regime: String = config.get[String]("downstream-apis.regime")

  val cryptoKey: String      = config.get[String]("crypto.key")
  val cryptoEnabled: Boolean = config.get[Boolean]("crypto.isEnabled")

  val enrolmentServiceName: String   = config.get[String]("enrolment.serviceName")
  val enrolmentIdentifierKey: String = config.get[String]("enrolment.identifierKey")

  val dbTimeToLiveInSeconds: Int = 1200

  def getSubscriptionUrl(appaId: String): String =
    s"$subscriptionHost$subscriptionGetSubscriptionUrlPrefix/$regime/$idType/$appaId"

  def getVerifiedEmailsUrl(credId: String): String =
    s"$emailVerificationHost$emailVerificationGetVerifiedEmailsPrefix/$credId"

  private[config] def getConfStringAndThrowIfNotFound(key: String) =
    servicesConfig.getConfString(key, throw new RuntimeException(s"Could not find services config key '$key'"))
}
