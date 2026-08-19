package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CreacionDeRol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CreacionDeUsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.LoginResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuthService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/auth")
@SecurityRequirement(name = "Authorization")
public class AuthController {

    private AuthService authService;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    /*
    @PostMapping("/login")
    @Operation(summary = "Login del usuario del sistema")
    public ResponseEntity<BodyEncriptado> login(HttpServletRequest httpServletRequest,
                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<LoginResponse> df = this.authService.loginUserSistema(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyEncriptado.toString(), df, fechaInicio, EtiquetaNemonico.ACCION_LOGIN_USUARIO_SISTEMA);

        BodyEncriptado body = df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }
     */

    @PostMapping("/login")
    @Operation(summary = "Login del usuario del sistema")
    public ResponseEntity<RespuestaPorDefectoAuditoria<LoginResponse>> login(HttpServletRequest httpServletRequest,
                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<LoginResponse> df = this.authService.loginUserSistema(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyEncriptado.toString(), df, fechaInicio, EtiquetaNemonico.ACCION_LOGIN_USUARIO_SISTEMA);

        //BodyEncriptado body = df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null);

        return ResponseEntity.ok(df);
    }

    @GetMapping("/verificarJWT")
    @Operation(summary = "Verifica un jwt válido")
    public ResponseEntity<BodyEncriptado> verificarJWT(HttpServletRequest httpServletRequest) throws Exception {

        RespuestaPorDefectoAuditoria<LoginResponse> df = this.authService.verificarJwt(httpServletRequest);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearUsuario")
    @Operation(summary = "Crea un usuario nuevo del sistema")
    public ResponseEntity<BodyEncriptado> crearUsuario(HttpServletRequest httpServletRequest,
                                                       @RequestBody
                                                       BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<CreacionDeUsuarioSistema> df = this.authService.creaUnUsuarioDelSistema(httpServletRequest, bodyEncriptado);
        
        // Determinar acción de auditoría basada en si es edición o creación
        String accion = EtiquetaNemonico.ACCION_CREAR_USUARIO_DEL_SISTEMA; // Por defecto
        if (df.getData() != null) {
            if (df.getData().getEsEdicion() != null && df.getData().getEsEdicion()) {
                accion = EtiquetaNemonico.ACCION_EDITAR_USUARIO_DEL_SISTEMA;
            } else {
                accion = EtiquetaNemonico.ACCION_CREAR_USUARIO_DEL_SISTEMA;
            }
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerUsuariosValidos")
    @Operation(summary = "Obtiene los usuario válidos del sistema")
    public ResponseEntity<BodyEncriptado> obtenerUsuariosValidos(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> df = this.authService.obtenerUsuarioDelSistema(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_USUARIO_VALIDOS_DEL_SISTEMA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerUsuariosValidosActivos")
    @Operation(summary = "Obtiene los usuario válidos del sistema")
    public ResponseEntity<BodyEncriptado> obtenerUsuariosValidosActivos(HttpServletRequest httpServletRequest,
                                                                 @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeUsuarioSistema>> df = this.authService.obtenerUsuarioValidosDelSistema(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_USUARIO_VALIDOS_DEL_SISTEMA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarUsuario")
    @Operation(summary = "Elimina un usuario del sistema")
    public ResponseEntity<BodyEncriptado> eliminarUsuario(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.authService.eliminarUsuario(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_USUARIO_DEL_SISTEMA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/bloquearUsuario")
    @Operation(summary = "Bloquea un usuario del sistema")
    public ResponseEntity<BodyEncriptado> bloquearUsuario(HttpServletRequest httpServletRequest,
                                                          @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.authService.bloquearUsuarioSistema(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_BLOQUEAR_USUARIO_DEL_SISTEMA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearRol")
    @Operation(summary = "Crea un rol nuevo")
    public ResponseEntity<BodyEncriptado> crearRol(HttpServletRequest httpServletRequest,
                                                   @RequestBody
                                                   BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<CreacionDeRol> df = this.authService.creaUnRol(httpServletRequest, bodyEncriptado);
        
        // Determinar acción de auditoría basada en si es edición o creación
        String accion = EtiquetaNemonico.ACCION_CREAR_ROL; // Por defecto
        if (df.getData() != null) {
            if (df.getData().getEsEdicion() != null && df.getData().getEsEdicion()) {
                accion = EtiquetaNemonico.ACCION_EDITAR_ROL;
            } else {
                accion = EtiquetaNemonico.ACCION_CREAR_ROL;
            }
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerRolesValidos")
    @Operation(summary = "Obtiene los roles válidos")
    public ResponseEntity<BodyEncriptado> obtenerRolesValidos(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();
        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> df = this.authService.obtenerRoles(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_ROL_VALIDOS);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarRol")
    @Operation(summary = "Elimina un rol")
    public ResponseEntity<BodyEncriptado> eliminarRol(HttpServletRequest httpServletRequest,
                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.authService.eliminarRol(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_ROL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/bloquearRol")
    @Operation(summary = "Bloquea un rol")
    public ResponseEntity<BodyEncriptado> bloquearRol(HttpServletRequest httpServletRequest,
                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.authService.bloquearRol(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_BLOQUEAR_ROL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearRelacionMenusRol")
    @Operation(summary = "Crea la relacion en menus rol empresas para habilitar los permisos de los roles a ciertos menus")
    public ResponseEntity<BodyEncriptado> crearRelacionMenusRol(HttpServletRequest httpServletRequest,
                                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.authService.crearRelacionMenusRol(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_RELACIONAR_MENUS_ROL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerMenusAccesiblesPorRol")
    @Operation(summary = "Obtiene los menus a los que ya tiene acceso el rol")
    public ResponseEntity<BodyEncriptado> obtenerMenusAccesiblesPorRol(HttpServletRequest httpServletRequest,
                                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = this.authService.obtenerMenusAccesiblesPorRol(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_MENUS_ROL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/crearMenu")
    @Operation(summary = "Crea un menú nuevo del sistema")
    public ResponseEntity<BodyEncriptado> crearMenu(HttpServletRequest httpServletRequest,
                                                    @RequestBody
                                                    BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<MenuDTO> df = this.authService.creaUnMenuDelSistema(httpServletRequest, bodyEncriptado);
        
        // Determinar acción de auditoría basada en si es edición o creación
        String accion = EtiquetaNemonico.ACCION_CREAR_MENU_DEL_SISTEMA; // Por defecto
        if (df.getData() != null) {
            if (df.getData().getId() != null) {
                accion = EtiquetaNemonico.ACCION_ACTUALIZAR_DATOS_MENU_DEL_SISTEMA;
            } else {
                accion = EtiquetaNemonico.ACCION_CREAR_MENU_DEL_SISTEMA;
            }
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accion);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/obtenerMenusValidos")
    @Operation(summary = "Obtiene los usuario válidos del sistema")
    public ResponseEntity<BodyEncriptado> obtenerMenusValidos(HttpServletRequest httpServletRequest,
                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<PaginacionResponse<MenuDTO>> df = this.authService.obtenerMenuDelSistema(httpServletRequest, bodyEncriptado);

        // Usar la misma acción que obtener menús por rol ya que es la misma funcionalidad
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_OBTENER_MENUS_ROL);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/eliminarMenu")
    @Operation(summary = "Elimina un menú")
    public ResponseEntity<BodyEncriptado> eliminarMenu(HttpServletRequest httpServletRequest,
                                                       @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.authService.eliminarMenu(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, EtiquetaNemonico.ACCION_ELIMINAR_MENU_DEL_SISTEMA);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/verificarPermisos")
    @Operation(summary = "Verifica los permisos a una pantalla del menu")
    public ResponseEntity<BodyEncriptado> verificarPermisos(HttpServletRequest httpServletRequest,
                                                            @RequestBody
                                                            BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<MenuDTO> df = this.authService.verificarPermisos(httpServletRequest, bodyEncriptado);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/buscar")
    @Operation(summary = "Obtiene los roles que coincidan con el valor ingresado")
    public ResponseEntity<BodyEncriptado> buscarPorValor(HttpServletRequest httpServletRequest,
                                                         @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        // Obtener el nemónico del menú desde el header para determinar el tipo de auditoría
        String nemonicoMenu = httpServletRequest.getHeader(EtiquetaNemonico.HEAD_NEMONICO_MENU);

        RespuestaPorDefectoAuditoria<PaginacionResponse<CreacionDeRol>> df = this.authService.obtenerRolesPorFiltro(httpServletRequest, bodyEncriptado);

        // Determinar qué etiqueta de acción usar según el contexto del menú
        String accionAuditoria;
        if (nemonicoMenu != null && nemonicoMenu.equals(EtiquetaNemonico.NEMONICO_MENU_MENU_ROL)) {
            accionAuditoria = EtiquetaNemonico.ACCION_BUSCAR_ROLES_MENU_ROL;
        } else if (nemonicoMenu != null && nemonicoMenu.equals(EtiquetaNemonico.NEMONICO_MENU_ROL)) {
            accionAuditoria = EtiquetaNemonico.ACCION_BUSCAR_ROLES_POR_VALOR;
        } else {
            // Si no se identifica un contexto específico, usar obtener roles válidos
            accionAuditoria = EtiquetaNemonico.ACCION_OBTENER_ROL_VALIDOS;
        }

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyDesencriptado, df, fechaInicio, accionAuditoria);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null));
    }

    @PostMapping("/cambioJerarquia")
    @Operation(summary = "Cambio de jerarquia del usuario del sistema")
    public ResponseEntity<BodyEncriptado> cambioJerarquia(HttpServletRequest httpServletRequest,
                                                @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<LoginResponse> df = this.authService.cambioJerarquiaUserSistema(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                bodyEncriptado.toString(), df, fechaInicio, EtiquetaNemonico.ACCION_LOGIN_USUARIO_SISTEMA);

        BodyEncriptado body = df.transFormarEnbodyEncriptado(parametroDelSistemaRepository, null);

        return ResponseEntity.ok(body);
    }
}