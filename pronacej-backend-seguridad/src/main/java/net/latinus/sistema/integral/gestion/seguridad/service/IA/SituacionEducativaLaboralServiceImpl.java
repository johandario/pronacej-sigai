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
import net.latinus.sistema.integral.gestion.seguridad.entities.AreasSituacionEducativaLaboralOcio;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Laboral;
import net.latinus.sistema.integral.gestion.seguridad.entities.SituacionEducativaLaboralOcio;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AreasSituacionEducativaLaboralOcioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.LaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionEducativaLaboralDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituacionEducativaLaboralOcioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.AreasSituacionEducativaLaboralOcioRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.LaboralRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SituacionEducativaLaboralOcioRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class SituacionEducativaLaboralServiceImpl implements SituacionEducativaLaboralService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private SituacionEducativaLaboralOcioRepository situacionEducativaLaboralOcioRepository;
    private LaboralRepository laboralRepository;
    private AreasSituacionEducativaLaboralOcioRepository areasSituacionEducativaLaboralOcioRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    // Mapa para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();
    
    @Override
    public RespuestaPorDefectoAuditoria<AreasSituacionEducativaLaboralOcioDTO> obtenerAreasSituacionEducativaLaboralOcio(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
       RespuestaPorDefectoAuditoria<AreasSituacionEducativaLaboralOcioDTO> df = new RespuestaPorDefectoAuditoria<>();

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

           AreasSituacionEducativaLaboralOcio areas = areasSituacionEducativaLaboralOcioRepository
               .findByFichaIdentificacionTokenIdentificadorAndRemovido(
                   paginacionRequest.getTokenIdentificador(), 
                   Boolean.FALSE);

           if (areas != null) {
               AreasSituacionEducativaLaboralOcioDTO areasDTO = new AreasSituacionEducativaLaboralOcioDTO();
               areasDTO.setTokenIdentificador(areas.getTokenIdentificador());
               areasDTO.setTokenIdentificadorFichaIdentificacion(areas.getFichaIdentificacion().getTokenIdentificador());
               areasDTO.setActitudEstudios(areas.getActitudEstudios());
               areasDTO.setDesarrolloEducativo(areas.getDesarrolloEducativo());
               areasDTO.setInteresesVocacionales(areas.getInteresesVocacionales());
               areasDTO.setObservacionesEducativas(areas.getObservacionesEducativas());
               areasDTO.setActitudEmpleo(areas.getActitudEmpleo());
               areasDTO.setCapacitacionesEmpleabilidad(areas.getCapacitacionesEmpleabilidad());
               areasDTO.setObservacionesLaborales(areas.getObservacionesLaborales());
               areasDTO.setPasatiempos(areas.getPasatiempos());
               areasDTO.setTalentos(areas.getTalentos());
               areasDTO.setParticipacionGrupal(areas.getParticipacionGrupal());
               areasDTO.setUsoTiempo(areas.getUsoTiempo());
               areasDTO.setObservacionesOcio(areas.getObservacionesOcio());

               // Obtener nombres completos para los mensajes
               String nombresCompletos = obtenerNombresCompletos(areas.getFichaIdentificacion());

               // Mensaje para el usuario
               String mensajeUsuario = "Obteniendo las áreas de situación educativa, laboral y de ocio de " + nombresCompletos;

               // Mensaje para auditoría
               String identificacionPersona = obtenerIdentificacionPersona(areas.getFichaIdentificacion());
               String mensajeAuditoria = "Se ha encontrado la situación educativa, laboral y de ocio de la persona con identificación: " + identificacionPersona;

               df.llenarRespuestaExitosa(mensajeUsuario, areasDTO, mensajeAuditoria);
           } else {
               // Mensaje para el usuario
               String mensajeUsuario = "Obteniendo las áreas de situación educativa, laboral y de ocio";

               // Mensaje para auditoría
               String mensajeAuditoria = "No se encontraron datos de áreas";

               df.llenarRespuestaExitosa(mensajeUsuario, null, mensajeAuditoria);
           }

       } catch (Exception ex) {
           df.llenarConDatosDeException(ex);
       }

       return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionEducativaLaboralOcioDTO>> obtenerSituacionesEducativasLaboralesOcio(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<PaginacionResponse<SituacionEducativaLaboralOcioDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
                    Sort.by("idSituacionEducativaLaboralOcio").descending()
            );

            Page<SituacionEducativaLaboralOcio> situacionesPage = this.situacionEducativaLaboralOcioRepository.findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                    paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            PaginacionResponse<SituacionEducativaLaboralOcioDTO> paginacionResponse = new PaginacionResponse<>();
            List<SituacionEducativaLaboralOcioDTO> situacionesDTOList = new ArrayList<>();
            
            for (SituacionEducativaLaboralOcio situacionEducativaLaboralOcio : situacionesPage.toList()) {
                SituacionEducativaLaboralOcioDTO situacionEducativaLaboralOcioDTO = new SituacionEducativaLaboralOcioDTO();
                situacionEducativaLaboralOcioDTO.setTokenIdentificador(situacionEducativaLaboralOcio.getTokenIdentificador());
                situacionEducativaLaboralOcioDTO.setTokenIdentificadorFichaIdentificacion(situacionEducativaLaboralOcio.getFichaIdentificacion().getTokenIdentificador());
                situacionEducativaLaboralOcioDTO.setTokenIdentificadorEmpresa(situacionEducativaLaboralOcio.getEmpresa().getTokenIdentificador());
                
                situacionEducativaLaboralOcioDTO.setCentroEstudios(situacionEducativaLaboralOcio.getCentroEstudios());
                
                if (situacionEducativaLaboralOcio.getSituacionEducativa() != null) {
                    situacionEducativaLaboralOcioDTO.setTokenIdentificadorSituacionEducativa(
                        situacionEducativaLaboralOcio.getSituacionEducativa().getTokenIdentificador());
                }

                if (situacionEducativaLaboralOcio.getModalidadEducativa() != null) {
                    situacionEducativaLaboralOcioDTO.setTokenIdentificadorModalidadEducativa(situacionEducativaLaboralOcio.getModalidadEducativa().getTokenIdentificador());
                }
                if (situacionEducativaLaboralOcio.getRendimientoEducativo() != null) {
                    situacionEducativaLaboralOcioDTO.setTokenIdentificadorRendimientoEducativo(situacionEducativaLaboralOcio.getRendimientoEducativo().getTokenIdentificador());
                }
                if (situacionEducativaLaboralOcio.getModalidadEstudio() != null) {
                    situacionEducativaLaboralOcioDTO.setTokenIdentificadorModalidadEstudio(
                        situacionEducativaLaboralOcio.getModalidadEstudio().getTokenIdentificador());
                }

                situacionesDTOList.add(situacionEducativaLaboralOcioDTO);
            }

            paginacionResponse.setData(situacionesDTOList);
            paginacionResponse.setTotalItems(situacionesPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + situacionesPage.getTotalElements() + " situaciones educativas, laborales y de ocio";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + situacionesPage.getTotalElements() + " registros educativos, laborales y de ocio";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<LaboralDTO>> obtenerLaborales(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<LaboralDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación del JWT y obtención de datos asociados
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar y mapear el cuerpo del request
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            // Obtener empresa desde el token JWT
            Empresa empresa = df2.getData().getEmpresa();

            // Configuración de la paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idLaboral").descending()
            );

            // Consulta paginada de laborales
            Page<Laboral> laboralesPage = this.laboralRepository.findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                    paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            // Construcción de la respuesta paginada
            PaginacionResponse<LaboralDTO> paginacionResponse = new PaginacionResponse<>();
            List<LaboralDTO> laboralesDTOList = new ArrayList<>();

            for (Laboral laboral : laboralesPage.toList()) {
                LaboralDTO laboralDTO = new LaboralDTO();
                laboralDTO.setTokenIdentificador(laboral.getTokenIdentificador());
                laboralDTO.setTokenIdentificadorFichaIdentificacion(laboral.getFichaIdentificacion().getTokenIdentificador());
                laboralDTO.setTokenIdentificadorEmpresa(laboral.getEmpresa().getTokenIdentificador());
                laboralDTO.setExperienciaLaboral(laboral.getExperienciaLaboral());

                if (laboral.getOcupacionLaboral() != null) {
                    laboralDTO.setTokenIdentificadorOcupacionLaboral(laboral.getOcupacionLaboral().getTokenIdentificador());
                }
                if (laboral.getModalidadLaboral() != null) {
                    laboralDTO.setTokenIdentificadorModalidadLaboral(laboral.getModalidadLaboral().getTokenIdentificador());
                }
                if (laboral.getRecursosApoyoLaboral() != null) {
                    laboralDTO.setTokenIdentificadorRecursosApoyoLaboral(laboral.getRecursosApoyoLaboral().getTokenIdentificador());
                }

                laboralesDTOList.add(laboralDTO);
            }

            // Configuración de la respuesta paginada
            paginacionResponse.setData(laboralesDTOList);
            paginacionResponse.setTotalItems(laboralesPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + laboralesPage.getTotalElements() + " experiencias laborales";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + laboralesPage.getTotalElements() + " experiencias laborales registradas";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<SituacionEducativaLaboralDTO> crearSituacionEducativaLaboral(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<SituacionEducativaLaboralDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            SituacionEducativaLaboralDTO situacionEducativaLaboralDTO = new Gson().fromJson(bodyString, SituacionEducativaLaboralDTO.class);

            // PROTECCIÓN CONTRA DUPLICADOS
            String idSolicitud = situacionEducativaLaboralDTO.getTokenIdentificadorFichaIdentificacion() + "-situacionEducativaLaboral";

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
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(situacionEducativaLaboralDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);

                // Determinar si es edición verificando si hay elementos existentes
                boolean esEdicion = false;

                // Verificar si hay áreas existentes
                AreasSituacionEducativaLaboralOcio areasExistentes = areasSituacionEducativaLaboralOcioRepository
                    .findByFichaIdentificacionTokenIdentificadorAndRemovido(
                        situacionEducativaLaboralDTO.getTokenIdentificadorFichaIdentificacion(),
                        Boolean.FALSE
                    );
                if (areasExistentes != null) {
                    esEdicion = true;
                }

                // Verificar si hay elementos en las listas que no sean nuevos
                if (!esEdicion && situacionEducativaLaboralDTO.getListaSituEducLaboOcio() != null) {
                    for (SituacionEducativaLaboralOcioDTO item : situacionEducativaLaboralDTO.getListaSituEducLaboOcio()) {
                        if (item.getTokenIdentificador() != null && !item.getTokenIdentificador().equals("0")) {
                            esEdicion = true;
                            break;
                        }
                    }
                }

                if (!esEdicion && situacionEducativaLaboralDTO.getListaLaboral() != null) {
                    for (LaboralDTO item : situacionEducativaLaboralDTO.getListaLaboral()) {
                        if (item.getTokenIdentificador() != null && !item.getTokenIdentificador().equals("0")) {
                            esEdicion = true;
                            break;
                        }
                    }
                }

                // Procesar áreas
                if (situacionEducativaLaboralDTO.getAreas() != null) {
                    AreasSituacionEducativaLaboralOcio areas = areasSituacionEducativaLaboralOcioRepository
                        .findByFichaIdentificacionTokenIdentificadorAndRemovido(
                            situacionEducativaLaboralDTO.getTokenIdentificadorFichaIdentificacion(),
                            Boolean.FALSE
                        );

                    if (areas == null) {
                        areas = new AreasSituacionEducativaLaboralOcio();
                        areas.setFechaCreacion(new Date());
                        areas.setIpCrea(ip);
                        areas.setUsuarioSistemaCrea(usuarioLogin);
                        areas.setEmpresa(empresa);
                    } else {
                        areas.setFechaEdicion(new Date());
                        areas.setIpEdita(ip);
                        areas.setUsuarioSistemaEdita(usuarioLogin);
                    }

                    // Mapear datos del DTO a la entidad
                    areas.setFichaIdentificacion(fichaIdentificacion);
                    areas.setActitudEstudios(situacionEducativaLaboralDTO.getAreas().getActitudEstudios());
                    areas.setDesarrolloEducativo(situacionEducativaLaboralDTO.getAreas().getDesarrolloEducativo());
                    areas.setInteresesVocacionales(situacionEducativaLaboralDTO.getAreas().getInteresesVocacionales());
                    areas.setObservacionesEducativas(situacionEducativaLaboralDTO.getAreas().getObservacionesEducativas());
                    areas.setActitudEmpleo(situacionEducativaLaboralDTO.getAreas().getActitudEmpleo());
                    areas.setCapacitacionesEmpleabilidad(situacionEducativaLaboralDTO.getAreas().getCapacitacionesEmpleabilidad());
                    areas.setObservacionesLaborales(situacionEducativaLaboralDTO.getAreas().getObservacionesLaborales());
                    areas.setPasatiempos(situacionEducativaLaboralDTO.getAreas().getPasatiempos());
                    areas.setTalentos(situacionEducativaLaboralDTO.getAreas().getTalentos());
                    areas.setParticipacionGrupal(situacionEducativaLaboralDTO.getAreas().getParticipacionGrupal());
                    areas.setUsoTiempo(situacionEducativaLaboralDTO.getAreas().getUsoTiempo());
                    areas.setObservacionesOcio(situacionEducativaLaboralDTO.getAreas().getObservacionesOcio());

                    areasSituacionEducativaLaboralOcioRepository.save(areas);
                }

                for (SituacionEducativaLaboralOcioDTO situacionEducativaLaboralOcioDTO : situacionEducativaLaboralDTO.getListaSituEducLaboOcio()) {
                    SituacionEducativaLaboralOcio situacionEducativaLaboralOcio;
                    if (situacionEducativaLaboralOcioDTO.getTokenIdentificador().equals("0")) {
                        situacionEducativaLaboralOcio = new SituacionEducativaLaboralOcio();
                        situacionEducativaLaboralOcio.setFechaCreacion(new Date());
                        situacionEducativaLaboralOcio.setIpCrea(ip);
                        situacionEducativaLaboralOcio.setUsuarioSistemaCrea(usuarioLogin);
                        situacionEducativaLaboralOcio.setEmpresa(empresa);
                    } else {
                        situacionEducativaLaboralOcio = situacionEducativaLaboralOcioRepository.findByTokenIdentificadorAndRemovido(situacionEducativaLaboralOcioDTO.getTokenIdentificador(), Boolean.FALSE);
                        situacionEducativaLaboralOcio.setFechaEdicion(new Date());
                        situacionEducativaLaboralOcio.setIpEdita(ip);
                        situacionEducativaLaboralOcio.setUsuarioSistemaEdita(usuarioLogin);
                    }

                    situacionEducativaLaboralOcio.setCentroEstudios(situacionEducativaLaboralOcioDTO.getCentroEstudios());
                    Catalogo situacionEducativa = catalogoRepository.findByTokenIdentificadorAndRemovido(situacionEducativaLaboralOcioDTO.getTokenIdentificadorSituacionEducativa(), Boolean.FALSE);
                    situacionEducativaLaboralOcio.setSituacionEducativa(situacionEducativa);
                    Catalogo modalidadEducativa = catalogoRepository.findByTokenIdentificadorAndRemovido(situacionEducativaLaboralOcioDTO.getTokenIdentificadorModalidadEducativa(), Boolean.FALSE);
                    situacionEducativaLaboralOcio.setModalidadEducativa(modalidadEducativa);
                    Catalogo rendimientoEducativo = catalogoRepository.findByTokenIdentificadorAndRemovido(situacionEducativaLaboralOcioDTO.getTokenIdentificadorRendimientoEducativo(), Boolean.FALSE);
                    situacionEducativaLaboralOcio.setRendimientoEducativo(rendimientoEducativo);
                    Catalogo modalidadEstudio = catalogoRepository.findByTokenIdentificadorAndRemovido(situacionEducativaLaboralOcioDTO.getTokenIdentificadorModalidadEstudio(), Boolean.FALSE);
                    situacionEducativaLaboralOcio.setModalidadEstudio(modalidadEstudio);

                    situacionEducativaLaboralOcio.setFichaIdentificacion(fichaIdentificacion);

                    situacionEducativaLaboralOcio = this.situacionEducativaLaboralOcioRepository.save(situacionEducativaLaboralOcio);
                    situacionEducativaLaboralOcioDTO.setTokenIdentificador(situacionEducativaLaboralOcio.getTokenIdentificador());

                }

                for (LaboralDTO laboralDTO : situacionEducativaLaboralDTO.getListaLaboral()) {
                    Laboral laboral;
                    if (laboralDTO.getTokenIdentificador().equals("0")) {
                        laboral = new Laboral();
                        laboral.setFechaCreacion(new Date());
                        laboral.setIpCrea(ip);
                        laboral.setUsuarioSistemaCrea(usuarioLogin);
                        laboral.setEmpresa(empresa);
                    }else {
                        laboral = laboralRepository.findByTokenIdentificadorAndRemovido(laboralDTO.getTokenIdentificador(), Boolean.FALSE);
                        laboral.setFechaEdicion(new Date());
                        laboral.setIpEdita(ip);
                        laboral.setUsuarioSistemaEdita(usuarioLogin);
                    }

                    laboral.setExperienciaLaboral(laboralDTO.getExperienciaLaboral());

                    Catalogo ocupacionLaboral = catalogoRepository.findByTokenIdentificadorAndRemovido(laboralDTO.getTokenIdentificadorOcupacionLaboral(), Boolean.FALSE);
                    laboral.setOcupacionLaboral(ocupacionLaboral);
                    Catalogo modalidadLaboral = catalogoRepository.findByTokenIdentificadorAndRemovido(laboralDTO.getTokenIdentificadorModalidadLaboral(), Boolean.FALSE);
                    laboral.setModalidadLaboral(modalidadLaboral);
                    Catalogo recursosApoyoLaboral = catalogoRepository.findByTokenIdentificadorAndRemovido(laboralDTO.getTokenIdentificadorRecursosApoyoLaboral(), Boolean.FALSE);
                    laboral.setRecursosApoyoLaboral(recursosApoyoLaboral);

                    laboral.setFichaIdentificacion(fichaIdentificacion);

                    laboral = this.laboralRepository.save(laboral);
                    laboralDTO.setTokenIdentificador(laboral.getTokenIdentificador());
                }

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                
                // Mensaje para el usuario
                String accion = esEdicion ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito la situación educativa, laboral y de ocio de " + nombresCompletos;
                
                // Mensaje para auditoría
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                String mensajeAuditoria = "Se " + accion + " con éxito la situación educativa, laboral y de ocio de la persona con identificación: " + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, situacionEducativaLaboralDTO, mensajeAuditoria);
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
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSituacionEducativaLaboralOcio(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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

            SituacionEducativaLaboralOcioDTO situacionEducativaLaboralOcioDTO = new Gson().fromJson(bodyString, SituacionEducativaLaboralOcioDTO.class);

            SituacionEducativaLaboralOcio situacionEducativaLaboralOcio = this.situacionEducativaLaboralOcioRepository.findByTokenIdentificadorAndRemovido(
                    situacionEducativaLaboralOcioDTO.getTokenIdentificador(), false
            );

            if (situacionEducativaLaboralOcio == null) {
                df.setMensaje("La situación educativa/laboral/ocio no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(situacionEducativaLaboralOcio.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(situacionEducativaLaboralOcio.getFichaIdentificacion());

            Date fecha = new Date();
            situacionEducativaLaboralOcio.setRemovido(true);
            situacionEducativaLaboralOcio.setIpElimina(ip);
            situacionEducativaLaboralOcio.setUsuarioSistemaElimina(usuarioSistemaLogin);
            situacionEducativaLaboralOcio.setFechaEliminacion(fecha);

            this.situacionEducativaLaboralOcioRepository.save(situacionEducativaLaboralOcio);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la situación educativa, laboral y de ocio de " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito el registro educativo, laboral y de ocio de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarLaboral(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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

            LaboralDTO laboralDTO = new Gson().fromJson(bodyString, LaboralDTO.class);

            Laboral laboral = this.laboralRepository.findByTokenIdentificadorAndRemovido(
                    laboralDTO.getTokenIdentificador(), false
            );

            if (laboral == null) {
                df.setMensaje("El laboral no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(laboral.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(laboral.getFichaIdentificacion());

            Date fecha = new Date();
            laboral.setRemovido(true);
            laboral.setIpElimina(ip);
            laboral.setUsuarioSistemaElimina(usuarioSistemaLogin);
            laboral.setFechaEliminacion(fecha);

            this.laboralRepository.save(laboral);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la experiencia laboral de " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito la experiencia laboral de la persona con identificación: " + identificacionPersona;

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
}