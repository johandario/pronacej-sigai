package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion.UbicacionJerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.ubicacion.UbicacionJerarquiaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Controller REST para UbicacionJerarquia
 * Maneja las peticiones HTTP relacionadas con ubicaciones jerárquicas
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/ubicacion-jerarquia")
@SecurityRequirement(name = "Authorization")
public class UbicacionJerarquiaController {

    private UbicacionJerarquiaService ubicacionJerarquiaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    /**
     * Obtiene una lista paginada de ubicaciones jerárquicas
     *
     * @param httpServletRequest request de la petición
     * @param bodyEncriptado objeto encriptado con parámetros de paginación
     * @return ResponseEntity con BodyEncriptado
     * @throws Exception si hay error en desencriptación
     */
    @PostMapping("/obtenerListaPaginada")
    @Operation(summary = "Obtiene la lista paginada de ubicaciones jerárquicas")
    public ResponseEntity<BodyEncriptado> obtenerListaPaginada(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<UbicacionJerarquiaDTO>> df = 
                this.ubicacionJerarquiaService.obtenerListaPaginada(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_UBICACION_JERARQUIA
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtiene la lista completa de ubicaciones jerárquicas
     *
     * @param httpServletRequest request de la petición
     * @return ResponseEntity con BodyEncriptado
     * @throws Exception si hay error en encriptación
     */
    @GetMapping("/obtenerListaCompleta")
    @Operation(summary = "Obtiene la lista completa de ubicaciones jerárquicas")
    public ResponseEntity<BodyEncriptado> obtenerListaCompleta(
            HttpServletRequest httpServletRequest) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> df = 
                this.ubicacionJerarquiaService.obtenerListaCompleta(httpServletRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, "", df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_UBICACION_JERARQUIA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtiene una ubicación jerárquica por su token identificador
     *
     * @param httpServletRequest request de la petición
     * @param tokenIdentificador token de identificación
     * @return ResponseEntity con BodyEncriptado
     * @throws Exception si hay error en encriptación
     */
    @GetMapping("/obtenerPorTokenIdentificador")
    @Operation(summary = "Obtiene una ubicación jerárquica por token identificador")
    public ResponseEntity<BodyEncriptado> obtenerPorTokenIdentificador(
            HttpServletRequest httpServletRequest,
            @RequestParam String tokenIdentificador) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> df = 
                this.ubicacionJerarquiaService.obtenerPorTokenIdentificador(httpServletRequest, tokenIdentificador);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, tokenIdentificador, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_UBICACION_JERARQUIA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtiene los hijos de una ubicación jerárquica padre
     *
     * @param httpServletRequest request de la petición
     * @param tokenIdentificadorPadre token del padre
     * @return ResponseEntity con BodyEncriptado
     * @throws Exception si hay error en encriptación
     */
    @GetMapping("/obtenerHijosPorTokenIdentificadorPadre")
    @Operation(summary = "Obtiene las ubicaciones jerárquicas hijas de un padre")
    public ResponseEntity<BodyEncriptado> obtenerHijosPorTokenIdentificadorPadre(
            HttpServletRequest httpServletRequest,
            @RequestParam String tokenIdentificadorPadre) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> df = 
                this.ubicacionJerarquiaService.obtenerHijosPorTokenIdentificadorPadre(httpServletRequest, tokenIdentificadorPadre);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, tokenIdentificadorPadre, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_UBICACION_JERARQUIA
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Obtiene ubicaciones jerárquicas por token identificador de jerarquía centro
     *
     * @param httpServletRequest request de la petición
     * @param tokenIdentificadorCentro token de la jerarquía centro
     * @return ResponseEntity con BodyEncriptado
     * @throws Exception si hay error en encriptación
     */
    @GetMapping("/obtenerPorTokenIdentificadorJerarquiaCentro")
    @Operation(summary = "Obtiene ubicaciones jerárquicas por token identificador de jerarquía centro")
    public ResponseEntity<BodyEncriptado> obtenerPorTokenIdentificadorJerarquiaCentro(
            HttpServletRequest httpServletRequest,
            @RequestParam String tokenIdentificadorCentro) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> df =
                this.ubicacionJerarquiaService.obtenerPorTokenIdentificadorJerarquiaCentro(
                        httpServletRequest,
                        tokenIdentificadorCentro
                );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, tokenIdentificadorCentro, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_UBICACION_JERARQUIA
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Crea o edita una ubicación jerárquica
     *
     * @param httpServletRequest request de la petición
     * @param bodyEncriptado objeto encriptado con datos de UbicacionJerarquiaDTO
     * @return ResponseEntity con BodyEncriptado
     * @throws Exception si hay error en desencriptación
     */
    @PostMapping("/crearEditar")
    @Operation(summary = "Crea o edita una ubicación jerárquica")
    public ResponseEntity<BodyEncriptado> crearEditar(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> df = 
                this.ubicacionJerarquiaService.crearEditar(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                df.isExito() && df.getData() != null && df.getData().getTokenIdentificador() != null 
                        ? EtiquetaNemonico.ACCION_EDITAR_UBICACION_JERARQUIA 
                        : EtiquetaNemonico.ACCION_CREAR_UBICACION_JERARQUIA
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Elimina una ubicación jerárquica
     *
     * @param httpServletRequest request de la petición
     * @param bodyEncriptado objeto encriptado con token de identificación
     * @return ResponseEntity con BodyEncriptado
     * @throws Exception si hay error en desencriptación
     */
    @PostMapping("/eliminar")
    @Operation(summary = "Elimina una ubicación jerárquica")
    public ResponseEntity<BodyEncriptado> eliminar(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> df = 
                this.ubicacionJerarquiaService.eliminar(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_ELIMINAR_UBICACION_JERARQUIA
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}



