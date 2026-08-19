package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadOcupacional;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.ActividadOcupacionalRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@AllArgsConstructor
public class ActividadOcupacionalServiceImpl implements ActividadOcupacionalService{

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private ActividadOcupacionalRepository actividadOcupacionalRepository;
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JerarquiaRepository jerarquiaRepository;
    
    // Mapa para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();
    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadOcupacionalDTO>> obtenerActividadesOcupacionalesPorFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ActividadOcupacionalDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idActividadOcupacional").descending()
            );

            Page<ActividadOcupacional> actividadesPage = this.actividadOcupacionalRepository
                    .buscarPorFiltro(
                            paginacionRequest.getTokenIdentificador(),
                            paginacionRequest.getFilter(),
                            pageable
                    );

            PaginacionResponse<ActividadOcupacionalDTO> paginacionResponse = new PaginacionResponse<>();
            List<ActividadOcupacionalDTO> actividadDTOList = new ArrayList<>();

            // Obtener la ficha de identificación para los mensajes
            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository
                    .findByTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), Boolean.FALSE);

            for (ActividadOcupacional actividad : actividadesPage.toList()) {
                ActividadOcupacionalDTO actividadDTO = new ActividadOcupacionalDTO();
                actividadDTO.setTokenIdentificador(actividad.getTokenIdentificador());

                if (actividad.getFichaIdentificacion() != null) {
                    actividadDTO.setTokenFichaIdentificacion(actividad.getFichaIdentificacion().getTokenIdentificador());
                }

                actividadDTO.setFechaInicio(actividad.getFechaInicio());
                actividadDTO.setTipoActividadOcupacional(
                        actividad.getTipoActividadOcupacional() != null
                                ? catalogoToDTO(actividad.getTipoActividadOcupacional())
                                : null
                );
                actividadDTO.setEstadoActividadOcupacional(
                        actividad.getEstadoActividadOcupacional() != null
                                ? catalogoToDTO(actividad.getEstadoActividadOcupacional())
                                : null
                );
                actividadDTO.setTipoPrograma(
                        actividad.getTipoPrograma() != null
                                ? catalogoToDTO(actividad.getTipoPrograma())
                                : null
                );
                actividadDTO.setObjetivoActividad(actividad.getObjetivoActividad());
                actividadDTO.setNumeroDocumento(actividad.getNumeroDocumento());
                actividadDTO.setPrograma(
                        actividad.getPrograma() != null
                                ? entidadADtoJerarquia(actividad.getPrograma())
                                : null
                );

                actividadDTO.setAmbiente(
                        actividad.getAmbiente() != null
                                ? entidadADtoJerarquia(actividad.getAmbiente())
                                : null
                );
                actividadDTO.setDocumentoAprobacion(
                        actividad.getDocumentoAprobacion() != null
                                ? actividad.getDocumentoAprobacion()
                                : null
                );
                actividadDTO.setFechaCreacion(actividad.getFechaCreacion());

                actividadDTOList.add(actividadDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            actividadDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(actividadDTOList);
            paginacionResponse.setTotalItems(actividadesPage.getTotalElements());

            // Mensajes con información de la persona
            if (fichaIdentificacion != null) {
                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                
                // Mensaje para el usuario - SOLO agregar cédula entre paréntesis
                String mensajeUsuario = "Obteniendo " + actividadesPage.getTotalElements() + 
                        " actividades socio recreativas de " + nombresCompletos + " (" + identificacionPersona + ")";
                
                // Mensaje para auditoría - formato original corregido
                String mensajeAuditoria = "Se han encontrado un total de " + actividadesPage.getTotalElements() + 
                        " actividades socio recreativas de la persona con identificación: " + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
            } else {
                // Mensaje genérico si no se encuentra la ficha
                String mensajeUsuario = "Se han encontrado un total de: " + actividadDTOList.size() +
                        " de: " + actividadesPage.getTotalElements() + " elementos disponibles";
                String mensajeAuditoria = "Se obtuvieron " + actividadesPage.getTotalElements() + " actividades socio recreativas";
                
                df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> crearActividadOcupacional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{
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
            String body = df22.getData();
            ActividadOcupacionalDTO actividadDTO = new Gson().fromJson(body, ActividadOcupacionalDTO.class);

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = actividadDTO.getTokenFichaIdentificacion() + "-actividadOcupacional";
            if (actividadDTO.getEsEdicion() != null && actividadDTO.getEsEdicion()) {
                idSolicitud += "-" + actividadDTO.getTokenIdentificador();
            }

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
                Empresa empresa = df2.getData().getEmpresa();
                UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();
                String ip = httpServletRequest.getRemoteAddr();
                
                ActividadOcupacional actividad;
                boolean esEdicion = false;

                if (actividadDTO.getEsEdicion() != null && actividadDTO.getEsEdicion()) {
                    Optional<ActividadOcupacional> optionalActividad = actividadOcupacionalRepository
                            .findByTokenIdentificadorAndRemovido(actividadDTO.getTokenIdentificador(), false);
                    if(optionalActividad.isPresent()) {
                        actividad = optionalActividad.get();
                        esEdicion = true;
                        actividad.setFechaEdicion(new Date());
                        actividad.setIpEdita(ip);
                        actividad.setUsuarioSistemaEdita(usuarioLogin);
                    } else {
                        df.setExito(false);
                        df.setMensaje("No se encontró una actividad ocupacional para editar con el token proporcionado.");
                        return df;
                    }
                } else {
                    actividad = new ActividadOcupacional();
                    actividad.setFechaCreacion(new Date());
                    actividad.setIpCrea(ip);
                    actividad.setUsuarioSistemaCrea(usuarioLogin);
                }

                FichaIdentificacion ficha = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(actividadDTO.getTokenFichaIdentificacion(),
                        Boolean.FALSE);

                actividad.setFechaInicio(actividadDTO.getFechaInicio());
                actividad.setObjetivoActividad(actividadDTO.getObjetivoActividad());
                actividad.setNumeroDocumento(actividadDTO.getNumeroDocumento());

                actividad.setTipoActividadOcupacional(dtoToCatalogo(actividadDTO.getTipoActividadOcupacional()));
                actividad.setEstadoActividadOcupacional(dtoToCatalogo(actividadDTO.getEstadoActividadOcupacional()));
                actividad.setTipoPrograma(dtoToCatalogo(actividadDTO.getTipoPrograma()));
                actividad.setFichaIdentificacion(ficha);

                actividad.setPrograma(dtoAEntidadJerarquia(actividadDTO.getPrograma()));
                actividad.setAmbiente(dtoAEntidadJerarquia(actividadDTO.getAmbiente()));
                actividad.setDocumentoAprobacion(actividadDTO.getDocumentoAprobacion());

                actividadOcupacionalRepository.save(actividad);

                // Obtener datos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(ficha);
                String identificacionPersona = obtenerIdentificacionPersona(ficha);
                String accion = esEdicion ? "editó" : "creó";
                
                // Mensaje para el usuario - SOLO agregar cédula entre paréntesis
                String mensajeUsuario = "Se " + accion + " con éxito la actividad socio recreativa de " + 
                        nombresCompletos + " (" + identificacionPersona + ")";
                
                // Mensaje para auditoría - Programa + Ambiente + Objetivo + Persona (SIN corchetes en programa y ambiente)
                String detalleAuditoria = construirDetalleAuditoria(actividad);
                String mensajeAuditoria = "Se " + accion + " con éxito la actividad socio recreativa" + detalleAuditoria + 
                        " de la persona con identificación: " + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, actividadDTO, mensajeAuditoria);
            } finally {
                solicitudesEnProcesamiento.remove(idSolicitud);
            }

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> eliminarActividadOcupacional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar y obtener JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();

            String tokenIdentificador = body.trim();

            Optional<ActividadOcupacional> optionalActividad = actividadOcupacionalRepository
                    .findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if(!optionalActividad.isPresent()) {
                respuesta.setExito(false);
                respuesta.setMensaje("No se encontró la actividad ocupacional con el token proporcionado.");
                return respuesta;
            }

            ActividadOcupacional actividad = optionalActividad.get();
            
            // Obtener datos para los mensajes antes de eliminar
            String nombresCompletos = obtenerNombresCompletos(actividad.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(actividad.getFichaIdentificacion());
            
            actividad.setRemovido(true);
            actividad.setFechaEliminacion(new Date());
            actividad.setIpElimina(ip);
            actividad.setUsuarioSistemaElimina(usuarioLogin);

            actividadOcupacionalRepository.save(actividad);

            ActividadOcupacionalDTO actividadDTO = new ActividadOcupacionalDTO();
            actividadDTO.setTokenIdentificador(actividad.getTokenIdentificador());

            // Mensaje para el usuario - SOLO agregar cédula entre paréntesis
            String mensajeUsuario = "Se eliminó con éxito la actividad socio recreativa de " + 
                    nombresCompletos + " (" + identificacionPersona + ")";
            
            // Mensaje para auditoría - Programa + Ambiente + Objetivo + Persona (SIN corchetes en programa y ambiente)
            String detalleAuditoria = construirDetalleAuditoria(actividad);
            String mensajeAuditoria = "Se eliminó con éxito la actividad socio recreativa" + detalleAuditoria + 
                    " de la persona con identificación: " + identificacionPersona;
            
            respuesta.llenarRespuestaExitosa(mensajeUsuario, actividadDTO, mensajeAuditoria);
        } catch(Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> obtenerActividadOcupacionalPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ActividadOcupacionalDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar y obtener JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            // Desencriptar el body para extraer el token
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            String tokenIdentificador = body.trim(); // Se asume que el body contiene el token directamente

            // Buscar la actividad por token y que no esté removida
            Optional<ActividadOcupacional> optionalActividad = actividadOcupacionalRepository
                    .findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if (!optionalActividad.isPresent()) {
                respuesta.setExito(false);
                respuesta.setMensaje("Actividad Ocupacional no encontrada para el token proporcionado.");
                return respuesta;
            }

            ActividadOcupacional actividad = optionalActividad.get();

            // Mapear la entidad a DTO
            ActividadOcupacionalDTO actividadDTO = new ActividadOcupacionalDTO();
            actividadDTO.setTokenIdentificador(actividad.getTokenIdentificador());
            actividadDTO.setFechaInicio(actividad.getFechaInicio());
            actividadDTO.setObjetivoActividad(actividad.getObjetivoActividad());
            actividadDTO.setNumeroDocumento(actividad.getNumeroDocumento());

            actividadDTO.setTipoActividadOcupacional(
                    actividad.getTipoActividadOcupacional() != null
                            ? catalogoToDTO(actividad.getTipoActividadOcupacional())
                            : null
            );
            actividadDTO.setEstadoActividadOcupacional(
                    actividad.getEstadoActividadOcupacional() != null
                            ? catalogoToDTO(actividad.getEstadoActividadOcupacional())
                            : null
            );
            actividadDTO.setTipoPrograma(
                    actividad.getTipoPrograma() != null
                            ? catalogoToDTO(actividad.getTipoPrograma())
                            : null
            );

            actividadDTO.setPrograma(
                    actividad.getPrograma() != null
                            ? entidadADtoJerarquia(actividad.getPrograma())
                            : null
            );

            actividadDTO.setAmbiente(
                    actividad.getAmbiente() != null
                            ? entidadADtoJerarquia(actividad.getAmbiente())
                            : null
            );

            if (actividad.getFichaIdentificacion() != null) {
                actividadDTO.setTokenFichaIdentificacion(actividad.getFichaIdentificacion().getTokenIdentificador());
            }

            // Obtener nombres completos e identificación para los mensajes
            String nombresCompletos = obtenerNombresCompletos(actividad.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(actividad.getFichaIdentificacion());
            
            // Mensaje para el usuario - SOLO agregar cédula entre paréntesis
            String mensajeUsuario = "Obteniendo actividad socio recreativa de " + nombresCompletos + " (" + identificacionPersona + ")";
            
            // Mensaje para auditoría - formato original
            String mensajeAuditoria = "Se obtuvo con éxito la actividad socio recreativa de la persona con identificación: " + identificacionPersona;
            
            respuesta.llenarRespuestaExitosa(mensajeUsuario, actividadDTO, mensajeAuditoria);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }

    private CatalogoDTO catalogoToDTO(Catalogo catalogo){
        if (catalogo == null) {
            return null;
        }

        CatalogoDTO catalogoDTO = new CatalogoDTO();
        catalogoDTO.setNombre(catalogo.getNombre());
        catalogoDTO.setNemonico(catalogo.getNemonico());
        catalogoDTO.setDescripcion(catalogo.getDescripcion());
        catalogoDTO.setTokenIdentificador(catalogo.getTokenIdentificador());
        catalogoDTO.setCodigoExterno(catalogo.getCodigoExterno());

        return catalogoDTO;
    }

    private Catalogo dtoToCatalogo(CatalogoDTO catalogoDTO){
        if (catalogoDTO == null) {
            return null;
        }

        Catalogo catalogo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);

        return catalogo;
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

    /**
     * NUEVO MÉTODO: Construir detalle de actividad para AUDITORÍA con etiquetas descriptivas
     */
    private String construirDetalleAuditoria(ActividadOcupacional actividad) {
        StringBuilder detalle = new StringBuilder();
        
        // Programa con etiqueta descriptiva
        if (actividad.getPrograma() != null && actividad.getPrograma().getNombre() != null) {
            detalle.append(" programa ").append(actividad.getPrograma().getNombre());
        }
        
        // Ambiente con etiqueta descriptiva
        if (actividad.getAmbiente() != null && actividad.getAmbiente().getNombre() != null) {
            detalle.append(" ambiente ").append(actividad.getAmbiente().getNombre());
        }
        
        // Objetivo de la actividad con etiqueta descriptiva completa
        if (actividad.getObjetivoActividad() != null && !actividad.getObjetivoActividad().trim().isEmpty()) {
            String objetivo = actividad.getObjetivoActividad().trim();
            // Limitar el objetivo a 80 caracteres para mayor contexto en auditoría
            if (objetivo.length() > 80) {
                objetivo = objetivo.substring(0, 80) + "...";
            }
            detalle.append(" objetivo de la actividad ").append(objetivo);
        }
        
        return detalle.toString();
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
            return "N/A";
        }

        String identificacion = "N/A";
        
        // Prioridad 1: DNI
        if (fichaIdentificacion.getDni() != null && !fichaIdentificacion.getDni().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getDni();
        }
        // Prioridad 2: Número de identificación
        else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getNumeroIdentificacion();
        }
        // Prioridad 3: Alias
        else if (fichaIdentificacion.getAlias() != null && !fichaIdentificacion.getAlias().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getAlias();
        }
        // Prioridad 4: Nombres completos
        else {
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }
}