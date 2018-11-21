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

package uk.gov.hmrc.taxaccountrouter.model

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.taxaccountrouter.connectors.{RouterAuthConnector, UserAuthority, UserDetail, UserDetailsConnector}

import scala.concurrent.{ExecutionContext, Future}

class RuleContext @Inject()(credId: Option[String])(authConnector: RouterAuthConnector, userDetailsConnector: UserDetailsConnector)(implicit hc: HeaderCarrier, ec: ExecutionContext) {
  lazy val authority: Future[UserAuthority] = credId.fold(authConnector.currentUserAuthority()) {
    id => authConnector.userAuthority(id)
  }
  lazy val userDetails: Future[UserDetail] = authority.flatMap {
    auth => userDetailsConnector.getUserDetails(auth)
  }
}
