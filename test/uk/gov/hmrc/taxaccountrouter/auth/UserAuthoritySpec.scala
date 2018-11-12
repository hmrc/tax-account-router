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
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.CredentialStrength
import uk.gov.hmrc.play.test.UnitSpec

class UserAuthoritySpec extends UnitSpec with MockitoSugar with ScalaFutures {

  "responses for UserAuthorities" should {
    "parse correctly into the UserAuthority domain object" in {
      val userDetailsLink = "/user-details/id/5658962a3d00003d002f3ca1"
      val twoFactorOtpId = "/user-details/id/5658962a3d00003d002f3ca1"
      val credentialStrength = CredentialStrength.Strong
      val saUtr = SaUtr("12345")
      val nino = Nino("CS100700A")
      val idsUri = "/auth/ids-uri"
      val enrolmentsUri = "/auth/enrolments-uri"
      val authResponse =
        s"""{
           |    "userDetailsLink": "$userDetailsLink",
           |    "twoFactorAuthOtpId": "$twoFactorOtpId",
           |    "credentialStrength": "${credentialStrength.name.toLowerCase}",
           |    "nino": "${nino.value}",
           |    "saUtr": "${saUtr.value}",
           |    "ids": "$idsUri",
           |    "enrolments": "$enrolmentsUri"
           |    }""".stripMargin

      Json.parse(authResponse).as[UserAuthority] shouldBe UserAuthority(Some(twoFactorOtpId), Some(idsUri), Some(userDetailsLink), Some(enrolmentsUri), credentialStrength, Some(nino), Some(saUtr))
    }
  }
}
