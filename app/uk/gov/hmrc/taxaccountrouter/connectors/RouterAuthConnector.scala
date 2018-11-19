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

import javax.inject.{Inject, Singleton}
import org.slf4j.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, Reads, __}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouterAuthConnector @Inject()(httpClient: HttpClient, servicesConfig: ServicesConfig, log: Logger)(implicit ec: ExecutionContext) {
  lazy val serviceUrl: String = servicesConfig.baseUrl("auth")

  def currentUserAuthority()(implicit hc: HeaderCarrier): Future[UserAuthority] = {
    httpClient.GET[UserAuthority](s"$serviceUrl/auth/authority").recover {
      case e: Throwable => {
        log.warn("Unable to retrieve current user", e)
        throw e
      }
    }
  }

  def userAuthority(credId: String)(implicit hc: HeaderCarrier): Future[Option[UserAuthority]] = {
    httpClient.GET[UserAuthority](s"$serviceUrl/auth/gg/$credId").recover {
      case _: NotFoundException => None
      case e: Throwable => {
        log.warn(s"No user found with credId $credId", e)
        throw e
      }
    }
  }

  def getIds(idsUri: String)(implicit hc: HeaderCarrier): Future[InternalUserIdentifier] = {
    httpClient.GET[InternalUserIdentifier](s"$serviceUrl$idsUri").recover {
      case e: Throwable => {
        log.warn(s"Unable to retrieve internal Identifier with idUri $idsUri", e)
        throw e
      }
    }
  }

  def getEnrolments(enrolmentsUri: String)(implicit hc: HeaderCarrier): Future[Seq[Any]] = {
    httpClient.GET[Seq[GovernmentGatewayEnrolment]](s"$serviceUrl$enrolmentsUri").recover {
      case e: Throwable => {
        log.warn(s"Unable to retrieve gg enrolment with enrolment Uri $enrolmentsUri", e)
        throw e
      }
    }
  }
}

case class GovernmentGatewayEnrolment(key: String, identifiers: Seq[EnrolmentIdentifier], state: String)

object GovernmentGatewayEnrolment {
  implicit val idFmt: Format[EnrolmentIdentifier] = Json.format[EnrolmentIdentifier]
  implicit val fmt: Format[GovernmentGatewayEnrolment] = Json.format[GovernmentGatewayEnrolment]
}

case class EnrolmentIdentifier(key: String, value: String)

case class InternalUserIdentifier(internalId: String)

object InternalUserIdentifier {
  implicit val reads: Reads[InternalUserIdentifier] = (__ \ "internalId").read[String].map(InternalUserIdentifier(_))
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
