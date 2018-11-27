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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AccountLocation @Inject()(conditions: Conditions) {

  def rules(context: RuleContext) = Seq(
    "If logged in via Verify" -> (() => conditions.fromVerify(context)) -> "pta",
    "If gg user with no enrolments" -> (() => all(conditions.fromGG(context), not(conditions.enrolmentAvailable(context)))) -> "bta",
    "If gg user with a business enrolment" -> (() => all(conditions.fromGG(context), conditions.hasBusinessEnrolment(context))) -> "bta",
    "If gg user with sa enrolment but sa offline" -> (() => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.saReturnAvailable(context)))) -> "bta",
    "If gg user with sa enrolment but no returns" -> (() => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.hasSaReturn(context)))) -> "bta",
    "If gg user with sa enrolment and in partnership or self employed" -> (() => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), any(conditions.inPartnership(context), conditions.isSelfEmployed(context)))) -> "bta",
    "If gg user with sa enrolment not in partnership or self employed no nino" -> (() => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.inPartnership(context)), not(conditions.isSelfEmployed(context)), not(conditions.hasNino(context)))) -> "bta",
    "If gg user with sa enrolment not in partnership or self employed" -> (() => all(conditions.fromGG(context), conditions.hasSaEnrolment(context), not(conditions.inPartnership(context)), not(conditions.isSelfEmployed(context)))) -> "pta",
    "If no groups or inactive enrolments" -> (() => all(not(conditions.hasInactiveEnrolments(context)), not(conditions.hasAffinityGroup(context)))) -> "bta",
    "If no inactive enrolments and is an individual" -> (() => all(not(conditions.hasInactiveEnrolments(context)), conditions.isIndividual(context))) -> "pta",
    "No rules matched" -> (() => Future(true)) -> "bta"
  )
}
