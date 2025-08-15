//package modules
//
//
//import cats.effect.{IO, Resource}
//import cats.effect.unsafe.IORuntime
//import fs2.kafka._
//import kafka.{BookingConflictConsumer, BookingConflictProducer}
//import models.BookingConflictEvent
//import play.api.{Configuration, Environment}
//
//import javax.inject._
//import scala.concurrent.ExecutionContext
//import models.BookingConflictEvent._
//
//class KafkaModule @Inject() (
//                              env: Environment,
//                              config: Configuration,
//                              bookingConsumer: BookingConflictConsumer,
//                              producer: BookingConflictProducer
//                            )(implicit ec: ExecutionContext) {
//
//  // Producer settings
//  val producerSettings: ProducerSettings[IO, String, BookingConflictEvent] =
//    ProducerSettings(
//      keySerializer = Serializer[IO, String],
//      valueSerializer = bookingConflictEventSerializer
//    ).withBootstrapServers("localhost:9092")
//
//  // Consumer settings
//  val consumerSettings: ConsumerSettings[IO, String, BookingConflictEvent] =
//    ConsumerSettings(
//      keyDeserializer = Deserializer[IO, String],
//      valueDeserializer = bookingConflictEventDeserializer
//    )
//      .withBootstrapServers("localhost:9092")
//      .withGroupId("booking-consumer-group")
//      .withAutoOffsetReset(AutoOffsetReset.Earliest)
//
//  val producerResource: Resource[IO, KafkaProducer[IO, String, BookingConflictEvent]] =
//    KafkaProducer.resource(producerSettings)
//
//  // Start consumer stream at startup
//  IO.println("Starting Kafka consumer...").unsafeRunSync()
//  bookingConsumer.start(consumerSettings).unsafeRunAndForget()
//}
