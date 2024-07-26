/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.thirdpartydeveloperfrontend.connectors

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging

import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.apiplatform.modules.tpd.session.domain.models.UserSessionId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.apiplatform.modules.tpd.session.domain.models.UserSession

object SessionProxyConnector {
  case class CoreUserDetails(email: LaxEmailAddress, id: UserId)
}

@Singleton
class SessionProxyConnector @Inject() (
    http: HttpClient,
    config: ApplicationConfig,
    metrics: ConnectorMetrics
  )(implicit val ec: ExecutionContext
  ) extends CommonResponseHandlers with Logging {

  import SessionProxyConnector._


  lazy val serviceBaseUrl: String = config.thirdPartyDeveloperUrl

  def updateSessionLoggedInState(sessionId: UserSessionId, request: UpdateLoggedInStateRequest)(implicit hc: HeaderCarrier): Future[UserSession] = metrics.record(api) {
    http.PUT[String, Option[UserSession]](s"$serviceBaseUrl/session/$sessionId/loggedInState/${request.loggedInState}", "")
      .map {
        case Some(session) => session
        case None          => throw new SessionInvalid
      }
  }
HeaderCarrier
  def fetchSession(sessionId: UserSessionId)(implicit hc: HeaderCarrier): Future[UserSession] = metrics.record(api) {
    http.GET[Option[UserSession]](s"$serviceBaseUrl/session/$sessionId")
      .map {
        case Some(session) => session
        case None          => throw new SessionInvalid
      }
  }

  def deleteSession(sessionId: UserSessionId)(implicit hc: HeaderCarrier): Future[Int] = metrics.record(api) {
    http.DELETE[ErrorOr[HttpResponse]](s"$serviceBaseUrl/session/$sessionId")
      .map {
        case Right(response)                                 => response.status
        // treat session not found as successfully destroyed
        case Left(UpstreamErrorResponse(_, NOT_FOUND, _, _)) => NO_CONTENT
        case Left(err)                                       => throw err
      }
  }
}
