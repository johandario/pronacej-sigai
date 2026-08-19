package net.latinus.sistema.integral.gestion.seguridad.service.general;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.CampoInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.InformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.PlantillaInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InformeService {

    //region Informe

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<InformeDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> obtenerInformes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<InformeDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> obtenerInformesPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<InformeDTO>
     */
    RespuestaPorDefectoAuditoria<InformeDTO> obtenerInformePorId(HttpServletRequest httpServletRequest,
                                                                 InformeDTO informeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeDTO si la encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param informeDTO         objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<InformeDTO>
     */
    RespuestaPorDefectoAuditoria<InformeDTO> crearInforme(HttpServletRequest httpServletRequest,
                                                          InformeDTO informeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeDTO si la encuesta se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param informeDTO         objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<InformeDTO>
     */
    RespuestaPorDefectoAuditoria<InformeDTO> crearInformePorToken(HttpServletRequest httpServletRequest,
                                                                  InformeDTO informeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data InformeDTO si la empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param informeDTO         objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<InformeDTO>
     */
    RespuestaPorDefectoAuditoria<InformeDTO> actualizarInforme(HttpServletRequest httpServletRequest,
                                                               InformeDTO informeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si el documento se sube con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado         objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> subirInformeFirmado(HttpServletRequest httpServletRequest,
                                                              MultipartFile multipartFile,
                                                              BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si el documento se sube con exito
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado         objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                     BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si el informe se remueve con exito
     *
     * @param httpServletRequest request peticion.
     * @param informeDTO         objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> removerInforme(HttpServletRequest httpServletRequest,
                                                         InformeDTO informeDTO);

    //endregion

    //region PlantillaInforme

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PlantillaInformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<PlantillaInformeDTO>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaInformeDTO>> obtenerListaPlantillasInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PlantillaInformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<PlantillaInformeDTO>
     */
    RespuestaPorDefectoAuditoria<List<PlantillaInformeDTO>> obtenerPlantillasInforme(HttpServletRequest httpServletRequest, String tokenCentro);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PlantillaInformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<PlantillaInformeDTO>
     */
    RespuestaPorDefectoAuditoria<PlantillaInformeDTO> obtenerPlantillaInformePorId(HttpServletRequest httpServletRequest,
                                                                                   PlantillaInformeDTO plantillaInformeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data PlantillaInformeDTO si la encuesta se creo con exito
     *
     * @param httpServletRequest  request peticion.
     * @param plantillaInformeDTO objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<PlantillaInformeDTO>
     */
    RespuestaPorDefectoAuditoria<PlantillaInformeDTO> crearPlantillaInforme(HttpServletRequest httpServletRequest,
                                                                            PlantillaInformeDTO plantillaInformeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si la plantilla se creo con exito
     *
     * @param httpServletRequest  request peticion.
     * @param bodyEncriptado objeto BodyEncriptado.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> actualizarPlantillaInforme(HttpServletRequest httpServletRequest,
                                                                     BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data Boolean si la plantilla se remueve con exito
     *
     * @param httpServletRequest  request peticion.
     * @param plantillaInformeDTO objeto encuesta dto.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<Boolean> removerPlantillaInforme(HttpServletRequest httpServletRequest,
                                                                  PlantillaInformeDTO plantillaInformeDTO);
    //endregion

    //region Campos

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data CampoInformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<CampoInformeDTO>
     */
    RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> obtenerCamposPorIdPlantilla(HttpServletRequest httpServletRequest,
                                                                                    PlantillaInformeDTO plantillaInformeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data CampoInformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<CampoInformeDTO>
     */
    RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> obtenerCamposPorIdInforme(HttpServletRequest httpServletRequest,
                                                                                  InformeDTO informeDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data CampoInformeDTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<CampoInformeDTO>
     */
    RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> obtenerCamposPorNemonico(HttpServletRequest httpServletRequest,
                                                                                 String nemonico);
    //endregion
}
