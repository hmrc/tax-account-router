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

import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import uk.gov.hmrc.taxaccountrouter.engine.RuleEngine
import uk.gov.hmrc.taxaccountrouter.model.{Conditions, RuleContext}

import scala.concurrent.Await

class AccountTypeSpec  extends UnitSpec with MockitoSugar with ScalaFutures {
  val fakeLogger:Logger = Mockito.spy(classOf[Logger])
  val realLogger:Logger = LoggerFactory.getLogger("testLogger")
  val engine: RuleEngine = new RuleEngine(fakeLogger)
  val mockRuleContext: RuleContext = mock[RuleContext]
  val mockConditions: Conditions = mock[Conditions]
  val accountType: AccountType = new AccountType(mockConditions)

  "AccountLocation" should {
    "return Agent if the account type is Agent" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(true)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Agent")
    }
    "return Individual if the user is from verify" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(true)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Individual")
    }
    "return Organisation if the user is from gg and has no enrolments" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(false)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Organisation if the user is from gg and has a business enrolment" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(true)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Organisation if the user is from gg, has an Sa enrolment but could not retrieve SaReturns" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(true)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(false)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Organisation if the user is from gg, has an Sa enrolment but there were no SaReturns" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(true)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(false)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Organisation if the user is from gg, has an Sa enrolment and in a partnership" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(true)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(true)
      when(mockConditions.inPartnership(mockRuleContext)).thenReturn(true)
      when(mockConditions.isSelfEmployed(mockRuleContext)).thenReturn(false)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Organisation if the user is from gg, has an Sa enrolment and Self-employed" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(true)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(true)
      when(mockConditions.inPartnership(mockRuleContext)).thenReturn(false)
      when(mockConditions.isSelfEmployed(mockRuleContext)).thenReturn(true)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Organisation if the user is from gg, has an Sa enrolment, not in a partnership or self employed with no nino" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(true)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(true)
      when(mockConditions.inPartnership(mockRuleContext)).thenReturn(false)
      when(mockConditions.isSelfEmployed(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasNino(mockRuleContext)).thenReturn(false)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Individual if the user is from gg, has an Sa enrolment, not in a partnership or self employed with nino" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(true)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(true)
      when(mockConditions.inPartnership(mockRuleContext)).thenReturn(false)
      when(mockConditions.isSelfEmployed(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasNino(mockRuleContext)).thenReturn(true)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Individual")
    }
    "return Organisation if the user has no groups or inactive enrolments" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(true)
      when(mockConditions.inPartnership(mockRuleContext)).thenReturn(false)
      when(mockConditions.isSelfEmployed(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasNino(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasInactiveEnrolments(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasAffinityGroup(mockRuleContext)).thenReturn(false)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
    "return Individual if the user has no inactive enrolments and is an individual" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(true)
      when(mockConditions.inPartnership(mockRuleContext)).thenReturn(false)
      when(mockConditions.isSelfEmployed(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasNino(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasInactiveEnrolments(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasAffinityGroup(mockRuleContext)).thenReturn(true)
      when(mockConditions.isIndividual(mockRuleContext)).thenReturn(true)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Individual")
    }
    "return Organisation if no rules matched" in {
      when(mockConditions.isAgent(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromVerify(mockRuleContext)).thenReturn(false)
      when(mockConditions.fromGG(mockRuleContext)).thenReturn(true)
      when(mockConditions.enrolmentAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasBusinessEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasSaEnrolment(mockRuleContext)).thenReturn(false)
      when(mockConditions.saReturnAvailable(mockRuleContext)).thenReturn(true)
      when(mockConditions.hasSaReturn(mockRuleContext)).thenReturn(true)
      when(mockConditions.inPartnership(mockRuleContext)).thenReturn(false)
      when(mockConditions.isSelfEmployed(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasNino(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasInactiveEnrolments(mockRuleContext)).thenReturn(false)
      when(mockConditions.hasAffinityGroup(mockRuleContext)).thenReturn(true)
      when(mockConditions.isIndividual(mockRuleContext)).thenReturn(false)
      val result = Await.result(engine.assess(accountType.rules(mockRuleContext)), 5 seconds)
      result shouldBe Some("Organisation")
    }
  }
}
