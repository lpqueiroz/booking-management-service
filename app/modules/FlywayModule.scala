package modules

import com.google.inject.{AbstractModule, Singleton}
import com.typesafe.config.ConfigFactory
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

  val config = ConfigFactory.load()
  val dbConfig = config.getConfig("db.default")

  val url      = dbConfig.getString("url")
  val user     = dbConfig.getString("username")
  val password = dbConfig.getString("password")

  private val flyway = Flyway.configure()
    .dataSource(
      url,
      user,
      password
    )
    .load()

  flyway.migrate()

  lifecycle.addStopHook(() => Future.successful(()))
}
