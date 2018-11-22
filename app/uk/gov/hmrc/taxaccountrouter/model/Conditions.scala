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

import com.softwaremill.macmemo.memoize

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.language.postfixOps

object Conditions {
  def enrolmentAvailable(context: RuleContext): Future[Boolean] = context.enrolments.map(_ => true).recover { case _ => false }

  def fromVerify(context: RuleContext): Future[Boolean] = Future.successful(!context.sessionData.contains("token") && context.credId.isEmpty)

  @memoize(maxSize = 2000, expiresAfter = 2 hours)
  def fromGG(context: RuleContext): Future[Boolean] = checkFromGG(context)

  def hasAffinityGroup(context: RuleContext): Future[Boolean] = context.userDetails.map(_.affinityGroup.nonEmpty).recover { case _ => false }

  def hasBusinessEnrolment(context: RuleContext): Future[Boolean] = ???

  @memoize(maxSize = 2000, expiresAfter = 2 hours)
  def hasInactiveEnrolments(context: RuleContext): Future[Boolean] = checkInactiveEnrolments(context)

  def hasNino(context: RuleContext): Future[Boolean] = context.authority.map(_.nino.isDefined)

  @memoize(maxSize = 2000, expiresAfter = 2 hours)
  def hasSaEnrolment(context: RuleContext): Future[Boolean] = checkForSaEnrolment(context)

  def hasSaReturn(context: RuleContext): Future[Boolean] = context.lastSaReturn.map(_.previousReturn)

  //Can I Haz MAJIC?
  @memoize(maxSize = 2000, expiresAfter = 2 hours)
  def inPartnership(context: RuleContext): Future[Boolean] = checkForInPartenrship(context)

  def isAgent(context: RuleContext): Future[Boolean] = context.userDetails.map( _.affinityGroup.contains("Agent"))

  def isIndividual(context: RuleContext): Future[Boolean] = context.userDetails.map(_.affinityGroup.contains("Individual"))

  @memoize(maxSize = 2000, expiresAfter = 2 hours)
  def isSelfEmployed(context: RuleContext): Future[Boolean] = checkForIsSelfEmployed(context)

  def saReturnAvailable(context: RuleContext): Future[Boolean] = context.lastSaReturn.map(_ => true).recover{ case _ => false }

  private def checkFromGG(context: RuleContext): Future[Boolean] = Future(context.sessionData.contains("token") || context.credId.isDefined)
  private def checkForSaEnrolment(context: RuleContext): Future[Boolean] = ???
  private def checkForInPartenrship(context: RuleContext): Future[Boolean] = context.lastSaReturn.map(_.supplementarySchedules.contains("partnership"))
  private def checkForIsSelfEmployed(context: RuleContext): Future[Boolean] = context.lastSaReturn.map(_.supplementarySchedules.contains("self_employment"))
  private def checkInactiveEnrolments(context: RuleContext): Future[Boolean] = context.enrolments.map(enrolments => enrolments.exists(_.state != "Activated"))
}
