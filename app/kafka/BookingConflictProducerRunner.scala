package kafka

import cats.effect.{IO, Resource}
import cats.effect.unsafe.IORuntime
import play.api.Configuration

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

@Singleton
class BookingConflictProducerRunner @Inject()(
                                               lifecycle: ApplicationLifecycle,
                                               config: Configuration
                                             )(implicit runtime: IORuntime) {

  private val bootstrapServers = config.get[String]("kafka.bootstrapServer")

  private val producerResource: Resource[IO, BookingConflictProducer] =
    BookingConflictProducer.resource(bootstrapServers)

  private val allocated: (BookingConflictProducer, IO[Unit]) = producerResource.allocated.unsafeRunSync()

  val producer: BookingConflictProducer = allocated._1
  private val finalizer: IO[Unit] = allocated._2

  lifecycle.addStopHook { () =>
    finalizer.unsafeToFuture()
  }
}

