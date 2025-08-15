package modules

import com.google.inject.{AbstractModule, Singleton}
import javax.inject._
import org.flywaydb.core.Flyway
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future

class FlywayModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[FlywayStarter]).asEagerSingleton()
  }
}

// This class runs Flyway on startup
@Singleton
class FlywayStarter @Inject()(lifecycle: ApplicationLifecycle) {
  private val flyway = Flyway.configure()
    .dataSource("jdbc:postgresql://localhost:5432/postgres", "myuser", "mypassword")
    .load()

  flyway.migrate()

  lifecycle.addStopHook(() => Future.successful(()))
}
