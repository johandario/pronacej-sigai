package net.latinus.sistema.integral.gestion.seguridad.service.IA;


import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SancionDisciplinariaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SancionDisciplinariaService {


    /**
     * Obtiene una sanción disciplinaria por su token identificador.
     *
     * @param httpServletRequest Request HTTP.
     * @param tokenIdentificador Token identificador único del registro.
     * @return Sanción encontrada.
     */
    RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> obtenerSancionPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador);

    /**
     * Crea una nueva sanción disciplinaria.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado Datos encriptados del formulario.
     * @return Sanción creada.
     */
    RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> crearSancion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Elimina lógicamente una sanción disciplinaria.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado Contiene ID/token encriptado.
     * @return Estado de eliminación.
     */
    RespuestaPorDefectoAuditoria<Boolean> eliminarSancion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve una lista paginada de sanciones por ficha de identificación.
     *
     * @param httpServletRequest Request HTTP.
     * @param bodyEncriptado Contiene el token de ficha y filtros opcionales.
     * @return Lista paginada de sanciones.
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<SancionDisciplinariaDTO>> obtenerListadoPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Sube un documento y lo asocia al registro respectivo de evaluación domiciliaria
     *
     * @param httpServletRequest Solicitud HTTP
     * @param bodyEncriptado     Cuerpo encriptado con los datos de relación
     * @param multipartFiles     Archivo a subir
     * @return RespuestaPorDefectoAuditoria<DocumentoDTO>
     */
    RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest,
                                                          BodyEncriptado bodyEncriptado,
                                                          MultipartFile[] multipartFiles);

    /**
     * Obtiene todos los documentos asociados al registro de evaluación domiciliaria
     *
     * @param httpServletRequest Solicitud HTTP
     * @param bodyEncriptado     Cuerpo encriptado con los datos de relación
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse <DocumentoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                     BodyEncriptado bodyEncriptado);

}
