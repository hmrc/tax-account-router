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

package uk.gov.hmrc.taxaccountrouter.config

import javax.inject.{Inject, Provider}
import play.api.Configuration

case class EnrolmentConfiguration(businessEnrolments: Set[String], saEnrolments: Set[String]) { }

class EnrolmentConfigurationProvider @Inject()(runConfiguration: Configuration) extends Provider[EnrolmentConfiguration] {
  override def get(): EnrolmentConfiguration = {
    def readEnrolments(group: String):Set[String] = {
      runConfiguration.getString(group).map(_.split(",").map(_.trim).filter(_.nonEmpty).toSet).getOrElse(Set.empty[String])
    }

    val businessEnrolments = readEnrolments("business-enrolments")
    val saEnrolments = readEnrolments("sa-enrolments")
    EnrolmentConfiguration(businessEnrolments, saEnrolments)
  }
}
