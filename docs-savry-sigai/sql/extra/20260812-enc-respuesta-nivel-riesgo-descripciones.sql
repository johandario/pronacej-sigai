-- =============================================================================
-- Migración: textos largos Bajo/Medio/Alto en opciones de nivel de riesgo
-- Origen: 42.14 (dbpronacejqa2) → aplicar en 42.50 y luego en producción
-- Fecha: 2026-08-12
--
-- Problema: el front muestra enc_respuesta.respuesta (innerHTML). Si solo queda
-- "Bajo"/"Moderado"/"Alto", no se ven las descripciones al costado del radio.
-- =============================================================================

BEGIN;

-- Pregunta 905 (ids 1645-1647)
UPDATE enc_respuesta
SET respuesta = 'Bajo — El adolescente no ha tenido situaciones de comportamiento violento, donde ha causado daño a otra persona',
    fecha_edicion = NOW()
WHERE id_respuesta = 1645
  AND id_pregunta = 905;

UPDATE enc_respuesta
SET respuesta = 'Medio — El adolescente tuvo algunas situaciones de comportamiento violento, donde ha causado daño a otra persona.',
    fecha_edicion = NOW()
WHERE id_respuesta = 1646
  AND id_pregunta = 905;

UPDATE enc_respuesta
SET respuesta = 'Alto — El adolescente tuvo diversas situaciones de comportamiento violento, donde ha causado daño a otra persona.',
    fecha_edicion = NOW()
WHERE id_respuesta = 1647
  AND id_pregunta = 905;

-- Pregunta 907 (ids 1651-1653) — segunda pregunta del mismo tipo
UPDATE enc_respuesta
SET respuesta = 'Bajo — El adolescente no ha tenido comportamiento violento grave que cause daño físico a otra persona, o el primer acto violento se realizó cuando tenía 14 años o más',
    fecha_edicion = NOW()
WHERE id_respuesta = 1651
  AND id_pregunta = 907;

UPDATE enc_respuesta
SET respuesta = 'Medio — Entre los 11 y 13 años de edad, el adolescente ha tenido algun  comportamiento violento  que causó daño físico a otra persona, pero nada de gravedad',
    fecha_edicion = NOW()
WHERE id_respuesta = 1652
  AND id_pregunta = 907;

UPDATE enc_respuesta
SET respuesta = 'Alto — Antes de los 11 años, el adolescente ha tenido comportamiento violento grave que causó daño físico a otra persona',
    fecha_edicion = NOW()
WHERE id_respuesta = 1653
  AND id_pregunta = 907;

COMMIT;

-- Verificación
SELECT id_respuesta, id_pregunta, orden, left(respuesta, 100) AS respuesta
FROM enc_respuesta
WHERE id_respuesta IN (1645, 1646, 1647, 1651, 1652, 1653)
ORDER BY id_respuesta;
