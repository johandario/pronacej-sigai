package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionEstadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionJobIniciadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ExportInfoAdolescentesService {
    public RespuestaPorDefectoAuditoria<ExportacionJobIniciadoDTO> iniciarExportacion(HttpServletRequest var1, BodyEncriptado var2);

    public RespuestaPorDefectoAuditoria<ExportacionEstadoDTO> consultarEstadoExportacion(HttpServletRequest var1, BodyEncriptado var2);

    public RespuestaPorDefectoAuditoria<List<ExportacionEstadoDTO>> listarExportaciones(HttpServletRequest var1, BodyEncriptado var2);

    public RespuestaPorDefectoAuditoria<Void> cancelarExportacion(HttpServletRequest var1, BodyEncriptado var2);

    public RespuestaPorDefectoAuditoria<Void> descartarExportacion(HttpServletRequest var1, BodyEncriptado var2);
}
