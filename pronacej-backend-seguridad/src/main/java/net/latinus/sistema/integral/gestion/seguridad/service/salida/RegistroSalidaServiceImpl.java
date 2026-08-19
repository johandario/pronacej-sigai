package net.latinus.sistema.integral.gestion.seguridad.service.salida;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.InstanciaProceso;
import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.InformePermisoSalidaAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.InstanciaProcesoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.InformePermisoSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.HistoricoEntradaSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.fuga.EventoFugaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ActaExternamientoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.InformeFinalAbiertoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.InformePermisoSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.ReforzamientoDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.RegistroSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIngresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.tras.TrasladoAdolescenteRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.tras.TrasladoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.HistoricoFichaIdentificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class RegistroSalidaServiceImpl implements RegistroSalidaService {

    private CatalogoRepository catalogoRepository;
    private RegistroSalidaRepository registroSalidaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JerarquiaRepository jerarquiaRepository;
    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;
    private EventoFugaRepository fugaRepository;
    private TrasladoRepository trasladoRepository;
    private InformePermisoSalidaRepository informePermisoSalidaRepository;
    private HistoricoEntradaSalidaRepository historicoEntradaSalidaRepository;
    private ActaExternamientoRepository actaExternamientoRepository;
    private InformeFinalAbiertoRepository informeFinalAbiertoRepository;
    private TrasladoAdolescenteRepository trasladoAdolescenteRepository;
    private FichaIngresoRepository fichaIngresoRepository;
    private ReforzamientoDocumentoRepository reforzamientoDocumentoRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private HistoricoFichaIdentificacionService historicoFichaIdentificacionService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> obtenerlistadoPorToken(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            String tokenIdentificadorAdolescente = paginacionRequest.getTokenIdentificador();
            if (tokenIdentificadorAdolescente == null || tokenIdentificadorAdolescente.isEmpty()) {
                throw new IllegalArgumentException("El campo tokenIdentificadorAdolescente es requerido.");
            }
            var listaSalidas = registroSalidaRepository.findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
                    tokenIdentificadorAdolescente,
                    false
            );
            List<RegistroSalidaDTO> salidaDTOList = new ArrayList<>();
            for (RegistroSalida salida : listaSalidas) {
                RegistroSalidaDTO salidaDTO = entidadADto(salida);
                salidaDTOList.add(salidaDTO);
            }
            salidaDTOList.sort((a, b) -> b.getFechaHoraSalida().compareTo(a.getFechaHoraSalida()));
            PaginacionResponse<RegistroSalidaDTO> paginacionResponse = paginacionService.obtenerDatos(salidaDTOList, paginacionRequest);
            respuesta.llenarRespuestaExitosa("Salidas encontradas", paginacionResponse);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> obtenerSalidas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idRegistroSalida").descending()
            );
            Page<RegistroSalida> fugaPage;
            if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
                if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                    fugaPage = this.registroSalidaRepository.buscarPorFiltroYCentro(
                            paginacionRequest.getTokenIdentificador(),
                            paginacionRequest.getFilter(),
                            pageable
                    );
                }
                else {
                    fugaPage = this.registroSalidaRepository.findByCentroTokenIdentificador(
                            paginacionRequest.getTokenIdentificador(), pageable
                    );
                }
            }
            else if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                fugaPage = this.registroSalidaRepository.buscarPorFiltro(paginacionRequest.getFilter(), pageable);
            }
            else {
                fugaPage = this.registroSalidaRepository.findByRemovido(false, pageable);
            }
            PaginacionResponse<RegistroSalidaDTO> paginacionResponse = new PaginacionResponse<>();
            List<RegistroSalidaDTO> fugaDTOList = new ArrayList<>();
            for (RegistroSalida fuga : fugaPage.toList()) {
                RegistroSalidaDTO fugaDTO = entidadADto(fuga);
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
    public RespuestaPorDefectoAuditoria<RegistroSalidaDTO> obtenerRegistroSalidaPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<RegistroSalidaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            RegistroSalida fuga = this.registroSalidaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if (fuga == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            RegistroSalidaDTO fugaDTO = entidadADto(fuga);
//            List<ActividadSalidaDTO> actividadDTO = new ArrayList<>();
//            for (ActividadSalida actividad : fuga.getActividades()) {
//                ActividadSalidaDTO actividadDTOItem = new ActividadSalidaDTO();
//                actividadDTOItem.setIdActividadSalida(actividad.getIdActividadSalida());
//                actividadDTOItem.setDescripcion(actividad.getDescripcion());
//                actividadDTO.add(actividadDTOItem);
//            }
//            fugaDTO.setActividades(actividadDTO);
            df.llenarRespuestaExitosa("Se ha encontrado el registro de salida del adolescente: " + fugaDTO.getNombreAdolescente(), fugaDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<RegistroSalidaDTO> crearRegistroSalida(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<RegistroSalidaDTO> df = new RespuestaPorDefectoAuditoria<>();
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

            RegistroSalidaDTO fugaEntranteDTO = new Gson().fromJson(bodyDecifrado, RegistroSalidaDTO.class);
            RegistroSalida fugaEncontrado = this.registroSalidaRepository.findByTokenIdentificadorAndRemovido(fugaEntranteDTO.getTokenIdentificador(), false);
            if (fugaEncontrado == null && fugaEntranteDTO.getEsEdicion()) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            if (!fugaEntranteDTO.getEsEdicion()) {
                RegistroSalida fuga = dtoAEntidad(fugaEntranteDTO, fichaIdentificacionRepository);
                fuga.setFechaCreacion(new Date());
                fuga.setTokenIdentificador(UUID.randomUUID().toString());
                fuga = this.registroSalidaRepository.save(fuga);
                FichaIdentificacion fichaIdentificacion = fuga.getTokenFichaIdentificacion();
                fichaIdentificacion.setTieneProceso(true);
                fichaIdentificacionRepository.save(fichaIdentificacion);
                if (fichaIdentificacion != null) {
                    String nombreCompleto =
                            (fichaIdentificacion.getNombres() != null ? fichaIdentificacion.getNombres() : "") + " " +
                                    (fichaIdentificacion.getApellidoPaterno() != null ? fichaIdentificacion.getApellidoPaterno() : "") + " " +
                                    (fichaIdentificacion.getApellidoMaterno() != null ? fichaIdentificacion.getApellidoMaterno() : "");
                    fugaEntranteDTO.setNombreAdolescente(nombreCompleto.trim());
                }
                String mensajeUsuario = "Se creó con éxito el registro de salida:" + fugaEntranteDTO.getNroDocumento();
                String mensajeAuditoria = "Se creó con éxito el registro de salida: " + fugaEntranteDTO.getNroDocumento();
                df.llenarRespuestaExitosa(mensajeUsuario, fugaEntranteDTO, mensajeAuditoria);
                if (fuga.getExternamiento() != null || fuga.getInformeFinalAbierto() !=null) {
                    fichaIdentificacion.setEstado(
                            this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_ADOLESCENTE_LIBRE", false)
                    );
                    List<FichaIngreso> fichasIngreso = fichaIngresoRepository.findByFichaIdentificacionTokenIdentificadorAndRemovidoAndActivo(
                            fichaIdentificacion.getTokenIdentificador(), false, true
                    );
                    if (!fichasIngreso.isEmpty()) {
                        for (FichaIngreso ficha : fichasIngreso) {
                            ficha.setActivo(false);
                        }
                        fichaIngresoRepository.saveAll(fichasIngreso);
                    } else {
                        System.out.println("No se encontraron fichas de ingreso activas.");
                    }
                }
                else if (fuga.getPermisoSalida() != null){
                    fichaIdentificacion.setPermisoTemporal(true);
                    fichaIdentificacionRepository.save(fichaIdentificacion);
                }
                else {
                    System.out.println(" NO entró al if: fuga.getExternamiento() es NULL.");
                }
                if(fugaEntranteDTO.getMotivoSalida().getNemonico().equals("SALIDA_FUGA")){

                    EventoFuga eventoFuga = this.fugaRepository.findByTokenIdentificadorAndRemovido(
                            fugaEntranteDTO.getEventoFuga().getTokenIdentificador(), false
                    );

                    Optional<HistoricoEntradaSalida> historicoEntradaSalida = this.historicoEntradaSalidaRepository.findByEventoFugaTokenIdentificador(
                            eventoFuga.getTokenIdentificador()
                    );

                    if(historicoEntradaSalida.isPresent()){
                        HistoricoEntradaSalida historico = historicoEntradaSalida.get();
                        historico.setRegistroSalida(fuga);
                        this.historicoEntradaSalidaRepository.save(historico);
                    }

                }else if(fugaEntranteDTO.getMotivoSalida().getNemonico().equals("SALIDA_TRASLADO")){

                    Traslado traslado = this.trasladoRepository.findByTokenIdentificadorAndRemovido(
                            fugaEntranteDTO.getTraslado().getTokenIdentificador(), false
                    );

                    Optional<TrasladoAdolescente> trasladoAdolescente = this.trasladoAdolescenteRepository.findByTrasladoTokenAndFichaIdentificacionToken(
                            traslado.getTokenIdentificador(),
                            fichaIdentificacion.getTokenIdentificador()
                    );

                    if(trasladoAdolescente.isPresent()){
                        Optional<HistoricoEntradaSalida> historicoEntradaSalida = this.historicoEntradaSalidaRepository.findByTrasladoAdolescenteTokenIdentificador(
                                fugaEntranteDTO.getTraslado().getTokenIdentificador()
                        );
                        if(historicoEntradaSalida.isPresent()){
                            HistoricoEntradaSalida historico = historicoEntradaSalida.get();
                            historico.setRegistroSalida(fuga);
                            this.historicoEntradaSalidaRepository.save(historico);
                        }
                    }


                }else if(fugaEntranteDTO.getMotivoSalida().getNemonico().equals("SALIDA_TEMPORAL")){

                    InformePermisoSalidaAdolescente salidaTemporal = this.informePermisoSalidaRepository.findByTokenIdentificadorAndRemovido(
                            fugaEntranteDTO.getPermisoSalida().getTokenIdentificador(), false
                    );

                    List<HistoricoEntradaSalida> historicos = this.historicoEntradaSalidaRepository.findByPermisoSalidaTemporalTokenIdentificador(
                            salidaTemporal.getTokenIdentificador()
                    );
                    if (!historicos.isEmpty()) {
                        for (HistoricoEntradaSalida historico : historicos) {
                            historico.setRegistroSalida(fuga);
                        }
                        this.historicoEntradaSalidaRepository.saveAll(historicos);
                    }

                }else if(fugaEntranteDTO.getMotivoSalida().getNemonico().equals("SALIDA_EXTERNAMIENTO")){
                    ActaExternamiento actaExternamiento = this.actaExternamientoRepository.findByTokenIdentificadorAndRemovido(
                            fugaEntranteDTO.getExternamiento().getTokenIdentificador(),false
                    );
                    Optional<HistoricoEntradaSalida> historicoEntradaSalida = this.historicoEntradaSalidaRepository.findByExternamientoTokenIdentificador(
                            fugaEntranteDTO.getExternamiento().getTokenIdentificador()
                    );

                    fichaIdentificacion.setTieneProceso(false);
                    fichaIdentificacionRepository.save(fichaIdentificacion);

                }else if(fugaEntranteDTO.getMotivoSalida().getNemonico().equals("SALIDA_INFORME_FINAL")){

                    InformeFinalAbierto informeFinalAbierto = this.informeFinalAbiertoRepository.findByTokenIdentificadorAndRemovido(
                            fugaEntranteDTO.getInformeFinalAbierto().getTokenIdentificador(),false);

                    Optional<HistoricoEntradaSalida> historicoEntradaSalida = this.historicoEntradaSalidaRepository.findByInformeFinalTokenIdentificador(
                            fugaEntranteDTO.getInformeFinalAbierto().getTokenIdentificador()
                    );

//                    procesarSalida(historicoEntradaSalida, fichaIdentificacion, fuga);

                }

                // Llenar histórico de ficha de identificación
                List<String> motivosSalida = List.of(
                        "SALIDA_FUGA",
                        "SALIDA_TRASLADO",
                        "SALIDA_EXTERNAMIENTO",
                        "SALIDA_INFORME_FINAL"
                );

                if (motivosSalida.contains(fugaEntranteDTO.getMotivoSalida().getNemonico())){
                    AuditObject auditObject = AuditObject.builder()
                            .usuarioSistema(df2.getData().getUsuarioSistema())
                            .ip(httpServletRequest.getRemoteAddr())
                            .build();

                    this.historicoFichaIdentificacionService.crearActualizar(
                            fichaIdentificacion,
                            null,
                            fuga,
                            auditObject,
                            true);
                }

            } else {
                RegistroSalida fuga = dtoAEntidad(fugaEntranteDTO, fichaIdentificacionRepository);
                fuga.setFechaEdicion(new Date());
                this.registroSalidaRepository.save(fuga);
                FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
                if (ficha != null) {
                    String nombreCompleto =
                            (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                                    (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                                    (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "");
                    fugaEntranteDTO.setNombreAdolescente(nombreCompleto.trim());
                }
                //df.llenarRespuestaExitosa("Se ha editado con éxito el registro. ", fugaEntranteDTO);
                String mensajeUsuario = "Se editado con éxito el registro de salida del adolescente: " + fugaEntranteDTO.getNombreAdolescente();
                String mensajeAuditoria = "Se editó con éxito el registro de salida del adolescente: " + fugaEntranteDTO.getNombreAdolescente();
                df.llenarRespuestaExitosa(mensajeUsuario, fugaEntranteDTO, mensajeAuditoria);

                // Llenar histórico de ficha de identificación
                if ("SALIDA_EXTERNAMIENTO".equals(fugaEntranteDTO.getMotivoSalida().getNemonico())){
                    FichaIdentificacion fichaIdentificacion = fuga.getTokenFichaIdentificacion();

                    AuditObject auditObject = AuditObject.builder()
                            .usuarioSistema(df2.getData().getUsuarioSistema())
                            .ip(httpServletRequest.getRemoteAddr())
                            .build();

                    this.historicoFichaIdentificacionService.crearActualizar(
                            fichaIdentificacion,
                            null,
                            fuga,
                            auditObject,
                            true);
                }
            }



        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarRegistroSalida(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            RegistroSalidaDTO fugaDTO = new Gson().fromJson(bodyString, RegistroSalidaDTO.class);
            RegistroSalida fuga = this.registroSalidaRepository.findByTokenIdentificadorAndRemovido(
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
            this.registroSalidaRepository.save(fuga);
            FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
            String nombreCompleto = "";
            if (ficha != null) {
                nombreCompleto =
                        (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                                (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                                (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "");

            }

            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema el registro de salida del adolescente: "+ nombreCompleto
                    , true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> obtenerlistadoFugasTrasladosCompletados(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroSalidaDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idRegistroSalida").descending()
            );
            Page<RegistroSalida> fugaPage;

            if (paginacionRequest.getFilter() != null) {
                String textoFiltro = paginacionRequest.getFilter();
                fugaPage = this.registroSalidaRepository.obtenerSalidasConFugaYTrasladoInactivosPorFiltro(paginacionRequest.getTokenIdentificador(), textoFiltro, pageable);
            } else {
                fugaPage = this.registroSalidaRepository.obtenerSalidasConFugaYTrasladoInactivos(paginacionRequest.getTokenIdentificador(), pageable);
            }

            PaginacionResponse<RegistroSalidaDTO> paginacionResponse = new PaginacionResponse<>();
            List<RegistroSalidaDTO> fugaDTOList = new ArrayList<>();
            for (RegistroSalida fuga : fugaPage.toList()) {
                RegistroSalidaDTO fugaDTO = entidadADto(fuga);
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


    @NotNull
    private static RegistroSalidaDTO entidadADto(RegistroSalida fuga) {
        RegistroSalidaDTO fugaDTO = new RegistroSalidaDTO();
        fugaDTO.setIdRegistroSalida(fuga.getIdRegistroSalida());
        fugaDTO.setFechaHoraSalida(fuga.getFechaHoraSalida());
        fugaDTO.setUsuarioSalida(fuga.getUsuarioSalida());
        fugaDTO.setMotivoSalida(entidadADtoCatalogo(fuga.getMotivoSalida()));
        fugaDTO.setNroDocumento(fuga.getNroDocumento());
        fugaDTO.setFechaHoraRegreso(fuga.getFechaHoraRegreso());
        fugaDTO.setObservaciones(fuga.getObservaciones());
        fugaDTO.setTipoSalida(entidadADtoCatalogo(fuga.getTipoSalida()));
        fugaDTO.setTipoSalidaLugar(fuga.getTipoSalidaLugar());
        fugaDTO.setCentroSalida(entidadADtoJerarquia(fuga.getCentroSalida()));
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setMotivoSalida(entidadADtoCatalogo(fuga.getMotivoSalida()));
        fugaDTO.setEventoFuga(entidadADtoFuga(fuga.getEventoFuga()));
        fugaDTO.setTraslado(entidadADtoTraslado(fuga.getTraslado()));
        fugaDTO.setPermisoSalida(entidadADtoPermiso(fuga.getPermisoSalida()));
        fugaDTO.setExternamiento(entidadADtoermisoExternamiento(fuga.getExternamiento()));
        fugaDTO.setInformeFinalAbierto(entidadADtoInformeFinal(fuga.getInformeFinalAbierto()));
        if (fuga.getMotivoSalida() != null) {
            fugaDTO.setNombreMotivoSalida(fuga.getMotivoSalida().getNombre());
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
        return fugaDTO;
    }


    private RegistroSalida dtoAEntidad(RegistroSalidaDTO dto, FichaIdentificacionRepository fichaIdentificacionRepository) {
        RegistroSalida fuga = new RegistroSalida();
        if (dto.getTokenFichaIdentificacion() != null) {
            FichaIdentificacion ficha = fichaIdentificacionRepository.findByIdFichaIdentificacion(Long.valueOf(dto.getTokenFichaIdentificacion()));
            if (ficha == null) {
                throw new IllegalArgumentException("FichaIdentificacion no encontrada para ID: " + dto.getTokenFichaIdentificacion());
            }
            fuga.setTokenFichaIdentificacion(ficha);
        } else {
            throw new IllegalArgumentException("El tokenFichaIdentificacion no puede ser nulo");
        }
        fuga.setIdRegistroSalida(dto.getIdRegistroSalida());
        fuga.setFechaHoraSalida(dto.getFechaHoraSalida());
        fuga.setUsuarioSalida(dto.getUsuarioSalida());
        fuga.setMotivoSalida(dtoAEntidadCatalogo(dto.getMotivoSalida()));
        fuga.setEventoFuga(dtoAEntidadFuga(dto.getEventoFuga()));
        fuga.setTraslado(dtoAEntidadTraslado(dto.getTraslado()));
        fuga.setPermisoSalida(dtoAEntidadPermiso(dto.getPermisoSalida()));
        fuga.setTipoSalida(dtoAEntidadCatalogo(dto.getTipoSalida()));
        fuga.setNroDocumento(dto.getNroDocumento());
        fuga.setFechaHoraRegreso(dto.getFechaHoraRegreso());
        fuga.setObservaciones(dto.getObservaciones());
        fuga.setTipoSalidaLugar(dto.getTipoSalidaLugar());
        fuga.setCentroSalida(dtoAEntidadJerarquia(dto.getCentroSalida()));
        fuga.setExternamiento(dtoAEntidadExternamiento(dto.getExternamiento()));
        fuga.setInformeFinalAbierto(dtoAEntidadInforme(dto.getInformeFinalAbierto()));

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

    private EventoFuga dtoAEntidadFuga(EventoFugaDTO dto) {
        if (dto == null) return null;
        return this.fugaRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }

    private static EventoFugaDTO entidadADtoFuga(EventoFuga entidad) {
        if (entidad == null) return null;
        EventoFugaDTO dto = new EventoFugaDTO();
        dto.setIdFuga(entidad.getIdFuga());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaFuga(entidad.getFechaFuga());
        dto.setNumFuga(entidad.getNumFuga());
        dto.setAsunto(entidad.getAsunto());
        if(!ObjectUtils.isEmpty(entidad.getFechaCreacion())){
            dto.setFechaCreacion(entidad.getFechaCreacion());
        }
        return dto;
    }

    private Traslado dtoAEntidadTraslado(TrasladoDTO dto) {
        if (dto == null) return null;
        return this.trasladoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }

    private static TrasladoDTO entidadADtoTraslado(Traslado entidad) {
        if (entidad == null) return null;
        TrasladoDTO dto = new TrasladoDTO();
        dto.setIdTraslado(entidad.getIdTraslado());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setAnalisis(entidad.getAnalisis());
        dto.setInstanciaProcesoDTO(entidadAInstanciaDto(entidad.getInstanciaProceso()));
        dto.setNumTraslado(entidad.getNumTraslado());
        dto.setUsuarioCreaTraslado(entidad.getUsuarioCreaTraslado());
        if(!ObjectUtils.isEmpty(entidad.getMotivoTraslado())){
            dto.setMotivoTraslado(entidadADtoCatalogo(entidad.getMotivoTraslado()));
        }
        if(!ObjectUtils.isEmpty(entidad.getFechaCreacion())){
            dto.setFechaCreacion(entidad.getFechaCreacion());
        }
        return dto;
    }

    private InformePermisoSalidaAdolescente dtoAEntidadPermiso(InformePermisoSalidaDTO dto) {
        if (dto == null) return null;
        return this.informePermisoSalidaRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }

    private static InformePermisoSalidaDTO entidadADtoPermiso(InformePermisoSalidaAdolescente entidad) {
        if (entidad == null) return null;
        InformePermisoSalidaDTO dto = new InformePermisoSalidaDTO();
        dto.setIdPermisoSalida(entidad.getIdPermisoSalida());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setNroDocumento(entidad.getNroDocumento());
        dto.setFechaHoraSalida(entidad.getFechaHoraSalida());
        dto.setObservaciones(entidad.getObservaciones());
        return dto;
    }

    private static ActaExternamientoDTO entidadADtoermisoExternamiento(ActaExternamiento entidad) {
        if (entidad == null) return null;
        ActaExternamientoDTO dto = new ActaExternamientoDTO();
        dto.setIdActaExternamiento(entidad.getIdActaExternamiento());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setAutorizacion(entidad.getAutorizacion());
        dto.setFechaRegistro(entidad.getFechaRegistro());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setNumeroDocumento(entidad.getNumeroDocumento());
        return dto;
    }

    private ActaExternamiento dtoAEntidadExternamiento(ActaExternamientoDTO dto) {
        if (dto == null) return null;
        return this.actaExternamientoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }


    private static InformeFinalAbiertoDTO entidadADtoInformeFinal(InformeFinalAbierto entidad) {
        if (entidad == null) return null;
        InformeFinalAbiertoDTO dto = new InformeFinalAbiertoDTO();
        dto.setIdInformeFinalAbierto(entidad.getIdInformeFinalAbierto());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setConclusionesRecomendaciones(entidad.getConclusionesRecomendaciones());
        return dto;
    }

    private InformeFinalAbierto dtoAEntidadInforme(InformeFinalAbiertoDTO dto) {
        if (dto == null) return null;
        return this.informeFinalAbiertoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }


    private static InstanciaProcesoDTO entidadAInstanciaDto(InstanciaProceso entidad) {
        if (entidad == null) return null;

        InstanciaProcesoDTO dto = new InstanciaProcesoDTO();
        dto.setIdInstanciaProceso(entidad.getIdInstanciaProceso());
        dto.setEstado(entidad.getEstado());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        return dto;
    }

    public Optional<Documento> obtenerDocumentoActaConsentimientoPorFicha(String tokenIdentificador) {
        List<Documento> documentos = reforzamientoDocumentoRepository.buscarDocumentosActaConsentimientoPorFicha(tokenIdentificador);
        return documentos.stream().findFirst();
    }

    private void procesarSalida(Optional<HistoricoEntradaSalida> historicoEntradaSalida,
                                FichaIdentificacion fichaIdentificacion,
                                RegistroSalida fuga) {

        historicoEntradaSalida.ifPresent(historico -> {
            historico.setRegistroSalida(fuga);
            this.historicoEntradaSalidaRepository.save(historico);
        });

        Optional<Documento> documentoOptional = this.obtenerDocumentoActaConsentimientoPorFicha(fichaIdentificacion.getTokenIdentificador());

        if (documentoOptional.isPresent()) {
            fichaIdentificacion.setPostEgreso(true);
            this.fichaIdentificacionRepository.save(fichaIdentificacion);

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getCentroIngreso().getJerarquiaPadre()) &&
                    fichaIdentificacion.getCentroIngreso().getJerarquiaPadre().getNemonico().equals("CJDR")) {
                fichaIdentificacion.setCentroIngreso(
                        this.jerarquiaRepository.findJerarquiaByNemonico("UAPISE OFICINA CENTRAL")
                );
            }

            fichaIdentificacion.setEstado(catalogoRepository.findByNemonicoAndRemovido(
                    "ESTADO_ADOLESCENTE_POST_EGRESO", Boolean.FALSE
            ));
        }
    }

}
