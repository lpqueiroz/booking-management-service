package graphql

import cats.effect.unsafe.implicits.global
import sangria.schema._
import services.BookingService

import java.util.UUID

object QueryType {
  val QueryType: ObjectType[BookingService, Unit] = ObjectType(
    "Query",
    fields[BookingService, Unit](
      Field(
        name = "bookings",
        fieldType = ListType(BookingType.BookingType),
        arguments = Argument("homeId", Scalars.UUIDType) :: Nil,
        resolve = ctx => ctx.ctx.getBookingsByHomeId(ctx.arg[UUID]("homeId")).unsafeToFuture()
      )
    )
  )
}
