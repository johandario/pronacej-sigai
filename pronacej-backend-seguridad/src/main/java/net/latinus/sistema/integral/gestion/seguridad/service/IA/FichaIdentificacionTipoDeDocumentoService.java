package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionTipoDeDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.TipoDeArchivoSeccionFichaPrincipal;

import java.util.List;

public interface FichaIdentificacionTipoDeDocumentoService {

    /**
     * Devuelve una lista de
     *
     * @param httpServletRequest String token identificador de la ficha de identificacion.
     * @param nemonicoSeccion String
     *
     * @return RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>> obtenerTiposDeDocumentosDeUnaSeccionDeLaFichaPrincipal(
            HttpServletRequest httpServletRequest, String nemonicoSeccion
    );


    /**
     * Devuelve una lista de se secciones de ficha principal con la cantidad total de tipos de archivos
     *
     * @param httpServletRequest String token identificador de la ficha de identificacion.
     *
     * @return RespuestaPorDefectoAuditoria<List<TipoDeArchivoSeccionFichaPrincipal>>
     */
    RespuestaPorDefectoAuditoria<List<TipoDeArchivoSeccionFichaPrincipal>> obtenerSeccionDefichaPrincipalConTotalDeTipoDeDocumentos(
            HttpServletRequest httpServletRequest);


    /**
     * Devuelve una lista de FichaIdentificacionTipoDeDocumentoDTO pr token de seccion de ficha principal
     *
     * @param httpServletRequest String token identificador de la ficha de identificacion.
     *
     * @return RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>>
    obtenerPorSeccionFichaPrincipal(HttpServletRequest httpServletRequest,
                                    String tokenSeccionFichaPrinicipal);


    /**
     * Crea una nueva seccion de la ficha principal con un nuevo tipo de documento del sistema
     *
     * @param httpServletRequest String token identificador de la ficha de identificacion.
     * @param fichaIdentificacionTipoDeDocumentoDTO FichaIdentificacionTipoDeDocumentoDTO
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO>
    crear(HttpServletRequest httpServletRequest,
          FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO);


    /**
     * Edita una seccion de la ficha principal con un nuevo tipo de documento del sistema
     *
     * @param httpServletRequest String token identificador de la ficha de identificacion.
     * @param fichaIdentificacionTipoDeDocumentoDTO FichaIdentificacionTipoDeDocumentoDTO
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO>
    editar(HttpServletRequest httpServletRequest,
          FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO);


    /**
     * Elimina una seccion de la ficha principal con un nuevo tipo de documento del sistema
     *
     * @param httpServletRequest String token identificador de la ficha de identificacion.
     * @param fichaIdentificacionTipoDeDocumentoDTO FichaIdentificacionTipoDeDocumentoDTO
     *
     * @return RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO>
    eliminar(HttpServletRequest httpServletRequest,
           FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO);
}
