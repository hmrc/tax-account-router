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
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.taxaccountrouter.config.EnrolmentConfiguration
import uk.gov.hmrc.taxaccountrouter.connectors.{GovernmentGatewayEnrolment, SaReturn, UserAuthority, UserDetail}

import scala.concurrent.{ExecutionContext, Future}

class ConditionsSpec extends UnitSpec with MockitoSugar with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockConfig: EnrolmentConfiguration = mock[EnrolmentConfiguration]
  val mockRuleContext: RuleContext = mock[RuleContext]
  val conditions: Conditions = new Conditions(mockConfig)

  "enrolmentAvailable" should {
    "return true if there is an enrolment" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = Seq(GovernmentGatewayEnrolment("1", Seq.empty, "HandedToAgent"))
      when(mockRuleContext.enrolments).thenReturn(contextResponse)
      val result = await(conditions.enrolmentAvailable(mockRuleContext))
      result shouldBe true
    }
    "return false if state is no enrolment" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = Future.failed(new RuntimeException("error"))
      when(mockRuleContext.enrolments).thenReturn(contextResponse)
      val result = await(conditions.enrolmentAvailable(mockRuleContext))
      result shouldBe false
    }
  }

  "fromVerify" should {
    "return false if credId and token is present" in {
      when(mockRuleContext.credId).thenReturn(Some("credId"))
      when(mockRuleContext.sessionData).thenReturn(Map(("token", "present")))
      val result = await(conditions.fromVerify(mockRuleContext))
      result shouldBe false
    }
    "return false if credId is not present but token is" in {
      when(mockRuleContext.credId).thenReturn(None)
      when(mockRuleContext.sessionData).thenReturn(Map(("token", "present")))
      val result = await(conditions.fromVerify(mockRuleContext))
      result shouldBe false
    }
    "return false if credId is present but token is not" in {
      when(mockRuleContext.credId).thenReturn(Some("credId"))
      when(mockRuleContext.sessionData).thenReturn(Map(("notoken", "present")))
      val result = await(conditions.fromVerify(mockRuleContext))
      result shouldBe false
    }
    "return true if credId and token are not present" in {
      when(mockRuleContext.credId).thenReturn(None)
      when(mockRuleContext.sessionData).thenReturn(Map(("notoken", "present")))
      val result = await(conditions.fromVerify(mockRuleContext))
      result shouldBe true
    }
  }

  "fromGG" should {
    "return true if credId and token is present" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockRuleContext.credId).thenReturn(Some("credId"))
      when(mockRuleContext.sessionData).thenReturn(Map(("token", "present")))
      val result = await(conditions.fromGG(mockRuleContext))
      result shouldBe true
    }
    "return true if credId is not present but token is" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockRuleContext.credId).thenReturn(None)
      when(mockRuleContext.sessionData).thenReturn(Map(("token", "present")))
      val result = await(conditions.fromGG(mockRuleContext))
      result shouldBe true
    }
    "return true if credId is present but token is not" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockRuleContext.credId).thenReturn(Some("credId"))
      when(mockRuleContext.sessionData).thenReturn(Map(("notoken", "present")))
      val result = await(conditions.fromGG(mockRuleContext))
      result shouldBe true
    }
    "return false if credId and token are not present" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockRuleContext.credId).thenReturn(None)
      when(mockRuleContext.sessionData).thenReturn(Map(("notoken", "present")))
      val result = await(conditions.fromGG(mockRuleContext))
      result shouldBe false
    }
  }

  "hasAffinityGroup" should{
    "return true if affinityGroup is present" in {
      val contextResponse = UserDetail(None, "Agent")
      when(mockRuleContext.userDetails).thenReturn(contextResponse)
      val result = await(conditions.hasAffinityGroup(mockRuleContext))
      result shouldBe true
    }
    "return false if affinityGroup is not present" in {
      val contextResponse = Future.failed(new RuntimeException("error"))
      when(mockRuleContext.userDetails).thenReturn(contextResponse)
      val result = await(conditions.hasAffinityGroup(mockRuleContext))
      result shouldBe false
    }
  }

  "hasBusinessEnrolment" should {
    "return true id the enrolment is found in the config" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockConfig.businessEnrolments).thenReturn(Set("test1","test2"))
      when(mockRuleContext.enrolments).thenReturn(Seq(GovernmentGatewayEnrolment("test1", Seq.empty, "Active")))
      val result = await(conditions.hasBusinessEnrolment(mockRuleContext))
      result shouldBe true
    }
    "return false id the enrolment is not found in the config" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockConfig.businessEnrolments).thenReturn(Set("test1","test2"))
      when(mockRuleContext.enrolments).thenReturn(Seq(GovernmentGatewayEnrolment("failTest1", Seq.empty, "Active")))
      val result = await(conditions.hasBusinessEnrolment(mockRuleContext))
      result shouldBe false
    }
  }

  "hasInactiveEnrolments" should {
    "return true if state is not Active" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = Seq(GovernmentGatewayEnrolment("1", Seq.empty, "HandedToAgent"))
      when(mockRuleContext.enrolments).thenReturn(contextResponse)
      val result = await(conditions.hasInactiveEnrolments(mockRuleContext))
      result shouldBe true
    }
    "return false if state is Active" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = Seq(GovernmentGatewayEnrolment("1", Seq.empty, "Activated"))
      when(mockRuleContext.enrolments).thenReturn(contextResponse)
      val result = await(conditions.hasInactiveEnrolments(mockRuleContext))
      result shouldBe false
    }
  }

  "hasNino" should {
    "return true if there is a nino" in {
      val contextResponse = UserAuthority(None, None, None, None, "Weak", Some("nino"), None)
      when(mockRuleContext.authority).thenReturn(contextResponse)
      val result = await(conditions.hasNino(mockRuleContext))
      result shouldBe true
    }
    "return false if there is no nino" in {
      val contextResponse = UserAuthority(None, None, None, None, "Weak", None, None)
      when(mockRuleContext.authority).thenReturn(contextResponse)
      val result = await(conditions.hasNino(mockRuleContext))
      result shouldBe false
    }
  }

  "hasSaEnrolment" should {
    "return true id the enrolment is found in the config" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockConfig.saEnrolments).thenReturn(Set("test1","test2"))
      when(mockRuleContext.enrolments).thenReturn(Seq(GovernmentGatewayEnrolment("test2", Seq.empty, "Active")))
      val result = await(conditions.hasSaEnrolment(mockRuleContext))
      result shouldBe true
    }
    "return false id the enrolment is not found in the config" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      when(mockConfig.saEnrolments).thenReturn(Set("test1","test2"))
      when(mockRuleContext.enrolments).thenReturn(Seq(GovernmentGatewayEnrolment("failTest2", Seq.empty, "Active")))
      val result = await(conditions.hasSaEnrolment(mockRuleContext))
      result shouldBe false
    }
  }

  "hasSaReturn" should{
    "return true if SaReturn has a previous return" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = SaReturn(List("partnership"), true)
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.hasSaReturn(mockRuleContext))
      result shouldBe true
    }
    "return false if SaReturn does not have a previous return" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = SaReturn.noSaReturn
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.hasSaReturn(mockRuleContext))
      result shouldBe false
    }
  }

  "inPartnership" should{
    "return true if Sa Return contains partnership" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = SaReturn(List("partnership"), true)
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.inPartnership(mockRuleContext))
      result shouldBe true
    }
    "return false if Sa Return does not contain partnership" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = SaReturn(List("self_employed"), true)
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.inPartnership(mockRuleContext))
      result shouldBe false
    }
  }

  "isAgent" should{
    "return true if affinityGroup is agent" in {
      val contextResponse = UserDetail(None, "Agent")
      when(mockRuleContext.userDetails).thenReturn(contextResponse)
      val result = await(conditions.isAgent(mockRuleContext))
      result shouldBe true
    }
    "return false if affinityGroup is not agent" in {
      val contextResponse = UserDetail(None, "Individual")
      when(mockRuleContext.userDetails).thenReturn(contextResponse)
      val result = await(conditions.isAgent(mockRuleContext))
      result shouldBe false
    }
  }

  "isIndividual" should{
    "return true if affinityGroup is Individual" in {
      val contextResponse = UserDetail(None, "Individual")
      when(mockRuleContext.userDetails).thenReturn(contextResponse)
      val result = await(conditions.isIndividual(mockRuleContext))
      result shouldBe true
    }
    "return false if affinityGroup is not Individual" in {
      val contextResponse = UserDetail(None, "Agent")
      when(mockRuleContext.userDetails).thenReturn(contextResponse)
      val result = await(conditions.isIndividual(mockRuleContext))
      result shouldBe false
    }
  }

  "isSelfEmployed" should{
    "return true if supplamentaryShedules contains Self Employed" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = SaReturn(List("self_employment"), true)
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.isSelfEmployed(mockRuleContext))
      result shouldBe true
    }
    "return false if supplamentaryShedules does not contain Self Employed" in {
      val mockRuleContext: RuleContext = mock[RuleContext]
      val contextResponse = SaReturn(List("partnership"), true)
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.isSelfEmployed(mockRuleContext))
      result shouldBe false
    }
  }

  "saReturnAvailible" should {
    "return true if there is an SaReturn" in {
      val contextResponse = SaReturn(List("self_employment"), true)
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.saReturnAvailable(mockRuleContext))
      result shouldBe true
    }
    "return false if the SaReturn is a Future.failed" in {
      val contextResponse = Future.failed(new RuntimeException("exception"))
      when(mockRuleContext.lastSaReturn).thenReturn(contextResponse)
      val result = await(conditions.saReturnAvailable(mockRuleContext))
      result shouldBe false
    }
  }
}
