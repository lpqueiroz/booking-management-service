package repositories

import cats.effect.IO
import com.google.inject.ImplementedBy
import models.BookingConflictEvent

@ImplementedBy(classOf[BookingConflictRepositoryImpl])
trait BookingConflictRepository {

  def insert(event: BookingConflictEvent): IO[BookingConflictEvent]

}
