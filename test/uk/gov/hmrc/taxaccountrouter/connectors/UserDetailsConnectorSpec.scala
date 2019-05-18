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
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import org.slf4j.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.{Assistant, User}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

class UserDetailsConnectorSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  val fakeLogger: Logger = Mockito.spy(classOf[Logger])
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  "getUserDetails" should {
    val userDetailsUri = "user-details-uri"
    val mockHttp = mock[HttpClient]
    val connector = new UserDetailsConnector(mockHttp, fakeLogger)
    "execute a call to user-details to retreive a UserDetails" in {
      val userDetailsResponse = new UserDetail(None, "")
      when(mockHttp.GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(userDetailsResponse))
      val result = await(connector.getUserDetails(userDetailsUri))
      result shouldBe userDetailsResponse
      verify(mockHttp).GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute a call to user-details and return an error" in {
      when(mockHttp.GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.getUserDetails(userDetailsUri)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"Was unable to execute call to User-Details for $userDetailsUri", result)
    }
  }

  "getUserDetails for UserAuthority" should {
    val userDetailsUri = "user-details-uri"
    val mockHttp = mock[HttpClient]
    val connector = new UserDetailsConnector(mockHttp, fakeLogger)
    "execute a call to user-details to retreive a UserDetails" in {
      val request = new UserAuthority(None, None, Some(userDetailsUri), None, "Weak", None, None)
      val userDetailsResponse = new UserDetail(None, "")
      when(mockHttp.GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(userDetailsResponse))
      val result = await(connector.getUserDetails(request))
      result shouldBe userDetailsResponse
      verify(mockHttp).GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])
    }
    "execute a call to user-details and return an error" in {
      val request = new UserAuthority(None, None, Some(userDetailsUri), None, "Weak", None, None)
      when(mockHttp.GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[RuntimeException](await(connector.getUserDetails(request)))
      result.getMessage shouldBe "error.resource_access_failure"
      verify(fakeLogger).warn(s"Was unable to execute call to User-Details for $userDetailsUri", result)
    }
    "throw error when no userDetailsUri is provided" in {
      val request = new UserAuthority(None, None, None, None, "Weak", None, None)
      when(mockHttp.GET(eqTo(userDetailsUri))(any[HttpReads[Any]](), any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.failed(new RuntimeException("error.resource_access_failure")))
      val result = intercept[NotFoundException](await(connector.getUserDetails(request)))
      result.getMessage shouldBe "no userDetailsUri found in UserAuthority"
    }
  }

  "reads of UserDetails" should {
    "read credentialRole if available" in {
      Json.parse("""{"credentialRole":"User","affinityGroup":"Organisation"}""").as[UserDetail] shouldBe UserDetail(Some(User), "Organisation")
    }
    "read credentialRole as None if not available" in {
      Json.parse("""{"affinityGroup":"Baz"}""").as[UserDetail] shouldBe UserDetail(None, "Baz")
    }
  }

  "isAdmin" should {
    val scenarios = Table(
      ("role", "result"),
      (UserDetail(Some(User), "Organisation"), true),
      (UserDetail(Some(Assistant), "Organisation"), false),
      (UserDetail(None, "Organisation"), false)
    )
    forAll(scenarios) {
      (role: UserDetail, expectedResult: Boolean) =>
        s"return $expectedResult if user has credential role " + role.credentialRole.getOrElse("None") in {
          role.isAdmin shouldBe expectedResult
        }
    }
  }
}
