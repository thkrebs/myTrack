-- Prüfen, ob Spalte bereits existiert.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'authority'
          AND column_name = 'isrole'
    ) THEN
        -- Neue Spalte 'is_role' hinzufügen
ALTER TABLE authority
    ADD COLUMN isrole BOOLEAN NOT NULL DEFAULT FALSE;
END IF;
END $$;

-- Optional: Bestehende Daten mit Standardwerten aktualisieren (je nach Anforderung)
UPDATE authority
SET isrole = FALSE
WHERE isrole IS NULL;


-- Prüfen, ob die Spalte 'user_id' bereits in der Tabelle 'journey' existiert.
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'journey'
              AND column_name = 'userid'
        ) THEN
            -- Neue Spalte 'user_id' hinzufügen
            ALTER TABLE journey
                ADD COLUMN userid BIGINT;
        END IF;
    END $$;

UPDATE journey
SET userid = (SELECT id FROM _user LIMIT 1)
WHERE userid IS NULL;

-- Fremdschlüssel auf die Tabelle 'user' erstellen
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.constraint_column_usage
            WHERE table_name = 'journey'
              AND column_name = 'userid'
        ) THEN
            ALTER TABLE journey
                ADD CONSTRAINT fk_journey_user
                    FOREIGN KEY (userid) REFERENCES _user (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE;
        END IF;
    END $$;


-- Einschränkung 'NOT NULL' hinzufügen
ALTER TABLE journey
    ALTER COLUMN userid SET NOT NULL;

-- Spalte 'user_id' als NULL hinzugefügt
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'imei'
              AND column_name = 'userid'
        ) THEN
            -- Neue Spalte 'user_id' hinzufügen (NULL erlaubt)
            ALTER TABLE imei
                ADD COLUMN userid BIGINT;
        END IF;
    END $$;

-- Initialisierung: Weist bestehenden IMEI-Einträgen einen Standardbenutzer zu.
UPDATE imei
SET userid = (SELECT id FROM _user LIMIT 1)
WHERE userid IS NULL;

-- Einschränkung 'NOT NULL' auf 'user_id' setzen
ALTER TABLE imei
    ALTER COLUMN userid SET NOT NULL;

-- Fremdschlüssel hinzufügen
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.constraint_column_usage
            WHERE table_name = 'imei'
              AND column_name = 'userid'
        ) THEN
            ALTER TABLE imei
                ADD CONSTRAINT fk_imei_user
                    FOREIGN KEY (userid) REFERENCES _user (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE;
        END IF;
    END $$;


-- Sicherstellen, dass es einen Index für die Performance gibt
CREATE INDEX IF NOT EXISTS idx_imei_user_id ON imei (userid);
