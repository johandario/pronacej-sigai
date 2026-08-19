package net.latinus.sistema.integral.gestion.seguridad.service.general;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta.EncabezadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta.EncuestaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EncuestaService {
    // region Encuesta

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado request peticion.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EncuestaDTO>> obtenerListaEncuestas(HttpServletRequest httpServletRequest,
                                                                                        BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado request peticion.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<List<EncuestaDTO>> obtenerEncuestas(HttpServletRequest httpServletRequest,
                                                                     BodyEncriptado bodyEncriptado);

        /**
         * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncabezadoDTO
         *
         * @param httpServletRequest request peticion.
         * @param bodyEncriptado     objeto body encriptado.
         * @return RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>>
         */
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> obtenerEvaluacionesPorFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                                                                                 BodyEncriptado bodyEncriptado, String nemonicoCategoria);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncabezadoDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto body encriptado.
     * @param nemonicoEncuesta ficha identificacion.
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>>
     */
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> obtenerEvaluacionesPorNemonicoEncuesta(HttpServletRequest httpServletRequest,
                                                                                                                 BodyEncriptado bodyEncriptado, String nemonicoEncuesta);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncabezadoDTO
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto body encriptado.
     * @param nemonicosCategoria lista de nemonicos a ser incluidos en la respuesta.
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>>
     */
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> obtenerEvaluacionesPorNemonicoCategoria(HttpServletRequest httpServletRequest,
                                                                                                                  BodyEncriptado bodyEncriptado, String nemonicoCentro, List<String> nemonicosCategoria );


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<EncuestaDTO> obtenerEncuestaPorTokenEncuesta(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<EncuestaDTO> obtenerEvaluacionPorTokenEncabezado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<EncuestaDTO> obtenerEncuestaPorId(HttpServletRequest httpServletRequest,
            EncuestaDTO encuestaDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param encuestaDTO        objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<EncuestaDTO> crearEncuesta(HttpServletRequest httpServletRequest,
            EncuestaDTO encuestaDTO);
    
    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<Boolean> crearEvaluacion(HttpServletRequest httpServletRequest,
                                                          BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EncuestaDTO si la
     * empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarEncuesta(HttpServletRequest httpServletRequest,
                                                             BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si la
     * encuesta se remueve con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<EncuestaDTO>
     */
    RespuestaPorDefectoAuditoria<Boolean> removerEncuesta(HttpServletRequest httpServletRequest,
                                                          BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si la
     * evaluacion se remueve con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado        objeto encabezado dto.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> removerEvaluacion(HttpServletRequest httpServletRequest,
                                                          BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si el documento se sube con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado         objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> subirDocumento(HttpServletRequest httpServletRequest,
                                                              MultipartFile[] multipartFile,
                                                              BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PaginacionResponse<DocumentoDTO>
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado         objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                     BodyEncriptado bodyEncriptado);

    // endregion
}
