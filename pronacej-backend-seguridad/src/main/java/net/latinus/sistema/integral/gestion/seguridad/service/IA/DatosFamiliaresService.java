package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosFamiliaresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface DatosFamiliaresService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data DatosFamiliaresDTO si el datoFamiliar se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param datosFamiliaresDTO objeto datoFamiliar  dto.
     *
     * @return RespuestaPorDefectoAuditoria<DatosFamiliaresDTO>
     */
    public RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> crearDatosFamiliares(HttpServletRequest httpServletRequest, DatosFamiliaresDTO datosFamiliaresDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data DatosFamiliaresDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado tokenIdentificador ficha.
     *
     * @return RespuestaPorDefectoAuditoria<DatosFamiliaresDTO>
     */
    RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> obtenerDatosFamiliaresToken(HttpServletRequest httpServletRequest,
                                                                                          BodyEncriptado bodyEncriptado);

}
