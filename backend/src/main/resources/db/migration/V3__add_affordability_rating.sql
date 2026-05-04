ALTER TABLE ratings
    ADD COLUMN affordability_rating NUMERIC(4,2) NOT NULL DEFAULT 5.00
    CHECK (affordability_rating BETWEEN 1.0 AND 10.0);
