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

import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.taxaccountrouter.auth.{GovernmentGatewayEnrolment, InternalUserIdentifier, UserAuthority}
import uk.gov.hmrc.taxaccountrouter.config.WSHttpClient

import scala.concurrent.{ExecutionContext, Future}

trait RouterAuthConnector extends ServicesConfig {
  def http: HttpGet
  def serviceUrl: String

  def currentUserAuthority(implicit hc: HeaderCarrier, ec: ExecutionContext):Future[UserAuthority] = http.GET[UserAuthority](s"$serviceUrl/auth/authority")

  def userAuthority(credId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext):Future[UserAuthority] = http.GET[UserAuthority](s"$serviceUrl/auth/gg/$credId")

  def getIds(idsUri: String)(implicit hc: HeaderCarrier, ec: ExecutionContext):Future[InternalUserIdentifier] = http.GET[InternalUserIdentifier](s"$serviceUrl$idsUri")

  def getEnrolments(enrolmentsUri: String)(implicit hc: HeaderCarrier, ec: ExecutionContext):Future[Seq[Any]] = http.GET[Seq[GovernmentGatewayEnrolment]](s"$serviceUrl$enrolmentsUri")
}

object RouterAuthConnector extends RouterAuthConnector {
  override def http: HttpGet = WSHttpClient
  override def serviceUrl: String = baseUrl("auth")

}
