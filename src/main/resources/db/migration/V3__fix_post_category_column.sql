-- Rename 'categories' → 'category' to match the Post entity field name.
-- Wrapped in a conditional block so the migration is safe to re-run
-- (e.g. on a fresh DB where V2 already created the column as 'category').
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'post'
          AND column_name = 'categories'
    ) THEN
        ALTER TABLE post RENAME COLUMN categories TO category;
    END IF;
END $$;
