package testcontainers

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie._
import doobie.implicits._
import cats.effect.IO
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

trait PostgresTestContainer extends ForAllTestContainer {

}
