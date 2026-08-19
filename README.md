# PRONACEJ (Programa Nacional de Centros Juveniles)
Sistema Integral de Gestión del Adolescente Infractor

## Notas de la versión, Backend: 1.10.4

### Corrección de Errores

- **Error de cabecera nula en impresión** Se corrigió el error que aparecía al imprimir cualquier encuesta.

## Notas de la versión, Frontend: 1.10.5

### Corrección de Errores

- **Acción exportar lista de Estudios y Trabajo no funcionaba** Se corrigió el funcionamiento interno de la acción exportar que generaba un error que no se mostraba y no permitía la exportación. 

## Notas de la versión, Frontend: 1.10.4, Backend: 1.10.3

### Corrección de Errores

- **Permiso de exclusión de jornada no funciona en ficha de ingreso** Al terminar la jornada laboral, el sistema bloquea correctamente la edición de la ficha de ingreso del adolescente. Sin embargo, cuando se otorga el permiso de exclusión de jornada, el usuario seguía sin poder editar la ficha. Se corrigió la validación del permiso de exclusión para que permita la edición fuera de jornada cuando corresponda.

- **Flujo fuga: error al grabar borrador por fecha null** Al guardar como borrador un proceso en el flujo de fuga, el sistema mostraba un mensaje de "en mantenimiento" debido a que el campo de fecha se almacenaba como `null`. Se garantiza que esa fecha no intervenga en el guardado inicial de la fuga.

### Mejoras

- **Reubicar botones de acción en Ficha Principal** Los botones de acción del formulario se reubicaron al final, después de la sección "Trabajo Laboral", para mantener el flujo natural de navegación y registro de información. La sección "Datos Generales" ahora permite expandir y contraer, de forma consistente con las demás secciones del formulario.

- **Reubicar columna Fecha/Hora de Registro en listados** En las secciones "Estudios" y "Trabajo Laboral" de la Ficha Principal, la columna "Fecha/Hora de Registro" se reposicionó inmediatamente después de la columna "Acciones", alineándose con el estándar definido para los listados del sistema.

- **Aplicar color primario al botón "Volver" en Ficha Principal/Ubicación** Se actualizó el estilo del botón "Volver" en la vista de ubicación de la Ficha Principal para utilizar el color primario del sistema, manteniendo la consistencia visual.

## Notas de la versión, Frontend: 1.10.3, Backend: 1.10.2

### Corrección de errores
- En exportación de csv: cambio de nombre de columna en desuso.
- Flujos traslado/fuga: cambio de estado y validación de ingreso.

## Notas de la versión, Frontend: 1.10.2, Backend: 1.10.1

### Cambios generales
- Mostrar/Ocultar secciones en Expediente Matriz
Se permite configurar la visibilidad de las secciones Historia Clínica y Evaluación de Salud dentro del Expediente Matriz.

- Ubicaciones jerárquicas — Estilos visuales
Se aplican colores primarios a los tipos de ubicación y a los puntos de acción en el módulo de Configuración – Ubicaciones Jerárquicas.

- Expediente Matriz / Ubicaciones
Se agregan al listado principal y al Excel exportable todas las columnas del formulario de ubicaciones (centro, celda, observaciones, etc.).

- Ficha Principal — Sección Estudios
Se incorpora la columna Fecha y Hora de Registro, se abrevia la columna de fecha a "F. Inicio Estudios" (sin hora) y se añade la acción Visualizar con permisos.

- Ficha Principal — Sección Trabajo Laboral
Se integra "Trabajo Laboral" como sección colapsable dentro de Ficha Principal (antes aparecía como opción independiente en el menú). Se agrega columna Fecha y Hora de Registro, se abrevia a "F. Ingreso laboral" y se añade acción Visualizar.

- Ficha Principal — Validaciones varias
Se integran "Estudio" y "Trabajo Laboral" como secciones colapsables dentro de Ficha Principal. Se aplican etiquetas en negrita, se reemplaza botón "Regresar" por "Cancelar", se corrigen checkboxes y se actualiza el PDF.

### Corrección de errores
Ubicaciones jerárquicas — Edición no recuperaba datos 
Se corrige el problema al editar una etapa, pabellón, piso o celda que no cargaba la información previamente ingresada.

- Dashboard — Porcentajes y totales incorrectos en Estudios/Convenios 
Se corrige el cálculo de porcentajes y totales en convenios. Se cambia gráfico a barras y se aplica filtro por rol/centro asignado (Super Rol ve todos los centros).

## Notas de la versión, Frontend: 1.10.1
- Corrección de mostrar/ocultar Historia Clínica y Evaluación de salud de acuerdo al rol.
- Corrección de mensaje de error duplicado en registro nuevo ingreso.

