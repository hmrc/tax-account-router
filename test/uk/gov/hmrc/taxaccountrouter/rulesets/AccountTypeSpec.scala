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
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import uk.gov.hmrc.taxaccountrouter.engine.RuleEngine
import uk.gov.hmrc.taxaccountrouter.model.{Conditions, RuleContext}

import scala.concurrent.{Await, Future}

class AccountTypeSpec extends FunSuite with ScalaFutures with MockitoSugar {
  val fakeLogger:Logger = Mockito.spy(classOf[Logger])
  val realLogger:Logger = LoggerFactory.getLogger("testLogger")
  val engine: RuleEngine = new RuleEngine(fakeLogger)
  val mockRuleContext: RuleContext = mock[RuleContext]
  val mockConditions: Conditions = mock[Conditions]
  val accountType: AccountType = new AccountType(mockConditions)

  test("Rule 'Check if the user is an Agent' is triggered if the user has an account type of Agent") {
    val expected = "Agent"
    when(mockConditions.isAgent(mockRuleContext)).thenReturn(Future(true))
    val result = Await.result(engine.assessLogged(accountType.rules(mockRuleContext)), 5 seconds)
    assert(expected.equalsIgnoreCase(result.fold("")(t=>t)))
  }
}
