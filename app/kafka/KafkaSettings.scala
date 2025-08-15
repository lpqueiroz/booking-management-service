package kafka

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import fs2.kafka._
import play.api.libs.json._
import models.BookingConflictEvent

object KafkaSettings {
  implicit val runtime: IORuntime = IORuntime.global

  implicit val bookingConflictEventFormat: OFormat[BookingConflictEvent] = Json.format[BookingConflictEvent]

  implicit val bookingConflictEventSerializer: Serializer[IO, BookingConflictEvent] =
    Serializer.string[IO].contramap(event => Json.stringify(Json.toJson(event)))

  implicit val bookingConflictEventDeserializer: Deserializer[IO, BookingConflictEvent] =
    Deserializer.string[IO].map(json => Json.parse(json).as[BookingConflictEvent])

  val producerSettings: ProducerSettings[IO, String, BookingConflictEvent] =
    ProducerSettings(
      Serializer[IO, String],
      bookingConflictEventSerializer
    ).withBootstrapServers("localhost:9092")

  val consumerSettings: ConsumerSettings[IO, String, BookingConflictEvent] =
    ConsumerSettings(
      Deserializer[IO, String],
      bookingConflictEventDeserializer
    )
      .withBootstrapServers("localhost:9092")
      .withGroupId("booking-consumer-group")
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
}
