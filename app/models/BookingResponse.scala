package models

case class BookingResponse(success: Boolean, message: String, alternativeDates: Seq[AlternativeDate])

case class AlternativeDate(from: String, to: String)
