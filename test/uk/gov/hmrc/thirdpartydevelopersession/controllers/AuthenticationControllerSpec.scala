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

package uk.gov.hmrc.thirdpartydevelopersession.controllers

import scala.concurrent.Future

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import uk.gov.hmrc.apiplatform.modules.tpd.test.data.UserTestData
import uk.gov.hmrc.apiplatform.modules.tpd.test.utils.LocalUserIdTracker
import uk.gov.hmrc.thirdpartydevelopersession.mocks.connectors.AuthenticationConnectorMockModule
import uk.gov.hmrc.thirdpartydevelopersession.models.SecretRequest
import uk.gov.hmrc.thirdpartydevelopersession.utils.AsyncHmrcSpec

class AuthenticationControllerSpec extends AsyncHmrcSpec {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  trait Setup extends AuthenticationConnectorMockModule with LocalUserIdTracker with UserTestData {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val controller      = new AuthenticationController(AuthenticationConnectorMock.aMock, stubControllerComponents())
    val OurRegularError = new RuntimeException("Bang")
  }

  "Authenticate" should {

    "should proxy through the response" in new Setup {
      val fakeResponsePayload = Json.obj("field" -> "value")
      val httpResponse        = mock[HttpResponse]
      when(httpResponse.status).thenReturn(OK)
      when(httpResponse.json).thenReturn(fakeResponsePayload)

      AuthenticationConnectorMock.Authenticate.willReturn(httpResponse)
      val result: Future[Result] = controller.authenticate().apply(FakeRequest().withBody(Json.toJson(SecretRequest("bob"))))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe fakeResponsePayload
    }
  }

  "Authenticate MFA" should {

    "should proxy through the response" in new Setup {
      val fakeResponsePayload = Json.obj("field" -> "value")
      val httpResponse        = mock[HttpResponse]
      when(httpResponse.status).thenReturn(OK)
      when(httpResponse.json).thenReturn(fakeResponsePayload)

      AuthenticationConnectorMock.AuthenticateAccessCode.willReturn(httpResponse)
      val result: Future[Result] = controller.authenticateAccessCode().apply(FakeRequest().withBody(Json.toJson(SecretRequest("bob"))))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe fakeResponsePayload
    }
  }
}
