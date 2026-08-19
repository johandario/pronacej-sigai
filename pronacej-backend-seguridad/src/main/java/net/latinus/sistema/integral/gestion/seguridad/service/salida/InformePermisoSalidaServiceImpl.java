package net.latinus.sistema.integral.gestion.seguridad.service.salida;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.ActividadSalida;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.InformePermisoSalidaAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.ActividadSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.InformePermisoSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.HistoricoEntradaSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.InformePermisoSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FuncionarioRepository;
import org.springframework.data.domain.PageImpl;



import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class InformePermisoSalidaServiceImpl implements InformePermisoSalidaService {

    private CatalogoRepository catalogoRepository;
    private InformePermisoSalidaRepository informeSalidaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private PaginacionService paginacionService;
    private FuncionarioRepository funcionarioRepository;
    private HistoricoEntradaSalidaRepository historicoEntradaSalidaRepository;
    private JerarquiaRepository jerarquiaRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformePermisoSalidaDTO>> obtenerPermisosSalidas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformePermisoSalidaDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
                    Sort.by("idPermisoSalida").descending()
            );
            Page<InformePermisoSalidaAdolescente> fugaPage = this.informeSalidaRepository.findByRemovido(false,pageable);
            PaginacionResponse<InformePermisoSalidaDTO> paginacionResponse = new PaginacionResponse<>();
            List<InformePermisoSalidaDTO> fugaDTOList = new ArrayList<>();
            for (InformePermisoSalidaAdolescente fuga : fugaPage.toList()) {
                InformePermisoSalidaDTO fugaDTO  = entidadADto(fuga);
                fugaDTOList.add(fugaDTO);
            }

            paginacionResponse.setData(fugaDTOList);
            paginacionResponse.setTotalItems(fugaPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + fugaDTOList.size() + " de: " + fugaPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);
        }
        catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformePermisoSalidaDTO> obtenerPermisosRegistroSalidaPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<InformePermisoSalidaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            InformePermisoSalidaAdolescente fuga  = this.informeSalidaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if(fuga == null ){
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            InformePermisoSalidaDTO fugaDTO = entidadADto(fuga);
            List<ActividadSalidaDTO> actividadDTO = new ArrayList<>();
            for (ActividadSalida actividad : fuga.getActividades()) {
                ActividadSalidaDTO actividadDTOItem = new ActividadSalidaDTO();
                actividadDTOItem.setIdActividadSalida(actividad.getIdActividadSalida());
                actividadDTOItem.setDescripcion(actividad.getDescripcion());
                actividadDTO.add(actividadDTOItem);
            }
            fugaDTO.setActividades(actividadDTO);
            df.llenarRespuestaExitosa("Se ha encontrado el registro: " , fugaDTO);
        }
        catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<InformePermisoSalidaDTO> crearPermisoSalida(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<InformePermisoSalidaDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            InformePermisoSalidaDTO dto = new Gson().fromJson(bodyDecifrado, InformePermisoSalidaDTO.class);

            InformePermisoSalidaAdolescente fugaEncontrado = this.informeSalidaRepository
                    .findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

            if (dto.getEsEdicion() && fugaEncontrado == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }

            if (!dto.getEsEdicion()) {
                // CREACIÓN
                InformePermisoSalidaAdolescente fuga = dtoAEntidad(dto, fichaIdentificacionRepository);
                fuga.setFechaCreacion(new Date());
                fuga.setTokenIdentificador(UUID.randomUUID().toString());

                Set<String> descripcionesUnicas = new HashSet<>();
                List<ActividadSalida> actividadSalida = new ArrayList<>();

                for (ActividadSalidaDTO actividadDTO : dto.getActividades()) {
                    String descripcionNormalizada = actividadDTO.getDescripcion().trim().toLowerCase();
                    if (descripcionesUnicas.add(descripcionNormalizada)) {
                        ActividadSalida actividad = new ActividadSalida();
                        actividad.setDescripcion(actividadDTO.getDescripcion());
                        actividad.setInformePermisoSalidaAdolescente(fuga);
                        actividadSalida.add(actividad);
                    }
                }

                fuga.setActividades(actividadSalida);

                // Crear histórico
                FichaIdentificacion fichaIdentificacion = fuga.getTokenFichaIdentificacion();
                if (fichaIdentificacion == null) {
                    throw new IllegalArgumentException("Ficha de identificación no encontrada.");
                }

                HistoricoEntradaSalida historico = new HistoricoEntradaSalida();
                historico.setNumeroIdentificacion(fichaIdentificacion.getNumeroIdentificacion());
                historico.setFichaIdentificacion(fichaIdentificacion);
                historico.setFechaEntrada(new Date());
                historico.setRegistroActivo(true);
                historico.setPermisoSalida(fuga);
                historico.setFechaSalida(fuga.getFechaHoraSalida());
                historico.setCentroSalida(fichaIdentificacion.getCentroIngreso());
                historico.setMotivoSalida(this.catalogoRepository.findByNemonicoAndRemovido("SALIDA_TEMPORAL", false));

                this.historicoEntradaSalidaRepository.save(historico);
                this.informeSalidaRepository.save(fuga);
                FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
                if (ficha != null) {
                    String nombreCompleto =
                            (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                                    (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                                    (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "");
                    dto.setNombreAdolescente(nombreCompleto.trim());
                }

                String mensajeUsuario = "Se creó con éxito el permiso de salida de: " + dto.getNombreAdolescente();
                String mensajeAuditoria = "Se creó con éxito el permiso de salida de: " + dto.getNombreAdolescente();
                df.llenarRespuestaExitosa(mensajeUsuario, dto, mensajeAuditoria);

            } else {

                // EDICIÓN
                InformePermisoSalidaAdolescente fuga = fugaEncontrado;
                fuga.setFechaEdicion(new Date());
                fuga.getActividades().clear();

                // Descripciones actuales ya guardadas (normalizadas)
                Set<String> descripcionesActuales = fuga.getActividades().stream()
                        .map(a -> a.getDescripcion().trim().toLowerCase())
                        .collect(Collectors.toSet());

                // Agregar solo las nuevas (no duplicadas)
                for (ActividadSalidaDTO actividadDTO : dto.getActividades()) {
                    String nuevaDesc = actividadDTO.getDescripcion().trim().toLowerCase();
                    if (!descripcionesActuales.contains(nuevaDesc)) {
                        ActividadSalida nueva = new ActividadSalida();
                        nueva.setDescripcion(actividadDTO.getDescripcion());
                        nueva.setInformePermisoSalidaAdolescente(fuga);
                        fuga.getActividades().add(nueva);
                        descripcionesActuales.add(nuevaDesc); // evita duplicados dentro del mismo envío
                    }
                }

                this.informeSalidaRepository.save(fuga);
                FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
                if (ficha != null) {
                    String nombreCompleto =
                            (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                                    (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                                    (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "");
                    dto.setNombreAdolescente(nombreCompleto.trim());
                }
                String mensajeUsuario = "Se editó con éxito el permiso de salida de: " + dto.getNombreAdolescente();
                String mensajeAuditoria = "Se editó con éxito el permiso de salida de:  " + dto.getNombreAdolescente();
                df.llenarRespuestaExitosa(mensajeUsuario, dto, mensajeAuditoria);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarPermisoSalida(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            InformePermisoSalidaDTO fugaDTO = new Gson().fromJson(bodyString, InformePermisoSalidaDTO.class);
            InformePermisoSalidaAdolescente fuga = this.informeSalidaRepository.findByTokenIdentificadorAndRemovido(
                    fugaDTO.getTokenIdentificador(), false
            );
            if (fuga == null) {
                df.setMensaje("La salida no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }
            Date fecha = new Date();
            fuga.setRemovido(true);
            fuga.setIpElimina(ip);
            fuga.setUsuarioSistemaElimina(usuarioSistemaLogin);
            fuga.setFechaEliminacion(fecha);
            this.informeSalidaRepository.save(fuga);

            // ACTUALIZAR PERMISO TEMPORAL DE LA FICHA
            FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
            String nombreCompleto = "";
            if (ficha != null) {
                ficha.setPermisoTemporal(false);
                this.fichaIdentificacionRepository.save(ficha);
                nombreCompleto =
                        (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                                (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                                (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "");

            }

            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema el permiso de salida " + fuga.getNroDocumento() +"del adolescente: "+ nombreCompleto
                    , true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformePermisoSalidaDTO>> obtenerlistadoPorToken(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<InformePermisoSalidaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            // Desencriptar el cuerpo de la solicitud
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            // Obtener parámetros
            String tokenIdentificador = paginacionRequest.getTokenIdentificador();
            String filtro = paginacionRequest.getFilter();
            int page = paginacionRequest.getPage() != null ? paginacionRequest.getPage() : 0;
            int size = paginacionRequest.getSize() != null ? paginacionRequest.getSize() : 10;

            if (tokenIdentificador == null || tokenIdentificador.isEmpty()) {
                throw new IllegalArgumentException("El campo tokenIdentificador es requerido.");
            }

            // Configurar la paginación
            Pageable pageable = PageRequest.of(page, size);

            // Obtener datos filtrados por token y filtro
            Page<InformePermisoSalidaAdolescente> listaSalidas;
            if (filtro != null && !filtro.trim().isEmpty()) {
                listaSalidas = informeSalidaRepository.buscarPorTokenYFiltro(tokenIdentificador, filtro, pageable);
            } else {
                listaSalidas = new PageImpl<>(
                        informeSalidaRepository.findAllByFichaIdentificacionTokenIdentificadorAndRemovido(tokenIdentificador, false),
                        pageable,
                        size
                );
            }

            // Convertir entidades a DTO y ordenar por fecha de salida descendente
            List<InformePermisoSalidaDTO> salidaDTOList = listaSalidas.stream()
                    .map(InformePermisoSalidaServiceImpl::entidadADto)
                    .sorted((a, b) -> b.getFechaHoraSalida().compareTo(a.getFechaHoraSalida()))
                    .collect(Collectors.toList());

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            salidaDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            // Generar la paginación de respuesta
            PaginacionResponse<InformePermisoSalidaDTO> paginacionResponse = paginacionService.obtenerDatos(salidaDTOList, paginacionRequest);
            respuesta.llenarRespuestaExitosa("Salidas encontradas", paginacionResponse);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    @Override
    public RespuestaPorDefectoAuditoria<List<InformePermisoSalidaDTO>> obtenerPermisosSalidaPorFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<InformePermisoSalidaDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
            Map<String, Object> datos = new Gson().fromJson(bodyDecifrado, Map.class);
            Long idFichaIdentificacion = Long.valueOf(datos.get("idFichaIdentificacion").toString().split("\\.")[0]);
            List<InformePermisoSalidaAdolescente> eventos = this.informeSalidaRepository.findByTokenFichaIdentificacionIdFichaIdentificacionAndRemovido(idFichaIdentificacion, false);
            if (eventos.isEmpty()) {
                df.setMensaje("No se encontraron permisos de salida para el adolescente seleccionado.");
                return df;
            }
            List<InformePermisoSalidaDTO> eventosDTO = new ArrayList<>();
            for (InformePermisoSalidaAdolescente evento : eventos) {
                InformePermisoSalidaDTO dto = entidadADto(evento);
                eventosDTO.add(dto);
            }
            df.llenarRespuestaExitosa("Permisos salidas encontrados: " + eventosDTO.size(), eventosDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @NotNull
    private static InformePermisoSalidaDTO entidadADto(InformePermisoSalidaAdolescente fuga) {
        InformePermisoSalidaDTO fugaDTO = new InformePermisoSalidaDTO();
        fugaDTO.setIdPermisoSalida(fuga.getIdPermisoSalida());
        fugaDTO.setFechaHoraSalida(fuga.getFechaHoraSalida());
        fugaDTO.setUsuarioSalida(fuga.getUsuarioSalida());
        fugaDTO.setMotivoSalida(entidadADtoCatalogo(fuga.getMotivoSalida()));
        fugaDTO.setNroDocumento(fuga.getNroDocumento());
        fugaDTO.setFechaHoraRegreso(fuga.getFechaHoraRegreso());
        fugaDTO.setObservaciones(fuga.getObservaciones());
        fugaDTO.setTipoSalida(entidadADtoCatalogo(fuga.getTipoSalida()));
        fugaDTO.setFrecuenciaSalida(entidadADtoCatalogo(fuga.getFrecuenciaSalida()));
        fugaDTO.setTipoSalidaLugar(fuga.getTipoSalidaLugar());
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setIsComplete(fuga.getIsComplete());
        fugaDTO.setMotivoSalida(entidadADtoCatalogo(fuga.getMotivoSalida()));
        if (fuga.getFrecuenciaSalida() != null) {
            fugaDTO.setNombreFrecuenciaSalida(fuga.getFrecuenciaSalida().getNombre());
        }
        fugaDTO.setTipoSalida(entidadADtoCatalogo(fuga.getTipoSalida()));
        if (fuga.getTipoSalida() != null) {
            fugaDTO.setNombreTipoSalida(fuga.getTipoSalida().getNombre());
        }
        if (fuga.getTokenFichaIdentificacion() != null) {
            FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
            fugaDTO.setTokenFichaIdentificacion(ficha.getIdFichaIdentificacion());
            fugaDTO.setTokenIdentificadorAdolescente(ficha.getTokenIdentificador());
            fugaDTO.setDniAdolescente(ficha.getDni());
            fugaDTO.setNombreAdolescente(
                    (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                            (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                            (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "")
            );
        }
        if (fuga.getFechaCreacion() != null) fugaDTO.setFechaCreacion(fuga.getFechaCreacion());
        fugaDTO.setEstadoEvento(entidadADtoCatalogo(fuga.getEstadoEvento()));
        fugaDTO.setCentro(entidadADtoJerarquia(fuga.getCentro()));
        fugaDTO.setOtrosSalida(fuga.getOtrosSalida());
        return fugaDTO;
    }

    private InformePermisoSalidaAdolescente dtoAEntidad(InformePermisoSalidaDTO dto, FichaIdentificacionRepository fichaIdentificacionRepository) {
        InformePermisoSalidaAdolescente fuga = new InformePermisoSalidaAdolescente();
        fuga.setIdPermisoSalida(dto.getIdPermisoSalida());
        if (dto.getTokenFichaIdentificacion() != null) {
            FichaIdentificacion ficha = fichaIdentificacionRepository.findByIdFichaIdentificacion(Long.valueOf(dto.getTokenFichaIdentificacion()));
            if (ficha == null) {
                throw new IllegalArgumentException("FichaIdentificacion no encontrada para ID: " + dto.getTokenFichaIdentificacion());
            }
            fuga.setTokenFichaIdentificacion(ficha);
        }
        else {
            throw new IllegalArgumentException("El tokenFichaIdentificacion no puede ser nulo");
        }

        fuga.setFechaHoraSalida(dto.getFechaHoraSalida());
        fuga.setUsuarioSalida(dto.getUsuarioSalida());
        fuga.setMotivoSalida(dtoAEntidadCatalogo(dto.getMotivoSalida()));
        fuga.setFrecuenciaSalida(dtoAEntidadCatalogo(dto.getFrecuenciaSalida()));
        fuga.setTipoSalida(dtoAEntidadCatalogo(dto.getTipoSalida()));
        fuga.setNroDocumento(dto.getNroDocumento());
        fuga.setFechaHoraRegreso(dto.getFechaHoraRegreso());
        fuga.setObservaciones(dto.getObservaciones());
        fuga.setTipoSalidaLugar(dto.getTipoSalidaLugar());
        fuga.setIsComplete(dto.getIsComplete());
        fuga.setEstadoEvento(dtoAEntidadCatalogo(dto.getEstadoEvento()));
        fuga.setCentro(dtoAEntidadJerarquia(dto.getCentro()));
        fuga.setOtrosSalida(dto.getOtrosSalida());
        return fuga;
    }


    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }

    private static CatalogoDTO entidadADtoCatalogo(Catalogo entidad) {
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


    public RespuestaPorDefectoAuditoria<Map<String, String>> obtenerDirectorPorJerarquia(String nombreJerarquia) {
        RespuestaPorDefectoAuditoria<Map<String, String>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            List<Funcionario> directores = funcionarioRepository.findDirectorsByJerarquia(nombreJerarquia);

            if (directores != null && !directores.isEmpty()) {
                Funcionario funcionario = directores.get(0);

                Map<String, String> datosDirector = new HashMap<>();
                datosDirector.put("nombres", funcionario.getNombres());
                datosDirector.put("apellidos", funcionario.getApellidos());

                respuesta.llenarRespuestaExitosa(" Director encontrado.", datosDirector);
            } else {
                respuesta.setMensaje(" No se encontró un director para la jerarquía dada.");
            }
        } catch (Exception e) {
            respuesta.llenarConDatosDeException(e);
        }

        return respuesta;
    }


    @Override
    public RespuestaPorDefectoAuditoria<Map<String, String>> obtenerDirectorPorDepartamento(HttpServletRequest httpServletRequest ,Long idDepartamento) {
        RespuestaPorDefectoAuditoria<Map<String, String>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            System.out.println("ID Departamento recibido: " + idDepartamento);

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Jerarquia jerarquiaActual = bodyJwtValido.getJerarquia();

            List<Funcionario> directores = funcionarioRepository.findDirectoresByDepartamento(idDepartamento);
            if (directores != null && !directores.isEmpty()) {
                Funcionario funcionario = directores.get(0);
                Map<String, String> datosDirector = new HashMap<>();
                datosDirector.put("nombres", funcionario.getNombres());
                datosDirector.put("apellidos", funcionario.getApellidos());
                datosDirector.put("email", funcionario.getEmail());
                datosDirector.put("numeroDeDocumento", funcionario.getNumeroDeDocumento());
                datosDirector.put("cargo", funcionario.getCargo().getNombre());
                datosDirector.put("departamento", jerarquiaActual.getNombre());
                respuesta.llenarRespuestaExitosa("Director encontrado.", datosDirector);
            } else {
                respuesta.setMensaje("No se encontró un director para el departamento dado.");
            }
        } catch (Exception e) {
            respuesta.llenarConDatosDeException(e);
        }

        return respuesta;
    }



    private Jerarquia dtoAEntidadJerarquia(JerarquiaDTO dto) {
        if (dto == null) return null;
        return this.jerarquiaRepository.findJerarquiaByTokenIdentificador(dto.getTokenIdentificador());
    }

    private static  JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
        if (entidad == null) return null;
        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setId(entidad.getIdJerarquia());
        dto.setNombre(entidad.getNombre());
        dto.setNemonico(entidad.getNemonico());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }




}
