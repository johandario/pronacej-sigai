package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

import java.util.List;

public interface FuncionarioService {

    /**
     * Devuelve una lista de funcionarios
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado PaginacionRequest datos de funcionarios.
     *
     * @return RespuestaPorDefectoAuditoria<List<FuncionarioDTO>>
     */
    RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> obtenerFuncionarios(HttpServletRequest httpServletRequest,
                                                                                         BodyEncriptado bodyEncriptado);

    /**
     * Devuelve una lista de funcionarios sin paginación
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<List<FuncionarioDTO>>
     */
    RespuestaPorDefectoAuditoria<List<FuncionarioDTO>> obtenerFuncionariosSinPaginacion(HttpServletRequest httpServletRequest);
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
     * Devuelve un objeto RespuestaPorDefectoAuditoria<FuncionarioDTO> del funcionario creado o actualizado
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param FuncionarioDTO FuncionarioDTO datos del funcionario a crear o actualizar.
     *
     * @return RespuestaPorDefectoAuditoria<FuncionarioDTO>
     */
    RespuestaPorDefectoAuditoria<FuncionarioDTO> crearFuncionario(HttpServletRequest httpServletRequest, FuncionarioDTO FuncionarioDTO);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<FuncionarioDTO> del funcionario eliminado
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param FuncionarioDTO FuncionarioDTO datos del funcionario a eliminar.
     *
     * @return RespuestaPorDefectoAuditoria<FuncionarioDTO>
     */
    RespuestaPorDefectoAuditoria<FuncionarioDTO> eliminarFuncionario(HttpServletRequest httpServletRequest, FuncionarioDTO FuncionarioDTO);

    /**
     * Devuelve una lista de funcionarios filtradas por un parámetro de consulta
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     *
     * @return RespuestaPorDefectoAuditoria<List<FuncionarioDTO>>
     */
    RespuestaPorDefectoAuditoria<FuncionarioDTO> obtenerFuncionarioDelUsuario(HttpServletRequest httpServletRequest);

    /**
     * Devuelve una lista de jerarquías filtradas por funcionario
     *
     * @param httpServletRequest request peticion.
     *
     * @return RespuestaPorDefectoAuditoria<JerarquiaDTO>
     */
    RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorFuncionarios(HttpServletRequest httpServletRequest);
}
