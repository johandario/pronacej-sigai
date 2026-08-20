# SAVRY en SIGAI — Documentación maestra

| Campo | Valor |
|-------|--------|
| **Actualizado** | 2026-08-12 |
| **Código** | `/home/informatica08/SISTEMA-SIGAI` |
| **Deploy** | `/home/informatica08/sigai-deploy/` |
| **SQL / docs SAVRY** | `/home/informatica08/docs-savry-sigai/` |

Este es el **único documento vivo** del módulo SAVRY. El resto está en `archivo/` (histórico).

---

## 1. Ambientes (nombres fijos)

| IP | Nombre | Rol |
|----|--------|-----|
| **192.168.42.14** | **Desarrollo** (`sigaiqa`) | Aquí se desarrolla y prueba primero |
| **192.168.42.50** | **QA** (`srvfrontqa` / `srvapiqa`) | QA oficial; solo con promoción autorizada |
| 192.168.42.24 | siserv | Solo consulta (no modificar) |
| Prod (p. ej. 42.45 / 42.46) | Producción | Solo con orden explícita |

```
Desarrollo 42.14  →  QA 42.50  →  Producción
```

| Ambiente | Front (navegador) | API del front |
|----------|-------------------|---------------|
| Desarrollo | `https://sigaiqa.pronacej.gob.pe/` | `sigaiqa…:8443` (`npm run build`) |
| QA | `https://srvfrontqa.pronacej.gob.pe/` | `srvapiqa…:8443` (`npm run build:qa`) |
| Prod | según host Latinus | `srvapi…:8443` (`npm run build:prod`) |

---

## 2. Qué es SAVRY en SIGAI

No se portó el PHP de siserv. Se reutiliza la encuesta genérica:

| Concepto | Valor |
|----------|--------|
| Catálogo | `ENCUESTA_FACTORES_DE_RIESGO` |
| Encuesta activa | `id_encuesta = 59` |
| Motor | tablas `enc_*` + Angular + Java |
| BD | `dbpronacejqa2` (PostgreSQL `:5434`) |

### Entregado

1. Maestro de opciones alineado a siserv (30 ítems).
2. **Ítem crítico** por pregunta.
3. **Valoración final** (Bajo/Medio/Alto) + justificación + Revalorar.
4. **Resumen SAVRY** (Grupo / Bajo / Medio / Alto / Presente / Ausente / Críticos) en panel de valoración.
5. **PDF** *INFORME DE VALORACIÓN DE RIESGO* (secciones I–V).
6. **Upload de documentos** en valoración de riesgo (Alfresco).
7. UI: sin tabla antigua “Sección / Bajo — texto largo…”; opciones a **ancho completo**; sin paso **Derivación/fuentes**.

### Estado de promoción (2026-08-12)

| Ambiente | SAVRY funcional | Notas |
|----------|-----------------|--------|
| Desarrollo 42.14 | Sí | Origen de cambios |
| QA 42.50 | Sí | Promovido (front + back + SQL) |
| Producción | No | Pendiente de orden |

---

## 3. Base de datos (solo migraciones incrementales)

**Nunca** dump/restore de desarrollo/QA sobre producción.

| Script | Qué hace | Aplicado en |
|--------|----------|-------------|
| `sql/01-align-savry-sigai.sql` | Maestro opciones / 30 ítems | Desarrollo + QA |
| `sql/02-critico-valoracion.sql` | Columnas `critico`, valoración final | Desarrollo + QA |
| `sql/03-ocultar-derivacion-fuentes.sql` | `removido=true` sección Derivación (encuesta 59) | Desarrollo + QA |
| `sql/04-fk-valoracion-final.sql` | FK `fk_enc_encabezado_valoracion_final` si falta | QA (y Desarrollo ya la tenía) |

Catálogo relevante: `CARPETA_GESTION_ADOLES_NIVEL_RIESGO`, hijos de `NIVEL_RIESGO` (Bajo/Medio/Alto).

---

## 4. Flujo de usuario

