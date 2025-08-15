package services

import cats.effect.unsafe.implicits.global
import kafka.BookingConflictProducer
import models.{AlternativeDate, Booking, BookingConflictEvent, BookingResponse}
import repositories.BookingRepository
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.{ChronoField, ChronoUnit}
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future
import cats.effect.unsafe.IORuntime
import com.typesafe.config.Config
import org.postgresql.util.PSQLException

class BookingServiceImpl @Inject()(bookingRepository: BookingRepository, config: Config) extends BookingService {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  private val bootstrap = config.getString("kafka.producer.bootstrap.servers")
  private val producerResource = BookingConflictProducer.resource(bootstrap)
  private val formatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .optionalStart()
    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 6, true)
    .optionalEnd()
    .toFormatter()

  def getBookingsByHomeId(homeId: UUID): Future[List[Booking]] = {
    bookingRepository.getBookingsByHomeId(homeId).unsafeToFuture()(IORuntime.global)
  }

  def createBooking(
                     homeId: UUID,
                     fromDate: LocalDate,
                     toDate: LocalDate,
                     guestEmail: String,
                     source: String
                   ): Future[BookingResponse] = {
    bookingRepository.createBooking(homeId, fromDate, toDate, guestEmail, source)
      .unsafeToFuture()(IORuntime.global)
      .recover {
        case e: PSQLException if e.getSQLState == "23P01" => Left(e) // Only overlap errors
      }
      .flatMap {
        case Right(_) => Future.successful(BookingResponse(true, "Booking created successfully", Seq.empty))
        case Left(_) => handleBookingConflicts(homeId, fromDate, toDate, guestEmail)
      }
  }

  private def handleBookingConflicts(
                                      homeId: UUID,
                                      fromDate: LocalDate,
                                      toDate: LocalDate,
                                      guestEmail: String
                                    ): Future[BookingResponse] = {
    bookingRepository.findConflictingBookings(homeId, fromDate, toDate)
      .unsafeToFuture()
      .flatMap { conflicts =>
        bookingRepository.getCurrentDbTime.unsafeToFuture().map { dbTime =>
          val isConcurrency = conflicts.exists(b => LocalDateTime.parse(b.createdAt, formatter)
            .isAfter(LocalDateTime.ofInstant(dbTime, ZoneOffset.UTC)))
          val message = if (isConcurrency) "Another booking was made at the same time" else "Home is already booked for these dates"
          val eventType = if (isConcurrency) "Concurrency Conflict" else "Regular Conflict"

          sendEventToKafka(BookingConflictEvent(homeId, fromDate, toDate, guestEmail, eventType))

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

  private def sendEventToKafka(event: BookingConflictEvent) = {
    producerResource.use { producer =>
      producer.send(event)
    }.unsafeRunAndForget()
  }
}
