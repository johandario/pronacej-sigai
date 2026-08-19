package net.latinus.sistema.integral.gestion.seguridad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefecto;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.ServiciosExternos;
import net.latinus.sistema.integral.gestion.seguridad.service.UtilsService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.AlfrescoService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@SecurityRequirement(name = "Authorization")
@RequestMapping(path = "api/v1/utils")
public class UtilsController {

    private UtilsService utilsService;
    private ServiciosExternos serviciosExternos;
    private AlfrescoService alfrescoService;

    @PostMapping("/crearBodyEncriptadoFront")
    @Operation(summary = "Crea un body encriptado del front end con el string enviado")
    public ResponseEntity<RespuestaPorDefecto<BodyEncriptado>> crearDirecto(HttpServletRequest httpServletRequest,
                                                                            @RequestBody String body) {

        return ResponseEntity.ok(this.utilsService.crearBodyEncriptado(httpServletRequest, body, EtiquetaNemonico.PARAM_RSA_CLAVE_PUBLICA_BACKEND));
    }

    @PostMapping("/crearBodyEncriptadoBackend")
    @Operation(summary = "Crea un body encriptado del backend con el string enviado")
    public ResponseEntity<RespuestaPorDefecto<BodyEncriptado>> crearBodyEncriptadoBackend(HttpServletRequest httpServletRequest,
                                                                                          @RequestBody String body) {
        return ResponseEntity.ok(this.utilsService.crearBodyEncriptado(httpServletRequest, body, EtiquetaNemonico.PARAM_RSA_CLAVE_PUBLICA_FRONTEND));
    }

    @PostMapping("/desencriptarBodyEncriptadoBackend")
    @Operation(summary = "Desencripta un body encriptado enviado desde el backend")
    public ResponseEntity<RespuestaPorDefecto<String>> desencriptarBodyEncriptadoBackend(HttpServletRequest httpServletRequest,
                                                                                         @RequestBody BodyEncriptado body) {

        return ResponseEntity.ok(this.utilsService.desecriptarBodyEncriptado(httpServletRequest, body,
                EtiquetaNemonico.PARAM_RSA_CLAVE_PRIVADA_FRONTEND));
    }

    @PostMapping("/desencriptarBodyEncriptadoFrontEnd")
    @Operation(summary = "Desencripta un body encriptado enviado desde el frontend")
    public ResponseEntity<RespuestaPorDefecto<String>> desencriptarBodyEncriptadoFrontEnd(HttpServletRequest httpServletRequest,
                                                                                          @RequestBody BodyEncriptado body) {

        return ResponseEntity.ok(this.utilsService.desecriptarBodyEncriptado(httpServletRequest, body, EtiquetaNemonico.PARAM_RSA_CLAVE_PRIVADA_BACKEND));
    }

    @PostMapping("/encriptarAes")
    @Operation(summary = "Encripta un texto con aes")
    public ResponseEntity<RespuestaPorDefectoAuditoria<String>> encriptarBodyEncriptadoBackend(HttpServletRequest httpServletRequest,
                                                                                               @RequestBody String texto) {

        return ResponseEntity.ok(this.utilsService.encriptarConAes(httpServletRequest, texto));
    }

    @PostMapping("/desencriptarAes")
    @Operation(summary = "Desencriptar con aes un body encriptado")
    public ResponseEntity<RespuestaPorDefectoAuditoria<String>> desencriptarBodyEncriptadoBackend(HttpServletRequest httpServletRequest,
                                                                                                  @RequestBody String texto) {

        return ResponseEntity.ok(this.utilsService.desencriptarConAes(httpServletRequest, texto));
    }

    @PostMapping("/generarPdfFormulario")
    @Operation(summary = "Devuelve archivo PDF del formulario indicado")
    public ResponseEntity<RespuestaPorDefectoAuditoria<byte[]>> generarPdfFormulario(HttpServletRequest httpServletRequest,
                                                                                     @RequestBody BodyEncriptado body) {

        return ResponseEntity.ok(this.utilsService.generarPdfFormulario(httpServletRequest, body));
    }


    @GetMapping("/data")
    @Operation(summary = "Devuelve archivo PDF del formulario indicado")
    public ResponseEntity<String> data(HttpServletRequest httpServletRequest,
                                       @RequestParam String dni) {

        return this.serviciosExternos.data(httpServletRequest, dni).getData();
    }

    @GetMapping("/dataSunat")
    @Operation(summary = "Devuelve archivo PDF del formulario indicado")
    public ResponseEntity<String> dataSunat(HttpServletRequest httpServletRequest,
                                            @RequestParam String ruc) {

        return this.serviciosExternos.dataSunat(httpServletRequest, ruc).getData();
    }

    @GetMapping("/actualizarCarpetasAlfresco")
    @Operation(summary = "Actualiza las carpetas de Alfresco")
    public ResponseEntity<RespuestaPorDefectoAuditoria<Boolean>> actualizarCarpetasAlfresco(HttpServletRequest httpServletRequest) {

        return ResponseEntity.ok(this.utilsService.actualizarCarpetasAlfresco(httpServletRequest));
    }

    @PostMapping("/refactor")
    public ResponseEntity<RespuestaPorDefectoAuditoria<Boolean>> refactor(
            @RequestBody String jsonArray
    ) {
        JSONArray jsonArray1 = new JSONArray(jsonArray);
        return ResponseEntity.ok(this.alfrescoService.ayuda(jsonArray1));
    }
}
