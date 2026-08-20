-- Ocultar sección "Derivación/fuentes de información" del SAVRY activo (encuesta 59)
-- Ambiente: aplicar primero en .14; luego en .50 con el mismo script.
-- No borra datos: solo removido=true (el API ya filtra removido=false).

BEGIN;

UPDATE enc_seccion
SET removido = true
WHERE id_seccion = 158
  AND id_encuesta = 59
  AND nombre ILIKE 'Derivación/fuentes de información';

UPDATE enc_pregunta
SET removido = true
WHERE id_seccion = 158
  AND COALESCE(removido, false) = false;

COMMIT;

-- Verificación
SELECT s.orden, s.id_seccion, s.nombre, s.removido,
  (SELECT COUNT(*) FROM enc_pregunta p
   WHERE p.id_seccion = s.id_seccion AND COALESCE(p.removido, false) = false) AS preguntas_activas
FROM enc_seccion s
WHERE s.id_encuesta = 59
ORDER BY s.orden, s.id_seccion;
