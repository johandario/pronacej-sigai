package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndIntervRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndSeguiRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
@AllArgsConstructor
public class PlanTratamientoIndSeguiServiceImpl implements PlanTratamientoIndSeguiService {
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JwtProviderService jwtProviderService;
    private PlanTratamientoIndSeguiRepository planTratamientoIndSeguiRepository;
    private PaginacionService paginacionService;
    private PlanTratamientoIndRepository planTratamientoIndRepository;
    private PlanTratamientoIndIntervRepository planTratamientoIndIntervRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiDTO>> obtenerSeguimientos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
           /* Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idPlanTratamientoIndSegui").descending()
            );

            Page<PlanTratamientoIndSegui> planTratamientoPage = this.planTratamientoIndSeguiRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false, pageable);
*/
            List<PlanTratamientoIndSegui> planTratamientoPage = this.planTratamientoIndSeguiRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false);

            PaginacionResponse<PlanTratamientoIndSeguiDTO> paginacionResponse = new PaginacionResponse<>();
            List<PlanTratamientoIndSeguiDTO> planTratamientoIndDTOList = new ArrayList<>();

            for (PlanTratamientoIndSegui plan : planTratamientoPage) {
                PlanTratamientoIndSeguiDTO planDTO = entidadADto(plan);
                planDTO.setIdPlanTratamiento(plan.getPlanTratamientoInd().getIdPlanTratamiento());
                planDTO.setTokenIdentificador(plan.getTokenIdentificador());

                String pattern = "dd-MM-yyyy";
                DateFormat fecha = new SimpleDateFormat(pattern);
                planDTO.setFecInicio(fecha.format(plan.getFechaInicio()));
                planDTO.setFecFin(fecha.format(plan.getFechaFin()));

                planTratamientoIndDTOList.add(planDTO);
            }

            planTratamientoIndDTOList.sort(
                    Comparator.comparing(PlanTratamientoIndSeguiDTO::getFechaCreacion).reversed()
            );

            /*paginacionResponse.setData(planTratamientoIndDTOList);
            paginacionResponse.setTotalItems(planTratamientoPage.getTotalElements());*/

            paginacionResponse = paginacionService.obtenerDatos(planTratamientoIndDTOList, paginacionRequest);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + planTratamientoIndDTOList.size() + " de: " + planTratamientoPage.size() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> obtenerSeguimientoPorId(HttpServletRequest httpServletRequest, Long id) {
        return null;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> crearSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String bodyDecifrado = FuncionesAyuda.descomprimirBase64Gzip(df22.getData());

            PlanTratamientoIndSeguiDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndSeguiDTO.class);

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenPadre(), false);
            if (ficha == null) {
                df.setMensaje("No existe una ficha principal creada");
                return df;
            }

            List<PlanTratamientoInd> planesTratamientoInd = this.planTratamientoIndRepository.findByidPlanTratamientoAndRemovido(planDTO.getIdPlanTratamiento(), false);

            if (planesTratamientoInd.isEmpty()) {
                df.setMensaje("No existe un plan de tratamiento asociado");
                return df;
            }

            List<PlanTratamientoIndSegui> planesTemp = this.planTratamientoIndSeguiRepository.findByIdPlanTratamientoIndSeguiAndRemovido(planDTO.getIdPlanTratamientoIndSegui(), false);

            if (!planesTemp.isEmpty() && !planDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un registro con el mismo identificador");
                return df;
            }

            PlanTratamientoIndSegui plan;
            if (planesTemp.isEmpty() && !planDTO.getEsEdicion()) {
                plan = dtoAEntidad(planDTO);
                plan.setFechaCreacion(new Date());
                plan.setFichaIdentificacion(ficha);
                plan.setPlanTratamientoInd(planesTratamientoInd.get(0));
                this.planTratamientoIndSeguiRepository.save(plan);
                planDTO = entidadADto(plan);
                df.llenarRespuestaExitosa("Se ha creado con éxito el registro: " + plan.getIdPlanTratamientoIndSegui(), planDTO);
            } else {
                plan = planesTemp.get(0);
                plan = dtoAEntidad(planDTO);
                plan.setFechaEdicion(new Date());
                plan.setFichaIdentificacion(ficha);
                plan.setPlanTratamientoInd(planesTratamientoInd.get(0));
                this.planTratamientoIndSeguiRepository.save(plan);
                df.llenarRespuestaExitosa("Se ha editado con éxito el registro: " + plan.getIdPlanTratamientoIndSegui(), planDTO);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> eliminarSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            PlanTratamientoIndSeguiDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndSeguiDTO.class);

            List<PlanTratamientoIndSegui> planesTemp = this.planTratamientoIndSeguiRepository.findByIdPlanTratamientoIndSeguiAndRemovido(planDTO.getIdPlanTratamientoIndSegui(), false);

            if (planesTemp.isEmpty()) {
                df.setMensaje("No existen registros que coincidan");
                return df;
            }

            PlanTratamientoIndSegui plan = planesTemp.get(0);
            plan.setRemovido(true);
            plan.setFechaEliminacion(new Date());
            this.planTratamientoIndSeguiRepository.save(plan);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito el registro: " + plan.getIdPlanTratamientoIndSegui(), planDTO);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private PlanTratamientoIndSegui dtoAEntidad(PlanTratamientoIndSeguiDTO dto) {
        if (dto == null) return null;

        PlanTratamientoIndSegui entidad = new PlanTratamientoIndSegui();
        entidad.setIdPlanTratamientoIndSegui(dto.getIdPlanTratamientoIndSegui());
        if (dto.getPeriodoTiempo() != null) entidad.setPeriodoTiempo(dtoAEntidadCatalogo(dto.getPeriodoTiempo()));
        entidad.setPrograma(dto.getPrograma());
        entidad.setResumen(dto.getResumen());
        entidad.setEstadoSalud(dto.getEstadoSalud());
        entidad.setObservaciones(dto.getObservaciones());
        entidad.setRecomendaciones(dto.getRecomendaciones());
        entidad.setFechaInicio(dto.getFechaInicio());
        entidad.setFechaFin(dto.getFechaFin());

        if (dto.getIntervObjetivos() != null) {
            List<PlanTratamientoIndSeguiDetalle> intervList = dto.getIntervObjetivos().stream()
                    .map(this::detalleDtoAEntidad)
                    .toList();
            intervList.forEach(interv -> interv.setPlanTratamientoIndObjetivo(entidad));
            entidad.setIntervObjetivos(intervList);
        }

        if (dto.getIntervNoCriminogenos() != null) {
            List<PlanTratamientoIndSeguiDetalle> intervList = dto.getIntervNoCriminogenos().stream()
                    .map(this::detalleDtoAEntidad)
                    .toList();
            intervList.forEach(interv -> interv.setPlanTratamientoIndNoCriminogeno(entidad));
            entidad.setIntervNoCriminogenos(intervList);
        }

        if (dto.getIntervDiferenciada() != null) {
            List<PlanTratamientoIndSeguiDetalle> intervList = dto.getIntervDiferenciada().stream()
                    .map(this::detalleDtoAEntidad)
                    .toList();
            intervList.forEach(interv -> interv.setPlanTratamientoIndDiferenciada(entidad));
            entidad.setIntervDiferenciada(intervList);
        }

        if (dto.getIntervMedidas() != null) {
            List<PlanTratamientoIndSeguiDetalle> intervList = dto.getIntervMedidas().stream()
                    .map(this::detalleDtoAEntidad)
                    .toList();
            intervList.forEach(interv -> interv.setPlanTratamientoMedidas(entidad));
            entidad.setIntervMedidas(intervList);
        }

        return entidad;
    }

    private PlanTratamientoIndSeguiDetalle detalleDtoAEntidad(PlanTratamientoIndSeguiDetalleDTO dto) {
        if (dto == null) return null;

        PlanTratamientoIndSeguiDetalle entidad = new PlanTratamientoIndSeguiDetalle();

        if (dto.getPlanTratamientoIndInterv() != null && dto.getPlanTratamientoIndInterv().getTokenIdentificador() != null) {
            PlanTratamientoIndInterv interv = this.planTratamientoIndIntervRepository.findByTokenIdentificadorAndRemovido(dto.getPlanTratamientoIndInterv().getTokenIdentificador(), false);

            if (interv != null) entidad.setPlanTratamientoIndInterv(interv);
        }

        entidad.setFrecuencia(dtoAEntidadCatalogo(dto.getFrecuencia()));
        entidad.setFrecuenciaParticipacion(dtoAEntidadCatalogo(dto.getFrecuenciaParticipacion()));
        entidad.setSituacionActual(dtoAEntidadCatalogo(dto.getSituacionActual()));
        entidad.setActitud(dtoAEntidadCatalogo(dto.getActitud()));
        entidad.setAprovechamiento(dtoAEntidadCatalogo(dto.getAprovechamiento()));
        entidad.setFechaInicio(dto.getFechaInicio());
        entidad.setFechaFin(dto.getFechaFin());
        entidad.setObservaciones(dto.getObservaciones());
        entidad.setIndicadorDeficiente(dto.getIndicadorDeficiente());
        entidad.setIndicadorEnProceso(dto.getIndicadorEnProceso());
        entidad.setIndicadorLogrado(dto.getIndicadorLogrado());
        entidad.setAnalisis(dto.getAnalisis());
        return entidad;
    }

    private PlanTratamientoIndSeguiDTO entidadADto(PlanTratamientoIndSegui entidad) {
        if (entidad == null) return null;

        PlanTratamientoIndSeguiDTO dto = new PlanTratamientoIndSeguiDTO();
        dto.setIdPlanTratamientoIndSegui(entidad.getIdPlanTratamientoIndSegui());
        if (entidad.getPeriodoTiempo() != null) dto.setPeriodoTiempo(entidadADtoCatalogo(entidad.getPeriodoTiempo()));
        dto.setPrograma(entidad.getPrograma());
        dto.setResumen(entidad.getResumen());
        dto.setEstadoSalud(entidad.getEstadoSalud());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setRecomendaciones(entidad.getRecomendaciones());
        dto.setFechaInicio(entidad.getFechaInicio());
        dto.setFechaFin(entidad.getFechaFin());
        dto.setFechaCreacion(entidad.getFechaCreacion());

        if (entidad.getIntervObjetivos() != null) {
            List<PlanTratamientoIndSeguiDetalleDTO> intervDTOS = entidad.getIntervObjetivos().stream()
                    .map(this::detalleEntidadADto)
                    .toList();
            dto.setIntervObjetivos(intervDTOS);
        }

        if (entidad.getIntervNoCriminogenos() != null) {
            List<PlanTratamientoIndSeguiDetalleDTO> intervDTOS = entidad.getIntervNoCriminogenos().stream()
                    .map(this::detalleEntidadADto)
                    .toList();
            dto.setIntervNoCriminogenos(intervDTOS);
        }

        if (entidad.getIntervDiferenciada() != null) {
            List<PlanTratamientoIndSeguiDetalleDTO> intervDTOS = entidad.getIntervDiferenciada().stream()
                    .map(this::detalleEntidadADto)
                    .toList();
            dto.setIntervDiferenciada(intervDTOS);
        }

        if (entidad.getIntervMedidas() != null) {
            List<PlanTratamientoIndSeguiDetalleDTO> intervDTOS = entidad.getIntervMedidas().stream()
                    .map(this::detalleEntidadADto)
                    .toList();
            dto.setIntervMedidas(intervDTOS);
        }

        return dto;
    }

    private PlanTratamientoIndSeguiDetalleDTO detalleEntidadADto(PlanTratamientoIndSeguiDetalle entidad) {
        if (entidad == null) return null;

        PlanTratamientoIndSeguiDetalleDTO dto = new PlanTratamientoIndSeguiDetalleDTO();
        dto.setPlanTratamientoIndInterv(intervEntidadADto(entidad.getPlanTratamientoIndInterv()));
        if (entidad.getFrecuencia() != null) dto.setFrecuencia(entidadADtoCatalogo(entidad.getFrecuencia()));
        if (entidad.getFrecuenciaParticipacion() != null) dto.setFrecuenciaParticipacion(entidadADtoCatalogo(entidad.getFrecuenciaParticipacion()));
        if (entidad.getSituacionActual() != null) dto.setSituacionActual(entidadADtoCatalogo(entidad.getSituacionActual()));
        if (entidad.getActitud() != null) dto.setActitud(entidadADtoCatalogo(entidad.getActitud()));
        dto.setAprovechamiento(entidadADtoCatalogo(entidad.getAprovechamiento()));
        dto.setFechaInicio(entidad.getFechaInicio());
        dto.setFechaFin(entidad.getFechaFin());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setIndicadorDeficiente(entidad.getIndicadorDeficiente());
        dto.setIndicadorEnProceso(entidad.getIndicadorEnProceso());
        dto.setIndicadorLogrado(entidad.getIndicadorLogrado());
        dto.setAnalisis(entidad.getAnalisis());
        return dto;
    }

    private PlanTratamientoIndInterv intervDtoAEntidad(PlanTratamientoIndIntervDTO dto) {
        if (dto == null) return null;

        PlanTratamientoIndInterv interv = new PlanTratamientoIndInterv();
        interv.setIdPlanTratIndInterv(dto.getIdPlanTratIndInterv());
        interv.setVersion(dto.getVersion());
        interv.setReajuste(dto.getReajuste());
        interv.setActivo(dto.getActivo());
        interv.setFundamentacionReajuste(dto.getFundamentacionReajuste());
        interv.setFechaReajuste(dto.getFechaReajuste());
        if (dto.getDimension() != null) {
            Catalogo dimension = this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getDimension().getTokenIdentificador(), false);
            interv.setDimension(dimension);
        }
        interv.setObjetivo(dto.getObjetivo());
        interv.setActividadPrograma(dto.getActividadPrograma());
        interv.setEquipoResponsable(dto.getEquipoResponsable());
        interv.setTiempoEstimado(dto.getTiempoEstimado());
        interv.setNumAtencionIndividual(dto.getNumAtencionIndividual());
        interv.setNumAtencionGrupal(dto.getNumAtencionGrupal());
        interv.setLugar(dto.getLugar());
        if (dto.getModalidad() != null) {
            Catalogo modalidad = this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getModalidad().getTokenIdentificador(), false);
            interv.setModalidad(modalidad);
        }
        if (dto.getFrecuencia() != null) {
            Catalogo frecuencia = this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getFrecuencia().getTokenIdentificador(), false);
            interv.setFrecuencia(frecuencia);
        }
        interv.setDescripcion(dto.getDescripcion());
        return interv;
    }

    private PlanTratamientoIndIntervDTO intervEntidadADto(PlanTratamientoIndInterv entidad) {
        if (entidad == null) return null;

        PlanTratamientoIndIntervDTO dto = new PlanTratamientoIndIntervDTO();
        dto.setIdPlanTratIndInterv(entidad.getIdPlanTratIndInterv());
        dto.setVersion(entidad.getVersion());
        dto.setReajuste(entidad.getReajuste());
        dto.setActivo(entidad.getActivo());
        dto.setFundamentacionReajuste(entidad.getFundamentacionReajuste());
        dto.setFechaReajuste(entidad.getFechaReajuste());
        if (entidad.getDimension() != null) dto.setDimension(entidadADtoCatalogo(entidad.getDimension()));
        dto.setObjetivo(entidad.getObjetivo());
        dto.setActividadPrograma(entidad.getActividadPrograma());
        dto.setEquipoResponsable(entidad.getEquipoResponsable());
        dto.setTiempoEstimado(entidad.getTiempoEstimado());
        dto.setNumAtencionIndividual(entidad.getNumAtencionIndividual());
        dto.setNumAtencionGrupal(entidad.getNumAtencionGrupal());
        dto.setLugar(entidad.getLugar());
        if (entidad.getModalidad() != null) dto.setModalidad(entidadADtoCatalogo(entidad.getModalidad()));
        if (entidad.getFrecuencia() != null) dto.setFrecuencia(entidadADtoCatalogo(entidad.getFrecuencia()));
        dto.setDescripcion(entidad.getDescripcion());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
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
