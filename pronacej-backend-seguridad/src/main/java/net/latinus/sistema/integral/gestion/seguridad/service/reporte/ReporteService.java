package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.AdolescenteExternadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface ReporteService {

    /**
     * Obtiene los adolescentes externados.
     *
     * @param httpServletRequest    objeto con datos de petición
     * @param bodyEncriptado        objeto que contiene body encriptado
     * @return                      lista paginadad de adolescentes externados
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<AdolescenteExternadoDTO>> obtenerAdolescentesExternados(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
