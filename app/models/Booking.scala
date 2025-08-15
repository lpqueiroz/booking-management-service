package models

import java.util.UUID

case class Booking(
                    id: UUID,
                    homeId: UUID,
                    fromDate: String,
                    toDate: String,
                    guestEmail: String,
                    source: String,
                    createdAt: String
                  )
