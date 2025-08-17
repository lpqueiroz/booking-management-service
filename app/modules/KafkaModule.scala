package modules

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.google.inject.{AbstractModule, Provides}
import kafka.{BookingConflictConsumerRunner, BookingConflictProducerRunner}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class KafkaModule extends AbstractModule {

  override def configure(): Unit = {
    implicit val runtime: IORuntime = IORuntime.global

    bind(classOf[BookingConflictProducerRunner]).asEagerSingleton()
    bind(classOf[BookingConflictConsumerRunner]).asEagerSingleton()
  }

  @Provides
  def provideLogger: Logger[IO] = Slf4jLogger.getLogger[IO]
}
