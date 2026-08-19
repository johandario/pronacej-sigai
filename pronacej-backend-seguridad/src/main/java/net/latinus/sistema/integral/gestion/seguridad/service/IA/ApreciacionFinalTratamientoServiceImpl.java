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
import net.latinus.sistema.integral.gestion.seguridad.entities.SituacionActualAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.entities.FactoresPresentes;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ApreciacionFinalTratamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionActualAdolescenteDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FactoresPresentesDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SituacionActualAdolescenteRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FactoresPresentesRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class ApreciacionFinalTratamientoServiceImpl implements ApreciacionFinalTratamientoService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private SituacionActualAdolescenteRepository situacionActualRepository;
    private FactoresPresentesRepository factoresPresentesRepository;
    // Variable para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ApreciacionFinalTratamientoDTO>> obtenerApreciacionesFinalesPaginado(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<ApreciacionFinalTratamientoDTO>> df = new RespuestaPorDefectoAuditoria<>();

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

            // Obtener todos los registros sin paginación
            List<SituacionActualAdolescente> situaciones = this.situacionActualRepository
                .findByFichaIdentificacionTokenIdentificadorAndRemovido(
                    paginacionRequest.getTokenIdentificador(), false);

            List<ApreciacionFinalTratamientoDTO> apreciacionFinalesDTOList = new ArrayList<>();

            for (SituacionActualAdolescente situacion : situaciones) {
                ApreciacionFinalTratamientoDTO apreciacionFinalDTO = new ApreciacionFinalTratamientoDTO();
                apreciacionFinalDTO.setTokenIdentificador(situacion.getTokenIdentificador());
                apreciacionFinalDTO.setTokenIdentificadorFichaIdentificacion(situacion.getFichaIdentificacion().getTokenIdentificador());
                apreciacionFinalDTO.setFechaRegistro(situacion.getFechaCreacion());
                apreciacionFinalDTO.setNombreCompletoUsuarioCreacion(
                    situacion.getUsuarioSistemaCrea().getNombres() + " " + 
                    situacion.getUsuarioSistemaCrea().getApellidos());

                // Obtener situaciones relacionadas
                List<SituacionActualAdolescente> situacionesRelacionadas = this.situacionActualRepository
                    .findByFichaIdentificacionTokenIdentificadorAndRemovido(
                        situacion.getFichaIdentificacion().getTokenIdentificador(), false);

                List<SituacionActualAdolescenteDTO> situacionesDTOList = new ArrayList<>();
                for (SituacionActualAdolescente s : situacionesRelacionadas) {
                    if (mismaFechaCreacion(s.getFechaCreacion(), situacion.getFechaCreacion())) {
                        SituacionActualAdolescenteDTO situacionDTO = new SituacionActualAdolescenteDTO();
                        situacionDTO.setTokenIdentificador(s.getTokenIdentificador());
                        situacionDTO.setTokenIdentificadorFichaIdentificacion(s.getFichaIdentificacion().getTokenIdentificador());
                        situacionDTO.setTokenIdentificadorTipoArea(s.getTipoArea().getTokenIdentificador());
                        situacionDTO.setTokenIdentificadorTipoSituacion(s.getTipoSituacion().getTokenIdentificador());
                        situacionDTO.setDescripcion(s.getDescripcion());
                        situacionDTO.setObservacion(s.getObservacion());

                        situacionesDTOList.add(situacionDTO);
                    }
                }
                apreciacionFinalDTO.setListaSituaciones(situacionesDTOList);

                // Obtener factores relacionados
                List<FactoresPresentes> factoresRelacionados = this.factoresPresentesRepository
                    .findByFichaIdentificacionTokenIdentificadorAndRemovido(
                        situacion.getFichaIdentificacion().getTokenIdentificador(), false);

                List<FactoresPresentesDTO> factoresDTOList = new ArrayList<>();
                for (FactoresPresentes f : factoresRelacionados) {
                    if (mismaFechaCreacion(f.getFechaCreacion(), situacion.getFechaCreacion())) {
                        FactoresPresentesDTO factorDTO = new FactoresPresentesDTO();
                        factorDTO.setTokenIdentificador(f.getTokenIdentificador());
                        factorDTO.setTokenIdentificadorFichaIdentificacion(f.getFichaIdentificacion().getTokenIdentificador());
                        factorDTO.setFactoresProtectores(f.getFactoresProtectores());
                        factorDTO.setFactoresRiesgo(f.getFactoresRiesgo());

                        factoresDTOList.add(factorDTO);
                    }
                }
                apreciacionFinalDTO.setListaFactoresPresentes(factoresDTOList);

                if (situacion.getFechaCreacion() != null) {
                    apreciacionFinalDTO.setFechaCreacion(situacion.getFechaCreacion());
                }

                apreciacionFinalesDTOList.add(apreciacionFinalDTO);
            }

            apreciacionFinalesDTOList.sort((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()));

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            apreciacionFinalesDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            // Aplicar filtrado, ordenamiento y paginación en memoria
            PaginacionResponse<ApreciacionFinalTratamientoDTO> paginacionResponse = 
                paginacionService.obtenerDatos(apreciacionFinalesDTOList, paginacionRequest);

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + paginacionResponse.getTotalItems() + " apreciaciones finales del tratamiento";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + paginacionResponse.getTotalItems() + " registros de apreciaciones finales";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ApreciacionFinalTratamientoDTO> crearApreciacionFinal(
        HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoMenu) {

        RespuestaPorDefectoAuditoria<ApreciacionFinalTratamientoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            ApreciacionFinalTratamientoDTO apreciacionDTO = new Gson()
                .fromJson(bodyString, ApreciacionFinalTratamientoDTO.class);

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = apreciacionDTO.getTokenIdentificadorFichaIdentificacion() + "-apreciacionFinal";

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

                // Validar que exista la ficha de identificación
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository
                    .findByTokenIdentificadorAndRemovido(apreciacionDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);

                if (fichaIdentificacion == null) {
                    df.setMensaje("La ficha de identificación no existe o ya fue eliminada anteriormente");
                    return df;
                }

                boolean esEdicion = false;

                // Si es edición, eliminar todos los registros previos
                if (apreciacionDTO.getEsEdicion()) {
                    eliminarRegistrosPrevios(apreciacionDTO.getTokenIdentificador(), usuarioLogin, ip);
                    esEdicion = true;
                }

                // Procesar y guardar las situaciones
                Date fechaActual = new Date();
                List<SituacionActualAdolescenteDTO> situacionesDTO = apreciacionDTO.getListaSituaciones();
                List<SituacionActualAdolescenteDTO> situacionesGuardadas = new ArrayList<>();

                if (situacionesDTO != null && !situacionesDTO.isEmpty()) {
                    for (SituacionActualAdolescenteDTO situacionDTO : situacionesDTO) {
                        SituacionActualAdolescente situacion = new SituacionActualAdolescente();

                        situacion.setFichaIdentificacion(fichaIdentificacion);
                        situacion.setEmpresa(empresa);

                        // Buscar catálogos para tipo área y tipo situación
                        Catalogo tipoArea = catalogoRepository
                            .findByTokenIdentificadorAndRemovido(situacionDTO.getTokenIdentificadorTipoArea(), Boolean.FALSE);
                        situacion.setTipoArea(tipoArea);

                        Catalogo tipoSituacion = catalogoRepository
                            .findByTokenIdentificadorAndRemovido(situacionDTO.getTokenIdentificadorTipoSituacion(), Boolean.FALSE);
                        situacion.setTipoSituacion(tipoSituacion);

                        situacion.setDescripcion(situacionDTO.getDescripcion());
                        situacion.setObservacion(situacionDTO.getObservacion());

                        // Datos de auditoría
                        situacion.setFechaCreacion(fechaActual);
                        situacion.setIpCrea(ip);
                        situacion.setUsuarioSistemaCrea(usuarioLogin);

                        // Guardar la situación
                        situacion = situacionActualRepository.save(situacion);

                        // Actualizar el DTO con el token generado
                        situacionDTO.setTokenIdentificador(situacion.getTokenIdentificador());
                        situacionesGuardadas.add(situacionDTO);
                    }
                }

                // Procesar y guardar los factores
                List<FactoresPresentesDTO> factoresDTO = apreciacionDTO.getListaFactoresPresentes();
                List<FactoresPresentesDTO> factoresGuardados = new ArrayList<>();

                if (factoresDTO != null && !factoresDTO.isEmpty()) {
                    for (FactoresPresentesDTO factorDTO : factoresDTO) {
                        FactoresPresentes factor = new FactoresPresentes();

                        factor.setFichaIdentificacion(fichaIdentificacion);
                        factor.setEmpresa(empresa);

                        factor.setFactoresProtectores(factorDTO.getFactoresProtectores());
                        factor.setFactoresRiesgo(factorDTO.getFactoresRiesgo());

                        // Datos de auditoría
                        factor.setFechaCreacion(fechaActual);
                        factor.setIpCrea(ip);
                        factor.setUsuarioSistemaCrea(usuarioLogin);

                        // Guardar el factor
                        factor = factoresPresentesRepository.save(factor);

                        // Actualizar el DTO con el token generado
                        factorDTO.setTokenIdentificador(factor.getTokenIdentificador());
                        factoresGuardados.add(factorDTO);
                    }
                }

                // Actualizar el DTO de respuesta
                apreciacionDTO.setListaSituaciones(situacionesGuardadas);
                apreciacionDTO.setListaFactoresPresentes(factoresGuardados);

                // Si hay al menos una situación, usar su token como identificador de la apreciación
                if (!situacionesGuardadas.isEmpty()) {
                    apreciacionDTO.setTokenIdentificador(situacionesGuardadas.get(0).getTokenIdentificador());
                } else if (!factoresGuardados.isEmpty()) {
                    // Si no hay situaciones pero hay factores, usar el token del primer factor
                    apreciacionDTO.setTokenIdentificador(factoresGuardados.get(0).getTokenIdentificador());
                }

                // Establecer fecha de registro
                apreciacionDTO.setFechaRegistro(fechaActual);

                // Establecer nombre de usuario que registró
                apreciacionDTO.setNombreCompletoUsuarioCreacion(
                    usuarioLogin.getNombres() + " " + usuarioLogin.getApellidos());

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito la apreciación final del tratamiento de " + nombresCompletos;
                
                // Mensaje para auditoría (información detallada de la apreciación y DNI)
                String mensajeApreciacion = construirMensajeApreciacion(situacionesGuardadas, factoresGuardados);
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                String mensajeAuditoria = "Se " + accion + " con éxito " + mensajeApreciacion + identificacionPersona;

                df.llenarRespuestaExitosa(mensajeUsuario, apreciacionDTO, mensajeAuditoria);
            } finally {
                // Siempre eliminar el token de procesamiento cuando se complete
                solicitudesEnProcesamiento.remove(idSolicitud);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private void eliminarRegistrosPrevios(String tokenReferencia, UsuarioSistema usuario, String ip) {
        SituacionActualAdolescente situacionReferencia = situacionActualRepository
            .findByTokenIdentificadorAndRemovido(tokenReferencia, false);
        
        if (situacionReferencia != null) {
            Date fechaCreacion = situacionReferencia.getFechaCreacion();
            String tokenFichaIdentificacion = situacionReferencia.getFichaIdentificacion().getTokenIdentificador();
            
            // Eliminar situaciones previas
            List<SituacionActualAdolescente> situaciones = situacionActualRepository
                .findByFichaIdentificacionTokenIdentificadorAndRemovido(tokenFichaIdentificacion, false);
            
            Date fechaEliminacion = new Date();
            for (SituacionActualAdolescente s : situaciones) {
                if (mismaFechaCreacion(s.getFechaCreacion(), fechaCreacion)) {
                    s.setRemovido(true);
                    s.setIpElimina(ip);
                    s.setUsuarioSistemaElimina(usuario);
                    s.setFechaEliminacion(fechaEliminacion);
                    situacionActualRepository.save(s);
                }
            }
            
            // Eliminar factores previos
            List<FactoresPresentes> factores = factoresPresentesRepository
                .findByFichaIdentificacionTokenIdentificadorAndRemovido(tokenFichaIdentificacion, false);
            
            for (FactoresPresentes f : factores) {
                if (mismaFechaCreacion(f.getFechaCreacion(), fechaCreacion)) {
                    f.setRemovido(true);
                    f.setIpElimina(ip);
                    f.setUsuarioSistemaElimina(usuario);
                    f.setFechaEliminacion(fechaEliminacion);
                    factoresPresentesRepository.save(f);
                }
            }
        }
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarApreciacionFinal(
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

            ApreciacionFinalTratamientoDTO apreciacionDTO = new Gson()
                .fromJson(bodyString, ApreciacionFinalTratamientoDTO.class);

            // Buscar la situación que sirve como referencia
            SituacionActualAdolescente situacionReferencia = situacionActualRepository
                .findByTokenIdentificadorAndRemovido(apreciacionDTO.getTokenIdentificador(), false);
            
            if (situacionReferencia == null) {
                df.setMensaje("La apreciación final no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }
            
            Date fechaCreacion = situacionReferencia.getFechaCreacion();
            String tokenFichaIdentificacion = situacionReferencia.getFichaIdentificacion().getTokenIdentificador();
            
            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(situacionReferencia.getFichaIdentificacion());
            
            // Buscar todas las situaciones relacionadas y eliminarlas
            List<SituacionActualAdolescente> situaciones = situacionActualRepository
                .findByFichaIdentificacionTokenIdentificadorAndRemovido(tokenFichaIdentificacion, false);
            
            Date fechaActual = new Date();
            int contadorEliminados = 0;
            
            for (SituacionActualAdolescente s : situaciones) {
                if (mismaFechaCreacion(s.getFechaCreacion(), fechaCreacion)) {
                    s.setRemovido(true);
                    s.setIpElimina(ip);
                    s.setUsuarioSistemaElimina(usuarioSistemaLogin);
                    s.setFechaEliminacion(fechaActual);
                    
                    situacionActualRepository.save(s);
                    contadorEliminados++;
                }
            }
            
            // Buscar todos los factores relacionados y eliminarlos
            List<FactoresPresentes> factores = factoresPresentesRepository
                .findByFichaIdentificacionTokenIdentificadorAndRemovido(tokenFichaIdentificacion, false);
            
            for (FactoresPresentes f : factores) {
                if (mismaFechaCreacion(f.getFechaCreacion(), fechaCreacion)) {
                    f.setRemovido(true);
                    f.setIpElimina(ip);
                    f.setUsuarioSistemaElimina(usuarioSistemaLogin);
                    f.setFechaEliminacion(fechaActual);
                    
                    factoresPresentesRepository.save(f);
                    contadorEliminados++;
                }
            }
            
            if (contadorEliminados == 0) {
                df.setMensaje("No se encontraron registros para eliminar");
                return df;
            }

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la apreciación final del tratamiento de " + nombresCompletos;

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(situacionReferencia.getFichaIdentificacion());
            String mensajeAuditoria = "Se eliminó con éxito la apreciación final del tratamiento" + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSituacion(
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

            SituacionActualAdolescenteDTO situacionDTO = new Gson()
                .fromJson(bodyString, SituacionActualAdolescenteDTO.class);

            SituacionActualAdolescente situacion = this.situacionActualRepository
                .findByTokenIdentificadorAndRemovido(situacionDTO.getTokenIdentificador(), false);

            if (situacion == null) {
                df.setMensaje("La situación no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            situacion.setRemovido(true);
            situacion.setIpElimina(ip);
            situacion.setUsuarioSistemaElimina(usuarioSistemaLogin);
            situacion.setFechaEliminacion(fecha);

            this.situacionActualRepository.save(situacion);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la situación del adolescente";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se ha eliminado con éxito la situación del adolescente";

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarFactor(
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

            FactoresPresentesDTO factorDTO = new Gson()
                .fromJson(bodyString, FactoresPresentesDTO.class);

            FactoresPresentes factor = this.factoresPresentesRepository
                .findByTokenIdentificadorAndRemovido(factorDTO.getTokenIdentificador(), false);

            if (factor == null) {
                df.setMensaje("El factor no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            Date fecha = new Date();
            factor.setRemovido(true);
            factor.setIpElimina(ip);
            factor.setUsuarioSistemaElimina(usuarioSistemaLogin);
            factor.setFechaEliminacion(fecha);

            this.factoresPresentesRepository.save(factor);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito el factor";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se ha eliminado con éxito el factor";

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    private boolean mismaFechaCreacion(Date fecha1, Date fecha2) {
        if (fecha1 == null || fecha2 == null) {
            return false;
        }
        
        // Considerar solo día, mes y año para la comparación
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTime(fecha1);
        
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTime(fecha2);
        
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
               cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH);
    }

    /**
     * Método auxiliar para construir el mensaje con información de la apreciación final
     */
    private String construirMensajeApreciacion(List<SituacionActualAdolescenteDTO> situaciones, List<FactoresPresentesDTO> factores) {
        StringBuilder mensaje = new StringBuilder();
        
        // Agregar base de la apreciación
        mensaje.append("la apreciación final del tratamiento");
        
        // Agregar información sobre situaciones
        if (situaciones != null && !situaciones.isEmpty()) {
            mensaje.append(" con ").append(situaciones.size()).append(" situación(es) registrada(s)");
        }
        
        // Agregar información sobre factores
        if (factores != null && !factores.isEmpty()) {
            if (situaciones != null && !situaciones.isEmpty()) {
                mensaje.append(" y ");
            } else {
                mensaje.append(" con ");
            }
            mensaje.append(factores.size()).append(" factor(es) presente(s)");
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
