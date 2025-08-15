package modules

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.google.inject.AbstractModule
import fs2.kafka.{KafkaProducer, ProducerSettings}
import kafka.{BookingConflictConsumer, BookingConflictProducer, KafkaSerdes}
import models.BookingConflictEvent

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[BookingConflictConsumer]).asEagerSingleton()

    implicit val runtime: IORuntime = IORuntime.global

    val bootstrap = "localhost:9092"
    val producerSettings = ProducerSettings[IO, String, BookingConflictEvent](
      keySerializer   = fs2.kafka.Serializer[IO, String],
      valueSerializer = KafkaSerdes.jsonSerializer[BookingConflictEvent]
    ).withBootstrapServers(bootstrap)

    val producer: KafkaProducer[IO, String, BookingConflictEvent] =
      KafkaProducer.resource(producerSettings).allocated.unsafeRunSync()._1

    bind(classOf[BookingConflictProducer])
      .toInstance(new BookingConflictProducer(producer))
  }
}
