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
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.{TableDrivenPropertyChecks, Tables}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

import uk.gov.hmrc.taxaccountrouter.engine.RuleEngine
import uk.gov.hmrc.taxaccountrouter.rulesets.FizzBuzz.rules

class FizzBuzzSpec extends FunSuite with ScalaFutures {
  val fakeLogger:Logger = Mockito.spy(classOf[Logger])
  val realLogger:Logger = LoggerFactory.getLogger("testLogger")
  val engine: RuleEngine = new RuleEngine(fakeLogger)

  test("Rules can be constructed for fizzbuzz as a table") {
    val scenarios = Tables.Table[Int, String](
      ("param", "fizz or buzz or fizzbuzz or param"),
      (1, "1"),
      (2, "2"),
      (3, "fizz"),
      (4, "4"),
      (5, "buzz"),
      (6, "fizz"),
      (7, "7"),
      (8, "8"),
      (9, "fizz"),
      (10, "buzz"),
      (11, "11"),
      (12, "fizz"),
      (13, "13"),
      (14, "14"),
      (15, "fizzbuzz"),
      (16, "16")
    )

    TableDrivenPropertyChecks.forAll(scenarios) { (n, expected) =>
      val result = Await.result(engine.assessLogged(rules(n), "fizzbuzz"), 5 seconds).get
      assert(expected == result)
    }
  }
}
