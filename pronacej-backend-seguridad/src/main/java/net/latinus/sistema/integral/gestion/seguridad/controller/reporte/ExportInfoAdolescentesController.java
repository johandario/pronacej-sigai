package net.latinus.sistema.integral.gestion.seguridad.controller.reporte;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.reporte.ExportInfoAdolescentesService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/reporte")
@SecurityRequirement(name = "Authorization")
public class ExportInfoAdolescentesController {

    private final ExportInfoAdolescentesService exportInfoAdolescentesService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    @PostMapping("/exportarAdolescentes")
    @Operation(summary = "Exporta información de adolescentes a CSV")
    public ResponseEntity<?> exportarAdolescentes(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();

        try {
            // Desencriptar body para auditoría
            String bodyDesencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                    .getData();

            // Llamar al servicio de exportación
            RespuestaPorDefectoAuditoria<byte[]> resp = this.exportInfoAdolescentesService.exportarAdolescentes(
                    httpServletRequest,
                    bodyEncriptado
            );

            // Registrar auditoría - se omite el data (byte[]) para evitar serializar el CSV completo
            RespuestaPorDefectoAuditoria<String> respAuditoria = new RespuestaPorDefectoAuditoria<>();
            respAuditoria.setExito(resp.isExito());
            respAuditoria.setMensaje(resp.getMensaje());
            respAuditoria.setMensajeAuditoria(resp.getMensajeAuditoria());
            respAuditoria.setTitulo(resp.getTitulo());
            respAuditoria.setCodigoEstado(resp.getCodigoEstado());
            respAuditoria.setSinAcceso(resp.getSinAcceso());
            respAuditoria.setTokenIdentificadorEmpresa(resp.getTokenIdentificadorEmpresa());
            respAuditoria.setData(resp.isExito() ? "CSV generado correctamente (" + (resp.getData() != null ? resp.getData().length : 0) + " bytes)" : null);

            this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                    httpServletRequest,
                    bodyDesencriptado,
                    respAuditoria,
                    fechaInicio,
                    EtiquetaNemonico.ACCION_EXPORTAR_ADOLESCENTES
            );

            // Si la respuesta no es exitosa, retornar encriptada
            if (!resp.isExito()) {
                return ResponseEntity.ok(resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
            }

            // Si es exitosa, retornar el CSV directamente descargable
            byte[] csvBytes = resp.getData();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"adolescentes.csv\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(csvBytes.length))
                    .body(csvBytes);

        } catch (Exception e) {
            RespuestaPorDefectoAuditoria<byte[]> respError = new RespuestaPorDefectoAuditoria<>();
            respError.llenarConDatosDeException(e);

            // Se usa una copia sin data para la auditoría (previene serializar binarios innecesariamente)
            RespuestaPorDefectoAuditoria<String> respErrorAuditoria = new RespuestaPorDefectoAuditoria<>();
            respErrorAuditoria.llenarConDatosDeException(e);

            this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                    httpServletRequest,
                    "",
                    respErrorAuditoria,
                    fechaInicio,
                    EtiquetaNemonico.ACCION_EXPORTAR_ADOLESCENTES
            );

            return ResponseEntity.ok(respError.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
        }
    }
}

