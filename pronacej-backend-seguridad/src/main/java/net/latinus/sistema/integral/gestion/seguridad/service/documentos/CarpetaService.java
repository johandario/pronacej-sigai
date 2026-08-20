package net.latinus.sistema.integral.gestion.seguridad.service.documentos;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaIdentificacionCarpetaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.doc.ContenidoCarpetaResponse;

public interface CarpetaService {

    /**
     * crea una carpeta em el servidor de Alfresco, se debe de enviar un padre carpetaDTO para la creación, si no se valida la sesión toma los datos de la empresa de la carpeta DTO
     *
     * @param httpServletRequest string token identificador de la empresa.
     * @param validarSesion      Boolean si se valida la sesion o no
     * @param carpetaDTO         CarpetaDTO objeto que tiene los datos de la carpeta a crear.
     * @return RespuestaPorDefectoAuditoria<CarpetaDTO>
     */
    RespuestaPorDefectoAuditoria<CarpetaDTO> crearCarpeta(HttpServletRequest httpServletRequest,
                                                          boolean validarSesion,
                                                          CarpetaDTO carpetaDTO);


    /**
     * Obten la información de una carpeta (mas la informacion de carpetas hijas y documentos)
     *
     * @param httpServletRequest                HttpServletRequest peticion.
     * @param fichaIdentificacionCarpetaRequest FichaIdentificacionCarpetaRequest
     * @return RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse>
     */
    RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse> obterInformacionDeCarpetaDesdeLaFichaPrincipal(HttpServletRequest httpServletRequest,
                                                                                     FichaIdentificacionCarpetaRequest fichaIdentificacionCarpetaRequest);

    /**
     * Verifica que el nodo de Alfresco de la carpeta exista. Si el UUID guardado en BD
     * quedó huérfano (p. ej. Alfresco recreado), recrea la carpeta bajo su padre
     * (y la cadena de padres si también faltan) y actualiza identificadorAlfresco.
     *
     * @param tokenEmpresa token de la empresa (credenciales Alfresco)
     * @param carpeta      carpeta de BD a asegurar en Alfresco
     * @return carpeta con identificadorAlfresco válido en este Alfresco
     */
    RespuestaPorDefectoAuditoria<Carpeta> asegurarExistenciaEnAlfresco(String tokenEmpresa, Carpeta carpeta);

}
