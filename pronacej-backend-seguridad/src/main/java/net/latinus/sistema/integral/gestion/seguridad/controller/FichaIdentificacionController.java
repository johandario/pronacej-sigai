package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.FichaIdentificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/ficha-identificacion")
@SecurityRequirement(name = "Authorization")
public class FichaIdentificacionController {

    private FichaIdentificacionService fichaIdentificacionService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerFichasIdentificacionPaginado")
    @Operation(summary = "Obtiene las fichas")
    public ResponseEntity<BodyEncriptado> obtenerFichasIdentificacionPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionDTO>> df = this.fichaIdentificacionService.obtenerFichasIdentificacion(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_FICHA_IDENTIFICACION
                );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerFichasIdentificacionResumido")
    @Operation(summary = "Obtiene las fichas")
    public ResponseEntity<BodyEncriptado> obtenerFichasIdentificacionResumido(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionResumenDTO>> df = this.fichaIdentificacionService.obtenerFichasIdentificacionResumido(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_FICHA_IDENTIFICACION_RESUMIDA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarFichaIdentificacion")
    @Operation(summary = "Elimina una ficha de identificacion")
    public ResponseEntity<BodyEncriptado> eliminarFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.fichaIdentificacionService.eliminarFichaIdentificacion(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_FICHA_IDENTIFICACION
                );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearFichaIdentificacion")
    @Operation(summary = "Crea una ficha de identificacion")
    public ResponseEntity<BodyEncriptado> crearFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = this.fichaIdentificacionService.crearFichaIdentificacion(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, df.getData().getEsEdicion()? EtiquetaNemonico.ACCION_CREAR_FICHA_IDENTIFICACION : EtiquetaNemonico.ACCION_EDITAR_FICHA_IDENTIFICACION
                );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerFichaIdentificacionPorTokenIdentificador")
    @Operation(summary = "Obtiene una ficha de identificacion por su token Identificador")
    public ResponseEntity<BodyEncriptado> obtenerFichaIdentificacionPorTokenIdentificador(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = this.fichaIdentificacionService.obtenerFichaIdentificacionPorTokenIdentificador(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_FICHA_IDENTIFICACION_POR_TOKEN
                );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerFichaIdentificacionPorId")
    @Operation(summary = "Obtiene una ficha de identificacion por su token Identificador")
    public ResponseEntity<BodyEncriptado> obtenerFichaIdentificacionPorId(HttpServletRequest httpServletRequest,
                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = this.fichaIdentificacionService.obtenerFichaIdentificacionPorId(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_FICHA_IDENTIFICACION_POR_TOKEN
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerNombresFichas")
    @Operation(summary = "Obtiene el listado de nombres de las fichas")
    public ResponseEntity<BodyEncriptado> obtenerNombresFichas(HttpServletRequest httpServletRequest,
                                                               @RequestParam(required = false) String tokenCentro) throws Exception {

        RespuestaPorDefectoAuditoria<List<FichaIdentificacionDTO>> df = this.fichaIdentificacionService.obtenerNombresFichas(httpServletRequest, tokenCentro);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerFichaIdentificacionPorNumeroIdentificacion")
    @Operation(summary = "Obtiene una ficha de identificacion por su token Identificador")
    public ResponseEntity<BodyEncriptado> obtenerFichaIdentificacionPorNumeroIdentificacion(HttpServletRequest httpServletRequest,
                                                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = this.fichaIdentificacionService.obtenerFichaIdentificacionPorNumeroDocumento(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_FICHA_IDENTIFICACION_POR_TOKEN
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEstadisticasEdades")
    public ResponseEntity<BodyEncriptado> obtenerEstadisticasEdades(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado body) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<List<EdadEstadisticaDTO>> respuesta =
                fichaIdentificacionService.obtenerEstadisticasEdades(httpServletRequest, body);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_REPORTE_POBLACION_EDAD
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEstadisticasEstados")
    public ResponseEntity<BodyEncriptado> obtenerEstadisticasEstados(
            HttpServletRequest httpServletRequest) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> respuesta =
                fichaIdentificacionService.obtenerEstadisticasEstados(httpServletRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_REPORTE_POBLACION_ESTADOS
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerEstadisticasSexo")
    public ResponseEntity<BodyEncriptado> obtenerEstadisticasSexo(
            HttpServletRequest httpServletRequest) throws Exception {

        Date fechaRequest = new Date();

        RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> respuesta =
                fichaIdentificacionService.obtenerEstadisticasSexo(httpServletRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",
                respuesta, fechaRequest, EtiquetaNemonico.ACCION_OBTENER_REPORTE_POBLACION_ESTADOS
        );

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/validarIngresoNuevo")
    @Operation(summary = "Validar ingreso nuevo a centro")
    public ResponseEntity<BodyEncriptado> validarIngresoNuevo(HttpServletRequest httpServletRequest,
                                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.fichaIdentificacionService.validarIngresoNuevo(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_VALIDAR_FICHA_IDENTIFICACION
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
