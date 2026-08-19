package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.CometimientoInfraccion;
import net.latinus.sistema.integral.gestion.seguridad.entities.InformeVisitas;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;
import net.latinus.sistema.integral.gestion.seguridad.entities.SuspensionVisitas;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.CometimientoInfraccionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InformeVisitasRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PersonaRelacionadaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SuspensionVisitasRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@AllArgsConstructor
public class InformeVisitasServiceImpl implements InformeVisitasService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private InformeVisitasRepository informeVisitasRepository;
    private SuspensionVisitasRepository suspensionVisitasRepository;
    private PersonaRelacionadaRepository personaRelacionadaRepository;
    private CometimientoInfraccionRepository cometimientoInfraccionRepository;
    // Variables para protección contra duplicados diferenciadas
    private Map<String, Long> solicitudesCreacionInformesEnProcesamiento = new ConcurrentHashMap<>();
    private Map<String, Long> solicitudesEdicionInformesEnProcesamiento = new ConcurrentHashMap<>();
    private Map<String, Long> solicitudesCreacionSuspensionesEnProcesamiento = new ConcurrentHashMap<>();
    private Map<String, Long> solicitudesEdicionSuspensionesEnProcesamiento = new ConcurrentHashMap<>();

    /**
     * Formatea una fecha para mostrarla en español en los mensajes
     * @param fecha La fecha a formatear
     * @return String con formato "martes, 2 de enero del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", new Locale("es", "ES"));
        return formatter.format(fecha);
    }

    /**
     * Método auxiliar para obtener nombres completos desde PersonaRelacionada
     */
    private String obtenerNombresCompletos(PersonaRelacionada personaRelacionada) {
        if (personaRelacionada == null) {
            return "N/A";
        }

        // Usar el campo nombresCompletos si está disponible
        if (personaRelacionada.getNombresCompletos() != null && 
            !personaRelacionada.getNombresCompletos().trim().isEmpty()) {
            return personaRelacionada.getNombresCompletos();
        }

        // Si no, construir desde los campos individuales
        StringBuilder nombreCompleto = new StringBuilder();
        
        if (personaRelacionada.getPrimerNombre() != null && 
            !personaRelacionada.getPrimerNombre().trim().isEmpty()) {
            nombreCompleto.append(personaRelacionada.getPrimerNombre());
        }
        
        if (personaRelacionada.getSegundoNombre() != null && 
            !personaRelacionada.getSegundoNombre().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(personaRelacionada.getSegundoNombre());
        }
        
        if (personaRelacionada.getPrimerApellido() != null && 
            !personaRelacionada.getPrimerApellido().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(personaRelacionada.getPrimerApellido());
        }
        
        if (personaRelacionada.getSegundoApellido() != null && 
            !personaRelacionada.getSegundoApellido().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(personaRelacionada.getSegundoApellido());
        }

        // Si no hay nombres individuales, usar el campo 'nombres' como fallback
        if (nombreCompleto.length() == 0 && personaRelacionada.getNombres() != null && 
            !personaRelacionada.getNombres().trim().isEmpty()) {
            return personaRelacionada.getNombres();
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una persona desde PersonaRelacionada
     */
    private String obtenerIdentificacionPersona(PersonaRelacionada personaRelacionada) {
        if (personaRelacionada == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        if (personaRelacionada.getIdentificacion() != null && 
            !personaRelacionada.getIdentificacion().trim().isEmpty()) {
            identificacion = personaRelacionada.getIdentificacion();
        }
        else {
            String nombresCompletos = obtenerNombresCompletos(personaRelacionada);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeVisitasDTO>> obtenerInformesVisitasPaginado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeVisitasDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
                    Sort.by("idInformeVisitas").descending()
            );

            Page<InformeVisitas> informePage = this.informeVisitasRepository
                    .findByTokenIdentificadorFichaPrincipalAndEmpresaIdEmpresaAndRemovido(
                            paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<InformeVisitasDTO> paginacionResponse = new PaginacionResponse<>();
            List<InformeVisitasDTO> informeDTOList = new ArrayList<>();

            for (InformeVisitas informe : informePage.toList()) {
                InformeVisitasDTO informeDTO = new InformeVisitasDTO();
                informeDTO.setTokenIdentificador(informe.getTokenIdentificador());

                if (informe.getPersonaRelacionada() != null) {
                    informeDTO.setTokenIdentificadorPersonaRelacionada(informe.getPersonaRelacionada().getTokenIdentificador());
                }

                if (informe.getTipoAutorizacion() != null) {
                    informeDTO.setTokenIdentificadorTipoAutorizacion(informe.getTipoAutorizacion().getTokenIdentificador());
                }

                informeDTO.setFechaInicio(informe.getFechaInicio());
                informeDTO.setFechaFin(informe.getFechaFin());
                informeDTO.setCausalesRestriccion(informe.getCausalesRestriccion());
                informeDTO.setObservaciones(informe.getObservaciones());
                informeDTO.setTokenIdentificadorFichaPrincipal(informe.getTokenIdentificadorFichaPrincipal());

                informeDTOList.add(informeDTO);
            }

            paginacionResponse.setData(informeDTOList);
            paginacionResponse.setTotalItems(informePage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + informeDTOList.size() + " informes de visitas";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado " + informeDTOList.size() + " informes registrados";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SuspensionVisitasDTO>> obtenerSuspensionVisitasPaginado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<SuspensionVisitasDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
                    Sort.by("idSuspensionVisitas").descending()
            );

            Page<SuspensionVisitas> suspensionPage = this.suspensionVisitasRepository
                    .findByTokenIdentificadorFichaPrincipalAndEmpresaIdEmpresaAndRemovido(
                            paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<SuspensionVisitasDTO> paginacionResponse = new PaginacionResponse<>();
            List<SuspensionVisitasDTO> suspensionDTOList = new ArrayList<>();

            for (SuspensionVisitas suspension : suspensionPage.toList()) {
                SuspensionVisitasDTO suspensionDTO = new SuspensionVisitasDTO();
                suspensionDTO.setTokenIdentificador(suspension.getTokenIdentificador());
                
                // Obtener los cometimientos de infracción
                List<CometimientoInfraccion> cometimientosInfraccion = this.cometimientoInfraccionRepository
                    .findBySuspensionVisitasAndRemovido(suspension, false);
                    
                // Convertir a DTOs
                List<CometimientoInfraccionDTO> cometimientosDTO = new ArrayList<>();
                List<String> tokenesCausalesSeleccionadas = new ArrayList<>();
                
                for (CometimientoInfraccion cometimiento : cometimientosInfraccion) {
                    CometimientoInfraccionDTO cometimientoDTO = new CometimientoInfraccionDTO();
                    cometimientoDTO.setTokenIdentificador(cometimiento.getTokenIdentificador());
                    cometimientoDTO.setTokenIdentificadorSuspensionVisitas(suspension.getTokenIdentificador());
                    
                    if (cometimiento.getCausalSuspension() != null) {
                        String tokenCausal = cometimiento.getCausalSuspension().getTokenIdentificador();
                        cometimientoDTO.setTokenIdentificadorCausalSuspension(tokenCausal);
                        if (cometimiento.getSeleccionado()) {
                            tokenesCausalesSeleccionadas.add(tokenCausal);
                        }
                    }
                    
                    cometimientoDTO.setSeleccionado(cometimiento.getSeleccionado());
                    cometimientosDTO.add(cometimientoDTO);
                }
                
                suspensionDTO.setCometimientosInfraccion(cometimientosDTO);
                suspensionDTO.setTokenIdentificadorCausalesSuspensionSeleccionadas(tokenesCausalesSeleccionadas);

                suspensionDTO.setFechaInicio(suspension.getFechaInicio());
                suspensionDTO.setFechaFin(suspension.getFechaFin());
                suspensionDTO.setOficioDeSancion(suspension.getOficioDeSancion());
                suspensionDTO.setObservaciones(suspension.getObservaciones());
                suspensionDTO.setTokenIdentificadorFichaPrincipal(suspension.getTokenIdentificadorFichaPrincipal());

                suspensionDTOList.add(suspensionDTO);
            }

            paginacionResponse.setData(suspensionDTOList);
            paginacionResponse.setTotalItems(suspensionPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + suspensionDTOList.size() + " suspensiones de visitas";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado " + suspensionDTOList.size() + " suspensiones registradas";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private String obtenerInformacionInforme(InformeVisitasDTO informeDTO, Catalogo tipoAutorizacion) {
        StringBuilder info = new StringBuilder();
        
        // Agregar tipo
        if (tipoAutorizacion != null && tipoAutorizacion.getNombre() != null) {
            info.append("tipo: ").append(tipoAutorizacion.getNombre());
        } else if (informeDTO.getCausalesRestriccion() != null && !informeDTO.getCausalesRestriccion().trim().isEmpty()) {
            String causales = informeDTO.getCausalesRestriccion().length() > 30 
                ? informeDTO.getCausalesRestriccion().substring(0, 30) + "..." 
                : informeDTO.getCausalesRestriccion();
            info.append("causales: ").append(causales);
        } else {
            info.append("informe");
        }
        
        // Agregar fechas
        if (informeDTO.getFechaInicio() != null) {
            info.append(", desde: ").append(formatearFechaEspanol(informeDTO.getFechaInicio()));
        }
        if (informeDTO.getFechaFin() != null) {
            info.append(", hasta: ").append(formatearFechaEspanol(informeDTO.getFechaFin()));
        }
        
        return info.toString();
    }
    
    private String obtenerInformacionSuspension(SuspensionVisitasDTO suspensionDTO) {
        StringBuilder info = new StringBuilder();
        
        // Agregar número de sanción
        if (suspensionDTO.getOficioDeSancion() != null && !suspensionDTO.getOficioDeSancion().trim().isEmpty()) {
            info.append("oficio: ").append(suspensionDTO.getOficioDeSancion());
        } else {
            info.append("suspensión");
        }
        
        // Agregar fechas
        if (suspensionDTO.getFechaInicio() != null) {
            info.append(", desde: ").append(formatearFechaEspanol(suspensionDTO.getFechaInicio()));
        }
        if (suspensionDTO.getFechaFin() != null) {
            info.append(", hasta: ").append(formatearFechaEspanol(suspensionDTO.getFechaFin()));
        }
        
        return info.toString();
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformeVisitasDTO> crearInformeVisitas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu) {
        RespuestaPorDefectoAuditoria<InformeVisitasDTO> df = new RespuestaPorDefectoAuditoria<>();
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

            InformeVisitasPorPersonaDTO informeVisitasPorPersonaDTO = new Gson().fromJson(bodyString, InformeVisitasPorPersonaDTO.class);

            // Verificar si la lista está vacía o es null
            if (informeVisitasPorPersonaDTO.getListaInformeVisitas() == null || 
                informeVisitasPorPersonaDTO.getListaInformeVisitas().isEmpty()) {
                df.llenarRespuestaExitosa("No hay informes de visitas para procesar", new InformeVisitasDTO());
                return df;
            }

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            int informesCreados = 0;
            int informesEditados = 0;
            String informacionInforme = "";
            String nombresCompletos = "N/A";
            PersonaRelacionada personaRelacionada = null;

            // Procesar cada informe de la lista
            for (InformeVisitasDTO informeDTO : informeVisitasPorPersonaDTO.getListaInformeVisitas()) {
                boolean esEdicion = !informeDTO.getTokenIdentificador().equals("0");
                
                // PROTECCIÓN CONTRA DUPLICADOS diferenciada
                String baseKey = informeVisitasPorPersonaDTO.getTokenIdentificadorFichaPrincipal() + "_" + usuarioLogin.getIdUsuarioSistema();
                String requestKey = baseKey + "_informeVisitas_" + (esEdicion ? "edit_" + informeDTO.getTokenIdentificador() : "create");

                Map<String, Long> mapaEnProcesamiento = esEdicion ? solicitudesEdicionInformesEnProcesamiento : solicitudesCreacionInformesEnProcesamiento;

                Long tiempoProcesamiento = mapaEnProcesamiento.get(requestKey);
                if (tiempoProcesamiento != null) {
                    if (System.currentTimeMillis() - tiempoProcesamiento < 5000) {
                        df.setExito(false);
                        df.setMensaje("Una solicitud de " + (esEdicion ? "edición" : "creación") + 
                                     " de informe similar ya está siendo procesada. Por favor, espere unos segundos antes de intentar nuevamente.");
                        return df;
                    }
                }

                mapaEnProcesamiento.put(requestKey, System.currentTimeMillis());

                try {
                    InformeVisitas informe;
                    Catalogo tipoAutorizacion = null;

                    if (esEdicion) {
                        informe = informeVisitasRepository.findByTokenIdentificadorAndRemovido(
                                informeDTO.getTokenIdentificador(), Boolean.FALSE);
                        if (informe == null) {
                            df.setMensaje("Uno de los informes de visitas que intenta editar no existe o ya fue eliminado del sistema");
                            return df;
                        }
                        informe.setFechaEdicion(new Date());
                        informe.setIpEdita(ip);
                        informe.setUsuarioSistemaEdita(usuarioLogin);
                        informesEditados++;
                    } else {
                        informe = new InformeVisitas();
                        informe.setFechaCreacion(new Date());
                        informe.setIpCrea(ip);
                        informe.setUsuarioSistemaCrea(usuarioLogin);
                        informe.setEmpresa(empresa);
                        informesCreados++;
                    }

                    // Configurar persona relacionada si existe
                    if (informeDTO.getTokenIdentificadorPersonaRelacionada() != null &&
                            !informeDTO.getTokenIdentificadorPersonaRelacionada().equals("0")) {
                        personaRelacionada = personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(
                                informeDTO.getTokenIdentificadorPersonaRelacionada(), Boolean.FALSE);
                        if (personaRelacionada != null) {
                            informe.setPersonaRelacionada(personaRelacionada);
                            // Obtener nombres completos una sola vez
                            if ("N/A".equals(nombresCompletos)) {
                                nombresCompletos = obtenerNombresCompletos(personaRelacionada);
                            }
                        }
                    }

                    // Configurar tipo de autorización si existe
                    if (informeDTO.getTokenIdentificadorTipoAutorizacion() != null &&
                            !informeDTO.getTokenIdentificadorTipoAutorizacion().equals("0")) {
                        tipoAutorizacion = catalogoRepository.findByTokenIdentificadorAndRemovido(
                                informeDTO.getTokenIdentificadorTipoAutorizacion(), Boolean.FALSE);
                        if (tipoAutorizacion != null) {
                            informe.setTipoAutorizacion(tipoAutorizacion);
                        }
                    }

                    informe.setFechaInicio(informeDTO.getFechaInicio());
                    informe.setFechaFin(informeDTO.getFechaFin());
                    informe.setCausalesRestriccion(informeDTO.getCausalesRestriccion());
                    informe.setObservaciones(informeDTO.getObservaciones());
                    informe.setTokenIdentificadorFichaPrincipal(informeVisitasPorPersonaDTO.getTokenIdentificadorFichaPrincipal());

                    informe = this.informeVisitasRepository.save(informe);
                    informeDTO.setTokenIdentificador(informe.getTokenIdentificador());

                    // Obtener información para auditoría del primer informe procesado
                    if (informacionInforme.isEmpty()) {
                        informacionInforme = obtenerInformacionInforme(informeDTO, tipoAutorizacion);
                    }

                } finally {
                    mapaEnProcesamiento.remove(requestKey);
                }
            }

            InformeVisitasDTO primerInformeDTO = informeVisitasPorPersonaDTO.getListaInformeVisitas().isEmpty() ?
                    new InformeVisitasDTO() : informeVisitasPorPersonaDTO.getListaInformeVisitas().get(0);

            // Mensaje para el usuario - CORREGIDO para mostrar información de persona solo si está disponible
            String mensajeUsuario;
            if (informesCreados == 0 && informesEditados == 0) {
                mensajeUsuario = "No se procesaron cambios en los informes de visitas";
            } else if (informesCreados > 0 && informesEditados > 0) {
                if (!"N/A".equals(nombresCompletos)) {
                    mensajeUsuario = String.format("Se procesaron con éxito %d informe(s) nuevo(s) y %d editado(s) de %s", 
                                                   informesCreados, informesEditados, nombresCompletos);
                } else {
                    mensajeUsuario = String.format("Se procesaron con éxito %d informe(s) de visitas nuevo(s) y %d editado(s)", 
                                                   informesCreados, informesEditados);
                }
            } else if (informesCreados > 0) {
                if (!"N/A".equals(nombresCompletos)) {
                    String textoCreados = informesCreados == 1 ? "Se creó con éxito el informe de visitas de " : 
                                          "Se crearon con éxito " + informesCreados + " informes de visitas de ";
                    mensajeUsuario = textoCreados + nombresCompletos;
                } else {
                    String textoCreados = informesCreados == 1 ? "Se creó con éxito el informe de visitas" : 
                                          "Se crearon con éxito " + informesCreados + " informes de visitas";
                    mensajeUsuario = textoCreados;
                }
            } else {
                if (!"N/A".equals(nombresCompletos)) {
                    String textoEditados = informesEditados == 1 ? "Se editó con éxito el informe de visitas de " : 
                                           "Se editaron con éxito " + informesEditados + " informes de visitas de ";
                    mensajeUsuario = textoEditados + nombresCompletos;
                } else {
                    String textoEditados = informesEditados == 1 ? "Se editó con éxito el informe de visitas" : 
                                           "Se editaron con éxito " + informesEditados + " informes de visitas";
                    mensajeUsuario = textoEditados;
                }
            }

            // Mensaje para auditoría
            String mensajeAuditoria;
            if (informesCreados == 0 && informesEditados == 0) {
                mensajeAuditoria = "No se procesaron cambios en los informes de visitas";
            } else if (informesCreados > 0 && informesEditados > 0) {
                mensajeAuditoria = String.format("Se procesaron con éxito %d informe(s) nuevo(s) y %d editado(s) de %s", 
                                                 informesCreados, informesEditados, informacionInforme);
            } else if (informesCreados > 0) {
                String textoCreados = informesCreados == 1 ? "Se creó con éxito el informe" : 
                                      "Se crearon con éxito " + informesCreados + " informes";
                mensajeAuditoria = textoCreados + " de " + informacionInforme;
            } else {
                String textoEditados = informesEditados == 1 ? "Se editó con éxito el informe" : 
                                       "Se editaron con éxito " + informesEditados + " informes";
                mensajeAuditoria = textoEditados + " de " + informacionInforme;
            }

            df.llenarRespuestaExitosa(mensajeUsuario, primerInformeDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<SuspensionVisitasDTO> crearSuspensionVisitas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu) {
        RespuestaPorDefectoAuditoria<SuspensionVisitasDTO> df = new RespuestaPorDefectoAuditoria<>();
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

            SuspensionVisitasPorPersonaDTO suspensionVisitasPorPersonaDTO = new Gson().fromJson(bodyString, SuspensionVisitasPorPersonaDTO.class);

            // Verificar si la lista está vacía o es null
            if (suspensionVisitasPorPersonaDTO.getListaSuspensionVisitas() == null || 
                suspensionVisitasPorPersonaDTO.getListaSuspensionVisitas().isEmpty()) {
                df.llenarRespuestaExitosa("No hay suspensiones de visitas para procesar", new SuspensionVisitasDTO());
                return df;
            }

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            int suspensionesCreadas = 0;
            int suspensionesEditadas = 0;
            String informacionSuspension = "";

            // Procesar cada suspensión de la lista
            for (SuspensionVisitasDTO suspensionDTO : suspensionVisitasPorPersonaDTO.getListaSuspensionVisitas()) {
                boolean esEdicion = !suspensionDTO.getTokenIdentificador().equals("0");
                
                // PROTECCIÓN CONTRA DUPLICADOS diferenciada
                String baseKey = suspensionVisitasPorPersonaDTO.getTokenIdentificadorFichaPrincipal() + "_" + usuarioLogin.getIdUsuarioSistema();
                String requestKey = baseKey + "_suspensionVisitas_" + (esEdicion ? "edit_" + suspensionDTO.getTokenIdentificador() : "create");

                Map<String, Long> mapaEnProcesamiento = esEdicion ? solicitudesEdicionSuspensionesEnProcesamiento : solicitudesCreacionSuspensionesEnProcesamiento;

                Long tiempoProcesamiento = mapaEnProcesamiento.get(requestKey);
                if (tiempoProcesamiento != null) {
                    if (System.currentTimeMillis() - tiempoProcesamiento < 5000) {
                        df.setExito(false);
                        df.setMensaje("Una solicitud de " + (esEdicion ? "edición" : "creación") + 
                                     " de suspensión similar ya está siendo procesada. Por favor, espere unos segundos antes de intentar nuevamente.");
                        return df;
                    }
                }

                mapaEnProcesamiento.put(requestKey, System.currentTimeMillis());

                try {
                    SuspensionVisitas suspension;

                    if (esEdicion) {
                        suspension = suspensionVisitasRepository.findByTokenIdentificadorAndRemovido(
                                suspensionDTO.getTokenIdentificador(), Boolean.FALSE);
                        if (suspension == null) {
                            df.setMensaje("Una de las suspensiones de visitas que intenta editar no existe o ya fue eliminada del sistema");
                            return df;
                        }
                        suspension.setFechaEdicion(new Date());
                        suspension.setIpEdita(ip);
                        suspension.setUsuarioSistemaEdita(usuarioLogin);
                        suspensionesEditadas++;
                    } else {
                        suspension = new SuspensionVisitas();
                        suspension.setFechaCreacion(new Date());
                        suspension.setIpCrea(ip);
                        suspension.setUsuarioSistemaCrea(usuarioLogin);
                        suspension.setEmpresa(empresa);
                        suspensionesCreadas++;
                    }

                    suspension.setFechaInicio(suspensionDTO.getFechaInicio());
                    suspension.setFechaFin(suspensionDTO.getFechaFin());
                    suspension.setOficioDeSancion(suspensionDTO.getOficioDeSancion());
                    suspension.setObservaciones(suspensionDTO.getObservaciones());
                    suspension.setTokenIdentificadorFichaPrincipal(suspensionVisitasPorPersonaDTO.getTokenIdentificadorFichaPrincipal());

                    suspension = this.suspensionVisitasRepository.save(suspension);
                    suspensionDTO.setTokenIdentificador(suspension.getTokenIdentificador());

                    // Obtener información para auditoría de la primera suspensión procesada
                    if (informacionSuspension.isEmpty()) {
                        informacionSuspension = obtenerInformacionSuspension(suspensionDTO);
                    }

                    // Procesar las selecciones de cometimientos de infracción
                    if (suspensionDTO.getCometimientosInfraccion() != null && 
                        !suspensionDTO.getCometimientosInfraccion().isEmpty()) {

                        // Para cada cometimiento que llega del frontend
                        for (CometimientoInfraccionDTO cometimientoDTO : suspensionDTO.getCometimientosInfraccion()) {
                            CometimientoInfraccion cometimiento;

                            // Si tiene un token identificador, intentamos buscar el existente
                            if (cometimientoDTO.getTokenIdentificador() != null && 
                                !cometimientoDTO.getTokenIdentificador().equals("0")) {

                                cometimiento = cometimientoInfraccionRepository.findByTokenIdentificadorAndRemovido(
                                    cometimientoDTO.getTokenIdentificador(), Boolean.FALSE);

                                // Si no existe, creamos uno nuevo
                                if (cometimiento == null) {
                                    cometimiento = new CometimientoInfraccion();
                                    cometimiento.setFechaCreacion(new Date());
                                    cometimiento.setIpCrea(ip);
                                    cometimiento.setUsuarioSistemaCrea(usuarioLogin);
                                    cometimiento.setEmpresa(empresa);
                                } else {
                                    // Si existe, lo actualizamos
                                    cometimiento.setFechaEdicion(new Date());
                                    cometimiento.setIpEdita(ip);
                                    cometimiento.setUsuarioSistemaEdita(usuarioLogin);
                                }
                            } else {
                                // Si no tiene token, creamos uno nuevo
                                cometimiento = new CometimientoInfraccion();
                                cometimiento.setFechaCreacion(new Date());
                                cometimiento.setIpCrea(ip);
                                cometimiento.setUsuarioSistemaCrea(usuarioLogin);
                                cometimiento.setEmpresa(empresa);
                            }

                            // Configurar la relación con la suspensión
                            cometimiento.setSuspensionVisitas(suspension);

                            // Configurar la causal de suspensión si existe
                            if (cometimientoDTO.getTokenIdentificadorCausalSuspension() != null &&
                                !cometimientoDTO.getTokenIdentificadorCausalSuspension().equals("0")) {

                                Catalogo causal = catalogoRepository.findByTokenIdentificadorAndRemovido(
                                    cometimientoDTO.getTokenIdentificadorCausalSuspension(), Boolean.FALSE);

                                if (causal != null) {
                                    cometimiento.setCausalSuspension(causal);
                                }
                            }

                            // Configurar si está seleccionado
                            cometimiento.setSeleccionado(cometimientoDTO.getSeleccionado());

                            // Guardar el cometimiento
                            cometimientoInfraccionRepository.save(cometimiento);
                        }
                    }

                } finally {
                    mapaEnProcesamiento.remove(requestKey);
                }
            }

            SuspensionVisitasDTO primerSuspensionDTO = suspensionVisitasPorPersonaDTO.getListaSuspensionVisitas().isEmpty() ?
                    new SuspensionVisitasDTO() : suspensionVisitasPorPersonaDTO.getListaSuspensionVisitas().get(0);

            // Mensaje para el usuario - CORREGIDO para no mostrar información de persona
            String mensajeUsuario;
            if (suspensionesCreadas == 0 && suspensionesEditadas == 0) {
                mensajeUsuario = "No se procesaron cambios en las suspensiones de visitas";
            } else if (suspensionesCreadas > 0 && suspensionesEditadas > 0) {
                mensajeUsuario = String.format("Se procesaron con éxito %d suspensión(es) de visitas nueva(s) y %d editada(s)", 
                                               suspensionesCreadas, suspensionesEditadas);
            } else if (suspensionesCreadas > 0) {
                String textoCreadas = suspensionesCreadas == 1 ? "Se creó con éxito la suspensión de visitas" : 
                                      "Se crearon con éxito " + suspensionesCreadas + " suspensiones de visitas";
                mensajeUsuario = textoCreadas;
            } else {
                String textoEditadas = suspensionesEditadas == 1 ? "Se editó con éxito la suspensión de visitas" : 
                                       "Se editaron con éxito " + suspensionesEditadas + " suspensiones de visitas";
                mensajeUsuario = textoEditadas;
            }

            // Mensaje para auditoría
            String mensajeAuditoria;
            if (suspensionesCreadas == 0 && suspensionesEditadas == 0) {
                mensajeAuditoria = "No se procesaron cambios en las suspensiones de visitas";
            } else if (suspensionesCreadas > 0 && suspensionesEditadas > 0) {
                mensajeAuditoria = String.format("Se procesaron con éxito %d suspensión(es) nueva(s) y %d editada(s) de %s", 
                                                 suspensionesCreadas, suspensionesEditadas, informacionSuspension);
            } else if (suspensionesCreadas > 0) {
                String textoCreadas = suspensionesCreadas == 1 ? "Se creó con éxito la suspensión" : 
                                      "Se crearon con éxito " + suspensionesCreadas + " suspensiones";
                mensajeAuditoria = textoCreadas + " de " + informacionSuspension;
            } else {
                String textoEditadas = suspensionesEditadas == 1 ? "Se editó con éxito la suspensión" : 
                                       "Se editaron con éxito " + suspensionesEditadas + " suspensiones";
                mensajeAuditoria = textoEditadas + " de " + informacionSuspension;
            }

            df.llenarRespuestaExitosa(mensajeUsuario, primerSuspensionDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarInformeVisitas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            InformeVisitasDTO informeDTO = new Gson().fromJson(bodyString, InformeVisitasDTO.class);

            InformeVisitas informe = this.informeVisitasRepository
                    .findByTokenIdentificadorAndRemovido(informeDTO.getTokenIdentificador(), false);

            if (informe == null) {
                df.setMensaje("El informe de visitas que intenta eliminar no fue encontrado o ya fue eliminado anteriormente del sistema");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(informe.getPersonaRelacionada());
            String informacionInforme = obtenerInformacionInforme(informeDTO, informe.getTipoAutorizacion());

            Date fecha = new Date();
            informe.setRemovido(true);
            informe.setIpElimina(ip);
            informe.setUsuarioSistemaElimina(usuarioSistemaLogin);
            informe.setFechaEliminacion(fecha);

            this.informeVisitasRepository.save(informe);

            // Mensaje para el usuario - CORREGIDO para no mostrar N/A
            String mensajeUsuario;
            if (!"N/A".equals(nombresCompletos)) {
                mensajeUsuario = "Se eliminó con éxito el informe de visitas de " + nombresCompletos;
            } else {
                mensajeUsuario = "Se eliminó con éxito el informe de visitas";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se ha eliminado con éxito del sistema el informe de " + informacionInforme;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSuspensionVisitas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            SuspensionVisitasDTO suspensionDTO = new Gson().fromJson(bodyString, SuspensionVisitasDTO.class);

            SuspensionVisitas suspension = this.suspensionVisitasRepository
                    .findByTokenIdentificadorAndRemovido(suspensionDTO.getTokenIdentificador(), false);

            if (suspension == null) {
                df.setMensaje("La suspensión de visitas que intenta eliminar no fue encontrada o ya fue eliminada anteriormente del sistema");
                return df;
            }

            // Obtener información para los mensajes
            String informacionSuspension = obtenerInformacionSuspension(suspensionDTO);

            // Eliminar los cometimientos de infracción relacionados
            List<CometimientoInfraccion> cometimientos = this.cometimientoInfraccionRepository
                .findBySuspensionVisitasAndRemovido(suspension, false);
                
            for (CometimientoInfraccion cometimiento : cometimientos) {
                cometimiento.setRemovido(true);
                cometimiento.setIpElimina(ip);
                cometimiento.setUsuarioSistemaElimina(usuarioSistemaLogin);
                cometimiento.setFechaEliminacion(new Date());
                this.cometimientoInfraccionRepository.save(cometimiento);
            }

            // Eliminar la suspensión
            Date fecha = new Date();
            suspension.setRemovido(true);
            suspension.setIpElimina(ip);
            suspension.setUsuarioSistemaElimina(usuarioSistemaLogin);
            suspension.setFechaEliminacion(fecha);

            this.suspensionVisitasRepository.save(suspension);

            // Mensaje para el usuario - CORREGIDO para no mostrar información de persona
            String mensajeUsuario = "Se eliminó con éxito la suspensión de visitas";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se ha eliminado con éxito del sistema la suspensión de " + informacionSuspension;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
