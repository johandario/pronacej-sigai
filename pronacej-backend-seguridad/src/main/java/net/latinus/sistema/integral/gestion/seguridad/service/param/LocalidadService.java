package net.latinus.sistema.integral.gestion.seguridad.service.param;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosFamiliaresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.LocalidadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface LocalidadService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de localidades
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto localidad que contiene los campos para la busqueda.
     *
     * @return RespuestaPorDefectoAuditoria<List<LocalidadDTO>>
     */
    RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerLocalidadPorNemonicPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de localidades
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto localidad que contiene los campos para la busqueda.
     *
     * @return RespuestaPorDefectoAuditoria<List<LocalidadDTO>>
     */
    RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerLocalidadPorNemonicTipo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data DatosFamiliaresDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado ubigeo de la localidad.
     *
     * @return RespuestaPorDefectoAuditoria<LocalidadDTO>
     */
    RespuestaPorDefectoAuditoria<LocalidadDTO> obtenerLocalidadUbigeo(HttpServletRequest httpServletRequest,
                                                                                 BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el arbol de localidades
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado objeto localidad que contiene los campos para la busqueda.
     *
     * @return RespuestaPorDefectoAuditoria<List<LocalidadDTO>>
     */
    RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerArbolPorNemonicPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data DatosFamiliaresDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado tokenIdentificador de la localidad.
     *
     * @return RespuestaPorDefectoAuditoria<LocalidadDTO>
     */
    RespuestaPorDefectoAuditoria<LocalidadDTO> obtenerLocalidadTokenIdentificador(HttpServletRequest httpServletRequest,
                                                                      BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerDescendencia(HttpServletRequest httpServletRequest,
                                                                        String tokenIdentificador);

    RespuestaPorDefectoAuditoria<LocalidadDTO> crearLocalidad(HttpServletRequest httpServletRequest,
                                                                          BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<LocalidadDTO> obtenerLocalidadPorNemonico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    RespuestaPorDefectoAuditoria<LocalidadDTO> editarLocalidad(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
}
