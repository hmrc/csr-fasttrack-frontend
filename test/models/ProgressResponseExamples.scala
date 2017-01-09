/*
 * Copyright 2017 HM Revenue & Customs
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

package models

import java.util.UUID

import connectors.exchange._

object ProgressResponseExamples {
  val Initial = ProgressResponse(applicationId = UUID.randomUUID().toString(),
    personalDetails = false,
    frameworksLocation = false,
    assistanceDetails = false,
    review = false,
    questionnaire = Nil,
    submitted = false,
    withdrawn = false,
    onlineTestInvited = false,
    onlineTestStarted = false,
    onlineTestCompleted = false,
    onlineTestExpired = false,
    onlineTestAwaitingReevaluation = false,
    onlineTestFailed = false,
    onlineTestFailedNotified = false,
    onlineTestAwaitingAllocation = false,
    onlineTestAllocationConfirmed = false,
    onlineTestAllocationUnconfirmed = false,
    failedToAttend = false,
    assessmentScores = AssessmentScores(),
    assessmentCentre = AssessmentCentre()
  )
  val InProgress = Initial.copy(personalDetails = true)
  val InPersonalDetails = Initial.copy(personalDetails = true)
  val InSchemePreferencesDetails = InPersonalDetails.copy(frameworksLocation = true)
  val InAssistanceDetails = InSchemePreferencesDetails.copy(assistanceDetails = true)
  val InQuestionnaire = InAssistanceDetails.copy(questionnaire = List("start_questionnaire", "diversity_questionnaire",
    "education_questionnaire", "occupation_questionnaire"))
  val InPreview = InQuestionnaire.copy(review = true)
  val Submitted = InPreview.copy(submitted = true)
  val WithdrawnAfterSubmitted = Submitted.copy(withdrawn = true)
}
