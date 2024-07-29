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
import uk.gov.hmrc.thirdpartydeveloperfrontend.mocks.connectors.UserSessionProxyConnectorMockModule

import uk.gov.hmrc.apiplatform.modules.tpd.session.domain.models.{LoggedInState, UserSession, UserSessionId}
import uk.gov.hmrc.apiplatform.modules.tpd.test.data.UserTestData
import uk.gov.hmrc.apiplatform.modules.tpd.test.utils.LocalUserIdTracker
import uk.gov.hmrc.thirdpartydevelopersession.utils.AsyncHmrcSpec

class UserSessionControllerSpec extends AsyncHmrcSpec {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  trait Setup extends UserSessionProxyConnectorMockModule with LocalUserIdTracker with UserTestData {
    val controller      = new UserSessionController(UserSessionProxyConnectorMock.aMock, stubControllerComponents())
    val OurRegularError = new RuntimeException("Bang")

    val validSessionId    = UserSessionId.random
    val validSession      = UserSession(validSessionId, LoggedInState.LOGGED_IN, JoeBloggs)
    val notfoundSessionId = UserSessionId.random
    val brokenSessionId   = UserSessionId.random
  }

  "Fetch session" should {

    "find an valid session" in new Setup {
      UserSessionProxyConnectorMock.FetchSession.willReturn(validSession)
      val result: Future[Result] = controller.fetch(validSessionId).apply(FakeRequest())
      status(result) shouldBe OK

      contentAsJson(result) shouldBe Json.toJson(validSession)
    }

    "find no valid session" in new Setup {
      UserSessionProxyConnectorMock.FetchSession.willReturnNoSession(notfoundSessionId)
      val result: Future[Result] = controller.fetch(notfoundSessionId).apply(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }

    "return 5xx when appropriate" in new Setup {
      UserSessionProxyConnectorMock.FetchSession.willFailWith(brokenSessionId, OurRegularError)
      val result: Future[Result] = controller.fetch(brokenSessionId).apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Update State of session" should {

    "find an valid session and update it" in new Setup {
      UserSessionProxyConnectorMock.UpdateSession.willSucceedWith(validSession)
      val result: Future[Result] = controller.updateLoggedInState(validSessionId, LoggedInState.LOGGED_IN).apply(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(validSession)
    }

    "return invalid session error code when no session found" in new Setup {
      UserSessionProxyConnectorMock.UpdateSession.willReturnNoSession()
      val result: Future[Result] = controller.updateLoggedInState(notfoundSessionId, LoggedInState.LOGGED_IN).apply(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }

    "return 5xx when appropriate" in new Setup {
      UserSessionProxyConnectorMock.UpdateSession.willFailWith(OurRegularError)
      val result: Future[Result] = controller.updateLoggedInState(brokenSessionId, LoggedInState.LOGGED_IN).apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Delete session" should {

    "find an valid session to delete" in new Setup {
      UserSessionProxyConnectorMock.DeleteSession.willSucceedFor(validSessionId)
      val result: Future[Result] = controller.delete(validSessionId).apply(FakeRequest())
      status(result) shouldBe NO_CONTENT
    }

    "find no valid session to delete" in new Setup {
      UserSessionProxyConnectorMock.DeleteSession.willSucceedFor(notfoundSessionId)
      val result: Future[Result] = controller.delete(notfoundSessionId).apply(FakeRequest())
      status(result) shouldBe NO_CONTENT
    }

    "return 5xx when appropriate" in new Setup {
      UserSessionProxyConnectorMock.DeleteSession.willFailWith(brokenSessionId, OurRegularError)
      val result: Future[Result] = controller.delete(brokenSessionId).apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
