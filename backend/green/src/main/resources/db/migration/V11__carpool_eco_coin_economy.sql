ALTER TABLE trips
    ADD COLUMN price_eco_coins BIGINT NOT NULL DEFAULT 1;

ALTER TABLE trips
    ADD CONSTRAINT chk_trips_price_eco_coins
        CHECK (price_eco_coins BETWEEN 1 AND 100000);


ALTER TABLE trip_participants
    ADD COLUMN reserved_eco_coins BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'RESERVED';

UPDATE trip_participants participant
SET payment_status =
        CASE
            WHEN participant.is_cancelled = TRUE
                OR trip.trip_status = 'CANCELLED'
                THEN 'REFUNDED'
            WHEN trip.trip_status = 'COMPLETED'
                THEN 'SETTLED'
            ELSE 'RESERVED'
            END
    FROM trips trip
WHERE participant.trip_id = trip.id;

ALTER TABLE trip_participants
    ADD CONSTRAINT chk_trip_participant_reserved_coins
        CHECK (reserved_eco_coins >= 0),

    ADD CONSTRAINT chk_trip_participant_payment_status
        CHECK (
            payment_status IN (
                'RESERVED',
                'REFUNDED',
                'SETTLED'
            )
        ),

    ADD CONSTRAINT chk_trip_participant_payment_consistency
        CHECK (
            (is_cancelled = TRUE AND payment_status = 'REFUNDED')
            OR
            (
                is_cancelled = FALSE
                AND payment_status IN ('RESERVED', 'SETTLED')
            )
        );


ALTER TABLE eco_transactions
DROP CONSTRAINT chk_eco_transactions_source;

ALTER TABLE eco_transactions
    ADD CONSTRAINT chk_eco_transactions_source
        CHECK (
            source IN (
                       'TRIP_COMPLETED',
                       'WASTE_DEPOSIT',
                       'ADMIN_ADJUSTMENT',
                       'CARPOOL_FARE_RESERVED',
                       'CARPOOL_FARE_REFUND',
                       'CARPOOL_FARE_EARNING'
                )
            );


DROP INDEX uk_eco_transactions_reward;

CREATE UNIQUE INDEX uk_eco_transactions_idempotent
    ON eco_transactions(user_id, source, reference_id)
    WHERE reference_id IS NOT NULL
      AND source IN (
          'TRIP_COMPLETED',
          'WASTE_DEPOSIT',
          'ADMIN_ADJUSTMENT',
          'CARPOOL_FARE_EARNING'
      );


DROP INDEX idx_users_leaderboard;

CREATE INDEX idx_users_leaderboard
    ON users(
             esg_rating DESC,
             total_co2_saved DESC,
             id ASC
        )
    WHERE role IN ('STUDENT', 'EMPLOYEE');