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

import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class OpperatorsSpec extends FunSuite with ScalaFutures{
  def time[R](block: => R):(R, Double) = {
    val start = System.nanoTime()
    val result = block
    val end = System.nanoTime()
    (result, (end - start) / 1e9)
  }

  //Found Fails
  test("A mutable.Seq of Future.successful(true) returns false because it has no failures") {
    val buffer = Seq(Future.successful(true), Future.successful(true), Future.successful(true)).toBuffer
    val result = Operators.hasFailures(buffer)
    assert(!result)
  }

  test("A mutable.Seq of Future.successful(false) returns false because it has no failures") {
    val buffer = Seq(Future.successful(false), Future.successful(false), Future.successful(false)).toBuffer
    val result = Operators.hasFailures(buffer)
    assert(!result)
  }

  test("A mutable.Seq of a mix of Future.successful(true) and Future.successful(false) returns false because it has no failures") {
    val buffer = Seq(Future.successful(false), Future.successful(false), Future.successful(false)).toBuffer
    val result = Operators.hasFailures(buffer)
    assert(!result)
  }

  test("An mutable.Seq returns false because it has no failures") {
    val buffer = Seq[Future[Boolean]]().toBuffer
    val result = Operators.hasFailures(buffer)
    assert(!result)
  }

  test("A mutable Seq with Future.sucessful true, falses, and fails returns a true because it fails in it.") {
    val buffer = Seq(Future.successful(false), Future.successful(false), Future.failed(new RuntimeException)).toBuffer
    val result = Operators.hasFailures(buffer)
    assert(result)
  }

  //All
  test("all returns Future.successful(true) when all arguments are Future.successful(true)") {
    val result = Await.result(Operators.all(Future.successful(true), Future.successful(true)), 5 seconds)
    assert(result)
  }

  test("all returns Future.successful(false) when some arguments are Future.successful(false)") {
    val result = Await.result(Operators.all(Future.successful(true), Future.successful(true), Future.successful(false)), 5 seconds)
    assert(!result)
  }

  test("all returns Future.successful(false) when all arguments are Future.successful(false)") {
    val result = Await.result(Operators.all(Future.successful(false), Future.successful(true), Future.successful(false)), 5 seconds)
    assert(!result)
  }

  test("Failed futures are cause a fail when calculating a true result using all") {
    val result = intercept[RuntimeException]{
      Await.result(Operators.all(Future.failed(new RuntimeException("Failed")), Future.successful(true), Future.successful(true)), 5 seconds)
    }
    assert(result.getMessage == "Fail detected in all when all others were true")
  }

  test("Failed futures are ignored when calculating a false result using all") {
    val result = Await.result(Operators.all(Future.failed(new RuntimeException("Failed")), Future.successful(false), Future.successful(true)), 5 seconds)
    assert(!result)
  }

  test ("all returns false fast if there are trues and falses, but trues are delayed") {
    val (result, secs) = time(Await.result(Operators.all(Future {Thread.sleep(500); true}, Future.successful(false)), 5 seconds))
    println("Time was " + secs)
    assert(!result)
    assert(secs < 0.5)
  }

  test ("all returns true at the rate of the slowest true if they are all trues.") {
    val (result, secs) = time(Await.result(Operators.all(Future {Thread.sleep(500); true}, Future.successful(true)), 5 seconds))
    println("Time was " + secs)
    assert(result)
    assert(secs > 0.5)
  }

  test ("all returns true at the rate of the slowest true/fail if they are all trues/fails.") {
    val (result, secs) = time(Await.result(Operators.all(Future {Thread.sleep(500); true}, Future.successful(true)), 5 seconds))
    println("Time was " + secs)
    assert(result)
    assert(secs > 0.5)
  }

  // Any
  test("any returns Future.successful(true) when all arguments are Future.successful(true)") {
    val result = Await.result(Operators.any(Future.successful(true), Future.successful(true), Future.successful(true)), 5 seconds)
    assert(result)
  }

  test("any returns Future.successful(true) when some arguments are Future.successful(false)") {
    val result = Await.result(Operators.any(Future.successful(true), Future.successful(false), Future.successful(false)), 5 seconds)
    assert(result)
  }

  test("any returns Future.successful(true) when all arguments are Future.successful(false)") {
    val result = Await.result(Operators.any(Future.successful(false), Future.successful(false), Future.successful(false)), 5 seconds)
    assert(!result)
  }

  test("Failed futures are ignored when calculating a true result using any") {
    val result = Await.result(Operators.any(Future.failed(new RuntimeException("Failed")), Future.successful(true), Future.successful(false)), 5 seconds)
    assert(result)
  }

  test("Failed futures are cause a fail when calculating a false result using any") {
    val result = intercept[RuntimeException]{
      Await.result(Operators.any(Future.failed(new RuntimeException("Failed")), Future.successful(false), Future.successful(false)), 5 seconds)
    }
    assert(result.getMessage == "Fail detected in any when all others were false")
  }

  test ("any returns true fast if there are trues and falses, but falses are delayed") {
    val (result, secs) = time(Await.result(Operators.any(Future {Thread.sleep(500); false}, Future.successful(true)), 5 seconds))
    println("Time was " + secs)
    assert(result)
    assert(secs < 0.5)
  }

  // Not
  test ("not true is false") {
    val result = Await.result(Operators.not(Future.successful(true)), 5 seconds)
    assert(!result)
  }

  test ("not false is true") {
    val result = Await.result(Operators.not(Future.successful(false)), 5 seconds)
    assert(result)
  }

  test("not Future failed is Future failed") {
    val result = intercept[RuntimeException] {
      Await.result(Operators.not(Future.failed(new RuntimeException("Failed"))), 5 seconds)
    }
    assert(result.getMessage == "Failed")
  }

  //Combos
  test("Combinations of all/any/not work as expected 0") {
    val result = Await.result(Operators.not(Operators.all(Future(true), Future(true), Operators.any(Future(false), Future(true)))), 5 seconds)
    assert(!result)
  }

  test("Combinations of all/any/not work as expected 1") {
    val result = Await.result(Operators.not(Operators.all(Future(true), Future(true), Operators.any(Future(false), Future(false)))), 5 seconds)
    assert(result)
  }

  test("Combinations of all/any/not work as expected 2") {
    val result = intercept[RuntimeException] {
      Await.result(Operators.not(Operators.all(Future(true), Future(true), Operators.any(Future(false), Future(false), Future.failed(new RuntimeException)))), 5 seconds)
    }
    assert(result.getMessage == "Fail detected in all when all others were true")
  }

  test("Combinations of all/any/not work as expected 3") {
    val result = Await.result(Operators.all(Future(true), Future(false), Operators.any(Future.failed(new RuntimeException), Future(false))), 5 seconds)
    assert(!result)
  }
}
