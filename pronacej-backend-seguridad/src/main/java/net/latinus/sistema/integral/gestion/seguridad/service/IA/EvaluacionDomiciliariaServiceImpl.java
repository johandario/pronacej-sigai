package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionDomiciliaria;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionDomiciliariaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionDomiciliariaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionDomiciliariaCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionDomiciliariaDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionDomiciliariaDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.EvaluacionDomiciliariaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.EvaluacionDomiciliariaCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.EvaluacionDomiciliariaDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PersonaRelacionadaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
@AllArgsConstructor
public class EvaluacionDomiciliariaServiceImpl implements EvaluacionDomiciliariaService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private PersonaRelacionadaRepository personaRelacionadaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private EvaluacionDomiciliariaRepository evaluacionDomiciliariaRepository;
    private JerarquiaRepository jerarquiaRepository;
    private CatalogoRepository catalogoRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private CarpetaRepository carpetaRepository;
    private EvaluacionDomiciliariaCarpetaRepository evaluacionDomiciliariaCarpetaRepository;
    private EvaluacionDomiciliariaDocumentoRepository evaluacionDomiciliariaDocumentoRepository;
    private CarpetaService carpetaService;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;
    
    // Mapa para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionDomiciliariaDTO>> obtenerEvaluacionesDomiciliarias(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionDomiciliariaDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            // CAMBIO MULTI-JERÁRQUICO: Obtener la jerarquía específica del usuario
            Jerarquia centroUsuario = bodyJwtValido.getJerarquia();
            if (centroUsuario == null) {
                df.setMensaje("No se pudo determinar la jerarquía del usuario. Contacte al administrador.");
                return df;
            }

            // CAMBIO PRINCIPAL: Usar jerarquía padre para ver todos los centros de la misma jerarquía
            Jerarquia jerarquiaPadre = centroUsuario.getJerarquiaPadre();
            if (jerarquiaPadre == null) {
                df.setMensaje("No se pudo determinar la jerarquía padre del usuario. Contacte al administrador.");
                return df;
            }

            String filtro = paginacionRequest.getFilter() != null ? paginacionRequest.getFilter() : "";
            Page<EvaluacionDomiciliaria> evaluacionDomiciliariaPage;

            // Manejar ordenamiento especial para persona entrevistada
            if (paginacionRequest.getSort() != null && "personaEntrevistada".equals(paginacionRequest.getSort())) {
                String direccion = paginacionRequest.getDirection() != null ? 
                    paginacionRequest.getDirection().toUpperCase() : "ASC";

                Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

                if (filtro.isEmpty()) {
                    if ("DESC".equals(direccion)) {
                        evaluacionDomiciliariaPage = this.evaluacionDomiciliariaRepository.buscarOrdenadoPorPersonaEntrevistadaDescPorJerarquiaPadre(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(),
                                jerarquiaPadre.getIdJerarquia(), // FILTRO POR JERARQUÍA PADRE
                                pageable);
                    } else {
                        evaluacionDomiciliariaPage = this.evaluacionDomiciliariaRepository.buscarOrdenadoPorPersonaEntrevistadaAscPorJerarquiaPadre(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(),
                                jerarquiaPadre.getIdJerarquia(), // FILTRO POR JERARQUÍA PADRE
                                pageable);
                    }
                } else {
                    if ("DESC".equals(direccion)) {
                        evaluacionDomiciliariaPage = this.evaluacionDomiciliariaRepository.buscarConFiltroOrdenadoPorPersonaEntrevistadaDescPorJerarquiaPadre(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(),
                                jerarquiaPadre.getIdJerarquia(), // FILTRO POR JERARQUÍA PADRE
                                filtro,
                                pageable);
                    } else {
                        evaluacionDomiciliariaPage = this.evaluacionDomiciliariaRepository.buscarConFiltroOrdenadoPorPersonaEntrevistadaAscPorJerarquiaPadre(
                                paginacionRequest.getTokenIdentificador(), 
                                empresa.getIdEmpresa(),
                                jerarquiaPadre.getIdJerarquia(), // FILTRO POR JERARQUÍA PADRE
                                filtro,
                                pageable);
                    }
                }
            } else {
                // Ordenamiento normal para otros campos
                Pageable pageable;

                if (paginacionRequest.getSort() != null && !paginacionRequest.getSort().isEmpty() 
                        && paginacionRequest.getDirection() != null && !paginacionRequest.getDirection().isEmpty()) {

                    String campoOrdenamiento = mapearCampoOrdenamiento(paginacionRequest.getSort());

                    Sort.Direction direction = paginacionRequest.getDirection().equalsIgnoreCase("asc") 
                            ? Sort.Direction.ASC : Sort.Direction.DESC;

                    pageable = PageRequest.of(
                            paginacionRequest.getPage(),
                            paginacionRequest.getSize(),
                            Sort.by(direction, campoOrdenamiento)
                    );
                } else {
                    pageable = PageRequest.of(
                            paginacionRequest.getPage(),
                            paginacionRequest.getSize(),
                            Sort.by("idEvaluacionDomiciliaria").descending()
                    );
                }

                // CAMBIO MULTI-JERÁRQUICO: Usar método con filtro por jerarquía padre
                evaluacionDomiciliariaPage = this.evaluacionDomiciliariaRepository.buscarPorFiltroYJerarquiaPadre(
                        paginacionRequest.getTokenIdentificador(), 
                        empresa.getIdEmpresa(),
                        jerarquiaPadre.getIdJerarquia(), // FILTRO POR JERARQUÍA PADRE
                        filtro, 
                        pageable);
            }

            // Convertir entidades a DTOs
            PaginacionResponse<EvaluacionDomiciliariaDTO> paginacionResponse = new PaginacionResponse<>();
            List<EvaluacionDomiciliariaDTO> evaluacionDomiciliariaDTOList = new ArrayList<>();

            for (EvaluacionDomiciliaria evaluacionDomiciliaria : evaluacionDomiciliariaPage.getContent()) {
                EvaluacionDomiciliariaDTO evaluacionDomiciliariaDTO = new EvaluacionDomiciliariaDTO();
                evaluacionDomiciliariaDTO.setTokenIdentificador(evaluacionDomiciliaria.getTokenIdentificador());
                evaluacionDomiciliariaDTO.setTokenIdentificadorEmpresa(evaluacionDomiciliaria.getEmpresa().getTokenIdentificador());

                if(evaluacionDomiciliaria.getCentro() != null) {
                    JerarquiaDTO tipoCentro = new JerarquiaDTO();
                    tipoCentro.setNombre(evaluacionDomiciliaria.getCentro().getJerarquiaPadre().getNombre());
                    tipoCentro.setNemonico(evaluacionDomiciliaria.getCentro().getJerarquiaPadre().getNemonico());
                    JerarquiaDTO centro = new JerarquiaDTO();
                    centro.setTokenIdentificador(evaluacionDomiciliaria.getCentro().getTokenIdentificador());
                    centro.setNombre(evaluacionDomiciliaria.getCentro().getNombre());
                    centro.setUbigeo(evaluacionDomiciliaria.getCentro().getUbigeo());
                    centro.setJerarquiaPadre(tipoCentro);
                    if(!ObjectUtils.isEmpty(evaluacionDomiciliaria.getCentro())){
                        centro.setGenero(catalogoToDTO(evaluacionDomiciliaria.getCentro().getGenero()));
                    }
                    evaluacionDomiciliariaDTO.setCentro(centro);
                }

                evaluacionDomiciliariaDTO.setFechaRegistro(evaluacionDomiciliaria.getFechaRegistro());
                evaluacionDomiciliariaDTO.setFechaEntrevista(evaluacionDomiciliaria.getFechaEntrevista());

                if (evaluacionDomiciliaria.getPersonaRelacionada() != null) {
                    evaluacionDomiciliariaDTO.setTokenIdentificadorPersonaRelacionada(evaluacionDomiciliaria.getPersonaRelacionada().getTokenIdentificador());
                }

                evaluacionDomiciliariaDTO.setOtraPersonaRelacionada(evaluacionDomiciliaria.getOtraPersonaRelacionada());
                evaluacionDomiciliariaDTO.setDuracionVista(evaluacionDomiciliaria.getDuracionVista());
                evaluacionDomiciliariaDTO.setVisitaRealizada(evaluacionDomiciliaria.getVisitaRealizada());
                evaluacionDomiciliariaDTO.setMotivoNoVisita(evaluacionDomiciliaria.getMotivoNoVisita());
                evaluacionDomiciliariaDTO.setObjetivoGeneral(evaluacionDomiciliaria.getObjetivoGeneral());
                evaluacionDomiciliariaDTO.setDesarrolloVisitaDomiciliaria(evaluacionDomiciliaria.getDesarrolloVisitaDomiciliaria());
                evaluacionDomiciliariaDTO.setCaracteristicasDomicilioVisitado(evaluacionDomiciliaria.getCaracteristicasDomicilioVisitado());
                evaluacionDomiciliariaDTO.setConclusiones(evaluacionDomiciliaria.getConclusiones());
                evaluacionDomiciliariaDTO.setRecomendaciones(evaluacionDomiciliaria.getRecomendaciones());
                evaluacionDomiciliariaDTO.setDinamicaFamiliarDisfuncional(evaluacionDomiciliaria.getDinamicaFamiliarDisfuncional());
                evaluacionDomiciliariaDTO.setCaracteristicasEntornoSocialMC(evaluacionDomiciliaria.getCaracteristicasEntornoSocialMC());
                evaluacionDomiciliariaDTO.setFactoresProtectores(evaluacionDomiciliaria.getFactoresProtectores());
                evaluacionDomiciliariaDTO.setFactoresRiesgoFamilia(evaluacionDomiciliaria.getFactoresRiesgoFamilia());
                evaluacionDomiciliariaDTO.setFactoresRiesgoSocial(evaluacionDomiciliaria.getFactoresRiesgoSocial());
                evaluacionDomiciliariaDTO.setFactoresProtectoresFamilia(evaluacionDomiciliaria.getFactoresProtectoresFamilia());
                evaluacionDomiciliariaDTO.setFactoresProtectoresSocial(evaluacionDomiciliaria.getFactoresProtectoresSocial());
                evaluacionDomiciliariaDTO.setFechaCreacion(evaluacionDomiciliaria.getFechaCreacion());

                if (evaluacionDomiciliaria.getFichaIdentificacion() != null) {
                    evaluacionDomiciliariaDTO.setTokenIdentificadorFichaIdentificacion(evaluacionDomiciliaria.getFichaIdentificacion().getTokenIdentificador());
                }

                evaluacionDomiciliariaDTOList.add(evaluacionDomiciliariaDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            evaluacionDomiciliariaDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(evaluacionDomiciliariaDTOList);
            paginacionResponse.setTotalItems(evaluacionDomiciliariaPage.getTotalElements());

            String mensajeUsuario = "Obteniendo " + evaluacionDomiciliariaPage.getTotalElements() + " evaluaciones domiciliarias de centros " + jerarquiaPadre.getNombre();
            String mensajeAuditoria = "Se han encontrado un total de " + evaluacionDomiciliariaPage.getTotalElements() + " evaluaciones domiciliarias de centros " + jerarquiaPadre.getNombre();

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            ex.printStackTrace();
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
            case "fechaEntrevista":
                return "fechaEntrevista";
            case "duracionVista":
                return "duracionVista";
            case "visitaRealizada":
                return "visitaRealizada";
            case "fechaRegistro":
                return "fechaRegistro";
            case "numero":
                return "idEvaluacionDomiciliaria";
            default:
                return "fechaEntrevista";
        }
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO> crearEvaluacionDomiciliaria(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            
            // Obtener la jerarquía del usuario desde el JWT (sistema multijerárquico)
            Jerarquia centroUsuario = bodyJwtValido.getJerarquia();
            if (centroUsuario == null) {
                df.setMensaje("No se pudo determinar la jerarquía del usuario. Contacte al administrador.");
                return df;
            }

            EvaluacionDomiciliariaDTO evaluacionDomiciliariaDTO = new Gson().fromJson(bodyString, EvaluacionDomiciliariaDTO.class);
            evaluacionDomiciliariaDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();

            // Generar clave única para esta solicitud (ficha + usuario + operación)
            String requestKey = evaluacionDomiciliariaDTO.getTokenIdentificadorFichaIdentificacion() + "_" + 
                               usuarioLogin.getIdUsuarioSistema() + "_" + 
                               (evaluacionDomiciliariaDTO.getEsEdicion() ? "edit" : "create");

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
                EvaluacionDomiciliaria evaluacionDomiciliaria;
                if(evaluacionDomiciliariaDTO.getEsEdicion()){
                    evaluacionDomiciliaria = evaluacionDomiciliariaRepository.findByTokenIdentificadorAndRemovido(evaluacionDomiciliariaDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (evaluacionDomiciliaria == null) {
                        df.setMensaje("La evaluación domiciliaria a editar no existe o ya fue eliminada anteriormente");
                        return df;
                    }
                    
                    // Verificar que la evaluación pertenezca al centro del usuario actual
                    if (!evaluacionDomiciliaria.getCentro().getTokenIdentificador().equals(centroUsuario.getTokenIdentificador())) {
                        df.setMensaje("No tiene permisos para editar esta evaluación domiciliaria");
                        return df;
                    }
                    
                    evaluacionDomiciliaria.setFechaEdicion(new Date());
                    evaluacionDomiciliaria.setIpEdita(ip);
                    evaluacionDomiciliaria.setUsuarioSistemaEdita(usuarioLogin);
                }else{                
                    evaluacionDomiciliaria = new EvaluacionDomiciliaria();
                    evaluacionDomiciliaria.setFechaCreacion(new Date());
                    evaluacionDomiciliaria.setIpCrea(ip);
                    evaluacionDomiciliaria.setUsuarioSistemaCrea(usuarioLogin);
                    evaluacionDomiciliaria.setEmpresa(empresa);
                    
                    FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(evaluacionDomiciliariaDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);
                    evaluacionDomiciliaria.setFichaIdentificacion(fichaIdentificacion);
                    
                    // Usar la jerarquía del usuario desde el JWT en lugar del DTO
                    evaluacionDomiciliaria.setCentro(centroUsuario);
                }

                evaluacionDomiciliaria.setFechaRegistro(evaluacionDomiciliariaDTO.getFechaRegistro());
                evaluacionDomiciliaria.setFechaEntrevista(evaluacionDomiciliariaDTO.getFechaEntrevista());
                
                if (evaluacionDomiciliariaDTO.getTokenIdentificadorPersonaRelacionada() != null && 
                    !evaluacionDomiciliariaDTO.getTokenIdentificadorPersonaRelacionada().equals("0")) {

                    if (evaluacionDomiciliariaDTO.getTokenIdentificadorPersonaRelacionada().equals("OTROS")) {
                        // Para el caso de "Otros", solo guardamos el texto en otraPersonaRelacionada
                        evaluacionDomiciliaria.setPersonaRelacionada(null);
                        evaluacionDomiciliaria.setOtraPersonaRelacionada(evaluacionDomiciliariaDTO.getOtraPersonaRelacionada());
                    } else {
                        // Buscar la persona relacionada por su token
                        PersonaRelacionada personaRelacionada = personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(
                            evaluacionDomiciliariaDTO.getTokenIdentificadorPersonaRelacionada(), Boolean.FALSE
                        );
                        if (personaRelacionada != null) {
                            evaluacionDomiciliaria.setPersonaRelacionada(personaRelacionada);
                            evaluacionDomiciliaria.setOtraPersonaRelacionada(null);
                        }
                    }
                }
                
                evaluacionDomiciliaria.setDuracionVista(evaluacionDomiciliariaDTO.getDuracionVista());
                evaluacionDomiciliaria.setVisitaRealizada(evaluacionDomiciliariaDTO.getVisitaRealizada());
                evaluacionDomiciliaria.setMotivoNoVisita(evaluacionDomiciliariaDTO.getMotivoNoVisita());
                evaluacionDomiciliaria.setObjetivoGeneral(evaluacionDomiciliariaDTO.getObjetivoGeneral());
                evaluacionDomiciliaria.setDesarrolloVisitaDomiciliaria(evaluacionDomiciliariaDTO.getDesarrolloVisitaDomiciliaria());
                evaluacionDomiciliaria.setCaracteristicasDomicilioVisitado(evaluacionDomiciliariaDTO.getCaracteristicasDomicilioVisitado());
                evaluacionDomiciliaria.setConclusiones(evaluacionDomiciliariaDTO.getConclusiones());
                evaluacionDomiciliaria.setRecomendaciones(evaluacionDomiciliariaDTO.getRecomendaciones());
                evaluacionDomiciliaria.setDinamicaFamiliarDisfuncional(evaluacionDomiciliariaDTO.getDinamicaFamiliarDisfuncional());
                evaluacionDomiciliaria.setCaracteristicasEntornoSocialMC(evaluacionDomiciliariaDTO.getCaracteristicasEntornoSocialMC());
                evaluacionDomiciliaria.setFactoresProtectores(evaluacionDomiciliariaDTO.getFactoresProtectores());
                evaluacionDomiciliaria.setFactoresRiesgoFamilia(evaluacionDomiciliariaDTO.getFactoresRiesgoFamilia());
                evaluacionDomiciliaria.setFactoresRiesgoSocial(evaluacionDomiciliariaDTO.getFactoresRiesgoSocial());
                evaluacionDomiciliaria.setFactoresProtectoresFamilia(evaluacionDomiciliariaDTO.getFactoresProtectoresFamilia());
                evaluacionDomiciliaria.setFactoresProtectoresSocial(evaluacionDomiciliariaDTO.getFactoresProtectoresSocial());

                evaluacionDomiciliaria = this.evaluacionDomiciliariaRepository.save(evaluacionDomiciliaria);
                evaluacionDomiciliariaDTO.setTokenIdentificador(evaluacionDomiciliaria.getTokenIdentificador());

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(evaluacionDomiciliaria.getFichaIdentificacion());
                String identificacionPersona = obtenerIdentificacionPersona(evaluacionDomiciliaria.getFichaIdentificacion());
                
                // Mensaje para el usuario
                String accion = evaluacionDomiciliariaDTO.getEsEdicion() ? "editó" : "creó";
                String mensajeUsuario = "Se " + accion + " con éxito la evaluación domiciliaria de " + nombresCompletos;
                
                // Mensaje para auditoría
                String mensajeAuditoria = "Se " + accion + " con éxito la evaluación domiciliaria de la persona con identificación: " + identificacionPersona;
                
                df.llenarRespuestaExitosa(mensajeUsuario, evaluacionDomiciliariaDTO, mensajeAuditoria);
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
    public RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionDomiciliaria(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistemaLogin = bodyJwtValido.getUsuarioSistema();
            Jerarquia centroUsuario = bodyJwtValido.getJerarquia();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            EvaluacionDomiciliariaDTO evaluacionDomiciliariaDTO = new Gson().fromJson(bodyString, EvaluacionDomiciliariaDTO.class);

            EvaluacionDomiciliaria evaluacionDomiciliaria = this.evaluacionDomiciliariaRepository.findByTokenIdentificadorAndRemovido(
                    evaluacionDomiciliariaDTO.getTokenIdentificador(), false
            );

            if (evaluacionDomiciliaria == null) {
                df.setMensaje("La evaluación domiciliaria no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            // CAMBIO MULTI-JERÁRQUICO: Verificar que la evaluación pertenezca a la misma jerarquía padre
            if (centroUsuario != null && centroUsuario.getJerarquiaPadre() != null 
                && !evaluacionDomiciliaria.getCentro().getJerarquiaPadre().getTokenIdentificador().equals(centroUsuario.getJerarquiaPadre().getTokenIdentificador())) {
                df.setMensaje("No tiene permisos para eliminar esta evaluación domiciliaria");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(evaluacionDomiciliaria.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(evaluacionDomiciliaria.getFichaIdentificacion());

            Date fecha = new Date();
            evaluacionDomiciliaria.setRemovido(true);
            evaluacionDomiciliaria.setIpElimina(ip);
            evaluacionDomiciliaria.setUsuarioSistemaElimina(usuarioSistemaLogin);
            evaluacionDomiciliaria.setFechaEliminacion(fecha);

            this.evaluacionDomiciliariaRepository.save(evaluacionDomiciliaria);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la evaluación domiciliaria de " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito la evaluación domiciliaria de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDTO> removerFicha(HttpServletRequest httpServletRequest, EvaluacionDomiciliariaDTO evaluacionDomiciliariaDTO) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    /**
     * Método auxiliar para obtener la identificación de una evaluación domiciliaria
     */
    private String obtenerIdentificacionEvaluacion(EvaluacionDomiciliaria evaluacionDomiciliaria) {
        if (evaluacionDomiciliaria == null || evaluacionDomiciliaria.getFichaIdentificacion() == null) {
            // Como último recurso, usar la fecha de entrevista si está disponible
            if (evaluacionDomiciliaria != null && evaluacionDomiciliaria.getFechaEntrevista() != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                return "evaluación del " + formatter.format(evaluacionDomiciliaria.getFechaEntrevista());
            }
            return "N/A";
        }

        FichaIdentificacion fichaIdentificacion = evaluacionDomiciliaria.getFichaIdentificacion();
        String identificacion = "N/A";
        
        if (fichaIdentificacion.getDni() != null && !fichaIdentificacion.getDni().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getDni();
        }
        else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getNumeroIdentificacion();
        }
        else if (fichaIdentificacion.getNombres() != null || fichaIdentificacion.getApellidoPaterno() != null) {
            StringBuilder nombreCompleto = new StringBuilder();
            if (fichaIdentificacion.getNombres() != null) {
                nombreCompleto.append(fichaIdentificacion.getNombres());
            }
            if (fichaIdentificacion.getApellidoPaterno() != null) {
                if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                nombreCompleto.append(fichaIdentificacion.getApellidoPaterno());
            }
            if (fichaIdentificacion.getApellidoMaterno() != null) {
                if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                nombreCompleto.append(fichaIdentificacion.getApellidoMaterno());
            }
            if (nombreCompleto.length() > 0) {
                identificacion = nombreCompleto.toString();
            }
        }
        
        // Si aún no hay identificación válida, usar la fecha de entrevista
        if (identificacion.equals("N/A") && evaluacionDomiciliaria.getFechaEntrevista() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            identificacion = "evaluación del " + formatter.format(evaluacionDomiciliaria.getFechaEntrevista());
        }

        return identificacion;
    }
     
    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, MultipartFile multipartFile) {
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            // VALIDACIÓN MULTI-JERÁRQUICA: Obtener centro del usuario
            Jerarquia centroUsuario = bodyJwtValido.getJerarquia();
            if (centroUsuario == null) {
                df.setMensaje("No se pudo determinar la jerarquía del usuario. Contacte al administrador.");
                return df;
            }

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            EvaluacionDomiciliariaDocumentoDTO evaluacionDomiciliariaDocumentoDTO = new Gson().fromJson(bodyDesencriptado, EvaluacionDomiciliariaDocumentoDTO.class);

            EvaluacionDomiciliaria evaluacionDomiciliaria = this.evaluacionDomiciliariaRepository.findByTokenIdentificadorAndRemovido(
                    evaluacionDomiciliariaDocumentoDTO.getTokenIdentificadorEvaluacionDomiciliaria(), false
            );

            if (evaluacionDomiciliaria == null) {
                df.setMensaje("No existe el registro solicitado");
                return df;
            }

            // VALIDACIÓN MULTI-JERÁRQUICA: Verificar que la evaluación pertenezca a la misma jerarquía padre
            if (centroUsuario.getJerarquiaPadre() != null 
                && !evaluacionDomiciliaria.getCentro().getJerarquiaPadre().getTokenIdentificador().equals(centroUsuario.getJerarquiaPadre().getTokenIdentificador())) {
                df.setMensaje("No tiene permisos para subir documentos a esta evaluación domiciliaria");
                return df;
            }

            EvaluacionDomiciliariaCarpeta registroCarpeta = this.evaluacionDomiciliariaCarpetaRepository.findFirstByEvaluacionDomiciliariaTokenIdentificadorAndRemovido(evaluacionDomiciliaria.getTokenIdentificador(), false);

            DocumentoDTO documentoDTO = evaluacionDomiciliariaDocumentoDTO.getDocumentoDTO();

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(evaluacionDomiciliaria.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(evaluacionDomiciliaria.getFichaIdentificacion());

            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();
            if (registroCarpeta == null) {
                // Crear carpeta si no existe
                FichaIdentificacion fichaIdentificacion = evaluacionDomiciliaria.getFichaIdentificacion();

                // Buscar carpeta principal de fichaIdentificación
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                        fichaIdentificacion.getTokenIdentificador(), null, false);

                if (fichaIdentificacionCarpetaPrincipal == null) {
                    df.setMensaje(fallo + ", debido a que no existe la carpeta principal.");
                    return df;
                }

                Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

                // Crear o buscar carpeta para evaluaciones domiciliarias
                String nemonicoEvaluacionDomiciliaria = EtiquetaNemonico.CARPETA_GESTION_ADOLES_EVALUACION_DOMICILIARIA;
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaEvaluacion = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                        fichaIdentificacion.getTokenIdentificador(), nemonicoEvaluacionDomiciliaria, false);

                Carpeta carpetaPadreEvaluaciones;

                if (fichaIdentificacionCarpetaEvaluacion == null) {
                    // Crear carpeta para evaluaciones domiciliarias
                    String nombreCarpetaPrincipal = "Evaluaciones domiciliarias";

                    CarpetaDTO carpetaDTO = new CarpetaDTO();
                    carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                    carpetaDTO.setDescripcion("Carpeta de evaluaciones domiciliarias");
                    CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                    carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                    carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                    RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                    if (!respuestaCarpeta.isExito()) {
                        df.setMensaje(fallo + ", debido a que no se pudo crear la carpeta principal para evaluaciones domiciliarias.");
                        return df;
                    }

                    Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

                    // Crear relación entre la carpeta y la ficha de identificación
                    fichaIdentificacionCarpetaEvaluacion = new FichaIdentificacionCarpeta();
                    fichaIdentificacionCarpetaEvaluacion.setCarpeta(carpetaGuardada);
                    fichaIdentificacionCarpetaEvaluacion.setFichaIdentificacion(fichaIdentificacion);
                    Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoEvaluacionDomiciliaria, false);
                    fichaIdentificacionCarpetaEvaluacion.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                    fichaIdentificacionCarpetaEvaluacion.setFechaCreacion(new Date());
                    fichaIdentificacionCarpetaEvaluacion.setIpCrea(httpServletRequest.getRemoteAddr());
                    fichaIdentificacionCarpetaEvaluacion.setUsuarioSistemaCrea(usuarioSistema);
                    this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaEvaluacion);

                    carpetaPadreEvaluaciones = carpetaGuardada;
                } else {
                    carpetaPadreEvaluaciones = fichaIdentificacionCarpetaEvaluacion.getCarpeta();
                }

                // Crear carpeta específica para esta evaluación domiciliaria
                String nombreCarpeta = "eval_dom_" + evaluacionDomiciliaria.getTokenIdentificador();

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de evaluación domiciliaria relacionada a: " + evaluacionDomiciliaria.getTokenIdentificador());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadreEvaluaciones.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                if (!respuestaCarpeta.isExito()) {
                    df.setMensaje(fallo + ", debido a que no se pudo crear la carpeta específica para la evaluación domiciliaria.");
                    return df;
                }

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

                // Crear relación entre la carpeta y la evaluación domiciliaria
                registroCarpeta = new EvaluacionDomiciliariaCarpeta();
                registroCarpeta.setCarpeta(carpetaGuardada);
                registroCarpeta.setEvaluacionDomiciliaria(evaluacionDomiciliaria);
                registroCarpeta.setFechaCreacion(new Date());
                registroCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
                registroCarpeta.setUsuarioSistemaCrea(usuarioSistema);
                this.evaluacionDomiciliariaCarpetaRepository.save(registroCarpeta);
            }

            Carpeta carpeta = registroCarpeta.getCarpeta();

            String idNode = carpeta.getIdentificadorAlfresco();
            RespuestaPorDefectoAuditoria<DocumentoDTO> df3 = this.documentoService.subirDocumentoAlfresco(
                    httpServletRequest,
                    idNode,
                    multipartFile,
                    documentoDTO
            );

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            documentoDTO = df3.getData();
            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    documentoDTO.getTokenIdentificador(), false
            );

            EvaluacionDomiciliariaDocumento evaluacionDomiciliariaDocumento = new EvaluacionDomiciliariaDocumento();
            evaluacionDomiciliariaDocumento.setDocumento(documento);
            evaluacionDomiciliariaDocumento.setEvaluacionDomiciliaria(evaluacionDomiciliaria);
            evaluacionDomiciliariaDocumento.setCarpeta(carpeta);
            evaluacionDomiciliariaDocumento.setUsuarioSistemaCrea(usuarioSistema);
            evaluacionDomiciliariaDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.evaluacionDomiciliariaDocumentoRepository.save(evaluacionDomiciliariaDocumento);

            // Mensaje para el usuario
            String mensajeUsuario = "Se subió con éxito el documento " + documento.getNombreReal() + " a la evaluación domiciliaria de " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se subió con éxito el documento " + documento.getNombreReal() + " a la evaluación domiciliaria de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, documentoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, EvaluacionDomiciliariaDocumentosRequest evaluacionDomiciliariaDocumentosRequest) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            // VALIDACIÓN MULTI-JERÁRQUICA: Obtener centro del usuario
            Jerarquia centroUsuario = bodyJwtValido.getJerarquia();
            if (centroUsuario == null) {
                df.setMensaje("No se pudo determinar la jerarquía del usuario. Contacte al administrador.");
                return df;
            }

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            EvaluacionDomiciliaria evaluacionDomiciliaria = this.evaluacionDomiciliariaRepository.findByTokenIdentificadorAndRemovido(
                    evaluacionDomiciliariaDocumentosRequest.getTokenIdentificadorEvaluacionDomiciliaria(),
                    false
            );

            if (evaluacionDomiciliaria == null) {
                df.setMensaje("El registro es inválido");
                return df;
            }

            // VALIDACIÓN MULTI-JERÁRQUICA: Verificar que la evaluación pertenezca a la misma jerarquía padre
            if (centroUsuario.getJerarquiaPadre() != null 
                && !evaluacionDomiciliaria.getCentro().getJerarquiaPadre().getTokenIdentificador().equals(centroUsuario.getJerarquiaPadre().getTokenIdentificador())) {
                df.setMensaje("No tiene permisos para ver los documentos de esta evaluación domiciliaria");
                return df;
            }

            Pageable pageable = PageRequest.of(evaluacionDomiciliariaDocumentosRequest.getPage(),
                    evaluacionDomiciliariaDocumentosRequest.getSize());
            Page<EvaluacionDomiciliariaDocumento> evaluacionDomiciliariaDocumentoPage =
                    this.evaluacionDomiciliariaDocumentoRepository.findByEvaluacionDomiciliariaTokenIdentificadorAndRemovido(
                            evaluacionDomiciliaria.getTokenIdentificador(),
                            false,
                            pageable
                    );
            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<EvaluacionDomiciliariaDocumento> evaluacionDomiciliariaDocumentos = evaluacionDomiciliariaDocumentoPage.toList();

            for (EvaluacionDomiciliariaDocumento evaluacionDomiciliariaDocumento : evaluacionDomiciliariaDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();
                Documento documento = evaluacionDomiciliariaDocumento.getDocumento();

                documentoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
                Catalogo tipoDeDocumentoSistema = documento.getTipoDeDocumentoSistema();
                CatalogoDTO tipoDeDocumentoSistemaDTO = tipoDeDocumentoSistema.convertirADTO();

                documentoDTO.setTipoDocumentoSistema(tipoDeDocumentoSistemaDTO);
                documentoDTO.setNombre(documento.getNombreReal());
                documentoDTO.setTokenIdentificador(documento.getTokenIdentificador());
                documentoDTO.setDescripcion(documento.getDescripcion());
                documentoDTO.setFechaCreacion(documento.getFechaCreacion());
                documentoDTO.setMimeType(documento.getMimeType());
                documentoDTO.setTamanioBytes(documento.getTamanioByteDocumento());
                documentoDTO.setTipoDeDocumentoSistemaOtro(documento.getTipoDeDocumentoSistemaOtro());

                documentoList.add(documentoDTO);
            }

            PaginacionResponse paginacionResponse = new PaginacionResponse();
            paginacionResponse.setData(documentoList);
            paginacionResponse.setTotalItems(evaluacionDomiciliariaDocumentoPage.getTotalElements());

            // Mensaje para el usuario usando jerarquía padre
            String mensajeUsuario = "Obteniendo " + evaluacionDomiciliariaDocumentoPage.getTotalElements() + " documentos asociados a la evaluación domiciliaria de centros " + centroUsuario.getJerarquiaPadre().getNombre();

            // Mensaje para auditoría usando jerarquía padre
            String mensajeAuditoria = "Se han encontrado un total de " + evaluacionDomiciliariaDocumentoPage.getTotalElements() + " documentos asociados a la evaluación domiciliaria de centros " + centroUsuario.getJerarquiaPadre().getNombre();

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, EvaluacionDomiciliariaDocumentoDTO evaluacionDomiciliariaDocumentoDTO) {
        RespuestaPorDefectoAuditoria<EvaluacionDomiciliariaDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(df2.getLogOut());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            // VALIDACIÓN MULTI-JERÁRQUICA: Obtener centro del usuario
            Jerarquia centroUsuario = bodyJwtValido.getJerarquia();
            if (centroUsuario == null) {
                df.setMensaje("No se pudo determinar la jerarquía del usuario. Contacte al administrador.");
                return df;
            }

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String tokenDoc = evaluacionDomiciliariaDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenEvaluacionDomiciliaria = evaluacionDomiciliariaDocumentoDTO.getTokenIdentificadorEvaluacionDomiciliaria();

            EvaluacionDomiciliariaDocumento evaluacionDomiciliariaDocumento = this.
                    evaluacionDomiciliariaDocumentoRepository.findFirstByEvaluacionDomiciliariaTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenEvaluacionDomiciliaria,
                            tokenDoc,
                            false
                    );

            if (evaluacionDomiciliariaDocumento == null) {
                df.setMensaje("La relación entre el documento y la evaluación domiciliaria no existe o ya fue eliminada anteriormente");
                return df;
            }

            // VALIDACIÓN MULTI-JERÁRQUICA: Verificar que la evaluación pertenezca a la misma jerarquía padre
            EvaluacionDomiciliaria evaluacionDomiciliaria = evaluacionDomiciliariaDocumento.getEvaluacionDomiciliaria();
            if (centroUsuario.getJerarquiaPadre() != null 
                && !evaluacionDomiciliaria.getCentro().getJerarquiaPadre().getTokenIdentificador().equals(centroUsuario.getJerarquiaPadre().getTokenIdentificador())) {
                df.setMensaje("No tiene permisos para eliminar documentos de esta evaluación domiciliaria");
                return df;
            }

            Documento documento = evaluacionDomiciliariaDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(evaluacionDomiciliaria.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(evaluacionDomiciliaria.getFichaIdentificacion());

            evaluacionDomiciliariaDocumento.setRemovido(true);
            evaluacionDomiciliariaDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            evaluacionDomiciliariaDocumento.setFechaEliminacion(new Date());
            evaluacionDomiciliariaDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.evaluacionDomiciliariaDocumentoRepository.save(evaluacionDomiciliariaDocumento);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito el documento " + documento.getNombreReal() + " de la evaluación domiciliaria de " + nombresCompletos;

            // Mensaje para auditoría usando jerarquía padre
            String mensajeAuditoria = "Se eliminó con éxito el documento " + documento.getNombreReal() + " de la evaluación domiciliaria de la persona con identificación: " + identificacionPersona + " (Centros " + centroUsuario.getJerarquiaPadre().getNombre() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, evaluacionDomiciliariaDocumentoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
