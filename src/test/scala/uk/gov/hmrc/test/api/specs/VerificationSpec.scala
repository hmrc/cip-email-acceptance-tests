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

import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.http.Status.{ACCEPTED, BAD_REQUEST}
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables.readableAsJson

import scala.util.Random

class VerificationSpec extends BaseSpec {
  Scenario("I wish to verify an email and use an invalid passcode") {
    // inputs

    // a list of passcode input
    val invalidPasscodes = Table(
      "", // blank
      "PLY", // too short
      "NHYTREL", // too long
      "LKJH", // invalid length
      " " // empty
    )

    forAll(invalidPasscodes) { passcode =>

      val emailRandomizer = Random.alphanumeric.take(5).mkString
      val email = s"$emailRandomizer@example.com"
      
      Given("I have a valid email")
      When("I validate it against the verification service")
      val verifyResponse = verifyMatchingHelper.verify(email)

      Then("I should receive a notification Id")
      verifyResponse.header("Location").head shouldBe a[String]

      And("Once I receive the correct passcode in my inbox and ignore it")

      When("I verify incorrect passcode")

      val verifyPasscodeResponse = verifyMatchingHelper.verifyPasscode(email, passcode)

      And("I get an error response")
      (verifyPasscodeResponse.body[JsValue] \ "code").as[Int] shouldBe 1002
      (verifyPasscodeResponse.body[JsValue] \ "message").as[String] shouldBe "Enter a valid passcode"
    }
  }

  Scenario("I wish to verify an email and use an incorrect passcode") {
    // inputs
    val emailRandomizer = Random.alphanumeric.take(4).mkString
    val email = s"$emailRandomizer@example.com"

    Given("I have a valid email")
    When("I validate it against the verification service")
    val verifyResponse = verifyMatchingHelper.verify(email)

    Then("I should receive a notification Id")
    verifyResponse.header("Location").head shouldBe a[String]

    And("Once I receive the correct passcode in my inbox and ignore it")
    When("I verify incorrect passcode")
    val verifyPasscodeResponse = verifyMatchingHelper.verifyPasscode(email, "123456")

    And("I get a not verified response")
    (verifyPasscodeResponse.body[JsValue] \ "status").as[String] shouldBe "Not verified"
  }

  Scenario("I wish to verify a valid email and use correct passcode") {
    val emailRandomizer = Random.alphanumeric.take(1).mkString
    val validEmailData = Table(
      s"$emailRandomizer@a.com",
      s"$emailRandomizer@a",
      s"$emailRandomizer@A.COM",
      s"&!@$emailRandomizer.com",
      s"123@$emailRandomizer.com"
    )

    forAll(validEmailData) { email =>
      Given("I have a valid email")
      When("I verify it against the verification service")
      val verifyResponse = verifyMatchingHelper.verify(email)

      Then("I should receive a notification Id")
      verifyResponse.header("Location").head shouldBe a[String]

      And("Once I receive the correct passcode in my inbox")
      // retrieve the expected passcode from the stubs repo
      val emailAndPasscodeData = testDataHelper.getPasscodeForEmail(email).get

      When("I verify correct passcode")
      val verifyPasscodeResponse = verifyMatchingHelper.verifyPasscode(email, emailAndPasscodeData.passcode)

      And("I get verified status with verified message")
      (verifyPasscodeResponse.body[JsValue] \ "status").as[String] shouldBe "Verified"
    }
  }

  Scenario("I wish to verify an invalid email") {
    // a list of input
    val invalidEmailData = Table(
      "a@ a.com", // email with spaces
      "", // Blank submission
      "aata.com", // missing @ symbol
      "@a.com", // without host
      " " // empty submission
    )

    forAll(invalidEmailData) { phoneNumber: String =>
      Given("I have a invalid email")
      When("I verify it against the verification service")
      val verifyResponse = verifyMatchingHelper.verify(phoneNumber)

      Then("I should receive a validation error")
      verifyResponse.status shouldBe BAD_REQUEST
      (verifyResponse.body[JsValue] \ "code").as[Int] shouldBe 1002
      (verifyResponse.body[JsValue] \ "message").as[String] shouldBe "Enter a valid email"
    }
  }
}
