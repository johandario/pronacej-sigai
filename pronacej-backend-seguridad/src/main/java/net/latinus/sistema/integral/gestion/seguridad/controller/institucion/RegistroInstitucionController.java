package net.latinus.sistema.integral.gestion.seguridad.controller.institucion;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosFamiliaresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.institucion.RegistroInstitucionService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/institucion")
@SecurityRequirement(name = "Authorization")
public class RegistroInstitucionController {

    private final RegistroInstitucionService registroInstitucionService;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/lista")
    @Operation(summary = "Obtiene todos los eventos de fuga de manera paginada.")
    public ResponseEntity<BodyEncriptado> obtenerRegistroSalida(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroInstitucionDTO>> df = registroInstitucionService.
                obtenerInstituciones(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_REGISTRO_INSTITUCION);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/crear")
    @Operation(summary = "Crea un nuevo evento de fuga.")
    public ResponseEntity<BodyEncriptado> crearRegistroInstitucion(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> respuesta = registroInstitucionService.crearRegistroInstitucion(httpServletRequest, bodyEncriptado);
        String accion;
        if (respuesta.getData() != null && Boolean.TRUE.equals(respuesta.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_ACTUALIZAR_REGISTRO_INSTITUCION;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_REGISTRO_INSTITUCION;
        }
        auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, respuesta,
                fechaInicio, accion);
        return ResponseEntity.ok(respuesta.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @GetMapping("/buscar")
    @Operation(summary = "Obtener objeto por número de id")
    public ResponseEntity<BodyEncriptado> obtenerRegistroInstitucionPorToken(HttpServletRequest httpServletRequest,
                                                                    @RequestParam String ID) throws Exception {
        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> df = this.registroInstitucionService.obtenerRegistroInstitucionPorToken(httpServletRequest, ID);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                ID, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_REGISTRO_INSTITUCION);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


    @PostMapping("/eliminar")
    @Operation(summary = "Elimina registro")
    public ResponseEntity<BodyEncriptado> eliminarRegistroInstitucion(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<Boolean> df = this.registroInstitucionService.eliminarRegistroInstitucion(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_REGISTRO_INSTITUCION);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerInstituciones")
    public RespuestaPorDefectoAuditoria<List<RegistroInstitucionDTO>> obtenerTodasLasInstituciones(HttpServletRequest request) {
        return registroInstitucionService.obtenerTodasLasInstituciones(request);
    }

    @GetMapping("/buscarPorRuc")
    @Operation(summary = "Obtener institución por RUC")
    public ResponseEntity<BodyEncriptado> obtenerRegistroInstitucionPorRuc(
            HttpServletRequest httpServletRequest,
            @RequestParam String ruc
    ) throws Exception {
        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> df =
                this.registroInstitucionService.obtenerRegistroInstitucionPorRuc(
                        httpServletRequest,
                        ruc
                );
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                ruc,
                df,
                fechaInicio,
                EtiquetaNemonico.ACCION_OBTENER_REGISTRO_INSTITUCION
        );
        return ResponseEntity.ok(
                df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null)
        );
    }

}
