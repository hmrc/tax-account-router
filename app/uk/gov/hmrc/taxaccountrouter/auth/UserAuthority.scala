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

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.CredentialStrength

case class UserAuthority(twoFactorAuthOptId: Option[String], idsUri: Option[String], userDetailsUri: Option[String], enrolmentsUri: Option[String], credentialStrength: CredentialStrength, nino: Option[Nino], saUtr: Option[SaUtr])

object UserAuthority {
  implicit val reads: Reads[UserAuthority] =
    ((__ \ "twoFactorAuthOtpId").readNullable[String] and
      (__ \ "ids").readNullable[String] and
      (__ \ "userDetailsLink").readNullable[String] and
      (__ \ "enrolments").readNullable[String] and
      (__ \ "credentialStrength").read[CredentialStrength] and
      (__ \ "nino").readNullable[Nino] and
      (__ \ "saUtr").readNullable[SaUtr]).apply(UserAuthority.apply _)
}