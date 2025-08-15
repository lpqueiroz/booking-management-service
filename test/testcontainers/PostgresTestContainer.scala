package testcontainers

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie._
import doobie.implicits._
import cats.effect.IO
import org.flywaydb.core.Flyway
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.testcontainers.utility.DockerImageName

trait PostgresTestContainer extends ForAllTestContainer { self: AsyncFlatSpec with Matchers =>

  override val container: PostgreSQLContainer = PostgreSQLContainer(DockerImageName.parse("postgres:13"))

  lazy val xa: Transactor[IO] = {
    container.start()

    Transactor.fromDriverManager[IO](
      container.driverClassName,
      container.jdbcUrl,
      container.username,
      container.password,
      None
    )
  }

  override def afterStart(): Unit = {
    // Run DB migrations here if needed (Flyway, Liquibase, etc.)
    // Example with Flyway:
     Flyway.configure().dataSource(container.jdbcUrl, container.username, container.password).load().migrate()
  }
}
