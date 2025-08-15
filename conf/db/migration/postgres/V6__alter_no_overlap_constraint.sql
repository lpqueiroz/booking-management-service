ALTER TABLE bookings
DROP CONSTRAINT no_overlap;

ALTER TABLE bookings
ADD CONSTRAINT no_overlap
EXCLUDE USING gist (
  home_id WITH =,
  daterange(from_date, to_date, '[)') WITH &&
);
