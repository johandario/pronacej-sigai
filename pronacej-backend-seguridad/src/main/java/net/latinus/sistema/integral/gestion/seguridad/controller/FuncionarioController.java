package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
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

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/funcionario")
@SecurityRequirement(name = "Authorization")
public class FuncionarioController {

    private FuncionarioService funcionarioService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/crear")
    @Operation(summary = "Crear o editar un funcionario en el sistema")
    public ResponseEntity<BodyEncriptado> crearFuncionarioDirecto(HttpServletRequest httpServletRequest,
                                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        FuncionarioDTO funcionarioDTO = new Gson().fromJson(bodyDesencriptado, FuncionarioDTO.class);

        // Determinar la acción antes de ejecutar el servicio para evitar problemas con datos null
        String accion = (funcionarioDTO.getEsEdicion() != null && funcionarioDTO.getEsEdicion()) ? 
            EtiquetaNemonico.ACCION_EDITAR_FUNCIONARIO_DEL_SISTEMA : 
            EtiquetaNemonico.ACCION_CREAR_FUNCIONARIO_DEL_SISTEMA;

        RespuestaPorDefectoAuditoria<FuncionarioDTO> df = this.funcionarioService.crearFuncionario(httpServletRequest, funcionarioDTO);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminar")
    @Operation(summary = "Eliminado lógico de un funcionario en el sistema")
    public ResponseEntity<BodyEncriptado> eliminarFuncionario(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                        .getData();

        FuncionarioDTO funcionarioDTO = new Gson().fromJson(bodyDesencriptado, FuncionarioDTO.class);

        RespuestaPorDefectoAuditoria<FuncionarioDTO> df = this.funcionarioService.eliminarFuncionario(httpServletRequest, funcionarioDTO);
        String accion = EtiquetaNemonico.ACCION_ELIMINAR_FUNCIONARIO_DEL_SISTEMA;

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }



    @PostMapping("/lista")
    @Operation(summary = "Obtener todos los funcionarios del sistema con paginación")
    public ResponseEntity<BodyEncriptado> obtenerFuncionarios(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> df = this.funcionarioService.obtenerFuncionarios(httpServletRequest, bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_FUNCIONARIOS_DEL_SISTEMA);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/listaSinPaginacion")
    @Operation(summary = "Obtener todos los funcionarios del sistema con paginación")
    public ResponseEntity<BodyEncriptado> obtenerFuncionariosSinPaginacion(HttpServletRequest httpServletRequest) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<FuncionarioDTO>> df = this.funcionarioService.obtenerFuncionariosSinPaginacion(httpServletRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                null, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_FUNCIONARIOS_DEL_SISTEMA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar")
    @Operation(summary = "Buscar funcionarios que coincidan con el valor ingresado")
    public ResponseEntity<BodyEncriptado> buscarPorValor(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        
        RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> df = this.funcionarioService.obtenerFuncionariosPorValor(httpServletRequest, "", bodyEncriptado);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_BUSCAR_FUNCIONARIOS_POR_VALOR);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerFuncionarioDelUsuario")
    @Operation(summary = "Obtener el funcionario asociado al usuario actual")
    public ResponseEntity<BodyEncriptado> obtenerFuncionarioDelUsuario(HttpServletRequest httpServletRequest) throws Exception {
        Date fechaInicio = new Date();
        
        RespuestaPorDefectoAuditoria<FuncionarioDTO> df = this.funcionarioService.obtenerFuncionarioDelUsuario(httpServletRequest);
        
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                "", df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_FUNCIONARIO_DEL_USUARIO);
        
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerJerarquiasFuncionarioDelUsuario")
    @Operation(summary = "Obtener el funcionario asociado al usuario actual")
    public ResponseEntity<BodyEncriptado> obtenerJerarquiasFuncionarioDelUsuario(HttpServletRequest httpServletRequest) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = this.funcionarioService.obtenerJerarquiasPorFuncionarios(httpServletRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                "", df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_FUNCIONARIO_DEL_USUARIO);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}