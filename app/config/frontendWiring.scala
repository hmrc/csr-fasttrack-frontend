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

package config

import java.util.Base64

import com.mohiva.play.silhouette.api.EventBus
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.{ SessionAuthenticatorService, SessionAuthenticatorSettings }
import com.mohiva.play.silhouette.impl.util.DefaultFingerprintGenerator
import connectors.{ ApplicationClient, UserManagementClient }
import models.services.UserCacheService
import play.api.Play
import play.api.Play.current
import play.api.libs.ws.WS
import play.api.mvc.Results.NotImplemented
import play.api.mvc.{ Call, RequestHeader, Result }
import security.CsrCredentialsProvider
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{ AppName, RunMode, ServicesConfig }
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

import scala.concurrent.Future
import scala.concurrent.duration._

object FrontendAuditConnector extends AuditConnector {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

object CSRHttp extends CSRHttp

class CSRHttp extends WSHttp {
  override val hooks = NoneRequired
  val wS = WS
}

trait CSRCache extends SessionCache with AppName with ServicesConfig

object CSRCache extends CSRCache {
  override lazy val http = CSRHttp
  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("cachable.session-cache")
  override lazy val domain = getConfString(
    "cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'")
  )
}

class SecurityEnvironmentImpl extends security.SecurityEnvironment {

  override lazy val eventBus: EventBus = EventBus()

  override val userService = new UserCacheService(ApplicationClient, UserManagementClient)
  override val identityService = userService

  override lazy val authenticatorService = new SessionAuthenticatorService(SessionAuthenticatorSettings(
    sessionKey = Play.configuration.getString("silhouette.authenticator.sessionKey").get,
    useFingerprinting = Play.configuration.getBoolean("silhouette.authenticator.useFingerprinting").get,
    authenticatorIdleTimeout = Play.configuration.getInt("silhouette.authenticator.authenticatorIdleTimeout").map(x => x seconds),
    authenticatorExpiry = Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").get seconds
  ), new DefaultFingerprintGenerator(false), , Clock())

  override lazy val credentialsProvider = new CsrCredentialsProvider {
    val http: CSRHttp = CSRHttp
  }

  override def providers = Map(credentialsProvider.id -> credentialsProvider)
  val http: CSRHttp = CSRHttp
}

object WhitelistFilter extends AkamaiWhitelistFilter with RunMode with MicroserviceFilterSupport{

  // Whitelist Configuration
  private def whitelistConfig(key: String): Seq[String] =
    Some(new String(Base64.getDecoder().decode(Play.configuration.getString(key).getOrElse("")), "UTF-8"))
      .map(_.split(",")).getOrElse(Array.empty).toSeq

  // List of IP addresses
  override def whitelist: Seq[String] = whitelistConfig("whitelist")

  // Es. /ping/ping,/admin/details
  override def excludedPaths: Seq[Call] = whitelistConfig("whitelistExcludedCalls").map {
    path => Call("GET", path)
  }

  override def destination: Call = Call("GET", "https://www.apply-civil-service-apprenticeship.service.gov.uk/outage-fset-fasttrack/index.html")
}
