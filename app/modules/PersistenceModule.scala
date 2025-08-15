package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import cats.effect.IO
import com.typesafe.config.ConfigFactory
import doobie.util.transactor.Transactor

class PersistenceModule extends AbstractModule {

  val config = ConfigFactory.load()
  val dbConfig = config.getConfig("db.default")

  val url      = dbConfig.getString("url")
  val user     = dbConfig.getString("username")
  val password = dbConfig.getString("password")
  val driver   = dbConfig.getString("driver")

  @Provides
  @Singleton
  def provideTransactor(): Transactor[IO] = {
    Transactor.fromDriverManager[IO].apply(
      driver,
      url,
      user,
      password,
      None
    )
  }

  override def configure(): Unit = {}
}
