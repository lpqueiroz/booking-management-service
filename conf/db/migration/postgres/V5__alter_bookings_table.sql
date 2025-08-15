CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings
ADD CONSTRAINT no_overlap
EXCLUDE USING gist (
  home_id WITH =,
  daterange(from_date, to_date, '[]') WITH &&
);

ALTER TABLE bookings
ADD CONSTRAINT chk_dates CHECK (from_date < to_date);