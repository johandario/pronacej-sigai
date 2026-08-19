package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Estudios;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EstudiosDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EstudiosEstadisticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.RegistroInstitucionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EstudiosRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.springframework.stereotype.Service;
import java.util.*;

@Service

@AllArgsConstructor

public class EstudiosServiceImpl implements EstudiosService {

    private EstudiosRepository estudiosRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private RegistroInstitucionRepository registroInstitucionRepository;
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private PaginacionService paginacionService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EstudiosDTO>> obtenerListaEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EstudiosDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
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
            PaginacionRequest request = new Gson().fromJson(dfBody.getData(), PaginacionRequest.class);
            List<Estudios> estudiosList =
                    this.estudiosRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(
                            request.getTokenIdentificador(),
                            false
                    );
            List<EstudiosDTO> dtos = new ArrayList<>();
            for (Estudios estudios : estudiosList) {
                dtos.add(entidadADto(estudios));
            }
            dtos.sort(Comparator.comparing(
                    EstudiosDTO::getFechaCreacion,
                    Comparator.nullsLast(Date::compareTo)
            ).reversed());
            PaginacionResponse<EstudiosDTO> response = this.paginacionService.obtenerDatos(dtos, request);
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
    public RespuestaPorDefectoAuditoria<EstudiosDTO> crearEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<EstudiosDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            EstudiosDTO dto = new Gson().fromJson(dfBody.getData(), EstudiosDTO.class);
            boolean esEdicion = dto.getTokenIdentificador() != null && !dto.getTokenIdentificador().isBlank();
            Estudios entidad = dtoAEntidad(dto);
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
                entidad = this.estudiosRepository.save(entidad);
                df.llenarRespuestaExitosa(
                        "Se ha creado con éxito el registro de estudios.",
                        entidadADto(entidad)
                );
            }
            else {
                entidad.setFechaEdicion(new Date());
                entidad = this.estudiosRepository.save(entidad);
                df.llenarRespuestaExitosa(
                        "Se ha editado con éxito el registro de estudios.",
                        entidadADto(entidad)
                );
            }
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EstudiosDTO> obtenerEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<EstudiosDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            EstudiosDTO dto = new Gson().fromJson(dfBody.getData(), EstudiosDTO.class);
            Estudios estudios =
                    this.estudiosRepository.findByTokenIdentificadorAndRemovido(
                            dto.getTokenIdentificador(),
                            false
                    );
            if (estudios == null) {
                df.setMensaje("No se encontró el registro de estudios solicitado.");
                return df;
            }
            df.llenarRespuestaExitosa("Registro encontrado correctamente.", entidadADto(estudios));
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);

        }
        return df;

    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<Boolean> eliminarEstudios(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
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
            EstudiosDTO dto = new Gson().fromJson(dfBody.getData(), EstudiosDTO.class);
            Estudios estudios =
                    this.estudiosRepository.findByTokenIdentificadorAndRemovido(
                            dto.getTokenIdentificador(),
                            false
                    );
            if (estudios == null) {
                df.setMensaje("No existe el registro buscado.");
                return df;
            }
            estudios.setRemovido(true);
            estudios.setFechaEliminacion(new Date());
            estudios.setIpElimina(httpServletRequest.getRemoteAddr());
            estudios.setUsuarioSistemaElimina(dfJwt.getData().getUsuarioSistema());
            this.estudiosRepository.save(estudios);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito el registro de estudios.", true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);

        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EstudiosDTO> consultarInstitucionPorRuc(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<EstudiosDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            EstudiosDTO dto = new Gson().fromJson(dfBody.getData(), EstudiosDTO.class);
            String ruc = dto.getRegistroInstitucion() != null
                    ? dto.getRegistroInstitucion().getRuc()
                    : null;
            if (ruc == null || ruc.isBlank()) {
                df.setMensaje("Debe ingresar el RUC de la institución.");
                return df;
            }
            RegistroInstitucion institucion =
                    this.registroInstitucionRepository.findByRucAndRemovido(ruc, false);
            if (institucion == null) {
                df.setMensaje("No se encontró una institución registrada con el RUC ingresado.");
                return df;
            }
            EstudiosDTO response = new EstudiosDTO();
            response.setRegistroInstitucion(registroInstitucionADto(institucion));
            df.llenarRespuestaExitosa("Institución encontrada correctamente.", response);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);

        }
        return df;

    }


    @Override
    public RespuestaPorDefectoAuditoria<Long> obtenerCantidadUsuariosEstudiando(
            HttpServletRequest httpServletRequest
    ) {
        RespuestaPorDefectoAuditoria<Long> respuesta =
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
            Long cantidad =
                    this.estudiosRepository.countUsuariosEstudiando(
                            jerarquiaActual.getTokenIdentificador()
                    );
            respuesta.llenarRespuestaExitosa(
                    "Cantidad obtenida correctamente.",
                    cantidad
            );
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    @Override
    public RespuestaPorDefectoAuditoria<List<EstudiosEstadisticoDTO>> obtenerEstadisticasEstudios(
            HttpServletRequest httpServletRequest
    ) {
        RespuestaPorDefectoAuditoria<List<EstudiosEstadisticoDTO>> respuesta =
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
            List<Object[]> resultados =
                    this.estudiosRepository.countEstudiosPorInstitucion(
                            jerarquiaActual.getTokenIdentificador()
                    );
            List<EstudiosEstadisticoDTO> estadisticas =
                    resultados.stream()
                            .map(obj -> new EstudiosEstadisticoDTO(
                                    obj[0] != null ? obj[0].toString() : "Sin institución",
                                    obj[1] != null ? obj[1].toString() : "Sin RUC",
                                    obj[2] != null ? ((Number) obj[2]).intValue() : 0
                            ))
                            .toList();
            respuesta.llenarRespuestaExitosa(
                    "Estadísticas obtenidas correctamente.",
                    estadisticas
            );
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    @Override
    public RespuestaPorDefectoAuditoria<Double> obtenerPorcentajeConvenioPronacej(
            HttpServletRequest httpServletRequest
    ) {
        RespuestaPorDefectoAuditoria<Double> respuesta =
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
            List<Object[]> resultados =
                    this.estudiosRepository.porcentajeConvenioPronacej(
                            jerarquiaActual.getTokenIdentificador()
                    );

            Long convenio = 0L;
            Long total = 0L;

            if (resultados != null && !resultados.isEmpty()) {
                Object[] resultado = resultados.get(0);

                convenio = resultado[0] != null ? ((Number) resultado[0]).longValue() : 0L;
                total = resultado[1] != null ? ((Number) resultado[1]).longValue() : 0L;
            }

            double porcentaje = total > 0
                    ? Math.round(((convenio.doubleValue() * 100.0) / total.doubleValue()) * 100.0) / 100.0
                    : 0.0;
            respuesta.llenarRespuestaExitosa(
                    "Porcentaje obtenido correctamente.",
                    porcentaje
            );
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }



    private Estudios dtoAEntidad(EstudiosDTO dto) {
        if (dto == null) return null;
        Estudios estudiosEncontrado =
                this.estudiosRepository.findByTokenIdentificadorAndRemovido(
                        dto.getTokenIdentificador(),
                        false
                );
        Estudios entidad = Objects.requireNonNullElseGet(estudiosEncontrado, Estudios::new);
        entidad.setFechaInicioEstudios(dto.getFechaInicioEstudios());
        entidad.setCicloAcademicoActual(dto.getCicloAcademicoActual());
        entidad.setConvenioPronacej(Boolean.TRUE.equals(dto.getConvenioPronacej()));
        entidad.setIndependiente(Boolean.TRUE.equals(dto.getIndependiente()));
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

    private EstudiosDTO entidadADto(Estudios entidad) {
        if (entidad == null) return null;
        EstudiosDTO dto = new EstudiosDTO();
        dto.setIdEstudios(entidad.getIdEstudios());
        dto.setFechaInicioEstudios(entidad.getFechaInicioEstudios());
        dto.setCicloAcademicoActual(entidad.getCicloAcademicoActual());
        dto.setConvenioPronacej(entidad.getConvenioPronacej());
        dto.setIndependiente(entidad.getIndependiente());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        if (entidad.getFichaIdentificacion() != null) {
            dto.setIdFichaIdentificacion(entidad.getFichaIdentificacion().getIdFichaIdentificacion());
            dto.setTokenFichaIdentificacion(entidad.getFichaIdentificacion().getTokenIdentificador());
        }

        if (entidad.getRegistroInstitucion() != null) {
            dto.setRegistroInstitucion(registroInstitucionADto(entidad.getRegistroInstitucion()));
        }
        return dto;

    }

    private RegistroInstitucionDTO registroInstitucionADto(RegistroInstitucion entidad) {
        RegistroInstitucionDTO dto = new RegistroInstitucionDTO();
        dto.setIdRegistroInstitucion(entidad.getIdRegistroInstitucion());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setNombreOrganizacion(entidad.getNombreOrganizacion());
        dto.setRuc(entidad.getRuc());
        return dto;

    }

}
