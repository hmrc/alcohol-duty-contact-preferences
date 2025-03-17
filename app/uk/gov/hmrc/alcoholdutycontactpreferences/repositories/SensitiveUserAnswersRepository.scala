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

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes, ReplaceOptions, Updates}
import org.mongodb.scala.result.UpdateResult
import play.api.Configuration
import play.api.libs.json.Format
import uk.gov.hmrc.alcoholdutycontactpreferences.config.AppConfig
import uk.gov.hmrc.alcoholdutycontactpreferences.models.{ReturnId, UserAnswers}
import uk.gov.hmrc.crypto.SymmetricCryptoFactory
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait UpdateResult
case object UpdateSuccess extends UpdateResult
case object UpdateFailure extends UpdateResult

//object TestObject {
//  val userAnswersSchema =
//    BsonDocument(
//      """
//        { bsonType: "object"
//        , required: [ "_id", "returnId", "groupId", "internalId", "sensitiveString", "startedTime", "lastUpdated" ]
//        , properties:
//          { _id              : { bsonType: "objectId" }
//          , returnId         : { bsonType: "string" }
//          , groupId          : { bsonType: "string" }
//          , internalId       : { bsonType: "string" }
//          , sensitiveString  : { bsonType: "object", required: [ "encrypted", "value" ], properties: { "encrypted": { bsonType: "bool"}, value: { bsonType: "string" } } }
//          , startedTime      : { bsonType: "date" }
//          , lastUpdated      : { bsonType: "date" }
//          , validUntil       : { bsonType: "date" }
//          }
//        }
//      """
//    )
//}

@Singleton
class SensitiveUserAnswersRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: AppConfig,
  config: Configuration,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserAnswers](
      collectionName = "sensitive-user-answers",
      mongoComponent = mongoComponent,
      domainFormat = UserAnswers.format(
        SymmetricCryptoFactory.aesCryptoFromConfig("crypto", config.underlying)
      ),
//      optSchema = None,
//        Some(TestObject.userAnswersSchema),
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.dbTimeToLiveInSeconds, TimeUnit.SECONDS)
        )
      ),
      extraCodecs = Seq(
        Codecs.playFormatCodec(ReturnId.format)
//        ,
//        Codecs.playFormatCodec(UserAnswers.ssFormat),
//        Codecs.playFormatCodec(UserAnswers.sbFormat)
      ),
      replaceIndexes = true
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: ReturnId) = Filters.equal("_id", id)

  def keepAlive(id: ReturnId): Future[Boolean] =
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.combine(
          Updates.set("lastUpdated", Instant.now(clock)),
          Updates.set("validUntil", Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds))
        )
      )
      .toFuture()
      .map(_ => true)

  def get(id: ReturnId): Future[Option[UserAnswers]] =
    keepAlive(id).flatMap { _ =>
      collection
        .find(byId(id))
        .headOption()
    }

  def set(answers: UserAnswers): Future[UpdateResult] = {

    val updatedAnswers = answers.copy(
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds))
    )

    collection
      .replaceOne(
        filter = byId(updatedAnswers.returnId),
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(false)
      )
      .toFuture()
      .map(res => if (res.getModifiedCount == 1) UpdateSuccess else UpdateFailure)
  }

  def add(answers: UserAnswers): Future[UserAnswers] = {

    val updatedAnswers = answers.copy(
      lastUpdated = Instant.now(clock),
      validUntil = Some(Instant.now(clock).plusSeconds(appConfig.dbTimeToLiveInSeconds))
    )

//    println("BBBBBBBBB " + updatedAnswers.sensitiveUserInformation.emailAddress)

    collection
      .insertOne(updatedAnswers)
      .toFuture()
      .map(_ => updatedAnswers)
  }

  def clearUserAnswersById(returnId: ReturnId): Future[Unit] =
    collection.deleteOne(filter = byId(returnId)).toFuture().map(_ => ())
}
