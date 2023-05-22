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
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.inject.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelfAssessmentConnector @Inject()(httpClient: HttpClient, log: Logger, servicesConfig: ServicesConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext) {
  private val serviceUrl:String = servicesConfig.baseUrl("sa")

  //returns 404 in case of problem
  def lastReturn(utr: String): Future[SaReturn] = {
    httpClient.GET[SaReturn](s"$serviceUrl/sa/individual/$utr/return/last").recoverWith {
      case nfe @ (_: NotFoundException | _: Upstream4xxResponse) =>
        Future.successful(SaReturn.noSaReturn)
      case up5xx: Upstream5xxResponse =>
        log.warn(s"SA was unable to handle the request for $utr", up5xx)
        Future.failed(up5xx)
      case e: Throwable =>
        log.warn(s"Was unable to execute call to SA for $utr", e)
        throw e
    }
  }

  def lastReturn(userAuthority: UserAuthority): Future[SaReturn] = {
    userAuthority.saUtr.fold(Future.successful(SaReturn.noSaReturn))(saUtr => lastReturn(saUtr))
  }
}

case class SaReturn(supplementarySchedules: List[String], previousReturn: Boolean = false)

object SaReturn {
  lazy val noSaReturn = SaReturn(List.empty)
  implicit val reads: Reads[SaReturn] = (__ \ "supplementarySchedules").readNullable[List[String]].map(f => SaReturn(f.getOrElse(List.empty), f.nonEmpty))
}
