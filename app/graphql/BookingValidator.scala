package graphql

import exceptions.UserError

import java.time.LocalDate

object BookingValidator {

  def validate(
                fromDateStr: String,
                toDateStr: String,
                guestEmail: String
              ): (LocalDate, LocalDate) = {
    if (fromDateStr.trim.isEmpty || toDateStr.trim.isEmpty)
      throw UserError("fromDate and toDate must not be empty")

    val fromDate = try LocalDate.parse(fromDateStr)
    catch {
      case _: Exception => throw UserError("Invalid fromDate format")
    }

    val toDate = try LocalDate.parse(toDateStr)
    catch {
      case _: Exception => throw UserError("Invalid toDate format")
    }

    if (toDate.isBefore(fromDate))
      throw UserError("toDate must be after fromDate")

    if (guestEmail.trim.isEmpty || !guestEmail.contains("@"))
      throw UserError("Invalid guestEmail")

    (fromDate, toDate)
  }
}
