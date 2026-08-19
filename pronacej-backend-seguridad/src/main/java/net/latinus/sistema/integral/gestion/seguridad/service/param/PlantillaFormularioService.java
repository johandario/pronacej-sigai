package net.latinus.sistema.integral.gestion.seguridad.service.param;

import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.*;
import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface PlantillaFormularioService {

    /**
     * Devuelve una lista de plantillas de formulario
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de plantillas de formulario.
     *
     * @return RespuestaPorDefectoAuditoria<List<PlantillaFormularioDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaFormularioDTO>> obtenerPlantillasFormulario(HttpServletRequest httpServletRequest,
                                                                                         BodyEncriptado bodyEncriptado);
    /**
     * Devuelve una lista de funcionarios filtradas por un parámetro de consulta
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param valor String valor de parámetro de consulta
     * @param bodyEncriptado PaginacionRequest datos de funcionarios de acuerdo a paginador.
     *
     * @return RespuestaPorDefectoAuditoria<List<FuncionarioDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> obtenerFuncionariosPorValor(HttpServletRequest httpServletRequest, String valor,
                                                                                         BodyEncriptado bodyEncriptado);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> de la plantilla para formulario creada
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param PlantillaFormularioDTO PlantillaFormularioDTO datos de la plantilla para formulario a crear o editar
     *
     * @return RespuestaPorDefectoAuditoria<PlantillaFormularioDTO>
     */
    RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> crearPlantillaFormulario(HttpServletRequest httpServletRequest, PlantillaFormularioDTO plantillaFormularioDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> del funcionario eliminado
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param FuncionarioDTO PlantillaFormularioDTO datos de la plantilla formulario a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<PlantillaFormularioDTO>
     */
    RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> eliminarPlantillaFormulario(HttpServletRequest httpServletRequest, PlantillaFormularioDTO plantillaFormularioDTO);

}
