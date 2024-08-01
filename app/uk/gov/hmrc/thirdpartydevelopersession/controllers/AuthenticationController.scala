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

import play.api.libs.json._
import play.api.mvc.{ControllerComponents, _}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.thirdpartydevelopersession.connectors.AuthenticationConnector
import uk.gov.hmrc.thirdpartydevelopersession.models.SecretRequest

@Singleton
class AuthenticationController @Inject() (
    authenticationConnector: AuthenticationConnector,
    cc: ControllerComponents
  )(implicit val ec: ExecutionContext
  ) extends BackendController(cc) {

  def authenticate: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[SecretRequest] { sr =>
      authenticationConnector.authenticate(sr)
        .map(r => Status(r.status)(r.json))
    }
  }

  def authenticateAccessCode: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[SecretRequest] { sr =>
      authenticationConnector.authenticateAccessCode(sr)
        .map(r => Status(r.status)(r.json))
    }
  }
}
