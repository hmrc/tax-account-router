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

package uk.gov.hmrc.taxaccountrouter.connectors

import javax.inject.{Inject, Singleton}
import org.slf4j.Logger
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelfAssessmentConnector @Inject()(httpClient: HttpClient, servicesConfig: ServicesConfig, log: Logger)(implicit hc: HeaderCarrier, ec: ExecutionContext) {
  def serviceUrl:String = servicesConfig.baseUrl("sa")

  def lastReturn(utr: String): Future[Option[SaReturn]] = {
    httpClient.GET[SaReturn](s"$serviceUrl/sa/individual/$utr/return/last").recover{
      case _: NotFoundException => None
      case e: Throwable =>
        log.warn(s"Unable to retrieve last SA return for user with utr $utr", e)
        throw e
    }
  }

  def lastReturn(userAuthority: UserAuthority): Future[Option[SaReturn]] = {
    userAuthority.saUtr.fold(Future.successful(Option(SaReturn())))(saUtr => lastReturn(saUtr))
  }
}

case class SaReturn(supplementarySchedules: List[String] = List.empty, previousReturn: Boolean = false)

object SaReturn {
  implicit val reads: Reads[SaReturn] = (__ \ "supplementarySchedules").readNullable[List[String]].map(f => SaReturn(f.getOrElse(List.empty), f.nonEmpty))
}
