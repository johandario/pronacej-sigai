package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeVisitasDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeVisitasPorPersonaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SuspensionVisitasDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SuspensionVisitasPorPersonaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.InformeVisitasService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/informe-visitas")
@SecurityRequirement(name = "Authorization")
public class InformeVisitasController {
    
    private InformeVisitasService informeVisitasService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    
    @PostMapping("/obtenerInformesVisitasPaginado")
    @Operation(summary = "Obtiene los informes de visitas válidos con paginación")
    public ResponseEntity<BodyEncriptado> obtenerInformesVisitasPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeVisitasDTO>> df = this.informeVisitasService.obtenerInformesVisitasPaginado(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_INFORME_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerSuspensionVisitasPaginado")
    @Operation(summary = "Obtiene las suspensiones de visitas válidas con paginación")
    public ResponseEntity<BodyEncriptado> obtenerSuspensionVisitasPaginado(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<SuspensionVisitasDTO>> df = this.informeVisitasService.obtenerSuspensionVisitasPaginado(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_SUSPENSION_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarInformeVisitas")
    @Operation(summary = "Elimina un informe de visitas")
    public ResponseEntity<BodyEncriptado> eliminarInformeVisitas(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.informeVisitasService.eliminarInformeVisitas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_INFORME_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarSuspensionVisitas")
    @Operation(summary = "Elimina una suspensión de visitas")
    public ResponseEntity<BodyEncriptado> eliminarSuspensionVisitas(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.informeVisitasService.eliminarSuspensionVisitas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_SUSPENSION_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearInformeVisitas")
    @Operation(summary = "Crea un nuevo informe de visitas")
    public ResponseEntity<BodyEncriptado> crearInformeVisitas(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        String nemonicoMenu = this.obtenerNemonicoMenuDesdeRequest(body);

        // Verificar si es una operación de edición
        InformeVisitasPorPersonaDTO informeVisitasPorPersonaDTO = new Gson()
                .fromJson(body, InformeVisitasPorPersonaDTO.class);
        
        // Verificar si algún informe tiene un token diferente de "0" (indicando edición)
        boolean esEdicion = informeVisitasPorPersonaDTO.getListaInformeVisitas().stream()
                .anyMatch(informe -> !informe.getTokenIdentificador().equals("0"));
        
        if (esEdicion) {
            // Redirigir a editar si es una edición
            return editarInformeVisitas(httpServletRequest, bodyEncriptado);
        }

        RespuestaPorDefectoAuditoria<InformeVisitasDTO> df = this.informeVisitasService.crearInformeVisitas(httpServletRequest, bodyEncriptado, nemonicoMenu);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_INFORME_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/editarInformeVisitas")
    @Operation(summary = "Edita un informe de visitas existente")
    public ResponseEntity<BodyEncriptado> editarInformeVisitas(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        String nemonicoMenu = this.obtenerNemonicoMenuDesdeRequest(body);

        RespuestaPorDefectoAuditoria<InformeVisitasDTO> df = this.informeVisitasService.crearInformeVisitas(httpServletRequest, bodyEncriptado, nemonicoMenu);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_INFORME_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearSuspensionVisitas")
    @Operation(summary = "Crea una nueva suspensión de visitas")
    public ResponseEntity<BodyEncriptado> crearSuspensionVisitas(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        String nemonicoMenu = this.obtenerNemonicoMenuDesdeRequest(body);

        // Verificar si es una operación de edición
        SuspensionVisitasPorPersonaDTO suspensionVisitasPorPersonaDTO = new Gson()
                .fromJson(body, SuspensionVisitasPorPersonaDTO.class);
        
        // Verificar si alguna suspensión tiene un token diferente de "0" (indicando edición)
        boolean esEdicion = suspensionVisitasPorPersonaDTO.getListaSuspensionVisitas().stream()
                .anyMatch(suspension -> !suspension.getTokenIdentificador().equals("0"));
        
        if (esEdicion) {
            // Redirigir a editar si es una edición
            return editarSuspensionVisitas(httpServletRequest, bodyEncriptado);
        }

        RespuestaPorDefectoAuditoria<SuspensionVisitasDTO> df = this.informeVisitasService.crearSuspensionVisitas(httpServletRequest, bodyEncriptado, nemonicoMenu);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_CREAR_SUSPENSION_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/editarSuspensionVisitas")
    @Operation(summary = "Edita una suspensión de visitas existente")
    public ResponseEntity<BodyEncriptado> editarSuspensionVisitas(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();
        String nemonicoMenu = this.obtenerNemonicoMenuDesdeRequest(body);

        RespuestaPorDefectoAuditoria<SuspensionVisitasDTO> df = this.informeVisitasService.crearSuspensionVisitas(httpServletRequest, bodyEncriptado, nemonicoMenu);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_EDITAR_SUSPENSION_VISITAS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    /**
     * Extrae el nemonico del menú desde el cuerpo de la petición JSON
     * 
     * @param body El cuerpo JSON deserializado de la petición
     * @return El nemonico del menú o una cadena vacía si no se encuentra
     */
    private String obtenerNemonicoMenuDesdeRequest(String body) {
        try {
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            if (jsonObject.has("nemonicoMenu")) {
                return jsonObject.get("nemonicoMenu").getAsString();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}