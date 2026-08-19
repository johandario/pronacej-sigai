package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.OrientacionConsejeriaFamiliarDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.OrientacionConsejeriaPorPersonaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.OrientacionConsejeriaFamiliarService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/orientacion-consejeria-familiar")
@SecurityRequirement(name = "Authorization")
public class OrientacionConsejeriaFamiliarController {
    
    private OrientacionConsejeriaFamiliarService orientacionConsejeriaFamiliarService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerOrientacionesConsejeriasFamiliaresPaginado")
    @Operation(summary = "Obtiene las orientaciones/consejerias válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerOrientacionesConsejeriasFamiliaresPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<OrientacionConsejeriaFamiliarDTO>> df = this.orientacionConsejeriaFamiliarService.obtenerOrientacionesConsejerias(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ORIENTACION_CONSEJERIA_FAMILIAR);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarOrientacionConsejeriaFamiliar")
    @Operation(summary = "Elimina una orientacion/consejeria")
    public ResponseEntity<BodyEncriptado> eliminarOrientacionConsejeriaFamiliar(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.orientacionConsejeriaFamiliarService.eliminarOrientacionConsejeria(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_ORIENTACION_CONSEJERIA_FAMILIAR);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearOrientacionConsejeriaFamiliar")
    @Operation(summary = "Crea o edita una orientacion/consejería")
    public ResponseEntity<BodyEncriptado> crearOrientacionConsejeriaFamiliar(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        // Deserializar el DTO para determinar si es creación o edición
        OrientacionConsejeriaPorPersonaDTO orientacionPorPersonaDTO = new Gson().fromJson(body, OrientacionConsejeriaPorPersonaDTO.class);

        RespuestaPorDefectoAuditoria<OrientacionConsejeriaFamiliarDTO> df = this.orientacionConsejeriaFamiliarService.crearOrientacionConsejeria(httpServletRequest, bodyEncriptado);

        // Determinar la acción de auditoría basada en si hay elementos que no son nuevos (token != "0")
        String accionAuditoria;
        boolean esEdicion = false;
        boolean esCreacion = false;

        // Verificar si hay elementos en la lista
        if (orientacionPorPersonaDTO.getListaOrientacionesConsejerias() != null) {
            for (OrientacionConsejeriaFamiliarDTO item : orientacionPorPersonaDTO.getListaOrientacionesConsejerias()) {
                if (item.getTokenIdentificador() != null && !item.getTokenIdentificador().equals("0")) {
                    esEdicion = true;
                } else if (item.getTokenIdentificador() != null && item.getTokenIdentificador().equals("0")) {
                    esCreacion = true;
                }
            }
        }

        // Determinar la acción prioritaria
        if (esEdicion && esCreacion) {
            // Si hay tanto ediciones como creaciones, priorizar edición
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_ORIENTACION_CONSEJERIA_FAMILIAR;
        } else if (esEdicion) {
            // Solo ediciones
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_ORIENTACION_CONSEJERIA_FAMILIAR;
        } else {
            // Solo creaciones o lista vacía
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_ORIENTACION_CONSEJERIA_FAMILIAR;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
}