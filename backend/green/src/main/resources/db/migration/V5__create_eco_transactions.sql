CREATE TABLE eco_transactions (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL REFERENCES users(id),
                                  source VARCHAR(30) NOT NULL,
                                  reference_id BIGINT,
                                  eco_coins_delta BIGINT NOT NULL,
                                  co2_saved_delta NUMERIC(15,3),
                                  created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_eco_transactions_user ON eco_transactions(user_id);