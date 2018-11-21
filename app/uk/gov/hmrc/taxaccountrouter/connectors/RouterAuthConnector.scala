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
import uk.gov.hmrc.http.{NotFoundException, _}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouterAuthConnector @Inject()(httpClient: HttpClient, log: Logger, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext) {
  private val serviceUrl: String = servicesConfig.baseUrl("auth")

  //Appears to return Upstream4xxResponse in case of problems
  def currentUserAuthority()(implicit hc: HeaderCarrier): Future[UserAuthority] = {
    httpClient.GET[UserAuthority](s"$serviceUrl/auth/authority").recoverWith {
      case up4xx @ (_: NotFoundException | _: Upstream4xxResponse) => {
        log.warn("we made a request that auth was unable to handle", up4xx)
        Future.failed(up4xx)
      }
      case up5xx: Upstream5xxResponse => {
        log.warn("auth was unable to handle the request", up5xx)
        Future.failed(up5xx)
      }
      case e: Throwable => {
        log.warn("Was unable to execute call to auth", e)
        throw e
      }
    }
  }

  //Appears to return status 404 in case of problem
  def userAuthority(credId: String)(implicit hc: HeaderCarrier): Future[UserAuthority] = {
    httpClient.GET[UserAuthority](s"$serviceUrl/auth/gg/$credId").recoverWith {
      case nfe @ (_: NotFoundException | _: Upstream4xxResponse) => {
        log.warn(s"we made a request for credId $credId that auth was unable to handle", nfe)
        Future.failed(nfe)
      }
      case up5xx: Upstream5xxResponse => {
        log.warn(s"auth was unable to handle the request for credId $credId", up5xx)
        Future.failed(up5xx)
      }
      case e: Throwable => {
        log.warn(s"Was unable to execute call to auth for credId $credId", e)
        throw e
      }
    }
  }

  //Appears to return status 404 passed down userAuthority
  def getIds(idsUri: String)(implicit hc: HeaderCarrier): Future[InternalUserIdentifier] = {
    httpClient.GET[InternalUserIdentifier](s"$serviceUrl$idsUri").recoverWith {
      case nfe @ (_: NotFoundException | _: Upstream4xxResponse) => {
        log.warn(s"we made a request for $idsUri that auth was unable to handle", nfe)
        Future.failed(nfe)
      }
      case up5xx: Upstream5xxResponse => {
        log.warn(s"auth was unable to handle the request for id $idsUri", up5xx)
        Future.failed(up5xx)
      }
      case e: Throwable => {
        log.warn(s"Was unable to execute call to auth for $idsUri", e)
        throw e
      }
    }
  }

  //Appears to never return an error!
  def getEnrolments(enrolmentsUri: String)(implicit hc: HeaderCarrier): Future[Seq[Any]] = {
    httpClient.GET[Seq[GovernmentGatewayEnrolment]](s"$serviceUrl$enrolmentsUri").recoverWith {
      case up5xx: Upstream5xxResponse => {
        log.warn(s"auth was unable to handle the request for enrolment $enrolmentsUri", up5xx)
        Future.failed(up5xx)
      }
      case e: Throwable => {
        log.warn(s"Was unable to execute call to auth for $enrolmentsUri", e)
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
