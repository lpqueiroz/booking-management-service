package repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import models.Booking

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

    val insertSql =
      sql"""
        INSERT INTO bookings (home_id, from_date, to_date, guest_email, source)
        VALUES ($homeId, $fromDate, $toDate, $guestEmail, $source)
        RETURNING id, home_id, from_date, to_date, guest_email, source, created_at
      """

    insertSql
      .query[Booking]
      .unique
      .transact(xa)
      .attempt
  }

  def getBookingsByHomeId(id: UUID): IO[List[Booking]] = {
    sql"""
      SELECT id, home_id, from_date, to_date, guest_email, source, created_at
      FROM bookings
      WHERE home_id = $id
    """.query[Booking].to[List].transact(xa)
  }

  def findConflictingBookings(homeId: UUID, fromDate: LocalDate, toDate: LocalDate): IO[List[Booking]] = {
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

  def getCurrentDbTime: IO[Instant] =
    sql"SELECT now()".query[Instant].unique.transact(xa)
}
