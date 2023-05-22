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
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.auth.core.{CredentialRole, User}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDetailsConnector @Inject()(httpClient: HttpClient, log: Logger)(implicit hc: HeaderCarrier, ec: ExecutionContext){

  //Returns status 404 in case of problems
  def getUserDetails(userDetailsUri: String): Future[UserDetail] = httpClient.GET[UserDetail](userDetailsUri).recoverWith {
    case nfe @ (_: NotFoundException | _: Upstream4xxResponse) => {
      log.warn(s"we made a request for $userDetailsUri that User-Details was unable to handle", nfe)
      Future.failed(nfe)
    }
    case up5xx: Upstream5xxResponse => {
      log.warn(s"User-Details was unable to handle the request for $userDetailsUri", up5xx)
      Future.failed(up5xx)
    }
    case e: Throwable =>
      log.warn(s"Was unable to execute call to User-Details for $userDetailsUri", e)
      throw e
  }

  def getUserDetails(userAuthority: UserAuthority): Future[UserDetail] = {
    userAuthority.userDetailsUri.fold[Future[UserDetail]](Future.failed(new NotFoundException("no userDetailsUri found in UserAuthority"))) {
      userDetailsUri => getUserDetails(userDetailsUri)
    }
  }
}

case class UserDetail(credentialRole: Option[CredentialRole], affinityGroup: String) {
  def isAdmin: Boolean = credentialRole.contains(User)
}

object UserDetail {
  implicit val reads: Reads[UserDetail] = Json.reads[UserDetail]
}
