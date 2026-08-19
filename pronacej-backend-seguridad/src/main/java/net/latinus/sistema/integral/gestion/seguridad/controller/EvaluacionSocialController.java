package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSocialDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.EvaluacionSocialService;
import com.google.gson.Gson;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacion-social")
@SecurityRequirement(name = "Authorization")
public class EvaluacionSocialController {
    
    private EvaluacionSocialService evaluacionSocialService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerEvaluacionesSocialesPaginado")
    @Operation(summary = "Obtiene las evaluaciones sociales válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerEvaluacionesSocialesPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialDTO>> df = this.evaluacionSocialService.obtenerEvaluacionesSociales(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_EVALUACION_SOCIAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarEvaluacionSocial")
    @Operation(summary = "Elimina una evaluación social")
    public ResponseEntity<BodyEncriptado> eliminarEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionSocialService.eliminarEvaluacionSocial(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_EVALUACION_SOCIAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearEvaluacionSocial")
    @Operation(summary = "Crea o edita una evaluación social")
    public ResponseEntity<BodyEncriptado> crearEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        EvaluacionSocialDTO evaluacionSocialDTO = new Gson().fromJson(body, EvaluacionSocialDTO.class);

        RespuestaPorDefectoAuditoria<EvaluacionSocialDTO> df = this.evaluacionSocialService.crearEvaluacionSocial(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en el campo esEdicion del frontend
        String accionAuditoria;
        boolean esEdicion = evaluacionSocialDTO.getEsEdicion() != null && evaluacionSocialDTO.getEsEdicion();
        if (esEdicion) {
            // Si esEdicion es true, es una edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_EVALUACION_SOCIAL;
        } else {
            // Si esEdicion es false o null, es una creación
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_EVALUACION_SOCIAL;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
}
