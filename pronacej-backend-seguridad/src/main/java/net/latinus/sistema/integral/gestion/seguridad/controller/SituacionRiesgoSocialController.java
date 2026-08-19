package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionRiesgoSocialDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.SituacionRiesgoSocialService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/situacion-riesgo-social")
@SecurityRequirement(name = "Authorization")
public class SituacionRiesgoSocialController {
    
    private SituacionRiesgoSocialService situacionRiesgoSocialService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerSituacionesRiesgoSocialPaginado")
    @Operation(summary = "Obtiene las situaciones de riesgo social válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerSituacionesRiesgoSocialPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionRiesgoSocialDTO>> df = this.situacionRiesgoSocialService.obtenerSituacionesRiesgoSocial(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SITUACION_RIESGO_SOCIAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarSituacionRiesgoSocial")
    @Operation(summary = "Elimina una situación de riesgo social")
    public ResponseEntity<BodyEncriptado> eliminarSituacionRiesgoSocial(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.situacionRiesgoSocialService.eliminarSituacionRiesgoSocial(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_SITUACION_RIESGO_SOCIAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearSituacionRiesgoSocial")
    @Operation(summary = "Crea o edita una situación de riesgo social")
    public ResponseEntity<BodyEncriptado> crearSituacionRiesgoSocial(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        // Deserializar el DTO para determinar si es creación o edición
        SituacionRiesgoSocialDTO situacionRiesgoSocialDTO = new Gson().fromJson(body, SituacionRiesgoSocialDTO.class);

        RespuestaPorDefectoAuditoria<SituacionRiesgoSocialDTO> df = this.situacionRiesgoSocialService.crearSituacionRiesgoSocial(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en el campo esEdicion del frontend
        String accionAuditoria;
        boolean esEdicion = situacionRiesgoSocialDTO.getEsEdicion() != null && situacionRiesgoSocialDTO.getEsEdicion();
        if (esEdicion) {
            // Si esEdicion es true, es una edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_SITUACION_RIESGO_SOCIAL;
        } else {
            // Si esEdicion es false o null, es una creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_SITUACION_RIESGO_SOCIAL;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
}