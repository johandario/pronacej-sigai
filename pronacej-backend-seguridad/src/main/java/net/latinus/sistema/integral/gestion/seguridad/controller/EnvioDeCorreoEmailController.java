package net.latinus.sistema.integral.gestion.seguridad.controller;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.request.EnvioEmailRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/envio-correo")
@SecurityRequirement(name = "Authorization")
public class EnvioDeCorreoEmailController {

    private EmailService emailService;

    @PostMapping("/enviar-correo")
    @Operation(summary = "Sube un archivo al sistema de alfresco")
    public ResponseEntity<RespuestaPorDefectoAuditoria<Boolean>> enviarCorreo(HttpServletRequest httpServletRequest,
                                                                              @RequestParam("documentos") MultipartFile[] multipartFile,
                                                                              @RequestParam("body") String envioEmailRequestString) throws Exception {
        //Date fechaInicio = new Date();
        //ObjectMapper objectMapper = new ObjectMapper();
        //EnvioEmailRequest envioEmailRequest = objectMapper.convertValue(envioEmailRequestString, EnvioEmailRequest.class);
        EnvioEmailRequest envioEmailRequest = new Gson().fromJson(envioEmailRequestString, EnvioEmailRequest.class);
        envioEmailRequest.setMultipartFiles(multipartFile);
        RespuestaPorDefectoAuditoria<Boolean> df = this.emailService.enviarCorreoPrueba(httpServletRequest, envioEmailRequest);

        /*
        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(httpServletRequest,
                request, df, fechaInicio, "ENVIO_CORREO_DIRECTO");*/

        return ResponseEntity.ok(df);
    }
}
