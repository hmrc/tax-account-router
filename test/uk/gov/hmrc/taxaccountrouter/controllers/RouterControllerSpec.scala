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

import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RouterControllerSpec extends FreeSpec with MustMatchers with GuiceOneAppPerSuite with OptionValues  {

  lazy val fakeRequest = FakeRequest("GET", routes.RouterController.hello().url)

  "GET /tax-account-router/hello-world" - {
    "return OK" in {
      val result = route(app, fakeRequest).value
      status(result) mustEqual Status.OK
    }
  }
}
