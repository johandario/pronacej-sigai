package net.latinus.sistema.integral.gestion.seguridad.service.fuga;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.InstanciaProceso;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Paso;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Tarea;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.TareaUsuario;
import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.TareaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.TareaEventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.TareaTrasladoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoAdolescenteDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.HistoricoEntradaSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.flujo.InstanciaProcesoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.flujo.TareaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.flujo.TareaUsuarioRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.fuga.EventoFugaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import net.latinus.sistema.integral.gestion.seguridad.service.flujo.FlujoService;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.tras.TrasladoServiceImpl;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class EventoFugaServiceImpl implements EventoFugaService {
    private CatalogoRepository catalogoRepository;
    private InstanciaProcesoRepository instanciaProcesoRepository;
    private EventoFugaRepository eventoFugaRepository;
    private FlujoService flujoService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private HistoricoEntradaSalidaRepository historicoEntradaSalidaRepository;
    private JerarquiaRepository jerarquiaRepository;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private TareaRepository tareaRepository;
    private TareaUsuarioRepository tareaUsuarioRepository;
    private EmailService emailService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EventoFugaDTO>> obtenerFugas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EventoFugaDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            // Desencriptar body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDecifrado, PaginacionRequest.class);
            // Configuración de paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idFuga").descending()
            );
            // Obtener eventos paginados
            Page<EventoFuga> fugaPage = this.eventoFugaRepository.findByRemovido(false, pageable);
            // Preparar respuesta
            PaginacionResponse<EventoFugaDTO> paginacionResponse = new PaginacionResponse<>();
            // Convertir a DTO
            List<EventoFugaDTO> fugaDTOList = new ArrayList<>();
            for (EventoFuga fuga : fugaPage.toList()) {
                EventoFugaDTO fugaDTO = entidadADto(fuga);
                fugaDTOList.add(fugaDTO);
            }
            paginacionResponse.setData(fugaDTOList);
            paginacionResponse.setTotalItems(fugaPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + fugaDTOList.size() + " de: " + fugaPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EventoFugaDTO> obtenerFugaPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<EventoFugaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            EventoFuga fuga = this.eventoFugaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if (fuga == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            EventoFugaDTO fugaDTO = entidadADto(fuga);
            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + fugaDTO.getTokenIdentificador(), fugaDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<EventoFugaDTO> crearFuga(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EventoFugaDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TareaEventoFugaDTO tareaEventoFugaDTO = new Gson().fromJson(bodyDecifrado, TareaEventoFugaDTO.class);
            EventoFugaDTO fugaEntranteDTO = tareaEventoFugaDTO.getEventoFuga();
            TareaDTO tareaDTO = tareaEventoFugaDTO.getTarea();

            if (tareaDTO == null || tareaDTO.getTokenIdentificador() == null) {
                df.setMensaje("No se está recibiendo una tarea válida.");
                return df;
            }

            Tarea tareaActual = this.tareaRepository.findByTokenIdentificadorAndRemovido(tareaDTO.getTokenIdentificador(), false);
            if (tareaActual == null) {
                df.setMensaje("No se está obteniendo una tarea del flujo, no es posible continuar.");
                return df;
            }

            EventoFuga eventoFuga;

            eventoFuga = dtoAEntidad(fugaEntranteDTO, fichaIdentificacionRepository);

            if (fugaEntranteDTO.getTokenIdentificador() == null) {
                if (eventoFuga.getNumFuga() == null) eventoFuga.setNumFuga(generarCodigoIncremental());
                eventoFuga.setFechaCreacion(new Date());
                eventoFuga.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                eventoFuga.setIpCrea(httpServletRequest.getRemoteAddr());
                eventoFuga.setInstanciaProceso(tareaActual.getInstanciaProceso());
            } else {
                eventoFuga.setTokenIdentificador(fugaEntranteDTO.getTokenIdentificador());
                eventoFuga.setFechaEdicion(new Date());
                eventoFuga.setIpEdita(httpServletRequest.getRemoteAddr());
                eventoFuga.setInstanciaProceso(tareaActual.getInstanciaProceso());
                eventoFuga.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
            }

            eventoFuga = this.eventoFugaRepository.save(eventoFuga);

            if (!fugaEntranteDTO.getEsEdicion()) {
                // Completar tarea actual
                tareaActual.setEstado("Completada");
                tareaActual.setFechaEdicion(new Date());
                tareaActual.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                tareaActual.setIpEdita(httpServletRequest.getRemoteAddr());
                tareaActual.setUrl(tareaActual.getPaso().getUrl() + "/" + eventoFuga.getTokenIdentificador());
                this.tareaRepository.save(tareaActual);

                // Siguiente tarea
                Tarea siguienteTarea = this.tareaRepository.findByInstanciaProcesoTokenIdentificadorAndOrdenAndEstadoAndRemovido(
                        tareaActual.getInstanciaProceso().getTokenIdentificador(),
                        tareaActual.getOrden() + 1,
                        "Pendiente",
                        false
                );

                if (siguienteTarea != null) {
                    siguienteTarea.setEstado("En curso");
                    siguienteTarea.setFechaCreacion(new Date());
                    siguienteTarea.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                    siguienteTarea.setIpCrea(httpServletRequest.getRemoteAddr());
                    siguienteTarea.setUrl(siguienteTarea.getPaso().getUrl() + "/" + eventoFuga.getTokenIdentificador());
                    this.tareaRepository.save(siguienteTarea);
                }
            }

            df.llenarRespuestaExitosa("Se ha editado con éxito el registro.", fugaEntranteDTO);

            EventoFuga fuga = dtoAEntidad(fugaEntranteDTO, fichaIdentificacionRepository);
            if (Boolean.TRUE.equals(fuga.getUltimoPaso())) {
                // Auditoría
                FichaIdentificacion fichaIdentificacion = fuga.getTokenFichaIdentificacion();
                if (fichaIdentificacion == null) {
                    throw new IllegalArgumentException("Ficha de identificación no encontrada para la fuga.");
                }
                HistoricoEntradaSalida historico = new HistoricoEntradaSalida();
                historico.setNumeroIdentificacion(fichaIdentificacion.getNumeroIdentificacion());
                historico.setFichaIdentificacion(fichaIdentificacion);
                historico.setFechaEntrada(new Date());
                historico.setRegistroActivo(true);
                historico.setEventoFuga(fuga);
                historico.setFechaSalida(fuga.getFechaFuga());
                historico.setCentroSalida(fuga.getCentro());
                historico.setMotivoSalida(this.catalogoRepository.findByNemonicoAndRemovido("SALIDA_FUGA", false));
                this.historicoEntradaSalidaRepository.save(historico);
                fichaIdentificacion.setEstado(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_ADOLESCENTE_FUGADO", false));
                // Enviar correo si el paso lo requiere
                Paso pasoActual = tareaActual.getPaso();
                if (pasoActual.getRequiereNotificacionCorreo()) {
                    List<TareaUsuario> listaTareaUsuarios = this.tareaUsuarioRepository.findByTareaTokenIdentificadorAndRemovido(tareaActual.getTokenIdentificador(), false);
                    List<String> correos = new ArrayList<>();
                    for (TareaUsuario tu : listaTareaUsuarios) {
                        if (tu.getUsuarioSistema().getEmail() != null) {
                            correos.add(tu.getUsuarioSistema().getEmail());
                        }
                    }
                    System.out.println("Contenido HTML recibido: " + fugaEntranteDTO.getHtml());
                    this.emailService.enviarCorreo(
                            correos,
                            "PROCESO: " + pasoActual.getProceso().getNombre() + " - " + pasoActual.getNombre(),
                            "Tiene un proceso de fuga completado. Revise su bandeja de entrada.<br><br>" + fugaEntranteDTO.getHtml(),
                            df2.getData().getEmpresa().getTokenIdentificador(),
                            "text/html",
                            null
                    );
                }
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<EventoFugaDTO> guardarBorrador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EventoFugaDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TareaEventoFugaDTO tareaEventoFugaDTO = new Gson().fromJson(bodyDecifrado, TareaEventoFugaDTO.class);

            EventoFugaDTO fugaEntranteDTO = tareaEventoFugaDTO.getEventoFuga();
            TareaDTO tareaDTO = tareaEventoFugaDTO.getTarea();

            Tarea tareaActual = this.tareaRepository.findByTokenIdentificadorAndRemovido(tareaDTO.getTokenIdentificador(), false);

            if (tareaActual == null) {
                df.setMensaje("No se està obteniendo una tarea del flujo, no es posible continuar.");
                return df;
            }

            EventoFuga eventoFuga;

            eventoFuga = dtoAEntidad(fugaEntranteDTO, fichaIdentificacionRepository);

            if (fugaEntranteDTO.getTokenIdentificador() == null) {
                if (eventoFuga.getNumFuga() == null) eventoFuga.setNumFuga(generarCodigoIncremental());
                eventoFuga.setFechaCreacion(new Date());
                eventoFuga.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                eventoFuga.setIpCrea(httpServletRequest.getRemoteAddr());
                eventoFuga.setInstanciaProceso(tareaActual.getInstanciaProceso());
            } else {
                eventoFuga.setTokenIdentificador(fugaEntranteDTO.getTokenIdentificador());
                eventoFuga.setFechaEdicion(new Date());
                eventoFuga.setIpEdita(httpServletRequest.getRemoteAddr());
                eventoFuga.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
            }

            eventoFuga = this.eventoFugaRepository.save(eventoFuga);

            if (fugaEntranteDTO.getTokenIdentificador() == null) {
                tareaActual.setFechaEdicion(new Date());
                tareaActual.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                tareaActual.setIpEdita(httpServletRequest.getRemoteAddr());
                tareaActual.setUrl(tareaActual.getPaso().getUrl() + "/" + eventoFuga.getTokenIdentificador());

                this.tareaRepository.save(tareaActual);
            }

            df.llenarRespuestaExitosa("Se ha guardado con éxito el borrador de fuga.", entidadADto(eventoFuga));

        } catch (
                Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarFuga(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            EventoFugaDTO fugaDTO = new Gson().fromJson(bodyString, EventoFugaDTO.class);
            EventoFuga fuga = this.eventoFugaRepository.findByTokenIdentificadorAndRemovido(
                    fugaDTO.getTokenIdentificador(), false
            );
            if (fuga == null) {
                df.setMensaje("La fuga no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }
            Date fecha = new Date();
            fuga.setRemovido(true);
            fuga.setIpElimina(ip);
            fuga.setUsuarioSistemaElimina(usuarioSistemaLogin);
            fuga.setFechaEliminacion(fecha);
            this.eventoFugaRepository.save(fuga);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema a la fuga: "
                    + fuga.getTokenIdentificador(), true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<List<EventoFugaDTO>> obtenerFugasPorFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<EventoFugaDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
            List<EventoFuga> eventos = this.eventoFugaRepository.findByTokenFichaIdentificacionIdFichaIdentificacionAndRemovido(idFichaIdentificacion, false);
            if (eventos.isEmpty()) {
                df.setMensaje("No se encontraron eventos de fuga para eladolescente seleccionado.");
                return df;
            }
            List<EventoFugaDTO> eventosDTO = new ArrayList<>();
            for (EventoFuga evento : eventos) {
                EventoFugaDTO dto = entidadADto(evento);
                eventosDTO.add(dto);
            }
            df.llenarRespuestaExitosa("Eventos encontrados: " + eventosDTO.size(), eventosDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    private String generarCodigoIncremental() {
        String prefijo = "FUGA";
        EventoFuga ultimoTrasladoEncontrado = eventoFugaRepository.findFirstByOrderByIdFugaDesc();
        long nuevoId = 1L;
        if (ultimoTrasladoEncontrado != null) {
            Long ultimoId = ultimoTrasladoEncontrado.getIdFuga();
            nuevoId = ultimoId + 1;
        }
        return String.format("%s-%08d", prefijo, nuevoId);
    }


    @NotNull
    private static EventoFugaDTO entidadADto(EventoFuga fuga) {
        EventoFugaDTO fugaDTO = new EventoFugaDTO();
        fugaDTO.setIdFuga(fuga.getIdFuga());
        if (fuga.getTokenFichaIdentificacion() != null) {
            fugaDTO.setTokenFichaIdentificacion(fuga.getTokenFichaIdentificacion().getIdFichaIdentificacion());
        }
        fugaDTO.setParentesco(entidadADtoCatalogo(fuga.getParentesco()));
        fugaDTO.setFechaRegistro(fuga.getFechaRegistro());
        fugaDTO.setFechaFuga(fuga.getFechaFuga());
        fugaDTO.setFechaInformeDirector(fuga.getFechaInformeDirector());
        fugaDTO.setFechaInformeApoderado(fuga.getFechaInformeApoderado());
        fugaDTO.setDescripcionHechos(fuga.getDescripcionHechos());
        fugaDTO.setAccionesRealizadas(fuga.getAccionesRealizadas());
        fugaDTO.setPresenciaDe(fuga.getPresenciaDe());
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setAsunto(fuga.getAsunto());
        fugaDTO.setDirigidoA(fuga.getDirigidoA());
        fugaDTO.setDe(fuga.getDe());
        fugaDTO.setApoderado(fuga.getApoderado());
        fugaDTO.setDni(fuga.getDni());
        fugaDTO.setNumFuga(fuga.getNumFuga());
        fugaDTO.setIsComplete(fuga.getIsComplete());
        fugaDTO.setEstadoEvento(entidadADtoCatalogo(fuga.getEstadoEvento()));
        fugaDTO.setCentro(entidadADtoJerarquia(fuga.getCentro()));
        fugaDTO.setUltimoPaso(fuga.getUltimoPaso());
        if (fuga.getTokenFichaIdentificacion() != null) {
            FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
            fugaDTO.setTokenFichaIdentificacion(ficha.getIdFichaIdentificacion());
            fugaDTO.setNumeroIdentificacion(ficha.getNumeroIdentificacion());
            fugaDTO.setFechaNacimiento(ficha.getFechaNacimiento());
            fugaDTO.setNombreAdolescente(
                    (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                            (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                            (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "")
            );
        }
        return fugaDTO;
    }

    private EventoFuga dtoAEntidad(EventoFugaDTO dto, FichaIdentificacionRepository fichaIdentificacionRepository) {
        EventoFuga fuga = new EventoFuga();
        if (dto.getTokenFichaIdentificacion() != null) {
            FichaIdentificacion ficha = fichaIdentificacionRepository.findByIdFichaIdentificacion(Long.valueOf(dto.getTokenFichaIdentificacion()));
            if (ficha == null) {
                throw new IllegalArgumentException("FichaIdentificacion no encontrada para ID: " + dto.getTokenFichaIdentificacion());
            }
            fuga.setTokenFichaIdentificacion(ficha);
        } else {
            throw new IllegalArgumentException("El tokenFichaIdentificacion no puede ser nulo");
        }
        fuga.setIdFuga(dto.getIdFuga());
        fuga.setFechaRegistro(dto.getFechaRegistro());
        fuga.setFechaFuga(dto.getFechaFuga());
        fuga.setDescripcionHechos(dto.getDescripcionHechos());
        fuga.setAccionesRealizadas(dto.getAccionesRealizadas());
        fuga.setPresenciaDe(dto.getPresenciaDe());
        fuga.setDirigidoA(dto.getDirigidoA());
        fuga.setAsunto(dto.getAsunto());
        fuga.setDe(dto.getDe());
        fuga.setApoderado(dto.getApoderado());
        fuga.setFechaInformeDirector(dto.getFechaInformeDirector());
        fuga.setFechaInformeApoderado(dto.getFechaInformeApoderado());
        fuga.setDni(dto.getDni());
        fuga.setParentesco(dtoAEntidadCatalogo(dto.getParentesco()));
        fuga.setEstadoEvento(dtoAEntidadCatalogo(dto.getEstadoEvento()));
        fuga.setNumFuga(dto.getNumFuga());
        fuga.setIsComplete(dto.getIsComplete());
        fuga.setCentro(dtoAEntidadJerarquia(dto.getCentro()));
        fuga.setUltimoPaso(dto.getUltimoPaso());
        return fuga;
    }

    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
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

    private Jerarquia dtoAEntidadJerarquia(JerarquiaDTO dto) {
        if (dto == null) return null;
        return this.jerarquiaRepository.findJerarquiaByTokenIdentificador(dto.getTokenIdentificador());
    }

    private static JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
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
