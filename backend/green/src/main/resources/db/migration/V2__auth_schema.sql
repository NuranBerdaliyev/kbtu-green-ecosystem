ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(72) NOT NULL DEFAULT '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7qV8lRW0R2y6YfJOLLm1Aun1vDLte';

ALTER TABLE users
    ADD CONSTRAINT chk_users_password_hash_not_blank CHECK (btrim(password_hash) <> '');

CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                token VARCHAR(512) NOT NULL,
                                user_id BIGINT NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL,
                                created_at TIMESTAMP NOT NULL,

                                CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
                                CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                CONSTRAINT chk_refresh_tokens_token_not_blank CHECK (btrim(token) <> '')
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);