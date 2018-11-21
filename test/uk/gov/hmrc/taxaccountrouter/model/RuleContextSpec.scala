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

package uk.gov.hmrc.taxaccountrouter.model

import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.taxaccountrouter.connectors.{RouterAuthConnector, UserAuthority, UserDetail, UserDetailsConnector}

import scala.concurrent.{ExecutionContext, Future}

class RuleContextSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockAuthConnector: RouterAuthConnector = mock[RouterAuthConnector]
  val mockUserDetailsConnector: UserDetailsConnector = mock[UserDetailsConnector]

  "RuleContext authority" should {
    val response = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
    "call RouterAuthConnector for the current UserAuthority if no credId is given" in {
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(response)
      val result = await(new RuleContext(None)(mockAuthConnector, mockUserDetailsConnector).authority)
      result shouldBe response
      verify(mockAuthConnector).currentUserAuthority()(any[HeaderCarrier])
    }
    "call RouterAuthConnector for the UserAuthority of the given credId" in {
      val credId = "test"
      when(mockAuthConnector.userAuthority(eqTo(credId))(any[HeaderCarrier])).thenReturn(response)
      val result = await(new RuleContext(Some(credId))(mockAuthConnector, mockUserDetailsConnector).authority)
      result shouldBe response
      verify(mockAuthConnector).userAuthority(eqTo(credId))(any[HeaderCarrier])
    }
    "Hold a Future.failed if RouterAuthConnector returns one" in {
      val failedResponse = Future.failed(new RuntimeException("exception"))
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(failedResponse)
      val result = new RuleContext(None)(mockAuthConnector, mockUserDetailsConnector).authority
      result shouldBe failedResponse
    }
  }

  "RuleContext userDetail" should {
    "call UserDetails Connector using the value in authority" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val userResponse = UserDetail(Some(User), "Test")
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockUserDetailsConnector.getUserDetails(authResponse)).thenReturn(userResponse)
      val result = await(new RuleContext(None)(mockAuthConnector, mockUserDetailsConnector).userDetails)
      result shouldBe userResponse
    }
    "call UserDetails Connector throws an exception" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val userResponse = Future.failed(new NotFoundException("no userDetailsUri found in UserAuthority"))
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockUserDetailsConnector.getUserDetails(authResponse)).thenReturn(userResponse)
      val result = intercept[NotFoundException](await(new RuleContext(None)(mockAuthConnector, mockUserDetailsConnector).userDetails))
      result.getMessage shouldBe "no userDetailsUri found in UserAuthority"
    }
  }
}
