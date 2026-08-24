ALTER TABLE trips
DROP CONSTRAINT chk_trips_status_values;

ALTER TABLE trips
    ADD CONSTRAINT chk_trips_status_values
        CHECK (
            trip_status IN (
                    'CREATED',
                    'PUBLISHED',
                    'IN_PROGRESS',
                    'COMPLETED',
                    'CANCELLED'
                )
            );

ALTER TABLE trips
DROP CONSTRAINT chk_trips_total_seats_positive;

ALTER TABLE trips
    ADD CONSTRAINT chk_trips_total_seats_range
        CHECK (total_seats BETWEEN 1 AND 8);

ALTER TABLE trips
    ADD CONSTRAINT chk_trips_departure_coordinates
        CHECK (
            ST_X(departure_location) BETWEEN -180 AND 180
                AND ST_Y(departure_location) BETWEEN -90 AND 90
            ),

    ADD CONSTRAINT chk_trips_destination_coordinates
        CHECK (
            ST_X(destination_location) BETWEEN -180 AND 180
            AND ST_Y(destination_location) BETWEEN -90 AND 90
        );