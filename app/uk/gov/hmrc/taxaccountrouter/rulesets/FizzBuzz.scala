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

import com.softwaremill.macmemo.memoize

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

import uk.gov.hmrc.taxaccountrouter.engine.Operators.all

object FizzBuzz {
  def rules(n: Int) = Seq(
    "Divisible by 5 & 3" -> (() => all(divisibleByThree(n), divisibleByFive(n))) -> "fizzbuzz",
    "Divisible by 3" -> (() => divisibleByThree(n)) -> "fizz",
    "Divisible by 5" -> (() => divisibleByFive(n)) -> "buzz",
    "Not divisible by 5 or 3" -> (() => Future(true)) -> n.toString
  )

  @memoize(2000, expiresAfter =2 hours)
  private def divisibleByThree(n: Int): Future[Boolean] = checkDivisibleByThree(n)
  @memoize(maxSize = 2000, expiresAfter = 2 hours)
  private def divisibleByFive(n: Int): Future[Boolean] = checkDivisibleByFive(n)

  private def checkDivisibleByThree(n: Int): Future[Boolean] = Future(n % 3 == 0)
  private def checkDivisibleByFive(n: Int): Future[Boolean] = Future(n % 5 == 0)
}
