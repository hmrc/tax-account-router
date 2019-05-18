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

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

class LocationSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockConfiguration: Configuration = mock[Configuration]
  val locations: Location = new Location(mockConfiguration)

  "locationBuilder" should {
    "return a Location that is in the configuration" in {
      when(mockConfiguration.getString(s"locations.test.url")).thenReturn(Some("testUrl"))
      val result = locations.buildLocation("test")
      result shouldBe "testUrl"
    }
    "throw an exception for a location that has no url in the configuration" in {
      when(mockConfiguration.getString(s"locations.test.url")).thenReturn(None)
      val result = intercept[RuntimeException](locations.buildLocation("test"))
      result.getMessage shouldBe s"location test not configured with the key url"
    }
  }
}