```
Ficha adolescente
  → Valoración de nivel de riesgo
  → SAVRY (pasos = factores; sin Derivación/fuentes)
  → Contestar + ítems críticos
  → Finalizar → Valoración Final (resumen SAVRY + nivel + justificación)
  → Guardar / Revalorar / Imprimir PDF I–V
  → Documentos (Alfresco) si aplica
```

---

## 5. Publicar cambios

### En Desarrollo (42.14)

```bash
sudo /home/informatica08/sigai-deploy/scripts/build-and-deploy-local.sh front   # o back | all
```

Smoke: login en `https://sigaiqa.pronacej.gob.pe/` + función tocada.  
Network debe ir a `sigaiqa…:8443` (no `srvapiqa`).

### Desarrollo → QA (42.50) — patrón Latinus (~10 min)

Solo artefactos front/back. **No** BD dump, **No** Alfresco/Kong salvo SQL incremental aparte.

```bash
QA_SUDO_PASS='…' /home/informatica08/sigai-deploy/scripts/deploy-devqa-to-qa.sh --all
# o --frontend / --backend
```

- Front se recompila con `build:qa` (API `srvapiqa`).
- Back: imagen Docker; **no** se copia el `docker-compose` de Desarrollo.
- En QA: `urlFront=https://srvfrontqa.pronacej.gob.pe/` (links de correo/reset).
- Puerto backend QA: `9595:8080` + `SPRING_PROFILES_ACTIVE=qa`.

Guía operativa completa: `/home/informatica08/sigai-deploy/docs/01-GUIA-OPERATIVA-DESPLIEGUE.md`

### QA → Producción

Solo con orden explícita. Ver la misma guía (`deploy-qa-to-prod.sh`). BD prod no se toca en el pase normal.

---

## 6. Upload documentos / Alfresco

| Ambiente | Cómo llega el backend a Alfresco |
|----------|----------------------------------|
| Desarrollo 42.14 | Perfil `sigaiqa` / compose: `urlAlfresco=http://alfresco:8080/...` + red `docker-compose_default` (Traefik host a menudo roto). Archivos: `application-sigaiqa.yml`, `docker-compose.yml` del backend |
| QA 42.50 | `application-qa.yml`: `srvalfrescoqa…:8080` vía hostname/Traefik (no copiar compose de .14) |

El WAR crea la carpeta de evaluación si falta (`crearCarpetaEvaluacionSiNoExiste`).

---

## 7. Checklist de prueba

**Desarrollo y/o QA**

- [ ] Login
- [ ] Nivel de riesgo → SAVRY **sin** paso Derivación/fuentes
- [ ] Opciones Bajo/Medio/Alto usan todo el ancho
- [ ] No aparece tabla antigua “Sección / Bajo — …”
- [ ] Ítems críticos + valoración final + resumen SAVRY
- [ ] Revalorar
- [ ] PDF I–V (edad, evaluador, fecha sin hora)
- [ ] Subir documento en valoración
- [ ] API correcta: `sigaiqa` en Desarrollo / `srvapiqa` en QA

---

## 8. Código tocado (referencia)

**Front:** `evaluacion.component.*`, `savryResumen.utils.ts`, `encuestaPdf.service.ts`, `environment.ts` / `.qa.ts` / `.prod.ts`, upload docs (`evaluacion-documento`, `popup-documentos`).

**Back:** `EncuestaServiceImpl` (crítico, valoración, carpetas Alfresco), `EncuestaController`, DTOs, `application-qa.yml` (CORS).

**Deploy:** `build-and-deploy-local.sh`, `deploy-devqa-to-qa.sh`, `deploy-qa-to-prod.sh`, `setup-ssh-to-qa.sh`.

---

## 9. Índice de esta carpeta

| Ruta | Uso |
|------|-----|
| **`00-DOCUMENTACION-MAESTRA-SAVRY-SIGAI.md`** | Este archivo (leer primero) |
| **`sql/`** | Scripts SQL numerados |
| `archivo/` | Documentos históricos (no actualizar) |
| `referencia/` | Spec siserv / PDF ejemplo (consulta) |

---

*Fuente de verdad SAVRY — prevalece sobre cualquier doc en `archivo/`.*
