package kafka

import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxApplyOps
import fs2.kafka._
import javax.inject._
import models.BookingConflictEvent
import kafka.KafkaSerdes._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import repositories.BookingConflictRepository

@Singleton
class BookingConflictConsumer @Inject() private (bookingConflictRepository: BookingConflictRepository,
                                                 consumer: KafkaConsumer[IO, String, BookingConflictEvent],
                                                 logger: Logger[IO]) {

  private val bookingTopic = "booking.conflicts"

  def stream: fs2.Stream[IO, Unit] =
    fs2.Stream.eval(consumer.subscribeTo(bookingTopic)) *>
      consumer.stream.evalMap { committable =>
        val event = committable.record.value
        bookingConflictRepository.insert(event).attempt.flatMap {
          case Right(_) => committable.offset.commit
          case Left(e)  => logger.error(e)(s"Failed to insert booking conflict: $event") *> IO.unit
        }
      }
}

object BookingConflictConsumer {

  def resource(
                bootstrapServers: String,
                groupId: String,
                repo: BookingConflictRepository
              ): Resource[IO, BookingConflictConsumer] = {
    val consumerSettings =
      ConsumerSettings(
        keyDeserializer = Deserializer[IO, String],
        valueDeserializer = KafkaSerdes.jsonDeserializer[BookingConflictEvent]
      )
        .withBootstrapServers(bootstrapServers)
        .withGroupId(groupId)
        .withEnableAutoCommit(false)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)

    for {
      consumer <- KafkaConsumer.resource(consumerSettings)
      logger <- Resource.eval(Slf4jLogger.create[IO])
    } yield new BookingConflictConsumer(repo, consumer, logger)
  }
}
