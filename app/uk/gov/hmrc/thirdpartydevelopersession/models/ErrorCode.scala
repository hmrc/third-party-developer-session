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

package uk.gov.hmrc.thirdpartydeveloper.models

import play.api.libs.json.Format

import uk.gov.hmrc.apiplatform.modules.common.domain.services.SealedTraitJsonFormatting
sealed trait ErrorCode

object ErrorCode {

  // case object INVALID_REQUEST_PAYLOAD  extends ErrorCode
  case object INTERNAL_SERVER_ERROR extends ErrorCode
  // case object EMAIL_ALREADY_REGISTERED extends ErrorCode
  // case object INVALID_PASSWORD_CHANGE  extends ErrorCode
  // case object UNVERIFIED               extends ErrorCode
  // case object PASSWORD_EXPIRED         extends ErrorCode
  // case object INVALID_EMAIL            extends ErrorCode
  // case object INVALID_DEVICE_SESSION   extends ErrorCode
  case object INVALID_SESSION       extends ErrorCode
  // case object LOCKED                   extends ErrorCode
  // case object USERID_NOT_FOUND         extends ErrorCode

  val values = Set(
    // INVALID_REQUEST_PAYLOAD,
    INTERNAL_SERVER_ERROR,
    // EMAIL_ALREADY_REGISTERED,
    // INVALID_PASSWORD_CHANGE,
    // UNVERIFIED,
    // PASSWORD_EXPIRED,
    // INVALID_EMAIL,
    // INVALID_DEVICE_SESSION,
    INVALID_SESSION
    // LOCKED,
    // USERID_NOT_FOUND
  )

  def apply(text: String): Option[ErrorCode] = ErrorCode.values.find(_.toString() == text.toUpperCase)

  def unsafeApply(text: String): ErrorCode = apply(text).getOrElse(throw new RuntimeException(s"$text is not a valid Error Code"))

  implicit val format: Format[ErrorCode] = SealedTraitJsonFormatting.createFormatFor[ErrorCode]("Error Code", apply)
}
