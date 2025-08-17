package services

import cats.effect.IO
import com.google.inject.ImplementedBy
import models.{Booking, BookingResponse}

import java.time.LocalDate
import java.util.UUID


@ImplementedBy(classOf[BookingServiceImpl])
trait BookingService {

  def createBooking(
                     homeId: UUID,
                     fromDate: LocalDate,
                     toDate: LocalDate,
                     guestEmail: String,
                     source: String
                   ): IO[BookingResponse]

  def getBookingsByHomeId(homeId: UUID): IO[List[Booking]]
}
