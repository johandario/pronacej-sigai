package net.latinus.sistema.integral.gestion.seguridad.service.param;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ClasificacionEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface ClasificacionEnfermedadService {

    /**
     * Devuelve una lista de clasificación internacional de enfermedades
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado body de la peticion.
     * @return RespuestaPorDefectoAuditoria<List<ClasificacionEnfermedadDTO>>
     */
    RespuestaPorDefectoAuditoria<List<ClasificacionEnfermedadDTO>> obtenerClasificacionEnfermerdades(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
