package net.latinus.sistema.integral.gestion.seguridad.model.both;

import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface CheckCamposRequeridos {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<Boolean> chequea los campos requeridos en el DTO
     **
     * @return RespuestaPorDefectoAuditoria<Boolean> true si paso, false si no
     */
    public RespuestaPorDefectoAuditoria<Boolean> chequearValoresRequeridos();
}
