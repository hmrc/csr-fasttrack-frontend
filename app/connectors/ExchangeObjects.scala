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

package connectors

import connectors.exchange.ProgressResponse
import mappings.Address
import mappings.PhoneNumberMapping._
import mappings.PostCodeMapping._
import models.ApplicationData.ApplicationStatus.ApplicationStatus
import models.UniqueIdentifier
import org.joda.time.format.{ DateTimeFormatterBuilder, PeriodFormatterBuilder }
import org.joda.time.{ DateTime, LocalDate, Period, PeriodType }
import play.api.libs.json.{ Format, Json }

/**
 * this is duplicated from the auth project
 * todo fix
 */
object ExchangeObjects {

  val frameworkId = "FastTrack-2015"

  type LoginInfo = String

  case class EmailWrapper(email: String, service: String)

  case class CreateApplicationRequest(userId: UniqueIdentifier, frameworkId: String)

  case class WithdrawApplicationRequest(reason: String, otherReason: Option[String], withdrawer: String)

  case class PersonalDetails(
    firstName: String,
    lastName: String,
    preferredName: String,
    email: String,
    dateOfBirth: LocalDate,
    outsideUk: Boolean,
    address: Address,
    postCode: Option[PostCode],
    country: Option[String],
    phone: Option[PhoneNumber],
    aLevel: Boolean,
    stemLevel: Boolean,
    civilServant: Boolean,
    department: Option[String]
  )

  case class AddMedia(userId: UniqueIdentifier, media: String)

  case class ApplicationResponse(applicationId: UniqueIdentifier, applicationStatus: String,
    userId: UniqueIdentifier, progressResponse: ProgressResponse)

  case class PersonalDetailsAdded(applicationId: UniqueIdentifier, userId: String)

  case class RegistrationEmail(to: List[String], templateId: String, parameters: Map[String, String])

  case class AddUserRequest(email: String, password: String, firstName: String, lastName: String, role: String, service: String)

  case class UpdateDetails(firstName: String, lastName: String, preferredName: Option[String], service: String)

  case class UpdateUserRequest(email: String, password: String, firstName: String, lastName: String, userId: UniqueIdentifier, isActive: Boolean)

  case class SignInRequest(email: String, password: String, service: String)

  case class FindUserRequest(email: String)

  case class ActivateEmailRequest(email: String, token: String, service: String)

  case class ResendActivationTokenRequest(email: String, service: String)

  case class SendPasswordCodeRequest(email: String, service: String)
  case class ResetPasswordRequest(email: String, token: String, newPassword: String, service: String)

  case class ReviewRequest(flag: Boolean)

  case class OnlineTest(
    cubiksUserId: Int,
    inviteDate: DateTime,
    expireDate: DateTime,
    onlineTestLink: String,
    token: String,
    isOnlineTestEnabled: Boolean = false,
    pdfReportAvailable: Boolean = false,
    startedDateTime: Option[DateTime] = None,
    completedDateTime: Option[DateTime] = None
  ) {

    def isStarted : Boolean = startedDateTime.isDefined
    def isCompleted: Boolean = completedDateTime.isDefined

    def getDuration: String = {

      val now = DateTime.now
      val date = expireDate

      val period = new Period(now, date).normalizedStandard(PeriodType.yearMonthDayTime())

      val periodFormat = new PeriodFormatterBuilder()
        .appendYears()
        .appendSuffix(" year", " years")
        .appendSeparator(", ")
        .appendMonths()
        .appendSuffix(" month", " months")
        .appendSeparator(", ")
        .printZeroAlways()
        .appendDays()
        .appendSuffix(" day", " days")
        .appendSeparator(" and ")
        .appendHours()
        .appendSuffix(" hour", " hours")
        .toFormatter

      periodFormat print period
    }

    def getExpireDateTime: String = {

      val dateTimeFormat = new DateTimeFormatterBuilder().
        appendClockhourOfHalfday(1).
        appendLiteral(":").
        appendMinuteOfHour(2).
        appendHalfdayOfDayText().
        appendLiteral(" on ").
        appendDayOfMonth(1).
        appendLiteral(" ").
        appendMonthOfYearText().
        appendLiteral(" ").
        appendYear(4, 4).
        toFormatter

      dateTimeFormat.print(expireDate)
    }

    def getExpireDate: String = {

      val dateTimeFormat = new DateTimeFormatterBuilder().
        appendDayOfMonth(1).
        appendLiteral(" ").
        appendMonthOfYearText().
        appendLiteral(" ").
        appendYear(4, 4).
        toFormatter

      dateTimeFormat.print(expireDate)
    }
  }

  case class OnlineTestStatus(status: ApplicationStatus)

  object Implicits {

    implicit val emailWrapperFormats = Json.format[EmailWrapper]
    implicit val addressFormats = Json.format[Address]
    implicit val mediaFormats = Json.format[AddMedia]

    implicit val userFormats = Json.format[UserResponse]

    implicit val resendActivationTokenRequestFormats = Json.format[ResendActivationTokenRequest]

    implicit val activateEmailRequestFormats = Json.format[ActivateEmailRequest]
    implicit val signInRequestFormats = Json.format[SignInRequest]
    implicit val findUserRequestFormats = Json.format[FindUserRequest]

    implicit val registrationEmail = Json.format[RegistrationEmail]
    implicit val addUserRequestFormats = Json.format[AddUserRequest]
    implicit val updateDetailsFormats = Json.format[UpdateDetails]

    /** Successes serialization */
    implicit val applicationAddedFormat = Json.format[ApplicationResponse]
    implicit val personalDetailsAddedFormat = Json.format[PersonalDetailsAdded]

    /** Requests serialization */
    implicit val createApplicationRequestFormats: Format[CreateApplicationRequest] = Json.format[CreateApplicationRequest]
    implicit val withdrawApplicationRequestFormats: Format[WithdrawApplicationRequest] = Json.format[WithdrawApplicationRequest]
    implicit val personalDetailsRequestFormats: Format[PersonalDetails] = Json.format[PersonalDetails]

    implicit val sendPasswordCodeRequestFormats = Json.format[SendPasswordCodeRequest]
    implicit val resetPasswordRequestFormats = Json.format[ResetPasswordRequest]

    implicit val reviewFormats = Json.format[ReviewRequest]

    implicit val onlineTestFormats = Json.format[OnlineTest]

    implicit val onlineTestStatusFormats = Json.format[OnlineTestStatus]
  }
}
