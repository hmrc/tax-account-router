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

package uk.gov.hmrc.taxaccountrouter.controllers

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.taxaccountrouter.connectors._
import uk.gov.hmrc.taxaccountrouter.model.{Conditions, RuleContext}

import scala.concurrent.{ExecutionContext, Future}

class RouterControllerSpec extends MockitoSugar with UnitSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val fakeLogger: Logger = mock[Logger]
  val realLogger:Logger = LoggerFactory.getLogger("testLogger")
  val mockAuthConnector: RouterAuthConnector = mock[RouterAuthConnector]
  val mockUserDetailsConnector: UserDetailsConnector = mock[UserDetailsConnector]
  val mockSelfAssessmentConnector: SelfAssessmentConnector = mock[SelfAssessmentConnector]
  val mockConditions: Conditions = mock[Conditions]
  val controller: RouterController = new RouterController(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector, stubControllerComponents(), fakeLogger, mockConditions)

  "GET /tax-account-router/hello-world" should {
    val request = FakeRequest()
    "return OK" in {
      val result = await(controller.hello()(request))
      status(result) shouldBe Status.OK
    }
  }

  "GET /" should {
    val request = FakeRequest()
    "return OK" in {
      val result = await(controller.routeAccount()(request))
      status(result) shouldBe Status.OK
    }
  }

  "GET /accountType" should {
    val credId = "id"
    lazy val request = FakeRequest()
    "return status 500 when user is not found" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val userResponse = Future.failed(new NotFoundException("no user"))
      when(mockAuthConnector.userAuthority(eqTo(credId))(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockUserDetailsConnector.getUserDetails(eqTo(authResponse))).thenReturn(userResponse)
      val result = await(controller.accountType(credId)(request))
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      userResponse.map(e => verify(fakeLogger).warn("Unable to get user details from downstream.", e))
    }
    "return OK with a response of Agent" in {
      val context = RuleContext(Some(credId))(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector)(request, hc, ec)
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val userResponse = UserDetail(Some(User), "Agent")
      when(mockConditions.isAgent(context)).thenReturn(Future(true))
      val result = await(controller.accountType(credId)(request))
      contentAsJson(result) shouldBe Json.toJson(AccountTypeResponse("Agent"))
      status(result) shouldBe Status.OK
    }
    "return OK with a response of Individual" in {
      val context = RuleContext(Some(credId))(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector)(request, hc, ec)
      when(mockConditions.isAgent(context)).thenReturn(Future(false))
      when(mockConditions.fromVerify(context)).thenReturn(Future(true))
      val result = await(controller.accountType(credId)(request))
      contentAsJson(result) shouldBe Json.toJson(AccountTypeResponse("Individual"))
      status(result) shouldBe Status.OK
    }
    "return OK with a response of Organisation" in {
      val context = RuleContext(Some(credId))(mockAuthConnector, mockUserDetailsConnector, mockSelfAssessmentConnector)(request, hc, ec)
      when(mockConditions.isAgent(context)).thenReturn(Future(false))
      when(mockConditions.fromVerify(context)).thenReturn(Future(false))
      when(mockConditions.fromGG(context)).thenReturn(Future(true))
      when(mockConditions.enrolmentAvailable(context)).thenReturn(Future(false))
      val result = await(controller.accountType(credId)(request))
      contentAsJson(result) shouldBe Json.toJson(AccountTypeResponse("Organisation"))
      status(result) shouldBe Status.OK
    }
  }
}
