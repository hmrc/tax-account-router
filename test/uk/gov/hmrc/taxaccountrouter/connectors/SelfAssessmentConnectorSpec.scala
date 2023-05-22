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

import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.slf4j.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

class SelfAssessmentConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  val fakeLogger: Logger = Mockito.spy(classOf[Logger])
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val saUrl = "sa-service-url"
  val testConfig = TestServicesConfig(saUrl)

  "lastReturn" should {
    val mockHttp = mock[HttpClient]
    val utr = "1"
    val connector = new SelfAssessmentConnector(mockHttp, fakeLogger, testConfig)
    "execute a call to sa returning an SaReturn" in {
      val saResponse = SaReturn(List("partnership"))
      when(mockHttp.GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(saResponse))
      val result = await(connector.lastReturn(utr))
      result shouldBe SaReturn(List("partnership"))
      verify(mockHttp).GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute a call to user-details that returns and logs an error" in {
      when(mockHttp.GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.lastReturn(utr)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"Was unable to execute call to SA for $utr", result)
    }
    "when no SA is found an empty SaReturn is returned" in {
      when(mockHttp.GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new NotFoundException("")))
      val result = await(connector.lastReturn(utr))
      result shouldBe SaReturn.noSaReturn
    }
  }

  "lastReturn with UserAuthority" should {
    val mockHttp = mock[HttpClient]
    val utr = "1"
    val connector = new SelfAssessmentConnector(mockHttp, fakeLogger, testConfig)
    "execute a call to sa returning an SaReturn" in {
      val userAuthority = new UserAuthority(None, None, None, None, "Weak", None, Some(utr))
      val saResponse = SaReturn(List("partnership"))
      when(mockHttp.GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(saResponse))
      val result = await(connector.lastReturn(userAuthority))
      result shouldBe saResponse
      verify(mockHttp).GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute a call to user-details that returns and logs an error" in {
      val userAuthority = new UserAuthority(None, None, None, None, "Weak", None, Some(utr))
      when(mockHttp.GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.lastReturn(userAuthority)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"Was unable to execute call to SA for $utr", result)
    }
    "when no utr is provided an empty SaReturn is returned" in {
      val userAuthority = new UserAuthority(None, None, None, None, "Weak", None, None)
      when(mockHttp.GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = await(connector.lastReturn(userAuthority))
      result shouldBe SaReturn(List.empty)
    }
    "when no SA is found an empty SaReturn is returned" in {
      val userAuthority = new UserAuthority(None, None, None, None, "Weak", None, None)
      when(mockHttp.GET(eqTo(s"$saUrl/sa/individual/$utr/return/last"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new NotFoundException("")))
      val result = await(connector.lastReturn(userAuthority))
      result shouldBe SaReturn(List.empty)
    }
  }

  "SaReturn" should {
    "read supplementarySchedules return if available" in {
      Json.parse("""{"utr":"5328981911", "supplementarySchedules":["individual_tax_form","self_employment"]}""").as[SaReturn] shouldBe SaReturn(List("individual_tax_form", "self_employment"), previousReturn=true)
    }
    "read no supplementarySchedules return if not available" in {
      Json.parse("""{"utr":"5328981911"}""").as[SaReturn] shouldBe SaReturn(List.empty)
    }
  }
}
