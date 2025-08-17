package services

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import kafka.BookingConflictProducerRunner
import models.{AlternativeDate, Booking, BookingConflictEvent, BookingResponse}
import repositories.BookingRepository

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.{ChronoField, ChronoUnit}
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import com.typesafe.config.Config

class BookingServiceImpl @Inject()(
                                    bookingRepository: BookingRepository,
                                    producerRunner: BookingConflictProducerRunner
                                  )(implicit ec: ExecutionContext) extends BookingService {

  private val formatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .optionalStart()
    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 6, true)
    .optionalEnd()
    .toFormatter()

  private def sendConflictEvent(event: BookingConflictEvent): Unit = producerRunner.producer.send(event).unsafeRunAndForget()

  def getBookingsByHomeId(homeId: UUID): IO[List[Booking]] = bookingRepository.getBookingsByHomeId(homeId)

  def createBooking(
                     homeId: UUID,
                     fromDate: LocalDate,
                     toDate: LocalDate,
                     guestEmail: String,
                     source: String
                   ): IO[BookingResponse] = {
    bookingRepository.createBooking(homeId, fromDate, toDate, guestEmail, source)
      .flatMap {
        case Right(_) => IO.pure(BookingResponse(true, "Booking created successfully", Seq.empty))
        case Left(_) => handleBookingConflicts(homeId, fromDate, toDate, guestEmail)
      }
  }

  private def handleBookingConflicts(
                                      homeId: UUID,
                                      fromDate: LocalDate,
                                      toDate: LocalDate,
                                      guestEmail: String
                                    ): IO[BookingResponse] = {
    bookingRepository.findConflictingBookings(homeId, fromDate, toDate)
      .flatMap { conflicts =>
        bookingRepository.getCurrentDbTime.map { dbTime =>
          val isConcurrency = conflicts.exists(b => LocalDateTime.parse(b.createdAt, formatter)
            .isAfter(LocalDateTime.ofInstant(dbTime, ZoneOffset.UTC)))
          val message = if (isConcurrency) "Another booking was made at the same time" else "Home is already booked for these dates"
          val eventType = if (isConcurrency) "Concurrency Conflict" else "Regular Conflict"

          sendConflictEvent(BookingConflictEvent(homeId, fromDate, toDate, guestEmail, eventType))

          BookingResponse(false, message, suggestAlternativeDates(fromDate, toDate, conflicts))
        }
      }
  }

  private def suggestAlternativeDates(
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
