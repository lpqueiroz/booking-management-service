package graphql

import repositories.BookingRepositoryImpl
import graphql.Scalars._
import graphql.BookingType
import graphql.QueryType
import sangria.schema.Schema
import services.{BookingService, BookingServiceImpl}

object SchemaDefinition {

  val BookingSchema: Schema[BookingService, Unit] =
    Schema(
      query = QueryType.QueryType,
      mutation = Some(MutationType.MutationType)
    )

}
