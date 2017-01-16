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

import controllers.BaseSpec
import forms.QuestionnaireEducationInfoForm.{Data, form}

class QuestionnaireEducationFormSpec extends BaseSpec {

  "the education form" should {
    "be valid when all values are correct" in new Fixture {
      val validForm = form.bind(validFormValues)
      val expectedData = validFormData
      val actualData = validForm.get
      actualData mustBe expectedData
    }

    "fail when no schoolName" in new Fixture {
      assertFieldRequired("schoolName", "schoolName")
    }

    "fail when no sixthForm" in new Fixture {
      assertFieldRequired("sixthForm", "sixthForm")
    }

    "fail when no postcode" in new Fixture {
      assertFieldRequired("postcodeQ", "postcodeQ", "preferNotSay_postcodeQ")
    }

    "fail when no freeSchoolMeals" in new Fixture {
      assertFieldRequired("freeSchoolMeals", "freeSchoolMeals")
    }

    "transform properly to a question list" in new Fixture {
      val questionList = validFormData.toQuestionnaire.questions
      questionList.size mustBe 4
      questionList(0).answer.answer mustBe Some("Some School")
      questionList(1).answer.answer mustBe Some("sixth form")
      questionList(2).answer.unknown mustBe Some(true)
      questionList(3).answer.answer mustBe Some("yes")
    }
  }

  trait Fixture {

    val validFormData = Data(Some("Some School"), None, Some("sixth form"), None, None, Some(true), "yes")

    val validFormValues = Map(
      "schoolName" -> "Some School",
      "preferNotSay_schoolName" -> "",
      "sixthForm" -> "sixth form",
      "preferNotSay_sixthForm" -> "",
      "postcodeQ" -> "",
      "preferNotSay_postcodeQ" -> "true",
      "freeSchoolMeals" -> "yes"
    )

    def assertFieldRequired(expectedError: String, fieldKey: String*) =
      assertFormError(expectedError, validFormValues ++ fieldKey.map(k => k -> ""))

    def assertFormError(expectedKey: String, invalidFormValues: Map[String, String]) = {
      val invalidForm = form.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.key) mustBe Seq(expectedKey)
    }
  }
}
