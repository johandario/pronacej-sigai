package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaIdentificacionCarpetaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.doc.ContenidoCarpetaResponse;

public interface FichaIdentificacionCarpetaService {

    /**
     * Obten la carpeta de una ficha principal
     *
     * @param httpServletRequest HttpServletRequest peticion.
     * @param fichaIdentificacionCarpetaRequest FichaIdentificacionCarpetaRequest
     *
     * @return RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse>
     */
    RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse> obtenerInformacionDeCarpetaPrincipalDeLaFichaDeIndentificacion(HttpServletRequest httpServletRequest,
                                                                                                                          FichaIdentificacionCarpetaRequest fichaIdentificacionCarpetaRequest);
}
