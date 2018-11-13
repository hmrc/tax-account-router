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
import play.api.libs.json.{Format, Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

case class CredentialRole(value: String)
object CredentialRole {
  implicit val fmt:Format[CredentialRole] = Json.format[CredentialRole]
}

case class UserDetail(credentialRole: Option[CredentialRole], affinityGroup: String)
object UserDetail {
  implicit val fmt:Format[UserDetail] = Json.format[UserDetail]
}

@Singleton
class UserDetailsConnector @Inject()(httpClient: HttpClient) {

  def getUserDetails(userAuthority: UserAuthority)(implicit rds: uk.gov.hmrc.http.HttpReads[UserDetail], hc:HeaderCarrier, ec: ExecutionContext): Future[UserDetail] = {
    httpClient.GET[UserDetail](userAuthority.userDetailsUri.get)
  }
}
