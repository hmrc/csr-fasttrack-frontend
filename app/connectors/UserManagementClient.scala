/*
 * Copyright 2019 HM Revenue & Customs
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

import config.CSRHttp
import connectors.ExchangeObjects._
import connectors.UserManagementClient._
import connectors.exchange.FindByUserIdRequest
import models.UniqueIdentifier
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, LockedException, NotFoundException, Upstream4xxResponse }

trait UserManagementClient {

  private val role = "candidate" // We have only one role for this application

  val http: CSRHttp

  private val ServiceName = "fasttrack18"

  import Implicits._
  import config.FrontendAppConfig.userManagementConfig._

  def register(email: String, password: String, firstName: String, lastName: String)(implicit hc: HeaderCarrier): Future[UserResponse] =
    http.POST(s"${url.host}/add",
      AddUserRequest(email.toLowerCase, password, firstName, lastName, role, ServiceName)).map { (resp: HttpResponse) =>
      resp.json.as[UserResponse]
    }.recover {
      case Upstream4xxResponse(_, 409, _, _) => throw new EmailTakenException()
    }

  def signIn(email: String, password: String)(implicit hc: HeaderCarrier): Future[UserResponse] =
    http.POST(s"${url.host}/authenticate", SignInRequest(email.toLowerCase, password, ServiceName)).map { (resp: HttpResponse) =>
      val response = resp.json.as[UserResponse]
      if (response.role != role) throw new InvalidRoleException() else {
        response
      }
    }.recover {
      case Upstream4xxResponse(_, 401, _, _) => throw new InvalidCredentialsException()
      case Upstream4xxResponse(_, 429, _, _) => throw new AccountLockedOutException()
    }

  def activate(email: String, token: String)(implicit hc: HeaderCarrier): Future[Unit] =
    http.POST(s"${url.host}/activate", ActivateEmailRequest(email.toLowerCase, token, ServiceName)).map(_ => (): Unit)
      .recover {
        case Upstream4xxResponse(_, 410, _, _) => throw new TokenExpiredException()
        case e: NotFoundException => throw new TokenEmailPairInvalidException()
      }

  def resendActivationCode(email: String)(implicit hc: HeaderCarrier): Future[Unit] =
    http.POST(s"${url.host}/resend-activation-code", ResendActivationTokenRequest(email.toLowerCase, ServiceName)).map(_ => (): Unit)
      .recover {
        case e: NotFoundException => throw new InvalidEmailException()
      }

  def sendResetPwdCode(email: String)(implicit hc: HeaderCarrier): Future[Unit] =
    http.POST(s"${url.host}/send-reset-password-code", SendPasswordCodeRequest(email.toLowerCase, ServiceName)).map(_ => (): Unit)
      .recover {
        case e: NotFoundException => throw new InvalidEmailException()
      }

  def resetPasswd(email: String, token: String, newPassword: String)(implicit hc: HeaderCarrier): Future[Unit] =
    http.POST(s"${url.host}/reset-password", ResetPasswordRequest(email.toLowerCase, token, newPassword, ServiceName)).map(_ => (): Unit)
      .recover {
        case Upstream4xxResponse(_, 410, _, _) => throw new TokenExpiredException()
        case e: NotFoundException => throw new TokenEmailPairInvalidException()
      }

  def updateDetails(userId: UniqueIdentifier, firstName: String, lastName: String,
    preferredName: Option[String])(implicit hc: HeaderCarrier): Future[Unit] =
    http.PUT(s"${url.host}/service/$ServiceName/details/$userId", UpdateDetails(firstName, lastName, preferredName, ServiceName)).map(_ => ())

  def failedLogin(email: String)(implicit hc: HeaderCarrier): Future[UserResponse] =
    http.PUT(s"${url.host}/failedAttempt", EmailWrapper(email.toLowerCase, ServiceName)).map { (resp: HttpResponse) =>
      resp.json.as[UserResponse]
    }.recover {
      case e: NotFoundException => throw new InvalidCredentialsException()
      case e: LockedException => throw new AccountLockedOutException()
      //TODO Figure out why LockedException is not caught, and fix this
      case Upstream4xxResponse(_, 423, _, _) => throw new AccountLockedOutException()
    }

  def find(email: String)(implicit hc: HeaderCarrier): Future[UserResponse] =
    http.POST(s"${url.host}/find", EmailWrapper(email.toLowerCase, ServiceName)).map { (resp: HttpResponse) =>
      resp.json.as[UserResponse]
    }.recover {
      case e: NotFoundException => throw new InvalidCredentialsException()
    }


  def findByUserId(userId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[UserResponse] =
    http.POST(s"${url.host}/service/$ServiceName/findUserById", FindByUserIdRequest(userId)).map { (resp: HttpResponse) =>
      resp.json.as[UserResponse]
    }.recover {
      case e: NotFoundException => throw new InvalidCredentialsException(s"UserId = $userId")
    }
}

object UserManagementClient extends UserManagementClient {
  val http: CSRHttp = CSRHttp

  sealed class InvalidRoleException extends Exception
  sealed class InvalidEmailException extends Exception
  sealed class EmailTakenException extends Exception
  sealed class InvalidCredentialsException(message: String = "") extends Exception(message)
  sealed class AccountLockedOutException extends Exception
  sealed class TokenEmailPairInvalidException extends Exception
  sealed class TokenExpiredException extends Exception
}
