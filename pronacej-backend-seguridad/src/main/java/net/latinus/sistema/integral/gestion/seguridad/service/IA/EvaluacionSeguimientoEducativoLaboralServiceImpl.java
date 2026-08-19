package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.EvaluacionSeguimientoEducativoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.EvaluacionSeguimientoEducativoDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.RegistroInstitucionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionSeguimientoEducativoLaboralRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.RecomendacionComentarioPorEvalSeguRepository;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@AllArgsConstructor
public class EvaluacionSeguimientoEducativoLaboralServiceImpl implements EvaluacionSeguimientoEducativoLaboralService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private EvaluacionSeguimientoEducativoLaboralRepository seguimientoRepository;
    private RecomendacionComentarioPorEvalSeguRepository recomendacionComentarioRepository;
    private RegistroInstitucionRepository registroInstitucionRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private EvaluacionSeguimientoEducativoCarpetaRepository seguimientoCarpetaRepository;
    private EvaluacionSeguimientoEducativoDocumentoRepository seguimientoDocumentoRepository;
    // Variable para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    private final LogService logService = new LogService(this.getClass());

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>> obtenerEvaluacionesSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            Empresa empresa = df2.getData().getEmpresa();
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idEvaluacionSeguimiento").descending()
            );

            Page<EvaluacionSeguimientoEducativoLaboral> evaluacionSeguimientoPage = this.seguimientoRepository
                    .buscarPorFiltro(
                            paginacionRequest.getTokenIdentificador(), paginacionRequest.getFilter(), pageable);

            PaginacionResponse<EvaluacionSeguimientoEducativoLaboralDTO> paginacionResponse = new PaginacionResponse<>();
            List<EvaluacionSeguimientoEducativoLaboralDTO> evaluacionSeguimientoDTOList = new ArrayList<>();

            for (EvaluacionSeguimientoEducativoLaboral evaluacionSeguimiento : evaluacionSeguimientoPage.toList()) {
                EvaluacionSeguimientoEducativoLaboralDTO evaluacionSeguimientoDTO = new EvaluacionSeguimientoEducativoLaboralDTO();
                evaluacionSeguimientoDTO.setTokenIdentificador(evaluacionSeguimiento.getTokenIdentificador());
                evaluacionSeguimientoDTO.setTokenIdentificadorEmpresa(evaluacionSeguimiento.getEmpresa().getTokenIdentificador());
                evaluacionSeguimientoDTO.setFechaCreacion(evaluacionSeguimiento.getFechaCreacion());

                evaluacionSeguimientoDTO.setFechaInicio(evaluacionSeguimiento.getFechaInicio());
                evaluacionSeguimientoDTO.setFechaFin(evaluacionSeguimiento.getFechaFin());

                if (evaluacionSeguimiento.getTipoEvaluacionSeguimiento() != null) {
                    evaluacionSeguimientoDTO.setTokenIdentificadorTipoEvaluacionSeguimiento(
                            evaluacionSeguimiento.getTipoEvaluacionSeguimiento().getTokenIdentificador());
                }

                if (evaluacionSeguimiento.getMedioVerificacion() != null) {
                    evaluacionSeguimientoDTO.setTokenIdentificadorMedioVerificacion(
                            evaluacionSeguimiento.getMedioVerificacion().getTokenIdentificador());
                }

                if (evaluacionSeguimiento.getInstitucionEducativaLaboral() != null) {
                    evaluacionSeguimientoDTO.setTokenIdentificadorInstitucion(
                            evaluacionSeguimiento.getInstitucionEducativaLaboral().getTokenIdentificador());
                } else {
                    evaluacionSeguimientoDTO.setNombreInstitucionOtros(evaluacionSeguimiento.getNombreInstitucionOtros());
                }
                evaluacionSeguimientoDTO.setResultadoSeguimiento(evaluacionSeguimiento.getResultadoSeguimiento());

                if (evaluacionSeguimiento.getFichaIdentificacion() != null) {
                    evaluacionSeguimientoDTO.setTokenIdentificadorFichaIdentificacion(
                            evaluacionSeguimiento.getFichaIdentificacion().getTokenIdentificador());
                }

                evaluacionSeguimientoDTO.setNombreCompletoUsuarioCreacion(
                        evaluacionSeguimiento.getUsuarioSistemaCrea().getNombres() + " " +
                                evaluacionSeguimiento.getUsuarioSistemaCrea().getApellidos());

                evaluacionSeguimientoDTOList.add(evaluacionSeguimientoDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            evaluacionSeguimientoDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(evaluacionSeguimientoDTOList);
            paginacionResponse.setTotalItems(evaluacionSeguimientoPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + evaluacionSeguimientoPage.getTotalElements() + " evaluaciones y seguimientos educativos laborales";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + evaluacionSeguimientoPage.getTotalElements() + " registros de evaluaciones y seguimientos educativos laborales";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO>> obtenerRecomendacionesComentariosPorEvaluacionSeguimiento(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            Empresa empresa = df2.getData().getEmpresa();
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idRecomendacionComentario").descending()
            );

            Page<RecomendacionComentarioPorEvalSegu> recomendacionPage = this.recomendacionComentarioRepository
                    .findByEvaluacionSeguimientoTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                            paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<RecomendacionComentarioPorEvalSeguDTO> paginacionResponse = new PaginacionResponse<>();
            List<RecomendacionComentarioPorEvalSeguDTO> recomendacionDTOList = new ArrayList<>();

            for (RecomendacionComentarioPorEvalSegu recomendacion : recomendacionPage.toList()) {
                RecomendacionComentarioPorEvalSeguDTO recomendacionDTO = new RecomendacionComentarioPorEvalSeguDTO();
                recomendacionDTO.setTokenIdentificador(recomendacion.getTokenIdentificador());
                recomendacionDTO.setTokenIdentificadorEvaluacionSeguimiento(
                        recomendacion.getEvaluacionSeguimiento().getTokenIdentificador());
                recomendacionDTO.setComentario(recomendacion.getComentario());
                recomendacionDTO.setFecha(recomendacion.getFecha());

                recomendacionDTOList.add(recomendacionDTO);
            }

            paginacionResponse.setData(recomendacionDTOList);
            paginacionResponse.setTotalItems(recomendacionPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + recomendacionPage.getTotalElements() + " recomendaciones y comentarios";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + recomendacionPage.getTotalElements() + " registros de recomendaciones y comentarios";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionSeguimientoEducativoLaboralDTO> crearEvaluacionSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionSeguimientoEducativoLaboralDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String bodyString = df22.getData();
            Empresa empresa = df2.getData().getEmpresa();
            EvaluacionSeguimientoEducativoLaboralDTO evaluacionSeguimientoDTO = new Gson()
                    .fromJson(bodyString, EvaluacionSeguimientoEducativoLaboralDTO.class);

            evaluacionSeguimientoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = evaluacionSeguimientoDTO.getTokenIdentificadorFichaIdentificacion() + "-evaluacionSeguimiento";

            Long tiempoProcesamiento = solicitudesEnProcesamiento.get(idSolicitud);
            if (tiempoProcesamiento != null) {
                if (System.currentTimeMillis() - tiempoProcesamiento < 5000) {
                    df.setExito(false);
                    df.setMensaje("Una solicitud similar ya está siendo procesada. Por favor, espere unos segundos antes de intentar nuevamente.");
                    return df;
                }
            }

            solicitudesEnProcesamiento.put(idSolicitud, System.currentTimeMillis());

            try {
                String ip = httpServletRequest.getRemoteAddr();
                UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

                EvaluacionSeguimientoEducativoLaboral evaluacionSeguimiento;
                boolean esEdicion = false;
                FichaIdentificacion fichaIdentificacion = null;

                if (evaluacionSeguimientoDTO.getEsEdicion()) {
                    evaluacionSeguimiento = seguimientoRepository
                            .findByTokenIdentificadorAndRemovido(evaluacionSeguimientoDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (evaluacionSeguimiento == null) {
                        df.setMensaje("La evaluación y seguimiento a editar no existe o ya fue eliminada anteriormente");
                        return df;
                    }
                    evaluacionSeguimiento.setFechaEdicion(new Date());
                    evaluacionSeguimiento.setIpEdita(ip);
                    evaluacionSeguimiento.setUsuarioSistemaEdita(usuarioLogin);
                    fichaIdentificacion = evaluacionSeguimiento.getFichaIdentificacion();
                    esEdicion = true;
                } else {
                    evaluacionSeguimiento = new EvaluacionSeguimientoEducativoLaboral();
                    evaluacionSeguimiento.setFechaCreacion(new Date());
                    evaluacionSeguimiento.setIpCrea(ip);
                    evaluacionSeguimiento.setUsuarioSistemaCrea(usuarioLogin);
                    evaluacionSeguimiento.setEmpresa(empresa);

                    fichaIdentificacion = fichaIdentificacionRepository
                            .findByTokenIdentificadorAndRemovido(evaluacionSeguimientoDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);
                    if (fichaIdentificacion == null) {
                        df.setMensaje("La ficha de identificación no existe o fue eliminada");
                        return df;
                    }
                    evaluacionSeguimiento.setFichaIdentificacion(fichaIdentificacion);
                }

                evaluacionSeguimiento.setFechaInicio(evaluacionSeguimientoDTO.getFechaInicio());
                evaluacionSeguimiento.setFechaFin(evaluacionSeguimientoDTO.getFechaFin());

                Catalogo tipoEvaluacionSeguimiento = catalogoRepository
                        .findByTokenIdentificadorAndRemovido(evaluacionSeguimientoDTO.getTokenIdentificadorTipoEvaluacionSeguimiento(), Boolean.FALSE);
                evaluacionSeguimiento.setTipoEvaluacionSeguimiento(tipoEvaluacionSeguimiento);

                Catalogo medioVerificacion = catalogoRepository
                        .findByTokenIdentificadorAndRemovido(evaluacionSeguimientoDTO.getTokenIdentificadorMedioVerificacion(), Boolean.FALSE);
                evaluacionSeguimiento.setMedioVerificacion(medioVerificacion);

                if (evaluacionSeguimientoDTO.getTokenIdentificadorInstitucion() != null) {
                    if (evaluacionSeguimientoDTO.getTokenIdentificadorInstitucion().equals("1")) {
                        evaluacionSeguimiento.setNombreInstitucionOtros(evaluacionSeguimientoDTO.getNombreInstitucionOtros());
                    } else {
                        RegistroInstitucion institucion = registroInstitucionRepository
                                .findByTokenIdentificadorAndRemovido(evaluacionSeguimientoDTO.getTokenIdentificadorInstitucion(), Boolean.FALSE);
                        if (institucion == null) {
                            df.setMensaje("La institución no existe o fue eliminada");
                            return df;
                        }
                        evaluacionSeguimiento.setInstitucionEducativaLaboral(institucion);
                    }

                }
                evaluacionSeguimiento.setResultadoSeguimiento(evaluacionSeguimientoDTO.getResultadoSeguimiento());

                evaluacionSeguimiento = this.seguimientoRepository.save(evaluacionSeguimiento);
                evaluacionSeguimientoDTO.setTokenIdentificador(evaluacionSeguimiento.getTokenIdentificador());

                if (evaluacionSeguimientoDTO.getListaRecomendacionesComentarios() != null &&
                        !evaluacionSeguimientoDTO.getListaRecomendacionesComentarios().isEmpty()) {

                    for (RecomendacionComentarioPorEvalSeguDTO recomendacionDTO : evaluacionSeguimientoDTO.getListaRecomendacionesComentarios()) {
                        RecomendacionComentarioPorEvalSegu recomendacion;

                        if ("0".equals(recomendacionDTO.getTokenIdentificador())) {
                            recomendacion = new RecomendacionComentarioPorEvalSegu();
                            recomendacion.setFechaCreacion(new Date());
                            recomendacion.setIpCrea(ip);
                            recomendacion.setUsuarioSistemaCrea(usuarioLogin);
                            recomendacion.setEmpresa(empresa);
                        } else {
                            recomendacion = recomendacionComentarioRepository
                                    .findByTokenIdentificadorAndRemovido(recomendacionDTO.getTokenIdentificador(), Boolean.FALSE);
                            if (recomendacion != null) {
                                recomendacion.setFechaEdicion(new Date());
                                recomendacion.setIpEdita(ip);
                                recomendacion.setUsuarioSistemaEdita(usuarioLogin);
                            } else {
                                continue;
                            }
                        }

                        recomendacion.setComentario(recomendacionDTO.getComentario());
                        recomendacion.setFecha(recomendacionDTO.getFecha());
                        recomendacion.setEvaluacionSeguimiento(evaluacionSeguimiento);

                        recomendacion = this.recomendacionComentarioRepository.save(recomendacion);
                        recomendacionDTO.setTokenIdentificador(recomendacion.getTokenIdentificador());
                    }
                }

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito la evaluación y seguimiento educativo laboral de " + nombresCompletos;
                
                // Mensaje para auditoría (información detallada de la evaluación y DNI)
                String mensajeEvaluacion = construirMensajeEvaluacion(evaluacionSeguimiento);
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                String mensajeAuditoria = "Se " + accion + " con éxito " + mensajeEvaluacion + identificacionPersona;

                df.llenarRespuestaExitosa(mensajeUsuario, evaluacionSeguimientoDTO, mensajeAuditoria);

            } finally {
                // Siempre eliminar el token de procesamiento cuando se complete
                solicitudesEnProcesamiento.remove(idSolicitud);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            EvaluacionSeguimientoEducativoLaboralDTO evaluacionSeguimientoDTO = new Gson()
                    .fromJson(bodyString, EvaluacionSeguimientoEducativoLaboralDTO.class);

            EvaluacionSeguimientoEducativoLaboral evaluacionSeguimiento = this.seguimientoRepository
                    .findByTokenIdentificadorAndRemovido(evaluacionSeguimientoDTO.getTokenIdentificador(), false);

            if (evaluacionSeguimiento == null) {
                df.setMensaje("La evaluación y seguimiento no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(evaluacionSeguimiento.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la evaluación y seguimiento educativo laboral de " + nombresCompletos;

            // Mensaje para auditoría (información detallada de la evaluación y DNI)
            String mensajeEvaluacion = construirMensajeEvaluacion(evaluacionSeguimiento);
            String identificacionPersona = obtenerIdentificacionPersona(evaluacionSeguimiento.getFichaIdentificacion());
            String mensajeAuditoria = "Se eliminó con éxito " + mensajeEvaluacion + identificacionPersona;

            Date fecha = new Date();
            evaluacionSeguimiento.setRemovido(true);
            evaluacionSeguimiento.setIpElimina(ip);
            evaluacionSeguimiento.setUsuarioSistemaElimina(usuarioSistemaLogin);
            evaluacionSeguimiento.setFechaEliminacion(fecha);

            this.seguimientoRepository.save(evaluacionSeguimiento);

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarRecomendacionComentario(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            RecomendacionComentarioPorEvalSeguDTO recomendacionDTO = new Gson()
                    .fromJson(bodyString, RecomendacionComentarioPorEvalSeguDTO.class);

            RecomendacionComentarioPorEvalSegu recomendacion = this.recomendacionComentarioRepository
                    .findByTokenIdentificadorAndRemovido(recomendacionDTO.getTokenIdentificador(), false);

            if (recomendacion == null) {
                df.setMensaje("La recomendación/comentario no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            recomendacion.setRemovido(true);
            recomendacion.setIpElimina(ip);
            recomendacion.setUsuarioSistemaElimina(usuarioSistemaLogin);
            recomendacion.setFechaEliminacion(fecha);

            this.recomendacionComentarioRepository.save(recomendacion);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la recomendación/comentario";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se ha eliminado con éxito la recomendación/comentario";

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest,
                                                                 BodyEncriptado bodyEncriptado,
                                                                 MultipartFile[] multipartFiles) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String bodyDesencriptado = df22.getData();
            SeguimientoEducativoDTO seguimientoDTO = new Gson().fromJson(bodyDesencriptado, SeguimientoEducativoDTO.class);

            EvaluacionSeguimientoEducativoLaboral seguimiento = this.seguimientoRepository.findByTokenIdentificadorAndRemovido(
                    seguimientoDTO.getTokenIdentificadorSeguimiento(), false
            );

            if (seguimiento == null) {
                respuesta.setMensaje("No existe el registro solicitado");
                return respuesta;
            }

            EvaluacionSeguimientoEducativoCarpeta registroCarpeta = this.seguimientoCarpetaRepository.findFirstByEvaluacionSeguimientoEducativoLaboralTokenIdentificadorAndRemovido(seguimiento.getTokenIdentificador(), false);

            List<DocumentoDTO> documentoDTOList = seguimientoDTO.getDocumentoDTOList();

            String fallo = "No se pudo guardar los documentos";
            if (registroCarpeta == null) {
                // Crear carpeta si no existe
                FichaIdentificacion fichaIdentificacion = seguimiento.getFichaIdentificacion();

                // Buscar carpeta principal de fichaIdentificación
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                        fichaIdentificacion.getTokenIdentificador(), null, false);

                if (fichaIdentificacionCarpetaPrincipal == null) {
                    respuesta.setMensaje(fallo + ", debido a que no existe la carpeta principal.");
                    return respuesta;
                }

                Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

                // Crear o buscar carpeta para evaluaciones domiciliarias
                String nemonicoEvaluacionDomiciliaria = EtiquetaNemonico.CARPETA_GESTION_ADOLES_SEGUIMIENTO_EDUCATIVO_LABORAL;
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaEvaluacion = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                        fichaIdentificacion.getTokenIdentificador(), nemonicoEvaluacionDomiciliaria, false);

                Carpeta carpetaPadreEvaluaciones;

                if (fichaIdentificacionCarpetaEvaluacion == null) {
                    // Crear carpeta para evaluaciones domiciliarias
                    String nombreCarpetaPrincipal = "Seguimiento educativo laboral";

                    CarpetaDTO carpetaDTO = new CarpetaDTO();
                    carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                    carpetaDTO.setDescripcion("Carpeta de seguimientos educativos laborales");
                    CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                    carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                    carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                    RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                    if (!respuestaCarpeta.isExito()) {
                        respuesta.setMensaje(fallo + ", debido a que no se pudo crear la carpeta principal para evaluaciones domiciliarias.");
                        return respuesta;
                    }

                    Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

                    // Crear relación entre la carpeta y la ficha de identificación
                    fichaIdentificacionCarpetaEvaluacion = new FichaIdentificacionCarpeta();
                    fichaIdentificacionCarpetaEvaluacion.setCarpeta(carpetaGuardada);
                    fichaIdentificacionCarpetaEvaluacion.setFichaIdentificacion(fichaIdentificacion);
                    Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoEvaluacionDomiciliaria, false);
                    fichaIdentificacionCarpetaEvaluacion.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                    fichaIdentificacionCarpetaEvaluacion.setFechaCreacion(new Date());
                    fichaIdentificacionCarpetaEvaluacion.setIpCrea(httpServletRequest.getRemoteAddr());
                    fichaIdentificacionCarpetaEvaluacion.setUsuarioSistemaCrea(usuarioSistema);
                    this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaEvaluacion);

                    carpetaPadreEvaluaciones = carpetaGuardada;
                } else {
                    carpetaPadreEvaluaciones = fichaIdentificacionCarpetaEvaluacion.getCarpeta();
                }

                // Crear carpeta específica para esta evaluación domiciliaria
                String nombreCarpeta = "segui_edu_lab_" + seguimiento.getTokenIdentificador();

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de seguimiento relacionada a: " + seguimiento.getTokenIdentificador());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadreEvaluaciones.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                if (!respuestaCarpeta.isExito()) {
                    respuesta.setMensaje(fallo + ", debido a que no se pudo crear la carpeta específica para el seguimiento.");
                    return respuesta;
                }

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

                // Crear relación entre la carpeta y la evaluación domiciliaria
                registroCarpeta = new EvaluacionSeguimientoEducativoCarpeta();
                registroCarpeta.setCarpeta(carpetaGuardada);
                registroCarpeta.setEvaluacionSeguimientoEducativoLaboral(seguimiento);
                registroCarpeta.setFechaCreacion(new Date());
                registroCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
                registroCarpeta.setUsuarioSistemaCrea(usuarioSistema);
                this.seguimientoCarpetaRepository.save(registroCarpeta);
            }

            Carpeta carpeta = registroCarpeta.getCarpeta();

            String idNodo = carpeta.getIdentificadorAlfresco();

            if (documentoDTOList != null && !documentoDTOList.isEmpty()) {
                for (int i = 0; multipartFiles.length > i; i++) {

                    MultipartFile multipartFile = multipartFiles[i];
                    DocumentoDTO documentoDTO = documentoDTOList.get(i);

                    RespuestaPorDefectoAuditoria<DocumentoDTO> respuestaDocumento = this.documentoService.subirDocumentoAlfresco(httpServletRequest,
                            idNodo, multipartFile, documentoDTO);

                    if (!respuestaDocumento.isExito()) {
                        respuesta.setMensaje(respuestaDocumento.getMensaje());
                        respuesta.setMensajeErrorReal(respuestaDocumento.getMensajeErrorReal());
                        return respuesta;
                    }

                    documentoDTO = respuestaDocumento.getData();
                    Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                            documentoDTO.getTokenIdentificador(), false
                    );

                    EvaluacionSeguimientoEducativoDocumento seguimientoDocumento = new EvaluacionSeguimientoEducativoDocumento();
                    seguimientoDocumento.setDocumento(documento);
                    seguimientoDocumento.setEvaluacionSeguimientoEducativoLaboral(seguimiento);
                    seguimientoDocumento.setCarpeta(carpeta);
                    seguimientoDocumento.setUsuarioSistemaCrea(usuarioSistema);
                    seguimientoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                    this.seguimientoDocumentoRepository.save(seguimientoDocumento);
                }
            }

            respuesta.llenarRespuestaExitosa("Se ha subido con éxito los documentos", true);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

            Page<EvaluacionSeguimientoEducativoDocumento> documentosPage;


            documentosPage = this.seguimientoDocumentoRepository.findByEvaluacionSeguimientoEducativoLaboralTokenIdentificadorAndRemovido(
                    paginacionRequest.getTokenIdentificador(),
                    false,
                    pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (EvaluacionSeguimientoEducativoDocumento segDoc : documentosPage.toList()) {
                Documento documento = segDoc.getDocumento();
                DocumentoDTO documentoDTO = new DocumentoDTO();
                // Asigna campos al DTO según sea necesario
                Catalogo tipoDeDocumentoSistema = documento.getTipoDeDocumentoSistema();
                CatalogoDTO tipoDeDocumentoSistemaDTO = tipoDeDocumentoSistema.convertirADTO();

                documentoDTO.setTipoDocumentoSistema(tipoDeDocumentoSistemaDTO);
                documentoDTO.setTokenIdentificador(documento.getTokenIdentificador());
                documentoDTO.setNombre(documento.getNombreReal());
                documentoDTO.setDescripcion(documento.getDescripcion());
                documentoDTO.setFechaCreacion(documento.getFechaCreacion());
                documentoDTO.setMimeType(documento.getMimeType());
                documentoDTO.setTamanioBytes(documento.getTamanioByteDocumento());
                documentoDTO.setTipoDeDocumentoSistemaOtro(documento.getTipoDeDocumentoSistemaOtro());
                documentoList.add(documentoDTO);
            }

            PaginacionResponse<DocumentoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(documentoList);
            paginacionResponse.setTotalItems(documentosPage.getTotalElements());

            df.llenarRespuestaExitosa(
                    "Se han encontrado " + documentoList.size() + " documentos, de un total de " + documentosPage.getTotalElements(),
                    paginacionResponse
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Método auxiliar para construir el mensaje con información de la evaluación y seguimiento
     */
    private String construirMensajeEvaluacion(EvaluacionSeguimientoEducativoLaboral evaluacion) {
        StringBuilder mensaje = new StringBuilder();
        
        // Agregar base de la evaluación
        mensaje.append("la evaluación y seguimiento educativo laboral");
        
        // Agregar tipo de evaluación si existe
        if (evaluacion.getTipoEvaluacionSeguimiento() != null && evaluacion.getTipoEvaluacionSeguimiento().getNombre() != null) {
            mensaje.append(" tipo: ").append(evaluacion.getTipoEvaluacionSeguimiento().getNombre()).append(",");
        }
        
        // Agregar resultado de seguimiento si existe
        if (evaluacion.getResultadoSeguimiento() != null && !evaluacion.getResultadoSeguimiento().trim().isEmpty()) {
            mensaje.append(" resultado: ").append(evaluacion.getResultadoSeguimiento()).append(",");
        }
        
        return mensaje.toString();
    }

    /**
     * Método auxiliar para obtener nombres completos de una ficha
     */
    private String obtenerNombresCompletos(FichaIdentificacion fichaIdentificacion) {
        if (fichaIdentificacion == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (fichaIdentificacion.getNombres() != null && !fichaIdentificacion.getNombres().trim().isEmpty()) {
            nombreCompleto.append(fichaIdentificacion.getNombres());
        }
        if (fichaIdentificacion.getApellidoPaterno() != null && !fichaIdentificacion.getApellidoPaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(fichaIdentificacion.getApellidoPaterno());
        }
        if (fichaIdentificacion.getApellidoMaterno() != null && !fichaIdentificacion.getApellidoMaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(fichaIdentificacion.getApellidoMaterno());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una persona desde su ficha
     */
    private String obtenerIdentificacionPersona(FichaIdentificacion fichaIdentificacion) {
        if (fichaIdentificacion == null) {
            return "";
        }

        String identificacion = "";
        
        if (fichaIdentificacion.getDni() != null && !fichaIdentificacion.getDni().trim().isEmpty()) {
            identificacion = " para la persona con DNI: " + fichaIdentificacion.getDni();
        }
        else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = " para la persona con DNI: " + fichaIdentificacion.getNumeroIdentificacion();
        }
        else if (fichaIdentificacion.getNombres() != null || fichaIdentificacion.getApellidoPaterno() != null) {
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = " para la persona: " + nombresCompletos;
            }
        }

        return identificacion;
    }
}
