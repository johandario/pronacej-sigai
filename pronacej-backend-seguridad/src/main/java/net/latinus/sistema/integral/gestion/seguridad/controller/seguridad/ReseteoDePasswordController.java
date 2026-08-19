package net.latinus.sistema.integral.gestion.seguridad.controller.seguridad;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.ReseteoDePasswordDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ReseteoDeContraseniaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.ReseteoDePasswordService;
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
@RequestMapping(path = "api/v1/reseteo-password")
@SecurityRequirement(name = "Authorization")
public class ReseteoDePasswordController {

    private ReseteoDePasswordService reseteoDePasswordService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/empezar")
    @Operation(summary = "Crea un proceso de reestablecimiento de contraseña")
    public ResponseEntity<BodyEncriptado> empezar(HttpServletRequest httpServletRequest,
                                                  @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        ReseteoDeContraseniaRequest reseteoDeContraseniaRequest = new Gson().fromJson(bodyDesencriptado, ReseteoDeContraseniaRequest.class);

        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = this.reseteoDePasswordService.empezarAccionDeReseteoDePassword(
                httpServletRequest, reseteoDeContraseniaRequest
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_CREACION_RESETEO_PASSWORD
        );
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/resetearPassword")
    @Operation(summary = "Resetea la contraseña de un usuario del sistema")
    public ResponseEntity<BodyEncriptado> resetearPassword(HttpServletRequest httpServletRequest,
                                                           @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        ReseteoDePasswordDTO reseteoDePasswordDTO = new Gson().fromJson(bodyDesencriptado, ReseteoDePasswordDTO.class);

        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = this.reseteoDePasswordService.reseteoDePassword(
                httpServletRequest, reseteoDePasswordDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyEncriptado.toString(),
                df, fechaRequest, EtiquetaNemonico.ACCION_EJECUCION_RESETEO_PASSWORD);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/cancelarReseteoDePassword")
    @Operation(summary = "Cancela el proceso de reseteo de la contraseña de un usuario del sistema")
    public ResponseEntity<BodyEncriptado> cancelarReseteoDePassword(HttpServletRequest httpServletRequest,
                                                                    @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        ReseteoDePasswordDTO reseteoDePasswordDTO = new Gson().fromJson(bodyDesencriptado, ReseteoDePasswordDTO.class);

        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = this.reseteoDePasswordService.cancelarReseteo(
                httpServletRequest, reseteoDePasswordDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_EJECUCION_RESETEO_PASSWORD);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    @PostMapping("/verificarReseteoDePassword")
    @Operation(summary = "Verifica el proceso de reseteo de la contraseña de un usuario del sistema")
    public ResponseEntity<BodyEncriptado> verificarReseteoDePassword(HttpServletRequest httpServletRequest,
                                                                     @RequestBody BodyEncriptado bodyEncriptado) throws Exception {
        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        ReseteoDePasswordDTO reseteoDePasswordDTO = new Gson().fromJson(bodyDesencriptado, ReseteoDePasswordDTO.class);

        RespuestaPorDefectoAuditoria<ReseteoDePasswordDTO> df = this.reseteoDePasswordService.verificarReseteoDePassword(
                httpServletRequest, reseteoDePasswordDTO
        );

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest, bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_EJECUCION_VERIFICACION_RESETEO_CONTRASENIA);
        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}
