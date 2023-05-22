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
import play.api.Configuration
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.taxaccountrouter.connectors.{RouterAuthConnector, SelfAssessmentConnector, UserDetailsConnector}
import uk.gov.hmrc.taxaccountrouter.engine.RuleEngine
import uk.gov.hmrc.taxaccountrouter.model.{Conditions, Location, RuleContext}
import uk.gov.hmrc.taxaccountrouter.rulesets.{AccountLocation, AccountType}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RouterController @Inject()(authConnector: RouterAuthConnector, userDetailsConnector: UserDetailsConnector, selfAssessmentConnector: SelfAssessmentConnector, cc: ControllerComponents, log: Logger, conditions: Conditions, configuration: Configuration)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def hello(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("Hello world"))
  }

  def routeAccount(): Action[AnyContent] = Action.async { implicit request =>
    val ruleContext = RuleContext(None)(authConnector, userDetailsConnector, selfAssessmentConnector)
    new RuleEngine(log).assess(new AccountLocation(conditions).rules(ruleContext), "Account Routing Rules").map {
      result => Redirect(new Location(configuration).buildLocation(result.getOrElse(throw new NoSuchFieldException("There was no result from the engine for account type"))))
    }.recover {
      case e =>
        log.warn("An error occurred.", e)
        InternalServerError("Unable to route user to a destination.")
    }
  }

  def accountType(credId: String): Action[AnyContent] = Action.async { implicit request =>
    val ruleContext = RuleContext(Some(credId))(authConnector, userDetailsConnector, selfAssessmentConnector)
    new RuleEngine(log).assess(new AccountType(conditions).rules(ruleContext), "Account Type Rules").map {
      result => Ok(Json.toJson(AccountTypeResponse(result.getOrElse(throw new NoSuchFieldException("There was no result from the engine for account type")))))
    }.recover {
      case e =>
        log.warn("An error occurred.", e)
        InternalServerError("Unable to match a user to an account type.")
    }
  }
}

case class AccountTypeResponse(`type`: String)

object AccountTypeResponse {
  implicit val fmt: Format[AccountTypeResponse] = Json.format[AccountTypeResponse]
}
