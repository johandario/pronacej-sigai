package net.latinus.sistema.integral.gestion.seguridad.controller.salida;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.salida.RegistroSalidaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/registro-salida")
@SecurityRequirement(name = "Authorization")
public class RegistroSalidaController {

    private final RegistroSalidaService registroSalidaService;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/lista")
    @Operation(summary = "Obtiene todos los registros de salida manera paginada.")
    public ResponseEntity<BodyEncriptado> obtenerRegistroSalidas(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {


        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> df = registroSalidaService.obtenerSalidas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_REGISTRO_SALIDA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/listado/token")
    @Operation(summary = "Obtiene todos los  registros de salida de manera paginada.")
    public ResponseEntity<BodyEncriptado> obtenerSalidasPorId(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> df = registroSalidaService.obtenerlistadoPorToken(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/crear")
    @Operation(summary = "Crea un nuevo  registros de salida.")
    public ResponseEntity<BodyEncriptado> crearRegistroSalida(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<RegistroSalidaDTO> respuesta = registroSalidaService.crearRegistroSalida(httpServletRequest, bodyEncriptado);
        String accion;
        if (respuesta.getData() != null && Boolean.TRUE.equals(respuesta.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_ACTUALIZAR_REGISTRO_SALIDA;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_REGISTRO_SALIDA;
        }
        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, respuesta,
                fechaInicio, accion);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> buscarRegistroSalidaPorId(HttpServletRequest httpServletRequest,
                                                          @RequestParam String ID) throws Exception {

        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<RegistroSalidaDTO> df = this.registroSalidaService.obtenerRegistroSalidaPorToken(httpServletRequest, ID);
        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, ID, df,
                fechaInicio, EtiquetaNemonico.ACCION_OBTENER_REGISTRO_SALIDA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/eliminar")
    @Operation(summary = "Elimina  registros de salida")
    public ResponseEntity<BodyEncriptado> eliminarRegistroSalida(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<Boolean> df = this.registroSalidaService.eliminarRegistroSalida(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_REGISTRO_SALIDA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/listadoCompletos/token")
    @Operation(summary = "Obtiene todos los  registros de salida de manera paginada.")
    public ResponseEntity<BodyEncriptado> listadoCompletos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> df = registroSalidaService.obtenerlistadoFugasTrasladosCompletados(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

}
