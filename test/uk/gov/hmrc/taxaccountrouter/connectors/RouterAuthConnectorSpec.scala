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
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.slf4j.Logger
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.frontend.auth.connectors.domain.CredentialStrength
import uk.gov.hmrc.play.test.UnitSpec

class RouterAuthConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  val fakeLogger: Logger = Mockito.spy(classOf[Logger])
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val authUrl = "auth-service-url"
  val testConfig = TestServicesConfig(authUrl)

  "currentUserAuthority" should {
    val mockHttp = mock[HttpClient]
    val connector = new RouterAuthConnector(mockHttp, fakeLogger, testConfig)
    "execute call to auth microservice to get the authority" in {
      val authResponse = UserAuthority(None, Some(""), Some(""), None, "Weak", None, None)
      when(mockHttp.GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.currentUserAuthority())
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up and logs exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.currentUserAuthority()))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn("Was unable to execute call to auth", result)
    }
    "execute call to auth microservice is rejected" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(Upstream5xxResponse("error.resource_access_failure", 500, 500)))
      val result = intercept[Upstream5xxResponse](await(connector.currentUserAuthority()))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn("auth was unable to handle the request", result)
    }
    "execute call to auth microservice returned 4xx error" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/authority"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(Upstream4xxResponse("error.resource_access_failure", 500, 500)))
      val result = intercept[Upstream4xxResponse](await(connector.currentUserAuthority()))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn("we made a request that auth was unable to handle", result)
    }
  }

  "userAuthority" should {
    val credId = "credId"
    val mockHttp = mock[HttpClient]
    val connector = new RouterAuthConnector(mockHttp, fakeLogger, testConfig)
    "execute call to auth microservice to get the authority" in {
      val authResponse = UserAuthority(None, Some(""), Some(""), None, "Weak", None, None)
      when(mockHttp.GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.userAuthority(credId))
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up and logs exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.userAuthority(credId)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"Was unable to execute call to auth for credId $credId", result)
    }
    "execute call to auth microservice passes up NotFoundException" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new NotFoundException("error.resource_access_failure")))
      val result = intercept[NotFoundException](await(connector.userAuthority(credId)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"we made a request for credId $credId that auth was unable to handle", result)
    }
    "execute call to auth microservice is rejected" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(Upstream5xxResponse("error.resource_access_failure", 500, 500)))
      val result = intercept[Upstream5xxResponse](await(connector.userAuthority(credId)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"auth was unable to handle the request for credId $credId", result)
    }
    "execute call to auth microservice returned 4xx error" in {
      when(mockHttp.GET(eqTo(s"$authUrl/auth/gg/$credId"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(Upstream4xxResponse("error.resource_access_failure", 500, 500)))
      val result = intercept[Upstream4xxResponse](await(connector.userAuthority(credId)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"we made a request for credId $credId that auth was unable to handle", result)
    }
  }

  "getIds" should {
    val ids = "1"
    val mockHttp = mock[HttpClient]
    val connector = new RouterAuthConnector(mockHttp, fakeLogger, testConfig)
    "execute call to auth microservice to get the InternalUserIdentifier" in {
      val authResponse = InternalUserIdentifier("")
      when(mockHttp.GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.getIds(ids))
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up and logs exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.getIds(ids)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"Was unable to execute call to auth for $ids", result)
    }
    "execute call to auth microservice is rejected" in {
      when(mockHttp.GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(Upstream5xxResponse("error.resource_access_failure", 500, 500)))
      val result = intercept[Upstream5xxResponse](await(connector.getIds(ids)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"auth was unable to handle the request for id $ids", result)
    }
    "execute call to auth microservice returned 4xx error" in {
      when(mockHttp.GET(eqTo(s"$authUrl$ids"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(Upstream4xxResponse("error.resource_access_failure", 500, 500)))
      val result = intercept[Upstream4xxResponse](await(connector.getIds(ids)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"we made a request for $ids that auth was unable to handle", result)
    }
  }

  "getEnrolments" should {
    val enrolment = "1"
    val mockHttp = mock[HttpClient]
    val connector = new RouterAuthConnector(mockHttp, fakeLogger, testConfig)
    "execute call to auth microservice to get the GovernmentGatewayEnrolment" in {
      val authResponse = Seq(GovernmentGatewayEnrolment("1", Seq(EnrolmentIdentifier("1", "test")), ""))
      when(mockHttp.GET(eqTo(s"$authUrl$enrolment"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(authResponse))
      val result = await(connector.getEnrolments(enrolment))
      result shouldBe authResponse
      verify(mockHttp).GET(eqTo(s"$authUrl$enrolment"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute call to auth microservice passes up and logs exception" in {
      when(mockHttp.GET(eqTo(s"$authUrl$enrolment"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.getEnrolments(enrolment)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"Was unable to execute call to auth for $enrolment", result)
    }
    "execute call to auth microservice is rejected" in {
      when(mockHttp.GET(eqTo(s"$authUrl$enrolment"))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(Upstream5xxResponse("error.resource_access_failure", 500, 500)))
      val result = intercept[Upstream5xxResponse](await(connector.getEnrolments(enrolment)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"auth was unable to handle the request for enrolment $enrolment", result)
    }
  }

  "responses for InternalUserIdentifiers" should {
    "parse correctly into the InternalUserIdentifier domain object" in {
      val internalId = "5658962a3d00003d002f3ca1"
      val authResponse = s"""{"internalId": "$internalId"}"""
      Json.parse(authResponse).as[InternalUserIdentifier] shouldBe InternalUserIdentifier(internalId)
    }
  }

  "responses for UserAuthorities" should {
    "parse correctly into the UserAuthority domain object" in {
      val userDetailsLink = "/user-details/id/5658962a3d00003d002f3ca1"
      val twoFactorOtpId = "/user-details/id/5658962a3d00003d002f3ca1"
      val credentialStrength = CredentialStrength.Strong
      val saUtr = SaUtr("12345")
      val nino = Nino("CS100700A")
      val idsUri = "/auth/ids-uri"
      val enrolmentsUri = "/auth/enrolments-uri"
      val authResponse =
        s"""{
           |    "userDetailsLink": "$userDetailsLink",
           |    "twoFactorAuthOtpId": "$twoFactorOtpId",
           |    "credentialStrength": "$credentialStrength",
           |    "nino": "${nino.value}",
           |    "saUtr": "${saUtr.value}",
           |    "ids": "$idsUri",
           |    "enrolments": "$enrolmentsUri"
           |    }""".stripMargin
      Json.parse(authResponse).as[UserAuthority] shouldBe UserAuthority(Some(twoFactorOtpId), Some(idsUri), Some(userDetailsLink), Some(enrolmentsUri), "Strong", Some("CS100700A"), Some("12345"))
    }
    "Parse values not provided to None correctly" in {
      val authResponse = s"""{"credentialStrength": "${CredentialStrength.Strong}"}"""
      Json.parse(authResponse).as[UserAuthority] shouldBe UserAuthority(None, None, None, None, "Strong", None, None)
    }
  }
}
