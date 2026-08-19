package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosDeSeguridadDeUsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaEmpresaRolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefecto;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.UsuarioSistemaEmpresaRolService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Authorization")
@RequestMapping(path = "api/v1/usuario-sistema-empresa-rol")
public class UsuarioSistemaEmpresaRolController {

    private UsuarioSistemaEmpresaRolService usuarioSistemaEmpresaRolService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/crearDirecto")
    @Operation(summary = "Crea una relación entre el usuario sistema, empresa y el rol directamente en el sistema")
    public ResponseEntity<RespuestaPorDefecto<UsuarioSistemaEmpresaRolDTO>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                                         @RequestBody UsuarioSistemaEmpresaRolDTO usuarioSistemaEmpresaRolDTO) {

        return ResponseEntity.ok(this.usuarioSistemaEmpresaRolService.crearUsuarioDirecto(httpServletRequest, usuarioSistemaEmpresaRolDTO).transformarARespuestaPorDefecto());
    }

    @GetMapping("/obtenerInformacionDeSeguridad")
    @Operation(summary = "Obten la información actual de seguridad de un usuario logeado")
    public ResponseEntity<BodyEncriptado> obtenerInformacionDeSeguridad(HttpServletRequest httpServletRequest) throws Exception {

        RespuestaPorDefectoAuditoria<DatosDeSeguridadDeUsuarioSistemaDTO> df =
                this.usuarioSistemaEmpresaRolService.obtenerDataDeSeguridad(httpServletRequest);

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
