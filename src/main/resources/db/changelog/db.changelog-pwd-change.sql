-- Create the password_reset_token table
CREATE TABLE IF NOT EXISTS password_reset_token (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    token VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user_password_token FOREIGN KEY (user_id) REFERENCES _user(id) ON DELETE CASCADE
    );

-- Ensure email column exists in _user table (if not already present)
-- You likely already have this, but it's good to double-check.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = '_user'
          AND column_name = 'email'
    ) THEN
ALTER TABLE _user ADD COLUMN email VARCHAR(255);
-- Note: You might want to add a UNIQUE constraint to email if it's not there
-- ALTER TABLE _user ADD CONSTRAINT uk_user_email UNIQUE (email);
END IF;
END $$;