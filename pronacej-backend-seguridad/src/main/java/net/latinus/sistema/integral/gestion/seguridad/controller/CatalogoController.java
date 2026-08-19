package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.param.CatalogoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/catalogo")
@SecurityRequirement(name = "Authorization")
public class CatalogoController {

    private CatalogoService catalogoService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea uno o varios catalogos directamente en el sistema")
    public ResponseEntity<RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<CatalogoDTO>>>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                                                                      @RequestBody List<CatalogoDTO> catalogoDTOlist) {

        return ResponseEntity.ok(this.catalogoService.crearVariosCatalogosDirecto(httpServletRequest, catalogoDTOlist));
    }

    @PostMapping("/obtenerHijos")
    @Operation(summary = "Obten los catalogos hijos en el sistema")
    public ResponseEntity<BodyEncriptado> obtenerHijos(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = this.catalogoService.obtenerCatalogoPorNemonicoPadre(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerCatalogos")
    @Operation(summary = "Obtener catalogos en el sistema")
    public ResponseEntity<BodyEncriptado> obtenerCatalogos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = this.catalogoService.obtenerCatalogos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerSubCatalogos")
    @Operation(summary = "Obtener sub catalogos en el sistema")
    public ResponseEntity<BodyEncriptado> obtenerSubCatalogos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = this.catalogoService.obtenerSubCatalogos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscarCatalogos")
    @Operation(summary = "buscar catalogos en el sistema")
    public ResponseEntity<BodyEncriptado> buscarCatalogos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = this.catalogoService.buscarCatalogos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_BUSCAR_CATALOGOS
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscarSubCatalogos")
    @Operation(summary = "buscar sub catalogos en el sistema")
    public ResponseEntity<BodyEncriptado> buscarSubCatalogos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = this.catalogoService.buscarSubCatalogos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_BUSCAR_CATALOGOS
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarCatalogo")
    @Operation(summary = "Actualiza un catálogo en el sistema")
    public ResponseEntity<BodyEncriptado> actualizarCatalogo(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<CatalogoDTO> df = this.catalogoService.actualizarCatalogo(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_EDITAR_CATALOGO
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarCatalogo")
    @Operation(summary = "Elimina un catálogo en el sistema")
    public ResponseEntity<BodyEncriptado> eliminarCatalogo(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<CatalogoDTO> df = this.catalogoService.eliminarCatalogo(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_ELIMINAR_CATALOGO
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearCatalogo")
    @Operation(summary = "Crea un catálogo en el sistema")
    public ResponseEntity<BodyEncriptado> crearCatalogo(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<CatalogoDTO> df = this.catalogoService.crearCatalogo(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_CREAR_CATALOGO
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerCatalogoPorNemonico")
    @Operation(summary = "Obten un catalogo válido por el nemonico")
    public ResponseEntity<BodyEncriptado> obtenerCatalogoPorNemonico(
            HttpServletRequest httpServletRequest,
            @RequestParam String nemonico,
            @RequestHeader(value = "nemonicoMenu", defaultValue = "") String nemonicoMenu) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<CatalogoDTO> df = this.catalogoService.obtenerUnCatalogo(httpServletRequest, nemonico);

        // CORREGIDO: Usar nemonicoMenu en lugar de nemonico para auditoría
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, nemonicoMenu, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerCatalogosPorNemonicoPadre")
    @Operation(summary = "Obtener lista de catálogos válidos por el nemonico del padre")
    public ResponseEntity<BodyEncriptado> obtenerCatalogosPorNemonicoPadre(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = this.catalogoService.obtenerSubCatalogosPorNemonicoPadre(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        if (Boolean.FALSE.equals(df.isExito())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerCatalogosPrincipales")
    @Operation(summary = "Obtener lista de catálogos principales")
    public ResponseEntity<BodyEncriptado> obtenerCatalogosPrincipales(
            HttpServletRequest httpServletRequest,
            @RequestHeader(value = "nemonicoMenu", defaultValue = "") String nemonicoMenu) throws Exception {

        Date fechaInicio = new Date();

        Date fechaRequest = new Date();
        RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = this.catalogoService.obtenerCatalogosPrincipales(httpServletRequest);

        // CORREGIDO: Usar nemonicoMenu en lugar de cadena vacía
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, nemonicoMenu, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerCatalogo")
    @Operation(summary = "Obtener catálogo por token")
    public ResponseEntity<BodyEncriptado> obtenerCatalogo(
            HttpServletRequest httpServletRequest, 
            @RequestParam String tokenIdentificador,
            @RequestHeader(value = "nemonicoMenu", defaultValue = "") String nemonicoMenu) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<CatalogoDTO> df = this.catalogoService.obtenerCatalogoPorToken(httpServletRequest, tokenIdentificador);

        // CORREGIDO: Usar nemonicoMenu en lugar de tokenIdentificador
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, nemonicoMenu, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerCatalogosHijos")
    @Operation(summary = "Obtener lista de catálogos hijos paginados")
    public ResponseEntity<BodyEncriptado> obtenerCatalogosHijos(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                null).getData();
        PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDesencriptado, PaginacionRequest.class);
        RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = this.catalogoService.obtenerCatalogosHijos(httpServletRequest,
                paginacionRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerHijos2")
    @Operation(summary = "Obtener lista de catálogos hijos")
    public ResponseEntity<BodyEncriptado> obtenerHijos2(
            HttpServletRequest httpServletRequest,
            @RequestParam String tokenIdentificador,
            @RequestHeader(value = "nemonicoMenu", defaultValue = "") String nemonicoMenu) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = this.catalogoService
                .obtenerHijos(httpServletRequest, tokenIdentificador);

        // CORREGIDO: Usar nemonicoMenu en lugar de tokenIdentificador
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, nemonicoMenu, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerDescendencia")
    @Operation(summary = "Obtener lista de catálogos padres por descendencia")
    public ResponseEntity<BodyEncriptado> obtenerDescendencia(
            HttpServletRequest httpServletRequest,
            @RequestParam String tokenIdentificador,
            @RequestHeader(value = "nemonicoMenu", defaultValue = "") String nemonicoMenu) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = this.catalogoService
                .obtenerDescendencia(httpServletRequest, tokenIdentificador);

        // CORREGIDO: Usar nemonicoMenu en lugar de tokenIdentificador
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, nemonicoMenu, df, fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_CATALOGOS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerTodosPorString")
    @Operation(summary = "Obtener lista de catálogos por filtro")
    public ResponseEntity<BodyEncriptado> obtenerTodosPorString(
            HttpServletRequest httpServletRequest,
            @RequestParam String stringFiltro,
            @RequestHeader(value = "nemonicoMenu", defaultValue = "") String nemonicoMenu) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = this.catalogoService
                .obtenerTodosPorString(httpServletRequest, stringFiltro);

        // CORREGIDO: Usar nemonicoMenu en lugar de stringFiltro
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, nemonicoMenu, df, fechaInicio,
                EtiquetaNemonico.ACCION_BUSCAR_CATALOGOS
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarCatalogosSinHijos")
    @Operation(summary = "Elimina todos los catalogos principales que no tengan hijos")
    public ResponseEntity<RespuestaPorDefectoAuditoria<List<Long>>> eliminarCatalogosSinHijos(
            HttpServletRequest httpServletRequest,
            @RequestHeader(EtiquetaNemonico.HEAD_TOKEN_EMPRESA) String tokenIdentificadorEmpresa) throws Exception {

        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<List<Long>> df = this.catalogoService.borrarCatalogosQueNoTenganHijos(httpServletRequest);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, tokenIdentificadorEmpresa, df, fechaInicio,
                EtiquetaNemonico.ACCION_ELIMINAR_CATALOGO
        );
        return ResponseEntity.ok(df);
    }
}