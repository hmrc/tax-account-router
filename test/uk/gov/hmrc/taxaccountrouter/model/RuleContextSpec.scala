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

import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.taxaccountrouter.connectors._

import scala.concurrent.{ExecutionContext, Future}

class RuleContextSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val rq: Request[AnyContent] = FakeRequest()
  val mockAuthConnector: RouterAuthConnector = mock[RouterAuthConnector]
  val mockUserDetailsConnector: UserDetailsConnector = mock[UserDetailsConnector]
  val mockSelfAssessmentConnector: SelfAssessmentConnector = mock[SelfAssessmentConnector]

  "RuleContext authority" should {
    val response = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
    "call RouterAuthConnector for the current UserAuthority if no credId is given" in {
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(response)
      val result = await(RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).authority)
      result shouldBe response
      verify(mockAuthConnector).currentUserAuthority()(any[HeaderCarrier])
    }
    "call RouterAuthConnector for the UserAuthority of the given credId" in {
      val credId = "test"
      when(mockAuthConnector.userAuthority(eqTo(credId))(any[HeaderCarrier])).thenReturn(response)
      val result = await(RuleContext(Some(credId))(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).authority)
      result shouldBe response
      verify(mockAuthConnector).userAuthority(eqTo(credId))(any[HeaderCarrier])
    }
    "Hold a Future.failed if RouterAuthConnector returns one" in {
      val failedResponse = Future.failed(new RuntimeException("exception"))
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(failedResponse)
      val result = RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).authority
      result shouldBe failedResponse
    }
  }

  "RuleContext userDetail" should {
    "call UserDetailsConnector using the value in authority" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val userResponse = UserDetail(Some(User), "Test")
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockUserDetailsConnector.getUserDetails(authResponse)).thenReturn(userResponse)
      val result = await(RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).userDetails)
      result shouldBe userResponse
    }
    "call UserDetailsConnector throws an exception" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val userResponse = Future.failed(new NotFoundException("no userDetailsUri found in UserAuthority"))
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockUserDetailsConnector.getUserDetails(authResponse)).thenReturn(userResponse)
      val result = intercept[NotFoundException](await(RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).userDetails))
      result.getMessage shouldBe "no userDetailsUri found in UserAuthority"
    }
  }

  "RuleContext enrolments" should {
    "call UserDetailsConnector using the value in authority" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val enrolmentResponse = Seq(GovernmentGatewayEnrolment("1", Seq.empty[EnrolmentIdentifier], "found"))
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockAuthConnector.getEnrolments(authResponse)).thenReturn(enrolmentResponse)
      val result = await(RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).enrolments)
      result shouldBe enrolmentResponse
    }
    "call UserDetailsConnector throws an exception" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val enrolmentResponse = Future.failed(new NotFoundException("could not call auth"))
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockAuthConnector.getEnrolments(authResponse)).thenReturn(enrolmentResponse)
      val result = intercept[NotFoundException](await(RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).enrolments))
      result.getMessage shouldBe "could not call auth"
    }
  }

  "RuleContext lastSaReturn" should {
    "call SelfAssessmentConnector using the value in authority" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val saResponse = SaReturn(List("return"), true)
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockSelfAssessmentConnector.lastReturn(authResponse)).thenReturn(saResponse)
      val result = await(RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).lastSaReturn)
      result shouldBe saResponse
    }
    "call SelfAssessmentConnector throws an exception" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val saResponse = Future.failed(new RuntimeException("cant call sa"))
      when(mockAuthConnector.currentUserAuthority()(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockSelfAssessmentConnector.lastReturn(authResponse)).thenReturn(saResponse)
      val result = intercept[RuntimeException](await(RuleContext(None)(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector).lastSaReturn))
      result.getMessage shouldBe "cant call sa"
    }
  }
}
