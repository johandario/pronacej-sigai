package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.InformeFinalAsistenciaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InformeFinalAsistenciaServiceImpl implements InformeFinalAsistenciaService {
    private PaginacionService paginacionService;
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private InformeFinalAsistenciaRepository informeFinalAsistenciaRepository;
    private PlanAsistenciaPostEgresoRepository planAsistenciaPostEgresoRepository;
    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAsistenciaDTO>> obtenerInformes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAsistenciaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

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
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            List<InformeFinalAsistencia> planes = this.informeFinalAsistenciaRepository.findByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false);

            PaginacionResponse<InformeFinalAsistenciaDTO> paginacionResponse;
            List<InformeFinalAsistenciaDTO> planesDTOLIst = new ArrayList<>();

            for (InformeFinalAsistencia planAsistencia : planes) {

                InformeFinalAsistenciaDTO dto = entidadADto(planAsistencia);
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
                    Comparator.comparing(plan -> plan.getEstado().getNombre())
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
    public RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> crearInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

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
            String bodyDecifrado = df22.getData();

            InformeFinalAsistenciaDTO planDTO = new Gson().fromJson(bodyDecifrado, InformeFinalAsistenciaDTO.class);

            InformeFinalAsistencia planEncontrado = this.informeFinalAsistenciaRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenIdentificador(), false);

            if (planEncontrado != null && !planDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un registro con la misma data");
                return df;
            }

            List<InformeFinalAsistencia> planes = this.informeFinalAsistenciaRepository.findByFichaIdentificacionTokenIdentificador(planDTO.getTokenFichaIdenticacion());
            Catalogo catalogoEncontrado = this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_PLAN_ASISTENCIA_FINALIZADO", false);
            planes.forEach(planTratamientoInd -> {
                planTratamientoInd.setEstado(catalogoEncontrado);
            });
            this.informeFinalAsistenciaRepository.saveAll(planes);

            InformeFinalAsistencia plan = dtoAEntidad(planDTO);
            if (planEncontrado == null && !planDTO.getEsEdicion()) {
                plan.setFechaCreacion(new Date());
                plan.setIpCrea(httpServletRequest.getRemoteAddr());
                plan.setEmpresa(df2.getData().getEmpresa());
                plan.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());

                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenFichaIdenticacion(), false);
                plan.setFichaIdentificacion(ficha);

                PlanAsistenciaPostEgreso planAsistenciaPostEgreso = this.planAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenPlanAsistencia(), false);
                plan.setPlanAsistenciaPostEgreso(planAsistenciaPostEgreso);

                this.informeFinalAsistenciaRepository.save(plan);

                df.llenarRespuestaExitosa("Se ha creado con éxito el plan.", entidadADto(plan));
            } else {
                plan.setFechaEdicion(new Date());
                plan.setIpEdita(httpServletRequest.getRemoteAddr());
                plan.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                plan = this.informeFinalAsistenciaRepository.save(plan);

                df.llenarRespuestaExitosa("Se ha editado con éxito el plan.", entidadADto(plan));
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;    
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> eliminarInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InformeFinalAsistenciaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

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
            String bodyDecifrado = df22.getData();

            InformeFinalAsistenciaDTO planDTO = new Gson().fromJson(bodyDecifrado, InformeFinalAsistenciaDTO.class);

            InformeFinalAsistencia planEncontrado = this.informeFinalAsistenciaRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenIdentificador(), false);

            if (planEncontrado == null) {
                df.setMensaje("No existe el registro buscado.");
                return df;
            }

            InformeFinalAsistencia plan = dtoAEntidad(planDTO);
            plan.setRemovido(true);
            plan.setFechaEliminacion(new Date());
            plan.setIpElimina(httpServletRequest.getRemoteAddr());
            plan.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenFichaIdenticacion(), false);
            plan.setFichaIdentificacion(ficha);

            PlanAsistenciaPostEgreso planAsistenciaPostEgreso = this.planAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenPlanAsistencia(), false);
            plan.setPlanAsistenciaPostEgreso(planAsistenciaPostEgreso);

            this.informeFinalAsistenciaRepository.save(plan);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito el plan.", entidadADto(plan));


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private InformeFinalAsistencia dtoAEntidad(InformeFinalAsistenciaDTO dto) {
        if (dto == null) return null;

        InformeFinalAsistencia plan = this.informeFinalAsistenciaRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        InformeFinalAsistencia entidad = Objects.requireNonNullElseGet(plan, InformeFinalAsistencia::new);

        entidad.setIdInformeFinalAsistencia(dto.getIdInformeFinalAsistencia());
        entidad.setEstado(dtoAEntidadCatalogo(dto.getEstado()));
        entidad.setFechaInicio(dto.getFechaInicio());
        entidad.setFechaFin(dto.getFechaFin());

        if (dto.getDetalle() != null) {
            List<InformeFinalAsistenciaDetalle> detalles = dto.getDetalle().stream()
                    .map(this::detalleDtoAEntidad)
                    .collect(Collectors.toList());
            detalles.forEach(detalle -> detalle.setInformeFinalAsistencia(entidad));
            entidad.setDetalle(detalles);
        }

        return entidad;
    }

    private InformeFinalAsistenciaDetalle detalleDtoAEntidad(InformeFinalAsistenciaDetalleDTO dto) {
        if (dto == null) return null;

        InformeFinalAsistenciaDetalle entidad = new InformeFinalAsistenciaDetalle();
        entidad.setIdInformeFinalAsistenciaDetalle(dto.getIdInformeFinalAsistenciaDetalle());
        entidad.setArea(dtoAEntidadCatalogo(dto.getArea()));
        entidad.setObjetivoGeneral(dto.getObjetivoGeneral());
        entidad.setObjetivoEspecifico(dto.getObjetivoEspecifico());
        entidad.setActividades(dto.getActividades());
        entidad.setDescripcionActividad(dto.getDescripcionActividad());
        entidad.setLogro(dto.getLogro());
        entidad.setDificultad(dto.getDificultad());
        return entidad;
    }

    private InformeFinalAsistenciaDTO entidadADto(InformeFinalAsistencia entidad) {
        if (entidad == null) return null;

        InformeFinalAsistenciaDTO dto = new InformeFinalAsistenciaDTO();

        dto.setIdInformeFinalAsistencia(entidad.getIdInformeFinalAsistencia());
        dto.setEstado(entidadADtoCatalogo(entidad.getEstado()));
        dto.setFechaInicio(entidad.getFechaInicio());
        dto.setFechaFin(entidad.getFechaFin());

        if (entidad.getDetalle() != null) {
            List<InformeFinalAsistenciaDetalleDTO> detalleDTOS = entidad.getDetalle().stream()
                    .map(this::detalleEntidadADto)
                    .toList();
            dto.setDetalle(detalleDTOS);
        }

        return dto;

    }

    private InformeFinalAsistenciaDetalleDTO detalleEntidadADto(InformeFinalAsistenciaDetalle entidad) {
        if (entidad == null) return null;

        InformeFinalAsistenciaDetalleDTO dto = new InformeFinalAsistenciaDetalleDTO();
        dto.setIdInformeFinalAsistenciaDetalle(entidad.getIdInformeFinalAsistenciaDetalle());
        dto.setArea(entidadADtoCatalogo(entidad.getArea()));
        dto.setObjetivoGeneral(entidad.getObjetivoGeneral());
        dto.setObjetivoEspecifico(entidad.getObjetivoEspecifico());
        dto.setActividades(entidad.getActividades());
        dto.setDescripcionActividad(entidad.getDescripcionActividad());
        dto.setLogro(entidad.getLogro());
        dto.setDificultad(entidad.getDificultad());
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
