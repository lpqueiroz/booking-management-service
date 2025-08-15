package kafka

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import fs2.kafka._
import javax.inject._
import models.BookingConflictEvent
import scala.concurrent.ExecutionContext
import kafka.KafkaSerdes._
import play.api.inject.ApplicationLifecycle
import repositories.BookingConflictRepository

@Singleton
class BookingConflictConsumer @Inject()(bookingConflictRepository: BookingConflictRepository, lifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext, runtime: IORuntime) {

  private val consumerSettings =
    ConsumerSettings(
      keyDeserializer   = Deserializer[IO, String],
      valueDeserializer = KafkaSerdes.jsonDeserializer[BookingConflictEvent]
    )
      .withBootstrapServers("localhost:9092")
      .withGroupId("booking-conflict-consumer-group")
      .withEnableAutoCommit(false)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)

  private val consumerStream =
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo("booking.conflicts")
      .records
      .evalMap { committable =>
        val event = committable.record.value
        bookingConflictRepository.insert(event).attempt.flatMap {
          case Left(err) => IO(println(s"Error inserting booking conflict: $err")) *>
            committable.offset.commit
          case Right(_) =>
            committable.offset.commit
        }
      }

  private val running = consumerStream.compile.drain.start.unsafeRunSync()

  lifecycle.addStopHook { () =>
    IO {
      running.cancel
    }.unsafeToFuture()
  }
}
