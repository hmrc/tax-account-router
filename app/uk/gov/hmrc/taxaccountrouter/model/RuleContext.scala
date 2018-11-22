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
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.taxaccountrouter.connectors._

import scala.concurrent.{ExecutionContext, Future}

case class RuleContext @Inject()(credId: Option[String])(authConnector: RouterAuthConnector, userDetailsConnector: UserDetailsConnector, selfAssessmentConnector: SelfAssessmentConnector)(implicit request: Request[AnyContent], hc: HeaderCarrier, ec: ExecutionContext) {
  val sessionData: Map[String, String] = request.session.data

  lazy val authority: Future[UserAuthority] = credId.fold(authConnector.currentUserAuthority()) {
    id => authConnector.userAuthority(id)
  }
  lazy val userDetails: Future[UserDetail] = authority.flatMap {
    authority => userDetailsConnector.getUserDetails(authority)
  }
  lazy val enrolments: Future[Seq[GovernmentGatewayEnrolment]] = authority.flatMap {
    authority => authConnector.getEnrolments(authority)
  }
  lazy val lastSaReturn: Future[SaReturn] = authority.flatMap {
    authority => selfAssessmentConnector.lastReturn(authority)
  }
}
