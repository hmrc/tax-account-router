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

package uk.gov.hmrc.taxaccountrouter.model

import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import uk.gov.hmrc.play.test.UnitSpec

class CredentialRoleSpec extends UnitSpec{
  "isAdmin" should {
    val expectedAffinityGroup = "affinityGroup"
    val scenarios = Table(
      ("role", "result"),
      (CredentialRole("User"), true),
      (CredentialRole("NotUser"), false)
    )
    forAll(scenarios) {
      (role: CredentialRole, expectedResult: Boolean) =>
        s"return $expectedResult if user has credential role $role" in {
          role.isAdmin shouldBe expectedResult
        }
    }
  }
}
