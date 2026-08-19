package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CargosJerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.CargosJerarquiaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/cargos-jerarquia")
@SecurityRequirement(name = "Authorization")
public class CargosJerarquiaController {
    
    private CargosJerarquiaService cargosJerarquiaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    
    @PostMapping("/obtenerCargosJerarquias")
    @Operation(summary = "Obten los cargos por jerarquia en el sistema")
    public ResponseEntity<BodyEncriptado> obtenerCargosJerarquias(HttpServletRequest httpServletRequest) throws Exception {

        Date fechaInicio = new Date();
        RespuestaPorDefectoAuditoria<List<CargosJerarquiaDTO>> df = this.cargosJerarquiaService.obtenerCargosJerarquias(httpServletRequest);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                "", df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_CARGOS_JERARQUIA_VALIDOS);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/obtenerCargosJerarquiasPaginado")
    @Operation(summary = "Obten los cargos por jerarquia en el sistema con paginacion")
    public ResponseEntity<BodyEncriptado> obtenerCargosJerarquiasPaginado(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> df = this.cargosJerarquiaService.obtenerCargosJerarquiasPaginado(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_CARGOS_JERARQUIA_VALIDOS);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/crearCargoJerarquia")
    @Operation(summary = "Crea o edita un cargo jerarquia")
    public ResponseEntity<BodyEncriptado> crearCargoJerarquia(HttpServletRequest httpServletRequest,
                                                   @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Deserializar el DTO para determinar si es creación o edición
        CargosJerarquiaDTO cargosJerarquiaDTO = new Gson().fromJson(bodyDesencriptado, CargosJerarquiaDTO.class);

        RespuestaPorDefectoAuditoria<CargosJerarquiaDTO> df = this.cargosJerarquiaService.crearCargoJerarquia(httpServletRequest, bodyEncriptado);
        
        // Determinar la acción de auditoría basada en el campo esEdicion del DTO
        String accionAuditoria;
        boolean esEdicion = false;

        // Verificar si es edición basándose en el campo esEdicion del DTO original
        if (cargosJerarquiaDTO.getEsEdicion() != null && cargosJerarquiaDTO.getEsEdicion()) {
            esEdicion = true;
        }
        
        // También verificar por tokenIdentificador si existe y no es vacío
        if (!esEdicion && cargosJerarquiaDTO.getTokenIdentificador() != null && 
            !cargosJerarquiaDTO.getTokenIdentificador().trim().isEmpty() && 
            !cargosJerarquiaDTO.getTokenIdentificador().equals("0")) {
            esEdicion = true;
        }

        if (esEdicion) {
            accionAuditoria = EtiquetaNemonico.ACCION_EDITAR_CARGOS_JERARQUIA;
        } else {
            accionAuditoria = EtiquetaNemonico.ACCION_CREAR_CARGOS_JERARQUIA;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accionAuditoria);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @PostMapping("/eliminarCargoJerarquia")
    @Operation(summary = "Elimina un cargo jerarquia")
    public ResponseEntity<BodyEncriptado> eliminarCargoJerarquia(HttpServletRequest httpServletRequest,
                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.cargosJerarquiaService.eliminarCargoJerarquia(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_CARGOS_JERARQUIA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar")
    @Operation(summary = "Obten los cargos que coincidan con el valor ingresado")
    public ResponseEntity<BodyEncriptado> buscarPorValor(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> df = this.cargosJerarquiaService.obtenerCargosJerarquiaPorValor(httpServletRequest, bodyEncriptado);

        // Para búsquedas, usamos la acción de obtener cargos válidos
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_CARGOS_JERARQUIA_VALIDOS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}