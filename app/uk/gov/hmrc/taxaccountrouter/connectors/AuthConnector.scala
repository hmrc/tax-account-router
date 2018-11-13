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

package uk.gov.hmrc.taxaccountrouter.connectors

import uk.gov.hmrc.auth.core.{CredentialStrength, Nino}
import play.api.libs.json.{Format, Json}


case class SaUtr(utr: String)
object SaUtr {
  implicit val fmt: Format[SaUtr] = Json.format[SaUtr]
}

case class UserAuthority(twoFactorAuthOtpId: Option[String], idsUri: Option[String], userDetailsUri: Option[String], enrolmentsUri: Option[String],
                         credentialStrength: CredentialStrength, nino: Option[Nino], saUtr: Option[SaUtr])
object UserAuthority {
  implicit val cFmt: Format[CredentialStrength] = Json.format[CredentialStrength]
  implicit val usFmt: Format[UserAuthority] = Json.format[UserAuthority]
}

class AuthConnector {

}
