package kafka

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import fs2.kafka._

import javax.inject._
import models.BookingConflictEvent
import play.api.Logger

import scala.concurrent.ExecutionContext
import kafka.KafkaSerdes._
import play.api.inject.ApplicationLifecycle
import repositories.BookingConflictRepository


@Singleton
class BookingConflictConsumer @Inject()(bookingConflictRepository: BookingConflictRepository, lifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext, runtime: IORuntime) {
  private val logger = Logger(this.getClass)

  println(">>> BookingConflictConsumer starting...")


  private val consumerSettings =
    ConsumerSettings(
      keyDeserializer   = Deserializer[IO, String],
      valueDeserializer = KafkaSerdes.jsonDeserializer[BookingConflictEvent]
    )
      .withBootstrapServers("localhost:9092")
      .withGroupId("booking-conflict-consumer-group")
      .withEnableAutoCommit(false)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)

  // fs2-kafka stream
  private val consumerStream =
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo("booking.conflicts")
      .records
      .evalMap { committable =>
        val event = committable.record.value
        bookingConflictRepository.insert(event).attempt.flatMap {
          case Left(err) => IO(println(s"⚠️ Error inserting booking conflict: $err")) *>
            committable.offset.commit // optionally commit offset to skip bad message
          case Right(_) =>
            committable.offset.commit
        }
      }

  // Run the stream and keep it alive
  private val running = consumerStream.compile.drain.start.unsafeRunSync()

  // Ensure consumer is canceled when app stopssay
  lifecycle.addStopHook { () =>
    IO {
      running.cancel
    }.unsafeToFuture()
  }
}