## Notas de la versión, Frontend: 1.10.0, Backend: 1.10.0

### Control de Cambios #6 — Resumen de cambios

### Gestión de Pabellones (Nuevo)
- Módulo CRUD jerárquico para la gestión de pabellones y subpabellones.
- Integración de la sección de pabellones en el Expediente Matriz.
- Incorporación de pabellones al sistema de permisos del administrador.

### Expediente Matriz
- Nueva sección de **Trabajo Laboral** con campos requeridos.
- Nueva sección de **Estudios**: listado, creación y visualización de registros.

### Pantalla de Inicio
- Query de consulta y controles en el dashboard principal.
- Query y controles del dashboard para la sección de estudios.

### Secciones en Expediente Matriz
- Habilitación de todas las secciones sin condicionales, respetando lo que se muestra por centro.

## Notas de la versión, Frontend: 1.9.2

### Correcciones de Errores

**Ficha de Salud**
- Validadores en controles autocomplete
- Evitar que modales se cierren al hacer clic afuera
- Cambio de texto en etiquetas de ciertos controles

## Notas de la versión, Frontend: 1.9.1, Backend: 1.9.1

### Correcciones de Errores

**Ficha de Salud**
- Antecedentes personales — Se corrigió que no se reflejaba el tipo de sangre del adolescente.

**Esquema Corporal Ectoscópico**
- Al editar, ahora se reflejan correctamente los campos: tipo de evaluación, peso, talla, IMC y clasificación IMC.

**Historia Clínica**
- Sección de subir documentos — Se corrigió que el archivo cargado no se visualizaba de forma inmediata; ya no es necesario refrescar toda la pantalla.

**Consulta Médica**
- Al editar una consulta, ahora se cargan correctamente los datos del grid de orden de exámenes. Se renombró el grid a "Orden Médica".
- Se validó que el pop-up de la orden médica no se cierre al hacer clic fuera de él.

**Receta**
- Se validó que el pop-up de receta no se cierre al hacer clic fuera de él. Además, se corrigió que al descargar la receta se generaba con un nombre incorrecto.

**Evaluación de Salud**
- Se corrigió que no se mostraba la enfermedad añadida en los antecedentes familiares desde la historia clínica.

**Reportes**
- Información de adolescente — Se corrigió que al generar el CSV (todos los adolescentes o uno específico) no se obtenía ninguna información.

**Dashboard Estadísticos (Módulo Inicio)**
- Se cambió la visualización a gráfico de barras para cantidad de adolescentes por nacionalidad y por departamento. Se añadió el título "Dashboard Estadístico", se aplicó color primario al botón actualizar y al filtro de centro, y se muestra el nombre del centro cuando aplique.

**Ficha Psicosocial**
- Persona relacionada — Se añadieron en el PDF de la sección los campos del grid: edad, fecha de nacimiento.

**Registro de Ingreso**
- Se eliminó el mensaje de validación que excluía a personas menores de 14 años durante el registro, permitiendo ahora el ingreso de datos manuales.


## Notas de la versión, Frontend: 1.9.0, Backend: 1.9.0
 
### 1. Módulo Historia Clínica

- **Habilitación automática de Historia Clínica al ingreso** — Cuando un adolescente ingresa al CJDR y pasa por Puerta de Seguridad, el sistema habilita automáticamente el acceso a la Historia Clínica para el personal médico, respetando los permisos de cada rol. 

- **Nueva pestaña de Antecedentes Familiares** — Se reubicó la sección de Antecedentes Familiares a una pestaña independiente dentro de Historia Clínica para evitar confusiones con otras secciones.

- **Selección de parentesco en Antecedentes Familiares** — Se reemplazó el campo "persona relacionada" por un campo de selección de parentesco que toma automáticamente la información de la Ficha Psicosocial (Composición Familiar), con opción de agregar nuevos parentescos manualmente.

- **Integración de CIE-10 en Antecedentes Familiares** — Se incorporó la base de datos del CIE-10 (~catálogo de diagnósticos) con dos campos autocomplete que permiten buscar por código o nombre, mostrando automáticamente la descripción completa del diagnóstico. 

- **Integración de CIE-10 en Antecedentes Personales** — Se reutilizó la información de CIE-10 en la sección de Antecedentes Personales, reemplazando el campo "tipo de enfermedad" por campos autocomplete vinculados al catálogo CIE-10.

- **Nueva sección Anamnesis y reorganización de Ficha de Salud** — Se añadió la sección de Anamnesis en la Ficha de Salud y se reorganizaron las secciones (Anamnesis, Examen Físico General, Examen Físico Regional, Impresión Diagnóstica). Se eliminó el ítem "Inspección" del examen físico regional. 

