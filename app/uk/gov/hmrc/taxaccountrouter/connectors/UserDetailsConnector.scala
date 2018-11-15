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
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDetailsConnector @Inject()(httpClient: HttpClient, log: Logger)(implicit hc: HeaderCarrier, ec: ExecutionContext){

  def getUserDetails(userDetailsUri: String): Future[UserDetail] = httpClient.GET[UserDetail](userDetailsUri).recover{
    case e: Throwable =>
      log.warn(s"Unable to retrieve user details with uri $userDetailsUri", e)
      throw e
  }

  def getUserDetails(userAuthority: UserAuthority): Future[UserDetail] = {
    getUserDetails(userAuthority.userDetailsUri.getOrElse{
      val e = new NotFoundException("no userDetailsUri found in UserAuthority")
      log.warn("user Authority did not contain a userDetailsUri", e)
      throw e
    })
  }
}

case class UserDetail(credentialRole: Option[CredentialRole], affinityGroup: String) {
  def isAdmin: Boolean = {
    credentialRole match {
      case Some(User) => true
      case _ => false
    }
  }
}

object UserDetail {
  implicit val reads: Reads[UserDetail] = Json.reads[UserDetail]
}
