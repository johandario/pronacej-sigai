package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.TrabajoLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.TrabajoLaboralEstadisticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.TrabajoLaboralService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/trabajo-laboral")
@SecurityRequirement(name = "Authorization")

public class TrabajoLaboralController {
    private TrabajoLaboralService trabajoLaboralService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;


    @PostMapping("/lista")
    @Operation(summary = "Obtener lista de trabajos laborales por adolescente")
    public ResponseEntity<BodyEncriptado> obtenerListaTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrabajoLaboralDTO>> df =
                this.trabajoLaboralService.obtenerListaTrabajoLaboral(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/crear")
    @Operation(summary = "Crear o editar trabajo laboral")
    public ResponseEntity<BodyEncriptado> crearTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado
                .desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> df =
                this.trabajoLaboralService.crearTrabajoLaboral(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_CREAR_TRABAJO_LABORAL
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/obtener")
    @Operation(summary = "Obtener trabajo laboral por token")
    public ResponseEntity<BodyEncriptado> obtenerTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> df =
                this.trabajoLaboralService.obtenerTrabajoLaboral(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/eliminar")
    @Operation(summary = "Eliminar trabajo laboral")
    public ResponseEntity<BodyEncriptado> eliminarTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado
                .desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<Boolean> df =
                this.trabajoLaboralService.eliminarTrabajoLaboral(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_ELIMINAR_TRABAJO_LABORAL
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/cantidadTrabajoActivo")
    public ResponseEntity<BodyEncriptado> obtenerCantidadTrabajoActivo(
            HttpServletRequest httpServletRequest
    ) throws Exception {
        Date fechaRequest = new Date();
        RespuestaPorDefectoAuditoria<Long> respuesta =
                trabajoLaboralService.obtenerCantidadTrabajoActivo(httpServletRequest);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta,
                fechaRequest,
                EtiquetaNemonico.ACCION_LISTAR_TRABAJO_LABORAL
        );
        return ResponseEntity.ok(
                respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null)
        );
    }


    @PostMapping("/estadisticasTrabajoLaboral")
    public ResponseEntity<BodyEncriptado> obtenerEstadisticasTrabajoLaboral(
            HttpServletRequest httpServletRequest
    ) throws Exception {
        Date fechaRequest = new Date();
        RespuestaPorDefectoAuditoria<List<TrabajoLaboralEstadisticoDTO>> respuesta =
                trabajoLaboralService.obtenerEstadisticasTrabajoLaboral(httpServletRequest);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta,
                fechaRequest,
                EtiquetaNemonico.ACCION_LISTAR_TRABAJO_LABORAL
        );
        return ResponseEntity.ok(
                respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null)
        );
    }

}