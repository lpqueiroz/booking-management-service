package repositories

import cats.effect.IO
import models.BookingConflictEvent
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import javax.inject.Inject

class BookingConflictRepositoryImpl @Inject()(xa: Transactor[IO]) extends BookingConflictRepository {

  def insert(event: BookingConflictEvent): IO[BookingConflictEvent] = {
    val insertSql =
      sql"""
        INSERT INTO booking_conflicts (home_id, attempted_from_date, attempted_to_date, guest_email, conflict_reason)
        VALUES (${event.homeId}, ${event.fromDate}, ${event.toDate}, ${event.guestEmail}, ${event.reason})
        RETURNING home_id, attempted_from_date, attempted_to_date, guest_email, conflict_reason
      """

    insertSql
      .query[BookingConflictEvent]
      .unique
      .transact(xa)
  }

}
