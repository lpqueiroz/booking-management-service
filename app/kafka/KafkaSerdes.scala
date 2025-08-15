package kafka

import cats.effect.IO
import fs2.kafka.{Deserializer, Serializer}
import play.api.libs.json._
import scala.util.{Try, Success, Failure}

object KafkaSerdes {
  implicit def jsonSerializer[A: Writes]: Serializer[IO, A] =
    Serializer.lift[IO, A](a => IO.pure(Json.stringify(Json.toJson(a)).getBytes))

  implicit def jsonDeserializer[A: Reads]: Deserializer[IO, A] =
    Deserializer.lift[IO, A] { bytes =>
      IO.fromTry(
        Try(Json.parse(bytes)).flatMap(_.validate[A] match {
          case JsSuccess(value, _) => Success(value)
          case JsError(errors)     => Failure(new RuntimeException(errors.toString))
        })
      )
    }
}
