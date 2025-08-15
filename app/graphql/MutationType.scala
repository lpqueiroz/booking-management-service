package graphql

import exceptions.UserError
import sangria.schema._
import services.BookingService
import java.util.UUID


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

          ctx.ctx.createBooking(homeId, fromDate, toDate, guestEmail, source)
            .recover {
              case ex: Exception =>
                throw UserError("Booking could not be created: " + ex.getMessage)
            }
        }
      )
    )
  )
}
