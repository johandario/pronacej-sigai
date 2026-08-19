package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.HistorialDeFotosFichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.HistorialDeFotosFichaIdentificacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface HistorialDeFotosFichaIdentificacionService {

    /**
     * Sube un archivo al historial de archivos de la ficha de identificacion
     *
     * @param httpServletRequest                     HttpServletRequest datos del request.
     * @param multipartFile                          MultipartFile archivo enviado
     * @param historialDeFotosFichaIdentificacionDTO HistorialDeFotosFichaIdentificacionDTO datos adicionales del archivo
     *
     * @return RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> subirArchivoAlHistorial(
            HttpServletRequest httpServletRequest, MultipartFile multipartFile,
            HistorialDeFotosFichaIdentificacionDTO historialDeFotosFichaIdentificacionDTO
    );

    /**
     * Obten el historial de las fotos subidas en la ficha de identificacion paginadas
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param historialDeFotosFichaIdentificacionRequest HistorialDeFotosFichaIdentificacionRequest
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>> obtener(
            HttpServletRequest httpServletRequest, HistorialDeFotosFichaIdentificacionRequest historialDeFotosFichaIdentificacionRequest
    );

    /**
     * Elimina la relacion con el documento
     *
     * @param httpServletRequest                     HttpServletRequest datos del request.
     * @param historialDeFotosFichaIdentificacionDTO HistorialDeFotosFichaIdentificacionDTO datos adicionales del archivo
     *
     * @return RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO>
     */
    RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> eliminarRelacionConElDocumento(
            HttpServletRequest httpServletRequest, HistorialDeFotosFichaIdentificacionDTO historialDeFotosFichaIdentificacionDTO
    );

    /**
     * Obteniene la ultima foto frontal relacionada a la fichaIdentificacion
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado tokenIdentificador de la ficha
     *
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>>
     */
    RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> obtenerFotoPerfil(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado
    );
}
