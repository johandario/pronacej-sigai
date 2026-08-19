package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.CriterioEvaluacionMedicaSeguimiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionadaEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.CriterioEvaluacionMedicaSeguimientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.CriterioEvaluacionMedicaSeguimientoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class CriterioEvaluacionMedicaSeguimientoServiceImpl implements CriterioEvaluacionMedicaSeguimientoService{

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;

    private CriterioEvaluacionMedicaSeguimientoRepository criterioEvaluacionMedicaSeguimientoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO>> getCriteriosDeEvaluacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idCriterioEvaluacionMedicaSeguimiento").descending()
            );

            Page<CriterioEvaluacionMedicaSeguimiento> listaCriterios = this.criterioEvaluacionMedicaSeguimientoRepository.
                    findByEvaluacionMedica_TokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false,pageable);
            List<CriterioEvaluacionMedicaSeguimientoDTO> listadoCriterioEvaluacionMedicaSeguimientoDTO= new ArrayList<>();

            PaginacionResponse<CriterioEvaluacionMedicaSeguimientoDTO> enfermedaCriterioEvaluacionPage = new PaginacionResponse<>();

            for(CriterioEvaluacionMedicaSeguimiento criterio: listaCriterios.getContent() ){
                CriterioEvaluacionMedicaSeguimientoDTO eriterioEvaluacionMedicaSeguimientoDTO = getCriterioEvaluacionMedicaSeguimientoDTO(criterio);
                listadoCriterioEvaluacionMedicaSeguimientoDTO.add(eriterioEvaluacionMedicaSeguimientoDTO);
            }

            enfermedaCriterioEvaluacionPage.setData(listadoCriterioEvaluacionMedicaSeguimientoDTO);
            enfermedaCriterioEvaluacionPage.setTotalItems(listaCriterios.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: "
                            + listadoCriterioEvaluacionMedicaSeguimientoDTO.size() + " de: " + listaCriterios.getTotalElements() + " elementos disponibles",
                    enfermedaCriterioEvaluacionPage);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private static CriterioEvaluacionMedicaSeguimientoDTO getCriterioEvaluacionMedicaSeguimientoDTO(CriterioEvaluacionMedicaSeguimiento entidad) {
        CriterioEvaluacionMedicaSeguimientoDTO dto = new CriterioEvaluacionMedicaSeguimientoDTO();

        // Llenado de detalle
        if (entidad.getDescripcion() != null) {
            dto.setDetalle(entidad.getDescripcion());
        }

        if (entidad.getTipoEvaluacion() != null) {
            Catalogo tipoEvaluacion = entidad.getTipoEvaluacion();
            if (tipoEvaluacion.getTokenIdentificador() != null) {
                dto.setTokenIdentifidorCriterioPadre(tipoEvaluacion.getTokenIdentificador());
            }
            if (tipoEvaluacion.getNombre() != null) {
                dto.setNombreCriterioPadre(tipoEvaluacion.getNombre());
            }
        }

        if (entidad.getCriterioEvaluacion() != null) {
            Catalogo criterioEvaluacion = entidad.getCriterioEvaluacion();
            if (criterioEvaluacion.getTokenIdentificador() != null) {
                dto.setTokenIdentificadorCriterioHijo(criterioEvaluacion.getTokenIdentificador());
            }
            if (criterioEvaluacion.getNombre() != null) {
                dto.setNombreCriterioHijo(criterioEvaluacion.getNombre());
            }
        }

        dto.setTokenIdentificador(entidad.getTokenIdentificador());

        return dto;
    }

}
