package graphql

import sangria.schema._
import sangria.macros.derive._
import models.{AlternativeDate, Booking, BookingResponse}
import repositories.BookingRepositoryImpl
import graphql.Scalars._

object BookingType {
  // Make sure to use the SAME context type: BookingRepository
  implicit val BookingType: OutputType[Booking] =
    deriveObjectType[BookingRepositoryImpl, Booking](
      ObjectTypeDescription("A booking for a home"),
      DocumentField("id", "Booking ID"),
      DocumentField("homeId", "Home ID for the booking"),
      DocumentField("fromDate", "Start date (YYYY-MM-DD)"),
      DocumentField("toDate", "End date (YYYY-MM-DD)"),
      DocumentField("guestEmail", "Email of the guest"),
      DocumentField("source", "Booking source"),
      DocumentField("createdAt", "When the booking was created")
    )

  implicit val AlternativeDateType: ObjectType[Unit, AlternativeDate] = ObjectType(
    "AlternativeDate",
    "Suggested alternative booking dates",
    fields[Unit, AlternativeDate](
      Field("from", StringType, resolve = _.value.from),
      Field("to", StringType, resolve = _.value.to)
    )
  )

  implicit val BookingResponseType: OutputType[BookingResponse] =
    deriveObjectType[BookingRepositoryImpl, BookingResponse](
      ObjectTypeDescription("A booking for a home"),
      DocumentField("success", "Success or Failure"),
      DocumentField("message", "Message"),
      DocumentField("alternativeDates", "Alternative Dates for Booking")
    )
}
