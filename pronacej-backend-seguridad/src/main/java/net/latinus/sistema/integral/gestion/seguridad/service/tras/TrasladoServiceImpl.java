package net.latinus.sistema.integral.gestion.seguridad.service.tras;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.*;
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
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.tras.TrasladoAdolescenteRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.tras.TrasladoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import net.latinus.sistema.integral.gestion.seguridad.service.flujo.FlujoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Year;
import java.util.*;

@Service
@AllArgsConstructor
public class TrasladoServiceImpl implements TrasladoService {
    private CatalogoRepository catalogoRepository;
    private InstanciaProcesoRepository instanciaProcesoRepository;
    private TrasladoRepository trasladoRepository;
    private FlujoService flujoService;
    private JerarquiaRepository jerarquiaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private HistoricoEntradaSalidaRepository historicoEntradaSalidaRepository;
    private TareaRepository tareaRepository;
    private TareaUsuarioRepository tareaUsuarioRepository;
    private EmailService emailService;
    private TrasladoAdolescenteRepository trasladoAdolescenteRepository;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> obtenerTraslados(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> df = new RespuestaPorDefectoAuditoria<>();


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
                    Sort.by("idTraslado").descending()
            );

            Page<Traslado> trasladoPage = this.trasladoRepository.findByRemovido(false, pageable);

            PaginacionResponse<TrasladoDTO> paginacionResponse = new PaginacionResponse<>();
            List<TrasladoDTO> trasladoDTOList = new ArrayList<>();

            for (Traslado traslado : trasladoPage.toList()) {
                TrasladoDTO trasladoDTO = entidadADto(traslado);
                trasladoDTOList.add(trasladoDTO);
            }

