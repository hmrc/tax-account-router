/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.taxaccountrouter.auth

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class InternalUserIdentifierSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  "responses for InternalUserIdentifiers" should {
    "parse correctly into the InternalUserIdentifier domain object" in {
      val internalId = "5658962a3d00003d002f3ca1"
      val authResponse =
        s"""{
           |    "internalId": "$internalId"
           |    }""".stripMargin

      Json.parse(authResponse).as[InternalUserIdentifier] shouldBe InternalUserIdentifier(internalId)
    }
  }
}
