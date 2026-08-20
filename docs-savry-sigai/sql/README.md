# SQL SAVRY — orden de aplicación

Aplicar solo lo que falte. **Nunca** dump completo entre ambientes.

| # | Archivo | Uso |
|---|---------|-----|
| 01 | `01-align-savry-sigai.sql` | Maestro de opciones (30 ítems) |
| 02 | `02-critico-valoracion.sql` | Columnas crítico + valoración final |
| 03 | `03-ocultar-derivacion-fuentes.sql` | Ocultar sección Derivación/fuentes |
| 04 | `04-fk-valoracion-final.sql` | FK valoración final si falta |

Extra: `extra/20260812-enc-respuesta-nivel-riesgo-descripciones.sql` (textos nivel de riesgo).

Detalle: `../00-DOCUMENTACION-MAESTRA-SAVRY-SIGAI.md`
