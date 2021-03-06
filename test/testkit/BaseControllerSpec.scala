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

package testkit

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.actions.{ SecuredRequest, UserAwareRequest }
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import config.SecurityEnvironmentImpl
import models.SecurityUserExamples._
import models._
import org.joda.time.DateTime
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{ CSRF, CSRFConfig, CSRFConfigProvider, CSRFFilter }
import security.Roles.CsrAuthorization
import security.{ SecureActions, SecurityEnvironment }

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

/**
  * Each Controller test needs to extend this class to simplify controller testing
  */
abstract class BaseControllerSpec extends UnitWithAppSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val rh: RequestHeader = FakeRequest()
  val mockSecurityEnvironment = mock[SecurityEnvironmentImpl]

  def currentCandidate: CachedData = ActiveCandidate

  def currentUserId = currentCandidate.user.userID

  def currentEmail = currentCandidate.user.email

  def currentUser = currentCandidate.user

  def currentCandidateWithApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    CreatedApplication.copy(userId = ActiveCandidate.user.userID))

  def currentApplicationId: UniqueIdentifier = {
    currentCandidate.user.userID
    currentCandidateWithApp.application.applicationId
  }

  def randomUUID = UniqueIdentifier(UUID.randomUUID().toString)

  private val csrfConfig     = app.injector.instanceOf[CSRFConfigProvider].get
  private val csrfFilter     = app.injector.instanceOf[CSRFFilter]
  private val token          = csrfFilter.tokenProvider.generateToken

  def fakeRequest = {
    val fakeRequest = FakeRequest()
      fakeRequest.copyFakeRequest(tags = fakeRequest.tags ++ Map(
      Token.NameRequestTag  -> csrfConfig.tokenName,
      Token.RequestTag      -> token
    )).withHeaders((csrfConfig.headerName, token))
  }

  def assertPageTitle(result: Future[Result], expectedTitle: String) = {
    status(result) must be(OK)
    val content = contentAsString(result)
    content must include(s"<title>$expectedTitle")
  }

  def assertPageRedirection(result: Future[Result], expectedUrl: String) = {
    status(result) must be(SEE_OTHER)
    redirectLocation(result) must be(Some(expectedUrl))
  }

  // scalastyle:off method.name
  trait TestableSecureActions extends SecureActions {

    val Candidate: CachedData = currentCandidate
    val CandidateWithApp: CachedDataWithApp = currentCandidateWithApp

    override def CSRSecureAction(role: CsrAuthorization)(block: SecuredRequest[_, _] => CachedData => Future[Result]): Action[AnyContent] =
      execute(Candidate)(block)

    override def CSRSecureAppAction(role: CsrAuthorization)(block: (SecuredRequest[_, _]) => (CachedDataWithApp) =>
      Future[Result]): Action[AnyContent] = execute(CandidateWithApp)(block)

    override def CSRUserAwareAction(block: UserAwareRequest[_, _] => Option[CachedData] => Future[Result]): Action[AnyContent] =
      Action.async { request =>
        val secReq = UserAwareRequest(None, None, request)
        implicit val carrier = hc(request)
        block(secReq)(None)
      }

    private def execute[T](result: T)(block: (SecuredRequest[_, _]) => (T) => Future[Result]): Action[AnyContent] = {
      Action.async { request =>
        val secReq = defaultAction(request)
        implicit val carrier = hc(request)
        block(secReq)(result)
      }
    }

    private def defaultAction[T](request: Request[AnyContent]) =
      SecuredRequest[SecurityEnvironment, AnyContent](
        SecurityUser(UUID.randomUUID.toString),
        SessionAuthenticator(
          LoginInfo("fakeProvider", "fakeKey"),
          DateTime.now(),
          DateTime.now().plusDays(1),
          None, None
        ), request
      )
  }
}
