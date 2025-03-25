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

package uk.gov.hmrc.alcoholdutycontactpreferences.repositories

import org.mongodb.scala.model._
import play.api.Configuration
import play.api.libs.json.Format
import uk.gov.hmrc.alcoholdutycontactpreferences.config.AppConfig
import uk.gov.hmrc.alcoholdutycontactpreferences.models.UserAnswers
import uk.gov.hmrc.crypto.SymmetricCryptoFactory
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait UpdateResult
case object UpdateSuccess extends UpdateResult
case object UpdateFailure extends UpdateResult

@Singleton
class UserAnswersRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: AppConfig,
  config: Configuration,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserAnswers](
      collectionName = "user-answers",
      mongoComponent = mongoComponent,
      domainFormat = UserAnswers.format(
        SymmetricCryptoFactory.aesCryptoFromConfig("crypto", config.underlying)
      ),
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.dbTimeToLiveInSeconds, TimeUnit.SECONDS)
        )
      ),
      extraCodecs = Seq.empty,
      replaceIndexes = true
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private val replaceDontUpsert = ReplaceOptions().upsert(false)
  private val replaceUpsert     = ReplaceOptions().upsert(true)

  private def byId(appaId: String) = Filters.equal("_id", appaId)

  def get(appaId: String): Future[Option[UserAnswers]] =
    collection
      .find(byId(appaId))
      .headOption()

  def set(answers: UserAnswers): Future[UpdateResult] = {

    val updatedAnswers = answers.copy(
      lastUpdated = Instant.now(clock)
    )

    collection
      .replaceOne(
        filter = byId(updatedAnswers.appaId),
        replacement = updatedAnswers,
        options = replaceDontUpsert
      )
      .toFuture()
      .map(res => if (res.getModifiedCount == 1) UpdateSuccess else UpdateFailure)
  }

  def add(answers: UserAnswers): Future[UserAnswers] = {

    val updatedAnswers = answers.copy(
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds))
    )

    collection
      .replaceOne(
        filter = byId(updatedAnswers.appaId),
        replacement = updatedAnswers,
        options = replaceUpsert
      )
      .toFuture()
      .map(_ => updatedAnswers)
  }

  def clearUserAnswersById(appaId: String): Future[Unit] =
    collection.deleteOne(filter = byId(appaId)).toFuture().map(_ => ())
}
