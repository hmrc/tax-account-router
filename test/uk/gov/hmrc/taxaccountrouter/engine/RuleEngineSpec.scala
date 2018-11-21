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

package uk.gov.hmrc.taxaccountrouter.engine

import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.FunSuite
import org.slf4j.Logger

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class RuleEngineSpec extends FunSuite with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val fakeLogger: Logger = Mockito.spy(classOf[Logger])
  val engine: RuleEngine = new RuleEngine(fakeLogger)


  test("An empty sequence of rules returns a None as a consequence when asseses") {
    val result = Await.result(engine.assessLogged(Seq()), 5 seconds)
    assert(result isEmpty)
  }

  test("A sequence of rules returns the value of the leftmost rule where the precedent evaluates to Future.successful(true) when assessed") {
    val rules = Seq(
      "FIRST_RULE" -> (() => Future(true)) -> "FIRST-TRUE-RULE",
      "SECOND-RULE" -> (() => Future(false)) -> "FALSE-RULE")
    val result = Await.result(engine.assessLogged(rules), 5 seconds)
    assert(result contains "FIRST-TRUE-RULE")
  }

  test("A sequence of rules with some precedents evaluating to false at the beginning, skips over them to the first true rule") {
    val rules = Seq(
      "FIRST_RULE" -> (() => Future(false)) -> "FIRST-FALSE-RULE",
      "SECOND-RULE" -> (() => Future(true)) -> "FIRST-TRUE-RULE",
      "THIRD-RULE" -> (() => Future(false)) -> "SECOND-FALSE-RULE"
    )
    val result = Await.result(engine.assessLogged(rules), 5 seconds)
    assert(result contains "FIRST-TRUE-RULE")
  }

  test("A sequence of rules with some precedents evaluating to false at the beginning, skips over them to the first true rule, subsequent true rules are ignored") {
    val rules = Seq(
      "FIRST_RULE" -> (() => Future(false)) -> "FIRST-FALSE-RULE",
      "SECOND-RULE" -> (() => Future(true)) -> "FIRST-TRUE-RULE",
      "THIRD-RULE" -> (() => Future(false)) -> "SECOND-FALSE-RULE",
      "FOURTH-RULE" -> (() => Future(true)) -> "SECOND-TRUE-RULE"
    )
    val result = Await.result(engine.assessLogged(rules), 5 seconds)
    assert(result contains "FIRST-TRUE-RULE")
  }

  test("A sequence of rules with all precedents evaluating to false returns None") {
    val rules = Seq(
      "FIRST_RULE" -> (() => Future(false)) -> "FIRST-FALSE-RULE",
      "SECOND-RULE" -> (() => Future(false)) -> "SECOND-FALSE-RULE",
      "THIRD-RULE" -> (() => Future(false)) -> "THIRD-FALSE-RULE",
      "FOURTH-RULE" -> (() => Future(false)) -> "FOURTH-FALSE-RULE"
    )
    val result = Await.result(engine.assessLogged(rules), 5 seconds)
    assert(result isEmpty)
  }

  test("A sequence of rules with an early true followed by a failure returns the value associated with the true") {
    val rules = Seq(
      "FIRST_RULE" -> (() => Future(false)) -> "FIRST-FALSE-RULE",
      "SECOND-RULE" -> (() => Future(true)) -> "FIRST-TRUE-RULE",
      "THIRD-RULE" -> (() => Future.failed(new RuntimeException("fail"))) -> "FAIL",
      "FOURTH-RULE" -> (() => Future(false)) -> "THIRD-FALSE-RULE"
    )
    val result = Await.result(engine.assessLogged(rules), 5 seconds)
    assert(result contains "FIRST-TRUE-RULE")
  }

  test("A sequence of rules with an early fail followed by a true returns the failure") {
    val rules = Seq(
      "FIRST_RULE" -> (() => Future(false)) -> "FIRST-FALSE-RULE",
      "SECOND-RULE" -> (() => Future.failed(new RuntimeException("fail"))) -> "FAIL",
      "THIRD-RULE" -> (() => Future(true)) -> "FIRST-TRUE-RULE",
      "FOURTH-RULE" -> (() => Future(false)) -> "THIRD-FALSE-RULE"
    )
    val result = intercept[RuntimeException] {
      Await.result(engine.assessLogged(rules), 5 seconds)
    }
    assert(result.getMessage == "fail")
  }

  test("A sequence of rules with an early fail followed another fail followed by a true returns the first failure") {
    val rules = Seq(
      "FIRST_RULE" -> (() => Future(false)) -> "FIRST-FALSE-RULE",
      "SECOND-RULE" -> (() => Future.failed(new RuntimeException("fail0"))) -> "FAIL0",
      "THIRD-RULE" -> (() => Future.failed(new RuntimeException("fail1"))) -> "FAIL1",
      "FOURTH-RULE" -> (() => Future(true)) -> "FIRST-TRUE-RULE",
      "RULE-THE-FIFTH" -> (() => Future(false)) -> "THIRD-FALSE-RULE"
    )
    val result = intercept[RuntimeException] {
      Await.result(engine.assessLogged(rules), 5 seconds)
    }
    assert(result.getMessage == "fail0")
  }
}
