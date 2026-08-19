package net.latinus.sistema.integral.gestion.seguridad.controller.tras;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.PlanTratamientoIndIntervSeguiService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.tras.TrasladoService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/traslados")
@SecurityRequirement(name = "Authorization")
public class TrasladoController {
    private TrasladoService trasladoService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/lista")
    @Operation(summary = "Obtener lista de registros traslados")
    public ResponseEntity<BodyEncriptado> obtenerTraslado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> df = this.trasladoService.obtenerTraslados(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TRASLADO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> buscarTrasladoPorId(HttpServletRequest httpServletRequest,
                                                          @RequestParam String ID) throws Exception {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = this.trasladoService.obtenerTrasladoPorToken(httpServletRequest, ID);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar-por-ficha")
    @Operation(summary = "Obtener lista de objetos por id de ficha")
    public ResponseEntity<BodyEncriptado> buscarTrasladoPorIdFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                                                 @RequestBody BodyEncriptado bodyEncriptado, @RequestParam Long ID) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> df = this.trasladoService.obtenerTrasladosPorIdFichaIdentificacion(httpServletRequest, bodyEncriptado, ID);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TRASLADO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crear")
    @Operation(summary = "Creación de traslados")
    public ResponseEntity<BodyEncriptado> crearTraslado(HttpServletRequest httpServletRequest,
                                                               @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<TrasladoDTO> df = this.trasladoService.crearTraslado(httpServletRequest, bodyEncriptado);

        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_TRASLADO;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_TRASLADO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/guardarBorrador")
    @Operation(summary = "Creación de traslados")
    public ResponseEntity<BodyEncriptado> guardarBorrador(HttpServletRequest httpServletRequest,
                                                        @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<TrasladoDTO> df = this.trasladoService.guardarBorrador(httpServletRequest, bodyEncriptado);

        String accion;
        if (df.getData() != null && Boolean.TRUE.equals(df.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_EDITAR_TRASLADO;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_TRASLADO;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));

    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminación de traslados")
    public ResponseEntity<BodyEncriptado> eliminarTraslado(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<TrasladoDTO> df = this.trasladoService.eliminarTraslado(httpServletRequest, bodyEncriptado);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_TRASLADO;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/rechazar")
    @Operation(summary = "Rechazo de traslados")
    public ResponseEntity<BodyEncriptado> rechazarTraslado(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<TrasladoDTO> df = this.trasladoService.rechazarTraslado(httpServletRequest, bodyEncriptado);
        String accion = "ACCION_RECHAZAR_TRASLADO";

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/buscar-traslados-por-ficha")
    @Operation(summary = "Obtener lista de traslados por ID de ficha de identificación")
    public ResponseEntity<BodyEncriptado> obtenerListadoTrasladosPorAdolescente(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        Long idFichaIdentificacion;
        try {
            Map<String, Object> datos = new Gson().fromJson(bodyDesencriptado, Map.class);
            idFichaIdentificacion = Long.valueOf(datos.get("idFichaIdentificacion").toString().split("\\.")[0]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("El cuerpo de la solicitud debe contener un campo válido 'idFichaIdentificacion'.", ex);
        }
        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<List<TrasladoDTO>> respuesta = trasladoService.obtenerListadoTrasladosPorAdolescente(httpServletRequest, idFichaIdentificacion);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, respuesta, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_TRASLADO);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar-por-ficha-tokenIdentificador")
    @Operation(summary = "Obtener lista de objetos por id de ficha")
    public ResponseEntity<BodyEncriptado> buscarTrasladoPorFichaIdentificaciontokenIdentificador(HttpServletRequest httpServletRequest,
                                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> df = this.trasladoService.obtenerTrasladosPorFichaIdentificacionTokenIdentificador(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

}
