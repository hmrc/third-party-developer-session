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

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.thirdpartydeveloper.models.ErrorCode

import uk.gov.hmrc.apiplatform.modules.tpd.session.domain.models.{LoggedInState, UserSessionId}
import uk.gov.hmrc.thirdpartydevelopersession.connectors.UserSessionProxyConnector
import uk.gov.hmrc.thirdpartydevelopersession.utils.ApplicationLogger

@Singleton
class UserSessionController @Inject() (sessionProxyConnector: UserSessionProxyConnector, cc: ControllerComponents)(implicit val ec: ExecutionContext)
    extends BackendController(cc) with ApplicationLogger {

  def fetch(userSessionId: UserSessionId) = Action.async { implicit request =>
    sessionProxyConnector.fetchSession(userSessionId)
      .map(_.fold(sessionNotFound(userSessionId))(userSession => Ok(Json.toJson(userSession))))
      .recover(recovery)
  }

  def delete(userSessionId: UserSessionId) = Action.async { implicit request =>
    sessionProxyConnector.deleteSession(userSessionId)
      .map(_ => NoContent)
      .recover(recovery)
  }

  def updateLoggedInState(userSessionId: UserSessionId, state: LoggedInState) = Action.async { implicit request =>
    sessionProxyConnector.updateLoggedInState(userSessionId, state)
      .map(_.fold(sessionNotFound(userSessionId))(userSession => Ok(Json.toJson(userSession))))
      .recover(recovery)
  }

  private def sessionNotFound(id: UserSessionId) = NotFound(error(ErrorCode.INVALID_SESSION, s"Invalid session: $id"))

  private def recovery: PartialFunction[Throwable, Result] = {
    case e: Throwable =>
      logger.error(e.getMessage, e)
      InternalServerError(error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage))
  }

  private def error(errorCode: ErrorCode, message: JsValueWrapper): JsObject = {
    Json.obj(
      "code"    -> errorCode.toString,
      "message" -> message
    )
  }

}
