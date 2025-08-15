package kafka

import cats.effect.{IO, Resource}
import cats.effect.unsafe.IORuntime
import fs2.kafka._
import javax.inject._
import models.BookingConflictEvent
import play.api.Logger
import kafka.KafkaSerdes._

@Singleton
class BookingConflictProducer @Inject()(private val producer: KafkaProducer[IO, String, BookingConflictEvent])(implicit runtime: IORuntime) {
  private val logger = Logger(this.getClass)

  private val producerSettings =
    ProducerSettings(
      keySerializer   = Serializer[IO, String],
      valueSerializer = KafkaSerdes.jsonSerializer[BookingConflictEvent]
    ).withBootstrapServers("localhost:9092")

  def send(event: BookingConflictEvent): IO[Unit] = {
    producer
      .produceOne(ProducerRecord("booking.conflicts", event.homeId.toString, event))
      .flatten >>
      IO {
        logger.info(s"Produced event for homeId=${event.homeId}")
      }
  }
}

object BookingConflictProducer {
  implicit val runtime: IORuntime = IORuntime.global

  def resource(bootstrapServers: String): Resource[IO, BookingConflictProducer] = {
    val producerSettings = ProducerSettings(
      keySerializer   = Serializer[IO, String],
      valueSerializer = KafkaSerdes.jsonSerializer[BookingConflictEvent]
    ).withBootstrapServers(bootstrapServers)

    KafkaProducer
      .resource(producerSettings)
      .map(producer => new BookingConflictProducer(producer))
  }
}
