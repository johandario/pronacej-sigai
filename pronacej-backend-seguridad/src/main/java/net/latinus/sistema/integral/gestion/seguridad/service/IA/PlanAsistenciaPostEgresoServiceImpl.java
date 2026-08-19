package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanAsistenciaPostEgresoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaAsistenciaPostEgresoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlanAsistenciaPostEgresoServiceImpl implements PlanAsistenciaPostEgresoService {
    private PaginacionService paginacionService;
    private CatalogoRepository catalogoRepository;
    private PlanAsistenciaPostEgresoRepository planAsistenciaPostEgresoRepository;
    private PlanAsistenciaPostEgresoCarpetaRepository planAsistenciaPostEgresoCarpetaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private CarpetaRepository carpetaRepository;

    private JwtProviderService jwtProviderService;
    private CarpetaService carpetaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlanAsistenciaPostEgresoDTO>> obtenerPlanes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanAsistenciaPostEgresoDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            List<PlanAsistenciaPostEgreso> planes = this.planAsistenciaPostEgresoRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false);

            PaginacionResponse<PlanAsistenciaPostEgresoDTO> paginacionResponse;
            List<PlanAsistenciaPostEgresoDTO> planesDTOLIst = new ArrayList<>();

            for (PlanAsistenciaPostEgreso planAsistencia : planes) {

                PlanAsistenciaPostEgresoDTO dto = entidadADto(planAsistencia);
                if (planAsistencia.getEstado() != null) {
                    dto.setNombreEstado(planAsistencia.getEstado().getNombre());
                }
                dto.setFechaCreacion(planAsistencia.getFechaCreacion());
                dto.setTokenIdentificador(planAsistencia.getTokenIdentificador());
                dto.setIdFichaIdentificacion(planAsistencia.getFichaIdentificacion().getIdFichaIdentificacion());

                String pattern = "dd-MM-yyyy";
                DateFormat fecha = new SimpleDateFormat(pattern);

                dto.setFecInicio(fecha.format(dto.getFechaInicio()));
                dto.setFecFin(fecha.format(dto.getFechaFin()));

                pattern = "dd-MM-yyyy HH:mm:ss";
                fecha = new SimpleDateFormat(pattern);
                dto.setFecCreacion(fecha.format(dto.getFechaCreacion()));

                planesDTOLIst.add(dto);
            }

            planesDTOLIst.sort(
                    //Comparator.comparing(plan -> plan.getEstado().getNombre())
                    Comparator.comparing(PlanAsistenciaPostEgresoDTO::getFechaCreacion).reversed()
                            .thenComparing(plan -> plan.getEstado().getNombre())
            );

            paginacionResponse = paginacionService.obtenerDatos(planesDTOLIst, paginacionRequest);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + planesDTOLIst.size() + " de: " + planes.size() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> crearPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            PlanAsistenciaPostEgresoDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanAsistenciaPostEgresoDTO.class);

            PlanAsistenciaPostEgreso planEncontrado = this.planAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenIdentificador(), false);

            if (planEncontrado != null && !planDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un registro con la misma data");
                return df;
            }

            List<PlanAsistenciaPostEgreso> planes = this.planAsistenciaPostEgresoRepository.findByFichaIdentificacionTokenIdentificador(planDTO.getTokenFichaIdenticacion());
            Catalogo catalogoEncontrado = this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_PLAN_ASISTENCIA_FINALIZADO", false);
            planes.forEach(planTratamientoInd -> {
                planTratamientoInd.setEstado(catalogoEncontrado);
            });
            this.planAsistenciaPostEgresoRepository.saveAll(planes);

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenFichaIdenticacion(), false);

            // CREACIÓN DE CARPETA POST EGRESO EN CASO DE QUE NO EXISTA
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(planDTO.getTokenFichaIdenticacion(), null, false);
            Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

            String nemonicoPlanAsistencia = EtiquetaNemonico.CARPETA_GESTION_ADOLES_POST_EGRESO;
            FichaIdentificacionCarpeta fichaCarpetaPostEgreso = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(planDTO.getTokenFichaIdenticacion(), nemonicoPlanAsistencia, false);

            if (fichaCarpetaPostEgreso == null) {
                String nombreCarpetaPrincipal = "Post Egreso";

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                carpetaDTO.setDescripcion("Carpeta principal de post egreso");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                fichaCarpetaPostEgreso = new FichaIdentificacionCarpeta();
                fichaCarpetaPostEgreso.setCarpeta(carpetaGuardadaRecientemente);
                fichaCarpetaPostEgreso.setFichaIdentificacion(ficha);
                Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoPlanAsistencia, false);
                fichaCarpetaPostEgreso.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                fichaCarpetaPostEgreso.setFechaCreacion(new Date());
                fichaCarpetaPostEgreso.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaCarpetaPostEgreso.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.fichaIdentificacionCarpetaRepository.save(fichaCarpetaPostEgreso);
            }

            PlanAsistenciaPostEgreso plan = dtoAEntidad(planDTO);
            if (planEncontrado == null && !planDTO.getEsEdicion()) {
                plan.setFechaCreacion(new Date());
                plan.setIpCrea(httpServletRequest.getRemoteAddr());
                plan.setEmpresa(df2.getData().getEmpresa());
                plan.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                plan.setFichaIdentificacion(ficha);

                plan = this.planAsistenciaPostEgresoRepository.save(plan);

                // CREACIÓN DE CARPETA PLAN ASISTENCIA
                String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_POST_EGRESO;
                FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(plan.getFichaIdentificacion().getTokenIdentificador(), nemonico, false);
                Carpeta carpetaPadrePostEgreso = fichaIdentificacionCarpeta.getCarpeta();

                String pattern = "yyyy-MM-dd-HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                String fechaFormateada = fecha.format(plan.getFechaCreacion());

                String nombreCarpeta = "plan-asistencia_" + fechaFormateada;

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de plan de asistencia: " + plan.getTokenIdentificador());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePostEgreso.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                PlanAsistenciaPostEgresoCarpeta carpetaDetalle = new PlanAsistenciaPostEgresoCarpeta();
                carpetaDetalle.setCarpeta(carpetaGuardada);
                carpetaDetalle.setPlanAsistenciaPostEgreso(plan);
                carpetaDetalle.setFechaCreacion(new Date());
                carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.planAsistenciaPostEgresoCarpetaRepository.save(carpetaDetalle);

                df.llenarRespuestaExitosa("Se ha creado con éxito el plan.", entidadADto(plan));
            } else {
                plan.setFechaEdicion(new Date());
                plan.setIpEdita(httpServletRequest.getRemoteAddr());
                plan.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                plan = this.planAsistenciaPostEgresoRepository.save(plan);

                // CREACIÓN DE CARPETA PLAN ASISTENCIA EN CASO DE QUE NO EXISTA
                String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_POST_EGRESO;
                FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(plan.getFichaIdentificacion().getTokenIdentificador(), nemonico, false);
                Carpeta carpetaPadrePostEgreso = fichaIdentificacionCarpeta.getCarpeta();

                PlanAsistenciaPostEgresoCarpeta carpetaEncontrada = this.planAsistenciaPostEgresoCarpetaRepository.findFirstByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(plan.getTokenIdentificador(), false);

                if (carpetaEncontrada == null) {
                    String pattern = "yyyy-MM-dd-HH:mm:ss";
                    DateFormat fecha = new SimpleDateFormat(pattern);
                    String fechaFormateada = fecha.format(plan.getFechaCreacion());

                    String nombreCarpeta = "plan-asistencia_" + fechaFormateada;

                    CarpetaDTO carpetaDTO = new CarpetaDTO();
                    carpetaDTO.setNombreCliente(nombreCarpeta);
                    carpetaDTO.setDescripcion("Carpeta de plan de asistencia: " + plan.getTokenIdentificador());
                    CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                    carpetaPadreDTO.setTokenIdentificador(carpetaPadrePostEgreso.getTokenIdentificador());
                    carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);
                    this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                    Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                    PlanAsistenciaPostEgresoCarpeta carpetaDetalle = new PlanAsistenciaPostEgresoCarpeta();
                    carpetaDetalle.setCarpeta(carpetaGuardada);
                    carpetaDetalle.setPlanAsistenciaPostEgreso(plan);
                    carpetaDetalle.setFechaCreacion(new Date());
                    carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                    carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                    this.planAsistenciaPostEgresoCarpetaRepository.save(carpetaDetalle);
                }


                df.llenarRespuestaExitosa("Se ha editado con éxito el plan.", entidadADto(plan));
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> eliminarPlan(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            PlanAsistenciaPostEgresoDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanAsistenciaPostEgresoDTO.class);

            PlanAsistenciaPostEgreso planEncontrado = this.planAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenIdentificador(), false);

            if (planEncontrado == null) {
                df.setMensaje("No existe el registro buscado.");
                return df;
            }

            PlanAsistenciaPostEgreso plan = dtoAEntidad(planDTO);
            plan.setRemovido(true);
            plan.setFechaEliminacion(new Date());
            plan.setIpElimina(httpServletRequest.getRemoteAddr());
            plan.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenFichaIdenticacion(), false);
            plan.setFichaIdentificacion(ficha);

            this.planAsistenciaPostEgresoRepository.save(plan);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito el plan.", entidadADto(plan));


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> obtenerPlanPorTokenIdentificador(HttpServletRequest httpServletRequest, String tokenIdentificadorPlan) {
        RespuestaPorDefectoAuditoria<PlanAsistenciaPostEgresoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            PlanAsistenciaPostEgreso planAsistenciaPostEgreso = this.planAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(tokenIdentificadorPlan, false);

            if (planAsistenciaPostEgreso == null) {
                df.setMensaje("No existe el documento solicitado.");
                return df;
            }

            PlanAsistenciaPostEgresoDTO planAsistenciaPostEgresoDTO = entidadADto(planAsistenciaPostEgreso);
            planAsistenciaPostEgresoDTO.setTokenFichaIdenticacion(planAsistenciaPostEgreso.getFichaIdentificacion().getTokenIdentificador());
            planAsistenciaPostEgresoDTO.setTokenIdentificador(planAsistenciaPostEgreso.getTokenIdentificador());

            df.llenarRespuestaExitosa("Se ha encontrado el plan de asistencia: " + planAsistenciaPostEgresoDTO.getIdPlanAsistenciaPostEgreso(), planAsistenciaPostEgresoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private PlanAsistenciaPostEgreso dtoAEntidad(PlanAsistenciaPostEgresoDTO dto) {
        if (dto == null) return null;

        PlanAsistenciaPostEgreso plan = this.planAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        PlanAsistenciaPostEgreso entidad = Objects.requireNonNullElseGet(plan, PlanAsistenciaPostEgreso::new);

        entidad.setIdPlanAsistenciaPostEgreso(dto.getIdPlanAsistenciaPostEgreso());
        entidad.setEstado(dtoAEntidadCatalogo(dto.getEstado()));
        entidad.setFechaInicio(dto.getFechaInicio());
        entidad.setFechaFin(dto.getFechaFin());

        if (dto.getPlanDetalle() != null) {
            List<PlanAsistenciaPostEgresoDetalle> detalles = dto.getPlanDetalle().stream()
                    .map(this::detalleDtoAEntidad)
                    .collect(Collectors.toList());
            detalles.forEach(detalle -> detalle.setPlanAsistencia(entidad));
            entidad.setPlanDetalle(detalles);
        }

        return entidad;
    }

    private PlanAsistenciaPostEgresoDetalle detalleDtoAEntidad(PlanAsistenciaPostEgresoDetalleDTO dto) {
        if (dto == null) return null;

        PlanAsistenciaPostEgresoDetalle entidad = new PlanAsistenciaPostEgresoDetalle();
        entidad.setIdPlanAsistenciaPostEgresoDetalle(dto.getIdPlanAsistenciaPostEgresoDetalle());
        entidad.setArea(dtoAEntidadCatalogo(dto.getArea()));
        entidad.setFactores(dto.getFactores());
        entidad.setObjetivoGeneral(dto.getObjetivoGeneral());
        entidad.setObjetivoEspecifico(dto.getObjetivoEspecifico());
        entidad.setActividades(dto.getActividades());
        entidad.setInstitucion(dto.getInstitucion());
        entidad.setFrecuencia(dto.getFrecuencia());
        entidad.setIndicador(dto.getIndicador());
        return entidad;
    }

    private PlanAsistenciaPostEgresoDTO entidadADto(PlanAsistenciaPostEgreso entidad) {
        if (entidad == null) return null;

        PlanAsistenciaPostEgresoDTO dto = new PlanAsistenciaPostEgresoDTO();

        dto.setIdPlanAsistenciaPostEgreso(entidad.getIdPlanAsistenciaPostEgreso());
        dto.setEstado(entidadADtoCatalogo(entidad.getEstado()));
        dto.setFechaInicio(entidad.getFechaInicio());
        dto.setFechaFin(entidad.getFechaFin());

        if (entidad.getPlanDetalle() != null) {
            List<PlanAsistenciaPostEgresoDetalleDTO> detalleDTOS = entidad.getPlanDetalle().stream()
                    .map(this::detalleEntidadADto)
                    .toList();
            dto.setPlanDetalle(detalleDTOS);
        }

        return dto;

    }

    private PlanAsistenciaPostEgresoDetalleDTO detalleEntidadADto(PlanAsistenciaPostEgresoDetalle entidad) {
        if (entidad == null) return null;

        PlanAsistenciaPostEgresoDetalleDTO dto = new PlanAsistenciaPostEgresoDetalleDTO();
        dto.setIdPlanAsistenciaPostEgresoDetalle(entidad.getIdPlanAsistenciaPostEgresoDetalle());
        dto.setArea(entidadADtoCatalogo(entidad.getArea()));
        dto.setFactores(entidad.getFactores());
        dto.setObjetivoGeneral(entidad.getObjetivoGeneral());
        dto.setObjetivoEspecifico(entidad.getObjetivoEspecifico());
        dto.setActividades(entidad.getActividades());
        dto.setInstitucion(entidad.getInstitucion());
        dto.setFrecuencia(entidad.getFrecuencia());
        dto.setIndicador(entidad.getIndicador());
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

    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }
}
