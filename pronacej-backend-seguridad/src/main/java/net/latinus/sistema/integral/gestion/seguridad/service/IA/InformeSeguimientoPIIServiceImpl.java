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
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.InformeSeguimientoPII;
import net.latinus.sistema.integral.gestion.seguridad.entities.InstrumentoEvaluacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InstrumentoEvaluacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeSeguimientoPIIDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InformeSeguimientoPIIRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InformeTecnicoSustentatorioRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InstrumentoEvaluacionRepository;

@Service
@Transactional
@AllArgsConstructor
public class InformeSeguimientoPIIServiceImpl implements InformeSeguimientoPIIService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private InformeTecnicoSustentatorioRepository informeTecnicoRepository;
    private InformeSeguimientoPIIRepository informeSeguimientoRepository;
    private InstrumentoEvaluacionRepository instrumentoEvaluacionRepository;
    // Variable para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeSeguimientoPIIDTO>> obtenerInformesSeguimientoPaginado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeSeguimientoPIIDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar la solicitud
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();

            // Configurar la paginación con ordenamiento dinámico
            Pageable pageable;
            if (paginacionRequest.getSort() != null && !paginacionRequest.getSort().isEmpty() 
                    && paginacionRequest.getDirection() != null && !paginacionRequest.getDirection().isEmpty()) {

                Sort.Direction direction = paginacionRequest.getDirection().equalsIgnoreCase("asc") 
                        ? Sort.Direction.ASC : Sort.Direction.DESC;

                pageable = PageRequest.of(
                        paginacionRequest.getPage(),
                        paginacionRequest.getSize(),
                        Sort.by(direction, paginacionRequest.getSort())
                );
            } else {
                // Ordenamiento por defecto
                pageable = PageRequest.of(
                        paginacionRequest.getPage(),
                        paginacionRequest.getSize(),
                        Sort.by("idInformeSeguimientoPII").descending()
                );
            }

            // Usar el filtro si existe
            String filtro = paginacionRequest.getFilter() != null ? paginacionRequest.getFilter() : "";

            // Usar el método de búsqueda con filtro
            Page<InformeSeguimientoPII> informePage = this.informeSeguimientoRepository.buscarPorFiltro(
                    paginacionRequest.getTokenIdentificador(), 
                    empresa.getIdEmpresa(), 
                    filtro, 
                    pageable);

            // Preparar la respuesta paginada
            PaginacionResponse<InformeSeguimientoPIIDTO> paginacionResponse = new PaginacionResponse<>();
            List<InformeSeguimientoPIIDTO> informeDTOList = new ArrayList<>();

            // Convertir entidades a DTOs
            for (InformeSeguimientoPII informe : informePage.getContent()) {
                InformeSeguimientoPIIDTO informeDTO = new InformeSeguimientoPIIDTO();
                informeDTO.setTokenIdentificador(informe.getTokenIdentificador());
                informeDTO.setTokenIdentificadorEmpresa(informe.getEmpresa().getTokenIdentificador());
                informeDTO.setFechaCreacion(informe.getFechaCreacion());

                informeDTO.setMotivoIngreso(informe.getMotivoIngreso());
                informeDTO.setAntecedentesOrganicidad(informe.getAntecedentesOrganicidad());
                informeDTO.setTecnicasUtilizadas(informe.getTecnicasUtilizadas());
                informeDTO.setObservacionConductual(informe.getObservacionConductual());
                informeDTO.setEvaluacionPlanPsicologica(informe.getEvaluacionPlanPsicologica());
                informeDTO.setEvaluacionPlanSocial(informe.getEvaluacionPlanSocial());
                informeDTO.setEvaluacionPlanConductual(informe.getEvaluacionPlanConductual());
                informeDTO.setEvaluacionPlanFamiliar(informe.getEvaluacionPlanFamiliar());
                informeDTO.setEvaluacionPlanEducativa(informe.getEvaluacionPlanEducativa());
                informeDTO.setEvaluacionPlanLaboral(informe.getEvaluacionPlanLaboral());
                if (informe.getNivelRiesgo() != null) {
                    informeDTO.setTokenIdentificadorNivelRiesgo(informe.getNivelRiesgo().getTokenIdentificador());
                }
                informeDTO.setConclusiones(informe.getConclusiones());
                informeDTO.setRecomendaciones(informe.getRecomendaciones());

                // Cargar instrumentos aplicados para cada informe
                List<InstrumentoEvaluacion> instrumentos = instrumentoEvaluacionRepository
                    .findByInformeSeguimientoPIITokenIdentificadorAndRemovido(
                        informe.getTokenIdentificador(), 
                        Boolean.FALSE
                    );

                List<InstrumentoEvaluacionDTO> instrumentosDTOList = new ArrayList<>();
                for (InstrumentoEvaluacion instrumento : instrumentos) {
                    InstrumentoEvaluacionDTO instrumentoDTO = new InstrumentoEvaluacionDTO();
                    instrumentoDTO.setTokenIdentificador(instrumento.getTokenIdentificador());
                    instrumentoDTO.setTokenIdentificadorInformeSeguimiento(informe.getTokenIdentificador());

                    if (instrumento.getTipoInstrumento() != null) {
                        instrumentoDTO.setTokenIdentificadorTipoInstrumento(
                            instrumento.getTipoInstrumento().getTokenIdentificador()
                        );
                    }

                    instrumentosDTOList.add(instrumentoDTO);
                }

                if (informe.getFichaIdentificacion() != null) {
                    informeDTO.setTokenIdentificadorFichaIdentificacion(informe.getFichaIdentificacion().getTokenIdentificador());
                }

                informeDTO.setListaInstrumentosAplicados(instrumentosDTOList);
                informeDTOList.add(informeDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            informeDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            // Configurar respuesta paginada
            paginacionResponse.setData(informeDTOList);
            paginacionResponse.setTotalItems(informePage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + informePage.getTotalElements() + " informes de seguimiento PII";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + informePage.getTotalElements() + " registros de informes de seguimiento";

            // Devolver resultado exitoso
            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformeSeguimientoPIIDTO> crearInformeSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InformeSeguimientoPIIDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            Empresa empresa = df2.getData().getEmpresa();

            InformeSeguimientoPIIDTO informeDTO = new Gson().fromJson(bodyString, InformeSeguimientoPIIDTO.class);

            informeDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = informeDTO.getTokenIdentificadorFichaIdentificacion() + "-informeSeguimientoPII";

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
                
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository
                    .findByTokenIdentificadorAndRemovido(
                        informeDTO.getTokenIdentificadorFichaIdentificacion(), 
                        Boolean.FALSE);

                InformeSeguimientoPII informeSeguimiento;
                boolean esEdicion = false;

                if (Boolean.TRUE.equals(informeDTO.getEsEdicion())) {
                    informeSeguimiento = informeSeguimientoRepository.findByTokenIdentificadorAndRemovido(informeDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (informeSeguimiento == null) {
                        df.setMensaje("El informe de seguimiento a editar no existe o ya fue eliminado anteriormente");
                        return df;
                    }
                    informeSeguimiento.setFechaEdicion(new Date());
                    informeSeguimiento.setIpEdita(ip);
                    informeSeguimiento.setUsuarioSistemaEdita(usuarioLogin);
                    esEdicion = true;
                } else {
                    informeSeguimiento = new InformeSeguimientoPII();
                    informeSeguimiento.setFechaCreacion(new Date());
                    informeSeguimiento.setIpCrea(ip);
                    informeSeguimiento.setUsuarioSistemaCrea(usuarioLogin);
                    informeSeguimiento.setEmpresa(empresa);
                    informeSeguimiento.setFichaIdentificacion(fichaIdentificacion);
                }

                // Configurar campos del informe de seguimiento
                informeSeguimiento.setMotivoIngreso(informeDTO.getMotivoIngreso());
                informeSeguimiento.setAntecedentesOrganicidad(informeDTO.getAntecedentesOrganicidad());
                informeSeguimiento.setTecnicasUtilizadas(informeDTO.getTecnicasUtilizadas());
                informeSeguimiento.setObservacionConductual(informeDTO.getObservacionConductual());
                informeSeguimiento.setEvaluacionPlanPsicologica(informeDTO.getEvaluacionPlanPsicologica());
                informeSeguimiento.setEvaluacionPlanSocial(informeDTO.getEvaluacionPlanSocial());
                informeSeguimiento.setEvaluacionPlanConductual(informeDTO.getEvaluacionPlanConductual());
                informeSeguimiento.setEvaluacionPlanFamiliar(informeDTO.getEvaluacionPlanFamiliar());
                informeSeguimiento.setEvaluacionPlanEducativa(informeDTO.getEvaluacionPlanEducativa());
                informeSeguimiento.setEvaluacionPlanLaboral(informeDTO.getEvaluacionPlanLaboral());
                if (informeDTO.getTokenIdentificadorNivelRiesgo() != null && !informeDTO.getTokenIdentificadorNivelRiesgo().equals("0")) {
                    Catalogo nivelRiesgo = catalogoRepository.findByTokenIdentificadorAndRemovido(
                        informeDTO.getTokenIdentificadorNivelRiesgo(), Boolean.FALSE);
                    informeSeguimiento.setNivelRiesgo(nivelRiesgo);
                }
                informeSeguimiento.setConclusiones(informeDTO.getConclusiones());
                informeSeguimiento.setRecomendaciones(informeDTO.getRecomendaciones());

                informeSeguimiento = this.informeSeguimientoRepository.save(informeSeguimiento);
                informeDTO.setTokenIdentificador(informeSeguimiento.getTokenIdentificador());

                // Guardar instrumentos de evaluación
                if (informeDTO.getListaInstrumentosAplicados() != null) {
                    for (InstrumentoEvaluacionDTO instrumentoDTO : informeDTO.getListaInstrumentosAplicados()) {
                        InstrumentoEvaluacion instrumentoEvaluacion;

                        if (instrumentoDTO.getTokenIdentificador().equals("0")) {
                            instrumentoEvaluacion = new InstrumentoEvaluacion();
                            instrumentoEvaluacion.setFechaCreacion(new Date());
                            instrumentoEvaluacion.setIpCrea(ip);
                            instrumentoEvaluacion.setUsuarioSistemaCrea(usuarioLogin);
                            instrumentoEvaluacion.setEmpresa(empresa);
                        } else {
                            instrumentoEvaluacion = instrumentoEvaluacionRepository
                                .findByTokenIdentificadorAndRemovido(instrumentoDTO.getTokenIdentificador(), Boolean.FALSE);
                            instrumentoEvaluacion.setFechaEdicion(new Date());
                            instrumentoEvaluacion.setIpEdita(ip);
                            instrumentoEvaluacion.setUsuarioSistemaEdita(usuarioLogin);
                        }

                        Catalogo tipoInstrumento = catalogoRepository
                            .findByTokenIdentificadorAndRemovido(instrumentoDTO.getTokenIdentificadorTipoInstrumento(), Boolean.FALSE);
                        instrumentoEvaluacion.setTipoInstrumento(tipoInstrumento);
                        instrumentoEvaluacion.setInformeSeguimientoPII(informeSeguimiento);

                        instrumentoEvaluacion = this.instrumentoEvaluacionRepository.save(instrumentoEvaluacion);
                        instrumentoDTO.setTokenIdentificador(instrumentoEvaluacion.getTokenIdentificador());
                    }
                }

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito el informe de seguimiento PII de " + nombresCompletos;
                
                // Mensaje para auditoría (información detallada del informe y DNI)
                String mensajeInforme = construirMensajeInforme(informeSeguimiento);
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                String mensajeAuditoria = "Se " + accion + " con éxito " + mensajeInforme + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, informeDTO, mensajeAuditoria);

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
    public RespuestaPorDefectoAuditoria<Boolean> eliminarInformeSeguimiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            InformeSeguimientoPIIDTO informeDTO = new Gson().fromJson(bodyString, InformeSeguimientoPIIDTO.class);

            InformeSeguimientoPII informeSeguimiento = this.informeSeguimientoRepository
                .findByTokenIdentificadorAndRemovido(informeDTO.getTokenIdentificador(), false);

            if (informeSeguimiento == null) {
                df.setMensaje("El informe de seguimiento no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(informeSeguimiento.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito el informe de seguimiento PII de " + nombresCompletos;

            // Mensaje para auditoría (información detallada del informe y DNI)
            String mensajeInforme = construirMensajeInforme(informeSeguimiento);
            String identificacionPersona = obtenerIdentificacionPersona(informeSeguimiento.getFichaIdentificacion());
            String mensajeAuditoria = "Se eliminó con éxito " + mensajeInforme + identificacionPersona;

            Date fecha = new Date();
            informeSeguimiento.setRemovido(true);
            informeSeguimiento.setIpElimina(ip);
            informeSeguimiento.setUsuarioSistemaElimina(usuarioSistemaLogin);
            informeSeguimiento.setFechaEliminacion(fecha);

            this.informeSeguimientoRepository.save(informeSeguimiento);

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Método auxiliar para construir el mensaje con información del informe de seguimiento
     */
    private String construirMensajeInforme(InformeSeguimientoPII informe) {
        StringBuilder mensaje = new StringBuilder();
        
        // Agregar base del informe
        mensaje.append("el informe de seguimiento PII");
        
        // Agregar motivo de ingreso
        if (informe.getMotivoIngreso() != null && !informe.getMotivoIngreso().trim().isEmpty()) {
            mensaje.append(" motivo de ingreso: ").append(informe.getMotivoIngreso()).append(",");
        }
        
        // Agregar técnicas utilizadas si existe
        if (informe.getTecnicasUtilizadas() != null && !informe.getTecnicasUtilizadas().trim().isEmpty()) {
            mensaje.append(" técnicas utilizadas: ").append(informe.getTecnicasUtilizadas()).append(",");
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