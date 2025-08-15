package repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import javax.inject._
import models.Booking

import java.sql.{Date, Timestamp}
import java.text.SimpleDateFormat
import java.time.{Instant, LocalDate}
import java.util.UUID
import javax.inject.Inject

class BookingRepositoryImpl @Inject()(xa: Transactor[IO]) extends BookingRepository {

  def createBooking(
                     homeId: UUID,
                     fromDate: LocalDate,
                     toDate: LocalDate,
                     guestEmail: String,
                     source: String
                   ): IO[Either[Throwable, Booking]] = {

    // SQL to insert booking (id and created_at handled by DB defaults)
    val insertSql =
      sql"""
        INSERT INTO bookings (home_id, from_date, to_date, guest_email, source)
        VALUES ($homeId, $fromDate, $toDate, $guestEmail, $source)
        RETURNING id, home_id, from_date, to_date, guest_email, source, created_at
      """

    insertSql
      .query[Booking] // Maps directly to Booking case class
      .unique
      .transact(xa)
      .attempt
  }

  def getBookingsByHomeId(id: UUID): IO[List[Booking]] = {
    // fetch from DB...
    sql"""
      SELECT id, home_id, from_date, to_date, guest_email, source, created_at
      FROM bookings
      WHERE home_id = $id
    """.query[Booking].to[List].transact(xa)
  }

  def findConflictingBookings(homeId: UUID, fromDate: LocalDate, toDate: LocalDate): IO[List[Booking]] = {

    println(">>> find conflicting bookings")

    sql"""
      SELECT
        id,
        home_id,
        from_date::text AS from_date,
        to_date::text AS to_date,
        guest_email,
        source,
        created_at::text AS created_at
      FROM bookings
      WHERE home_id = $homeId
        AND daterange(from_date, to_date, '[)') &&
            daterange($fromDate, $toDate, '[)')
    """
      .query[Booking]
      .to[List]
      .transact(xa)
  }
}
