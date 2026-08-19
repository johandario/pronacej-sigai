package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSeguimientoEducativoLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.RecomendacionComentarioPorEvalSeguDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.EvaluacionSeguimientoEducativoLaboralService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacion-seguimiento-educativo-laboral")
@SecurityRequirement(name = "Authorization")
public class EvaluacionSeguimientoEducativoLaboralController {
    
    private EvaluacionSeguimientoEducativoLaboralService evaluacionSeguimientoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerEvaluacionesSeguimientoPaginado")
    @Operation(summary = "Obtiene las evaluaciones y seguimientos válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionesSeguimientoPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>> df = 
            this.evaluacionSeguimientoService.obtenerEvaluacionesSeguimiento(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_EVALUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerRecomendacionesComentariosPorEvaluacionSeguimiento")
    @Operation(summary = "Obtiene las recomendaciones y comentarios de una evaluación y seguimiento con paginación")
    public ResponseEntity<BodyEncriptado> obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(
        HttpServletRequest httpServletRequest,
        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO>> df = 
            this.evaluacionSeguimientoService.obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(
                httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
            bodyDesencriptado, 
            df, 
            fechaInicio, 
            EtiquetaNemonico.ACCION_OBTENER_EVALUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarEvaluacionSeguimiento")
    @Operation(summary = "Elimina una evaluación y seguimiento")
    public ResponseEntity<BodyEncriptado> eliminarEvaluacionSeguimiento(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
            this.evaluacionSeguimientoService.eliminarEvaluacionSeguimiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarRecomendacionComentario")
    @Operation(summary = "Elimina una recomendación/comentario de una evaluación y seguimiento")
    public ResponseEntity<BodyEncriptado> eliminarRecomendacionComentario(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
            this.evaluacionSeguimientoService.eliminarRecomendacionComentario(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
            bodyDesencriptado, 
            df, 
            fechaInicio, 
            EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_EDUCATIVA_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearEvaluacionSeguimiento")
    @Operation(summary = "Crea o edita una evaluación y seguimiento")
    public ResponseEntity<BodyEncriptado> crearEvaluacionSeguimiento(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        EvaluacionSeguimientoEducativoLaboralDTO evaluacionSeguimientoDTO = new Gson().fromJson(body, EvaluacionSeguimientoEducativoLaboralDTO.class);

        RespuestaPorDefectoAuditoria<EvaluacionSeguimientoEducativoLaboralDTO> df = 
            this.evaluacionSeguimientoService.crearEvaluacionSeguimiento(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición
        String accionAuditoria;
        if (evaluacionSeguimientoDTO.getEsEdicion() != null && evaluacionSeguimientoDTO.getEsEdicion()) {
            // Si es edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_EVALUACION_EDUCATIVA_LABORAL;
        } else {
            // Si es creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_EVALUACION_EDUCATIVA_LABORAL;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/subirDocumentos")
    @Operation(summary = "Sube uno o varios documentos")
    public ResponseEntity<BodyEncriptado> subirDocumentos(HttpServletRequest httpServletRequest,
                                                          @RequestParam("documentos") MultipartFile[] multipartFiles,
                                                          @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionSeguimientoService.subirDocumentos(httpServletRequest, bodyEncriptado, multipartFiles);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_DOCUMENTO_SUBIDA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados a la ficha y carpeta")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.evaluacionSeguimientoService.obtenerDocumentos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_DOCUMENTO_OBTENER);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
