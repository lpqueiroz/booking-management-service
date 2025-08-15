package graphql

import graphql.Scalars._
import sangria.schema.Schema
import services.BookingService

object SchemaDefinition {

  val BookingSchema: Schema[BookingService, Unit] =
    Schema(
      query = QueryType.QueryType,
      mutation = Some(MutationType.MutationType)
    )
}
