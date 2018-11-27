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

package uk.gov.hmrc.taxaccountrouter.controllers

import javax.inject.{Inject, Singleton}
import org.slf4j.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.taxaccountrouter.connectors.{RouterAuthConnector, SelfAssessmentConnector, UserDetailsConnector}
import uk.gov.hmrc.taxaccountrouter.engine.RuleEngine
import uk.gov.hmrc.taxaccountrouter.model.{Conditions, RuleContext}
import uk.gov.hmrc.taxaccountrouter.rulesets.AccountType

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouterController @Inject()(authConnector: RouterAuthConnector, userDetailsConnector: UserDetailsConnector, selfAssessmentConnector: SelfAssessmentConnector, cc: ControllerComponents, log: Logger, conditions: Conditions)(implicit ec: ExecutionContext) extends BackendController(cc) {
  def hello(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("Hello world"))
  }

  def routeAccount(): Action[AnyContent] = Action.async { implicit request =>
    val ruleContext = RuleContext(None)(authConnector, userDetailsConnector, selfAssessmentConnector)
    val destination = "dest"
    Future.successful(Ok("Hello world"))
  }

  def accountType(credId: String): Action[AnyContent] = Action.async { implicit request =>
    val ruleContext = RuleContext(Some(credId))(authConnector, userDetailsConnector, selfAssessmentConnector)
    ruleContext.userDetails.map {
      ud =>
        new RuleEngine(log).assessLogged(new AccountType(conditions).rules(ruleContext))
        Ok(Json.toJson(s"Hello ${ud.affinityGroup}"))
    }.recover {
      case e =>
        log.warn("Unable to get user details from downstream.", e)
        InternalServerError("Unable to get user details from downstream.")
    }
  }
}
