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

package controllers

import _root_.forms.SignUpForm
import com.mohiva.play.silhouette.api.{ SignUpEvent, Silhouette }
import config.{ CSRCache, CSRHttp }
import connectors.{ ApplicationClient, UserManagementClient }
import connectors.ExchangeObjects.Implicits._
import connectors.UserManagementClient.EmailTakenException
import helpers.NotificationType._
import models.SecurityUser
import play.api.Play
import security.{ SecurityEnvironment, SignInUtils, SilhouetteComponent }
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object SignUpController extends SignUpController {
  val http = CSRHttp
  val cacheClient = CSRCache
  val userManagementClient = UserManagementClient
  lazy val silhouette = SilhouetteComponent.silhouette
}

trait SignUpController extends BaseController with SignInUtils with ApplicationClient {

  val userManagementClient: UserManagementClient

  def present = CSRUserAwareAction { implicit request =>
    implicit user =>

      Future.successful(request.identity match {
        case Some(_) => Redirect(routes.HomeController.present()).flashing(warning("activation.already"))
        case None => Ok(views.html.registration.signup(SignUpForm.form))
      })
  }

  val signUp = CSRUserAwareAction { implicit request =>
    implicit user =>

      SignUpForm.form.bindFromRequest.fold(
        invalidForm => Future.successful(Ok(views.html.registration.signup(invalidForm))),
        data => {
          userManagementClient.register(data.email.toLowerCase, data.password, data.firstName, data.lastName).flatMap { u =>
            addMedia(u.userId, extractMediaReferrer(data)).flatMap { _ =>
              signInUser(
                u.toCached,
                redirect = Redirect(routes.ActivationController.present()).flashing(success("account.successful"))
              ).map { r =>
                env.eventBus.publish(SignUpEvent(SecurityUser(u.userId.toString), request))
                r
              }
            }
          }.recover {
            case e: EmailTakenException =>
              Ok(views.html.registration.signup(SignUpForm.form.fill(data), Some(danger("user.exists"))))
          }
        }
      )
  }

  private def extractMediaReferrer(data: SignUpForm.Data): String = {
    if (data.campaignReferrer.contains("Other") || data.campaignReferrer.contains("Careers fair")) {
      data.campaignOther.getOrElse("")
    } else {
      data.campaignReferrer.getOrElse("")
    }
  }
}
