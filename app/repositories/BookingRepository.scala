package repositories

import cats.effect.IO
import com.google.inject.ImplementedBy
import models.Booking

import java.time.LocalDate
import java.util.UUID

@ImplementedBy(classOf[BookingRepositoryImpl])
trait BookingRepository {

  def createBooking(
                     homeId: UUID,
                     fromDate: LocalDate,
                     toDate: LocalDate,
                     guestEmail: String,
                     source: String
                   ): IO[Either[Throwable, Booking]]

  def getBookingsByHomeId(id: UUID): IO[List[Booking]]

  def findConflictingBookings(homeId: UUID, fromDate: LocalDate, toDate: LocalDate): IO[List[Booking]]
}
