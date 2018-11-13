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
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.CredentialStrength
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.taxaccountrouter.auth.{EnrolmentIdentifier, GovernmentGatewayEnrolment, InternalUserIdentifier, UserAuthority}
import uk.gov.hmrc.taxaccountrouter.config.HttpClient

class RouterAuthConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  "currentUserAuthority" should {
    val mockHttp:HttpClient = mock[HttpClient]
    val authUrl = "auth-service-url"
    val connector:RouterAuthConnector = new RouterAuthConnector {
      override def http:HttpClient = mockHttp
      override def serviceUrl:String = authUrl
    }
    "execute call to auth microservice to get the authority" in {
      val authResponse = UserAuthority(None, Some(""), Some(""), None, CredentialStrength.None, None, None)
      when(mockHttp.GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.currentUserAuthority)
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up an exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.currentUserAuthority))
      result.getMessage shouldBe "error.resource_access_failure"
    }
  }

  "userAuthority" should {
    val mockHttp:HttpClient = mock[HttpClient]
    val authUrl = "auth-service-url"
    val credId = "credId"
    val connector:RouterAuthConnector = new RouterAuthConnector {
      override def http:HttpClient = mockHttp
      override def serviceUrl:String = authUrl
    }
    "execute call to auth microservice to get the authority" in {
      val authResponse = UserAuthority(None, Some(""), Some(""), None, CredentialStrength.None, None, None)
      when(mockHttp.GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.userAuthority(credId))
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up an exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.userAuthority(credId)))
      result.getMessage shouldBe "error.resource_access_failure"
    }
  }

  "getIds" should {
    val mockHttp:HttpClient = mock[HttpClient]
    val authUrl = "auth-service-url"
    val ids = "1"
    val connector:RouterAuthConnector = new RouterAuthConnector {
      override def http:HttpClient = mockHttp
      override def serviceUrl:String = authUrl
    }
    "execute call to auth microservice to get the InternalUserIdentifier" in {
      val authResponse = InternalUserIdentifier("")
      when(mockHttp.GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.getIds(ids))
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up an exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.getIds(ids)))
      result.getMessage shouldBe "error.resource_access_failure"
    }
  }

  "getEnrolments" should {
    val mockHttp:HttpClient = mock[HttpClient]
    val authUrl = "auth-service-url"
    val enrolment = "1"
    val connector:RouterAuthConnector = new RouterAuthConnector {
      override def http:HttpClient = mockHttp
      override def serviceUrl:String = authUrl
    }
    "execute call to auth microservice to get the GovernmentGatewayEnrolment" in {
      val authResponse = Seq(GovernmentGatewayEnrolment("1", Seq(EnrolmentIdentifier("1", "test")), ""))
      when(mockHttp.GET(eqTo(s"$authUrl$enrolment"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.getEnrolments(enrolment))
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl$enrolment"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up an exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl$enrolment"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.getEnrolments(enrolment)))
      result.getMessage shouldBe "error.resource_access_failure"
    }
  }
}