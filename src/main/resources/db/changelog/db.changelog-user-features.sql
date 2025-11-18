-- Check if the 'features' column already exists in the '_user' table.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = '_user'
          AND column_name = 'features'
    ) THEN
        -- Add the new 'features' column with a default value of 0 and NOT NULL constraint.
        ALTER TABLE _user
            ADD COLUMN features BIGINT NOT NULL DEFAULT 0;
    END IF;
END $$;
