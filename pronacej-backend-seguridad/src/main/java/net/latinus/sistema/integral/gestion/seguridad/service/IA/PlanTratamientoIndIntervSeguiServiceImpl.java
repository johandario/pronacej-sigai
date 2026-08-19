package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndInterv;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndIntervSegui;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndIntervRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndIntervSeguiRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class PlanTratamientoIndIntervSeguiServiceImpl implements PlanTratamientoIndIntervSeguiService {
    private CatalogoRepository catalogoRepository;
    private PlanTratamientoIndIntervRepository planTratamientoIndIntervRepository;
    private PlanTratamientoIndIntervSeguiRepository planTratamientoIndIntervSeguiRepository;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndIntervSeguiDTO>> obtenerSeguimientos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndIntervSeguiDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDecifrado, PaginacionRequest.class);
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("fecha").descending()
            );

            Page<PlanTratamientoIndIntervSegui> planTratamientoPage = this.planTratamientoIndIntervSeguiRepository.findByActividadPlanTratamientoIndDiferenciadaTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false, pageable);

            PaginacionResponse<PlanTratamientoIndIntervSeguiDTO> paginacionResponse = new PaginacionResponse<>();
            List<PlanTratamientoIndIntervSeguiDTO> planTratamientoIndDTOList = new ArrayList<>();

            for (PlanTratamientoIndIntervSegui plan : planTratamientoPage.toList()) {
                PlanTratamientoIndIntervSeguiDTO planDTO = new PlanTratamientoIndIntervSeguiDTO();
                planDTO.setTokenIdentificador(plan.getTokenIdentificador());
                PlanTratamientoIndIntervDTO actividad = this.intervEntidadADto(plan.getActividad());
                planDTO.setActividad(actividad);
                planDTO.setFecha(plan.getFecha());
                planDTO.setHoraInicio(plan.getHoraInicio());
                planDTO.setHoraFin(plan.getHoraFin());
                planDTO.setObservaciones(plan.getObservaciones());
                planTratamientoIndDTOList.add(planDTO);
            }

            paginacionResponse.setData(planTratamientoIndDTOList);
            paginacionResponse.setTotalItems(planTratamientoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + planTratamientoIndDTOList.size() + " de: " + planTratamientoPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> crearSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PlanTratamientoIndIntervSeguiDTO planSeguiDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndIntervSeguiDTO.class);

            //List<PlanTratamientoIndIntervSegui> planSeguiTemp = this.planTratamientoIndIntervSeguiRepository.findByIdPlanTratamientoIndIntervSeguiAndRemovido(planSeguiDTO.getIdPlanTratamientoIndIntervSegui(), false);
            List<PlanTratamientoIndIntervSegui> planSeguiTemp = this.planTratamientoIndIntervSeguiRepository.findByTokenIdentificadorAndRemovido(planSeguiDTO.getTokenIdentificador(), false);

            if(!planSeguiTemp.isEmpty() && !planSeguiDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un registro con el mismo identificador");
                return df;
            }

            PlanTratamientoIndIntervSegui planSegui;
            if (planSeguiTemp.isEmpty() && !planSeguiDTO.getEsEdicion()) {
                planSegui = new PlanTratamientoIndIntervSegui();
                /*PlanTratamientoIndInterv planInterv = this.planTratamientoIndIntervRepository.findByTokenIdentificadorAndRemovido(
                        planSeguiDTO.getActividad().getTokenIdentificador(),
                        false
                );*/
                PlanTratamientoIndInterv planInterv = this.planTratamientoIndIntervRepository.findByIdPlanTratIndIntervAndRemovido(
                        planSeguiDTO.getActividad().getIdPlanTratIndInterv(),
                        false
                );
                planSegui.setActividad(planInterv);
                planSegui.setFecha(planSeguiDTO.getFecha());
                planSegui.setHoraInicio(planSeguiDTO.getHoraInicio());
                planSegui.setHoraFin(planSeguiDTO.getHoraFin());
                planSegui.setObservaciones(planSeguiDTO.getObservaciones());
                planSegui.setFechaCreacion(new Date());
                this.planTratamientoIndIntervSeguiRepository.save(planSegui);
                df.llenarRespuestaExitosa("Se ha creado con éxito el objeto: " + planSegui.getTokenIdentificador(), planSeguiDTO);
            } else {
                planSegui = planSeguiTemp.get(0);
                PlanTratamientoIndInterv planInterv = this.planTratamientoIndIntervRepository.findByIdPlanTratIndIntervAndRemovido(
                        planSeguiDTO.getActividad().getIdPlanTratIndInterv(),
                        false
                );
                planSegui.setActividad(planInterv);
                planSegui.setFecha(planSeguiDTO.getFecha());
                planSegui.setHoraInicio(planSeguiDTO.getHoraInicio());
                planSegui.setHoraFin(planSeguiDTO.getHoraFin());
                planSegui.setObservaciones(planSeguiDTO.getObservaciones());
                planSegui.setFechaEdicion(new Date());
                this.planTratamientoIndIntervSeguiRepository.save(planSegui);
                df.llenarRespuestaExitosa("Se ha editado con éxito el objeto: " + planSegui.getTokenIdentificador(), planSeguiDTO);
            }

        } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> eliminarSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervSeguiDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PlanTratamientoIndIntervSeguiDTO planSeguiDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndIntervSeguiDTO.class);

            List<PlanTratamientoIndIntervSegui> planSeguiTemp = this.planTratamientoIndIntervSeguiRepository.findByTokenIdentificadorAndRemovido(planSeguiDTO.getTokenIdentificador(), false);

            if(planSeguiTemp.isEmpty()) {
                df.setMensaje("No existen registros que coincidan");
                return df;
            }

            PlanTratamientoIndIntervSegui planSegui = new PlanTratamientoIndIntervSegui();
            planSegui = planSeguiTemp.get(0);
            planSegui.setRemovido(true);
            planSegui.setFechaEliminacion(new Date());
            this.planTratamientoIndIntervSeguiRepository.save(planSegui);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito el objeto", planSeguiDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private PlanTratamientoIndInterv intervDtoAEntidad(PlanTratamientoIndIntervDTO dto) {
        if (dto == null) return null;

        PlanTratamientoIndInterv interv = new PlanTratamientoIndInterv();
        interv.setIdPlanTratIndInterv(dto.getIdPlanTratIndInterv());
        interv.setTokenIdentificador(dto.getTokenIdentificador());
        Catalogo dimension = this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getDimension().getTokenIdentificador(), false);
        interv.setDimension(dimension);
        interv.setObjetivo(dto.getObjetivo());
        interv.setActividadPrograma(dto.getActividadPrograma());
        interv.setEquipoResponsable(dto.getEquipoResponsable());
        interv.setTiempoEstimado(dto.getTiempoEstimado());
        interv.setNumAtencionIndividual(dto.getNumAtencionIndividual());
        interv.setNumAtencionGrupal(dto.getNumAtencionGrupal());
        return interv;
    }

    private PlanTratamientoIndIntervDTO intervEntidadADto(PlanTratamientoIndInterv entidad) {
        if (entidad == null) return null;

        PlanTratamientoIndIntervDTO dto = new PlanTratamientoIndIntervDTO();
        dto.setIdPlanTratIndInterv(entidad.getIdPlanTratIndInterv());
        if (entidad.getDimension() != null) dto.setDimension(entidadADtoCatalogo(entidad.getDimension()));
        dto.setObjetivo(entidad.getObjetivo());
        dto.setActividadPrograma(entidad.getActividadPrograma());
        dto.setEquipoResponsable(entidad.getEquipoResponsable());
        dto.setTiempoEstimado(entidad.getTiempoEstimado());
        dto.setNumAtencionIndividual(entidad.getNumAtencionIndividual());
        dto.setNumAtencionGrupal(entidad.getNumAtencionGrupal());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    private CatalogoDTO entidadADtoCatalogo(Catalogo entidad) {
        if (entidad == null) return null;

        CatalogoDTO dto = new CatalogoDTO();
        dto.setIdCatalogo(entidad.getIdCatalogo());
        dto.setNombre(entidad.getNombre());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setNemonico(entidad.getNemonico());
        dto.setCodigoExterno(entidad.getCodigoExterno());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }
}
