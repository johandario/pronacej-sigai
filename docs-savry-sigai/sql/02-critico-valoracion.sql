-- SAVRY SIGAI: ítem crítico + valoración final
-- BD: dbpronacejqa2

BEGIN;

ALTER TABLE enc_contestacion
  ADD COLUMN IF NOT EXISTS critico boolean DEFAULT false;

COMMENT ON COLUMN enc_contestacion.critico IS
  'Ítem crítico SAVRY (factor crítico por pregunta)';

ALTER TABLE enc_encabezado
  ADD COLUMN IF NOT EXISTS id_valoracion_final bigint;

ALTER TABLE enc_encabezado
  ADD COLUMN IF NOT EXISTS justificacion_valoracion text;

ALTER TABLE enc_encabezado
  ADD COLUMN IF NOT EXISTS fecha_valoracion timestamp;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'fk_enc_encabezado_valoracion_final'
  ) THEN
    ALTER TABLE enc_encabezado
      ADD CONSTRAINT fk_enc_encabezado_valoracion_final
      FOREIGN KEY (id_valoracion_final)
      REFERENCES par_catalogo (id_catalogo);
  END IF;
END $$;

COMMENT ON COLUMN enc_encabezado.id_valoracion_final IS
  'Nivel de riesgo final SAVRY (catálogo NIVEL_RIESGO)';
COMMENT ON COLUMN enc_encabezado.justificacion_valoracion IS
  'Justificación de la valoración final SAVRY';
COMMENT ON COLUMN enc_encabezado.fecha_valoracion IS
  'Fecha/hora de la valoración final SAVRY';

COMMIT;

-- Verificación
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'enc_contestacion' AND column_name = 'critico';

SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'enc_encabezado'
  AND column_name IN ('id_valoracion_final', 'justificacion_valoracion', 'fecha_valoracion')
ORDER BY 1;

SELECT id_catalogo, nombre, nemonico, removido
FROM par_catalogo
WHERE nemonico ILIKE '%NIVEL_RIESGO%'
   OR id_catalogo_padre IN (
        SELECT id_catalogo FROM par_catalogo WHERE nemonico = 'NIVEL_RIESGO'
      )
ORDER BY id_catalogo;
