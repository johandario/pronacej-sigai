package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ExportInfoAdolescentesService {

    /**
     * Genera un archivo CSV con información de adolescentes para exportar.
     * Soporta >10K registros utilizando streaming de datos.
     *
     * @param httpServletRequest objeto con datos de petición
     * @param bodyEncriptado     objeto que contiene body encriptado (puede contener filtros)
     * @return                   respuesta con bytes del CSV encriptado
     */
    RespuestaPorDefectoAuditoria<byte[]> exportarAdolescentes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}

