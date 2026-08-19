package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaCentroEstadisticaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;
import java.util.Map;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;

public interface JerarquiaService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data JerarquiaDTO
     *
     * @param httpServletRequest request peticion.
     *
     * @return RespuestaPorDefectoAuditoria<JerarquiaDTO>
     */
    RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquias(HttpServletRequest httpServletRequest);
    
    /**
     * Devuelve una lista de jerarquías filtradas por nemónico padre
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado DTO encriptado con el nemónico padre
     *
     * @return RespuestaPorDefectoAuditoria<List<JerarquiaDTO>>
     */
    RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorNemonicoPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un mapa de jerarquías agrupadas por nemónico padre.
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado DTO encriptado con la lista de nemónicos padre
     *
     * @return RespuestaPorDefectoAuditoria<Map<String, List<JerarquiaDTO>>>
     */
    RespuestaPorDefectoAuditoria<Map<String, List<JerarquiaDTO>>> obtenerJerarquiasPorNemonicoPadreLista(HttpServletRequest httpServletRequest,
                                                                                                          BodyEncriptado bodyEncriptado);
    
    /**
     * Devuelve una lista de jerarquías filtradas por nemónico padre con estructura jerárquica completa (hijos anidados recursivamente)
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado DTO encriptado con el nemónico padre
     *
     * @return RespuestaPorDefectoAuditoria<List<JerarquiaDTO>>
     */
    RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorNemonicoPadreCompleto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);
    
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data JerarquiaDTO si la jerarquia se encontró con exito
     *
     * @param httpServletRequest request petición.
     *
     * @return RespuestaPorDefectoAuditoria<JerarquiaDTO>
     */
    RespuestaPorDefectoAuditoria<JerarquiaDTO> obtenerJerarquiaPorNumeroDeDocumento(HttpServletRequest httpServletRequest);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data JerarquiaDTO si la jerarquia se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param jerarquiaDTO objeto jerarquia dto.
     *
     * @return RespuestaPorDefectoAuditoria<EmpresaDTO>
     */
    RespuestaPorDefectoAuditoria<JerarquiaDTO> crearJerarquia(HttpServletRequest httpServletRequest,
                                                              JerarquiaDTO jerarquiaDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data JerarquiaDTO si la empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param jerarquiaDTO objeto jerarquia dto.
     *
     * @return RespuestaPorDefectoAuditoria<EmpresaDTO>
     */
    RespuestaPorDefectoAuditoria<JerarquiaDTO> actualizarJerarquia(HttpServletRequest httpServletRequest,
                                                              JerarquiaDTO jerarquiaDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data JerarquiaDTO si la empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param jerarquiaDTO objeto jerarquia dto.
     *
     * @return RespuestaPorDefectoAuditoria<EmpresaDTO>
     */
    RespuestaPorDefectoAuditoria<JerarquiaDTO> removerJerarquia(HttpServletRequest httpServletRequest,
                                                              JerarquiaDTO jerarquiaDTO);

    /**
     * Devuelve una lista de jerarquías filtradas por nemónico padre
     *
     * @param httpServletRequest request peticion.
     *
     * @return RespuestaPorDefectoAuditoria<List<JerarquiaDTO>>
     */
    RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorJerarquiaPadreFuncionario(HttpServletRequest httpServletRequest);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data JerarquiaDTO por tokenIdentificador de la jerarquiaPadre
     *
     * @param httpServletRequest request peticion.
     *
     * @return RespuestaPorDefectoAuditoria<JerarquiaDTO>
     */
    RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorTokenPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    RespuestaPorDefectoAuditoria<List<FichaCentroEstadisticaDTO>> obtenerEstadisticasFichasPorCentro(HttpServletRequest httpServletRequest,
                                                                                                     BodyEncriptado bodyEncriptado);
}
