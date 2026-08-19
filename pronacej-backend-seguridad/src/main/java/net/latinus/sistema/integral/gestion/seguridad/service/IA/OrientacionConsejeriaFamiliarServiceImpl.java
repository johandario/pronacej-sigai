package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.OrientacionConsejeriaFamiliar;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.OrientacionConsejeriaFamiliarDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.OrientacionConsejeriaPorPersonaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.OrientacionConsejeriaFamiliarRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PersonaRelacionadaRepository;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@AllArgsConstructor
public class OrientacionConsejeriaFamiliarServiceImpl implements OrientacionConsejeriaFamiliarService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private OrientacionConsejeriaFamiliarRepository orientacionConsejeriaFamiliarRepository;
    private PersonaRelacionadaRepository personaRelacionadaRepository;
    // Variable para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<OrientacionConsejeriaFamiliarDTO>> obtenerOrientacionesConsejerias(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<OrientacionConsejeriaFamiliarDTO>> df = new RespuestaPorDefectoAuditoria<>();

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

            // Obtener token de empresa para la respuesta (para auditoría)
            String tokenIdentificadorEmpresa = empresa.getTokenIdentificador();
            df.setTokenIdentificadorEmpresa(tokenIdentificadorEmpresa);

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idOrientacionConsejeriaFamiliar").descending()
            );

            Page<OrientacionConsejeriaFamiliar> orientacionPage = this.orientacionConsejeriaFamiliarRepository
                    .findByPersonaRelacionadaTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                            paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<OrientacionConsejeriaFamiliarDTO> paginacionResponse = new PaginacionResponse<>();
            List<OrientacionConsejeriaFamiliarDTO> orientacionDTOList = new ArrayList<>();

            // Obtener la persona relacionada para el mensaje
            PersonaRelacionada personaRelacionada = null;
            if (!orientacionPage.isEmpty()) {
                personaRelacionada = orientacionPage.getContent().get(0).getPersonaRelacionada();
            } else {
                // Si no hay orientaciones, buscar la persona relacionada directamente
                personaRelacionada = personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(
                        paginacionRequest.getTokenIdentificador(), Boolean.FALSE);
            }

            for (OrientacionConsejeriaFamiliar orientacion : orientacionPage.toList()) {
                OrientacionConsejeriaFamiliarDTO orientacionDTO = new OrientacionConsejeriaFamiliarDTO();
                orientacionDTO.setTokenIdentificador(orientacion.getTokenIdentificador());
                orientacionDTO.setFecha(orientacion.getFecha());
                orientacionDTO.setDescripcion(orientacion.getDescripcion());
                
                if (orientacion.getUsuarioSistemaCrea() != null) {
                    orientacionDTO.setNombreCompletoUsuarioCreacion(
                        orientacion.getUsuarioSistemaCrea().getNombres() + " " + 
                        orientacion.getUsuarioSistemaCrea().getApellidos());
                } else {
                    orientacionDTO.setNombreCompletoUsuarioCreacion("No especificado");
                }

                orientacionDTOList.add(orientacionDTO);
            }

            paginacionResponse.setData(orientacionDTOList);
            paginacionResponse.setTotalItems(orientacionPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + orientacionPage.getTotalElements() + " orientaciones y consejerías familiares";

            // Mensaje para auditoría
            String nombreCompletoPersona = obtenerNombreCompletoPersona(personaRelacionada);
            String mensajeAuditoria = "Se han encontrado un total de " + orientacionPage.getTotalElements() + " orientaciones y consejerías familiares de la persona " + nombreCompletoPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<OrientacionConsejeriaFamiliarDTO> crearOrientacionConsejeria(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<OrientacionConsejeriaFamiliarDTO> df = new RespuestaPorDefectoAuditoria<>();
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

            // Obtener token de empresa para la respuesta (para auditoría)
            String tokenIdentificadorEmpresa = empresa.getTokenIdentificador();
            df.setTokenIdentificadorEmpresa(tokenIdentificadorEmpresa);

            // Cambiar el tipo de deserialización a OrientacionConsejeriaPorPersonaDTO
            OrientacionConsejeriaPorPersonaDTO orientacionPorPersonaDTO = new Gson().fromJson(bodyString, OrientacionConsejeriaPorPersonaDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            // Generar clave única para esta solicitud
            String requestKey = orientacionPorPersonaDTO.getTokenIdentificadorPersonaRelacionada() + "_" + 
                                usuarioLogin.getIdUsuarioSistema();

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
                // Obtener la persona relacionada
                PersonaRelacionada personaRelacionada = personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(
                        orientacionPorPersonaDTO.getTokenIdentificadorPersonaRelacionada(), Boolean.FALSE);

                if (personaRelacionada == null) {
                    df.setMensaje("La persona relacionada no existe o ya fue eliminada anteriormente");
                    return df;
                }

                // Contadores para determinar el tipo de operación
                int contadorCreaciones = 0;
                int contadorEdiciones = 0;
                
                // Variables para mensajes específicos
                Date fechaOrientacion = null;
                boolean esOperacionUnica = orientacionPorPersonaDTO.getListaOrientacionesConsejerias().size() == 1;

                // Verificar y contar tipos de operaciones
                if (orientacionPorPersonaDTO.getListaOrientacionesConsejerias() != null && 
                    !orientacionPorPersonaDTO.getListaOrientacionesConsejerias().isEmpty()) {
                    
                    for (OrientacionConsejeriaFamiliarDTO orientacionDTO : orientacionPorPersonaDTO.getListaOrientacionesConsejerias()) {
                        if (orientacionDTO.getTokenIdentificador() != null && orientacionDTO.getTokenIdentificador().equals("0")) {
                            contadorCreaciones++;
                            if (fechaOrientacion == null) {
                                fechaOrientacion = orientacionDTO.getFecha();
                            }
                        } else if (orientacionDTO.getTokenIdentificador() != null && !orientacionDTO.getTokenIdentificador().equals("0")) {
                            contadorEdiciones++;
                            if (fechaOrientacion == null) {
                                fechaOrientacion = orientacionDTO.getFecha();
                            }
                        }
                    }
                }

                // Procesar cada orientación de la lista
                for (OrientacionConsejeriaFamiliarDTO orientacionDTO : orientacionPorPersonaDTO.getListaOrientacionesConsejerias()) {
                    OrientacionConsejeriaFamiliar orientacion;

                    if (orientacionDTO.getTokenIdentificador().equals("0")) {
                        orientacion = new OrientacionConsejeriaFamiliar();
                        orientacion.setFechaCreacion(new Date());
                        orientacion.setIpCrea(ip);
                        orientacion.setUsuarioSistemaCrea(usuarioLogin);
                        orientacion.setEmpresa(empresa);
                    } else {
                        orientacion = orientacionConsejeriaFamiliarRepository.findByTokenIdentificadorAndRemovido(
                                orientacionDTO.getTokenIdentificador(), Boolean.FALSE);
                        if (orientacion == null) {
                            df.setMensaje("Una de las orientaciones a editar no existe o ya fue eliminada anteriormente");
                            return df;
                        }
                        orientacion.setFechaEdicion(new Date());
                        orientacion.setIpEdita(ip);
                        orientacion.setUsuarioSistemaEdita(usuarioLogin);
                    }

                    orientacion.setFecha(orientacionDTO.getFecha());
                    orientacion.setDescripcion(orientacionDTO.getDescripcion());
                    orientacion.setPersonaRelacionada(personaRelacionada);

                    orientacion = this.orientacionConsejeriaFamiliarRepository.save(orientacion);
                    orientacionDTO.setTokenIdentificador(orientacion.getTokenIdentificador());
                }

                // Obtener nombres para los mensajes
                String nombreCompletoPersona = obtenerNombresCompletos(personaRelacionada);
                String identificacionPersona = obtenerIdentificacionPersona(personaRelacionada);
                String fechaFormateada = formatearFecha(fechaOrientacion);
                
                // Determinar mensajes según la lógica de informe de visitas
                String mensajeUsuario;
                String mensajeAuditoria;
                
                if (contadorCreaciones > 0 && contadorEdiciones > 0) {
                    // Caso mixto: creaciones + ediciones
                    mensajeUsuario = "Se procesaron con éxito " + (contadorCreaciones + contadorEdiciones) + " orientaciones/consejerías familiares de " + nombreCompletoPersona;
                    mensajeAuditoria = "Se procesaron con éxito " + contadorCreaciones + " creaciones y " + contadorEdiciones + " ediciones de orientaciones/consejerías familiares para la persona con identificación: " + identificacionPersona;
                    
                } else if (contadorCreaciones > 0) {
                    // Solo creaciones
                    if (esOperacionUnica) {
                        mensajeUsuario = "Se creó con éxito la orientación/consejería familiar del " + fechaFormateada + " de " + nombreCompletoPersona;
                        mensajeAuditoria = "Se creó con éxito la orientación/consejería familiar del " + fechaFormateada + " para la persona con identificación: " + identificacionPersona;
                    } else {
                        mensajeUsuario = "Se crearon con éxito " + contadorCreaciones + " orientaciones/consejerías familiares de " + nombreCompletoPersona;
                        mensajeAuditoria = "Se crearon con éxito " + contadorCreaciones + " orientaciones/consejerías familiares para la persona con identificación: " + identificacionPersona;
                    }
                    
                } else {
                    // Solo ediciones
                    if (esOperacionUnica) {
                        mensajeUsuario = "Se editó con éxito la orientación/consejería familiar del " + fechaFormateada + " de " + nombreCompletoPersona;
                        mensajeAuditoria = "Se editó con éxito la orientación/consejería familiar del " + fechaFormateada + " para la persona con identificación: " + identificacionPersona;
                    } else {
                        mensajeUsuario = "Se editaron con éxito " + contadorEdiciones + " orientaciones/consejerías familiares de " + nombreCompletoPersona;
                        mensajeAuditoria = "Se editaron con éxito " + contadorEdiciones + " orientaciones/consejerías familiares para la persona con identificación: " + identificacionPersona;
                    }
                }
                
                df.llenarRespuestaExitosa(mensajeUsuario, orientacionPorPersonaDTO.getListaOrientacionesConsejerias().get(0), mensajeAuditoria);
                
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
    public RespuestaPorDefectoAuditoria<Boolean> eliminarOrientacionConsejeria(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            Empresa empresa = df2.getData().getEmpresa();
            
            // Obtener token de empresa para la respuesta (para auditoría)
            String tokenIdentificadorEmpresa = empresa.getTokenIdentificador();
            df.setTokenIdentificadorEmpresa(tokenIdentificadorEmpresa);

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            OrientacionConsejeriaFamiliarDTO orientacionDTO = new Gson().fromJson(bodyString, OrientacionConsejeriaFamiliarDTO.class);

            OrientacionConsejeriaFamiliar orientacion = this.orientacionConsejeriaFamiliarRepository
                    .findByTokenIdentificadorAndRemovido(orientacionDTO.getTokenIdentificador(), false);

            if (orientacion == null) {
                df.setMensaje("La orientación no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombreCompletoPersona = obtenerNombresCompletos(orientacion.getPersonaRelacionada());
            String identificacionPersona = obtenerIdentificacionPersona(orientacion.getPersonaRelacionada());
            String fechaFormateada = formatearFecha(orientacion.getFecha());

            Date fecha = new Date();
            orientacion.setRemovido(true);
            orientacion.setIpElimina(ip);
            orientacion.setUsuarioSistemaElimina(usuarioSistemaLogin);
            orientacion.setFechaEliminacion(fecha);

            this.orientacionConsejeriaFamiliarRepository.save(orientacion);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la orientación/consejería familiar de " + nombreCompletoPersona;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito la orientación/consejería familiar del " + fechaFormateada + " de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Método auxiliar para obtener nombres completos de una persona relacionada
     */
    private String obtenerNombresCompletos(PersonaRelacionada personaRelacionada) {
        if (personaRelacionada == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (personaRelacionada.getNombres() != null && !personaRelacionada.getNombres().trim().isEmpty()) {
            nombreCompleto.append(personaRelacionada.getNombres());
        }
        if (personaRelacionada.getPrimerApellido() != null && !personaRelacionada.getPrimerApellido().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(personaRelacionada.getPrimerApellido());
        }
        if (personaRelacionada.getSegundoApellido() != null && !personaRelacionada.getSegundoApellido().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(personaRelacionada.getSegundoApellido());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una persona relacionada
     */
    private String obtenerIdentificacionPersona(PersonaRelacionada personaRelacionada) {
        if (personaRelacionada == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        if (personaRelacionada.getIdentificacion() != null && !personaRelacionada.getIdentificacion().trim().isEmpty()) {
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

    /**
     * Método auxiliar para obtener el nombre completo de una persona relacionada
     * Sigue la misma lógica que el frontend para concatenar los nombres
     */
    private String obtenerNombreCompletoPersona(PersonaRelacionada personaRelacionada) {
        if (personaRelacionada == null) {
            return "N/A";
        }

        // Si no tiene nombres pero tiene los componentes individuales
        List<String> partes = new ArrayList<>();

        if (personaRelacionada.getNombres() != null && !personaRelacionada.getNombres().trim().isEmpty()) {
            partes.add(personaRelacionada.getNombres());
        }
        if (personaRelacionada.getPrimerApellido() != null && !personaRelacionada.getPrimerApellido().trim().isEmpty()) {
            partes.add(personaRelacionada.getPrimerApellido());
        }
        if (personaRelacionada.getSegundoApellido() != null && !personaRelacionada.getSegundoApellido().trim().isEmpty()) {
            partes.add(personaRelacionada.getSegundoApellido());
        }

        if (!partes.isEmpty()) {
            return String.join(" ", partes);
        }

        // Como último recurso, usar identificación si está disponible
        if (personaRelacionada.getIdentificacion() != null && !personaRelacionada.getIdentificacion().trim().isEmpty()) {
            return personaRelacionada.getIdentificacion();
        }

        return "N/A";
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
}