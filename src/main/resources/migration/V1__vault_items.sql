-- Tabelle analog Backend (owner_id als UUID, *_enc NOT NULL DEFAULT '', Timestamps, Indizes)
CREATE TABLE IF NOT EXISTS vault_items (
    id            BIGSERIAL PRIMARY KEY,
    owner_id      UUID          NOT NULL,
    title_enc     VARCHAR(1024) NOT NULL DEFAULT '',
    username_enc  VARCHAR(1024) NOT NULL DEFAULT '',
    password_enc  VARCHAR(2048) NOT NULL DEFAULT '',
    url_enc       VARCHAR(1024) NOT NULL DEFAULT '',
    notes_enc     TEXT          NOT NULL DEFAULT '',
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_vault_owner      ON vault_items(owner_id);
CREATE INDEX IF NOT EXISTS ix_vault_created_at ON vault_items(created_at);

-- Optionaler FK, nur wenn es 'users' mit id::uuid gibt
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'users'
  ) THEN
    BEGIN
      ALTER TABLE vault_items
        ADD CONSTRAINT fk_vaultitem_owner
        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
    EXCEPTION WHEN duplicate_object THEN NULL;
    END;
  END IF;
END $$;

-- Timestamps + Null-Schutz wie im Backend-Entity
CREATE OR REPLACE FUNCTION set_vault_item_timestamps() RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    NEW.created_at := COALESCE(NEW.created_at, NOW());
    NEW.updated_at := COALESCE(NEW.updated_at, NEW.created_at);
    NEW.title_enc    := COALESCE(NEW.title_enc,    '');
    NEW.username_enc := COALESCE(NEW.username_enc, '');
    NEW.password_enc := COALESCE(NEW.password_enc, '');
    NEW.url_enc      := COALESCE(NEW.url_enc,      '');
    NEW.notes_enc    := COALESCE(NEW.notes_enc,    '');
  ELSIF TG_OP = 'UPDATE' THEN
    NEW.created_at := OLD.created_at;
    NEW.updated_at := NOW();
    NEW.title_enc    := COALESCE(NEW.title_enc,    '');
    NEW.username_enc := COALESCE(NEW.username_enc, '');
    NEW.password_enc := COALESCE(NEW.password_enc, '');
    NEW.url_enc      := COALESCE(NEW.url_enc,      '');
    NEW.notes_enc    := COALESCE(NEW.notes_enc,    '');
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_vault_items_set_timestamps_ins ON vault_items;
DROP TRIGGER IF EXISTS trg_vault_items_set_timestamps_upd ON vault_items;

CREATE TRIGGER trg_vault_items_set_timestamps_ins
BEFORE INSERT ON vault_items
FOR EACH ROW EXECUTE FUNCTION set_vault_item_timestamps();

CREATE TRIGGER trg_vault_items_set_timestamps_upd
BEFORE UPDATE ON vault_items
FOR EACH ROW EXECUTE FUNCTION set_vault_item_timestamps();
