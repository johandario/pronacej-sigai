package net.latinus.sistema.integral.gestion.seguridad.service.param;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface CatalogoService {

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con cada una de las respuestas de la creacion del catalogo
     *
     * @param httpServletRequest request peticion.
     * @param catalogosDto       objeto catalogo para crear.
     * @return RespuestaPorDefectoAuditoria<List < RespuestaPorDefectoAuditoria < CatalogoDTO>>>
     */
    RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<CatalogoDTO>>> crearVariosCatalogosDirecto(HttpServletRequest httpServletRequest, List<CatalogoDTO> catalogosDto);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de catalogos
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto catalogo que contiene los campos para la busqueda.
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerCatalogoPorNemonicoPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de catalogos
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> obtenerCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de sub catalogos
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     contiene el token del padre
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> obtenerSubCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de sub catalogos
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     contiene el token del padre
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerSubCatalogosPorNemonicoPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de catalogos
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     contiene el string de busqueda
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> buscarCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con una lista de sub catalogos
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     contiene el string de busqueda
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> buscarSubCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el catalogo actualizado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     objeto catalogo para actualizar
     * @return RespuestaPorDefectoAuditoria<CatalogoDTO>
     */
    RespuestaPorDefectoAuditoria<CatalogoDTO> actualizarCatalogo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el catalogo eliminado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     contiene el id del catalogo para eliminar
     * @return RespuestaPorDefectoAuditoria<CatalogoDTO>
     */
    RespuestaPorDefectoAuditoria<CatalogoDTO> eliminarCatalogo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con el catálogo creado
     *
     * @param httpServletRequest request peticion.
     * @param bodyEncriptado     contiene el objeto catalogo para crear
     * @return RespuestaPorDefectoAuditoria<CatalogoDTO>
     */
    RespuestaPorDefectoAuditoria<CatalogoDTO> crearCatalogo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado);


    /**
     * Devuelve un objeto Catalogo DTO
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<CatalogoDTO>
     */
    RespuestaPorDefectoAuditoria<CatalogoDTO> obtenerUnCatalogo(HttpServletRequest httpServletRequest,
                                                                String nemonico);


    /**
     * Obtener totales
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerTotales(HttpServletRequest httpServletRequest);


    /**
     * Obtener catalogos padres como arbol
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<List < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerCatalogosPrincipales(HttpServletRequest httpServletRequest);

    /**
     * Obtener catalogos Hijos
     *
     * @param httpServletRequest request peticion.
     * @param paginacionRequest  PaginacionRequest
     * @return RespuestaPorDefectoAuditoria<PaginacionResponse < CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> obtenerCatalogosHijos(HttpServletRequest httpServletRequest,
                                                                                        PaginacionRequest paginacionRequest);

    /**
     * Obtener un catalogo por el token de identificacion
     *
     * @param httpServletRequest request peticion.
     * @param tokenIdentificador PaginacionRequest
     * @return RespuestaPorDefectoAuditoria<CatalogoDTO>
     */
    RespuestaPorDefectoAuditoria<CatalogoDTO> obtenerCatalogoPorToken(HttpServletRequest httpServletRequest,
                                                                      String tokenIdentificador);


    /**
     * Obtener una lista de catalogo dto por padres e hijos
     *
     * @param httpServletRequest rHttpServletRequest.
     * @param tokenIdentificador String
     *
     * @return RespuestaPorDefectoAuditoria<List<CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerHijos(HttpServletRequest httpServletRequest,
                                                                                               String tokenIdentificador);


    /**
     * Obtener un catalogo padre con sus unicos hijos [p->h1->h11->h111->...]
     *
     * @param httpServletRequest rHttpServletRequest.
     * @param tokenIdentificador String tokenDel ultimo hijo
     *
     * @return RespuestaPorDefectoAuditoria<List<CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerDescendencia(HttpServletRequest httpServletRequest,
                                                                 String tokenIdentificador);


    /**
     * Obtener un catalogo padre con sus unicos hijos [p->h1->h11->h111->...]
     *
     * @param httpServletRequest rHttpServletRequest.
     * @param stringFiltro String
     *
     * @return RespuestaPorDefectoAuditoria<List<CatalogoDTO>>
     */
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerTodosPorString(HttpServletRequest httpServletRequest,
                                                                        String stringFiltro);

    /**
     *
     *
     * @param httpServletRequest request peticion.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    RespuestaPorDefectoAuditoria<List<Long>> borrarCatalogosQueNoTenganHijos(HttpServletRequest httpServletRequest);
}
