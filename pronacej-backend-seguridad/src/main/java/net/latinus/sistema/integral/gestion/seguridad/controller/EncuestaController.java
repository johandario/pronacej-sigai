package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta.EncabezadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta.EncuestaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.general.EncuestaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/encuesta")
@SecurityRequirement(name = "Authorization")
public class EncuestaController {

    private EncuestaService encuestaService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerListaEncuestas")
    @Operation(summary = "Obtiene el listado de encuestas paginado")
    public ResponseEntity<BodyEncriptado> obtenerListaEncuestas(HttpServletRequest httpServletRequest,
                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncuestaDTO>> df = this.encuestaService.obtenerListaEncuestas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LISTA_ENCUESTA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEncuestas")
    @Operation(summary = "Obtiene el listado de encuestas")
    public ResponseEntity<BodyEncriptado> obtenerEncuestas(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<EncuestaDTO>> df = this.encuestaService.obtenerEncuestas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LISTA_ENCUESTA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEncuestaPorTokenEncuesta")
    @Operation(summary = "Obtiene la plantilla de la encuesta especifica")
    public ResponseEntity<BodyEncriptado> obtenerEncuestaPorTokenEncuesta(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<EncuestaDTO> df = this.encuestaService.obtenerEncuestaPorTokenEncuesta(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ENCUESTA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEvaluacionPorTokenEncabezado")
    @Operation(summary = "Obtiene la evaluacion del encabezado especifico")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionPorTokenEncabezado(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<EncuestaDTO> df = this.encuestaService.obtenerEvaluacionPorTokenEncabezado(httpServletRequest, bodyEncriptado);

        String accionAuditoria = this.determinarAccionAuditoriaPorMenu(
            httpServletRequest.getHeader("nemonicoMenu"), 
            "IMPRIMIR"
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEvaluacionesPorFichaIdentificacion")
    @Operation(summary = "Obtiene las evaluaciones realizadas de un adolescente en especifico")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionesPorFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                                                    @RequestBody BodyEncriptado bodyEncriptado, @RequestParam String nemonicoCategoria) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // ✅ MEJORA: Detectar si es descarga de Excel basado en el tamaño de paginación
        PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDesencriptado, PaginacionRequest.class);
        boolean esDescargaExcel = paginacionRequest.getSize() != null && paginacionRequest.getSize() >= 100000;

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> df = this.encuestaService.obtenerEvaluacionesPorFichaIdentificacion(httpServletRequest, bodyEncriptado, nemonicoCategoria);

        // ✅ MEJORA: Usar acción diferente para descarga de Excel
        String tipoAccion = esDescargaExcel ? "DESCARGAR_EXCEL" : "OBTENER";
        String accionAuditoria = this.determinarAccionAuditoriaPorMenu(
            httpServletRequest.getHeader("nemonicoMenu"), 
            tipoAccion
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
            bodyDesencriptado, df, fechaInicio, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEvaluacionesPorNemonicoEncuesta")
    @Operation(summary = "Obtiene las evaluaciones realizadas de una encuesta y adolescente en especifico")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionesPorNemonicoEncuesta(HttpServletRequest httpServletRequest,
                                                                                 @RequestBody BodyEncriptado bodyEncriptado, @RequestParam String nemonicoEncuesta) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> df = this.encuestaService.obtenerEvaluacionesPorNemonicoEncuesta(httpServletRequest, bodyEncriptado, nemonicoEncuesta);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LISTA_ENCUESTA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEvaluacionesPorNemonicoCategoria")
    @Operation(summary = "Obtiene las evaluaciones realizadas de una categoria(s) y adolescente en especifico")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionesPorNemonicoCategoria(HttpServletRequest httpServletRequest,
                                                                                  @RequestBody BodyEncriptado bodyEncriptado, @RequestParam String nemonicoCentro, @RequestParam List<String> nemonicosCategoria) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> df = this.encuestaService.obtenerEvaluacionesPorNemonicoCategoria(httpServletRequest, bodyEncriptado, nemonicoCentro, nemonicosCategoria);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_LISTA_ENCUESTA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearEncuesta")
    @Operation(summary = "Crea o edita una encuesta")
    public ResponseEntity<BodyEncriptado> crearEncuesta(HttpServletRequest httpServletRequest,
                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        EncuestaDTO encuestaDTO = new Gson().fromJson(body, EncuestaDTO.class);

        RespuestaPorDefectoAuditoria<EncuestaDTO> df = this.encuestaService.crearEncuesta(httpServletRequest,
                new Gson().fromJson(body, EncuestaDTO.class));

        // ✅ MEJORA: Lógica mejorada para determinar crear vs editar
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si es edición basado en múltiples criterios
        if (encuestaDTO.getIdEncuesta() > 0) {
            esEdicion = true;
        } else if (encuestaDTO.getTokenIdentificador() != null && 
                   !encuestaDTO.getTokenIdentificador().isEmpty() && 
                   !encuestaDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_ENCUESTA;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_ENCUESTA;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearEvaluacion")
    @Operation(summary = "Crea o edita una evaluación de encuesta")
    public ResponseEntity<BodyEncriptado> crearEvaluacion(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        EncabezadoDTO encabezadoDTO = new Gson().fromJson(body, EncabezadoDTO.class);

        RespuestaPorDefectoAuditoria<Boolean> df = this.encuestaService.crearEvaluacion(httpServletRequest, bodyEncriptado);

        // ✅ MEJORA: Lógica mejorada para determinar crear vs editar evaluación
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si es edición basado en el token identificador
        if (encabezadoDTO.getTokenIdentificador() != null && 
            !encabezadoDTO.getTokenIdentificador().trim().isEmpty() &&
            !encabezadoDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = this.determinarAccionAuditoriaPorMenu(httpServletRequest.getHeader("nemonicoMenu"), "EDITAR");
        } else {
            accionAuditoria = this.determinarAccionAuditoriaPorMenu(httpServletRequest.getHeader("nemonicoMenu"), "CREAR");
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarEncuesta")
    @Operation(summary = "Actualiza una encuesta")
    public ResponseEntity<BodyEncriptado> actualizarEncuesta(HttpServletRequest httpServletRequest,
                                                             @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.encuestaService.actualizarEncuesta(httpServletRequest, bodyEncriptado);

        // ✅ Este endpoint es específicamente para editar, así que siempre usa EDITAR
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_ENCUESTA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/removerEncuesta")
    @Operation(summary = "Remueve una encuesta del sistema")
    public ResponseEntity<BodyEncriptado> removerEncuesta(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.encuestaService.removerEncuesta(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_ENCUESTA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/removerEvaluacion")
    @Operation(summary = "Remueve una evaluacion del sistema")
    public ResponseEntity<BodyEncriptado> removerEvaluacion(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.encuestaService.removerEvaluacion(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
            fechaRequest, this.determinarAccionAuditoriaPorMenu(httpServletRequest.getHeader("nemonicoMenu"), "ELIMINAR"));

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerPreguntas")
    @Operation(summary = "Obtiene las preguntas de la encuesta solicitada")
    public ResponseEntity<BodyEncriptado> obtenerPreguntas(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        return ResponseEntity.ok(null);
    }

    @PostMapping("/subirDocumento")
    @Operation(summary = "Sube un documento")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documentos") MultipartFile[] multipartFiles,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        String body = null;

        try {
            BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
            body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

            df = this.encuestaService.subirDocumento(httpServletRequest, multipartFiles, bodyEncriptado);

            this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, this.determinarAccionAuditoriaPorMenu(httpServletRequest.getHeader("nemonicoMenu"), "SUBIR_DOCUMENTO"));
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados a la ficha y carpeta")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.encuestaService.obtenerDocumentos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
            fechaRequest, this.determinarAccionAuditoriaPorMenu(httpServletRequest.getHeader("nemonicoMenu"), "OBTENER_DOCUMENTOS"));

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    /**
     * ✅ MEJORA: Método centralizado para determinar acciones de auditoría basadas en el menú
     * Determina la acción de auditoría basada en el nemónico del menú - VERSIÓN MEJORADA
     */
    private String determinarAccionAuditoriaPorMenu(String nemonicoMenu, String tipoAccion) {
        // Mapeo de nemónicos de menú a tipos de evaluación
        switch (nemonicoMenu) {
            case "MENU_EVALUACION_PSICOLOGICA_FORMULARIO":
                switch (tipoAccion) {
                    case "OBTENER": return EtiquetaNemonico.ACCION_OBTENER_EVALUACION_PSICOLOGICA;
                    case "CREAR": return EtiquetaNemonico.ACCION_CREAR_EVALUACION_PSICOLOGICA;
                    case "EDITAR": return EtiquetaNemonico.ACCION_EDITAR_EVALUACION_PSICOLOGICA;
                    case "ELIMINAR": return EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_PSICOLOGICA;
                    case "IMPRIMIR": return EtiquetaNemonico.ACCION_IMPRIMIR_EVALUACION_PSICOLOGICA;
                    case "SUBIR_DOCUMENTO": return EtiquetaNemonico.ACCION_SUBIR_DOCUMENTO_EVALUACION_PSICOLOGICA;
                    case "OBTENER_DOCUMENTOS": return EtiquetaNemonico.ACCION_VER_DOCUMENTOS_EVALUACION_PSICOLOGICA;
                    case "REALIZAR_SEGUIMIENTO": return EtiquetaNemonico.ACCION_REALIZAR_SEGUIMIENTO_EVALUACION_PSICOLOGICA;
                    default: return EtiquetaNemonico.ACCION_OBTENER_EVALUACION_PSICOLOGICA;
                }

            case "MENU_PRUEBAS_PSICOLOGICAS_FORMULARIO":
                switch (tipoAccion) {
                    case "OBTENER": return EtiquetaNemonico.ACCION_OBTENER_PRUEBAS_PSICOLOGICAS;
                    case "CREAR": return EtiquetaNemonico.ACCION_CREAR_PRUEBAS_PSICOLOGICAS;
                    case "EDITAR": return EtiquetaNemonico.ACCION_EDITAR_PRUEBAS_PSICOLOGICAS;
                    case "ELIMINAR": return EtiquetaNemonico.ACCION_ELIMINAR_PRUEBAS_PSICOLOGICAS;
                    case "IMPRIMIR": return EtiquetaNemonico.ACCION_IMPRIMIR_PRUEBAS_PSICOLOGICAS;
                    case "SUBIR_DOCUMENTO": return EtiquetaNemonico.ACCION_SUBIR_DOCUMENTO_PRUEBAS_PSICOLOGICAS;
                    case "OBTENER_DOCUMENTOS": return EtiquetaNemonico.ACCION_VER_DOCUMENTOS_PRUEBAS_PSICOLOGICAS;
                    default: return EtiquetaNemonico.ACCION_OBTENER_PRUEBAS_PSICOLOGICAS;
                }

            case "MENU_NIVEL_RIESGO":
                switch (tipoAccion) {
                    case "OBTENER": return EtiquetaNemonico.ACCION_OBTENER_EVALUACION_NIVEL_RIESGO;
                    case "CREAR": return EtiquetaNemonico.ACCION_CREAR_EVALUACION_NIVEL_RIESGO;
                    case "EDITAR": return EtiquetaNemonico.ACCION_EDITAR_EVALUACION_NIVEL_RIESGO;
                    case "ELIMINAR": return EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_NIVEL_RIESGO;
                    case "IMPRIMIR": return EtiquetaNemonico.ACCION_IMPRIMIR_EVALUACION_NIVEL_RIESGO;
                    case "SUBIR_DOCUMENTO": return EtiquetaNemonico.ACCION_SUBIR_DOCUMENTO_EVALUACION_NIVEL_RIESGO;
                    case "OBTENER_DOCUMENTOS": return EtiquetaNemonico.ACCION_OBTENER_DOCUMENTOS_EVALUACION_NIVEL_RIESGO;
                    case "DESCARGAR_EXCEL": return EtiquetaNemonico.ACCION_DESCARGAR_EXCEL_EVALUACION_NIVEL_RIESGO;
                    default: return EtiquetaNemonico.ACCION_OBTENER_EVALUACION_NIVEL_RIESGO;
                }

            case "MENU_EVALUACION_CONDUCTUAL_FORMULARIO":
                switch (tipoAccion) {
                    case "OBTENER": return EtiquetaNemonico.ACCION_OBTENER_EVALUACION_CONDUCTUAL;
                    case "CREAR": return EtiquetaNemonico.ACCION_CREAR_EVALUACION_CONDUCTUAL;
                    case "EDITAR": return EtiquetaNemonico.ACCION_EDITAR_EVALUACION_CONDUCTUAL;
                    case "ELIMINAR": return EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_CONDUCTUAL;
                    case "SUBIR_DOCUMENTO": return EtiquetaNemonico.ACCION_SUBIR_EVALUACION_DOCUMENTO;
                    case "OBTENER_DOCUMENTOS": return EtiquetaNemonico.ACCION_OBTENER_DOCUMENTOS_EVALUACION;
                    default: return EtiquetaNemonico.ACCION_OBTENER_EVALUACION_CONDUCTUAL;
                }

            default:
                // Fallback para otros menús - usar acciones genéricas de evaluación
                switch (tipoAccion) {
                    case "OBTENER": return EtiquetaNemonico.ACCION_OBTENER_EVALUACION;
                    case "CREAR": return EtiquetaNemonico.ACCION_CREAR_EVALUACION;
                    case "EDITAR": return EtiquetaNemonico.ACCION_EDITAR_EVALUACION;
                    case "ELIMINAR": return EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION;
                    case "SUBIR_DOCUMENTO": return EtiquetaNemonico.ACCION_SUBIR_EVALUACION_DOCUMENTO;
                    case "OBTENER_DOCUMENTOS": return EtiquetaNemonico.ACCION_OBTENER_DOCUMENTOS_EVALUACION;
                    default: return EtiquetaNemonico.ACCION_OBTENER_EVALUACION;
                }
        }
    }
}