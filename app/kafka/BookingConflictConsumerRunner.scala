package kafka

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, OutcomeIO}
import play.api.Configuration

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import repositories.BookingConflictRepository

import scala.concurrent.ExecutionContext

@Singleton
class BookingConflictConsumerRunner @Inject()(
                                               repo: BookingConflictRepository,
                                               lifecycle: ApplicationLifecycle,
                                               config: Configuration
                                             )(implicit runtime: IORuntime, ec: ExecutionContext) {

  private val bootstrapServers = config.get[String]("kafka.bootstrapServer")
  private val groupId          = config.get[String]("kafka.consumerGroupId")

  private val allocated: (IO[OutcomeIO[Unit]], IO[Unit]) =
    BookingConflictConsumer
      .resource(bootstrapServers, groupId, repo)
      .flatMap(_.stream.compile.drain.background)
      .allocated
      .unsafeRunSync()

  private val running: IO[OutcomeIO[Unit]] = allocated._1

  private val finalizer: IO[Unit] = allocated._2

  running.unsafeRunAsync(_ => ())

  lifecycle.addStopHook { () =>
    finalizer.unsafeToFuture()
  }
}
