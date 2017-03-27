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

package security

import javax.inject.Inject

import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, SecuredRequest }
import controllers.routes
import helpers.NotificationType.danger
import models.{ CachedData, SecurityUser }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ RequestHeader, Result }
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CustomSecuredErrorHandler @Inject() (val messagesApi: MessagesApi) extends SecuredErrorHandler with I18nSupport {

  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    val sec = request.asInstanceOf[SecuredRequest[SecurityEnvironment, SecurityUser]]
    val headerCarrier = HeaderCarrier.fromHeadersAndSession(sec.headers, Some(sec.session))
    sec.identity.toUserFuture(headerCarrier, request).map {
      case Some(user: CachedData) if user.user.isActive => Redirect(routes.HomeController.present).flashing(danger("access.denied"))
      case _ => Redirect(routes.ActivationController.present).flashing(danger("access.denied"))
    }
  }

  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] =
    Future.successful(Redirect(routes.SignInController.present))
}
