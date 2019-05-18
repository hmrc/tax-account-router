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

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.Future.successful
import scala.util.{Success, Try}

object Operators {
  def all(futures: Future[Boolean]*)(implicit ec: ExecutionContext): Future[Boolean] = {
    val buffer = futures.toBuffer
    find(buffer) {!_} flatMap {_.fold(if (hasFailures(buffer)) Future.failed(new RuntimeException("Fail detected in all when all others were true")) else Future(true)) {_ => Future(false)}}
  }

  def any(futures: Future[Boolean]*)(implicit ec: ExecutionContext): Future[Boolean] = {
    val buffer = futures.toBuffer
    find(buffer) {identity} flatMap {_.fold(if (hasFailures(buffer)) Future.failed(new RuntimeException("Fail detected in any when all others were false")) else Future.successful(false)) {_ => Future(true)}}
  }

  def not(fb: Future[Boolean])(implicit ec: ExecutionContext): Future[Boolean] = {
    fb map {b => !b}
  }

  def hasFailures[T](futuresBuffer: mutable.Seq[Future[T]]): Boolean = futuresBuffer.exists(_.value.fold(false) {xxx: Try[T] => xxx.isFailure })

  /** Asynchronously and non-blockingly returns a `Future` that will hold the optional result
    * of the first `Future` with a result that matches the predicate.
    *
    * @tparam T the type of the value in the future
    * @param futuresBuffer the `mutable sequence` of Futures to search
    * @param p       the predicate which indicates if it's a match
    * @return the `Future` holding the optional result of the search
    */
  private def find[T](futuresBuffer: mutable.Seq[Future[T]])(p: T => Boolean)(implicit executor: ExecutionContext): Future[Option[T]] = {
    if (futuresBuffer.isEmpty) successful[Option[T]](None)
    else {
      val result = Promise[Option[T]]()
      val ref = new AtomicInteger(futuresBuffer.size)
      val search: Try[T] => Unit = v => try {
        v match {
          case Success(r) if p(r) => result tryComplete Success(Some(r))
          case _ =>
        }
      } finally {
        if (ref.decrementAndGet == 0) {
          result tryComplete Success(None)
        }
      }
      futuresBuffer.foreach(_ onComplete search)
      result.future
    }
  }
}
