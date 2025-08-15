package graphql

import sangria.schema._
import services.BookingService
import java.util.UUID

object QueryType {
  val QueryType: ObjectType[BookingService, Unit] = ObjectType(
    "Query",
    fields[BookingService, Unit](
      Field(
        name = "bookings",
        fieldType = ListType(BookingType.BookingType), // BookingType implicit is in scope
        arguments = Argument("homeId", Scalars.UUIDType) :: Nil,
        resolve = ctx => ctx.ctx.getBookingsByHomeId(ctx.arg[UUID]("homeId"))
      )
    )
  )
}
