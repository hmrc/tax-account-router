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

import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsString
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser, Principal}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.util.Failure

class RouterControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar{
//  val routeController = new RouterController()
//
//  "Router Controller" must {
//    "return a route result when given a valid Auth Context and Request" in {
//      val authContext = AuthContext(mock[LoggedInUser], Principal(None, Accounts()), None, None, None, None)
//      val fakeRequest = FakeRequest().withCookies(Cookie("_ga", "gaClientId"))
//      val response = routeController.route(authContext, fakeRequest)
//
//      status(response) shouldBe 303
//    }
//
//    "return an error when given invalid Auth Context and Request" in {
//      val response = routeController.route(null, null)
//
//      status(response) shouldBe BAD_REQUEST
//    }
//
//    "return an error when given invalid Auth Context" in {
//      val fakeRequest = FakeRequest("GET", "").withCookies(Cookie("_ga", "gaClientId"))
//      val response = routeController.route(null, fakeRequest)
//
//      status(response) shouldBe BAD_REQUEST
//    }
//
//    "return an error when given invalid Request" in {
//      val authContext = AuthContext(mock[LoggedInUser], Principal(None, Accounts()), None, None, None, None)
//      val response = routeController.route(authContext, null)
//
//      status(response) shouldBe BAD_REQUEST
//    }

//    "return an error when the route fails" in {
//      val mockedRoutController= mock[RouterController]
//      when(mockedRoutController.apply(any[Any]) thenReturn Failure(new RuntimeException("error.resource_access_failure")))
//      val authContext = AuthContext(mock[LoggedInUser], Principal(None, Accounts()), None, None, None, None)
//      val fakeRequest = FakeRequest("GET", "").withCookies(Cookie("_ga", "gaClientId"))
//      val response = routeController.route(authContext, fakeRequest)
//
//      status(response) shouldBe INTERNAL_SERVER_ERROR
//      (contentAsJson(response) \ "message").as[JsString].value shouldBe messages("error.resource_access_failure")
//    }
//  }
}