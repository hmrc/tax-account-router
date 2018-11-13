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
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.taxaccountrouter.config.HttpClient
import uk.gov.hmrc.taxaccountrouter.model.UserDetails

import scala.concurrent.{ExecutionContext, Future}

class UserDetailsConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  "getUserDetails" should {
    val userDetailsUri = "user-details-uri"
    val mockHttp = mock[HttpClient]
    val connector = new UserDetailsConnector {
      override def httpClient: HttpClient = mockHttp
      override def baseUrl(serviceName: String): String = super.baseUrl(serviceName)
    }
    "execute a call to user-details to retreive a UserDetails" in {
      val userDetailsResponse = new UserDetails(None, "")
      when(mockHttp.GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(userDetailsResponse))
      val result = await(connector.getUserDetails(userDetailsUri))
      result shouldBe userDetailsResponse
      verify(mockHttp).GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute a call to user-details and return an error" in {
      when(mockHttp.GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.getUserDetails(userDetailsUri)))
      result.getMessage shouldBe "error.resource_access_failure"
    }
  }
}
