package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadOcupacional;
import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoActividadOcupacional;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SeguimientoActividadOcupacionalDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.ActividadOcupacionalRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SeguimientoActividadOcupacionalRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class SeguimientoActividadOcupacionalServiceImpl implements SeguimientoActividadOcupacionalService {

    private SeguimientoActividadOcupacionalRepository seguimientoActividadOcupacionalRepository;
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private ActividadOcupacionalRepository actividadOcupacionalRepository;

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<SeguimientoActividadOcupacionalDTO> crearSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<SeguimientoActividadOcupacionalDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            SeguimientoActividadOcupacionalDTO seguimientoDTO = new Gson().fromJson(body, SeguimientoActividadOcupacionalDTO.class);

            Optional<ActividadOcupacional> actividadOcupacional = this.actividadOcupacionalRepository
                    .findByTokenIdentificadorAndRemovido(seguimientoDTO.getTokenIdentificadorActividadOcupacional(), false);

            if (actividadOcupacional.isEmpty()) {
                df.setMensaje("La actividad ocupacional no fue encontrada o ya fue eliminada.");
                return df;
            }

            SeguimientoActividadOcupacional seguimiento;

            if (seguimientoDTO.getEsEdicion()) {
                // Edición: Buscar el seguimiento por token
                seguimiento = this.seguimientoActividadOcupacionalRepository
                        .findByTokenIdentificadorAndRemovido(seguimientoDTO.getTokenIdentificador(), false);

                if (seguimiento == null) {
                    df.setMensaje("El seguimiento no fue encontrado o ya fue eliminado.");
                    return df;
                }
            } else {
                // Creación: Instanciar nuevo seguimiento
                seguimiento = new SeguimientoActividadOcupacional();
            }

            // Llenar los datos del seguimiento
            if (!ObjectUtils.isEmpty(seguimientoDTO.getActividad())) {
                seguimiento.setActividad(seguimientoDTO.getActividad());
            }
            if (!ObjectUtils.isEmpty(seguimientoDTO.getVigente())) {
                seguimiento.setVigente(seguimientoDTO.getVigente());
            }
            if (!ObjectUtils.isEmpty(seguimientoDTO.getObservaciones())) {
                seguimiento.setObservaciones(seguimientoDTO.getObservaciones());
            }
            if (!ObjectUtils.isEmpty(seguimientoDTO.getFechaActividad())) {
                seguimiento.setFechaActividad(seguimientoDTO.getFechaActividad());
            }

            // Relación con la actividad ocupacional
            seguimiento.setActividadOcupacional(actividadOcupacional.get());

            // Guardar en la BD
            this.seguimientoActividadOcupacionalRepository.save(seguimiento);

            // Respuesta exitosa
            df.llenarRespuestaExitosa(
                    seguimientoDTO.getEsEdicion() ? "El seguimiento ha sido actualizado correctamente." : "El seguimiento ha sido creado correctamente.",
                    seguimientoDTO
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;

    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoActividadOcupacionalDTO>> obtenerSeguimientosPorActividad(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoActividadOcupacionalDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener JWT y validar
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            String tokenIdActividadOcupacional = paginacionRequest.getTokenIdentificador();

            // Configurar paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("fechaActividad").descending()
            );


            // Consultar los seguimientos relacionados a la actividad ocupacional
            Page<SeguimientoActividadOcupacional> seguimientosPage = this.seguimientoActividadOcupacionalRepository
                    .findByActividadOcupacionalTokenIdentificadorAndRemovido(tokenIdActividadOcupacional, false, pageable);

            // Mapear a DTO
            List<SeguimientoActividadOcupacionalDTO> seguimientoDTOs = seguimientosPage.getContent().stream()
                    .map(seguimiento -> {
                        SeguimientoActividadOcupacionalDTO dto = new SeguimientoActividadOcupacionalDTO();
                        dto.setTokenIdentificador(seguimiento.getTokenIdentificador());

                        if (!ObjectUtils.isEmpty(seguimiento.getActividad())) {
                            dto.setActividad(seguimiento.getActividad());
                        }
                        if (!ObjectUtils.isEmpty(seguimiento.getVigente())) {
                            dto.setVigente(seguimiento.getVigente());
                        }
                        if (!ObjectUtils.isEmpty(seguimiento.getObservaciones())) {
                            dto.setObservaciones(seguimiento.getObservaciones());
                        }
                        if (!ObjectUtils.isEmpty(seguimiento.getFechaActividad())) {
                            dto.setFechaActividad(seguimiento.getFechaActividad());
                        }
                        dto.setTokenIdentificadorActividadOcupacional(tokenIdActividadOcupacional);

                        return dto;
                    })
                    .toList();

            // Crear la respuesta paginada
            PaginacionResponse<SeguimientoActividadOcupacionalDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(seguimientoDTOs);
            paginacionResponse.setTotalItems(seguimientosPage.getTotalElements());

            df.llenarRespuestaExitosa("Lista de seguimientos obtenida correctamente.", paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<SeguimientoActividadOcupacionalDTO> obtenerSeguimientoPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        return null;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar usuario autenticado
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            SeguimientoActividadOcupacionalDTO seguimientoDTO = new Gson().fromJson(body, SeguimientoActividadOcupacionalDTO.class);

            // Buscar el seguimiento por su tokenIdentificador
            SeguimientoActividadOcupacional seguimiento = this.seguimientoActividadOcupacionalRepository
                    .findByTokenIdentificadorAndRemovido(seguimientoDTO.getTokenIdentificador(), false);

            if (seguimiento == null) {
                df.setMensaje("El seguimiento no fue encontrado o ya fue eliminado.");
                return df;
            }

            // Marcar como eliminado
            seguimiento.setRemovido(true);
            seguimiento.setFechaEliminacion(new Date());
            seguimiento.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());
            seguimiento.setIpElimina(httpServletRequest.getRemoteAddr());

            // Guardar cambios
            this.seguimientoActividadOcupacionalRepository.save(seguimiento);

            df.llenarRespuestaExitosa("El seguimiento ha sido eliminado correctamente.", true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
