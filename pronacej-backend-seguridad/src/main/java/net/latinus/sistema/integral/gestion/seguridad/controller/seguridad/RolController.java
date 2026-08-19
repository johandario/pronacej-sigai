package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefecto;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.RolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/rol")
@SecurityRequirement(name = "Authorization")
public class RolController {

    private RolService rolService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea un rol directamente en el sistema")
    public ResponseEntity<RespuestaPorDefecto<RolDTO>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                    @RequestBody RolDTO rolDTO) {

        return ResponseEntity.ok(this.rolService.crearRolDirecto(httpServletRequest, rolDTO).transformarARespuestaPorDefecto());
    }

    @GetMapping("/obtenerRoles")
    @Operation(summary = "Obten todos los roles del sistema")
    public ResponseEntity<BodyEncriptado> obtenerRoles(HttpServletRequest httpServletRequest) throws Exception {
        RespuestaPorDefectoAuditoria<List<RolDTO>> df = this.rolService.obtenerRolesDeEmpresa(httpServletRequest);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
