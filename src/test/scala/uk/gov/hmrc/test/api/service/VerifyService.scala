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

package uk.gov.hmrc.test.api.service

import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.test.api.client.HttpClient
import uk.gov.hmrc.test.api.conf.TestConfiguration

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class VerifyService extends HttpClient {
  private val host = TestConfiguration.url("cip-email")
  private val contextPath = "/customer-insight-platform/email"
  private val headers = Seq(("Content-Type", "application/json"), ("Accept", "application/json"),("Authorization", "fake-token"))

  def verify(email: String): StandaloneWSResponse = {
    val payload =
      s"""
         |{"email" : "$email" }
      """.stripMargin

    Await.result(
      post(
        s"$host$contextPath/verify",
        payload,
        headers: _*
      ),
      10.seconds)
  }

  def verifyPasscode(email: String, passcode: String): StandaloneWSResponse = {
    val payload =
      s"""
         |{
         |  "email": "$email",
         |  "passcode": "$passcode"
         |}
      """.stripMargin

    Await.result(
      post(
        s"$host$contextPath/verify/passcode",
        payload,
        headers: _*
      ),
      10.seconds)
  }
}
