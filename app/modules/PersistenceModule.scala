package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import cats.effect.IO
import doobie.util.transactor.Transactor

class PersistenceModule extends AbstractModule {

  @Provides
  @Singleton
  def provideTransactor(): Transactor[IO] = {
    Transactor.fromDriverManager[IO].apply(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "myuser",
      "mypassword",
      None
    )
  }

  override def configure(): Unit = {}
}
