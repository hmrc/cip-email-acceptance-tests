/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.test.api.helpers

import uk.gov.hmrc.mongo.CurrentTimestampSupport
import uk.gov.hmrc.mongo.cache.{CacheIdType, DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.test.MongoSupport
import uk.gov.hmrc.test.api.models.EmailAndPasscodeData

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class TestDataHelper extends MongoSupport {
  override def databaseName: String = "cip-email-verification"

  val repository = new MongoCacheRepository(
    mongoComponent = mongoComponent,
    collectionName = "cip-email-verification",
    ttl = 1.minute,
    timestampSupport = new CurrentTimestampSupport(),
    cacheIdType = CacheIdType.SimpleCacheId
  )

  def getPasscodeForEmail(email: String): Option[EmailAndPasscodeData] = {
    Await.result(
      repository.get[EmailAndPasscodeData](email)(DataKey("cip-email-verification")), 10.seconds)
  }
}
