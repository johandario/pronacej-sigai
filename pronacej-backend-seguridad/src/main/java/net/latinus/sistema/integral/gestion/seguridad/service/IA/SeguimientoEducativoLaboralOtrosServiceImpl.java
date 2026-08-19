package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralOtros;
import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralOtrosCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralOtrosDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SeguimientoEducativoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SeguimientoEducativoLaboralOtrosDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SeguimientoEducativoLaboralOtrosCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SeguimientoEducativoLaboralOtrosDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SeguimientoEducativoLaboralOtrosRepository;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@AllArgsConstructor
public class SeguimientoEducativoLaboralOtrosServiceImpl implements SeguimientoEducativoLaboralOtrosService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private SeguimientoEducativoLaboralOtrosRepository seguimientoEducativoLaboralOtrosRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private SeguimientoEducativoLaboralOtrosCarpetaRepository seguimientoCarpetaRepository;
    private SeguimientoEducativoLaboralOtrosDocumentoRepository seguimientoDocumentoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JerarquiaRepository jerarquiaRepository;
    // Mapa para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    private PermisoRolUsuarioService permisoRolUsuarioService;

    /**
     * Método auxiliar para obtener nombres completos desde FichaIdentificacion
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
     * Método auxiliar para obtener nombres completos desde UsuarioSistema
     */
    private String obtenerNombresCompletosUsuario(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        
        // Intentar obtener nombres y apellidos
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una persona desde su ficha
     */
    private String obtenerIdentificacionPersona(FichaIdentificacion fichaIdentificacion) {
        if (fichaIdentificacion == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        if (fichaIdentificacion.getDni() != null && !fichaIdentificacion.getDni().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getDni();
        }
        else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getNumeroIdentificacion();
        }
        else {
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }

    /**
     * Método auxiliar para formatear fechas
     */
    private String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "fecha no especificada";
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(fecha);
    }

    /**
     * Formatea una fecha para mostrarla en español en los mensajes de auditoría
     * @param fecha La fecha a formatear
     * @return String con formato "martes, 2 de enero del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", new Locale("es", "ES"));
        return formatter.format(fecha);
    }

    /**
     * Método auxiliar para construir el mensaje de auditoría con información del seguimiento
     */
    private String construirMensajeAuditoria(SeguimientoEducativoLaboralOtros seguimiento) {
        StringBuilder mensaje = new StringBuilder();
        
        // Agregar base del seguimiento
        mensaje.append("el seguimiento educativo");
        
        // Agregar tipo de seguimiento específico
        if (seguimiento.getTipoSeguimiento() != null && 
            seguimiento.getTipoSeguimiento().getDescripcion() != null) {
            mensaje.append(" de tipo: ").append(seguimiento.getTipoSeguimiento().getDescripcion());
        }
        
        // Agregar fecha de seguimiento
        if (seguimiento.getFechaSeguimiento() != null) {
            mensaje.append(" del ").append(formatearFechaEspanol(seguimiento.getFechaSeguimiento()));
        }
        
        // Agregar institución visitada
        if (seguimiento.getInstitucionVisitada() != null && !seguimiento.getInstitucionVisitada().trim().isEmpty()) {
            mensaje.append(" en ").append(seguimiento.getInstitucionVisitada());
        }
        
        return mensaje.toString();
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO>> obtenerSeguimientosPaginado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
                    Sort.by("idSeguimientoEducativoLaboral").descending()
            );

            Page<SeguimientoEducativoLaboralOtros> seguimientosPage = this.seguimientoEducativoLaboralOtrosRepository.findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                    paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<SeguimientoEducativoLaboralOtrosDTO> paginacionResponse = new PaginacionResponse<>();
            List<SeguimientoEducativoLaboralOtrosDTO> seguimientosDTOList = new ArrayList<>();

            // Obtener la ficha de identificación para el mensaje
            FichaIdentificacion fichaIdentificacion = null;
            if (!seguimientosPage.isEmpty()) {
                fichaIdentificacion = seguimientosPage.getContent().get(0).getFichaIdentificacion();
            } else {
                // Si no hay seguimientos, buscar la ficha directamente
                fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                        paginacionRequest.getTokenIdentificador(), Boolean.FALSE);
            }
            
            for (SeguimientoEducativoLaboralOtros seguimiento : seguimientosPage.toList()) {
                SeguimientoEducativoLaboralOtrosDTO seguimientoDTO = new SeguimientoEducativoLaboralOtrosDTO();
                seguimientoDTO.setTokenIdentificador(seguimiento.getTokenIdentificador());
                seguimientoDTO.setTokenEvaluacionSeguimiento(seguimiento.getFichaIdentificacion().getTokenIdentificador());
                seguimientoDTO.setInstitucionVisitada(seguimiento.getInstitucionVisitada());
                seguimientoDTO.setPersonaEntrevistada(seguimiento.getPersonaEntrevistada());
                seguimientoDTO.setDireccion(seguimiento.getDireccion());
                seguimientoDTO.setFechaSeguimiento(seguimiento.getFechaSeguimiento());
                seguimientoDTO.setMedioVerificacion(seguimiento.getMedioVerificacion());
                seguimientoDTO.setResultadoSeguimiento(seguimiento.getResultadoSeguimiento());
                seguimientoDTO.setSugerenciasRecomendaciones(seguimiento.getSugerenciasRecomendaciones());
                
                // CAMPOS AGREGADOS PARA MOSTRAR EN EL LISTADO
                seguimientoDTO.setFechaCreacion(seguimiento.getFechaCreacion());
                seguimientoDTO.setNombreCompletoUsuarioCreacion(obtenerNombresCompletosUsuario(seguimiento.getUsuarioSistemaCrea()));
                
                if (seguimiento.getTipoSeguimiento() != null) {
                    seguimientoDTO.setTokenIdentificadorTipoSeguimientoSocial(seguimiento.getTipoSeguimiento().getTokenIdentificador());
                }

                if (seguimiento.getFichaIdentificacion() != null) {
                    seguimientoDTO.setTokenFichaIdentificacion(seguimiento.getFichaIdentificacion().getTokenIdentificador());
                }

                seguimientosDTOList.add(seguimientoDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            seguimientosDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(seguimientosDTOList);
            paginacionResponse.setTotalItems(seguimientosPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + seguimientosPage.getTotalElements() + " seguimientos educativos/laborales";

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
            String mensajeAuditoria = "Se han encontrado un total de " + seguimientosPage.getTotalElements() + " registros educativos, laborales y de ocio de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<SeguimientoEducativoLaboralOtrosDTO> crearSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<SeguimientoEducativoLaboralOtrosDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            SeguimientoEducativoLaboralOtrosDTO seguimientoDTO = new Gson().fromJson(bodyString, SeguimientoEducativoLaboralOtrosDTO.class);

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = seguimientoDTO.getTokenEvaluacionSeguimiento() + "-seguimientoEducativoLaboralOtros";

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
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(seguimientoDTO.getTokenEvaluacionSeguimiento(), Boolean.FALSE);

                SeguimientoEducativoLaboralOtros seguimiento;
                boolean esEdicion = false;

                if (seguimientoDTO.getTokenIdentificador() != null && !seguimientoDTO.getTokenIdentificador().equals("0")) {
                    // Es una edición
                    seguimiento = seguimientoEducativoLaboralOtrosRepository.findByTokenIdentificadorAndRemovido(
                            seguimientoDTO.getTokenIdentificador(), Boolean.FALSE);
                    seguimiento.setFechaEdicion(new Date());
                    seguimiento.setIpEdita(ip);
                    seguimiento.setUsuarioSistemaEdita(usuarioLogin);
                    esEdicion = true;
                } else {
                    // Es una creación
                    seguimiento = new SeguimientoEducativoLaboralOtros();
                    seguimiento.setFechaCreacion(new Date());
                    seguimiento.setIpCrea(ip);
                    seguimiento.setUsuarioSistemaCrea(usuarioLogin);
                    seguimiento.setEmpresa(empresa);
                }

                // Mapear datos del DTO a la entidad
                seguimiento.setFichaIdentificacion(fichaIdentificacion);
                seguimiento.setInstitucionVisitada(seguimientoDTO.getInstitucionVisitada());
                seguimiento.setPersonaEntrevistada(seguimientoDTO.getPersonaEntrevistada());
                seguimiento.setDireccion(seguimientoDTO.getDireccion());
                seguimiento.setFechaSeguimiento(seguimientoDTO.getFechaSeguimiento());
                seguimiento.setMedioVerificacion(seguimientoDTO.getMedioVerificacion());
                seguimiento.setResultadoSeguimiento(seguimientoDTO.getResultadoSeguimiento());
                seguimiento.setSugerenciasRecomendaciones(seguimientoDTO.getSugerenciasRecomendaciones());

                // Establecer relaciones con catálogos y jerarquías
                if (seguimientoDTO.getTokenIdentificadorTipoSeguimientoSocial() != null) {
                    Catalogo tipoSeguimiento = catalogoRepository.findByTokenIdentificadorAndRemovido(
                            seguimientoDTO.getTokenIdentificadorTipoSeguimientoSocial(), Boolean.FALSE);
                    seguimiento.setTipoSeguimiento(tipoSeguimiento);
                }

                if (seguimientoDTO.getPrograma() != null && seguimientoDTO.getPrograma().getTokenIdentificador() != null) {
                    Jerarquia programa = jerarquiaRepository.findByTokenIdentificadorAndRemovido(
                            seguimientoDTO.getPrograma().getTokenIdentificador(), Boolean.FALSE);
                    seguimiento.setPrograma(programa);
                }

                if (seguimientoDTO.getAmbiente() != null && seguimientoDTO.getAmbiente().getTokenIdentificador() != null) {
                    Jerarquia ambiente = jerarquiaRepository.findByTokenIdentificadorAndRemovido(
                            seguimientoDTO.getAmbiente().getTokenIdentificador(), Boolean.FALSE);
                    seguimiento.setAmbiente(ambiente);
                }

                seguimiento = this.seguimientoEducativoLaboralOtrosRepository.save(seguimiento);
                seguimientoDTO.setTokenIdentificador(seguimiento.getTokenIdentificador());

                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                String mensajeAuditoriaDetalle = construirMensajeAuditoria(seguimiento);
                String accion = esEdicion ? "editó" : "creó";

                // Mensaje para el usuario
                String mensajeUsuario;
                if (!"N/A".equals(nombresCompletos)) {
                    mensajeUsuario = "Se " + accion + " con éxito el seguimiento educativo/laboral de " + nombresCompletos;
                } else {
                    mensajeUsuario = "Se " + accion + " con éxito el seguimiento educativo/laboral";
                }

                // Mensaje para auditoría
                String mensajeAuditoria = "Se " + accion + " con éxito " + mensajeAuditoriaDetalle + " de la persona con identificación: " + identificacionPersona;

                df.llenarRespuestaExitosa(mensajeUsuario, seguimientoDTO, mensajeAuditoria);

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
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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

            SeguimientoEducativoLaboralOtrosDTO seguimientoDTO = new Gson().fromJson(bodyString, SeguimientoEducativoLaboralOtrosDTO.class);

            SeguimientoEducativoLaboralOtros seguimiento = this.seguimientoEducativoLaboralOtrosRepository.findByTokenIdentificadorAndRemovido(
                    seguimientoDTO.getTokenIdentificador(), false
            );

            if (seguimiento == null) {
                df.setMensaje("El seguimiento educativo/laboral/otros no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            String nombresCompletos = obtenerNombresCompletos(seguimiento.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(seguimiento.getFichaIdentificacion());
            String mensajeAuditoriaDetalle = construirMensajeAuditoria(seguimiento);

            Date fecha = new Date();
            seguimiento.setRemovido(true);
            seguimiento.setIpElimina(ip);
            seguimiento.setUsuarioSistemaElimina(usuarioSistemaLogin);
            seguimiento.setFechaEliminacion(fecha);

            this.seguimientoEducativoLaboralOtrosRepository.save(seguimiento);

            // Mensaje para el usuario
            String mensajeUsuario;
            if (!"N/A".equals(nombresCompletos)) {
                mensajeUsuario = "Se eliminó con éxito el seguimiento educativo/laboral de " + nombresCompletos;
            } else {
                mensajeUsuario = "Se eliminó con éxito el seguimiento educativo/laboral";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito " + mensajeAuditoriaDetalle + " de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Sube documentos asociados a un seguimiento educativo laboral otros
     * Crea automáticamente las carpetas necesarias si no existen
     * @param httpServletRequest Request HTTP con información de la sesión
     * @param bodyEncriptado Datos encriptados del seguimiento y documentos
     * @param multipartFiles Archivos a subir
     * @return Respuesta con resultado de la operación
     */
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

        // Buscar el seguimiento educativo laboral otros
        SeguimientoEducativoLaboralOtros seguimiento = this.seguimientoEducativoLaboralOtrosRepository.findByTokenIdentificadorAndRemovido(
                seguimientoDTO.getTokenIdentificadorSeguimiento(), false
        );

        if (seguimiento == null) {
            respuesta.setMensaje("No existe el registro solicitado");
            return respuesta;
        }

        // Buscar carpeta existente para este seguimiento
        SeguimientoEducativoLaboralOtrosCarpeta registroCarpeta = this.seguimientoCarpetaRepository.findFirstBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(
                seguimiento.getTokenIdentificador(), false);

        List<DocumentoDTO> documentoDTOList = seguimientoDTO.getDocumentoDTOList();

        String nombresCompletos = obtenerNombresCompletos(seguimiento.getFichaIdentificacion());
        String identificacionPersona = obtenerIdentificacionPersona(seguimiento.getFichaIdentificacion());
        
        String falloUsuario = !"N/A".equals(nombresCompletos) ? 
            "No se pudieron subir los documentos al seguimiento educativo/laboral de " + nombresCompletos :
            "No se pudieron subir los documentos al seguimiento educativo/laboral";
        
        if (registroCarpeta == null) {
            // Crear estructura de carpetas si no existe
            FichaIdentificacion fichaIdentificacion = seguimiento.getFichaIdentificacion();

            // Buscar carpeta principal de fichaIdentificación
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                    fichaIdentificacion.getTokenIdentificador(), null, false);

            if (fichaIdentificacionCarpetaPrincipal == null) {
                respuesta.setMensaje(falloUsuario + ", debido a que no existe la carpeta principal.");
                return respuesta;
            }

            Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

            // Crear o buscar carpeta para seguimientos educativos laborales otros
            String nemonicoSeguimientoEducativo = EtiquetaNemonico.CARPETA_GESTION_ADOLES_SEGUIMIENTO_EDUCATIVO_LABORAL_OTROS;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaSeguimiento = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                    fichaIdentificacion.getTokenIdentificador(), nemonicoSeguimientoEducativo, false);

            Carpeta carpetaPadreSeguimientos;

            if (fichaIdentificacionCarpetaSeguimiento == null) {
                // Crear carpeta para seguimientos educativos laborales otros
                String nombreCarpetaPrincipal = "Seguimiento educativo laboral otros";

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                carpetaDTO.setDescripcion("Carpeta de seguimientos educativos laborales otros");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                if (!respuestaCarpeta.isExito()) {
                    respuesta.setMensaje(falloUsuario + ", debido a que no se pudo crear la carpeta principal para seguimientos educativos laborales otros.");
                    return respuesta;
                }

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

                // Crear relación entre la carpeta y la ficha de identificación
                fichaIdentificacionCarpetaSeguimiento = new FichaIdentificacionCarpeta();
                fichaIdentificacionCarpetaSeguimiento.setCarpeta(carpetaGuardada);
                fichaIdentificacionCarpetaSeguimiento.setFichaIdentificacion(fichaIdentificacion);
                Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoSeguimientoEducativo, false);
                fichaIdentificacionCarpetaSeguimiento.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                fichaIdentificacionCarpetaSeguimiento.setFechaCreacion(new Date());
                fichaIdentificacionCarpetaSeguimiento.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaIdentificacionCarpetaSeguimiento.setUsuarioSistemaCrea(usuarioSistema);
                this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaSeguimiento);

                carpetaPadreSeguimientos = carpetaGuardada;
            } else {
                carpetaPadreSeguimientos = fichaIdentificacionCarpetaSeguimiento.getCarpeta();
            }

            // Crear carpeta específica para este seguimiento educativo laboral otros
            String nombreCarpeta = "segui_edu_lab_otros_" + seguimiento.getTokenIdentificador();

            CarpetaDTO carpetaDTO = new CarpetaDTO();
            carpetaDTO.setNombreCliente(nombreCarpeta);
            carpetaDTO.setDescripcion("Carpeta de seguimiento educativo laboral otros relacionada a: " + seguimiento.getTokenIdentificador());
            CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
            carpetaPadreDTO.setTokenIdentificador(carpetaPadreSeguimientos.getTokenIdentificador());
            carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

            RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

            if (!respuestaCarpeta.isExito()) {
                respuesta.setMensaje(falloUsuario + ", debido a que no se pudo crear la carpeta específica para el seguimiento.");
                return respuesta;
            }

            Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

            // Crear relación entre la carpeta y el seguimiento educativo laboral otros
            registroCarpeta = new SeguimientoEducativoLaboralOtrosCarpeta();
            registroCarpeta.setCarpeta(carpetaGuardada);
            registroCarpeta.setSeguimientoEducativoLaboralOtros(seguimiento);
            registroCarpeta.setFechaCreacion(new Date());
            registroCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
            registroCarpeta.setUsuarioSistemaCrea(usuarioSistema);
            this.seguimientoCarpetaRepository.save(registroCarpeta);
        }

        Carpeta carpeta = registroCarpeta.getCarpeta();
        String idNodo = carpeta.getIdentificadorAlfresco();

        // Subir documentos a Alfresco y crear relaciones
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

                // Crear relación documento-seguimiento
                SeguimientoEducativoLaboralOtrosDocumento seguimientoDocumento = new SeguimientoEducativoLaboralOtrosDocumento();
                seguimientoDocumento.setDocumento(documento);
                seguimientoDocumento.setSeguimientoEducativoLaboralOtros(seguimiento);
                seguimientoDocumento.setCarpeta(carpeta);
                seguimientoDocumento.setUsuarioSistemaCrea(usuarioSistema);
                seguimientoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                this.seguimientoDocumentoRepository.save(seguimientoDocumento);
            }
        }

        // Mensaje para el usuario
        String mensajeUsuario = !"N/A".equals(nombresCompletos) ? 
            "Se subieron con éxito los documentos al seguimiento educativo/laboral de " + nombresCompletos :
            "Se subieron con éxito los documentos al seguimiento educativo/laboral";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se subieron con éxito los documentos al seguimiento educativo/laboral de la persona con identificación: " + identificacionPersona;

        respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

    } catch (Exception ex) {
        respuesta.llenarConDatosDeException(ex);
    }

    return respuesta;
}
    /**
     * Obtiene todos los documentos asociados a un seguimiento educativo laboral otros
     * @param httpServletRequest Request HTTP con información de la sesión
     * @param bodyEncriptado Datos encriptados con token del seguimiento
     * @return Respuesta con lista paginada de documentos
     */
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

            // Buscar documentos asociados al seguimiento educativo laboral otros
            Page<SeguimientoEducativoLaboralOtrosDocumento> documentosPage = 
                    this.seguimientoDocumentoRepository.findBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(
                            paginacionRequest.getTokenIdentificador(),
                            false,
                            pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (SeguimientoEducativoLaboralOtrosDocumento segDoc : documentosPage.toList()) {
                Documento documento = segDoc.getDocumento();
                DocumentoDTO documentoDTO = new DocumentoDTO();

                // Mapear información del tipo de documento
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

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + documentosPage.getTotalElements() + " documentos del seguimiento educativo/laboral";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + documentosPage.getTotalElements() + " documentos asociados al seguimiento educativo/laboral";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

}
