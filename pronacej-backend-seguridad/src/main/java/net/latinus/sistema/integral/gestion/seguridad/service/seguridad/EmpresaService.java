package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.EmpresaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;

public interface EmpresaService {

    /**
     * Obtiene una empresa por el token de identificacion no removida
     *
     * @param tokenIdentificador token identificador de la empresa.
     *
     * @return Empresa.
     */
    Empresa encontrarPorTokenIdentificador(String tokenIdentificador);


    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria con data EmpresaDTO si la empresa se creo con exito
     *
     * @param httpServletRequest request peticion.
     * @param empresaDTO objeto empresa dto.
     *
     * @return RespuestaPorDefectoAuditoria<EmpresaDTO>
     */
    RespuestaPorDefectoAuditoria<EmpresaDTO> crearEmpresaDirecto(HttpServletRequest httpServletRequest,
                                                                 EmpresaDTO empresaDTO);
}
