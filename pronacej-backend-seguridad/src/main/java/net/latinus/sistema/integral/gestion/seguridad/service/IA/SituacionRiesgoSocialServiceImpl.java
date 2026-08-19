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
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.SituacionRiesgoSocial;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionRiesgoSocialDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SituacionRiesgoSocialRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class SituacionRiesgoSocialServiceImpl implements SituacionRiesgoSocialService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private SituacionRiesgoSocialRepository situacionRiesgoSocialRepository;
    // Mapa para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();
    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionRiesgoSocialDTO>> obtenerSituacionesRiesgoSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionRiesgoSocialDTO>> df = new RespuestaPorDefectoAuditoria<>();

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

            // Usar el filtro si existe
            String filtro = paginacionRequest.getFilter() != null ? paginacionRequest.getFilter() : "";

            // Variable para almacenar el resultado
            Page<SituacionRiesgoSocial> situacionRiesgoSocialPage;

            // Manejar ordenamiento especial para campo calculado usuarioRegistro
            if (paginacionRequest.getSort() != null && "usuarioRegistro".equals(paginacionRequest.getSort())) {
                // Caso especial: ordenamiento por usuario que registró
                String direccion = paginacionRequest.getDirection() != null ? 
                    paginacionRequest.getDirection().toUpperCase() : "ASC";

                // Crear pageable para paginación (sin ordenamiento ya que se maneja en la consulta)
                Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

                if (filtro.isEmpty()) {
                    // Sin filtro
                    if ("DESC".equals(direccion)) {
                        situacionRiesgoSocialPage = this.situacionRiesgoSocialRepository.buscarOrdenadoPorUsuarioRegistroDesc(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(), 
                                pageable);
                    } else {
                        situacionRiesgoSocialPage = this.situacionRiesgoSocialRepository.buscarOrdenadoPorUsuarioRegistroAsc(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(), 
                                pageable);
                    }
                } else {
                    // Con filtro
                    if ("DESC".equals(direccion)) {
                        situacionRiesgoSocialPage = this.situacionRiesgoSocialRepository.buscarConFiltroOrdenadoPorUsuarioRegistroDesc(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(), 
                                filtro,
                                pageable);
                    } else {
                        situacionRiesgoSocialPage = this.situacionRiesgoSocialRepository.buscarConFiltroOrdenadoPorUsuarioRegistroAsc(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(), 
                                filtro,
                                pageable);
                    }
                }
            } else {
                // Ordenamiento normal para otros campos
                Pageable pageable;

                if (paginacionRequest.getSort() != null && !paginacionRequest.getSort().isEmpty() 
                        && paginacionRequest.getDirection() != null && !paginacionRequest.getDirection().isEmpty()) {

                    // Mapear los campos del frontend a los campos reales de la entidad
                    String campoOrdenamiento = mapearCampoOrdenamiento(paginacionRequest.getSort());

                    Sort.Direction direction = paginacionRequest.getDirection().equalsIgnoreCase("asc") 
                            ? Sort.Direction.ASC : Sort.Direction.DESC;

                    pageable = PageRequest.of(
                            paginacionRequest.getPage(),
                            paginacionRequest.getSize(),
                            Sort.by(direction, campoOrdenamiento)
                    );
                } else {
                    // Ordenamiento por defecto
                    pageable = PageRequest.of(
                            paginacionRequest.getPage(),
                            paginacionRequest.getSize(),
                            Sort.by("idSituacionRiesgoSocial").descending()
                    );
                }

                // Usar el método de búsqueda normal con filtro
                situacionRiesgoSocialPage = this.situacionRiesgoSocialRepository.buscarPorFiltro(
                        paginacionRequest.getTokenIdentificador(), 
                        empresa.getIdEmpresa(), 
                        filtro, 
                        pageable);
            }

            // Convertir entidades a DTOs
            PaginacionResponse<SituacionRiesgoSocialDTO> paginacionResponse = new PaginacionResponse<>();
            List<SituacionRiesgoSocialDTO> situacionRiesgoSocialDTOList = new ArrayList<>();

            for (SituacionRiesgoSocial situacionRiesgoSocial : situacionRiesgoSocialPage.getContent()) {
                SituacionRiesgoSocialDTO situacionRiesgoSocialDTO = new SituacionRiesgoSocialDTO();
                situacionRiesgoSocialDTO.setTokenIdentificador(situacionRiesgoSocial.getTokenIdentificador());
                situacionRiesgoSocialDTO.setTokenIdentificadorEmpresa(situacionRiesgoSocial.getEmpresa().getTokenIdentificador());
                situacionRiesgoSocialDTO.setFechaCreacion(situacionRiesgoSocial.getFechaCreacion());

                situacionRiesgoSocialDTO.setAnteDeliFami(situacionRiesgoSocial.getAnteDeliFami());
                situacionRiesgoSocialDTO.setPrimManiInfrAdol(situacionRiesgoSocial.getPrimManiInfrAdol());
                situacionRiesgoSocialDTO.setEvasionHogar(situacionRiesgoSocial.getEvasionHogar());
                situacionRiesgoSocialDTO.setEstadoSaludGeneral(situacionRiesgoSocial.getEstadoSaludGeneral());
                situacionRiesgoSocialDTO.setProblemasLegales(situacionRiesgoSocial.getProblemasLegales());
                situacionRiesgoSocialDTO.setObservaciones(situacionRiesgoSocial.getObservaciones());

                if (situacionRiesgoSocial.getFichaIdentificacion() != null) {
                    situacionRiesgoSocialDTO.setTokenIdentificadorFichaIdentificacion(situacionRiesgoSocial.getFichaIdentificacion().getTokenIdentificador());
                }

                situacionRiesgoSocialDTO.setNombreCompletoUsuarioCreacion(
                    situacionRiesgoSocial.getUsuarioSistemaCrea().getNombres() + " " + 
                    situacionRiesgoSocial.getUsuarioSistemaCrea().getApellidos());

                situacionRiesgoSocialDTOList.add(situacionRiesgoSocialDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            situacionRiesgoSocialDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(situacionRiesgoSocialDTOList);
            paginacionResponse.setTotalItems(situacionRiesgoSocialPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + situacionRiesgoSocialPage.getTotalElements() + " situaciones de riesgo social";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + situacionRiesgoSocialPage.getTotalElements() + " situaciones de riesgo social registradas";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            ex.printStackTrace(); // Para debug
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Mapea los campos del frontend a los campos reales de la entidad JPA
     * @param campoFrontend Campo solicitado desde el frontend
     * @return Campo real de la entidad que se puede usar para ordenamiento
     */
    private String mapearCampoOrdenamiento(String campoFrontend) {
        switch (campoFrontend) {
            case "fechaCreacion":
                return "fechaCreacion";
            case "numero":
                // Para ordenamiento por número, usar el ID (orden inverso)
                return "idSituacionRiesgoSocial";
            default:
                // Si no se encuentra mapeo, usar fechaCreacion como default
                return "fechaCreacion";
        }
    }

    @Override
    public RespuestaPorDefectoAuditoria<SituacionRiesgoSocialDTO> crearSituacionRiesgoSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<SituacionRiesgoSocialDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            SituacionRiesgoSocialDTO situacionRiesgoSocialDTO = new Gson().fromJson(bodyString, SituacionRiesgoSocialDTO.class);

            situacionRiesgoSocialDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            // Generar clave única para esta solicitud (ficha + usuario + operación)
            String requestKey = situacionRiesgoSocialDTO.getTokenIdentificadorFichaIdentificacion() + "_" + 
                               usuarioLogin.getIdUsuarioSistema() + "_" + 
                               (situacionRiesgoSocialDTO.getEsEdicion() ? "edit" : "create");

            // Verificar si ya hay una solicitud en proceso con esta clave
            Long tiempoProcesamiento = solicitudesEnProcesamiento.get(requestKey);
            if (tiempoProcesamiento != null) {
                if (System.currentTimeMillis() - tiempoProcesamiento < 5000) {
                    df.setExito(false);
                    df.setMensaje("Una solicitud similar ya está siendo procesada. Por favor, espere unos segundos antes de intentar nuevamente.");
                    return df;
                }
            }

            // Registrar esta solicitud como en procesamiento
            solicitudesEnProcesamiento.put(requestKey, System.currentTimeMillis());

            try {
                SituacionRiesgoSocial situacionRiesgoSocial;
                boolean esEdicion = false;
                
                if(situacionRiesgoSocialDTO.getEsEdicion()){
                    situacionRiesgoSocial = situacionRiesgoSocialRepository.findByTokenIdentificadorAndRemovido(situacionRiesgoSocialDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (situacionRiesgoSocial == null) {
                        df.setMensaje("La situación de riesgo social a editar no existe o ya fue eliminada anteriormente");
                        return df;
                    }
                    situacionRiesgoSocial.setFechaEdicion(new Date());
                    situacionRiesgoSocial.setIpEdita(ip);
                    situacionRiesgoSocial.setUsuarioSistemaEdita(usuarioLogin);
                    esEdicion = true;
                }else{                
                    situacionRiesgoSocial = new SituacionRiesgoSocial();
                    situacionRiesgoSocial.setFechaCreacion(new Date());
                    situacionRiesgoSocial.setIpCrea(ip);
                    situacionRiesgoSocial.setUsuarioSistemaCrea(usuarioLogin);
                    situacionRiesgoSocial.setEmpresa(empresa);
                    FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(situacionRiesgoSocialDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);
                    situacionRiesgoSocial.setFichaIdentificacion(fichaIdentificacion);
                }

                situacionRiesgoSocial.setAnteDeliFami(situacionRiesgoSocialDTO.getAnteDeliFami());
                situacionRiesgoSocial.setPrimManiInfrAdol(situacionRiesgoSocialDTO.getPrimManiInfrAdol());
                situacionRiesgoSocial.setEvasionHogar(situacionRiesgoSocialDTO.getEvasionHogar());
                situacionRiesgoSocial.setEstadoSaludGeneral(situacionRiesgoSocialDTO.getEstadoSaludGeneral());
                situacionRiesgoSocial.setProblemasLegales(situacionRiesgoSocialDTO.getProblemasLegales());
                situacionRiesgoSocial.setObservaciones(situacionRiesgoSocialDTO.getObservaciones());

                situacionRiesgoSocial = this.situacionRiesgoSocialRepository.save(situacionRiesgoSocial);
                situacionRiesgoSocialDTO.setTokenIdentificador(situacionRiesgoSocial.getTokenIdentificador());

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(situacionRiesgoSocial.getFichaIdentificacion());
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito la situación de riesgo social de " + nombresCompletos;
                
                // Mensaje para auditoría
                String identificacionPersona = obtenerIdentificacionPersona(situacionRiesgoSocial.getFichaIdentificacion());
                String mensajeAuditoria = "Se " + accion + " con éxito la situación de riesgo social de la persona con identificación: " + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, situacionRiesgoSocialDTO, mensajeAuditoria);
            } finally {
                // Eliminar la entrada del mapa una vez procesada (ya sea con éxito o error)
                solicitudesEnProcesamiento.remove(requestKey);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSituacionRiesgoSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            SituacionRiesgoSocialDTO situacionRiesgoSocialDTO = new Gson().fromJson(bodyString, SituacionRiesgoSocialDTO.class);

            SituacionRiesgoSocial situacionRiesgoSocial = this.situacionRiesgoSocialRepository.findByTokenIdentificadorAndRemovido(situacionRiesgoSocialDTO.getTokenIdentificador(), false
            );

            if (situacionRiesgoSocial == null) {
                df.setMensaje("La situación de riesgo social no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(situacionRiesgoSocial.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(situacionRiesgoSocial.getFichaIdentificacion());

            Date fecha = new Date();
            situacionRiesgoSocial.setRemovido(true);
            situacionRiesgoSocial.setIpElimina(ip);
            situacionRiesgoSocial.setUsuarioSistemaElimina(usuarioSistemaLogin);
            situacionRiesgoSocial.setFechaEliminacion(fecha);

            this.situacionRiesgoSocialRepository.save(situacionRiesgoSocial);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la situación de riesgo social de " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito la situación de riesgo social de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
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
     * Método auxiliar para obtener la identificación de una persona desde su ficha de identificación
     */
    private String obtenerIdentificacionPersona(FichaIdentificacion fichaIdentificacion) {
        if (fichaIdentificacion == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        // Primero intentar con el campo dni
        if (fichaIdentificacion.getDni() != null && !fichaIdentificacion.getDni().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getDni();
        }
        // Si no hay dni, intentar con numeroIdentificacion
        else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getNumeroIdentificacion();
        }
        // Si no hay ninguno, usar nombres y apellidos como identificación
        else {
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }
    
}