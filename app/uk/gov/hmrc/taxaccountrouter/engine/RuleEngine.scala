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

import javax.inject.Inject
import org.slf4j.Logger

import scala.concurrent.{ExecutionContext, Future}

class RuleEngine @Inject()(log: Logger)(implicit ec: ExecutionContext) {

  def assessLogged[V](rules: Seq[((String, () => Future[Boolean]), String)], ruleset: String = "test"): Future[Option[String]] = {
    log.info(s"assessing ruleset $ruleset")
    rules.foldLeft(Future(Option.empty[String])) { (accum, curr) =>
      accum flatMap {
        case v@Some(_) =>  Future(v)
        case None => curr._1._2().map {
          if (_) {
            log.info(curr._1._1 + " had the OUTCOME: " + curr._2)
            Some(curr._2)
          } else {
            log.debug("rule not met: " + curr._1._1)
            None
          }
        }
      }
    }
  }
}
