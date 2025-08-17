package kafka

import cats.effect.{IO, Resource}
import fs2.kafka._

import javax.inject._
import models.BookingConflictEvent
import kafka.KafkaSerdes._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

@Singleton
class BookingConflictProducer @Inject() private (producer: KafkaProducer[IO, String, BookingConflictEvent], logger: Logger[IO]) {

  private val bookingTopic = "booking.conflicts"

  private def produce(record: ProducerRecord[String, BookingConflictEvent]): IO[ProducerResult[String, BookingConflictEvent]] =
    producer.produceOne(record).flatten

  def send(event: BookingConflictEvent): IO[Unit] = {
    val record = ProducerRecord(bookingTopic, event.homeId.toString, event)
    produce(record).attempt.flatMap {
      case Right(_) => logger.info(s"Produced event for homeId=${event.homeId}")

      case Left(e) => logger.error(e)(s"Failed to produce event for homeId=${event.homeId}")
    }
  }
}

object BookingConflictProducer {

  def resource(bootstrapServers: String): Resource[IO, BookingConflictProducer] = {
    val producerSettings = ProducerSettings(
      keySerializer   = Serializer[IO, String],
      valueSerializer = KafkaSerdes.jsonSerializer[BookingConflictEvent]
    ).withBootstrapServers(bootstrapServers)

    for {
      producer <- KafkaProducer.resource(producerSettings)
      logger   <- Resource.eval(Slf4jLogger.create[IO])
    } yield new BookingConflictProducer(producer, logger)
  }
}
