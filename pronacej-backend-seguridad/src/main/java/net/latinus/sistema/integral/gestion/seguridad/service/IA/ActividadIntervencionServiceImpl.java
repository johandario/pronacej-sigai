package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadIntervencion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndInterv;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ActividadIntervencionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndIntervRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ActividadIntervencionServiceImpl implements ActividadIntervencionService{

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private ActividadIntervencionRepository actividadIntervencionRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private PlanTratamientoIndIntervRepository planRepository;

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> crearActualizarActividadIntervencion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            ActividadIntervencionDTO actividadIntervencionDTO = new Gson().fromJson(bodyString, ActividadIntervencionDTO.class);

            ActividadIntervencion actividad;

            if (actividadIntervencionDTO.getEsEdicion() != null && actividadIntervencionDTO.getEsEdicion()) {
                Optional<ActividadIntervencion> optionalActividad = actividadIntervencionRepository.
                        findById(actividadIntervencionDTO.getIdActividadIntervencion());
                if (!optionalActividad.isPresent()) {
                    df.setMensaje("No existe la actividad intervencion solicitada.");
                    return df;
                }
                actividad = optionalActividad.get();
            }else {
                actividad = new ActividadIntervencion();
            }

            Optional<PlanTratamientoIndInterv> planEntity = this.planRepository.
                    findByIdPlanTratIndInterv(actividadIntervencionDTO.getIdPlanTratIndInterv());

            actividad.setPlanTratamientoIndInterv(planEntity.get());
            actividad.setSubactividad(actividadIntervencionDTO.getSubactividad());
            actividad.setFrecuencia(this.catalogoRepository.
                    findByTokenIdentificadorAndRemovido(actividadIntervencionDTO.getFrecuencia().getTokenIdentificador(), false));
            actividad.setFechaInicio(actividadIntervencionDTO.getFechaInicio());
            actividad.setFechaFin(actividadIntervencionDTO.getFechaFin());
            actividad.setUsuarioSistemaEdita(usuarioSistema);
            actividad.setIpEdita(ip);
            actividad.setFechaEdicion(new Date());
            actividad.setRemovido(false);

            this.actividadIntervencionRepository.save(actividad);
            df.llenarRespuestaExitosa("Se ha actualizado la actividad intervencion: " +
                    actividad.getIdActividadIntervencion(), actividadIntervencionDTO);
        }
        catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadIntervencionDTO>> obtenerActividadesIntervencionPorIdPlanTratIndInterv(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadIntervencionDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación y obtención del JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el cuerpo de la solicitud y mapear a PaginacionRequest
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);


            // Configurar la paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idActividadIntervencion").descending()
            );

            // Realizar la consulta paginada por idPlanTratIndInterv y removido = false
            Page<ActividadIntervencion> actividadPage = this.actividadIntervencionRepository
                    .findByPlanTratamientoIndIntervIdPlanTratIndIntervAndRemovido(
                            Long.parseLong(paginacionRequest.getFilter()), // Asumiendo que PaginacionRequest contiene este campo
                            false,
                            pageable
                    );

            // Mapear las entidades a DTOs
            List<ActividadIntervencionDTO> actividadDTOList = new ArrayList<>();
            for (ActividadIntervencion actividad : actividadPage.toList()) {
                ActividadIntervencionDTO dto = new ActividadIntervencionDTO();
                dto.setIdActividadIntervencion(actividad.getIdActividadIntervencion());
                dto.setIdPlanTratIndInterv(
                        actividad.getPlanTratamientoIndInterv() != null
                                ? actividad.getPlanTratamientoIndInterv().getIdPlanTratIndInterv()
                                : null
                );
                // Suponiendo que la frecuencia se mapea directamente
                dto.setSubactividad(actividad.getSubactividad());
                dto.setFrecuencia(catalogoToDTO(actividad.getFrecuencia()));
                dto.setFechaInicio(actividad.getFechaInicio());
                dto.setFechaFin(actividad.getFechaFin());
                // Mapea otros campos necesarios si aplica

                actividadDTOList.add(dto);
            }

            // Preparar la respuesta paginada
            PaginacionResponse<ActividadIntervencionDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(actividadDTOList);
            paginacionResponse.setTotalItems(actividadPage.getTotalElements());

            df.llenarRespuestaExitosa(
                    "Se han encontrado " + actividadDTOList.size() + " actividades de intervención de un total de "
                            + actividadPage.getTotalElements(),
                    paginacionResponse
            );
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> getActividadIntervencionPorId(HttpServletRequest request, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar y obtener JWT si es necesario, similar a otros métodos.
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(request);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String bodyString = df22.getData();
            Long idActividad = Long.valueOf(bodyString.trim());

            Optional<ActividadIntervencion> optionalActividad = actividadIntervencionRepository.findById(idActividad);
            if (!optionalActividad.isPresent()) {
                respuesta.setExito(false);
                respuesta.setMensaje("ActividadIntervencion no encontrada para el ID proporcionado.");
                return respuesta;
            }
            ActividadIntervencion actividad = optionalActividad.get();

            ActividadIntervencionDTO dto = new ActividadIntervencionDTO();
            dto.setIdActividadIntervencion(actividad.getIdActividadIntervencion());
            dto.setIdPlanTratIndInterv(
                    actividad.getPlanTratamientoIndInterv() != null ?
                            actividad.getPlanTratamientoIndInterv().getIdPlanTratIndInterv() : null
            );
            dto.setSubactividad(actividad.getSubactividad());
            dto.setFrecuencia(catalogoToDTO(actividad.getFrecuencia()));
            dto.setFechaInicio(actividad.getFechaInicio());
            dto.setFechaFin(actividad.getFechaFin());

            respuesta.setExito(true);
            respuesta.setData(dto);
            respuesta.setMensaje("ActividadIntervencion encontrada con éxito.");
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> eliminarActividadIntervencion(HttpServletRequest request, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ActividadIntervencionDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar y obtener JWT si es necesario
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(request);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }
            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            String ip = request.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String bodyString = df22.getData();
            Long idActividad = Long.valueOf(bodyString.trim());

            Optional<ActividadIntervencion> optionalActividad = actividadIntervencionRepository.findById(idActividad);
            if (!optionalActividad.isPresent()) {
                respuesta.setExito(false);
                respuesta.setMensaje("No se encontró la ActividadIntervencion con el ID proporcionado.");
                return respuesta;
            }
            ActividadIntervencion actividad = optionalActividad.get();

            actividad.setRemovido(true);
            actividad.setFechaEliminacion(new Date());
            actividad.setIpElimina(ip);
            actividad.setUsuarioSistemaElimina(usuarioSistema);

            actividadIntervencionRepository.save(actividad);

            respuesta.setExito(true);
            respuesta.setMensaje("La ActividadIntervencion ha sido eliminada (marcada como removida) correctamente.");
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    private CatalogoDTO catalogoToDTO(Catalogo catalogo){
        if (catalogo == null) {
            return null;
        }

        CatalogoDTO catalogoDTO = new CatalogoDTO();
        catalogoDTO.setNombre(catalogo.getNombre());
        catalogoDTO.setNemonico(catalogo.getNemonico());
        catalogoDTO.setDescripcion(catalogo.getDescripcion());
        catalogoDTO.setTokenIdentificador(catalogo.getTokenIdentificador());
        catalogoDTO.setCodigoExterno(catalogo.getCodigoExterno());

        return catalogoDTO;
    }
}
