package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion.FichaUbicacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.ubicacion.FichaUbicacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.gson.Gson;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/ficha-ubicacion")
@SecurityRequirement(name = "Authorization")
public class FichaUbicacionController {

    private FichaUbicacionService fichaUbicacionService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/obtenerListaPaginada")
    @Operation(summary = "Obtiene la lista paginada de ubicaciones por ficha")
    public ResponseEntity<BodyEncriptado> obtenerListaPaginada(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaUbicacionDTO>> df =
                this.fichaUbicacionService.obtenerListaPaginada(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_FICHA_UBICACION
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerPorTokenIdentificador")
    @Operation(summary = "Obtiene una ficha ubicación por token identificador")
    public ResponseEntity<BodyEncriptado> obtenerPorTokenIdentificador(
            HttpServletRequest httpServletRequest,
            @RequestParam String tokenIdentificador
    ) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<FichaUbicacionDTO> df =
                this.fichaUbicacionService.obtenerPorTokenIdentificador(httpServletRequest, tokenIdentificador);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                tokenIdentificador,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_FICHA_UBICACION
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearEditar")
    @Operation(summary = "Crea o edita una ficha ubicación")
    public ResponseEntity<BodyEncriptado> crearEditar(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        FichaUbicacionDTO requestDTO = new Gson().fromJson(bodyDesencriptado, FichaUbicacionDTO.class);

        RespuestaPorDefectoAuditoria<FichaUbicacionDTO> df =
                this.fichaUbicacionService.crearEditar(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                requestDTO != null && requestDTO.getTokenIdentificador() != null && !requestDTO.getTokenIdentificador().isEmpty()
                        ? EtiquetaNemonico.ACCION_EDITAR_FICHA_UBICACION
                        : EtiquetaNemonico.ACCION_CREAR_FICHA_UBICACION
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminar")
    @Operation(summary = "Elimina una ficha ubicación")
    public ResponseEntity<BodyEncriptado> eliminar(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado
    ) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<FichaUbicacionDTO> df =
                this.fichaUbicacionService.eliminar(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_ELIMINAR_FICHA_UBICACION
        );

        if (!df.isExito()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
