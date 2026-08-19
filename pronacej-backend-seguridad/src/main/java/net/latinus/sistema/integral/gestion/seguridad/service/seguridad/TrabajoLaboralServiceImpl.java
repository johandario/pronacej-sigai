package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.TrabajoLaboral;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.TrabajoLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.RegistroInstitucionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.TrabajoLaboralRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.springframework.stereotype.Service;

import net.latinus.sistema.integral.gestion.seguridad.model.both.TrabajoLaboralEstadisticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import java.util.stream.Collectors;

import java.util.*;

@Service
@AllArgsConstructor

public class TrabajoLaboralServiceImpl implements TrabajoLaboralService {

    private TrabajoLaboralRepository trabajoLaboralRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private PaginacionService paginacionService;
    private RegistroInstitucionRepository registroInstitucionRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TrabajoLaboralDTO>> obtenerListaTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrabajoLaboralDTO>> df =
                new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }
            RespuestaPorDefectoAuditoria<String> dfBody =
                    bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!dfBody.isExito()) {
                df.setMensaje(dfBody.getMensaje());
                return df;
            }
            PaginacionRequest request =
                    new Gson().fromJson(dfBody.getData(), PaginacionRequest.class);
            List<TrabajoLaboral> trabajos =
                    this.trabajoLaboralRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(
                            request.getTokenIdentificador(),
                            false
                    );
            List<TrabajoLaboralDTO> dtos = new ArrayList<>();
            for (TrabajoLaboral trabajo : trabajos) {
                dtos.add(entidadADto(trabajo));
            }
            dtos.sort(Comparator.comparing(TrabajoLaboralDTO::getFechaCreacion,
                    Comparator.nullsLast(Date::compareTo)).reversed());
            PaginacionResponse<TrabajoLaboralDTO> response =
                    this.paginacionService.obtenerDatos(dtos, request);
            df.llenarRespuestaExitosa(
                    "Se han encontrado un total de: " + dtos.size() + " registros.",
                    response
            );
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;

    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> crearTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> df =
                new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }
            RespuestaPorDefectoAuditoria<String> dfBody =
                    bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!dfBody.isExito()) {
                df.setMensaje(dfBody.getMensaje());
                return df;
            }
            TrabajoLaboralDTO dto =
                    new Gson().fromJson(dfBody.getData(), TrabajoLaboralDTO.class);
            boolean esEdicion = dto.getTokenIdentificador() != null && !dto.getTokenIdentificador().isBlank();
            TrabajoLaboral entidad = dtoAEntidad(dto);
            if (!esEdicion) {
                FichaIdentificacion ficha =
                        this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                                dto.getTokenFichaIdentificacion(),
                                false
                        );
                if (ficha == null) {
                    df.setMensaje("No se encontró la ficha de identificación asociada.");
                    return df;
                }
                entidad.setFichaIdentificacion(ficha);
                entidad.setFechaCreacion(new Date());
                entidad.setTokenIdentificador(UUID.randomUUID().toString());
                entidad.setRemovido(false);
                entidad.setBloqueado(false);
                System.out.println("========== CREAR TRABAJO LABORAL ==========");
                System.out.println("DTO: " + new Gson().toJson(dto));
                System.out.println("tokenFichaIdentificacion: " + dto.getTokenFichaIdentificacion());
                System.out.println("tokenRegistroInstitucion: " +
                        (dto.getRegistroInstitucion() != null ? dto.getRegistroInstitucion().getTokenIdentificador() : null)
                );
                System.out.println("esEdicion: " + esEdicion);
                entidad = this.trabajoLaboralRepository.save(entidad);
                df.llenarRespuestaExitosa(
                        "Se ha creado con éxito el trabajo laboral.",
                        entidadADto(entidad)
                );
            } else {
                if (entidad.getFichaIdentificacion() == null && dto.getTokenFichaIdentificacion() != null) {
                    FichaIdentificacion ficha =
                            this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                                    dto.getTokenFichaIdentificacion(),
                                    false
                            );

                    if (ficha == null) {
                        df.setMensaje("No se encontró la ficha de identificación asociada.");
                        return df;
                    }
                    entidad.setFichaIdentificacion(ficha);
                }

                entidad.setFechaEdicion(new Date());
                entidad = this.trabajoLaboralRepository.save(entidad);
                df.llenarRespuestaExitosa(
                        "Se ha editado con éxito el trabajo laboral.",
                        entidadADto(entidad)
                );
            }
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;

    }


    @Override
    public RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> obtenerTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<TrabajoLaboralDTO> df =
                new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }
            RespuestaPorDefectoAuditoria<String> dfBody =
                    bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!dfBody.isExito()) {
                df.setMensaje(dfBody.getMensaje());
                return df;
            }
            TrabajoLaboralDTO dto =
                    new Gson().fromJson(dfBody.getData(), TrabajoLaboralDTO.class);
            TrabajoLaboral trabajo =
                    this.trabajoLaboralRepository.findByTokenIdentificadorAndRemovido(
                            dto.getTokenIdentificador(),
                            false
                    );
            if (trabajo == null) {
                df.setMensaje("No se encontró el trabajo laboral solicitado.");
                return df;
            }
            df.llenarRespuestaExitosa(
                    "Registro encontrado correctamente.",
                    entidadADto(trabajo)
            );
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<Boolean> eliminarTrabajoLaboral(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado

    ) {
        RespuestaPorDefectoAuditoria<Boolean> df =
                new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;

            }
            RespuestaPorDefectoAuditoria<String> dfBody =
                    bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!dfBody.isExito()) {
                df.setMensaje(dfBody.getMensaje());
                return df;
            }
            TrabajoLaboralDTO dto =
                    new Gson().fromJson(dfBody.getData(), TrabajoLaboralDTO.class);
            TrabajoLaboral trabajo =
                    this.trabajoLaboralRepository.findByTokenIdentificadorAndRemovido(
                            dto.getTokenIdentificador(),
                            false
                    );
            if (trabajo == null) {
                df.setMensaje("No existe el registro buscado.");
                return df;
            }
            trabajo.setRemovido(true);
            trabajo.setFechaEliminacion(new Date());
            trabajo.setIpElimina(httpServletRequest.getRemoteAddr());
            trabajo.setUsuarioSistemaElimina(dfJwt.getData().getUsuarioSistema());
            this.trabajoLaboralRepository.save(trabajo);
            df.llenarRespuestaExitosa(
                    "Se ha eliminado con éxito el trabajo laboral.",
                    true
            );
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;

    }


    private TrabajoLaboral dtoAEntidad(TrabajoLaboralDTO dto) {
        if (dto == null) return null;
        TrabajoLaboral entidad = null;
        if (dto.getTokenIdentificador() != null && !dto.getTokenIdentificador().isBlank()) {
            entidad = this.trabajoLaboralRepository.findByTokenIdentificadorAndRemovido(
                    dto.getTokenIdentificador(),
                    false
            );
        }
        if (entidad == null) {
            entidad = new TrabajoLaboral();
        }
        entidad.setFechaIngresoLaboral(dto.getFechaIngresoLaboral());
        entidad.setCargoLaboral(dto.getCargoLaboral());
        if (dto.getRegistroInstitucion() != null
                && dto.getRegistroInstitucion().getTokenIdentificador() != null) {
            RegistroInstitucion institucion =
                    this.registroInstitucionRepository.findByTokenIdentificadorAndRemovido(
                            dto.getRegistroInstitucion().getTokenIdentificador(),
                            false
                    );
            if (institucion == null) {
                throw new IllegalArgumentException("No se encontró la institución seleccionada.");
            }
            entidad.setRegistroInstitucion(institucion);
        } else {
            entidad.setRegistroInstitucion(null);
        }
        return entidad;
    }


    @Override
    public RespuestaPorDefectoAuditoria<Long> obtenerCantidadTrabajoActivo(
            HttpServletRequest httpServletRequest
    ) {
        RespuestaPorDefectoAuditoria<Long> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!dfJwt.isExito()) {
                respuesta.setMensaje(dfJwt.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            Jerarquia jerarquiaActual = dfJwt.getData().getJerarquia();

            String tokenCentro = jerarquiaActual != null
                    ? jerarquiaActual.getTokenIdentificador()
                    : null;

            Long cantidad = this.trabajoLaboralRepository
                    .countAdolescentesConTrabajoActivo(tokenCentro);

            respuesta.llenarRespuestaExitosa(
                    "Cantidad de adolescentes con trabajo activo obtenida con éxito.",
                    cantidad
            );

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<TrabajoLaboralEstadisticoDTO>> obtenerEstadisticasTrabajoLaboral(
            HttpServletRequest httpServletRequest
    ) {
        RespuestaPorDefectoAuditoria<List<TrabajoLaboralEstadisticoDTO>> respuesta =
                new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!dfJwt.isExito()) {
                respuesta.setMensaje(dfJwt.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            Jerarquia jerarquiaActual = dfJwt.getData().getJerarquia();

            String tokenCentro = jerarquiaActual != null
                    ? jerarquiaActual.getTokenIdentificador()
                    : null;

            List<Object[]> resultados =
                    this.trabajoLaboralRepository.countTrabajoLaboralPorInstitucion(tokenCentro);

            List<TrabajoLaboralEstadisticoDTO> estadisticas = resultados.stream()
                    .map(obj -> new TrabajoLaboralEstadisticoDTO(
                            obj[0] != null ? obj[0].toString() : "Sin institución",
                            obj[1] != null ? obj[1].toString() : "Sin RUC",
                            obj[2] != null ? ((Number) obj[2]).intValue() : 0
                    ))
                    .collect(Collectors.toList());

            respuesta.llenarRespuestaExitosa(
                    "Estadísticas de trabajo laboral obtenidas con éxito.",
                    estadisticas
            );

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }


    private TrabajoLaboralDTO entidadADto(TrabajoLaboral entidad) {
        if (entidad == null) return null;
        TrabajoLaboralDTO dto = new TrabajoLaboralDTO();
        dto.setIdTrabajoLaboral(entidad.getIdTrabajoLaboral());
        dto.setFechaIngresoLaboral(entidad.getFechaIngresoLaboral());
        dto.setCargoLaboral(entidad.getCargoLaboral());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        if (entidad.getFichaIdentificacion() != null) {
            dto.setIdFichaIdentificacion(entidad.getFichaIdentificacion().getIdFichaIdentificacion());
            dto.setTokenFichaIdentificacion(entidad.getFichaIdentificacion().getTokenIdentificador());
        }
        if (entidad.getRegistroInstitucion() != null) {
            RegistroInstitucionDTO institucionDTO = new RegistroInstitucionDTO();
            institucionDTO.setIdRegistroInstitucion(entidad.getRegistroInstitucion().getIdRegistroInstitucion());
            institucionDTO.setTokenIdentificador(entidad.getRegistroInstitucion().getTokenIdentificador());
            institucionDTO.setNombreOrganizacion(entidad.getRegistroInstitucion().getNombreOrganizacion());
            institucionDTO.setRuc(entidad.getRegistroInstitucion().getRuc());
            dto.setRegistroInstitucion(institucionDTO);
        }
        return dto;
    }

}