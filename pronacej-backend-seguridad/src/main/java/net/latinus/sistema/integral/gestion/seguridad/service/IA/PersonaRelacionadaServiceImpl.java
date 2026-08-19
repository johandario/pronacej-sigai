package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PersonaRelacionadaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;

import org.springframework.util.ObjectUtils;

@Service
@Transactional
@AllArgsConstructor
public class PersonaRelacionadaServiceImpl implements PersonaRelacionadaService{

    @Autowired
    private PersonaRelacionadaRepository personaRelacionadaRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionPersonaRelacionadaRepository fichaIdentificacionPersonaRelacionadaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private DireccionPersonaReferenciaRepository direccionPersonaReferenciaRepository;
    private InformacionUbicacionRepository informacionUbicacionRepository;
    private EvaluacionSocialRepository evaluacionSocialRepository;
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

@Override
public RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> obtenerPersonaRelacionada(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> df = new RespuestaPorDefectoAuditoria<>();
    try {
        // Verificación de JWT
        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(true);
            return df;
        }

        // Desencriptación del body
        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();

        // Parseo de la solicitud de paginación
        PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

        // Configuración de paginación
        int page = paginacionRequest.getPage();
        int size = paginacionRequest.getSize();
        String tokenIdentificador = paginacionRequest.getTokenIdentificador();

        // Preparar respuesta
        PaginacionResponse<PersonaRelacionadaDTO> personaRelacionadaResponse = new PaginacionResponse<>();
        List<PersonaRelacionadaDTO> personaRelacionadaDTOList = new ArrayList<>();

        // Obtener las relaciones usando el método convencional de Spring Data JPA
        List<FichaIdentificacionPersonaRelacionada> relaciones = 
            this.fichaIdentificacionPersonaRelacionadaRepository.findByIdFichaIdentificacionTokenIdentificadorAndRemovido(
                tokenIdentificador, false);
        
        // Lista para almacenar todas las personas relacionadas
        List<PersonaRelacionada> todasPersonasRelacionadas = new ArrayList<>();
        
        // Extraer las personas relacionadas de las relaciones
        if (relaciones != null && !relaciones.isEmpty()) {
            for (FichaIdentificacionPersonaRelacionada relacion : relaciones) {
                PersonaRelacionada persona = relacion.getIdPersonasRelacionadas();
                if (persona != null && !persona.getRemovido()) {
                    todasPersonasRelacionadas.add(persona);
                }
            }
        }
        
        // Ordenar las personas relacionadas por idPersonasRelacionadas de forma descendente
        todasPersonasRelacionadas.sort((p1, p2) -> p2.getIdPersonasRelacionadas().compareTo(p1.getIdPersonasRelacionadas()));
        
        // Aplicar paginación manualmente
        int totalItems = todasPersonasRelacionadas.size();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        
        List<PersonaRelacionada> personasPaginadas = new ArrayList<>();
        if (startIndex < totalItems) {
            personasPaginadas = todasPersonasRelacionadas.subList(startIndex, endIndex);
        }
        
        // Convertir entidades a DTOs
        for (PersonaRelacionada personaRelacionada : personasPaginadas) {
            PersonaRelacionadaDTO persona = getPersonaRelacionadaDTO(personaRelacionada);
            
            // Setear datos básicos
            persona.setNombres(personaRelacionada.getNombresCompletos());
            if (personaRelacionada.getParentesco() != null) {
                persona.setParentesco(personaRelacionada.getParentesco().getDescripcion());
            }
            persona.setFechaNacimiento(personaRelacionada.getFechaNacimiento());
            persona.setNumeroDocumento(personaRelacionada.getIdentificacion());
            persona.setIdPersonaRelacionada(personaRelacionada.getIdPersonasRelacionadas());
            persona.setTokenIdentificador(personaRelacionada.getTokenIdentificador());
            persona.setEnfermo(personaRelacionada.getEnfermo());

            // Setear empresa
            if (personaRelacionada.getEmpresa() != null) {
                persona.setTokenIdentificadorEmpresa(personaRelacionada.getEmpresa().getTokenIdentificador());
            }
            
            // Setear condición laboral
            if (personaRelacionada.getCondicionLaboral() != null) {
                persona.setTokenIdentificadorCondicionLaboral(personaRelacionada.getCondicionLaboral().getTokenIdentificador());
            }

            // Setear campos opcionales
            if (!ObjectUtils.isEmpty(personaRelacionada.getOtros())) {
                persona.setOtros(personaRelacionada.getOtros());
            }

            if (!ObjectUtils.isEmpty(personaRelacionada.getEsResponsableEconom())) {
                persona.setEsResponsableEconom(personaRelacionada.getEsResponsableEconom());
            }

            if (!ObjectUtils.isEmpty(personaRelacionada.getIngresoPromedio())) {
                persona.setIngresoPromedio(personaRelacionada.getIngresoPromedio());
            }
            
            // Setear tipo sexo biológico
            if (personaRelacionada.getTipoSexoBiologico() != null && 
                personaRelacionada.getTipoSexoBiologico().getNemonico() != null) {
                persona.setTipoSexo(personaRelacionada.getTipoSexoBiologico().getNemonico());
            } else {
                persona.setTipoSexo("");
            }
            
            // Setear evaluación social
            if (personaRelacionada.getEvaluacionSocial() != null) {
                persona.setTokenIdentificadorEvaluacionSocial(
                    personaRelacionada.getEvaluacionSocial().getTokenIdentificador());
            }
            
            // Agregar a la lista
            personaRelacionadaDTOList.add(persona);
        }

        // Completar respuesta paginada
        personaRelacionadaResponse.setData(personaRelacionadaDTOList);
        personaRelacionadaResponse.setTotalItems(Long.valueOf(totalItems));

        // Mensaje para el usuario
        String mensajeUsuario = "Obteniendo " + totalItems + " personas relacionadas";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se obtuvieron con éxito " + totalItems + " personas relacionadas";

        df.llenarRespuestaExitosa(mensajeUsuario, personaRelacionadaResponse, mensajeAuditoria);

    } catch (Exception ex) {
        System.err.println("Error en obtenerPersonaRelacionada: " + ex.getMessage());
        ex.printStackTrace();
        df.llenarConDatosDeException(ex);
    }
    return df;
}

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> crearPersonaRelacionada(HttpServletRequest httpServletRequest, PersonaRelacionadaDTO personaRelacionadaDTO) {
        RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            String idSolicitud = personaRelacionadaDTO.getTokenIdentificadorFicha() + "-" + 
                          (personaRelacionadaDTO.getTokenIdentificador() != null ? 
                           personaRelacionadaDTO.getTokenIdentificador() : 
                           "nuevo-" + System.currentTimeMillis());

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
                PersonaRelacionada newPersonaRelacionada;
                boolean esEdicion = personaRelacionadaDTO.getEsEdicion();
                
                if(esEdicion){
                    newPersonaRelacionada = this.personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (newPersonaRelacionada == null) {
                        df.setMensaje("La persona relacionada a editar no existe o ya fue eliminada anteriormente");
                        return df;
                    }
                }else{
                    newPersonaRelacionada = new PersonaRelacionada();
                }

                newPersonaRelacionada.setPrimerApellido(personaRelacionadaDTO.getApellidoPaterno());
                newPersonaRelacionada.setSegundoApellido(personaRelacionadaDTO.getApellidoMaterno());
                newPersonaRelacionada.setTipoDocumento(catalogoRepository.findByNemonicoAndRemovido(personaRelacionadaDTO.getTipoIdentificacion(),false));
                newPersonaRelacionada.setIdentificacion(personaRelacionadaDTO.getNumeroDocumento());
                newPersonaRelacionada.setFechaNacimiento(personaRelacionadaDTO.getFechaNacimiento());
                newPersonaRelacionada.setTipoSexoBiologico(catalogoRepository.findByNemonicoAndRemovido(personaRelacionadaDTO.getTipoSexo(),false));
                newPersonaRelacionada.setModalidadEstudio(catalogoRepository.findByNemonicoAndRemovido(personaRelacionadaDTO.getModalidadEstudio(),false));
                if(personaRelacionadaDTO.getNivelEBR() != null) {
                    newPersonaRelacionada.setNivelEBR(catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getNivelEBR(),false));
                }

                if(personaRelacionadaDTO.getNivelSuperior() != null) {
                    newPersonaRelacionada.setNivelSuperior(catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getNivelSuperior(),false));
                }

                if(personaRelacionadaDTO.getNivelEBA() != null) {  
                    newPersonaRelacionada.setNivelEBA(catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getNivelEBA(),false));
                }
                newPersonaRelacionada.setEstadoCivil(catalogoRepository.findByNemonicoAndRemovido(personaRelacionadaDTO.getEstadoCivil(),false));
                newPersonaRelacionada.setOcupacion(personaRelacionadaDTO.getOcupacion());
                newPersonaRelacionada.setTelefono(personaRelacionadaDTO.getTelefono());
                newPersonaRelacionada.setParentesco(catalogoRepository.findByNemonicoAndRemovido(personaRelacionadaDTO.getTipoParentesco(),false));
                newPersonaRelacionada.setObservaciones(personaRelacionadaDTO.getObservaciones());
                newPersonaRelacionada.setNombresCompletos(personaRelacionadaDTO.getNombres()+" "+newPersonaRelacionada.getPrimerApellido()
                +" "+newPersonaRelacionada.getSegundoApellido());
                newPersonaRelacionada.setEsTutor(personaRelacionadaDTO.getEsTutor().equals("S"));
                newPersonaRelacionada.setFallecido(personaRelacionadaDTO.getFallecido().equals("S"));
                newPersonaRelacionada.setAutorizaVisita(personaRelacionadaDTO.getVisitaAutorizada().equals("S"));
                newPersonaRelacionada.setRelacionAfectiva(this.catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getRelacionAfectiva(),false));
                newPersonaRelacionada.setRolesInfluencias(personaRelacionadaDTO.getRolesInfluencias());
                newPersonaRelacionada.setNombres(personaRelacionadaDTO.getNombres());
                newPersonaRelacionada.setTipoOcupacion(this.catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getTipoOcupacion(),false));
                newPersonaRelacionada.setEmpresa(df2.getData().getEmpresa());

                personaRelacionadaRepository.save(newPersonaRelacionada);

                if (personaRelacionadaDTO.getTipoIdentificacion().equals("TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO")) {
                    if (!personaRelacionadaDTO.getEsEdicion() || 
                        personaRelacionadaDTO.getNumeroDocumento() == null || 
                        personaRelacionadaDTO.getNumeroDocumento().isEmpty()) {

                        String idComoDocumento = String.valueOf(newPersonaRelacionada.getIdPersonasRelacionadas());
                        newPersonaRelacionada.setIdentificacion(idComoDocumento);
                        personaRelacionadaDTO.setNumeroDocumento(idComoDocumento);

                        personaRelacionadaRepository.save(newPersonaRelacionada);
                    }
                }

                FichaIdentificacionPersonaRelacionada fip = null;
                fip = this.fichaIdentificacionPersonaRelacionadaRepository.encontrarPorTokenFichaYIdPersonaRelacionada(personaRelacionadaDTO.getTokenIdentificadorFicha(),
                        newPersonaRelacionada.getTokenIdentificador());

                if(fip==null){
                    fip = new FichaIdentificacionPersonaRelacionada();
                    fip.setIdPersonasRelacionadas(newPersonaRelacionada);
                    fip.setIdFichaIdentificacion(fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getTokenIdentificadorFicha(),
                            Boolean.FALSE));
                    fip.setRemovido(false);
                    this.fichaIdentificacionPersonaRelacionadaRepository.save(fip);
                }

                for (InformacionUbicacionDTO info: personaRelacionadaDTO.getInformacionUbicaciones()){
                    InformacionUbicacion infoUbi;
                    if(ObjectUtils.isEmpty(info.getTokenIdentificador())){
                        infoUbi = new InformacionUbicacion();
                        infoUbi.setIdPersonasRelacionadas(newPersonaRelacionada);
                        infoUbi.setTipoInformacionUbicacion(catalogoRepository.findByNemonicoAndRemovido(info.getTipoInformacionUbicacion(),false));
                        infoUbi.setValor(info.getValor());
                        infoUbi.setRemovido(false);
                    }else{
                        infoUbi = this.informacionUbicacionRepository.findByTokenIdentificadorAndRemovido(info.getTokenIdentificador(),false);
                        infoUbi.setTipoInformacionUbicacion(catalogoRepository.findByNemonicoAndRemovido(info.getTipoInformacionUbicacion(),false));
                        infoUbi.setValor(info.getValor());
                    }
                    this.informacionUbicacionRepository.save(infoUbi);
                }

                for(String tokensInfoUbi: personaRelacionadaDTO.getInformacionUbicacionesEliminar()){

                    InformacionUbicacion infoUbi =this.informacionUbicacionRepository.findByTokenIdentificadorAndRemovido(tokensInfoUbi,false);
                    if(!ObjectUtils.isEmpty(infoUbi)){
                        infoUbi.setRemovido(true);
                        this.informacionUbicacionRepository.save(infoUbi);
                    }
                }

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletosPersonaRelacionada(newPersonaRelacionada);
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito la persona relacionada " + nombresCompletos;

                // Mensaje para auditoría
                String identificacionPersona = obtenerIdentificacionPersona(newPersonaRelacionada);
                String mensajeAuditoria = "Se " + accion + " con éxito la persona relacionada con identificación: " + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, personaRelacionadaDTO, mensajeAuditoria);
            } finally {
                solicitudesEnProcesamiento.remove(idSolicitud);
            }

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> obtenerPersonaRelacionadaPorToken(HttpServletRequest httpServletRequest,
                                                                                              BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try{

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
            String tokenIdentificador = new Gson().fromJson(bodyString, String.class);

            PersonaRelacionada persona = this.personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, Boolean.FALSE);
            if (persona == null) {
                df.setMensaje("La persona relacionada no existe o fue eliminada anteriormente");
                return df;
            }

            PersonaRelacionadaDTO personaDTO = getPersonaRelacionadaDTO(persona);

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletosPersonaRelacionada(persona);
            
            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo los datos de " + nombresCompletos;

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(persona);
            String mensajeAuditoria = "Se obtuvo con éxito la persona relacionada con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, personaDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> obtenerPersonasRelacionadasPorTokenIdentificadorEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{

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
                    Sort.by("idPersonasRelacionadas").descending()
            );
            Page<PersonaRelacionada> personaRelacionadaPage = this.personaRelacionadaRepository.findByEmpresaIdEmpresaAndEvaluacionSocialTokenIdentificadorAndRemovido(
                    empresa.getIdEmpresa(), paginacionRequest.getTokenIdentificador() ,false, pageable);
            
            PaginacionResponse<PersonaRelacionadaDTO> paginacionResponse = new PaginacionResponse<>();
            List<PersonaRelacionadaDTO> PersonaRelacionadaDTOList = new ArrayList<>();
            for (PersonaRelacionada personaRelacionada : personaRelacionadaPage.toList()) {

                PersonaRelacionadaDTO personaRelacionadaDTO = getPersonaRelacionadaDTO(personaRelacionada);
                personaRelacionadaDTO.setNombres(personaRelacionada.getNombresCompletos());
                personaRelacionadaDTO.setParentesco(personaRelacionada.getParentesco().getNombre());
                personaRelacionadaDTO.setFechaNacimiento(personaRelacionada.getFechaNacimiento());
                personaRelacionadaDTO.setNumeroDocumento(personaRelacionada.getIdentificacion());
                personaRelacionadaDTO.setTokenIdentificador(personaRelacionada.getTokenIdentificador());
                personaRelacionadaDTO.setTokenIdentificadorEmpresa(personaRelacionada.getEmpresa().getTokenIdentificador());
                if (personaRelacionada.getEvaluacionSocial() != null) {
                    personaRelacionadaDTO.setTokenIdentificadorEvaluacionSocial(personaRelacionada.getEvaluacionSocial().getTokenIdentificador());
                }
                
                if(personaRelacionada.getCondicionLaboral()!=null) {
                    personaRelacionadaDTO.setTokenIdentificadorCondicionLaboral(personaRelacionada.getCondicionLaboral().getTokenIdentificador());
                }
                
                personaRelacionadaDTO.setOtros(personaRelacionada.getOtros());
                personaRelacionadaDTO.setIngresoPromedio(personaRelacionada.getIngresoPromedio());
                personaRelacionadaDTO.setEsResponsableEconom(personaRelacionada.getEsResponsableEconom());
                
                if (personaRelacionada.getCondicionLaboral() != null) {
                    personaRelacionadaDTO.setTokenIdentificadorCondicionLaboral(personaRelacionada.getCondicionLaboral().getTokenIdentificador());
                }
                
                personaRelacionadaDTO.setOcupacion(personaRelacionada.getOcupacion());
                
                PersonaRelacionadaDTOList.add(personaRelacionadaDTO);
            }

            paginacionResponse.setData(PersonaRelacionadaDTOList);
            paginacionResponse.setTotalItems(personaRelacionadaPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + personaRelacionadaPage.getTotalElements() + " personas relacionadas";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se obtuvieron con éxito " + personaRelacionadaPage.getTotalElements() + " personas relacionadas";
            
            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarPersonaRelacionada(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try{

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
            PersonaRelacionadaDTO personaRelacionadaDTO = new Gson().fromJson(bodyString, PersonaRelacionadaDTO.class);

            PersonaRelacionada persona = this.personaRelacionadaRepository.findByIdPersonasRelacionadasAndRemovido(personaRelacionadaDTO.getIdPersonaRelacionada(), Boolean.FALSE);
            if (persona == null) {
                df.setMensaje("La persona relacionada no existe o fue eliminada anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletosPersonaRelacionada(persona);
            String identificacionPersona = obtenerIdentificacionPersona(persona);
            
            Date fecha = new Date();
            persona.setRemovido(true);

            this.personaRelacionadaRepository.save(persona);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la persona relacionada " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito la persona relacionada con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;

    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<DireccionPersonaDTO> crearDireccionPersona(HttpServletRequest httpServletRequest,
                                                                                     DireccionPersonaDTO direccionPersonaDTO) {
        RespuestaPorDefectoAuditoria<DireccionPersonaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            PersonaRelacionada newPersonaRelacionada;
            newPersonaRelacionada = this.personaRelacionadaRepository.findByIdPersonasRelacionadasAndRemovido(direccionPersonaDTO.getIdPersonaRelacionada(), Boolean.FALSE);

            if (newPersonaRelacionada == null) {
                df.setMensaje("La persona relacionada no existe.");
                return df;
            }

            DireccionPersonaReferencia direccion = new DireccionPersonaReferencia();
            direccion.setDireccion(direccionPersonaDTO.getDireccion());
            direccion.setIdPersonasRelacionadas(newPersonaRelacionada);
            direccion.setRemovido(false);
            direccion.setTipoDireccion(catalogoRepository.findByNemonicoAndRemovido(direccionPersonaDTO.getTipoDireccion(),false));

            this.direccionPersonaReferenciaRepository.save(direccion);

            String identificacionPersona = obtenerIdentificacionPersona(newPersonaRelacionada);
            df.llenarRespuestaExitosa("Se creó con éxito la dirección para la persona relacionada con identificación: " + identificacionPersona, direccionPersonaDTO);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DireccionPersonaDTO>> obtenerDireccionesRelacionadas(HttpServletRequest httpServletRequest,
                                                                                                                BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<DireccionPersonaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{

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
            Long idPersonaRelacionada = new Gson().fromJson(bodyString, Long.class);

            PersonaRelacionada persona = this.personaRelacionadaRepository.findByIdPersonasRelacionadasAndRemovido(idPersonaRelacionada, Boolean.FALSE);
            if (persona == null) {
                df.setMensaje("La persona relacionada no existe o fue eliminada anteriormente");
                return df;
            }

            List<DireccionPersonaReferencia> listadoDirecciones = this.direccionPersonaReferenciaRepository.encontrarDireccionesPersonaRelacionada(idPersonaRelacionada);
            List<DireccionPersonaDTO> listadoDireccionesDTO = new ArrayList<>();

            for(DireccionPersonaReferencia direccion: listadoDirecciones){
                DireccionPersonaDTO dirDTO = new DireccionPersonaDTO();
                dirDTO.setDireccion(direccion.getDireccion());
                dirDTO.setIdDireccion(direccion.getIdDireccionPersonaReferencia());
                dirDTO.setTipoDireccion(direccion.getTipoDireccion().getNemonico());
                dirDTO.setNombreDireccion(direccion.getTipoDireccion().getNombre());
                listadoDireccionesDTO.add(dirDTO);
            }

            PaginacionResponse<DireccionPersonaDTO> direccionesRelacionadaPage = new PaginacionResponse<>();

            direccionesRelacionadaPage.setData(listadoDireccionesDTO);

            df.llenarRespuestaExitosa("Se obtuvieron con éxito " + listadoDireccionesDTO.size() + " direcciones", direccionesRelacionadaPage);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarDireccionRelacionada(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try{

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
            DireccionPersonaDTO direccionPersonaDTO = new Gson().fromJson(bodyString, DireccionPersonaDTO.class);

            DireccionPersonaReferencia direccion = this.direccionPersonaReferenciaRepository.findByidDireccionPersonaReferenciaAndRemovido(direccionPersonaDTO.getIdDireccion(), Boolean.FALSE);
            if (direccion == null) {
                df.setMensaje("La dirección no existe o fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            direccion.setRemovido(true);

            this.direccionPersonaReferenciaRepository.save(direccion);

            df.llenarRespuestaExitosa("Se eliminó con éxito la dirección: " + direccion.getDireccion(), true);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> obtenerPersonasRelacionadasPorIdFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{

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
            FichaIdentificacionDTO fichaIdentificacionDTO = new Gson().fromJson(bodyString, FichaIdentificacionDTO.class);

            List<PersonaRelacionada> personasRelacionadas = this.personaRelacionadaRepository.encontrarPersonaRelacionadaPorIdFicha(fichaIdentificacionDTO.getIdFichaIdentificacion(), false);
            if (personasRelacionadas == null || personasRelacionadas.isEmpty()) {
                df.setMensaje("No existen personas relacionadas");
                return df;
            }

            List<PersonaRelacionadaDTO> personaRelacionadaDTOList = new ArrayList<>();
            for (PersonaRelacionada persona: personasRelacionadas)
            {
                PersonaRelacionadaDTO personaDTO = new PersonaRelacionadaDTO();
                personaDTO.setIdPersonaRelacionada(persona.getIdPersonasRelacionadas());
                personaDTO.setNombres(persona.getNombresCompletos());
                personaDTO.setEsTutor(persona.getEsTutor().toString());
                Catalogo catalogo = catalogoRepository.findByIdCatalogoAndRemovido(persona.getParentesco().getIdCatalogo(), false);
                personaDTO.setTipoParentesco(catalogo.getDescripcion());
                personaDTO.setOcupacion(persona.getOcupacion());
                personaDTO.setIdPersonaRelacionada(persona.getIdPersonasRelacionadas());

                List<InformacionUbicacion> informacionUbicacionList = informacionUbicacionRepository.findByIdPersonasRelacionadasIdPersonasRelacionadasAndRemovido(persona.getIdPersonasRelacionadas(),false);

                ArrayList<InformacionUbicacionDTO> informacionUbicacionDTOList = new ArrayList<>();
                for(InformacionUbicacion info: informacionUbicacionList)
                {
                    InformacionUbicacionDTO infoDTO = new InformacionUbicacionDTO();
                    infoDTO.setIdInformacionUbicacion(info.getIdInformacionUbicacion());
                    Catalogo tipoInfo = catalogoRepository.findByIdCatalogoAndRemovido(info.getTipoInformacionUbicacion().getIdCatalogo(), false);
                    infoDTO.setTipoInformacionUbicacion(tipoInfo.getNemonico());
                    infoDTO.setIdPersonaRelacionada(persona.getIdPersonasRelacionadas());
                    infoDTO.setValor(info.getValor());
                    infoDTO.setNombreTipoInformacion(tipoInfo.getNombre());

                    informacionUbicacionDTOList.add(infoDTO);
                }

                personaDTO.setInformacionUbicaciones(informacionUbicacionDTOList);

                personaRelacionadaDTOList.add(personaDTO);
            }

            df.llenarRespuestaExitosa("Se obtuvieron con éxito " + personaRelacionadaDTOList.size() + " personas relacionadas", personaRelacionadaDTOList);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> obtenerPersonasRelacionadasPorTokenIdenficadorFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{

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
            FichaIdentificacionDTO fichaIdentificacionDTO = new Gson().fromJson(bodyString, FichaIdentificacionDTO.class);

            List<PersonaRelacionada> personasRelacionadas = this.personaRelacionadaRepository.encontrarPersonaRelacionadaPorTokenFicha(fichaIdentificacionDTO.getTokenIdentificador(), false);
            if (personasRelacionadas == null || personasRelacionadas.isEmpty()) {
                df.setMensaje("No existen personas relacionadas");
                df.setData(new ArrayList<PersonaRelacionadaDTO>());
                return df;
            }

            List<PersonaRelacionadaDTO> personaRelacionadaDTOList = new ArrayList<>();
            for (PersonaRelacionada persona: personasRelacionadas)
            {
                PersonaRelacionadaDTO personaDTO = new PersonaRelacionadaDTO();
                personaDTO.setTokenIdentificador(persona.getTokenIdentificador());
                personaDTO.setNombres(persona.getNombresCompletos());
                personaDTO.setNumeroDocumento(persona.getIdentificacion());
                personaDTO.setTelefono(persona.getTelefono());
                Catalogo catalogo = catalogoRepository.findByIdCatalogoAndRemovido(persona.getParentesco().getIdCatalogo(), false);
                personaDTO.setTipoParentesco(catalogo.getDescripcion());


                List<InformacionUbicacion> informacionUbicaciones = informacionUbicacionRepository.findByIdPersonasRelacionadasIdPersonasRelacionadasAndRemovido(persona.getIdPersonasRelacionadas(), false);
                ArrayList<InformacionUbicacionDTO> informacionUbicacionDTOList = new ArrayList<>();

                for(InformacionUbicacion infoUbi: informacionUbicaciones)
                {
                    InformacionUbicacionDTO infoUbiDTO = new InformacionUbicacionDTO();
                    infoUbiDTO.setTipoInformacionUbicacion(infoUbi.getTipoInformacionUbicacion().getNemonico());
                    infoUbiDTO.setNombreTipoInformacion(infoUbi.getTipoInformacionUbicacion().getNombre());
                    infoUbiDTO.setValor(infoUbi.getValor());

                    informacionUbicacionDTOList.add(infoUbiDTO);
                }

                personaDTO.setInformacionUbicaciones(informacionUbicacionDTOList);
                personaRelacionadaDTOList.add(personaDTO);
            }

            df.llenarRespuestaExitosa("Se obtuvieron con éxito " + personaRelacionadaDTOList.size() + " personas relacionadas", personaRelacionadaDTOList);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> editarPersonaRelacionadaEnfermo(HttpServletRequest httpServletRequest, PersonaRelacionadaDTO personaRelacionadaDTO) {
    RespuestaPorDefectoAuditoria<PersonaRelacionadaDTO> df = new RespuestaPorDefectoAuditoria<>();
    try{
        PersonaRelacionada newPersonaRelacionada = this.personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getTokenIdentificador(), Boolean.FALSE);
        if (newPersonaRelacionada == null) {
            df.setMensaje("La persona relacionada a editar no existe o ya fue eliminada anteriormente");
            return df;
        }

        newPersonaRelacionada.setEnfermo(personaRelacionadaDTO.getEnfermo());
        personaRelacionadaRepository.save(newPersonaRelacionada);

        String identificacionPersona = obtenerIdentificacionPersona(newPersonaRelacionada);
        df.llenarRespuestaExitosa("Se actualizó con éxito la persona relacionada con identificación: " + identificacionPersona, personaRelacionadaDTO);

    }catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarPersonaRelacionadaPorSituacionEconomicaSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        
        try{
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

            PersonaRelacionadaDTO personaRelacionadaDTO = new Gson().fromJson(bodyString, PersonaRelacionadaDTO.class);
            
            PersonaRelacionada personaRelacionada = this.personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getTokenIdentificador(), Boolean.FALSE);
            
            if (personaRelacionada == null) {
                df.setMensaje("La persona relacionada a eliminar no existe o ya fue eliminada anteriormente");
                return df;
            }

            String identificacionPersona = obtenerIdentificacionPersona(personaRelacionada);

            Date fecha = new Date();
            personaRelacionada.setFechaEdicion(fecha);
            personaRelacionada.setIpEdita(ip);
            personaRelacionada.setUsuarioSistemaEdita(usuarioSistemaLogin);
            
            personaRelacionada.setCondicionLaboral(null);
            personaRelacionada.setOtros(null);
            personaRelacionada.setIngresoPromedio(null);
            personaRelacionada.setNumeroHijos(null);
            personaRelacionada.setObservaciones(null);
            personaRelacionada.setEsResponsableEconom(null);

            personaRelacionada.setEvaluacionSocial(null);

            personaRelacionadaRepository.save(personaRelacionada);
            
            df.llenarRespuestaExitosa("Se eliminó con éxito la evaluación de la persona relacionada con identificación: " + identificacionPersona, true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> buscarPersonaRelacionadaPorNumeroDocumento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<PersonaRelacionadaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Verificar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String numeroDocumento = new Gson().fromJson(df22.getData(), String.class);

            // Validar que el número de documento no sea nulo o vacío
            if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
                df.llenarRespuestaExitosa("El número de documento es requerido", new ArrayList<>());
                return df;
            }

            // Normalizar el número de documento (eliminar espacios)
            numeroDocumento = numeroDocumento.trim();

            // Registrar para debugging
            System.out.println("Buscando persona relacionada por número de documento: [" + numeroDocumento + "]");

            // Buscar personas relacionadas por número de documento exacto
            List<PersonaRelacionada> personasRelacionadas = this.personaRelacionadaRepository.findByIdentificacionAndRemovido(
                    numeroDocumento, false);

            System.out.println("Personas encontradas: " + personasRelacionadas.size());

            if (personasRelacionadas.isEmpty()) {
                df.llenarRespuestaExitosa("No se encontraron personas relacionadas con el número de documento proporcionado.", 
                    new ArrayList<PersonaRelacionadaDTO>());
                return df;
            }

            List<PersonaRelacionadaDTO> personaRelacionadaDTOList = new ArrayList<>();

            for (PersonaRelacionada persona : personasRelacionadas) {
                // Registrar para debugging
                System.out.println("Procesando persona ID: " + persona.getIdPersonasRelacionadas() + ", Identificación: " + persona.getIdentificacion());

                // Convertir entidad a DTO
                PersonaRelacionadaDTO personaDTO = getPersonaRelacionadaDTO(persona);

                // Buscar relaciones con fichas de identificación
                List<FichaIdentificacionPersonaRelacionada> relaciones = 
                    this.fichaIdentificacionPersonaRelacionadaRepository.findByIdPersonasRelacionadas_IdPersonasRelacionadasAndRemovido(
                        persona.getIdPersonasRelacionadas(), false);

                System.out.println("Relaciones encontradas: " + relaciones.size());

                // Agregar los tokens de fichas relacionadas si existen
                if (!relaciones.isEmpty()) {
                    for (FichaIdentificacionPersonaRelacionada relacion : relaciones) {
                        if (relacion.getIdFichaIdentificacion() != null) {
                            personaDTO.setTokenIdentificadorFicha(
                                relacion.getIdFichaIdentificacion().getTokenIdentificador());
                            System.out.println("Asignado token de ficha: " + personaDTO.getTokenIdentificadorFicha());
                            // Si hay múltiples relaciones, solo tomamos la primera para simplificar
                            break;
                        }
                    }
                }

                // Buscar información de ubicación
                List<InformacionUbicacion> informacionUbicaciones = 
                    this.informacionUbicacionRepository.findByIdPersonasRelacionadasIdPersonasRelacionadasAndRemovido(
                        persona.getIdPersonasRelacionadas(), false);

                ArrayList<InformacionUbicacionDTO> informacionUbicacionDTOs = new ArrayList<>();

                for (InformacionUbicacion info : informacionUbicaciones) {
                    InformacionUbicacionDTO infoDTO = new InformacionUbicacionDTO();
                    infoDTO.setIdInformacionUbicacion(info.getIdInformacionUbicacion());
                    infoDTO.setTokenIdentificador(info.getTokenIdentificador());
                    infoDTO.setTipoInformacionUbicacion(info.getTipoInformacionUbicacion().getNemonico());
                    infoDTO.setValor(info.getValor());
                    infoDTO.setIdPersonaRelacionada(persona.getIdPersonasRelacionadas());
                    infoDTO.setNombreTipoInformacion(info.getTipoInformacionUbicacion().getNombre());

                    informacionUbicacionDTOs.add(infoDTO);
                }

                personaDTO.setInformacionUbicaciones(informacionUbicacionDTOs);
                personaRelacionadaDTOList.add(personaDTO);
            }

            df.llenarRespuestaExitosa("Se encontraron " + personaRelacionadaDTOList.size() + " personas relacionadas con el número de documento proporcionado", personaRelacionadaDTOList);

        } catch (Exception ex) {
            // Registrar la excepción completa
            System.err.println("Error en buscarPersonaRelacionadaPorNumeroDocumento: " + ex.getMessage());
            ex.printStackTrace();
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Método auxiliar para convertir una entidad PersonaRelacionada a un DTO
     */
    private PersonaRelacionadaDTO getPersonaRelacionadaDTO(PersonaRelacionada persona) {
        PersonaRelacionadaDTO personaDTO = new PersonaRelacionadaDTO();

        // Setear datos básicos
        personaDTO.setIdPersonaRelacionada(persona.getIdPersonasRelacionadas());
        personaDTO.setTokenIdentificador(persona.getTokenIdentificador());
        personaDTO.setNombres(persona.getNombres());
        personaDTO.setApellidoPaterno(persona.getPrimerApellido());
        personaDTO.setApellidoMaterno(persona.getSegundoApellido());
        personaDTO.setNumeroDocumento(persona.getIdentificacion());
        personaDTO.setFechaNacimiento(persona.getFechaNacimiento());

        if (persona.getTipoDocumento() != null) {
            personaDTO.setTipoIdentificacion(persona.getTipoDocumento().getNemonico());
        }

        if (persona.getParentesco() != null) {
            personaDTO.setTipoParentesco(persona.getParentesco().getNemonico());
            personaDTO.setParentesco(persona.getParentesco().getDescripcion());
        }

        if (persona.getTipoSexoBiologico() != null) {
            personaDTO.setTipoSexo(persona.getTipoSexoBiologico().getNemonico());
        }

        if (persona.getEstadoCivil() != null) {
            personaDTO.setEstadoCivil(persona.getEstadoCivil().getNemonico());
        }
        
        if (persona.getModalidadEstudio() != null) {
            personaDTO.setModalidadEstudio(persona.getModalidadEstudio().getNemonico());
        }

        if (persona.getNivelEBR() != null) {
            personaDTO.setNivelEBR(persona.getNivelEBR().getTokenIdentificador());
        }

        if (persona.getNivelEBA() != null) {
            personaDTO.setNivelEBA(persona.getNivelEBA().getTokenIdentificador());
        }

        if (persona.getNivelSuperior() != null) {
            personaDTO.setNivelSuperior(persona.getNivelSuperior().getTokenIdentificador());
        }

        if (persona.getTipoOcupacion() != null) {
            personaDTO.setTipoOcupacion(persona.getTipoOcupacion().getTokenIdentificador());
        }

        if (persona.getRelacionAfectiva() != null) {
            personaDTO.setRelacionAfectiva(persona.getRelacionAfectiva().getTokenIdentificador());
        }
        
        personaDTO.setNumeroHijos(persona.getNumeroHijos());
        personaDTO.setRolesInfluencias(persona.getRolesInfluencias());
        personaDTO.setOcupacion(persona.getOcupacion());
        personaDTO.setTelefono(persona.getTelefono());
        personaDTO.setObservaciones(persona.getObservaciones());
        personaDTO.setEsTutor(persona.getEsTutor() ? "S" : "N");
        personaDTO.setVisitaAutorizada(persona.getAutorizaVisita() ? "S" : "N");
        personaDTO.setFallecido(persona.getFallecido() ? "S" : "N");

        return personaDTO;
    }

    /**
     * Método auxiliar para obtener nombres completos de una persona relacionada
     */
    private String obtenerNombresCompletosPersonaRelacionada(PersonaRelacionada persona) {
        if (persona == null) {
            return "N/A";
        }

        // Si ya existe el campo nombresCompletos, usarlo
        if (persona.getNombresCompletos() != null && !persona.getNombresCompletos().trim().isEmpty()) {
            return persona.getNombresCompletos();
        }

        // Construir nombres completos desde los campos individuales
        StringBuilder nombreCompleto = new StringBuilder();
        if (persona.getNombres() != null && !persona.getNombres().trim().isEmpty()) {
            nombreCompleto.append(persona.getNombres());
        }
        if (persona.getPrimerApellido() != null && !persona.getPrimerApellido().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(persona.getPrimerApellido());
        }
        if (persona.getSegundoApellido() != null && !persona.getSegundoApellido().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(persona.getSegundoApellido());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una persona relacionada (para auditoría)
     */
    private String obtenerIdentificacionPersona(PersonaRelacionada persona) {
        if (persona == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        if (persona.getIdentificacion() != null && !persona.getIdentificacion().trim().isEmpty()) {
            identificacion = persona.getIdentificacion();
        }
        else if (persona.getNombresCompletos() != null && !persona.getNombresCompletos().trim().isEmpty()) {
            identificacion = persona.getNombresCompletos();
        }
        else if (persona.getNombres() != null || persona.getPrimerApellido() != null) {
            StringBuilder nombreCompleto = new StringBuilder();
            if (persona.getNombres() != null) {
                nombreCompleto.append(persona.getNombres());
            }
            if (persona.getPrimerApellido() != null) {
                if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                nombreCompleto.append(persona.getPrimerApellido());
            }
            if (persona.getSegundoApellido() != null) {
                if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                nombreCompleto.append(persona.getSegundoApellido());
            }
            if (nombreCompleto.length() > 0) {
                identificacion = nombreCompleto.toString();
            }
        }

        return identificacion;
    }
}
