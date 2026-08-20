-- FK valoración final (idempotente). Aplicar en QA/Desarrollo si falta.
BEGIN;
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_enc_encabezado_valoracion_final'
  ) THEN
    ALTER TABLE enc_encabezado
      ADD CONSTRAINT fk_enc_encabezado_valoracion_final
      FOREIGN KEY (id_valoracion_final)
      REFERENCES par_catalogo (id_catalogo);
  END IF;
END $$;
COMMIT;
SELECT conname FROM pg_constraint WHERE conname = 'fk_enc_encabezado_valoracion_final';
