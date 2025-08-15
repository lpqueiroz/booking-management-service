package kafka

import javax.inject._
import fs2.kafka._
import cats.effect.IO
import cats.effect.unsafe.IORuntime

@Singleton
class KafkaProducerProvider  {
  implicit val runtime: IORuntime = IORuntime.global
  val producer: KafkaProducer.Metrics[IO, String, String] =
    KafkaProducer.resource(
      ProducerSettings[IO, String, String].withBootstrapServers("localhost:9092")
    ).allocated.unsafeRunSync()._1
}
