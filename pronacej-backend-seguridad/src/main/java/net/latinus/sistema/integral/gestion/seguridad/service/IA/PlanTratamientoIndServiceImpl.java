package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ExpedienteMatrizDetalleRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class PlanTratamientoIndServiceImpl implements PlanTratamientoIndService {
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private PlanTratamientoIndRepository planTratamientoIndRepository;
    private PaginacionService paginacionService;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private ExpedienteMatrizDetalleRepository expedienteMatrizDetalleRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndDTO>> obtenerPlanes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
                    Sort.by(
                            Sort.Order.desc("fechaCreacion"),
                            Sort.Order.asc("estado.nombre")
                    )
            );

            //Page<PlanTratamientoInd> planTratamientoPage = this.planTratamientoIndRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false, pageable);

            List<PlanTratamientoInd> planTratamientoPage = this.planTratamientoIndRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false);

            PaginacionResponse<PlanTratamientoIndDTO> paginacionResponse = new PaginacionResponse<>();
            List<PlanTratamientoIndDTO> planTratamientoIndDTOList = new ArrayList<>();

            for (PlanTratamientoInd plan : planTratamientoPage) {
                PlanTratamientoIndDTO planDTO = entidadADto(plan);
                if (planDTO.getEstado() != null) {
                    planDTO.setNombreEstado(planDTO.getEstado().getNombre());
                }
                planDTO.setIdFichaIdentificacion(plan.getFichaIdentificacion().getIdFichaIdentificacion());
                planTratamientoIndDTOList.add(planDTO);
            }

            planTratamientoIndDTOList.sort(
                    Comparator.comparing((PlanTratamientoIndDTO plan) -> plan.getEstado().getNombre())
                            .thenComparing(Comparator.comparing(PlanTratamientoIndDTO::getFechaCreacion).reversed())

            );

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            planTratamientoIndDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse = paginacionService.obtenerDatos(planTratamientoIndDTOList, paginacionRequest);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + planTratamientoIndDTOList.size() + " de: " + planTratamientoPage.size() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> obtenerPlanPorId(HttpServletRequest httpServletRequest, Long id) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            List<PlanTratamientoInd> plan = this.planTratamientoIndRepository.findByidPlanTratamientoAndRemovido(id, false);

            if (plan.isEmpty()) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }

            PlanTratamientoIndDTO planDTO = entidadADto(plan.get(0));
            planDTO.setIdFichaIdentificacion(plan.get(0).getFichaIdentificacion().getIdFichaIdentificacion());
            if (plan.get(0).getExpedienteMatrizDetalle() != null) {
                ExpedienteMatrizDetalle detalle = plan.get(0).getExpedienteMatrizDetalle();
                for (ExpedienteMatrizMedida medida : detalle.getMedidasAccesorias()) {
                    planDTO.getMedidasAccesorias().add(entidadADtoCatalogo(medida.getMedida()));
                }
            }

            if (!planDTO.getIntervObjetivos().isEmpty()) {
                List<PlanTratamientoIndIntervDTO> listaOrdenada = new ArrayList<>(planDTO.getIntervObjetivos());
                listaOrdenada.sort(Comparator.comparing(PlanTratamientoIndIntervDTO::getIdPlanTratIndInterv).reversed());
                planDTO.setIntervObjetivos(listaOrdenada);
            }

            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + planDTO.getIdPlanTratamiento(), planDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> obtenerPlanActivo(HttpServletRequest httpServletRequest, String tokenIdentificadorFicha) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            PlanTratamientoInd plan = this.planTratamientoIndRepository.findByEstadoNemonicoAndFichaIdentificacionTokenIdentificadorAndRemovido("ESTADO_PTI_ACTIVO", tokenIdentificadorFicha, false);

            if (plan == null) {
                df.setMensaje("No existe ningún PTI Activo.");
                return df;
            }

            PlanTratamientoIndDTO planDTO = entidadADto(plan);

            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + planDTO.getIdPlanTratamiento(), planDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> crearPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            PlanTratamientoIndDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndDTO.class);

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenPadre(), false);
            if (ficha == null) {
                df.setMensaje("No existe una ficha principal creada");
                return df;
            }

            List<PlanTratamientoInd> planesTemp = this.planTratamientoIndRepository.findByidPlanTratamientoAndRemovido(planDTO.getIdPlanTratamiento(), false);

            if (!planesTemp.isEmpty() && !planDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un registro con el mismo identificador");
                return df;
            }

            PlanTratamientoInd plan;
            if (planesTemp.isEmpty() && !planDTO.getEsEdicion()) {
                plan = dtoAEntidad(planDTO);
                plan.setFechaCreacion(new Date());
                plan.setFichaIdentificacion(ficha);

                if (planDTO.getTokenExpedienteMatrizDetalle() != null) {
                    ExpedienteMatrizDetalle expedienteMatrizDetalle = this.expedienteMatrizDetalleRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenExpedienteMatrizDetalle(), false);
                    if (expedienteMatrizDetalle != null) plan.setExpedienteMatrizDetalle(expedienteMatrizDetalle);
                }

                if (planDTO.getEsActivo() != null && planDTO.getEsActivo()) {
                    List<PlanTratamientoInd> planes = this.planTratamientoIndRepository.findByFichaIdentificacionTokenIdentificador(planDTO.getTokenPadre());
                    Catalogo catalogoEncontrado = this.catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.NEMONICO_ESTADO_PTI_FINALIZADO, false);
                    planes.forEach(planTratamientoInd -> {
                        if (!planTratamientoInd.getEstado().getNemonico().equals(EtiquetaNemonico.NEMONICO_ESTADO_PTI_BORRADOR)) {
                            planTratamientoInd.setCompletada(true);
                            planTratamientoInd.setEstado(catalogoEncontrado);
                        }
                    });
                    this.planTratamientoIndRepository.saveAll(planes);

                    Catalogo catalogoActivoEncontrado = this.catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.NEMONICO_ESTADO_PTI_ACTIVO, false);
                    plan.setEstado(catalogoActivoEncontrado);
                    plan.setCompletada(false);
                } else {
                    Catalogo catalogoActivoEncontrado = this.catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.NEMONICO_ESTADO_PTI_BORRADOR, false);
                    plan.setEstado(catalogoActivoEncontrado);
                    plan.setCompletada(false);
                }

                this.planTratamientoIndRepository.save(plan);
                df.llenarRespuestaExitosa("Se ha creado con éxito el registro: " + plan.getIdPlanTratamiento(), entidadADto(plan));
            } else {
                plan = planesTemp.get(0);
                plan = dtoAEntidad(planDTO);
                plan.setFechaEdicion(new Date());
                plan.setFichaIdentificacion(ficha);

                if (planesTemp.get(0).getExpedienteMatrizDetalle() != null) {
                    ExpedienteMatrizDetalle expedienteMatrizDetalle = this.expedienteMatrizDetalleRepository.findByTokenIdentificadorAndRemovido(planesTemp.get(0).getExpedienteMatrizDetalle().getTokenIdentificador(), false);
                    if (expedienteMatrizDetalle != null) plan.setExpedienteMatrizDetalle(expedienteMatrizDetalle);
                }

                if (planDTO.getEsActivo() != null && planDTO.getEsActivo()) {
                    List<PlanTratamientoInd> planes = this.planTratamientoIndRepository.findByFichaIdentificacionTokenIdentificador(planDTO.getTokenPadre());
                    Catalogo catalogoEncontrado = this.catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.NEMONICO_ESTADO_PTI_FINALIZADO, false);
                    planes.forEach(planTratamientoInd -> {
                        if (!planTratamientoInd.getEstado().getNemonico().equals(EtiquetaNemonico.NEMONICO_ESTADO_PTI_BORRADOR)) {
                            planTratamientoInd.setCompletada(true);
                            planTratamientoInd.setEstado(catalogoEncontrado);
                        }
                    });
                    this.planTratamientoIndRepository.saveAll(planes);

                    Catalogo catalogoActivoEncontrado = this.catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.NEMONICO_ESTADO_PTI_ACTIVO, false);
                    plan.setEstado(catalogoActivoEncontrado);
                    plan.setCompletada(false);
                }

                this.planTratamientoIndRepository.save(plan);
                df.llenarRespuestaExitosa("Se ha editado con éxito el registro: " + plan.getIdPlanTratamiento(), entidadADto(plan));
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> eliminarPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            PlanTratamientoIndDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndDTO.class);

            List<PlanTratamientoInd> planesTemp = this.planTratamientoIndRepository.findByidPlanTratamientoAndRemovido(planDTO.getIdPlanTratamiento(), false);

            if (planesTemp.isEmpty()) {
                df.setMensaje("No existen registros que coincidan");
                return df;
            }

            PlanTratamientoInd plan = planesTemp.get(0);
            plan.setRemovido(true);
            plan.setFechaEliminacion(new Date());
            this.planTratamientoIndRepository.save(plan);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito el registro: " + plan.getIdPlanTratamiento(), planDTO);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private PlanTratamientoInd dtoAEntidad(PlanTratamientoIndDTO dto) {
        if (dto == null) return null;

        //PlanTratamientoInd planTratamiento = this.planTratamientoIndRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        //PlanTratamientoInd plan = Objects.requireNonNullElseGet(planTratamiento, PlanTratamientoInd::new);

        PlanTratamientoInd plan = new PlanTratamientoInd();
        plan.setIdPlanTratamiento(dto.getIdPlanTratamiento());
        plan.setInstTecnicas(dto.getInstTecnicas());
        plan.setEstado(dtoAEntidadCatalogo(dto.getEstado()));

        if (dto.getEspecFactores() != null) {
            List<PlanTratamientoIndEspecif> especifs = dto.getEspecFactores().stream()
                    .map(this::especifDtoAEntidad)
                    .toList();
            especifs.forEach(especif -> especif.setPlanTratamientoIndEspecFactores(plan));
            plan.setEspecFactores(especifs);
        }

        if (dto.getEjecMedidas() != null) {
            List<PlanTratamientoIndEspecif> medidas = dto.getEjecMedidas().stream()
                    .map(this::especifDtoAEntidad)
                    .toList();
            medidas.forEach(especif -> especif.setPlanTratamientoIndEjecMedidas(plan));
            plan.setEjecMedidas(medidas);
        }

        if (dto.getUnidadReceptora() != null) {
            List<PlanTratamientoIndEspecif> unidad = dto.getUnidadReceptora().stream()
                    .map(this::especifDtoAEntidad)
                    .toList();
            unidad.forEach(especif -> especif.setPlanTratamientoIndUnidadReceptora(plan));
            plan.setUnidadReceptora(unidad);
        }

        plan.setFactRiesgoNoCrimin(dto.getFactRiesgoNoCrimin());
        plan.setValRiesgo(dto.getValRiesgo());
        plan.setHipotExplicativa(dto.getHipotExplicativa());
        plan.setIntensidadIntervTrat(dto.getIntensidadIntervTrat());
        plan.setTipoCentro(dto.getTipoCentro());
        plan.setTipoAbierto(dto.getTipoAbierto());

        if (dto.getIntervObjetivos() != null) {
            List<PlanTratamientoIndInterv> intervObjetivos = dto.getIntervObjetivos().stream()
                    .map(this::intervDtoAEntidad)
                    .toList();
            intervObjetivos.forEach(interv -> interv.setPlanTratamientoIndObjetivo(plan));
            plan.setIntervObjetivos(intervObjetivos);
        }

        if (dto.getIntervNoCriminogenos() != null) {
            List<PlanTratamientoIndInterv> intervNoCriminogenos = dto.getIntervNoCriminogenos().stream()
                    .map(this::intervDtoAEntidad)
                    .toList();
            intervNoCriminogenos.forEach(interv -> interv.setPlanTratamientoIndNoCriminogeno(plan));
            plan.setIntervNoCriminogenos(intervNoCriminogenos);
        }

        if (dto.getIntervDiferenciada() != null) {
            List<PlanTratamientoIndInterv> intervDiferenciada = dto.getIntervDiferenciada().stream()
                    .map(this::intervDtoAEntidad)
                    .toList();
            intervDiferenciada.forEach(interv -> interv.setPlanTratamientoIndDiferenciada(plan));
            plan.setIntervDiferenciada(intervDiferenciada);
        }

        if (dto.getIntervMedidas() != null) {
            List<PlanTratamientoIndInterv> intervMedidas = dto.getIntervMedidas().stream()
                    .map(this::intervDtoAEntidad)
                    .toList();
            intervMedidas.forEach(interv -> interv.setPlanTratamientoMedidas(plan));
            plan.setIntervMedidas(intervMedidas);
        }

        return plan;
    }

    private PlanTratamientoIndEspecif especifDtoAEntidad(PlanTratamientoIndEspecifDTO dto) {
        if (dto == null) return null;

        PlanTratamientoIndEspecif especif = new PlanTratamientoIndEspecif();
        especif.setIdPlanTratIndEspecif(dto.getIdPlanTratIndEspecif());
        Catalogo dimension = this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getDimension().getTokenIdentificador(), false);
        especif.setDimension(dimension);
        especif.setFactorRiesgo(dto.getFactorRiesgo());
        especif.setFactorProtector(dto.getFactorProtector());
        especif.setComentario(dto.getComentario());
        return especif;
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

    private PlanTratamientoIndDTO entidadADto(PlanTratamientoInd entidad) {
        if (entidad == null) return null;

        PlanTratamientoIndDTO dto = new PlanTratamientoIndDTO();
        dto.setIdPlanTratamiento(entidad.getIdPlanTratamiento());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setInstTecnicas(entidad.getInstTecnicas());
        dto.setEstado(entidadADtoCatalogo(entidad.getEstado()));
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setCompletada(entidad.getCompletada());

        if (entidad.getFichaIdentificacion() != null) dto.setTokenFichaIdentificacion(entidad.getFichaIdentificacion().getTokenIdentificador());

        if (entidad.getEspecFactores() != null) {
            List<PlanTratamientoIndEspecifDTO> especifDTOS = entidad.getEspecFactores().stream()
                    .map(this::especifEntidadADto)
                    .toList();
            dto.setEspecFactores(especifDTOS);
        }

        if (entidad.getEjecMedidas() != null) {
            List<PlanTratamientoIndEspecifDTO> especifDTOS = entidad.getEjecMedidas().stream()
                    .map(this::especifEntidadADto)
                    .toList();
            dto.setEjecMedidas(especifDTOS);
        }

        if (entidad.getUnidadReceptora() != null) {
            List<PlanTratamientoIndEspecifDTO> especifDTOS = entidad.getUnidadReceptora().stream()
                    .map(this::especifEntidadADto)
                    .toList();
            dto.setUnidadReceptora(especifDTOS);
        }

        dto.setFactRiesgoNoCrimin(entidad.getFactRiesgoNoCrimin());
        dto.setValRiesgo(entidad.getValRiesgo() != null ? entidad.getValRiesgo() : "N/A");
        dto.setHipotExplicativa(entidad.getHipotExplicativa());
        dto.setIntensidadIntervTrat(entidad.getIntensidadIntervTrat());
        dto.setTipoCentro(entidad.getTipoCentro());
        dto.setTipoAbierto(entidad.getTipoAbierto() != null ? entidad.getTipoAbierto() : "N/A");

        if (entidad.getIntervObjetivos() != null) {
            List<PlanTratamientoIndIntervDTO> intervDTOS = entidad.getIntervObjetivos().stream()
                    .map(this::intervEntidadADto)
                    .toList();
            dto.setIntervObjetivos(intervDTOS);
        }

        if (entidad.getIntervNoCriminogenos() != null) {
            List<PlanTratamientoIndIntervDTO> intervDTOS = entidad.getIntervNoCriminogenos().stream()
                    .map(this::intervEntidadADto)
                    .toList();
            dto.setIntervNoCriminogenos(intervDTOS);
        }

        if (entidad.getIntervDiferenciada() != null) {
            List<PlanTratamientoIndIntervDTO> intervDTOS = entidad.getIntervDiferenciada().stream()
                    .map(this::intervEntidadADto)
                    .toList();
            dto.setIntervDiferenciada(intervDTOS);
        }

        if (entidad.getIntervMedidas() != null) {
            List<PlanTratamientoIndIntervDTO> intervDTOS = entidad.getIntervMedidas().stream()
                    .map(this::intervEntidadADto)
                    .toList();
            dto.setIntervMedidas(intervDTOS);
        }

        return dto;
    }

    private PlanTratamientoIndEspecifDTO especifEntidadADto(PlanTratamientoIndEspecif entidad) {
        if (entidad == null) return null;

        PlanTratamientoIndEspecifDTO dto = new PlanTratamientoIndEspecifDTO();
        dto.setIdPlanTratIndEspecif(entidad.getIdPlanTratIndEspecif());
        if (entidad.getDimension() != null) dto.setDimension(entidadADtoCatalogo(entidad.getDimension()));
        dto.setFactorRiesgo(entidad.getFactorRiesgo());
        dto.setFactorProtector(entidad.getFactorProtector());
        dto.setComentario(entidad.getComentario());
        return dto;
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
