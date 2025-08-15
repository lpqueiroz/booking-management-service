package services

import com.google.inject.ImplementedBy
import models.{Booking, BookingResponse}

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[BookingServiceImpl])
trait BookingService {

  def createBooking(
                     homeId: UUID,
                     fromDate: String,
                     toDate: String,
                     guestEmail: String,
                     source: String
                   ): Future[BookingResponse]

  def getBookingsByHomeId(homeId: UUID): Future[List[Booking]]

}
