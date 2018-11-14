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

import javax.inject.{Inject, Named, Singleton}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OFormat, Reads, __}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouterAuthConnector @Inject()(httpClient: HttpClient, @Named("authUrl") serviceUrl: String)(implicit hc: HeaderCarrier, ec: ExecutionContext){
  def currentUserAuthority(): Future[UserAuthority] = httpClient.GET[UserAuthority](s"$serviceUrl/auth/authority")

  def userAuthority(credId: String): Future[UserAuthority] = httpClient.GET[UserAuthority](s"$serviceUrl/auth/gg/$credId")

  def getIds(idsUri: String): Future[InternalUserIdentifier] = httpClient.GET[InternalUserIdentifier](s"$serviceUrl$idsUri")

  def getEnrolments(enrolmentsUri: String): Future[Seq[Any]] = httpClient.GET[Seq[GovernmentGatewayEnrolment]](s"$serviceUrl$enrolmentsUri")
}

case class GovernmentGatewayEnrolment(key: String, identifiers: Seq[EnrolmentIdentifier], state: String)

object GovernmentGatewayEnrolment {
  implicit val idFmt: OFormat[EnrolmentIdentifier] = Json.format[EnrolmentIdentifier]
  implicit val fmt: OFormat[GovernmentGatewayEnrolment] = Json.format[GovernmentGatewayEnrolment]
}

case class EnrolmentIdentifier(key: String, value: String)

case class InternalUserIdentifier(internalId: String) extends AnyVal

object InternalUserIdentifier {
  implicit val reads: Reads[InternalUserIdentifier] = (__ \ "internalId").read[String].map(InternalUserIdentifier(_))
  implicit def convertToString(id: InternalUserIdentifier): String = id.internalId
}

case class UserAuthority(twoFactorAuthOptId: Option[String], idsUri: Option[String], userDetailsUri: Option[String], enrolmentsUri: Option[String], credentialStrength: String, nino: Option[String], saUtr: Option[String])

object UserAuthority {
  implicit val reads: Reads[UserAuthority] =
    ((__ \ "twoFactorAuthOtpId").readNullable[String] and
      (__ \ "ids").readNullable[String] and
      (__ \ "userDetailsLink").readNullable[String] and
      (__ \ "enrolments").readNullable[String] and
      (__ \ "credentialStrength").read[String] and
      (__ \ "nino").readNullable[String] and
      (__ \ "saUtr").readNullable[String]).apply(UserAuthority.apply _)
}
