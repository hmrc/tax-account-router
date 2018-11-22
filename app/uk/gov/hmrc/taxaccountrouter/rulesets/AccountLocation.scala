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

import uk.gov.hmrc.taxaccountrouter.engine.Operators._
import uk.gov.hmrc.taxaccountrouter.model.Conditions._
import uk.gov.hmrc.taxaccountrouter.model.RuleContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountLocation {

  def rules(context: RuleContext) = Seq(
    "If logged in via Verify" -> (() => fromVerify(context)) -> "pta",
    "If gg user with no enrolments" -> (() => all(fromGG(context), not(enrolmentAvailable(context)))) -> "bta",
    "If gg user with a business enrolment" -> (() => all(fromGG(context), hasBusinessEnrolment(context))) -> "bta",
    "If gg user with sa enrolment but sa offline" -> (() => all(fromGG(context)), hasSaEnrolment(context), not(saReturnAvailable(context))) -> "bta",
    "If gg user with sa enrolment but no returns" -> (() => all(fromGG(context), hasSaEnrolment(context), not(hasSaReturn(context)))) -> "bta",
    "If gg user with sa enrolment and in partnership or self employed" -> (() => all(fromGG(context), hasSaEnrolment(context), any(inPartnership(context), isSelfEmployed(context)))) -> "bta",
    "If gg user with sa enrolment not in partnership or self employed no nino" -> (() => all(fromGG(context), hasSaEnrolment(context), not(inPartnership(context)), not(isSelfEmployed(context)), not(hasNino(context)))) -> "bta",
    "If gg user with sa enrolment not in partnership or self employed" -> (() => all(fromGG(context), hasSaEnrolment(context), not(inPartnership(context)), not(isSelfEmployed(context)))) -> "pta",
    "If no inactive enrolments or group" -> (() => all(hasInactiveEnrolments(context), not(hasAffinityGroup(context)))) -> "bta",
    "If no inactive enrolments and is an individual" -> (() => all(hasInactiveEnrolments(context), isIndividual(context))) -> "pta",
    "No rules matched" -> (() => Future(true)) -> "bta"
  )
}
