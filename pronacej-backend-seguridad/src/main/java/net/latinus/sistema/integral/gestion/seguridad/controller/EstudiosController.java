package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;

import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;

import net.latinus.sistema.integral.gestion.seguridad.model.both.EstudiosDTO;

import net.latinus.sistema.integral.gestion.seguridad.model.both.EstudiosEstadisticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;

import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;

import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.EstudiosService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/estudios")
@SecurityRequirement(name = "Authorization")

public class EstudiosController {
    private EstudiosService estudiosService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/lista")
    @Operation(summary = "Obtener lista de estudios por adolescente")
    public ResponseEntity<BodyEncriptado> obtenerListaEstudios(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EstudiosDTO>> df =
                this.estudiosService.obtenerListaEstudios(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Crear o editar estudios")
    public ResponseEntity<BodyEncriptado> crearEstudios(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado
                .desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<EstudiosDTO> df =
                this.estudiosService.crearEstudios(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_CREAR_ESTUDIOS
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtener")
    @Operation(summary = "Obtener estudios por token")
    public ResponseEntity<BodyEncriptado> obtenerEstudios(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        RespuestaPorDefectoAuditoria<EstudiosDTO> df =
                this.estudiosService.obtenerEstudios(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminar estudios")
    public ResponseEntity<BodyEncriptado> eliminarEstudios(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado
                .desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<Boolean> df =
                this.estudiosService.eliminarEstudios(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_ELIMINAR_ESTUDIOS
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/consultar-institucion-ruc")
    @Operation(summary = "Consultar institución educativa por RUC")
    public ResponseEntity<BodyEncriptado> consultarInstitucionPorRuc(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        RespuestaPorDefectoAuditoria<EstudiosDTO> df =
                this.estudiosService.consultarInstitucionPorRuc(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/cantidadUsuariosEstudiando")
    public ResponseEntity<BodyEncriptado> obtenerCantidadUsuariosEstudiando(
            HttpServletRequest httpServletRequest
    ) throws Exception {

        RespuestaPorDefectoAuditoria<Long> respuesta =
                estudiosService.obtenerCantidadUsuariosEstudiando(httpServletRequest);

        return ResponseEntity.ok(
                respuesta.transFormarEnbodyEncriptado(
                        this.parametroDelSistemaRepository,
                        null
                )
        );
    }

    @PostMapping("/estadisticasEstudios")
    public ResponseEntity<BodyEncriptado> obtenerEstadisticasEstudios(
            HttpServletRequest httpServletRequest
    ) throws Exception {
        RespuestaPorDefectoAuditoria<List<EstudiosEstadisticoDTO>> respuesta =
                estudiosService.obtenerEstadisticasEstudios(httpServletRequest);
        return ResponseEntity.ok(
                respuesta.transFormarEnbodyEncriptado(
                        this.parametroDelSistemaRepository,
                        null
                )
        );
    }

    @PostMapping("/porcentajeConvenioPronacej")
    public ResponseEntity<BodyEncriptado> obtenerPorcentajeConvenioPronacej(
            HttpServletRequest httpServletRequest
    ) throws Exception {
        RespuestaPorDefectoAuditoria<Double> respuesta =
                estudiosService.obtenerPorcentajeConvenioPronacej(httpServletRequest);
        return ResponseEntity.ok(
                respuesta.transFormarEnbodyEncriptado(
                        this.parametroDelSistemaRepository,
                        null
                )
        );
    }


}