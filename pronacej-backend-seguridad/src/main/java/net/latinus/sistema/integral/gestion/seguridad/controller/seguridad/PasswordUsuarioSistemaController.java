package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.PasswordUserSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/password-usuario-sistema")
@SecurityRequirement(name = "Authorization")
public class PasswordUsuarioSistemaController {

    private PasswordUserSistemaService passwordUserSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/actualizarDatosDeSeguridad")
    @Operation(summary = "Crea un parametro del sistema")
    public ResponseEntity<BodyEncriptado> actualizarDatosDeSeguridad(HttpServletRequest httpServletRequest,
                                                                              @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyRequest = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<Boolean> df = this.passwordUserSistemaService.actualizarDatosDeSeguridad(httpServletRequest,
                bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyRequest, df, fechaRequest,
                EtiquetaNemonico.ACCION_ACTUALIZAR_DATOS_DE_SEGURIDAD_DEL_USUARIO_DEL_SISTEMA
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
