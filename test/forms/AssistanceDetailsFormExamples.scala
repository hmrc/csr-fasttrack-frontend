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

package forms

object AssistanceDetailsFormExamples {
  val DisabilityGisAndAdjustmentsForm = AssistanceDetailsForm.Data("Yes", Some("Epilepsy"), Some("Yes"), Some("Yes"),
    Some("Some adjustment"), "Yes", Some("Some other adjustments"))

  val NoDisabilitiesForm = AssistanceDetailsForm.Data("No", None, None, Some("No"), None, "No", None)

  val NoAdjustmentsForm = AssistanceDetailsForm.Data("Yes", Some("Some disabilities"), Some("No"), Some("No"), None,
    "No", None)

  val FullForm = AssistanceDetailsForm.Data("Yes", Some("Some disabilities"), Some("Yes"), Some("Yes"),
    Some("Some adjustments online"), "Yes", Some("Some adjustments at venue"))

  val DisabilityGisAndAdjustmentsMap = Map[String, String](
    "hasDisability" -> "Yes",
    "hasDisabilityDescription" -> "Epilepsy",
    "guaranteedInterview" -> "Yes",
    "needsSupportForOnlineAssessment" -> "Yes",
    "needsSupportForOnlineAssessmentDescription" -> "Some adjustment",
    "needsSupportAtVenue" -> "Yes",
    "needsSupportAtVenueDescription" -> "Some other adjustments")

  val DisabilityGisAndAdjustmentsFormUrlEncodedBody = Seq(
    "hasDisability" -> "Yes",
    "hasDisabilityDescription" -> "Epilepsy",
    "guaranteedInterview" -> "Yes",
    "needsSupportForOnlineAssessment" -> "Yes",
    "needsSupportForOnlineAssessmentDescription" -> "Some adjustment",
    "needsSupportAtVenue" -> "Yes",
    "needsSupportAtVenueDescription" -> "Some other adjustments")
}
