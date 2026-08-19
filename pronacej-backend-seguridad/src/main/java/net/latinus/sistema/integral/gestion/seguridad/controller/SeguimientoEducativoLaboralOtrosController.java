package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SeguimientoEducativoLaboralOtrosDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.SeguimientoEducativoLaboralOtrosService;
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
@RequestMapping(path = "api/v1/seguimiento-educativo-laboral-otros")
@SecurityRequirement(name = "Authorization")
public class SeguimientoEducativoLaboralOtrosController {
    
    private SeguimientoEducativoLaboralOtrosService seguimientoEducativoLaboralOtrosService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    /**
     * Obtiene los seguimientos educativos/laborales/otros válidos con paginación
     */
    @PostMapping("/obtenerSeguimientosPaginado")
    @Operation(summary = "Obtiene los seguimientos educativos/laborales/otros válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerSeguimientosPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO>> df = 
                this.seguimientoEducativoLaboralOtrosService.obtenerSeguimientosPaginado(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SEGUIMIENTO_EDUCATIVO_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    /**
     * Elimina un seguimiento educativo/laboral/otros
     */
    @PostMapping("/eliminarSeguimiento")
    @Operation(summary = "Elimina un seguimiento educativo/laboral/otros")
    public ResponseEntity<BodyEncriptado> eliminarSeguimiento(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
                this.seguimientoEducativoLaboralOtrosService.eliminarSeguimiento(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_SEGUIMIENTO_EDUCATIVO_LABORAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    /**
     * Crea o edita un seguimiento educativo/laboral/otros
     */
    @PostMapping("/crearSeguimiento")
    @Operation(summary = "Crea o edita un seguimiento educativo/laboral/otros")
    public ResponseEntity<BodyEncriptado> crearSeguimiento(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        SeguimientoEducativoLaboralOtrosDTO seguimientoEducativoLaboralOtrosDTO = 
                new Gson().fromJson(body, SeguimientoEducativoLaboralOtrosDTO.class);

        RespuestaPorDefectoAuditoria<SeguimientoEducativoLaboralOtrosDTO> df = 
                this.seguimientoEducativoLaboralOtrosService.crearSeguimiento(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si es edición o creación
        String accionAuditoria;
        boolean esEdicion = false;

        if (seguimientoEducativoLaboralOtrosDTO.getTokenIdentificador() != null && 
            !seguimientoEducativoLaboralOtrosDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_SEGUIMIENTO_EDUCATIVO_LABORAL;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_SEGUIMIENTO_EDUCATIVO_LABORAL;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    /**
     * Sube documentos asociados al seguimiento educativo/laboral/otros
     */
    @PostMapping("/subirDocumentos")
    @Operation(summary = "Sube documentos asociados al seguimiento educativo/laboral/otros")
    public ResponseEntity<BodyEncriptado> subirDocumentos(HttpServletRequest httpServletRequest,
                                                         @RequestParam("documentos") MultipartFile[] multipartFiles,
                                                         @RequestParam("body") String bodyEncriptadoString) throws Exception {

        Date fechaInicio = new Date();
        BodyEncriptado bodyEncriptado = new Gson().fromJson(bodyEncriptadoString, BodyEncriptado.class);
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = 
                this.seguimientoEducativoLaboralOtrosService.subirDocumentos(httpServletRequest, bodyEncriptado, multipartFiles);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_DOCUMENTO_SUBIDA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    /**
     * Obtiene los documentos asociados al seguimiento educativo/laboral/otros
     */
    @PostMapping("/obtenerDocumentos")
    @Operation(summary = "Obtiene los documentos asociados al seguimiento educativo/laboral/otros")
    public ResponseEntity<BodyEncriptado> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = 
                this.seguimientoEducativoLaboralOtrosService.obtenerDocumentos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_DOCUMENTO_OBTENER);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
