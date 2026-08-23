CREATE TABLE profiles (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          phone VARCHAR(20),
                          avatar_url VARCHAR(255),
                          bio VARCHAR(500),
                          birth_date DATE,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT uk_profiles_user UNIQUE (user_id),
                          CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_profiles_user_id ON profiles(user_id);

ALTER TABLE refresh_tokens RENAME TO authentications;

ALTER INDEX uk_refresh_tokens_token RENAME TO uk_authentications_token;
ALTER INDEX idx_refresh_tokens_user_id RENAME TO idx_authentications_user_id;

ALTER TABLE authentications RENAME CONSTRAINT fk_refresh_tokens_user TO fk_authentications_user;
ALTER TABLE authentications RENAME CONSTRAINT chk_refresh_tokens_token_not_blank TO chk_authentications_token_not_blank;