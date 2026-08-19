package net.latinus.sistema.integral.gestion.seguridad.controller.salida;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.InformePermisoSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.salida.InformePermisoSalidaService;
import net.latinus.sistema.integral.gestion.seguridad.service.salida.RegistroSalidaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.gson.reflect.TypeToken;


@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/permiso-salida")
@SecurityRequirement(name = "Authorization")
public class InformePermisoSalidaController {
    private final InformePermisoSalidaService permisoSalidaService;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/lista")
    @Operation(summary = "Obtiene todos los eventos de fuga de manera paginada.")
    public ResponseEntity<BodyEncriptado> obtenerEventosFuga(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformePermisoSalidaDTO>> df = permisoSalidaService.obtenerPermisosSalidas(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PERMISO_SALIDA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/crear")
    @Operation(summary = "Crea un nuevo evento de fuga.")
    public ResponseEntity<BodyEncriptado> crearRegistroSalida(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<InformePermisoSalidaDTO> respuesta = permisoSalidaService.crearPermisoSalida(httpServletRequest, bodyEncriptado);
        String accion;
        if (respuesta.getData() != null && Boolean.TRUE.equals(respuesta.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_ACTUALIZAR_PERMISO_SALIDA;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PERMISO_SALIDA;
        }
        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, respuesta,
                fechaInicio, accion);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> buscarRegistroSalidaPorId(HttpServletRequest httpServletRequest,
                                                                    @RequestParam String ID) throws Exception {
        RespuestaPorDefectoAuditoria<InformePermisoSalidaDTO> df = this.permisoSalidaService.obtenerPermisosRegistroSalidaPorToken(httpServletRequest, ID);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/eliminar")
    @Operation(summary = "Elimina una fuga")
    public ResponseEntity<BodyEncriptado> eliminarRegistroSalida(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<Boolean> df = this.permisoSalidaService.eliminarPermisoSalida(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PERMISO_SALIDA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/listado/token")
    @Operation(summary = "Obtiene todos los eventos de permiso de salida de manera paginada.")
    public ResponseEntity<BodyEncriptado> obtenerSalidasPorId(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformePermisoSalidaDTO>> df = permisoSalidaService.obtenerlistadoPorToken(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar-por-ficha")
    @Operation(summary = "Obtiene salidas relacionados con un ID de ficha de identificación.")
    public ResponseEntity<BodyEncriptado> buscarPermisosSalidasPorFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        RespuestaPorDefectoAuditoria<List<InformePermisoSalidaDTO>> df = this.permisoSalidaService.obtenerPermisosSalidaPorFichaIdentificacion(httpServletRequest, bodyEncriptado);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/por-jerarquia")
    @Operation(summary = "Obtiene el director de una jerarquía específica.")
    public ResponseEntity<BodyEncriptado> obtenerDirectorPorJerarquia(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        String bodyDecifrado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        Map<String, String> requestBody = new Gson().fromJson(bodyDecifrado, new TypeToken<Map<String, String>>() {}.getType());
        String nombreJerarquia = requestBody.get("nombreJerarquia");
        if (nombreJerarquia == null || nombreJerarquia.trim().isEmpty()) {
            RespuestaPorDefectoAuditoria<Map<String, String>> respuestaError = new RespuestaPorDefectoAuditoria<>();
            respuestaError.setMensaje("El campo 'nombreJerarquia' es obligatorio.");
            return ResponseEntity.badRequest().body(respuestaError.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
        RespuestaPorDefectoAuditoria<Map<String, String>> respuesta =
                permisoSalidaService.obtenerDirectorPorJerarquia(nombreJerarquia);

        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }



    @PostMapping("/directores")
    @Operation(summary = "Obtiene el director de una jerarquía por ID de departamento.")
    public ResponseEntity<BodyEncriptado> obtenerDirector(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        String bodyDecifrado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();
        Map<String, Object> requestMap = new Gson().fromJson(bodyDecifrado, Map.class);
        Object rawId = requestMap.get("idDepartamento");
        Long idDepartamento = rawId != null ? ((Number) rawId).longValue() : null;

        System.out.println("ID Departamento recibido: " + idDepartamento);

        if (idDepartamento == null) {
            RespuestaPorDefectoAuditoria<Map<String, String>> respuestaError = new RespuestaPorDefectoAuditoria<>();
            respuestaError.setMensaje("El campo 'idDepartamento' es obligatorio.");
            return ResponseEntity.badRequest().body(respuestaError.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }

        var respuesta = permisoSalidaService.obtenerDirectorPorDepartamento(httpServletRequest, idDepartamento);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }


}
