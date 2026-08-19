package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadIntervencion;
import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadIntervencionSeguimiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ActividadIntervencionSeguimientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ActividadIntervencionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ActividadIntervencionSeguimientoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ActividadIntervencionSeguimientoServiceImpl implements ActividadIntervencionSeguimientoService {

    private ActividadIntervencionSeguimientoRepository actividadIntervencionSeguimientoRepository;
    private ActividadIntervencionRepository actividadIntervencionRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadIntervencionSeguimientoDTO>> obtenerSeguimientosPorActividadId(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadIntervencionSeguimientoDTO>> df
                = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación y obtención del JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el cuerpo de la solicitud y mapear a PaginacionRequest
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            // Configurar la paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idActividadIntervencionSeguimiento").descending()
            );

            // Realizar la consulta paginada por idActividadIntervencion y removido = false
            Page<ActividadIntervencionSeguimiento> seguimientoPage =
                    this.actividadIntervencionSeguimientoRepository
                            .findByActividadIdActividadIntervencionAndRemovido(
                                    Long.parseLong(paginacionRequest.getFilter()),
                                    false,
                                    pageable
                            );

            // Mapear las entidades a DTOs
            List<ActividadIntervencionSeguimientoDTO> seguimientoDTOList = new ArrayList<>();
            for (ActividadIntervencionSeguimiento seguimiento : seguimientoPage.toList()) {
                ActividadIntervencionSeguimientoDTO dto = new ActividadIntervencionSeguimientoDTO();
                dto.setIdActividadIntervencionSeguimiento(seguimiento.getIdActividadIntervencionSeguimiento());
                dto.setIdActividadIntervencion(
                        seguimiento.getActividad() != null
                                ? seguimiento.getActividad().getIdActividadIntervencion()
                                : null
                );
                dto.setFecha(seguimiento.getFecha());
                dto.setHoraInicio(seguimiento.getHoraInicio());
                dto.setHoraFin(seguimiento.getHoraFin());
                dto.setObservaciones(seguimiento.getObservaciones());
                // Mapea otros campos si es necesario

                seguimientoDTOList.add(dto);
            }

            // Preparar la respuesta paginada
            PaginacionResponse<ActividadIntervencionSeguimientoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(seguimientoDTOList);
            paginacionResponse.setTotalItems(seguimientoPage.getTotalElements());

            df.llenarRespuestaExitosa(
                    "Se han encontrado " + seguimientoDTOList.size() + " seguimientos de intervención de un total de "
                            + seguimientoPage.getTotalElements(),
                    paginacionResponse
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ActividadIntervencionSeguimientoDTO> crearActualizarActividadIntervencionSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ActividadIntervencionSeguimientoDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String bodyString = df22.getData();
            ActividadIntervencionSeguimientoDTO dto = new Gson().fromJson(bodyString, ActividadIntervencionSeguimientoDTO.class);

            ActividadIntervencionSeguimiento seguimiento;

            if (dto.getEsEdicion() != null && dto.getEsEdicion()) {
                if (dto.getIdActividadIntervencionSeguimiento() == null) {
                    respuesta.setMensaje("Se requiere idActividadIntervencionSeguimiento para la edición.");
                    return respuesta;
                }
                Optional<ActividadIntervencionSeguimiento> optionalSeguimiento =
                        actividadIntervencionSeguimientoRepository.findById(dto.getIdActividadIntervencionSeguimiento());
                if (!optionalSeguimiento.isPresent()) {
                    respuesta.setMensaje("No existe el seguimiento de intervención solicitado.");
                    return respuesta;
                }
                seguimiento = optionalSeguimiento.get();
            } else {
                seguimiento = new ActividadIntervencionSeguimiento();
            }

            Optional<ActividadIntervencion> optionalActividad =
                    actividadIntervencionRepository.findById(dto.getIdActividadIntervencion());
            seguimiento.setActividad(optionalActividad.get());

            seguimiento.setFecha(dto.getFecha());
            seguimiento.setHoraInicio(dto.getHoraInicio());
            seguimiento.setHoraFin(dto.getHoraFin());
            seguimiento.setObservaciones(dto.getObservaciones());
            seguimiento.setRemovido(false);
            // Actualizar campos de auditoría
            seguimiento.setUsuarioSistemaEdita(usuarioSistema);
            seguimiento.setIpEdita(ip);
            seguimiento.setFechaEdicion(new Date());

            // Guardar la entidad
            actividadIntervencionSeguimientoRepository.save(seguimiento);

            respuesta.llenarRespuestaExitosa("Se ha guardado el seguimiento de intervención con ID: "
                    + seguimiento.getIdActividadIntervencionSeguimiento(), dto);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }
}