- **Campo Alergia a alimentos** — Se añadió un nuevo campo para registrar alergias a alimentos en Antecedentes Personales. 

- **Pestaña de Subida de Documentos** — Se creó una nueva pestaña en Historia Clínica que permite la carga y gestión de documentos adjuntos con toda la lógica asociada. 

- **Ajustes en Esquema Corporal Ectoscópico** — Se quitó la obligatoriedad de los campos "Estado" y "Grado de desnutrición" en Estado Nutricional, se ocultan de la impresión cuando están vacíos y se implementó formato de impresión.

- **Campo cicatrices ahora opcional** — Se eliminó la obligatoriedad del campo de cicatrices en Esquema Corporal Ectoscópico, permitiendo registrar adolescentes sin cicatrices.

### 2. Consulta Médica

- **Sección Orden Médica** — Se creó una nueva sección de Orden Médica dentro de Consulta Médica con datos de Especialidad y Productos (~1,300 registros cargados en BD), generando una orden imprimible con fecha, datos del paciente, y productos solicitados. 

- **Sección Receta con base de datos de Medicamentos** — Se integró un catálogo de medicamentos con campos autocomplete de solo lectura que incluyen tipo de administración (oral, inyección, crema) y duración del tratamiento.

### 3. Ficha Psicosocial

- **Ajustes en Composición Familiar** — Se quitó la obligatoriedad del campo "fecha de nacimiento", se añadió un campo autocalculado de edad, y se excluyen de la impresión los campos vacíos.

### 4. Categorización y Métricas de Salud

- **Clasificación automática de IMC** — Se añadió un campo de categoría de IMC que clasifica automáticamente según la tabla estándar, implementado en Esquema Corporal, Plan de Atención y Consulta Médica.

### 5. Permisos y Seguridad

- **Módulo Historia Clínica en sistema de permisos** — Se incorporó Historia Clínica al módulo de permisos del sistema, permitiendo controlar la visualización de todas las secciones por rol de usuario.

### 6. Registro e Integración con Servicios Externos

- **Integración con RENIEC** — Se implementó la consulta al servicio de RENIEC al registrar usuarios, completando automáticamente datos personales con la información del servicio externo.

- **Registro de menores de 14 años** — Se eliminó la validación que impedía registrar personas menores de 14 años, permitiendo ahora el ingreso manual de datos.

### 7. Exportación de Datos y Dashboard

- **Exportación masiva en CSV** — Se implementó la funcionalidad de exportar toda la información registrada de todos los adolescentes en formato CSV, disponible por cada sede (SOA y CJDR).

- **Dashboard interactivo** — Se creó una nueva pantalla tipo dashboard con información consolidada de todos los centros y SOAs, incluyendo filtros por tipo de delito, edad, nacionalidad, departamento, sexo, días de internamiento, tipo de enfermedad, grado de estudio, y adolescentes con/sin hijos.

### 8. Correcciones y Ajustes

- **Corrección de campo "esOficinaCentral"** — Se corrigió el comportamiento del campo para que solo aplique a la jerarquía "Unidad de Asistencia Técnica Post Egreso".
- **Corrección en Expedientes Legales** — Se corrigió la visualización del grid de documentos en la sección de disposiciones en modo visualizar.
- **Corrección de PDF en Ficha de Salud** — Se corrigió la generación del PDF para que muestre correctamente los datos del adolescente.
- **Corrección en PTI SOA** — Se corrigió el combo de modalidad en Servicios a la Comunidad para incluir las opciones: presencial, virtual y ambos. 
- **Corrección masiva de mandatos** — Se ejecutó un query correctivo masivo para registros de mandatos en expediente legal. 

## Notas de la versión, Frontend: 1.8.8, Backend: 1.8.5

- Se añaden 2 secciones en Expediente Matriz que redirigen a URL facilitada por PRONACEJ.

## Notas de la versión, Frontend: 1.8.7, Backend: 1.8.5

- Corrección de medidas accesorias que se cargaban en la misma lista que las medidas socioeducativas en detalle (mandatos) de expedientes legales.
- Habilitación de botón "Agregar mandato" para el caso de visualización del expediente legal incluso si no tiene permiso de edición.
- Corrección de paginador en lista de mandatos, no se mostraba correctamente el número de registros en el paginador.
- Bandera de adolescente tieneProceso no cambiaba de valor y no aparecía en registro de salida.

## Notas de la versión, Frontend: 1.8.6, Backend: 1.8.4

- Corrección de validación de estados de adolescente al realizar un registro de ingreso.

## Notas de la versión, Frontend: 1.8.5, Backend: 1.8.4

