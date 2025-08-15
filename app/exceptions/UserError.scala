package exceptions

import sangria.execution.UserFacingError

case class UserError(message: String) extends Exception(message) with UserFacingError
