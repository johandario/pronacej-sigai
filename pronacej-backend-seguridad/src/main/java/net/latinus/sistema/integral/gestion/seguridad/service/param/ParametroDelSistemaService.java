package net.latinus.sistema.integral.gestion.seguridad.service.param;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ParametroDelSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface ParametroDelSistemaService {
    /**
     * Devuelve un objeto parametro del sistema
     *
     * @param nemonico String nemonico del parametro del sistema.
     * @param idEmpresa Long id de la empresa.
     *
     * @return ParametroDelSistema
     */
    ParametroDelSistema encontrarPorNemonicoYEmpresa(String nemonico, Long idEmpresa);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con cada una de las respuestas de la creacion de un parametro del sistema
     *
     * @param httpServletRequest request peticion.
     * @param parametroDelSistemaDTOList objeto parametros del sistema para crear.
     *
     * @return RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<CatalogoDTO>>>
     */
    RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>>> crearVariosDirecto(HttpServletRequest httpServletRequest,
                                                                                                                         List<ParametroDelSistemaDTO> parametroDelSistemaDTOList);

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> parametro del sistema
     *
     * @param nemonico String nemonico del parametro del sistema.
     * @param tokenIdentificadorEmpresa String token identificador de la empresa.
     *
     * @return RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>
     */
    RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> encontrarPorNemonicoYEmpresa2(String nemonico, String tokenIdentificadorEmpresa);


    /**
     * Devuelve una lista de parametros del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado BodyEncriptado datos del parametro de sistema padre ParametroDelSistemaDTO.
     *
     * @return RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>
     */
    RespuestaPorDefectoAuditoria<List<ParametroDelSistemaDTO>> obtenerParametrosDelSistemaGenerales(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado
    );


    /**
     * Obten el valor del parametro del sistema en Base 64 o en texto
     *
     * @param nemonico String nemonico del parametro del sistema.
     * @param tokenIdentificadorEmpresa String token identificador de la empresa.
     * @param base64 Boolean
     *
     * @return RespuestaPorDefectoAuditoria<String>
     */
    RespuestaPorDefectoAuditoria<String> obtenerValorParam(String nemonico, String tokenIdentificadorEmpresa, Boolean base64);

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>
     *
     * @param httpServletRequest HttpServletRequest.
     * @param nemonico String nemonico del parametro del sistema.
     *
     * @return RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>
     */
    RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> obtenerPorNemonico(HttpServletRequest httpServletRequest,
                                                                            String nemonico);
}
