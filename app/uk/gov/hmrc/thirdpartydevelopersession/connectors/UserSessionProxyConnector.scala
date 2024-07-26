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

package uk.gov.hmrc.thirdpartydevelopersession.connectors

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse, _}
import uk.gov.hmrc.play.http.metrics.common.API

import uk.gov.hmrc.apiplatform.modules.tpd.session.domain.models.{LoggedInState, UserSession, UserSessionId}
import uk.gov.hmrc.thirdpartydevelopersession.config.AppConfig

@Singleton
class UserSessionProxyConnector @Inject() (
    http: HttpClientV2,
    config: AppConfig,
    metrics: ConnectorMetrics
  )(implicit val ec: ExecutionContext
  ) extends CommonResponseHandlers with Logging {

  private val api: API                    = API("third-party-developer-session")
  private lazy val serviceBaseUrl: String = config.thirdPartyDeveloperUrl

  def fetchSession(userSessionId: UserSessionId)(implicit hc: HeaderCarrier): Future[Option[UserSession]] = metrics.record(api) {
    http.get(url"$serviceBaseUrl/session/$userSessionId")
      .execute[Option[UserSession]]
  }

  def updateLoggedInState(userSessionId: UserSessionId, state: LoggedInState)(implicit hc: HeaderCarrier): Future[Option[UserSession]] = metrics.record(api) {
    http.put(url"$serviceBaseUrl/session/$userSessionId/loggedInState/${state}")
      .execute[Option[UserSession]]
  }

  def deleteSession(userSessionId: UserSessionId)(implicit hc: HeaderCarrier): Future[Int] = metrics.record(api) {
    http.delete(url"$serviceBaseUrl/session/$userSessionId")
      .execute[ErrorOr[HttpResponse]]
      .map {
        case Right(response)                                 => response.status
        // treat session not found as successfully destroyed
        case Left(UpstreamErrorResponse(_, NOT_FOUND, _, _)) => NO_CONTENT
        case Left(err)                                       => throw err
      }
  }
}