- Corrección de error al añadir un nuevo mandato legal en expedientes legales
- Modo de visualización en mandatos legales.

## Notas de la versión, Frontend: 1.8.4, Backend: 1.8.3

- Corrección de formato de fechas en lista de Pertenencias.
- Invalidar clic fuera del modal de búsqueda de adolescente en crear nueva ficha de identificación
- Correción de formato de fecha al exportar excel en módulo de permisos por menú
- Corrección al generar un permiso base por colaborador, no mostraba botón Agregar. 

## Notas de la versión, Frontend: 1.8.3, Backend: 1.8.2

- Corrección de formato de fechas en listas de Preparación para el egreso e Informes.
- Correcciones y modificaciones en Reportes de Adolescentes externados:
    - Corrección de búsqueda por filtro de centro.
    - Mostrar lista de todos los centros en cualquier centro (al inicio se consideró UAPISE únicamente).

## Notas de la versión, Frontend: 1.8.2, Backend: 1.8.1

- Se restauró la acción de "Visualizar" que no se reflejaba en los módulos de:
    Permisos de salida.
    Plan de Tratamiento Individual (PTI/SOA).
    Post egreso (Derivación a instituciones).

- Corregido el error que impedía mostrar el nombre del adolescente en los apartados de Informes y Notificaciones.
- Reporte de Externados: Se reparó la búsqueda; los filtros de consulta ya funcionan con todos los criterios de información.
- Se habilitó el botón "Agregar" en las evaluaciones (Social, Seguimiento educativo/laboral) para adolescentes pertenecientes a SOA.
- Corregido mensaje de confirmación al crear una Ficha de Ingreso nueva en SOA
- Módulo de Permisos: Los registros ahora se muestran en orden descendente.
- Se corrigió el desfase horario en la creación de permisos
- Se estandarizó el formato de fecha y hora en las columnas de "Fecha de creación" y "Fecha de sesión" del módulo de Preparación para el egreso.
- En el registro de salida, al seleccionar "Informe Final", el sistema ahora filtra y muestra exclusivamente aquellos que tengan estado Completado.

## Notas de la versión, Frontend: 1.8.1, Backend: 1.8.0

- Al crear un permiso de menú-acción por colaborador, se permite guardar el registro si la lista de roles se encuentra vacía, con esto se aplica el permiso a todos los roles del colaborador.

## Notas de la versión, Frontend: 1.8.0, Backend: 1.7.99

- Se corrige mensaje de validación al guardar borrador de ficha de ingreso, la validación queda para casos donde se intenta crear una ficha y el adolescente no ha sido externado.

## Notas de la versión, Frontend: 1.7.99, Backend: 1.7.98

- Se incluye tipo de asignación (rol/colaborador individual) y tipo de permiso (estándar/exclusión de jornada) en creación de permisos.
- Permisos sobre controles (editar/eliminar, visualizar se muestra por defecto) a nivel de ficha principal (expediente matriz).
- Permisos sobre control agregar en casos donde se recupera una ficha desde un centro ajeno al centro actual.
- Aquellos registros de fichas que pasen de la jornada (23h59), no podrán ser editados/eliminados a no ser que se configure una exclusión desde el módulo de Permisos (tipo de permiso exclusión de jornada).
- Nuevo reporte "Adolescentes Externados" que muestra información de adolescentes que han sido externados. Disponible en Reportes -> Adolescentes Externados.
- Nueva ventana de búsqueda de adolescentes en Ficha principal para recuperar fichas de adolescentes de otros centros. 

## Notas de la versión, Frontend: 1.7.98, Backend: 1.7.97

- Mensajes de info/advertencia al ingresar el número de identificación de un adolescente ya existente.
- Registros sólo de lectura en Ficha de identificación cuando el centro no es el actual.
- Registros históricos por ficha que registran movimiento por centros.
- Registros sólo de lectura en el caso de que sean datos históricos.
- Correción de errores en Acta de Externamiento y Notificaciones.

## Notas de la versión, Frontend: 1.7.97, Backend: 1.7.96

- Módulo de gestión de permisos por menú a nivel de Ficha de Identificación (Expediente Matriz).
- Gestión de permisos a nivel de controles (visualizar, editar, eliminar) en algunos listados del Expediente Matriz.

## Notas de la versión 1.7.0

Sistema Integrado de Gestión de Adolescentes Infractores - SIGAI

Esta versión contiene todas las funcionalidades del SIGAI, habiendo realizado en este realease:

- Cambios a observaciones realizadas por los usuarios finales
- Correcciones de bugs en diferentes funcionalidades conforme lo indicado en la bitácora de pruebas.



