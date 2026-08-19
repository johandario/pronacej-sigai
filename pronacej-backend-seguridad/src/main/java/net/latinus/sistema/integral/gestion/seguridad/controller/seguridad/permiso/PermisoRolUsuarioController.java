package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad.permiso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioNombresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/permisoRolUsuario")
@SecurityRequirement(name = "Authorization")
public class PermisoRolUsuarioController {
    private final PermisoRolUsuarioService permisoRolUsuarioService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/obtenerPermisos")
    @Operation(summary = "Obtener permisos generales")
    public ResponseEntity<BodyEncriptado> obtenerPermisos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<PermisoRolUsuarioNombresDTO>> resp = this.permisoRolUsuarioService.obtenerPermisos(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, resp, fechaInicio, EtiquetaNemonico.ACCION_LISTAR_PERMISOS_MENU_ROL_USUARIO);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerPermisosPorToken")
    @Operation(summary = "Obtener permisos por el token del permiso")
    public ResponseEntity<BodyEncriptado> obtenerPermisosPorToken(HttpServletRequest httpServletRequest, @RequestParam String token) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> resp = this.permisoRolUsuarioService.obtenerPermisosPorToken(httpServletRequest, token);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                token, resp, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PERMISO_MENU_ROL_USUARIO);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearEditarPermisos")
    @Operation(summary = "Crea o edita los permisos por menú, rol o funcionario/Jerarquía/Rol")
    public ResponseEntity<BodyEncriptado> crearEditarPermisos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> resp = this.permisoRolUsuarioService.crearEditarPermisos(httpServletRequest, bodyEncriptado);

        String accion;
        if (resp.getData() != null && Boolean.TRUE.equals(resp.getData().getEsEdicion())) {
            accion = EtiquetaNemonico.ACCION_ACTUALIZAR_PERMISO_MENU_ROL_USUARIO;
        } else {
            accion = EtiquetaNemonico.ACCION_CREAR_PERMISO_MENU_ROL_USUARIO;
        }
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, bodyDesencriptado, resp,
                fechaInicio, accion);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarPermisos")
    @Operation(summary = "Elimina los permisos por menú, rol o funcionario/Jerarquía/Rol")
    public ResponseEntity<BodyEncriptado> eliminarPermisos(HttpServletRequest httpServletRequest, @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> resp = this.permisoRolUsuarioService.eliminarPermisos(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, resp, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_PERMISO_MENU_ROL_USUARIO);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerPermisosUsuario")
    @Operation(summary = "Obtener permisos por el token del rol")
    public ResponseEntity<BodyEncriptado> obtenerPermisosUsuario(HttpServletRequest httpServletRequest, @RequestParam String uuid) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> resp = this.permisoRolUsuarioService.obtenerPermisosUsuarioPorTokenFicha(httpServletRequest, uuid);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                null, resp, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_PERMISO_MENU_ROL_USUARIO);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

}
