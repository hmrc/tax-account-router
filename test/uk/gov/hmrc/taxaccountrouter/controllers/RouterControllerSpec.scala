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
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.taxaccountrouter.connectors.{RouterAuthConnector, UserAuthority, UserDetail, UserDetailsConnector}

import scala.concurrent.ExecutionContext

class RouterControllerSpec extends MockitoSugar with UnitSpec  {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockAuthConnector: RouterAuthConnector = mock[RouterAuthConnector]
  val mockUserDetailsConnector: UserDetailsConnector = mock[UserDetailsConnector]
  val controller: RouterController = new RouterController(mockAuthConnector, mockUserDetailsConnector, stubControllerComponents())

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
    val authority = new UserAuthority(None, None, Some("userDetail-uri"), None, "None", None, None)
    lazy val request = FakeRequest()
    "return OK" in {
      val authResponse = UserAuthority(Some("twoFactorId"), Some("idsUri"), Some("userDetailsUri"), Some("enrolmentsUri"), "Weak", Some("nino"), Some("saUtr"))
      val userResponse = UserDetail(Some(User), "Test")
      when(mockAuthConnector.userAuthority(eqTo(credId))(any[HeaderCarrier])).thenReturn(authResponse)
      when(mockUserDetailsConnector.getUserDetails(eqTo(authResponse))).thenReturn(userResponse)
      val result = await(controller.accountType(credId)(request))
      status(result) shouldBe Status.OK
    }
  }
}
