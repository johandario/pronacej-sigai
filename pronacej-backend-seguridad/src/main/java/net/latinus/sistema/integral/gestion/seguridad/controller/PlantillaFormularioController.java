package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.FuncionarioService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlantillaFormularioDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.param.PlantillaFormularioService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/plantilla-formulario")
@SecurityRequirement(name = "Authorization")
public class PlantillaFormularioController {

    private FuncionarioService funcionarioService;
    private PlantillaFormularioService plantillaFormularioService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/crear")
    @Operation(summary = "Crear o editar una plantilla formulario")
    public ResponseEntity<BodyEncriptado> crearPlantillaFormulario(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        PlantillaFormularioDTO plantillaFormularioDTO = new Gson().fromJson(bodyDesencriptado, PlantillaFormularioDTO.class);

        RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> df = this.plantillaFormularioService.crearPlantillaFormulario(httpServletRequest, plantillaFormularioDTO);
        
        // Determinar acción de auditoría basada en si es edición o creación
        String accion = EtiquetaNemonico.ACCION_CREAR_PLANTILLA_FORMULARIO; // Por defecto
        if (df.getData() != null) {
            if (df.getData().getEsEdicion() != null && df.getData().getEsEdicion()) {
                accion = EtiquetaNemonico.ACCION_EDITAR_PLANTILLA_FORMULARIO;
            } else {
                accion = EtiquetaNemonico.ACCION_CREAR_PLANTILLA_FORMULARIO;
            }
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminado lógico de una plantilla formulario")
    public ResponseEntity<BodyEncriptado> eliminarPlantillaFormulario(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        PlantillaFormularioDTO plantillaFormularioDTO = new Gson().fromJson(bodyDesencriptado, PlantillaFormularioDTO.class);

        RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> df = this.plantillaFormularioService.eliminarPlantillaFormulario(httpServletRequest, plantillaFormularioDTO);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PLANTILLA_FORMULARIO);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/lista")
    @Operation(summary = "Obtener todas las plantillas de formularios")
    public ResponseEntity<BodyEncriptado> obtenerPlantillasFormulario(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaFormularioDTO>> df = this.plantillaFormularioService.obtenerPlantillasFormulario(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PLANTILLA_FORMULARIO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar")
    @Operation(summary = "Obten las plantillas de formularioss que coincidan con el valor ingresado")
    public ResponseEntity<BodyEncriptado> buscarPorValor(HttpServletRequest httpServletRequest, 
                                                         @RequestParam String param,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> df = this.funcionarioService.obtenerFuncionariosPorValor(httpServletRequest, param, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_BUSCAR_PLANTILLA_FORMULARIO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}