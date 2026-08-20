ALTER TABLE eco_transactions
    ADD COLUMN esg_rating_delta INTEGER NOT NULL DEFAULT 0;

ALTER TABLE eco_transactions
    ADD CONSTRAINT chk_eco_transactions_source
        CHECK (
            source IN (
                       'TRIP_COMPLETED',
                       'WASTE_DEPOSIT',
                       'ADMIN_ADJUSTMENT'
                )
            );

CREATE UNIQUE INDEX uk_eco_transactions_reward
    ON eco_transactions(user_id, source, reference_id)
    WHERE reference_id IS NOT NULL;


CREATE TABLE user_achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_code VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_user_achievements_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_user_achievement
        UNIQUE (user_id, achievement_code),

    CONSTRAINT chk_achievement_code
        CHECK (
            achievement_code IN (
                'FIRST_ACTION',
                'FIRST_SHARED_TRIP',
                'CARPOOL_REGULAR',
                'FIRST_WASTE_DEPOSIT',
                'RECYCLING_REGULAR',
                'ECOCOINS_100',
                'ESG_70',
                'CO2_10_KG'
            )
        )
);

CREATE INDEX idx_user_achievements_user
    ON user_achievements(user_id);

CREATE INDEX idx_users_leaderboard
    ON users(
             esg_rating DESC,
             eco_coins_balance DESC,
             total_co2_saved DESC,
             id ASC
        )
    WHERE role IN ('STUDENT', 'EMPLOYEE');