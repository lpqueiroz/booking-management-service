package graphql

import exceptions.UserError
import sangria.schema._
import services.BookingService

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.Future


object MutationType {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val MutationType: ObjectType[BookingService, Unit] = ObjectType(
    "Mutation",
    fields[BookingService, Unit](
      Field(
        name = "createBooking",
        fieldType = BookingType.BookingResponseType, // Returns created booking
        arguments = List(
          Argument("homeId", Scalars.UUIDType),
          Argument("fromDate", StringType),
          Argument("toDate", StringType),
          Argument("guestEmail", StringType),
          Argument("source", StringType)
        ),
        resolve = ctx => {
          val homeId     = ctx.arg[UUID]("homeId")
          val fromDate   = ctx.arg[String]("fromDate")
          val toDate     = ctx.arg[String]("toDate")
          val guestEmail = ctx.arg[String]("guestEmail")
          val source     = ctx.arg[String]("source")

          val fromDateConverted: LocalDate = LocalDate.parse(fromDate)
          val toDateConverted: LocalDate = LocalDate.parse(toDate)

          if (toDateConverted.isBefore(fromDateConverted)) {
            throw UserError("fromDate must be before toDate")
          } else {
            ctx.ctx.createBooking(homeId, fromDateConverted, toDateConverted, guestEmail, source)
              .recover {
                case ex: Exception =>
                  throw UserError("Booking could not be created: " + ex.getMessage)
              }
          }
        }
      )
    )
  )
}
