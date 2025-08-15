package services

import cats.effect.unsafe.implicits.global
import kafka.BookingConflictProducer
import models.{AlternativeDate, Booking, BookingConflictEvent, BookingResponse}
import repositories.BookingRepository
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.{ChronoField, ChronoUnit}
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future
import cats.effect.unsafe.IORuntime

class BookingServiceImpl @Inject()(bookingRepository: BookingRepository) extends BookingService {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val bootstrap = "localhost:9092"
  val producerResource = BookingConflictProducer.resource(bootstrap)

  def createBooking(
                     homeId: UUID,
                     fromDate: String,
                     toDate: String,
                     guestEmail: String,
                     source: String
                   ): Future[BookingResponse] = {

    val fromDateConverted: LocalDate = LocalDate.parse(fromDate)
    val toDateConverted: LocalDate = LocalDate.parse(toDate)
    val formatter: DateTimeFormatter = new DateTimeFormatterBuilder()
      .appendPattern("yyyy-MM-dd HH:mm:ss")
      .optionalStart()
      .appendFraction(ChronoField.NANO_OF_SECOND, 1, 6, true)
      .optionalEnd()
      .toFormatter()

    if (toDateConverted.isBefore(fromDateConverted)) {
      Future.failed(new Exception("Start date must be before end date"))
    } else {
      val txStart = LocalDateTime.now()
      bookingRepository.createBooking(homeId, fromDateConverted, toDateConverted, guestEmail, source).unsafeToFuture()(IORuntime.global).flatMap {
        case Right(_) => Future.successful(BookingResponse(true, "Booking created successfully", Seq.empty))
        case Left(e) => {

          getBookingsByHomeId(homeId).flatMap(bookings => {
            val alternatives = suggestAlternativeDates(fromDateConverted, toDateConverted, bookings)

            bookingRepository.findConflictingBookings(homeId, fromDateConverted, toDateConverted).unsafeToFuture().flatMap(conflicts => {
              if (conflicts.exists(b => LocalDateTime.parse(b.createdAt, formatter).isAfter(txStart))) {
                val event = BookingConflictEvent(homeId, fromDateConverted, toDateConverted, guestEmail, "Concurrency conflict")
                producerResource.use { producer =>
                  producer.send(event)
                }.unsafeRunAndForget()
                Future.successful(BookingResponse(false, "Another booking was made at the same time", alternatives))
              } else {
                val event = BookingConflictEvent(homeId, fromDateConverted, toDateConverted, guestEmail, "Regular conflict")
                producerResource.use { producer =>
                  producer.send(event)
                }.unsafeRunAndForget()
                Future.successful(BookingResponse(false, "Overlapping dates", alternatives))
              }
            })
          })
        }
      }
    }
  }

  def getBookingsByHomeId(homeId: UUID): Future[List[Booking]] = {
    bookingRepository.getBookingsByHomeId(homeId).unsafeToFuture()(IORuntime.global)
  }

  def suggestAlternativeDates(
                               desiredFrom: LocalDate,
                               desiredTo: LocalDate,
                               existingBookings: Seq[Booking],
                               maxSuggestions: Int = 3
                             ): Seq[AlternativeDate] = {

    val durationDays: Long = ChronoUnit.DAYS.between(desiredFrom, desiredTo)
    val shifts: Seq[Long] = Seq(1, 2, 3, -1, -2, -3)

    shifts
      .map { shift =>
        val candidateFrom = desiredFrom.plusDays(shift)
        val candidateTo = candidateFrom.plusDays(durationDays)
        (candidateFrom, candidateTo)
      }
      .filter { case (candidateFrom, candidateTo) =>
        !existingBookings.exists(b =>
          candidateFrom.isBefore(LocalDate.parse(b.toDate)) && candidateTo.isAfter(LocalDate.parse(b.fromDate))
        )
      }
      .take(maxSuggestions)
      .map(t => AlternativeDate(t._1.toString, t._2.toString))
  }
}
