package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.MedicamentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface MedicamentoService {

    /**
     * Devuelve una lista de medicamentos
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado body de la peticion.
     * @return RespuestaPorDefectoAuditoria<List<MedicamentoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<MedicamentoDTO>> obtenerMedicamentos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

}
