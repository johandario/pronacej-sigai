package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.NavigationFuseResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/menu")
@SecurityRequirement(name = "Authorization")
public class MenuController {

    private MenuService menuService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea un menu para el sistema")
    public ResponseEntity<RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<MenuDTO>>>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                                                                  @RequestBody List<MenuDTO> list) {
        return ResponseEntity.ok(this.menuService.crearOEditarMenus(httpServletRequest, list));
    }

    @GetMapping("/obtenerMenu")
    @Operation(summary = "Obten los menu poer el rol de la persona")
    public ResponseEntity<BodyEncriptado> obtenerMenu(HttpServletRequest httpServletRequest) throws Exception {
        RespuestaPorDefectoAuditoria<NavigationFuseResponse> resp = this.menuService.crearMenuPorJwtApp(httpServletRequest);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerTodosLosMenu")
    @Operation(summary = "Obten todos los menu del sistema")
    public ResponseEntity<BodyEncriptado> obtenerTodosLosMenu(HttpServletRequest httpServletRequest) throws Exception {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> resp = this.menuService.obtenerTodosLosMenu(httpServletRequest);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @GetMapping("/obtenerMenusPermisos")
    @Operation(summary = "Obten todos los menu del sistema habilitados para permisos por rol/usuario-rol")
    public ResponseEntity<BodyEncriptado> obtenerMenusPermisos(HttpServletRequest httpServletRequest) throws Exception {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> resp = this.menuService.obtenerMenusPermisos(httpServletRequest);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @GetMapping("/obtenerMenusPorEmpresa")
    @Operation(summary = "Obten los menu por empresa sin orden jerarquico")
    public ResponseEntity<BodyEncriptado> obtenerMenusPorEmpresa(HttpServletRequest httpServletRequest) throws Exception {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> resp = this.menuService.obtenerMenusPorEmpresa(httpServletRequest);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/actualizarTitloYAuditoria")
    @Operation(summary = "Actualiza el titulo y la acción de realizar la auditoria de un menu correspondiente")
    public ResponseEntity<BodyEncriptado> actualizarTitloYAuditoria(HttpServletRequest httpServletRequest, @RequestBody MenuDTO menuDTO) throws Exception {
        RespuestaPorDefectoAuditoria<MenuDTO> resp = this.menuService.editarTituloYRealizaAuditoria(httpServletRequest, menuDTO);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
    
    @GetMapping("/obtenerMenusPadres")
    @Operation(summary = "Obten todos los menu padres del sistema")
    public ResponseEntity<BodyEncriptado> obtenerMenusPadres(HttpServletRequest httpServletRequest) throws Exception {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> resp = this.menuService.obtenerMenusPadres(httpServletRequest);

        return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }


}
