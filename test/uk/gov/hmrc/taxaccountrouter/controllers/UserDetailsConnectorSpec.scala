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

import ch.qos.logback.classic.Level
import com.sun.xml.internal.bind.v2.model.core.NonElementRef
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.LoneElement
import org.scalatest.mockito.MockitoSugar
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.auth.core.{CredentialStrength, Nino, User}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.test.{LogCapturing, UnitSpec}
import uk.gov.hmrc.taxaccountrouter.connectors._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserDetailsConnectorSpec extends UnitSpec with MockitoSugar with LogCapturing with LoneElement {

  def getHttpMock(returnedStatus: Int, returnedJson: Option[JsValue]) = {
    val httpGetMock = mock[HttpClient]
    when(httpGetMock.GET[UserDetail](anyString)(any(), any(), any())).thenReturn(Future.successful(returnedJson.get.as[UserDetail]))
    httpGetMock
  }

  "UserDetailsConnector" should {
    "Return status of 200 when the user details uri is called" in {
      val userAuthority = new UserAuthority(None, None, Some("/"), None, CredentialStrength(CredentialStrength.strong), None, None)
      val userResponse  = UserDetail(Some(CredentialRole("user")), "wibble")
      val userResponseJson = Json.toJson(userResponse)
      val httpGet = getHttpMock(200, Some(userResponseJson))

      val response = HttpResponse(200, Some(userResponseJson))

      val connector = new UserDetailsConnector(httpGet)
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val result = await(connector.getUserDetails(userAuthority))

      result shouldBe userResponse
    }

    "Return a failed future and log an error when the UserDetailsConnector returns a 4XX error" in {}

    "Return a failed future and log an error when the UserDetails returns a 5xx error" in {}

  }

}
