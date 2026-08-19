package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSocialArtefactoDTO;
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
import net.latinus.sistema.integral.gestion.seguridad.service.IA.EvaluacionSocialArtefactoService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/evaluacion-social-artefacto")
@SecurityRequirement(name = "Authorization")
public class EvaluacionSocialArtefactoController {
    
    private EvaluacionSocialArtefactoService evaluacionSocialArtefactoService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerArtefactosPorEvaluacionSocialPaginado")
    @Operation(summary = "Obtiene los artefactos por evaluación social válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerArtefactosPorEvaluacionSocialPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialArtefactoDTO>> df = this.evaluacionSocialArtefactoService.obtenerArtefactosPorEvaluacionSocial(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ARTEFACTO_POR_EVALUACION_SOCIAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarArtefactoPorEvaluacionSocial")
    @Operation(summary = "Elimina un artefacto perteneciente a una evaluación social")
    public ResponseEntity<BodyEncriptado> eliminarArtefactoPorEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.evaluacionSocialArtefactoService.eliminarArtefactoPorEvaluacionSocial(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_ARTEFACTO_POR_EVALUACION_SOCIAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearEvaluacionSocialArtefacto")
    @Operation(summary = "Crea un artefacto perteneciente a una evaluación social")
    public ResponseEntity<BodyEncriptado> crearArtefactoPorEvaluacionSocial(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<EvaluacionSocialArtefactoDTO> df = this.evaluacionSocialArtefactoService.crearArtefactoPorEvaluacionSocial(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_ARTEFACTO_POR_EVALUACION_SOCIAL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
}
