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

package uk.gov.hmrc.taxaccountrouter.rulesets

import javax.inject.Inject
import uk.gov.hmrc.taxaccountrouter.engine.Operators._
import uk.gov.hmrc.taxaccountrouter.model.{Conditions, RuleContext}

import scala.concurrent.{ExecutionContext, Future}

class AccountLocation @Inject()(conditions: Conditions)(implicit ec: ExecutionContext) {

  def rules(context: RuleContext) = Seq(
    "If logged in via Verify" -> verifyRule(context) -> "pta",
    "If gg user with no enrolments" -> noEnrolmentRule(context) -> "bta",
    "If gg user with a business enrolment" -> businessEnrolmentRule(context) -> "bta",
    "If gg user with sa enrolment but sa offline" -> saOfflineRule(context) -> "bta",
    "If gg user with sa enrolment but no returns" -> noSaReturns(context) -> "bta",
    "If gg user with sa enrolment and in partnership or self employed" -> selfEmployedOrPartnershipRule(context) -> "bta",
    "If gg user with sa enrolment not in partnership or self employed no nino" -> missingNinoRule(context) -> "bta",
    "If gg user with sa enrolment not in partnership or self employed" -> notSelfEmployedOrPartnershipRule(context) -> "pta",
    "If no groups or inactive enrolments" -> noGroupsRule(context) -> "bta",
    "If no inactive enrolments and is an individual" -> individualRule(context) -> "pta",
    "No rules matched" -> (() => Future(true)) -> "bta"
  )

  protected def verifyRule(context: RuleContext): () => Future[Boolean] =
    () => conditions.fromVerify(context)

  protected def noEnrolmentRule(context: RuleContext): () => Future[Boolean] =
    () => all(conditions.fromGG(context), not(conditions.enrolmentAvailable(context)))

  protected def businessEnrolmentRule(context: RuleContext): () => Future[Boolean] =
    () => all(conditions.fromGG(context), conditions.hasBusinessEnrolment(context))

  protected def saOfflineRule(context: RuleContext): () => Future[Boolean] =
    () => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.saReturnAvailable(context)))

  protected def noSaReturns(context: RuleContext): () => Future[Boolean] =
    () => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.hasSaReturn(context)))

  protected def selfEmployedOrPartnershipRule(context: RuleContext): () => Future[Boolean] =
    () => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), any(conditions.inPartnership(context), conditions.isSelfEmployed(context)))

  protected def missingNinoRule(context: RuleContext): () => Future[Boolean] =
    () => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.inPartnership(context)), not(conditions.isSelfEmployed(context)), not(conditions.hasNino(context)))

  protected def notSelfEmployedOrPartnershipRule(context: RuleContext): () => Future[Boolean] =
    () => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.inPartnership(context)), not(conditions.isSelfEmployed(context)))

  protected def noGroupsRule(context: RuleContext): () => Future[Boolean] =
    () => all(not(conditions.hasInactiveEnrolments(context)), not(conditions.hasAffinityGroup(context)))

  protected def individualRule(context: RuleContext): () => Future[Boolean] =
    () => all(not(conditions.hasInactiveEnrolments(context)), conditions.isIndividual(context))
}
