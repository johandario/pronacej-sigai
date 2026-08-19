package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.HistoricoEntradaSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.HistoricoEntradaSalidaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
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
@RequestMapping(path = "api/v1/historico-entrada-salida")
@SecurityRequirement(name = "Authorization")
public class HistoricoEntradaSalidaController {

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;
    private HistoricoEntradaSalidaService historicoEntradaSalidaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @PostMapping("/obtenerHistoricoSalidaActivo")
    @Operation(summary = "Obtiene una ficha de identificacion por su token Identificador")
    public ResponseEntity<BodyEncriptado> obtenerHistoricoSalidaActivo(HttpServletRequest httpServletRequest,
                                                                                            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();

        RespuestaPorDefectoAuditoria<HistoricoEntradaSalidaDTO> df = this.historicoEntradaSalidaService.obtenerHistoricoEntradaSalida(httpServletRequest, bodyEncriptado);
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest, body, df,
                fechaRequest, EtiquetaNemonico.ACCION_OBTENER_FICHA_IDENTIFICACION_POR_TOKEN
        );

        return ResponseEntity.ok(df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

}
