package graphql

import sangria.schema._
import java.util.UUID

object Scalars {

  import sangria.validation.Violation

  case object InvalidUUIDFormatViolation extends Violation {
    override def errorMessage: String = "Invalid UUID format"
  }

  implicit val UUIDType: ScalarType[UUID] = ScalarType[UUID](
    name = "UUID",
    description = Some("A universally unique identifier (UUID)"),
    coerceOutput = (uuid, _) => uuid.toString,
    coerceUserInput = {
      case s: String =>
        try Right(UUID.fromString(s))
        catch { case _: IllegalArgumentException => Left(InvalidUUIDFormatViolation) }
      case _ => Left(InvalidUUIDFormatViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _, _, _) =>
        try Right(UUID.fromString(s))
        catch { case _: IllegalArgumentException => Left(InvalidUUIDFormatViolation) }
      case _ => Left(InvalidUUIDFormatViolation)
    }
  )
}
