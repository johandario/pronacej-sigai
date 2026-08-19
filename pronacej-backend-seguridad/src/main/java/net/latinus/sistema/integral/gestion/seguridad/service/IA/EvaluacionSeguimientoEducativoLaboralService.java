package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSeguimientoEducativoLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.RecomendacionComentarioPorEvalSeguDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

public interface EvaluacionSeguimientoEducativoLaboralService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionSeguimientoEducativoLaboralDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     PaginacionRequest datos para obtener todas las evaluaciones y seguimientos.
     * @return RespuestaPorDefectoAuditoria<EvaluacionSeguimientoEducativoLaboralDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>> obtenerEvaluacionesSeguimiento(HttpServletRequest httpServletRequest,
                                                                                                                              BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data RecomendacionComentarioPorEvalSeguDTO Paginada
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     PaginacionRequest datos para obtener todas las recomendaciones y comentarios.
     * @return RespuestaPorDefectoAuditoria<RecomendacionComentarioPorEvalSeguDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO>> obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EvaluacionSeguimientoEducativoLaboralDTO si la evaluación y seguimiento se creó con éxito
     *
     * @param httpServletRequest request petición.
     * @param bodyEncriptado     objeto EvaluacionSeguimientoEducativoLaboralDTO a crear.
     * @return RespuestaPorDefectoAuditoria<EvaluacionSeguimientoEducativoLaboralDTO>
     */
    RespuestaPorDefectoAuditoria<EvaluacionSeguimientoEducativoLaboralDTO> crearEvaluacionSeguimiento(HttpServletRequest httpServletRequest,
                                                                                                      BodyEncriptado bodyEncriptado);

    /**
     * Elimina una evaluación y seguimiento del sistema
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado     PaginacionRequest datos de la evaluación y seguimiento a eliminar.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Elimina lógicamente una recomendación/comentario específico de una evaluación y seguimiento.
     * La eliminación es lógica, lo que significa que el registro se marca como removido pero no se elimina físicamente de la base de datos.
     *
     * @param httpServletRequest HttpServletRequest datos del request para obtener información del usuario y la sesión
     * @param bodyEncriptado     BodyEncriptado contiene los datos encriptados de la recomendación/comentario a eliminar
     * @return RespuestaPorDefectoAuditoria<Boolean> retorna true si la eliminación fue exitosa, false en caso contrario
     * junto con los mensajes de auditoría correspondientes
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarRecomendacionComentario(HttpServletRequest httpServletRequest,
                                                                          BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si el documento se sube con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest,
                                                          BodyEncriptado bodyEncriptado,
                                                          MultipartFile[] multipartFiles);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PaginacionResponse<DocumentoDTO>
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                     BodyEncriptado bodyEncriptado);

}