            paginacionResponse.setData(trasladoDTOList);
            paginacionResponse.setTotalItems(trasladoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + trasladoDTOList.size() + " de: " + trasladoPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> obtenerTrasladosPorIdFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, Long idFichaIdentificacion) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> df = new RespuestaPorDefectoAuditoria<>();


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
                    Sort.by("idTraslado").descending()
            );

            Page<Traslado> trasladoPage = this.trasladoRepository.findByTrasladoAdolescentesFichaIdentificacionIdFichaIdentificacionAndRemovido(idFichaIdentificacion, false, pageable);

            PaginacionResponse<TrasladoDTO> paginacionResponse = new PaginacionResponse<>();
            List<TrasladoDTO> trasladoDTOList = new ArrayList<>();

            for (Traslado traslado : trasladoPage.toList()) {
                TrasladoDTO trasladoDTO = entidadADto(traslado);
                trasladoDTOList.add(trasladoDTO);
            }

            paginacionResponse.setData(trasladoDTOList);
            paginacionResponse.setTotalItems(trasladoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + trasladoDTOList.size() + " de: " + trasladoPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<TrasladoDTO> obtenerTrasladoPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = new RespuestaPorDefectoAuditoria<>();


        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Traslado traslado = this.trasladoRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);

            if (traslado == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }

            TrasladoDTO trasladoDTO = entidadADto(traslado);

            List<TrasladoAdolescenteDTO> trasladoAdolescentesDTO = new ArrayList<>();
            for (TrasladoAdolescente adolescente : traslado.getTrasladoAdolescentes()) {
                TrasladoAdolescenteDTO dto = new TrasladoAdolescenteDTO();
                FichaIdentificacionDTO fichaDTO = new FichaIdentificacionDTO();
                //fichaDTO.setDni(adolescente.getFichaIdentificacion().getDni());
                fichaDTO.setIdFichaIdentificacion(adolescente.getFichaIdentificacion().getIdFichaIdentificacion());
                fichaDTO.setApellidoMaterno(adolescente.getFichaIdentificacion().getApellidoMaterno());
                fichaDTO.setApellidoPaterno(adolescente.getFichaIdentificacion().getApellidoPaterno());
                fichaDTO.setNombres(adolescente.getFichaIdentificacion().getNombres());
                fichaDTO.setTokenIdentificador(adolescente.getFichaIdentificacion().getTokenIdentificador());
                fichaDTO.setNumeroIdentificacion(adolescente.getFichaIdentificacion().getNumeroIdentificacion());

                dto.setFichaIdentificacion(fichaDTO);
                trasladoAdolescentesDTO.add(dto);
            }
            trasladoDTO.setTrasladoAdolescentes(trasladoAdolescentesDTO);

            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + trasladoDTO.getTokenIdentificador(), trasladoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /*@Override
    @Transactional
    public RespuestaPorDefectoAuditoria<TrasladoDTO> crearTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TrasladoDTO trasladoEntranteDTO = new Gson().fromJson(bodyDecifrado, TrasladoDTO.class);

            Traslado trasladoEncontrado = this.trasladoRepository.findByTokenIdentificadorAndRemovido(trasladoEntranteDTO.getTokenIdentificador(), false);

            // Creación de nuevo traslado siempre con la misma instancia (En caso de existir)

            Traslado traslado = dtoAEntidad(trasladoEntranteDTO);
            traslado.setIdTraslado(null);
            traslado.setNumTraslado(generarCodigoIncremental());
            traslado.setTokenIdentificador(UUID.randomUUID().toString());
            traslado.setFechaCreacion(new Date());
            //traslado.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());

            List<TrasladoAdolescente> trasladoAdolescentes = new ArrayList<>();
            for (TrasladoAdolescenteDTO adolescente : trasladoEntranteDTO.getTrasladoAdolescentes()) {
                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(adolescente.getFichaIdentificacion().getTokenIdentificador(), false);
                TrasladoAdolescente trasladoAdolescente = new TrasladoAdolescente();
                trasladoAdolescente.setTraslado(traslado);
                trasladoAdolescente.setFichaIdentificacion(ficha);
                trasladoAdolescente.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_ACTIVO", false));
                trasladoAdolescentes.add(trasladoAdolescente);
                trasladoAdolescente.setIsComplete(false);
            }
            traslado.setTrasladoAdolescentes(trasladoAdolescentes);

            if (trasladoEncontrado != null && trasladoEncontrado.getInstanciaProceso() != null) {
                // Si existe, el traslado creado va a la misma instancia
                traslado.setInstanciaProceso(trasladoEncontrado.getInstanciaProceso());
            } else {
                // Caso contrario, se crea una nueva instancia
                InstanciaProceso instanciaProceso = this.flujoService.crearInstancia(trasladoEntranteDTO.getTokenProceso());
                traslado.setInstanciaProceso(instanciaProceso);
            }

            this.trasladoRepository.save(traslado);

            // TODO: (En mi caso) Siempre se crea un nuevo traslado pero se trabaja sobre la misma instancia
            // TODO: Cada vez que se crea un traslado debe completarse la tarea actual y dar paso a la nueva
            Tarea tareaActual = this.flujoService.completarTareaActualEIniciarSiguiente(df2.getData(), traslado.getInstanciaProceso(), traslado.getTokenIdentificador(), trasladoEntranteDTO.getHtml());

            if (tareaActual == null) {
                df.setMensajeErrorReal("No se puede continuar.");
                return df;
            }

            for (TrasladoAdolescente adolescente: traslado.getTrasladoAdolescentes()){
                //Creacion de objeto auditoria entrada/salida
                HistoricoEntradaSalida historico = new HistoricoEntradaSalida();
                historico.setNumeroIdentificacion(adolescente.getFichaIdentificacion().getNumeroIdentificacion());
                historico.setFichaIdentificacion(adolescente.getFichaIdentificacion());
//            historico.setTipoDocumentoIdentificacion();
                historico.setFechaEntrada(new Date());
                historico.setRegistroActivo(true);
                historico.setTraslado(traslado);
                historico.setCentroSalida(traslado.getCentroOrigen());
                historico.setFechaSalida(traslado.getFechaCreacion());
                historico.setTrasladoAdolescente(adolescente);
                historico.setMotivoSalida(this.catalogoRepository.findByNemonicoAndRemovido("SALIDA_TRASLADO",false));
                this.historicoEntradaSalidaRepository.save(historico);
            }



//            historico.setMotivoSalida();

            df.llenarRespuestaExitosa("Se ha creado con éxito el registro.", trasladoEntranteDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }*/

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<TrasladoDTO> crearTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TareaTrasladoDTO tareaTrasladoDTO = new Gson().fromJson(bodyDecifrado, TareaTrasladoDTO.class);

            TrasladoDTO trasladoEntranteDTO = tareaTrasladoDTO.getTraslado();
            TareaDTO tareaDTO = tareaTrasladoDTO.getTarea();

            Tarea tareaActual = this.tareaRepository.findByTokenIdentificadorAndRemovido(tareaDTO.getTokenIdentificador(), false);

            if (tareaActual == null) {
                df.setMensaje("No se està obteniendo una tarea del flujo, no es posible continuar.");
                return df;
            }

            Traslado traslado;

            if (trasladoEntranteDTO.getTokenIdentificador() != null) {
                traslado = this.trasladoRepository.findByTokenIdentificadorAndRemovido(trasladoEntranteDTO.getTokenIdentificador(), false);

                List<TrasladoAdolescente> adolescentes = trasladoAdolescenteRepository.findByTrasladoTokenIdentificadorAndRemovido(traslado.getTokenIdentificador(), false);
                trasladoAdolescenteRepository.deleteAll(adolescentes);
            }

            traslado = dtoAEntidad(trasladoEntranteDTO);
            traslado.setTokenIdentificador(trasladoEntranteDTO.getTokenIdentificador());

            if (trasladoEntranteDTO.getTokenIdentificador() == null) {
                if (traslado.getNumTraslado() == null) traslado.setNumTraslado(generarCodigoIncremental());
                if (traslado.getTokenIdentificador() == null) traslado.setTokenIdentificador(UUID.randomUUID().toString());
                traslado.setFechaCreacion(new Date());
                traslado.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                traslado.setIpCrea(httpServletRequest.getRemoteAddr());
                traslado.setInstanciaProceso(tareaActual.getInstanciaProceso());
            } else {
                traslado.setFechaEdicion(new Date());
                traslado.setIpEdita(httpServletRequest.getRemoteAddr());
                traslado.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
            }

            List<TrasladoAdolescente> trasladoAdolescentes = new ArrayList<>();
            for (TrasladoAdolescenteDTO adolescente : trasladoEntranteDTO.getTrasladoAdolescentes()) {
                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(adolescente.getFichaIdentificacion().getTokenIdentificador(), false);
                TrasladoAdolescente trasladoAdolescente = new TrasladoAdolescente();
                trasladoAdolescente.setTraslado(traslado);
                trasladoAdolescente.setFichaIdentificacion(ficha);
                trasladoAdolescente.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_ACTIVO", false));
                trasladoAdolescentes.add(trasladoAdolescente);
                trasladoAdolescente.setIsComplete(false);
            }
            traslado.setTrasladoAdolescentes(trasladoAdolescentes);

            traslado = this.trasladoRepository.save(traslado);

            tareaActual.setEstado("Completada");
            tareaActual.setFechaEdicion(new Date());
            tareaActual.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
            tareaActual.setIpEdita(httpServletRequest.getRemoteAddr());
            tareaActual.setUrl(tareaActual.getPaso().getUrl() + "/" + traslado.getTokenIdentificador());
            this.tareaRepository.save(tareaActual);

            Tarea siguienteTarea = this.tareaRepository.findByInstanciaProcesoTokenIdentificadorAndOrdenAndEstadoAndRemovido(
                    tareaActual.getInstanciaProceso().getTokenIdentificador(),
                    tareaActual.getOrden() + 1,
                    "Pendiente",
                    false
            );
            // Obtener siguiente tarea, caso contrario finaliza el flujo
            if (siguienteTarea != null) {
                Traslado trasladoNuevo = new Traslado();
                trasladoNuevo.setFechaCreacion(new Date());
                trasladoNuevo.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                trasladoNuevo.setIpCrea(httpServletRequest.getRemoteAddr());
                trasladoNuevo.setTrasladoAdolescentes(new ArrayList<>());
                trasladoNuevo.setNumTraslado(traslado.getNumTraslado());
                trasladoNuevo.setCentroOrigen(traslado.getCentroOrigen());
                trasladoNuevo.setCentroDestino(traslado.getCentroDestino());
                trasladoNuevo.setMotivoTraslado(traslado.getMotivoTraslado());
                trasladoNuevo.setAntecedentes(traslado.getAntecedentes());
                trasladoNuevo.setAnalisis(traslado.getAnalisis());
                trasladoNuevo.setConclusiones(traslado.getConclusiones());
                trasladoNuevo.setUsuarioCreaTraslado(traslado.getUsuarioCreaTraslado());
                trasladoNuevo.setRecomendaciones(traslado.getRecomendaciones());
                trasladoNuevo.setDescripcionSolicitud(traslado.getDescripcionSolicitud());
                trasladoNuevo.setInstanciaProceso(tareaActual.getInstanciaProceso());

                List<TrasladoAdolescente> listaTemp = new ArrayList<>(traslado.getTrasladoAdolescentes());
                List<TrasladoAdolescente> listaAdolescentesNuevos = new ArrayList<>();


                for (TrasladoAdolescente adolescente : listaTemp) {
                    TrasladoAdolescente trasladoAdolescente = new TrasladoAdolescente();
                    trasladoAdolescente.setFichaIdentificacion(adolescente.getFichaIdentificacion());
                    trasladoAdolescente.setTraslado(trasladoNuevo);
                    listaAdolescentesNuevos.add(trasladoAdolescente);
                }
                trasladoNuevo.setTrasladoAdolescentes(listaAdolescentesNuevos);

                trasladoNuevo = this.trasladoRepository.save(trasladoNuevo);

                siguienteTarea.setEstado("En curso");
                siguienteTarea.setFechaCreacion(new Date());
                siguienteTarea.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                siguienteTarea.setIpCrea(httpServletRequest.getRemoteAddr());
                siguienteTarea.setUrl(siguienteTarea.getPaso().getUrl() + "/" + trasladoNuevo.getTokenIdentificador());
                this.tareaRepository.save(siguienteTarea);

                if (siguienteTarea.getPaso().getRequiereNotificacionCorreo()) {
                    List<TareaUsuario> listaTareaUsuarios = this.tareaUsuarioRepository.findByTareaTokenIdentificadorAndRemovido(siguienteTarea.getTokenIdentificador(), false);
                    List<String> listaCorreos = new ArrayList<>();

                    for (TareaUsuario tareaUsuario : listaTareaUsuarios) {
                        if (tareaUsuario.getUsuarioSistema().getEmail() != null) {
                            listaCorreos.add(tareaUsuario.getUsuarioSistema().getEmail());
                        }
                    }

                    this.emailService.enviarCorreo(listaCorreos, "PROCESO: " + siguienteTarea.getPaso().getProceso().getNombre() + " - " + siguienteTarea.getPaso().getNombre(), "Tiene un proceso pendiente, revise su bandeja de entrada<br>" + trasladoEntranteDTO.getHtml(), df2.getData().getEmpresa().getTokenIdentificador(), "text/html", null);

                }

            } else {
                //TODO: MANEJAR LÓGICA DE COMPLETAR FLUJO
                traslado.setCompletado(true);
                this.trasladoRepository.save(traslado);

                for (TrasladoAdolescente trasladoAdolescente : traslado.getTrasladoAdolescentes()) {
                    trasladoAdolescente.setCompletado(true);
                }
                this.trasladoAdolescenteRepository.saveAll(traslado.getTrasladoAdolescentes());

                for (TrasladoAdolescente adolescente : traslado.getTrasladoAdolescentes()) {
                    adolescente.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_ACTIVO", false));
                    //Creacion de objeto auditoria entrada/salida
                    HistoricoEntradaSalida historico = new HistoricoEntradaSalida();
                    historico.setNumeroIdentificacion(adolescente.getFichaIdentificacion().getNumeroIdentificacion());
                    historico.setFichaIdentificacion(adolescente.getFichaIdentificacion());
//            historico.setTipoDocumentoIdentificacion();
                    historico.setFechaEntrada(new Date());
                    historico.setRegistroActivo(true);
                    historico.setTraslado(traslado);
                    historico.setCentroSalida(traslado.getCentroOrigen());
                    historico.setFechaSalida(traslado.getFechaCreacion());
                    historico.setTrasladoAdolescente(adolescente);
                    historico.setMotivoSalida(this.catalogoRepository.findByNemonicoAndRemovido("SALIDA_TRASLADO", false));
                    this.historicoEntradaSalidaRepository.save(historico);
                    trasladoAdolescenteRepository.save(adolescente);
                }
                traslado.setEstadoTraslado(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_TRASLADO_INICIALIZADO", false));
                trasladoRepository.save(traslado);

            }


            df.llenarRespuestaExitosa("Se ha creado con éxito el registro de traslado.", trasladoEntranteDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<TrasladoDTO> guardarBorrador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TareaTrasladoDTO tareaTrasladoDTO = new Gson().fromJson(bodyDecifrado, TareaTrasladoDTO.class);

            TrasladoDTO trasladoEntranteDTO = tareaTrasladoDTO.getTraslado();
            TareaDTO tareaDTO = tareaTrasladoDTO.getTarea();

            Tarea tareaActual = this.tareaRepository.findByTokenIdentificadorAndRemovido(tareaDTO.getTokenIdentificador(), false);

            if (tareaActual == null) {
                df.setMensaje("No se està obteniendo una tarea del flujo, no es posible continuar.");
                return df;
            }

            Traslado traslado;

            if (trasladoEntranteDTO.getTokenIdentificador() != null) {
                traslado = this.trasladoRepository.findByTokenIdentificadorAndRemovido(trasladoEntranteDTO.getTokenIdentificador(), false);

                List<TrasladoAdolescente> adolescentes = trasladoAdolescenteRepository.findByTrasladoTokenIdentificadorAndRemovido(traslado.getTokenIdentificador(), false);
                trasladoAdolescenteRepository.deleteAll(adolescentes);
            }

            traslado = dtoAEntidad(trasladoEntranteDTO);

            if (trasladoEntranteDTO.getTokenIdentificador() == null) {
                if (traslado.getNumTraslado() == null) traslado.setNumTraslado(generarCodigoIncremental());
                traslado.setFechaCreacion(new Date());
                traslado.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                traslado.setIpCrea(httpServletRequest.getRemoteAddr());
                traslado.setInstanciaProceso(tareaActual.getInstanciaProceso());
            } else {
                traslado.setTokenIdentificador(trasladoEntranteDTO.getTokenIdentificador());
                traslado.setFechaEdicion(new Date());
                traslado.setIpEdita(httpServletRequest.getRemoteAddr());
                traslado.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
            }

            List<TrasladoAdolescente> trasladoAdolescentes = new ArrayList<>();
            for (TrasladoAdolescenteDTO adolescente : trasladoEntranteDTO.getTrasladoAdolescentes()) {
                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(adolescente.getFichaIdentificacion().getTokenIdentificador(), false);
                TrasladoAdolescente trasladoAdolescente = new TrasladoAdolescente();
                trasladoAdolescente.setTraslado(traslado);
                trasladoAdolescente.setFichaIdentificacion(ficha);
                trasladoAdolescente.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_ACTIVO", false));
                trasladoAdolescentes.add(trasladoAdolescente);
                trasladoAdolescente.setIsComplete(false);
            }
            traslado.setTrasladoAdolescentes(trasladoAdolescentes);

            traslado = this.trasladoRepository.save(traslado);

            if (trasladoEntranteDTO.getTokenIdentificador() == null) {
                tareaActual.setFechaEdicion(new Date());
                tareaActual.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                tareaActual.setIpEdita(httpServletRequest.getRemoteAddr());
                tareaActual.setUrl(tareaActual.getPaso().getUrl() + "/" + traslado.getTokenIdentificador());

                this.tareaRepository.save(tareaActual);
            }

            df.llenarRespuestaExitosa("Se ha guardado con éxito el borrador de traslado.", entidadADto(traslado));

        } catch (
                Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<TrasladoDTO> eliminarTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TrasladoDTO trasladoEntranteDTO = new Gson().fromJson(bodyDecifrado, TrasladoDTO.class);

            Traslado trasladoEncontrado = this.trasladoRepository.findByTokenIdentificadorAndRemovido(trasladoEntranteDTO.getTokenIdentificador(), false);

            if (trasladoEncontrado == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }

            trasladoEncontrado.setRemovido(true);
            trasladoEncontrado.setFechaEliminacion(new Date());
            this.trasladoRepository.save(trasladoEncontrado);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito el registro de traslado.", trasladoEntranteDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /*@Override
    @Transactional
    public RespuestaPorDefectoAuditoria<TrasladoDTO> rechazarTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TrasladoDTO trasladoEntranteDTO = new Gson().fromJson(bodyDecifrado, TrasladoDTO.class);

            Traslado trasladoEncontrado = this.trasladoRepository.findByTokenIdentificadorAndRemovido(trasladoEntranteDTO.getTokenIdentificador(), false);

            Tarea tareaActual = this.flujoService.rechazarTareaActual(df2.getData(), trasladoEncontrado.getInstanciaProceso(), trasladoEncontrado.getTokenIdentificador());
            if (tareaActual == null) {
                df.setMensajeErrorReal("No se puede continuar.");
                return df;
            }

            df.llenarRespuestaExitosa("Se ha rechazado el registro.", trasladoEntranteDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }*/

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<TrasladoDTO> rechazarTraslado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<TrasladoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            TareaTrasladoDTO tareaTrasladoDTO = new Gson().fromJson(bodyDecifrado, TareaTrasladoDTO.class);

            TrasladoDTO trasladoEntranteDTO = tareaTrasladoDTO.getTraslado();
            TareaDTO tareaDTO = tareaTrasladoDTO.getTarea();

            Tarea tareaActual = this.tareaRepository.findByTokenIdentificadorAndRemovido(tareaDTO.getTokenIdentificador(), false);

            if (tareaActual == null) {
                df.setMensaje("No se està obteniendo una tarea del flujo, no es posible continuar.");
                return df;
            }

            Traslado traslado = dtoAEntidad(trasladoEntranteDTO);
            if (traslado.getNumTraslado() == null) traslado.setNumTraslado(generarCodigoIncremental());
            traslado.setIdTraslado(null);
            traslado.setFechaCreacion(new Date());
            traslado.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
            traslado.setIpCrea(httpServletRequest.getRemoteAddr());
            traslado.setInstanciaProceso(tareaActual.getInstanciaProceso());

            List<TrasladoAdolescente> trasladoAdolescentes = new ArrayList<>();
            for (TrasladoAdolescenteDTO adolescente : trasladoEntranteDTO.getTrasladoAdolescentes()) {
                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(adolescente.getFichaIdentificacion().getTokenIdentificador(), false);
                TrasladoAdolescente trasladoAdolescente = new TrasladoAdolescente();
                trasladoAdolescente.setTraslado(traslado);
                trasladoAdolescente.setFichaIdentificacion(ficha);
                trasladoAdolescente.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_ACTIVO", false));
                trasladoAdolescentes.add(trasladoAdolescente);
                trasladoAdolescente.setIsComplete(false);
            }
            traslado.setTrasladoAdolescentes(trasladoAdolescentes);

            traslado = this.trasladoRepository.save(traslado);

            tareaActual.setEstado("Rechazada");
            tareaActual.setFechaEdicion(new Date());
            tareaActual.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
            tareaActual.setIpEdita(httpServletRequest.getRemoteAddr());
            tareaActual.setUrl(tareaActual.getPaso().getUrl() + "/" + traslado.getTokenIdentificador());
            this.tareaRepository.save(tareaActual);

            df.llenarRespuestaExitosa("Se ha rechazado el registro.", trasladoEntranteDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private String generarCodigoIncremental() {
        String prefijo = "TRAS";

        Traslado ultimoTrasladoEncontrado = trasladoRepository.findFirstByOrderByIdTrasladoDesc();

        long nuevoId = 1L;
        if (ultimoTrasladoEncontrado != null) {
            Long ultimoId = ultimoTrasladoEncontrado.getIdTraslado();
            nuevoId = ultimoId + 1;
        }

        return String.format("%s-%08d", prefijo, nuevoId);
    }


    @Override
    public RespuestaPorDefectoAuditoria<List<TrasladoDTO>> obtenerListadoTrasladosPorAdolescente(HttpServletRequest httpServletRequest, Long idFichaIdentificacion) {
        RespuestaPorDefectoAuditoria<List<TrasladoDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Buscar todos los traslados asociados al adolescente
            List<Traslado> traslados = this.trasladoRepository.findByTrasladoAdolescentesFichaIdentificacionIdFichaIdentificacionAndRemovido(idFichaIdentificacion, false);

            if (traslados.isEmpty()) {
                df.setMensaje("No se encontraron traslados para el adolescente seleccionado.");
                return df;
            }

            // Convertir entidades a DTOs
            List<TrasladoDTO> trasladoDTOList = new ArrayList<>();
            for (Traslado traslado : traslados) {
                boolean contieneFicha = traslado.getTrasladoAdolescentes().stream()
                        .anyMatch(a -> a.getFichaIdentificacion() != null &&
                                a.getFichaIdentificacion().getIdFichaIdentificacion().equals(idFichaIdentificacion));

                if (contieneFicha) {
                    TrasladoDTO trasladoDTO = entidadADto(traslado);

                    List<TrasladoAdolescenteDTO> filtrados = trasladoDTO.getTrasladoAdolescentes().stream()
                            .filter(a -> a.getFichaIdentificacion() != null &&
                                    a.getFichaIdentificacion().getIdFichaIdentificacion().equals(idFichaIdentificacion) &&
                                    Boolean.FALSE.equals(a.getIsComplete()))
                            .toList();

                    trasladoDTO.setTrasladoAdolescentes(filtrados); // sobrescribimos con los filtrados
                    trasladoDTOList.add(trasladoDTO);
                }
            }

            df.llenarRespuestaExitosa("Traslados encontrados: " + trasladoDTOList.size(), trasladoDTOList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> obtenerTrasladosPorFichaIdentificacionTokenIdentificador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TrasladoDTO>> df = new RespuestaPorDefectoAuditoria<>();


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
                    Sort.by("idTraslado").descending()
            );

            Page<Traslado> trasladoPage = this.trasladoRepository.buscarTrasladosPorFichaYEstadoEvento(paginacionRequest.getTokenIdentificador(), false
                    , "ESTADO_SALIDA_INACTIVO", pageable);

            PaginacionResponse<TrasladoDTO> paginacionResponse = new PaginacionResponse<>();
            List<TrasladoDTO> trasladoDTOList = new ArrayList<>();

            for (Traslado traslado : trasladoPage.toList()) {
                TrasladoDTO trasladoDTO = entidadADto(traslado);
                trasladoDTOList.add(trasladoDTO);
            }

            paginacionResponse.setData(trasladoDTOList);
            paginacionResponse.setTotalItems(trasladoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + trasladoDTOList.size() + " de: " + trasladoPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    private TrasladoDTO entidadADto(Traslado traslado) {
        if (traslado == null) return null;

        TrasladoDTO trasladoDTO = new TrasladoDTO();
        trasladoDTO.setIdTraslado(traslado.getIdTraslado());
        trasladoDTO.setNumTraslado(traslado.getNumTraslado());
        trasladoDTO.setCentroDestino(entidadADtoJerarquia(traslado.getCentroDestino()));
        trasladoDTO.setCentroOrigen(entidadADtoJerarquia(traslado.getCentroOrigen()));
        trasladoDTO.setMotivoTraslado(entidadADtoCatalogo(traslado.getMotivoTraslado()));
        trasladoDTO.setAntecedentes(traslado.getAntecedentes());
        trasladoDTO.setAnalisis(traslado.getAnalisis());
        trasladoDTO.setConclusiones(traslado.getConclusiones());
        trasladoDTO.setRecomendaciones(traslado.getRecomendaciones());
        trasladoDTO.setDescripcionSolicitud(traslado.getDescripcionSolicitud());
        trasladoDTO.setComentarioRechazo(traslado.getComentarioRechazo());
        trasladoDTO.setInstanciaProcesoDTO(this.entidadAInstanciaDto(traslado.getInstanciaProceso()));
        trasladoDTO.setTokenIdentificador(traslado.getTokenIdentificador());
        trasladoDTO.setCompletado(traslado.getCompletado());
        trasladoDTO.setUsuarioCreaTraslado(traslado.getUsuarioCreaTraslado());

//        trasladoDTO.setNombreUsuarioCrea(traslado.getUsuarioSistemaCrea().getApellidos() + " " + traslado.getUsuarioSistemaCrea().getNombres());
        List<TrasladoAdolescenteDTO> trasladoAdolescentesDTO = new ArrayList<>();
        for (TrasladoAdolescente adolescente : traslado.getTrasladoAdolescentes()) {
            TrasladoAdolescenteDTO dto = new TrasladoAdolescenteDTO();
            dto.setIdTrasladoAdolescente(adolescente.getIdTrasladoAdolescente());
            dto.setIsComplete(adolescente.getIsComplete());
            dto.setCompletado(adolescente.getCompletado());


            FichaIdentificacionDTO fichaDTO = new FichaIdentificacionDTO();
            fichaDTO.setIdFichaIdentificacion(adolescente.getFichaIdentificacion().getIdFichaIdentificacion());
            fichaDTO.setApellidoMaterno(adolescente.getFichaIdentificacion().getApellidoMaterno());
            fichaDTO.setApellidoPaterno(adolescente.getFichaIdentificacion().getApellidoPaterno());
            fichaDTO.setNombres(adolescente.getFichaIdentificacion().getNombres());
            fichaDTO.setNumeroIdentificacion(adolescente.getFichaIdentificacion().getNumeroIdentificacion());
            fichaDTO.setTokenIdentificador(adolescente.getFichaIdentificacion().getTokenIdentificador());

            dto.setFichaIdentificacion(fichaDTO);
            trasladoAdolescentesDTO.add(dto);
        }
        trasladoDTO.setTrasladoAdolescentes(trasladoAdolescentesDTO);


        return trasladoDTO;
    }

    private Traslado dtoAEntidad(TrasladoDTO dto) {
        if (dto == null) return null;

        Traslado traslado = new Traslado();
        traslado.setIdTraslado(dto.getIdTraslado());
        traslado.setNumTraslado(dto.getNumTraslado());
        traslado.setCentroDestino(dtoAEntidadJerarquia(dto.getCentroDestino()));
        traslado.setCentroOrigen(dtoAEntidadJerarquia(dto.getCentroOrigen()));
        traslado.setMotivoTraslado(dtoAEntidadCatalogo(dto.getMotivoTraslado()));
        traslado.setAntecedentes(dto.getAntecedentes());
        traslado.setAnalisis(dto.getAnalisis());
        traslado.setConclusiones(dto.getConclusiones());
        traslado.setRecomendaciones(dto.getRecomendaciones());
        traslado.setDescripcionSolicitud(dto.getDescripcionSolicitud());
        traslado.setComentarioRechazo(dto.getComentarioRechazo());
        traslado.setInstanciaProceso(this.dtoAEntidadInstancia(dto.getInstanciaProcesoDTO()));
        traslado.setCompletado(dto.getCompletado());
        traslado.setUsuarioCreaTraslado(dto.getUsuarioCreaTraslado());
        return traslado;
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

    private Jerarquia dtoAEntidadJerarquia(JerarquiaDTO dto) {
        if (dto == null) return null;
        return this.jerarquiaRepository.findJerarquiaByTokenIdentificador(dto.getTokenIdentificador());
    }

    private JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
        if (entidad == null) return null;

        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setId(entidad.getIdJerarquia());
        dto.setNombre(entidad.getNombre());
        dto.setNemonico(entidad.getNemonico());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }

    private Proceso dtoAEntidadProceso(ProcesoDTO dto) {
        if (dto == null) return null;

        Proceso proceso = new Proceso();
        proceso.setIdProceso(dto.getIdProceso());
        proceso.setNombre(dto.getNombre());
        proceso.setVersion(dto.getVersion());
        proceso.setNemonico(dto.getNemonico());
        proceso.setTokenIdentificador(dto.getTokenIdentificador());
        proceso.setFechaCreacion(dto.getFechaCreacion());

        //if (dto.getPasos() != null) {
        List<Paso> pasos = dto.getPasos().stream()
                .map(this::dtoAEntidadPaso)
                .toList();
        pasos.forEach(paso -> paso.setProceso(proceso));
        proceso.setPasos(pasos);
        //}

        return proceso;
    }

    private Paso dtoAEntidadPaso(PasoDTO dto) {
        if (dto == null) return null;

        Paso paso = new Paso();
        paso.setIdPaso(dto.getIdPaso());
        paso.setNombre(dto.getNombre());
        paso.setUrl(dto.getUrl());
        paso.setPorcentajeAvance(dto.getPorcentajeAvance());
        paso.setOrden(dto.getOrden());
        paso.setJsonCondicional(dto.getJsonCondicional());
        paso.setRolUsuario(dto.getRolUsuario());
        paso.setRequiereNotificacionCorreo(dto.getRequiereNotificacionCorreo());
        paso.setRolUsuarioNotificacion(dto.getRolUsuarioNotificacion());
        paso.setTokenIdentificador(dto.getTokenIdentificador());
        if (dto.getRemovido() == null) dto.setRemovido(false);
        paso.setRemovido(dto.getRemovido());

        return paso;
    }

    private ProcesoDTO entidadAProcesoDto(Proceso proceso) {
        if (proceso == null) return null;

        ProcesoDTO dto = new ProcesoDTO();
        dto.setIdProceso(proceso.getIdProceso());
        dto.setNombre(proceso.getNombre());
        dto.setVersion(proceso.getVersion());
        dto.setNemonico(proceso.getNemonico());
        dto.setTokenIdentificador(proceso.getTokenIdentificador());
        dto.setFechaCreacion(proceso.getFechaCreacion());

        //if (dto.getPasos() != null) {
        List<PasoDTO> pasos = proceso.getPasos().stream()
                .map(this::entidadAPasoDto)
                .toList();
        dto.setPasos(pasos);
        //}

        return dto;
    }

    private PasoDTO entidadAPasoDto(Paso paso) {
        if (paso == null) return null;

        PasoDTO dto = new PasoDTO();
        dto.setIdPaso(paso.getIdPaso());
        dto.setNombre(paso.getNombre());
        dto.setUrl(paso.getUrl());
        dto.setPorcentajeAvance(paso.getPorcentajeAvance());
        dto.setOrden(paso.getOrden());
        dto.setJsonCondicional(paso.getJsonCondicional());
        dto.setRolUsuario(paso.getRolUsuario());
        dto.setRolUsuarioNotificacion(paso.getRolUsuarioNotificacion());
        dto.setRequiereNotificacionCorreo(paso.getRequiereNotificacionCorreo());
        dto.setTokenIdentificador(paso.getTokenIdentificador());
        if (paso.getRemovido() == null) paso.setRemovido(false);
        dto.setRemovido(paso.getRemovido());

        return dto;
    }

    private InstanciaProceso dtoAEntidadInstancia(InstanciaProcesoDTO dto) {
        if (dto == null) return null;

        InstanciaProceso entidad = new InstanciaProceso();
        entidad.setIdInstanciaProceso(dto.getIdInstanciaProceso());
        entidad.setEstado(dto.getEstado());
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setTokenIdentificador(dto.getTokenIdentificador());
        entidad.setFechaCreacion(dto.getFechaCreacion());
        entidad.setProceso(this.dtoAEntidadProceso(dto.getProceso()));

        List<Tarea> tareas = dto.getTareas().stream()
                .map(this::dtoAEntidadTarea)
                .toList();
        tareas.forEach(tarea -> tarea.setInstanciaProceso(entidad));
        entidad.setTareas(tareas);

        return entidad;
    }

    private Tarea dtoAEntidadTarea(TareaDTO dto) {
        if (dto == null) return null;

        Tarea entidad = new Tarea();
        entidad.setIdTarea(dto.getIdTarea());
        entidad.setEstado(dto.getEstado());
        entidad.setComentario(dto.getComentario());
        entidad.setComentarioRechazo(dto.getComentarioRechazo());
        entidad.setOrden(dto.getOrden());
        entidad.setUrl(dto.getUrl());
        entidad.setRolUsuarioRecibe(dto.getRolUsuarioRecibe());
        entidad.setRolUsuarioEnvia(dto.getRolUsuarioEnvia());
        entidad.setPaso(dtoAEntidadPaso(dto.getPaso()));
        entidad.setTokenIdentificador(dto.getTokenIdentificador());
        entidad.setFechaCreacion(dto.getFechaCreacion());
        return entidad;
    }

    private InstanciaProcesoDTO entidadAInstanciaDto(InstanciaProceso entidad) {
        if (entidad == null) return null;

        InstanciaProcesoDTO dto = new InstanciaProcesoDTO();
        dto.setIdInstanciaProceso(entidad.getIdInstanciaProceso());
        dto.setEstado(entidad.getEstado());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setProceso(this.entidadAProcesoDto(entidad.getProceso()));

        List<TareaDTO> tareas = entidad.getTareas().stream()
                .map(this::entidadATareaDto)
                .toList();
        dto.setTareas(tareas);

        return dto;
    }

    private TareaDTO entidadATareaDto(Tarea entidad) {
        if (entidad == null) return null;

        TareaDTO dto = new TareaDTO();
        dto.setIdTarea(entidad.getIdTarea());
        dto.setEstado(entidad.getEstado());
        dto.setComentario(entidad.getComentario());
        dto.setComentarioRechazo(entidad.getComentarioRechazo());
        dto.setOrden(entidad.getOrden());
        dto.setUrl(entidad.getUrl());
        dto.setRolUsuarioRecibe(entidad.getRolUsuarioRecibe());
        dto.setRolUsuarioEnvia(entidad.getRolUsuarioEnvia());
        dto.setPaso(entidadAPasoDto(entidad.getPaso()));
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        return dto;
    }


    private TrasladoAdolescenteDTO entidadADtoTrasladoAdolescente(TrasladoAdolescente entidad) {
        if (entidad == null) return null;
        TrasladoAdolescenteDTO dto = new TrasladoAdolescenteDTO();
        dto.setIdTrasladoAdolescente(entidad.getIdTrasladoAdolescente());
        dto.setIsComplete(entidad.getIsComplete());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    public void actualizarTrasladoPorTrasladoAdolescente(TrasladoAdolescente dto) {

        Optional<Traslado> trasladoOptional = this.trasladoRepository.findByTrasladoAdolescentesIdTrasladoAdolescente(dto.getIdTrasladoAdolescente());
        Boolean trasladoCompletado = true;
        if (trasladoOptional.isPresent()) {
            Traslado traslado = null;
            traslado = trasladoOptional.get();
            for (TrasladoAdolescente trasladoAdolescente : traslado.getTrasladoAdolescentes()) {
                if (!trasladoAdolescente.getEstadoEvento().getNemonico().equals("ESTADO_SALIDA_INACTIVO")) {
                    trasladoCompletado = false;
                    break;
                }
            }
            if (trasladoCompletado) {
                traslado.setEstadoTraslado(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_TRASLADO_FINALIZADO", false));
            }
            this.trasladoRepository.save(traslado);
        }
    }

}
