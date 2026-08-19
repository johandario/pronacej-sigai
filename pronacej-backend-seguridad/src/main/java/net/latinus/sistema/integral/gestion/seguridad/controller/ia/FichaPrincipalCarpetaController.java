package net.latinus.sistema.integral.gestion.seguridad.controller.ia;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaIdentificacionCarpetaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.doc.ContenidoCarpetaResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.IA.FichaIdentificacionCarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/ficha-principal-carpeta")
@SecurityRequirement(name = "Authorization")
public class FichaPrincipalCarpetaController {

    private FichaIdentificacionCarpetaService fichaIdentificacionCarpetaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/obtenerInformacionDeCarpeta")
    @Operation(summary = "Obten los datos de la carpeta de una seccion de la ficha principal")
    public ResponseEntity<BodyEncriptado> obtenerInformacionDeCarpeta(HttpServletRequest httpServletRequest,
                                                                      @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaRequest = new Date();
        String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
        FichaIdentificacionCarpetaRequest fichaIdentificacionCarpetaRequest = new Gson().fromJson(bodyDesencriptado, FichaIdentificacionCarpetaRequest.class);
        RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse> df = this.fichaIdentificacionCarpetaService.
                obtenerInformacionDeCarpetaPrincipalDeLaFichaDeIndentificacion(
                httpServletRequest, fichaIdentificacionCarpetaRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                df, fechaRequest, EtiquetaNemonico.ACCION_USUARIO_FICHA_PRINCIPAL_OBTENER_DATO_CARPETA
        );

        BodyEncriptado body = df.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null);
        return ResponseEntity.ok(body);
    }
}
