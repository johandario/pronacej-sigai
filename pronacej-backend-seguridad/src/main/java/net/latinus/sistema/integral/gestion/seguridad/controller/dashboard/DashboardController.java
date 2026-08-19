package net.latinus.sistema.integral.gestion.seguridad.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard.DashboardCentroDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard.DashboardEstadisticasDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.dashboard.DashboardService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaAccionesSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/dashboard")
@SecurityRequirement(name = "Authorization")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final AuditoriaAccionesSistemaService auditoriaAccionesSistemaService;

    /**
     * Devuelve la lista de centros disponibles para el selector del dashboard.
     * Los centros son Jerarquías hijas cuyo padre tiene nemónico SOA, CJDR o UAPISE.
     * La empresa se obtiene directamente del JWT — no se requiere cuerpo cifrado.
     */
    @PostMapping("/centros")
    @Operation(summary = "Obtener centros disponibles para el dashboard")
    public ResponseEntity<BodyEncriptado> obtenerCentros(
            HttpServletRequest httpServletRequest) throws Exception {

        Date fechaInicio = new Date();

        RespuestaPorDefectoAuditoria<List<DashboardCentroDTO>> resp =
                this.dashboardService.obtenerCentros(httpServletRequest);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                "",          // sin body de entrada
                resp,
                fechaInicio,
                EtiquetaNemonico.ACCION_LISTAR_DASHBOARD_CENTROS);

        return ResponseEntity.ok(
                resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }

    /**
     * Devuelve el conjunto de estadísticas agregadas para el centro seleccionado
     * (o para todos los centros de la empresa si no se envía tokenCentro).
     * El cuerpo cifrado debe contener un objeto {@code DashboardRequest}.
     */
    @PostMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de adolescentes por centro para el dashboard")
    public ResponseEntity<BodyEncriptado> obtenerEstadisticas(
            HttpServletRequest httpServletRequest,
            @RequestBody BodyEncriptado bodyEncriptado) throws Exception {

        Date fechaInicio = new Date();
        String bodyDesencriptado = bodyEncriptado
                .desencriptarPorEmpresa(this.parametroDelSistemaRepository, null)
                .getData();

        RespuestaPorDefectoAuditoria<DashboardEstadisticasDTO> resp =
                this.dashboardService.obtenerEstadisticas(httpServletRequest, bodyEncriptado);

        this.auditoriaAccionesSistemaService.guardarAccionRequestEncriptado(
                httpServletRequest,
                bodyDesencriptado,
                resp,
                fechaInicio,
                EtiquetaNemonico.ACCION_LISTAR_DASHBOARD_ESTADISTICAS);

        return ResponseEntity.ok(
                resp.transFormarEnbodyEncriptado(this.parametroDelSistemaRepository, null));
    }
}

