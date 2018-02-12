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

package forms

import forms.Mappings._
import mappings.PhoneNumberMapping.PhoneNumber
import mappings.PostCodeMapping._
import mappings.{ Address, DayMonthYear, PhoneNumberMapping }
import org.joda.time.LocalDate
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{ Form, FormError }

object GeneralDetailsForm {
  val minAge = 16
  val minDob = new LocalDate(1900, 1, 1)
  def ageReference(implicit now: LocalDate) = new LocalDate(now.getYear, 8, 31)

  val phoneNumberFormatter = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val phone: Option[String] = data.get("phone")

      phone match {
        case None | Some("") => Left(List(FormError("phone", "error.phone.required")))
        case Some(m) if !m.isEmpty && !PhoneNumberMapping.validatePhoneNumber(m) => Left(List(FormError("phone", "error.phoneNumber.format")))
        case _ => Right(phone.map(_.trim))
      }
    }

    override def unbind(key: String, value: Option[String]) = Map(key -> value.map(_.trim).getOrElse(""))
  }

  private[forms] val deptMaxLength = 256

  val civilServantDependentFormatter = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val check = data.get("civilServant")
      val value = data.get(key).filterNot(_.trim.isEmpty)

      (check, value) match {
        case (Some("Yes"), Some(v)) if v.trim.length > deptMaxLength => Left(List(FormError(key, s"error.$key.maxLength")))
        case (Some("Yes"), Some(v)) => Right(value)
        case (Some("Yes"), None) => Left(List(FormError(key, s"error.$key.required")))
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  val otherDepartmentDependentFormatter = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val check1 = data.get("civilServant")
      val check2 = data.get("department")
      val value = data.get(key).filterNot(_.trim.isEmpty)

      (check1, check2, value) match {
        case (Some("Yes"), Some("Other"), Some(v)) if v.trim.length > deptMaxLength => Left(List(FormError(key, s"error.$key.maxLength")))
        case (Some("Yes"), Some("Other"), Some(v)) => Right(value)
        case (Some("Yes"), Some("Other"), None) => Left(List(FormError(key, s"error.$key.required")))
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def form(implicit now: LocalDate) = {
    val maxDob = Some(ageReference.minusYears(minAge))

    Form(
      mapping(
        "firstName" -> nonEmptyTrimmedText("error.firstName", 256),
        "lastName" -> nonEmptyTrimmedText("error.lastName", 256),
        "preferredName" -> nonEmptyTrimmedText("error.preferredName", 256),
        "dateOfBirth" -> DayMonthYear.validDayMonthYear("error.dateOfBirth", "error.dateOfBirthInFuture")(Some(minDob), maxDob),
        "outsideUk" -> optional(checked("error.address.required")),
        "address" -> Address.address,
        "postCode" -> of(postCodeFormatter),
        "country" -> of(countryFormatter),
        "phone" -> of(phoneNumberFormatter),
        "alevel-d" -> optional(boolean).verifying("aleveld.required", _.isDefined),
        "alevel" -> optional(boolean).verifying("alevel.required", _.isDefined),
        "civilServant" -> nonEmptyTrimmedText("error.civilServant.required", 3),
        "department" -> of(civilServantDependentFormatter),
        "departmentOther" -> of(otherDepartmentDependentFormatter)
      )(Data.apply)(Data.unapply)
    )
  }

  val postCodeFormatter = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val outsideUK = data.getOrElse("outsideUk", "false").toBoolean
      val postCode = data.getOrElse(key, "").trim
      outsideUK match {
        case true => Right(None)
        case _ if postCode.isEmpty => Left(List(FormError(key, "error.postcode.required")))
        case _ if !postcodePattern.pattern.matcher(postCode).matches() => Left(List(FormError(key, "error.postcode.invalid")))
        case _ => Right(Some(postCode))
      }
    }

    override def unbind(key: String, value: Option[String]) = Map(key -> value.getOrElse(""))
  }

  val countryFormatter = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val outsideUK = data.getOrElse("outsideUk", "false").toBoolean
      val country = data.getOrElse(key, "").trim
      outsideUK match {
        case true if country.isEmpty => Left(List(FormError(key, "error.country.required")))
        case true if country.length > 100 => Left(List(FormError(key, "error.country.invalid")))
        case true => Right(Some(country))
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]) = Map(key -> value.getOrElse(""))
  }

  case class Data(

    //personal details
    firstName: String,
    lastName: String,
    preferredName: String,
    dateOfBirth: DayMonthYear,

    //contact details
    outsideUk: Option[Boolean],
    address: Address,
    postCode: Option[PostCode],
    country: Option[String],
    phone: Option[PhoneNumber],

    aLevel: Option[Boolean],
    stemLevel: Option[Boolean],
    civilServant: String,
    department: Option[String],
    departmentOther: Option[String]
  )
}
