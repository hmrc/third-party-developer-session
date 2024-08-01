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
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{SessionId => _, _}
import uk.gov.hmrc.play.http.metrics.common.API

import uk.gov.hmrc.thirdpartydevelopersession.config.AppConfig
import uk.gov.hmrc.thirdpartydevelopersession.connectors.{CommonResponseHandlers, ConnectorMetrics}
import uk.gov.hmrc.thirdpartydevelopersession.models.SecretRequest

@Singleton
class AuthenticationConnector @Inject() (
    http: HttpClientV2,
    config: AppConfig,
    metrics: ConnectorMetrics
  )(implicit val ec: ExecutionContext
  ) extends CommonResponseHandlers with Logging {

  lazy val serviceBaseUrl: String = config.thirdPartyDeveloperUrl
  val api: API                    = API("third-party-developer")

  def authenticate(secretRequest: SecretRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = metrics.record(api) {
    http.post(url"$serviceBaseUrl/authenticate")
      .withBody(Json.toJson(secretRequest))
      .execute[HttpResponse]
  }

  def authenticateAccessCode(secretRequest: SecretRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = metrics.record(api) {
    http.post(url"$serviceBaseUrl/authenticate-mfa")
      .withBody(Json.toJson(secretRequest))
      .execute[HttpResponse]
  }
}
