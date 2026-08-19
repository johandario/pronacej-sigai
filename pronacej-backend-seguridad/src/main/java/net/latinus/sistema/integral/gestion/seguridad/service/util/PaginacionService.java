package net.latinus.sistema.integral.gestion.seguridad.service.util;

import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;

import java.util.List;

public interface PaginacionService {
    /**
     * Devuelve un RespuestaPorDefectoAuditoria<LoginResponse>
     *
     * @param lista clase de la entidad a paginar.
     * @param paginacionRequest objeto con datos de paginacion.
     *
     * @return PaginacionResponse<T>
     */
    public<T> PaginacionResponse<T> obtenerDatos(List<T> lista, PaginacionRequest paginacionRequest);
}
