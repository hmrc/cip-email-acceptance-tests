/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.test.api.specs

import org.scalatest.Inspectors.forAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.{JsNull, JsValue}
import uk.gov.hmrc.test.api.helpers.requests.ValidationRequests.{callValidateEndpoint, emailRequest}
import uk.gov.hmrc.test.api.helpers.validate.ValidationResponses.emailErrorResponse

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ValidationSpec extends BaseSpec {

  Feature("Validation - email") {

    Scenario("I wish to validate a valid email") {
      // a list of
      // input, expected
      val validEmailData = List(
        ("07843274323"),
        ("0044(0)7890056734"),
        ("+44-7890056734")
      )

      forAll(validEmailData) { emailInput =>
        Given("I have a valid email")
        val email = emailInput

        When("I validate it against the validation service")
        val jsonBody = emailRequest(email)
        val result: Future[JsValue] = callValidateEndpoint(jsonBody)

        Then("I should receive a valid message")
        Await.result(result, 50 seconds) must not be JsNull
      }
    }

    Scenario("I wish to validate an invalid email") {
      // input, expected code, expected message
      val invalidEmailData = List(
        ("0784327432e"),
        ("999"),
        ("78900567343")
      )

      forAll(invalidEmailData) { emailInput =>
        Given("I have an invalid email")
        val email = emailInput

        When("I validate it against the validation service")
        val jsonBody = emailRequest(email)
        val result: Future[JsValue] = callValidateEndpoint(jsonBody)

        Then("I should receive a validation error message")
        Await.result(result, 50 seconds) must not be JsNull
        println("GOT HERE!!!")
        emailErrorResponse.code shouldBe "VALIDATION_ERROR"
        emailErrorResponse.message shouldBe "Enter a valid email address"
      }
    }
  }
}
