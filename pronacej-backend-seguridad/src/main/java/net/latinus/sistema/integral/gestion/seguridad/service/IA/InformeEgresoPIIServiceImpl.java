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
import net.latinus.sistema.integral.gestion.seguridad.entities.InformeEgresoPII;
import net.latinus.sistema.integral.gestion.seguridad.entities.InformeSeguimientoPII;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeEgresoPIIDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InformeSeguimientoPIIRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InformeEgresoPIIRepository;
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
public class InformeEgresoPIIServiceImpl implements InformeEgresoPIIService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private InformeSeguimientoPIIRepository informeSeguimientoPIIRepository;
    private InformeEgresoPIIRepository informeEgresoPIIRepository;
    // Variable para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeEgresoPIIDTO>> obtenerInformesEgreso(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeEgresoPIIDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();

            // Usar el filtro si existe
            String filtro = paginacionRequest.getFilter() != null ? paginacionRequest.getFilter() : "";

            // Variable para almacenar el resultado
            Page<InformeEgresoPII> informeEgresoPage;

            // Manejar ordenamiento especial para campos calculados
            if (paginacionRequest.getSort() != null && 
                esOrdenamientoEspecial(paginacionRequest.getSort())) {

                String direccion = paginacionRequest.getDirection() != null ? 
                    paginacionRequest.getDirection().toUpperCase() : "ASC";

                // Crear pageable para paginación (sin ordenamiento ya que se maneja en la consulta)
                Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

                informeEgresoPage = obtenerConOrdenamientoEspecial(
                    paginacionRequest.getTokenIdentificador(),
                    empresa.getIdEmpresa(),
                    filtro,
                    paginacionRequest.getSort(),
                    direccion,
                    pageable
                );

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
                            Sort.by("idInformeEgresoPII").descending()
                    );
                }

                // Usar el método de búsqueda normal con filtro
                informeEgresoPage = this.informeEgresoPIIRepository.buscarPorFiltro(
                    paginacionRequest.getTokenIdentificador(), 
                    empresa.getIdEmpresa(), 
                    filtro, 
                    false, 
                    pageable
                );
            }

            PaginacionResponse<InformeEgresoPIIDTO> paginacionResponse = new PaginacionResponse<>();
            List<InformeEgresoPIIDTO> informeEgresoDTOList = new ArrayList<>();

            for (InformeEgresoPII informeEgreso : informeEgresoPage.getContent()) {
                InformeEgresoPIIDTO informeEgresoDTO = new InformeEgresoPIIDTO();
                informeEgresoDTO.setTokenIdentificador(informeEgreso.getTokenIdentificador());
                informeEgresoDTO.setTokenIdentificadorEmpresa(informeEgreso.getEmpresa().getTokenIdentificador());
                informeEgresoDTO.setFechaCreacion(informeEgreso.getFechaCreacion());

                if(informeEgreso.getInformeSeguimiento() != null) {
                    informeEgresoDTO.setTokenIdentificadorInformeSeguimientoPII(
                        informeEgreso.getInformeSeguimiento().getTokenIdentificador());
                }

                informeEgresoDTO.setMotivoIngresoPII(informeEgreso.getMotivoIngresoPII());
                informeEgresoDTO.setDescripcionPsicologicaPlanTratamiento(informeEgreso.getDescripcionPsicologicaPlanTratamiento());
                informeEgresoDTO.setDescripcionSocialPlanTratamiento(informeEgreso.getDescripcionSocialPlanTratamiento());
                informeEgresoDTO.setDescripcionConductualPlanTratamiento(informeEgreso.getDescripcionConductualPlanTratamiento());
                informeEgresoDTO.setDescripcionFamiliarPlanTratamiento(informeEgreso.getDescripcionFamiliarPlanTratamiento());
                informeEgresoDTO.setDescripcionNivelRiesgoPlanTratamiento(informeEgreso.getDescripcionNivelRiesgoPlanTratamiento());
                informeEgresoDTO.setDescripcionEvolucionPsicologicaPlanTratamiento(informeEgreso.getDescripcionEvolucionPsicologicaPlanTratamiento());
                informeEgresoDTO.setDescripcionEvolucionSocialPlanTratamiento(informeEgreso.getDescripcionEvolucionSocialPlanTratamiento());
                informeEgresoDTO.setDescripcionEvolucionConductualPlanTratamiento(informeEgreso.getDescripcionEvolucionConductualPlanTratamiento());
                informeEgresoDTO.setDescripcionEvolucionFamiliarPlanTratamiento(informeEgreso.getDescripcionEvolucionFamiliarPlanTratamiento());
                informeEgresoDTO.setDescripcionEvolucionNivelRiesgoPlanTratamiento(informeEgreso.getDescripcionEvolucionNivelRiesgoPlanTratamiento());
                informeEgresoDTO.setConclusiones(informeEgreso.getConclusiones());
                informeEgresoDTO.setRecomendaciones(informeEgreso.getRecomendaciones());

                informeEgresoDTO.setNombreCompletoUsuarioCreacion(
                    informeEgreso.getUsuarioSistemaCrea().getNombres() + " " + 
                    informeEgreso.getUsuarioSistemaCrea().getApellidos());

                if (informeEgreso.getFichaIdentificacion() != null) {
                    informeEgresoDTO.setTokenIdentificadorFichaIdentificacion(informeEgreso.getFichaIdentificacion().getTokenIdentificador());
                }

                informeEgresoDTOList.add(informeEgresoDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            informeEgresoDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(informeEgresoDTOList);
            paginacionResponse.setTotalItems(informeEgresoPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + informeEgresoPage.getTotalElements() + " informes de egreso PII";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + informeEgresoPage.getTotalElements() + " registros de informes de egreso";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Determina si el campo requiere ordenamiento especial
     * @param campo Campo a verificar
     * @return true si requiere ordenamiento especial
     */
    private boolean esOrdenamientoEspecial(String campo) {
        return "nombreCompletoUsuarioCreacion".equals(campo);
    }

    /**
     * Obtiene los datos con ordenamiento especial según el campo
     * @param tokenIdentificador Token de la ficha
     * @param idEmpresa ID de la empresa
     * @param filtro Filtro de búsqueda
     * @param campo Campo por el que ordenar
     * @param direccion Dirección del ordenamiento (ASC/DESC)
     * @param pageable Configuración de paginación
     * @return Page con los resultados ordenados
     */
    private Page<InformeEgresoPII> obtenerConOrdenamientoEspecial(
            String tokenIdentificador, 
            Long idEmpresa, 
            String filtro, 
            String campo, 
            String direccion, 
            Pageable pageable) {

        boolean esAscendente = "ASC".equals(direccion);
        boolean tieneFiltro = !filtro.isEmpty();

        switch (campo) {
            case "nombreCompletoUsuarioCreacion":
                if (tieneFiltro) {
                    return esAscendente ? 
                        informeEgresoPIIRepository.buscarConFiltroOrdenadoPorUsuarioCreacionAsc(tokenIdentificador, idEmpresa, filtro, false, pageable) :
                        informeEgresoPIIRepository.buscarConFiltroOrdenadoPorUsuarioCreacionDesc(tokenIdentificador, idEmpresa, filtro, false, pageable);
                } else {
                    return esAscendente ?
                        informeEgresoPIIRepository.buscarOrdenadoPorUsuarioCreacionAsc(tokenIdentificador, idEmpresa, false, pageable) :
                        informeEgresoPIIRepository.buscarOrdenadoPorUsuarioCreacionDesc(tokenIdentificador, idEmpresa, false, pageable);
                }

            default:
                // Fallback a búsqueda normal
                return informeEgresoPIIRepository.buscarPorFiltro(tokenIdentificador, idEmpresa, filtro, false, pageable);
        }
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
            case "motivoIngresoPII":
                return "motivoIngresoPII";
            case "conclusiones":
                return "conclusiones";
            case "recomendaciones":
                return "recomendaciones";
            case "numero":
                // Para ordenamiento por número, usar el ID (orden inverso)
                return "idInformeEgresoPII";
            // Campo nombreCompletoUsuarioCreacion NO se mapea aquí
            // porque se maneja con ordenamiento especial
            default:
                // Si no se encuentra mapeo, usar fechaCreacion como default
                return "fechaCreacion";
        }
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformeEgresoPIIDTO> crearInformeEgreso(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InformeEgresoPIIDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            InformeEgresoPIIDTO informeEgresoDTO = new Gson()
                .fromJson(bodyString, InformeEgresoPIIDTO.class);

            informeEgresoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = informeEgresoDTO.getTokenIdentificadorFichaIdentificacion() + "-informeEgresoPII";

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
                    .findByTokenIdentificadorAndRemovido(informeEgresoDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);

                InformeEgresoPII informeEgreso;
                boolean esEdicion = false;

                if(informeEgresoDTO.getEsEdicion() != null && informeEgresoDTO.getEsEdicion()){
                    informeEgreso = informeEgresoPIIRepository
                        .findByTokenIdentificadorAndRemovido(informeEgresoDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (informeEgreso == null) {
                        df.setMensaje("El informe de egreso a editar no existe o ya fue eliminado anteriormente");
                        return df;
                    }
                    informeEgreso.setFechaEdicion(new Date());
                    informeEgreso.setIpEdita(ip);
                    informeEgreso.setUsuarioSistemaEdita(usuarioLogin);
                    esEdicion = true;
                } else {                
                    informeEgreso = new InformeEgresoPII();
                    informeEgreso.setFechaCreacion(new Date());
                    informeEgreso.setIpCrea(ip);
                    informeEgreso.setUsuarioSistemaCrea(usuarioLogin);
                    informeEgreso.setEmpresa(empresa);
                    informeEgreso.setFichaIdentificacion(fichaIdentificacion);

                    if (informeEgresoDTO.getTokenIdentificadorInformeSeguimientoPII() != null) {
                        InformeSeguimientoPII informeSeguimiento = informeSeguimientoPIIRepository
                            .findByTokenIdentificadorAndRemovido(informeEgresoDTO.getTokenIdentificadorInformeSeguimientoPII(), Boolean.FALSE);
                        informeEgreso.setInformeSeguimiento(informeSeguimiento);
                    }
                }

                informeEgreso.setMotivoIngresoPII(informeEgresoDTO.getMotivoIngresoPII());
                informeEgreso.setDescripcionPsicologicaPlanTratamiento(informeEgresoDTO.getDescripcionPsicologicaPlanTratamiento());
                informeEgreso.setDescripcionSocialPlanTratamiento(informeEgresoDTO.getDescripcionSocialPlanTratamiento());
                informeEgreso.setDescripcionConductualPlanTratamiento(informeEgresoDTO.getDescripcionConductualPlanTratamiento());
                informeEgreso.setDescripcionFamiliarPlanTratamiento(informeEgresoDTO.getDescripcionFamiliarPlanTratamiento());
                informeEgreso.setDescripcionNivelRiesgoPlanTratamiento(informeEgresoDTO.getDescripcionNivelRiesgoPlanTratamiento());
                informeEgreso.setDescripcionEvolucionPsicologicaPlanTratamiento(informeEgresoDTO.getDescripcionEvolucionPsicologicaPlanTratamiento());
                informeEgreso.setDescripcionEvolucionSocialPlanTratamiento(informeEgresoDTO.getDescripcionEvolucionSocialPlanTratamiento());
                informeEgreso.setDescripcionEvolucionConductualPlanTratamiento(informeEgresoDTO.getDescripcionEvolucionConductualPlanTratamiento());
                informeEgreso.setDescripcionEvolucionFamiliarPlanTratamiento(informeEgresoDTO.getDescripcionEvolucionFamiliarPlanTratamiento());
                informeEgreso.setDescripcionEvolucionNivelRiesgoPlanTratamiento(informeEgresoDTO.getDescripcionEvolucionNivelRiesgoPlanTratamiento());
                informeEgreso.setConclusiones(informeEgresoDTO.getConclusiones());
                informeEgreso.setRecomendaciones(informeEgresoDTO.getRecomendaciones());

                informeEgreso = this.informeEgresoPIIRepository.save(informeEgreso);
                informeEgresoDTO.setTokenIdentificador(informeEgreso.getTokenIdentificador());

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito el informe de egreso PII de " + nombresCompletos;
                
                // Mensaje para auditoría (información detallada del informe y DNI)
                String mensajeInforme = construirMensajeInforme(informeEgreso);
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                String mensajeAuditoria = "Se " + accion + " con éxito " + mensajeInforme + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, informeEgresoDTO, mensajeAuditoria);

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
    public RespuestaPorDefectoAuditoria<Boolean> eliminarInformeEgreso(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            InformeEgresoPIIDTO informeEgresoDTO = new Gson()
                .fromJson(bodyString, InformeEgresoPIIDTO.class);

            InformeEgresoPII informeEgreso = this.informeEgresoPIIRepository
                .findByTokenIdentificadorAndRemovido(informeEgresoDTO.getTokenIdentificador(), false);

            if (informeEgreso == null) {
                df.setMensaje("El informe de egreso no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(informeEgreso.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito el informe de egreso PII de " + nombresCompletos;

            // Mensaje para auditoría (información detallada del informe y DNI)
            String mensajeInforme = construirMensajeInforme(informeEgreso);
            String identificacionPersona = obtenerIdentificacionPersona(informeEgreso.getFichaIdentificacion());
            String mensajeAuditoria = "Se eliminó con éxito " + mensajeInforme + identificacionPersona;

            Date fecha = new Date();
            informeEgreso.setRemovido(true);
            informeEgreso.setIpElimina(ip);
            informeEgreso.setUsuarioSistemaElimina(usuarioSistemaLogin);
            informeEgreso.setFechaEliminacion(fecha);

            this.informeEgresoPIIRepository.save(informeEgreso);

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Método auxiliar para construir el mensaje con información del informe de egreso
     */
    private String construirMensajeInforme(InformeEgresoPII informe) {
        StringBuilder mensaje = new StringBuilder();
        
        // Agregar base del informe
        mensaje.append("el informe de egreso PII");
        
        // Agregar motivo de ingreso al PII
        if (informe.getMotivoIngresoPII() != null && !informe.getMotivoIngresoPII().trim().isEmpty()) {
            mensaje.append(" motivo de ingreso al PII: ").append(informe.getMotivoIngresoPII()).append(",");
        }
        
        // Agregar conclusiones si existen
        if (informe.getConclusiones() != null && !informe.getConclusiones().trim().isEmpty()) {
            mensaje.append(" conclusiones: ").append(informe.getConclusiones()).append(",");
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