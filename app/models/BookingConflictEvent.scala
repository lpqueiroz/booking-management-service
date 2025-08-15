package models

import play.api.libs.json.{Json, OFormat}
import java.time.LocalDate
import java.util.UUID

case class BookingConflictEvent(homeId: UUID, fromDate: LocalDate, toDate: LocalDate, guestEmail: String, reason: String)

object BookingConflictEvent {
  implicit val format: OFormat[BookingConflictEvent] = Json.format[BookingConflictEvent]
}
