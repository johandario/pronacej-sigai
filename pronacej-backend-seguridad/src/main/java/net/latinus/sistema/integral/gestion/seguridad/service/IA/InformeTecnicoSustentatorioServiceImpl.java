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
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.InformeTecnicoSustentatorio;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeTecnicoSustentatorioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InformeTecnicoSustentatorioRepository;
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
public class InformeTecnicoSustentatorioServiceImpl implements InformeTecnicoSustentatorioService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private InformeTecnicoSustentatorioRepository informeTecnicoRepository;
    // Variable para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeTecnicoSustentatorioDTO>> obtenerInformesTecnicosPaginado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeTecnicoSustentatorioDTO>> df = new RespuestaPorDefectoAuditoria<>();

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

            // Configuración de ordenamiento
            String sortField = paginacionRequest.getSort();
            String direction = paginacionRequest.getDirection();

            Sort sort = Sort.by("idInformeTecnicoSustentatorio").descending();

            if (sortField != null && !sortField.isEmpty() && direction != null && !direction.isEmpty()) {
                // Mapeo de campos para ordenamiento
                if ("usuarioRegistro".equals(sortField)) {
                    sortField = "usuarioSistemaCrea.nombres";
                } else if ("fechaCreacion".equals(sortField)) {
                    sortField = "fechaCreacion";
                } else if ("motivo".equals(sortField)) {
                    sortField = "motivo";
                } else if ("duracionMostrar".equals(sortField) || "duracion".equals(sortField)) {
                    sortField = "duracion";
                } else if ("criteriosSeleccion".equals(sortField)) {
                    sortField = "criteriosSeleccion";
                }

                if ("asc".equalsIgnoreCase(direction)) {
                    sort = Sort.by(sortField).ascending();
                } else {
                    sort = Sort.by(sortField).descending();
                }
            }

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    sort
            );

            // Aplicar filtro si se proporciona
            String filtro = paginacionRequest.getFilter();
            Page<InformeTecnicoSustentatorio> informePage;

            if (filtro != null && !filtro.isEmpty()) {
                // Preparar filtro para búsqueda LIKE
                String filtroLike = "%" + filtro.toLowerCase() + "%";

                // Usar el método de búsqueda con filtro
                informePage = this.informeTecnicoRepository.buscarPorFiltroGeneral(
                        paginacionRequest.getTokenIdentificador(),
                        empresa.getIdEmpresa(),
                        filtroLike,
                        pageable
                );
            } else {
                // Sin filtro, usar el método normal
                informePage = this.informeTecnicoRepository.buscarPorFiltro(
                        paginacionRequest.getTokenIdentificador(), 
                        empresa.getIdEmpresa(), 
                        "", 
                        pageable);
            }

            PaginacionResponse<InformeTecnicoSustentatorioDTO> paginacionResponse = new PaginacionResponse<>();
            List<InformeTecnicoSustentatorioDTO> informeDTOList = new ArrayList<>();

            for (InformeTecnicoSustentatorio informe : informePage.getContent()) {
                InformeTecnicoSustentatorioDTO informeDTO = new InformeTecnicoSustentatorioDTO();
                informeDTO.setTokenIdentificador(informe.getTokenIdentificador());
                informeDTO.setTokenIdentificadorEmpresa(informe.getEmpresa().getTokenIdentificador());
                informeDTO.setMotivo(informe.getMotivo());
                informeDTO.setCriteriosSeleccion(informe.getCriteriosSeleccion());
                informeDTO.setAnalisisPsicologico(informe.getAnalisisPsicologico());
                informeDTO.setAnalisisSocial(informe.getAnalisisSocial());
                informeDTO.setAnalisisConductual(informe.getAnalisisConductual());
                informeDTO.setAnalisisFamiliar(informe.getAnalisisFamiliar());
                informeDTO.setPropuestaActividadFormativa(informe.getPropuestaActividadFormativa());
                informeDTO.setImportanciaParticipacionAdolescente(informe.getImportanciaParticipacionAdolescente());
                informeDTO.setObjetivosConseguir(informe.getObjetivosConseguir());
                informeDTO.setDuracion(informe.getDuracion());
                informeDTO.setConclusiones(informe.getConclusiones());
                informeDTO.setRecomendaciones(informe.getRecomendaciones());
                informeDTO.setFechaCreacion(informe.getFechaCreacion());

                if (informe.getFichaIdentificacion() != null) {
                    informeDTO.setTokenIdentificadorFichaIdentificacion(informe.getFichaIdentificacion().getTokenIdentificador());
                }

                // Agregar el nombre completo del usuario que creó el registro
                if (informe.getUsuarioSistemaCrea() != null) {
                    informeDTO.setNombreCompletoUsuarioCreacion(
                        informe.getUsuarioSistemaCrea().getNombres() + " " + 
                        informe.getUsuarioSistemaCrea().getApellidos()
                    );
                } else {
                    informeDTO.setNombreCompletoUsuarioCreacion("No especificado");
                }

                informeDTOList.add(informeDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            informeDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(informeDTOList);
            paginacionResponse.setTotalItems(informePage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + informePage.getTotalElements() + " informes técnicos sustentatorios";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + informePage.getTotalElements() + " registros de informes técnicos";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformeTecnicoSustentatorioDTO> crearInformeTecnico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InformeTecnicoSustentatorioDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            InformeTecnicoSustentatorioDTO informeDTO = new Gson().fromJson(bodyString, InformeTecnicoSustentatorioDTO.class);
            informeDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = informeDTO.getTokenIdentificadorFichaIdentificacion() + "-informeTecnico";

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
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                    informeDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);

                InformeTecnicoSustentatorio informe;
                boolean esEdicion = false;

                if (informeDTO.getEsEdicion() != null && informeDTO.getEsEdicion()) {
                    informe = informeTecnicoRepository.findByTokenIdentificadorAndRemovido(
                        informeDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (informe == null) {
                        df.setMensaje("El informe técnico a editar no existe o ya fue eliminado anteriormente");
                        return df;
                    }
                    informe.setFechaEdicion(new Date());
                    informe.setIpEdita(ip);
                    informe.setUsuarioSistemaEdita(usuarioLogin);
                    esEdicion = true;
                } else {
                    informe = new InformeTecnicoSustentatorio();
                    informe.setFechaCreacion(new Date());
                    informe.setIpCrea(ip);
                    informe.setUsuarioSistemaCrea(usuarioLogin);
                    informe.setEmpresa(empresa);
                    informe.setFichaIdentificacion(fichaIdentificacion);
                }

                informe.setMotivo(informeDTO.getMotivo());
                informe.setCriteriosSeleccion(informeDTO.getCriteriosSeleccion());
                informe.setAnalisisPsicologico(informeDTO.getAnalisisPsicologico());
                informe.setAnalisisSocial(informeDTO.getAnalisisSocial());
                informe.setAnalisisConductual(informeDTO.getAnalisisConductual());
                informe.setAnalisisFamiliar(informeDTO.getAnalisisFamiliar());
                informe.setPropuestaActividadFormativa(informeDTO.getPropuestaActividadFormativa());
                informe.setImportanciaParticipacionAdolescente(informeDTO.getImportanciaParticipacionAdolescente());
                informe.setObjetivosConseguir(informeDTO.getObjetivosConseguir());
                informe.setDuracion(informeDTO.getDuracion());
                informe.setConclusiones(informeDTO.getConclusiones());
                informe.setRecomendaciones(informeDTO.getRecomendaciones());

                informe = this.informeTecnicoRepository.save(informe);
                informeDTO.setTokenIdentificador(informe.getTokenIdentificador());

                // Establecer la fecha de creación en el DTO para devolverla al frontend
                informeDTO.setFechaCreacion(informe.getFechaCreacion());

                // Establecer el nombre completo del usuario que creó el registro
                if (informe.getUsuarioSistemaCrea() != null) {
                    informeDTO.setNombreCompletoUsuarioCreacion(
                        informe.getUsuarioSistemaCrea().getNombres() + " " + 
                        informe.getUsuarioSistemaCrea().getApellidos()
                    );
                }

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito el informe técnico sustentatorio de " + nombresCompletos;
                
                // Mensaje para auditoría (información detallada del informe y DNI)
                String mensajeInforme = construirMensajeInforme(informe);
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
    public RespuestaPorDefectoAuditoria<Boolean> eliminarInformeTecnico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            InformeTecnicoSustentatorioDTO informeDTO = new Gson().fromJson(bodyString, InformeTecnicoSustentatorioDTO.class);

            InformeTecnicoSustentatorio informe = this.informeTecnicoRepository.findByTokenIdentificadorAndRemovido(
                    informeDTO.getTokenIdentificador(), false
            );

            if (informe == null) {
                df.setMensaje("El informe técnico no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(informe.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito el informe técnico sustentatorio de " + nombresCompletos;

            // Mensaje para auditoría (información detallada del informe y DNI)
            String mensajeInforme = construirMensajeInforme(informe);
            String identificacionPersona = obtenerIdentificacionPersona(informe.getFichaIdentificacion());
            String mensajeAuditoria = "Se eliminó con éxito " + mensajeInforme + identificacionPersona;

            informe.setRemovido(true);
            informe.setIpElimina(ip);
            informe.setUsuarioSistemaElimina(usuarioSistemaLogin);
            informe.setFechaEliminacion(new Date());

            this.informeTecnicoRepository.save(informe);

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Método auxiliar para construir el mensaje con información del informe técnico
     */
    private String construirMensajeInforme(InformeTecnicoSustentatorio informe) {
        StringBuilder mensaje = new StringBuilder();
        
        // Agregar base del informe
        mensaje.append("el informe técnico");
        
        // Agregar motivo
        if (informe.getMotivo() != null && !informe.getMotivo().trim().isEmpty()) {
            mensaje.append(" motivo: ").append(informe.getMotivo()).append(",");
        }
        
        // Agregar criterios de selección
        if (informe.getCriteriosSeleccion() != null && !informe.getCriteriosSeleccion().trim().isEmpty()) {
            mensaje.append(" criterios de selección: ").append(informe.getCriteriosSeleccion()).append(",");
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