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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Mode}
import uk.gov.hmrc.http.{HeaderCarrier}

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.thirdpartydevelopersession.connectors.ConnectorMetrics
import uk.gov.hmrc.thirdpartydevelopersession.connectors.AuthenticationConnector
import uk.gov.hmrc.thirdpartydevelopersession.connectors.NoopConnectorMetrics
import uk.gov.hmrc.thirdpartydevelopersession.models.SecretRequest

class AuthenticationConnectorIntegrationSpec extends BaseConnectorIntegrationSpec
    with GuiceOneAppPerSuite with WireMockExtensions with FixedClock {

  private val stubConfig = Configuration(
    "microservice.services.third-party-developer.port" -> stubPort
  )

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(stubConfig)
      .overrides(bind[ConnectorMetrics].to[NoopConnectorMetrics])
      .in(Mode.Test)
      .build()

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest: AuthenticationConnector     = app.injector.instanceOf[AuthenticationConnector]

    val dummyRequest = SecretRequest("bob")
  }

  "authenticate" should {
    "return the result" in new Setup {
      stubFor(
        post(urlPathEqualTo(s"/authenticate"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(s"""{
                           |  "field": "value"
                           |}""".stripMargin)
          )
      )

      private val result = await(underTest.authenticate(dummyRequest))

      result.status shouldBe OK
    }
  }

  "authenticate-mfa" should {
    "return the result" in new Setup {
      stubFor(
        post(urlPathEqualTo(s"/authenticate-mfa"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(s"""{
                           |  "field": "value"
                           |}""".stripMargin)
          )
      )

      private val result = await(underTest.authenticateAccessCode(dummyRequest))

      result.status shouldBe OK
    }
  }
}
