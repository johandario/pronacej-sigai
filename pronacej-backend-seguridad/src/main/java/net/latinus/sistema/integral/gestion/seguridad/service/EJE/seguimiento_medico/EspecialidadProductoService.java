package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EspecialidadProductoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface EspecialidadProductoService {

    /**
     * Devuelve una lista de especialidades/productos.
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado body de la peticion.
     * @return RespuestaPorDefectoAuditoria<List<EspecialidadProductoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<EspecialidadProductoDTO>> obtenerEspecialidadProductos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
