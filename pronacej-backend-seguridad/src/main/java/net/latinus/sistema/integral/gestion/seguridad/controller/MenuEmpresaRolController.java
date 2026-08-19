package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.web.bind.annotation.*;

import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.MenuEmpresaRolService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/menu-empresa-rol")
@SecurityRequirement(name = "Authorization")
public class MenuEmpresaRolController {

    private MenuEmpresaRolService menuService;

    private final Aes aes = new Aes();;

    private RSA rsa;

    /*
    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea un menu para el sistema")
    public ResponseEntity<RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<MenuDTO>>>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                                                                  @RequestBody List<MenuDTO> list) {
        return ResponseEntity.ok(this.menuService.crearOEditarMenus(httpServletRequest, list));
    }
    */

}
