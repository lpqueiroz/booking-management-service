package testcontainers

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import doobie.implicits.toSqlInterpolator
import models.{Booking, BookingConflictEvent}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import doobie.implicits._
import doobie.postgres.implicits._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import com.typesafe.config.ConfigFactory
import doobie._
import doobie.implicits._
import graphql.SchemaDefinition
import io.circe.Json
import repositories.BookingRepositoryImpl
import sangria.execution.Executor
import sangria.parser.QueryParser
import sangria.schema.Schema
import services.{BookingService, BookingServiceImpl}
import sangria.marshalling.circe._


class BookingIntegrationSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterEach with PostgresTestContainer
  with KafkaTestContainer with BeforeAndAfterAll {

  behavior of "Booking API"

  implicit val runtime: IORuntime = IORuntime.global
  val bookingRepository = new BookingRepositoryImpl(xa)
  val testConfig = ConfigFactory.load()
  val bookingService: BookingServiceImpl = new BookingServiceImpl(bookingRepository, testConfig)
  val schema: Schema[BookingService, Unit] = SchemaDefinition.BookingSchema

  override def beforeAll(): Unit = {
    kafkaContainer.start()
    container.start()
  }

  override def afterAll(): Unit = {
    kafkaContainer.stop()
    container.stop()
  }


  override protected def beforeEach(): Unit = {
    sql"TRUNCATE bookings, booking_conflicts RESTART IDENTITY"
      .update
      .run
      .transact(xa)
      .unsafeRunSync()
  }

  private def executeGraphQL(query: String, variables: Map[String, Any] = Map.empty): IO[Json] = {
    IO.fromFuture(IO {
      val ast = QueryParser.parse(query).get
      Executor.execute(
        schema = schema,
        queryAst = ast,
        userContext = bookingService,           // <-- service as context
        variables = sangria.marshalling.InputUnmarshaller.mapVars(variables),
        deferredResolver = sangria.execution.deferred.DeferredResolver.empty
      )
    })
  }

  private def listBookings(): IO[List[Booking]] =
    sql"SELECT id, home_id, from_date, to_date, guest_email, source, created_at FROM bookings"
      .query[Booking]
      .to[List]
      .transact(xa)

  private def listConflicts(): IO[List[BookingConflictEvent]] =
    sql"SELECT * FROM booking_conflicts"
      .query[BookingConflictEvent]
      .to[List]
      .transact(xa)

  it should "HAPPY PATH: create a booking and retrieve it" in {
    val mutation =
      """
        mutation {
          createBooking(
            homeId: "67f240fa-6a3b-4472-b230-42757b2caf8f",
            fromDate: "2025-11-29",
            toDate: "2025-11-30",
            guestEmail: "guest@example.com",
            source: "Website"
          ) {
            success,
            message,
            alternativeDates {
                from,
                to
            }
          }
        }
      """

    for {
      resp     <- executeGraphQL(mutation).unsafeToFuture()
      bookings <- listBookings().unsafeToFuture()
    } yield {
      resp.hcursor.downField("data").downField("createBooking").get[Boolean]("success").toOption.get shouldBe true
      bookings should have size 1
    }
  }

  it should "CONFLICT PATH: second booking with overlapping dates fails and records conflict" in {
    val firstBooking =
      """
        mutation {
          createBooking(
            homeId: "67f240fa-6a3b-4472-b230-42757b2caf8f",
            fromDate: "2025-11-29",
            toDate: "2025-11-30",
            guestEmail: "guest@example.com",
            source: "Website"
          ) {
              success,
              message,
              alternativeDates {
                  from,
                  to
              }
          }
        }
      """

    val secondBooking =
      """
        mutation {
          createBooking(
            homeId: "67f240fa-6a3b-4472-b230-42757b2caf8f",
            fromDate: "2025-11-29",
            toDate: "2025-11-30",
            guestEmail: "guest@example.com",
            source: "Website"
          ) {
              success,
              message,
              alternativeDates {
                  from,
                  to
              }
          }
        }
      """

    for {
      _         <- executeGraphQL(firstBooking).unsafeToFuture()
      resp2     <- executeGraphQL(secondBooking).unsafeToFuture()
      bookings  <- listBookings().unsafeToFuture()
      conflicts <- listConflicts().unsafeToFuture()
    } yield {
      resp2.hcursor.downField("data").downField("createBooking").get[Boolean]("success").toOption.get shouldBe false
      bookings should have size 1
//      conflicts should have size 1
    }
  }

  it should "RACE CONDITION PATH: concurrent booking attempts result in one success and one conflict" in {
    val mutation =
      """
        mutation {
          createBooking(
          homeId: "67f240fa-6a3b-4472-b230-42757b2caf8f",
          fromDate: "2025-11-29",
          toDate: "2025-11-30",
          guestEmail: "guest@example.com",
          source: "Website"
        ) {
            success,
            message,
            alternativeDates {
                from,
                to
            }
          }
        }
    """

    val booking1 = executeGraphQL(mutation)
    val booking2 = executeGraphQL(mutation)

    for {
      resultsTuple <- (booking1, booking2).parTupled.unsafeToFuture() // (Json, Json)
      bookings     <- listBookings().unsafeToFuture()
      conflicts    <- listConflicts().unsafeToFuture()
    } yield {
      // Convert tuple to sequence so we can count successes
      val results = Seq(resultsTuple._1, resultsTuple._2)

      val successes = results.count { r =>
        r.hcursor
          .downField("data")
          .downField("createBooking")
          .get[Boolean]("success")
          .toOption
          .get
      }

      // Assertions
      successes shouldBe 1
      bookings should have size 1
//      conflicts should have size 1
    }
  }
}
