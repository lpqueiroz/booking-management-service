package models

import java.time.LocalDate

case class BookingResponse(success: Boolean, message: String, alternativeDates: Seq[AlternativeDate])

case class AlternativeDate(from: String, to: String)
