package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionDomiciliariaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionDomiciliariaDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.EvaluacionDomiciliariaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.EvaluacionDomiciliariaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacion-domiciliaria")
@SecurityRequirement(name = "Authorization")
public class EvaluacionDomiciliariaController {
    
    private EvaluacionDomiciliariaService evaluacionDomiciliariaService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerEvaluacionesDomiciliariasPaginado")
    @Operation(summary = "Obtiene las evaluaciones domiciliarias válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerFichasIngresoPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionDomiciliariaDTO>> df = this.evaluacionDomiciliariaService.obtenerEvaluacionesDomiciliarias(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                 bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_EVALUACION_DOMICILIARIA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarEvaluacionDomiciliaria")
    @Operation(summary = "Elimina una evaluación domiciliaria")
    public ResponseEntity<BodyEncriptado> eliminarEvaluacionDomiciliaria(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionDomiciliariaService.eliminarEvaluacionDomiciliaria(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_DOMICILIARIA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearEvaluacionDomiciliaria")
    @Operation(summary = "Crea o edita una evaluación domiciliaria")
    public ResponseEntity<BodyEncriptado> crearEvaluacionDomiciliaria(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        EvaluacionDomiciliariaDTO evaluacionDomiciliariaDTO = new Gson().fromJson(body, EvaluacionDomiciliariaDTO.class);

        RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO> df = this.evaluacionDomiciliariaService.crearEvaluacionDomiciliaria(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición o creación
        String accionAuditoria;
        if (evaluacionDomiciliariaDTO.getEsEdicion() != null && evaluacionDomiciliariaDTO.getEsEdicion()) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_EVALUACION_DOMICILIARIA;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_EVALUACION_DOMICILIARIA;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    // Nuevos métodos para gestión de documentos
    
    @PostMapping("/subirDocumento")
    @Operation(summary = "Sube un documento y lo asocia al registro respectivo de evaluación domiciliaria")
    public ResponseEntity<BodyEncriptado> subirDocumento(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documento") MultipartFile multipartFile,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaRequest = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = this.evaluacionDomiciliariaService.subirDocumento(
                httpServletRequest, bodyEncriptado, multipartFile
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData(),
                df, fechaRequest, EtiquetaNemonico.ACCION_SUBIDA_DE_DOCUMENTOS_EVALUACION_DOMICILIARIA
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene todos los documentos asociados al registro de evaluación domiciliaria")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        EvaluacionDomiciliariaDocumentosRequest evaluacionDomiciliariaDocumentosRequest = new Gson().fromJson(
                bodyDesencriptado, EvaluacionDomiciliariaDocumentosRequest.class);
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = this.evaluacionDomiciliariaService.obtenerDocumentos(
                httpServletRequest, evaluacionDomiciliariaDocumentosRequest
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_DOCUMENTOS_EVALUACION_DOMICILIARIA
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/eliminarDocumento")
    @Operation(summary = "Eliminar documentos asociados a evaluación domiciliaria")
    public ResponseEntity<BodyEncriptado> eliminarDocumento(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        EvaluacionDomiciliariaDocumentoDTO evaluacionDomiciliariaDocumentoDTO = new Gson().fromJson(
                bodyDesencriptado, EvaluacionDomiciliariaDocumentoDTO.class);
                
        RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDocumentoDTO> df = this.evaluacionDomiciliariaService.eliminarRelacionConDocumento(
                httpServletRequest, evaluacionDomiciliariaDocumentoDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_ELIMINAR_DOCUMENTOS_EVALUACION_DOMICILIARIA
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}
