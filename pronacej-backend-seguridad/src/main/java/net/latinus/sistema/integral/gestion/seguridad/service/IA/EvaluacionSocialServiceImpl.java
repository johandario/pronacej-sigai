package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSocial;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSocialArtefacto;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSocialArtefactoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSocialDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PersonaRelacionadaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionSocialArtefactoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionSocialRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PersonaRelacionadaRepository;

@Service
@Transactional
@AllArgsConstructor
public class EvaluacionSocialServiceImpl implements EvaluacionSocialService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private EvaluacionSocialRepository evaluacionSocialRepository;
    private PersonaRelacionadaRepository personaRelacionadaRepository;
    private EvaluacionSocialArtefactoRepository evaluacionSocialArtefactoRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialDTO>> obtenerEvaluacionesSociales(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación JWT y desencriptado
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            // Configuración de ordenamiento
            String sortField = paginacionRequest.getSort();
            String direction = paginacionRequest.getDirection();

            Sort sort = Sort.by("idEvaluacionSocial").descending();

            if (sortField != null && !sortField.isEmpty() && direction != null && !direction.isEmpty()) {
                // Mapeo de campos para ordenamiento
                if ("usuarioRegistro".equals(sortField)) {
                    sortField = "usuarioSistemaCrea.nombres";
                } else if ("fechaCreacion".equals(sortField)) {
                    sortField = "fechaCreacion";
                } else if ("grupoAmical".equals(sortField)) {
                    sortField = "grupoAmical";
                } else if ("factoresRiesgoMedio".equals(sortField)) {
                    sortField = "factorRiesgoMedio";
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
            Page<EvaluacionSocial> evaluacionSocialPage;

            if (filtro != null && !filtro.isEmpty()) {
                // Preparar filtro para búsqueda LIKE
                String filtroLike = "%" + filtro.toLowerCase() + "%";

                // Usar el método de búsqueda con filtro
                evaluacionSocialPage = this.evaluacionSocialRepository.buscarPorFiltroGeneral(
                        paginacionRequest.getTokenIdentificador(),
                        empresa.getIdEmpresa(),
                        filtroLike,
                        pageable
                );
            } else {
                // Sin filtro, usar el método normal
                evaluacionSocialPage = this.evaluacionSocialRepository.findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                        paginacionRequest.getTokenIdentificador(),
                        empresa.getIdEmpresa(),
                        false,
                        pageable
                );
            }

            // Mapeo a DTOs
            PaginacionResponse<EvaluacionSocialDTO> paginacionResponse = new PaginacionResponse<>();
            List<EvaluacionSocialDTO> evaluacionSocialDTOList = new ArrayList<>();

            for (EvaluacionSocial evaluacionSocial : evaluacionSocialPage.getContent()) {
                EvaluacionSocialDTO evaluacionSocialDTO = new EvaluacionSocialDTO();
                evaluacionSocialDTO.setTokenIdentificador(evaluacionSocial.getTokenIdentificador());
                evaluacionSocialDTO.setTokenIdentificadorEmpresa(evaluacionSocial.getEmpresa().getTokenIdentificador());
                evaluacionSocialDTO.setFechaCreacion(evaluacionSocial.getFechaCreacion());

                if(evaluacionSocial.getZonaVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorZonaVivienda(evaluacionSocial.getZonaVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getSubZona()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorSubZona(evaluacionSocial.getSubZona().getTokenIdentificador());
                }
                if(evaluacionSocial.getMaterialParedVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorMaterialParedVivienda(evaluacionSocial.getMaterialParedVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getMaterialPisoVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorMaterialPisoVivienda(evaluacionSocial.getMaterialPisoVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getMaterialTechoVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorMaterialTechoVivienda(evaluacionSocial.getMaterialTechoVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getAbastecimientoAguaVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorAbastecimientoAguaVivienda(evaluacionSocial.getAbastecimientoAguaVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getTipoVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorTipoVivienda(evaluacionSocial.getTipoVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getTipoAlumbradoVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorTipoAlumbradoVivienda(evaluacionSocial.getTipoAlumbradoVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getCombustibleCocinarVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorCombustibleCocinarVivienda(evaluacionSocial.getCombustibleCocinarVivienda().getTokenIdentificador());
                }
                if(evaluacionSocial.getTipoDesagueVivienda()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorTipoDesagueVivienda(evaluacionSocial.getTipoDesagueVivienda().getTokenIdentificador());
                }

                if(evaluacionSocial.getTenencia()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorTenencia(evaluacionSocial.getTenencia().getTokenIdentificador());
                }

                if(evaluacionSocial.getOtrosServicios()!=null) {
                    evaluacionSocialDTO.setTokenIdentificadorOtrosServicios(evaluacionSocial.getOtrosServicios().getTokenIdentificador());
                }

                evaluacionSocialDTO.setNumeroAmbientes(evaluacionSocial.getNumeroAmbientes());
                evaluacionSocialDTO.setNumeroOcupantes(evaluacionSocial.getNumeroOcupantes());
                evaluacionSocialDTO.setNumeroHabitaciones(evaluacionSocial.getNumeroHabitaciones());
                evaluacionSocialDTO.setNumeroDormitorios(evaluacionSocial.getNumeroDormitorios());
                evaluacionSocialDTO.setGrupoAmical(evaluacionSocial.getGrupoAmical());
                evaluacionSocialDTO.setFactorRiesgoMedio(evaluacionSocial.getFactorRiesgoMedio());
                evaluacionSocialDTO.setAreaAcademicoLaboral(evaluacionSocial.getAreaAcademicoLaboral());
                evaluacionSocialDTO.setAreaSocialRecreacional(evaluacionSocial.getAreaSocialRecreacional());
                evaluacionSocialDTO.setAreaFamiliarPareja(evaluacionSocial.getAreaFamiliarPareja());
                evaluacionSocialDTO.setAreaPersonal(evaluacionSocial.getAreaPersonal());

                evaluacionSocialDTO.setNombreCompletoUsuarioCreacion(evaluacionSocial.getUsuarioSistemaCrea().getNombres() + " " + evaluacionSocial.getUsuarioSistemaCrea().getApellidos());

                if (evaluacionSocial.getFichaIdentificacion() != null) {
                    evaluacionSocialDTO.setTokenIdentificadorFichaIdentificacion(evaluacionSocial.getFichaIdentificacion().getTokenIdentificador());
                }

                evaluacionSocialDTOList.add(evaluacionSocialDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            evaluacionSocialDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(evaluacionSocialDTOList);
            paginacionResponse.setTotalItems(evaluacionSocialPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + evaluacionSocialPage.getTotalElements() + " situaciones económicas y entorno social";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + evaluacionSocialPage.getTotalElements() + " situaciones económicas y entorno social registradas";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionSocialDTO> crearEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionSocialDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            EvaluacionSocialDTO evaluacionSocialDTO = new Gson().fromJson(bodyString, EvaluacionSocialDTO.class);
            
            evaluacionSocialDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            
            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();
            
            EvaluacionSocial evaluacionSocial;
            boolean esEdicion = false;
            
            if(evaluacionSocialDTO.getEsEdicion()){
                evaluacionSocial = evaluacionSocialRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificador(), Boolean.FALSE);
                if (evaluacionSocial == null) {
                    df.setMensaje("La evaluación social a editar no existe o ya fue eliminada anteriormente");
                    return df;
                }
                evaluacionSocial.setFechaEdicion(new Date());
                evaluacionSocial.setIpEdita(ip);
                evaluacionSocial.setUsuarioSistemaEdita(usuarioLogin);
                esEdicion = true;
            }else{                
                evaluacionSocial = new EvaluacionSocial();
                evaluacionSocial.setFechaCreacion(new Date());
                evaluacionSocial.setIpCrea(ip);
                evaluacionSocial.setUsuarioSistemaCrea(usuarioLogin);
                evaluacionSocial.setEmpresa(empresa);
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);
                evaluacionSocial.setFichaIdentificacion(fichaIdentificacion);
            }
            
            Catalogo zonaVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorZonaVivienda(), Boolean.FALSE);
            evaluacionSocial.setZonaVivienda(zonaVivienda);
            Catalogo subZona = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorSubZona(), Boolean.FALSE);
            evaluacionSocial.setSubZona(subZona);
            Catalogo materialParedVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorMaterialParedVivienda(), Boolean.FALSE);
            evaluacionSocial.setMaterialParedVivienda(materialParedVivienda);
            Catalogo materialPisoVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorMaterialPisoVivienda(), Boolean.FALSE);
            evaluacionSocial.setMaterialPisoVivienda(materialPisoVivienda);
            Catalogo materialTechoVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorMaterialTechoVivienda(), Boolean.FALSE);
            evaluacionSocial.setMaterialTechoVivienda(materialTechoVivienda);
            Catalogo abastecimientoAguaVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorAbastecimientoAguaVivienda(), Boolean.FALSE);
            evaluacionSocial.setAbastecimientoAguaVivienda(abastecimientoAguaVivienda);
            Catalogo tipoVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorTipoVivienda(), Boolean.FALSE);
            evaluacionSocial.setTipoVivienda(tipoVivienda);
            Catalogo tipoAlumbradoVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorTipoAlumbradoVivienda(), Boolean.FALSE);
            evaluacionSocial.setTipoAlumbradoVivienda(tipoAlumbradoVivienda);
            Catalogo combustibleCocinarVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorCombustibleCocinarVivienda(), Boolean.FALSE);
            evaluacionSocial.setCombustibleCocinarVivienda(combustibleCocinarVivienda);
            Catalogo tipoDesguaceVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorTipoDesagueVivienda(), Boolean.FALSE);
            evaluacionSocial.setTipoDesagueVivienda(tipoDesguaceVivienda);
            Catalogo tenencia = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorTenencia(), Boolean.FALSE);
            evaluacionSocial.setTenencia(tenencia);
            Catalogo otrosServicios = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificadorOtrosServicios(), Boolean.FALSE);
            evaluacionSocial.setOtrosServicios(otrosServicios);
            evaluacionSocial.setNumeroAmbientes(evaluacionSocialDTO.getNumeroAmbientes());
            evaluacionSocial.setNumeroOcupantes(evaluacionSocialDTO.getNumeroOcupantes());
            evaluacionSocial.setNumeroHabitaciones(evaluacionSocialDTO.getNumeroHabitaciones());
            evaluacionSocial.setNumeroDormitorios(evaluacionSocialDTO.getNumeroDormitorios());
            evaluacionSocial.setGrupoAmical(evaluacionSocialDTO.getGrupoAmical());
            evaluacionSocial.setFactorRiesgoMedio(evaluacionSocialDTO.getFactorRiesgoMedio());
            evaluacionSocial.setAreaAcademicoLaboral(evaluacionSocialDTO.getAreaAcademicoLaboral());
            evaluacionSocial.setAreaSocialRecreacional(evaluacionSocialDTO.getAreaSocialRecreacional());
            evaluacionSocial.setAreaFamiliarPareja(evaluacionSocialDTO.getAreaFamiliarPareja());
            evaluacionSocial.setAreaPersonal(evaluacionSocialDTO.getAreaPersonal());
            
            evaluacionSocial = this.evaluacionSocialRepository.save(evaluacionSocial);
            evaluacionSocialDTO.setTokenIdentificador(evaluacionSocial.getTokenIdentificador());
            
            // Procesar personas relacionadas
            if (evaluacionSocialDTO.getListaPersonasRelacionadas() != null) {
                for (PersonaRelacionadaDTO personaRelacionadaDTO : evaluacionSocialDTO.getListaPersonasRelacionadas()) {
                    PersonaRelacionada personaRelacionada = personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getTokenIdentificador(), Boolean.FALSE);
                    personaRelacionada.setFechaEdicion(new Date());
                    personaRelacionada.setIpEdita(ip);
                    personaRelacionada.setUsuarioSistemaEdita(usuarioLogin);
                    personaRelacionada.setEmpresa(empresa);
                    personaRelacionada.setTokenIdentificador(personaRelacionadaDTO.getTokenIdentificador());

                    Catalogo condicionLaboral = catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaDTO.getTokenIdentificadorCondicionLaboral(), Boolean.FALSE);
                    personaRelacionada.setCondicionLaboral(condicionLaboral);
                    personaRelacionada.setOtros(personaRelacionadaDTO.getOtros());
                    personaRelacionada.setIngresoPromedio(personaRelacionadaDTO.getIngresoPromedio());
                    personaRelacionada.setNumeroHijos(personaRelacionadaDTO.getNumeroHijos());
                    personaRelacionada.setObservaciones(personaRelacionadaDTO.getObservaciones());
                    personaRelacionada.setEsResponsableEconom(personaRelacionadaDTO.getEsResponsableEconom());
                    personaRelacionada.setOcupacion(personaRelacionadaDTO.getOcupacion());

                    personaRelacionada.setEvaluacionSocial(evaluacionSocial);

                    personaRelacionada = this.personaRelacionadaRepository.save(personaRelacionada);
                    personaRelacionadaDTO.setTokenIdentificador(personaRelacionada.getTokenIdentificador());
                }
            }
            
            // Procesar artefactos con validaciones mejoradas
            if (evaluacionSocialDTO.getListaArtefactos() != null) {
                for (EvaluacionSocialArtefactoDTO evaluacionSocialArtefactoDTO : evaluacionSocialDTO.getListaArtefactos()) {
                    EvaluacionSocialArtefacto evaluacionSocialArtefacto;
                    
                    if (evaluacionSocialArtefactoDTO.getTokenIdentificador() == null || 
                        evaluacionSocialArtefactoDTO.getTokenIdentificador().equals("0")) {
                        // Crear nuevo artefacto
                        evaluacionSocialArtefacto = new EvaluacionSocialArtefacto();
                        evaluacionSocialArtefacto.setFechaCreacion(new Date());
                        evaluacionSocialArtefacto.setIpCrea(ip);
                        evaluacionSocialArtefacto.setUsuarioSistemaCrea(usuarioLogin);
                        evaluacionSocialArtefacto.setEmpresa(empresa);
                    } else {
                        // Editar artefacto existente
                        evaluacionSocialArtefacto = evaluacionSocialArtefactoRepository.findByTokenIdentificadorAndRemovido(
                            evaluacionSocialArtefactoDTO.getTokenIdentificador(), Boolean.FALSE);
                        
                        if (evaluacionSocialArtefacto == null) {
                            // Si no se encuentra el artefacto para editar, crear uno nuevo
                            evaluacionSocialArtefacto = new EvaluacionSocialArtefacto();
                            evaluacionSocialArtefacto.setFechaCreacion(new Date());
                            evaluacionSocialArtefacto.setIpCrea(ip);
                            evaluacionSocialArtefacto.setUsuarioSistemaCrea(usuarioLogin);
                            evaluacionSocialArtefacto.setEmpresa(empresa);
                        } else {
                            evaluacionSocialArtefacto.setFechaEdicion(new Date());
                            evaluacionSocialArtefacto.setIpEdita(ip);
                            evaluacionSocialArtefacto.setUsuarioSistemaEdita(usuarioLogin);
                        }
                    }

                    Catalogo artefactosVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(
                        evaluacionSocialArtefactoDTO.getTokenIdentificadorArtefactosVivienda(), Boolean.FALSE);
                    
                    if (artefactosVivienda == null) {
                        continue; // Saltar este artefacto si no se encuentra el catálogo
                    }
                    
                    evaluacionSocialArtefacto.setArtefactosVivienda(artefactosVivienda);
                    evaluacionSocialArtefacto.setCantidad(evaluacionSocialArtefactoDTO.getCantidad());
                    evaluacionSocialArtefacto.setEvaluacionSocial(evaluacionSocial);

                    evaluacionSocialArtefacto = this.evaluacionSocialArtefactoRepository.save(evaluacionSocialArtefacto);
                    evaluacionSocialArtefactoDTO.setTokenIdentificador(evaluacionSocialArtefacto.getTokenIdentificador());
                }
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(evaluacionSocial.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String accion = esEdicion ? "editó" : "creó";
            String mensajeUsuario = "Se " + accion + " con éxito la situación económica y entorno social de " + nombresCompletos;
            
            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(evaluacionSocial.getFichaIdentificacion());
            String mensajeAuditoria = "Se " + accion + " con éxito la situación económica y entorno social de la persona con identificación: " + identificacionPersona;
            
            df.llenarRespuestaExitosa(mensajeUsuario, evaluacionSocialDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            EvaluacionSocialDTO evaluacionSocialDTO = new Gson().fromJson(bodyString, EvaluacionSocialDTO.class);

            EvaluacionSocial evaluacionSocial = this.evaluacionSocialRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialDTO.getTokenIdentificador()
                    , false);

            if (evaluacionSocial == null) {
                df.setMensaje("La evaluación social no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(evaluacionSocial.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(evaluacionSocial.getFichaIdentificacion());

            Date fecha = new Date();
            evaluacionSocial.setRemovido(true);
            evaluacionSocial.setIpElimina(ip);
            evaluacionSocial.setUsuarioSistemaElimina(usuarioSistemaLogin);
            evaluacionSocial.setFechaEliminacion(fecha);

            this.evaluacionSocialRepository.save(evaluacionSocial);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la situación económica y entorno social de " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito la situación económica y entorno social de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Método auxiliar para obtener nombres completos de una ficha
     */
    private String obtenerNombresCompletos(FichaIdentificacion ficha) {
        if (ficha == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (ficha.getNombres() != null && !ficha.getNombres().trim().isEmpty()) {
            nombreCompleto.append(ficha.getNombres());
        }
        if (ficha.getApellidoPaterno() != null && !ficha.getApellidoPaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(ficha.getApellidoPaterno());
        }
        if (ficha.getApellidoMaterno() != null && !ficha.getApellidoMaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(ficha.getApellidoMaterno());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una persona a partir de su ficha de identificación
     */
    private String obtenerIdentificacionPersona(FichaIdentificacion ficha) {
        if (ficha == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        // Primero intentar con el campo dni
        if (ficha.getDni() != null && !ficha.getDni().trim().isEmpty()) {
            identificacion = ficha.getDni();
        } 
        // Si no hay dni, intentar con numeroIdentificacion
        else if (ficha.getNumeroIdentificacion() != null && !ficha.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = ficha.getNumeroIdentificacion();
        }
        // Si no hay ninguno, usar nombres y apellidos como identificación
        else {
            String nombresCompletos = obtenerNombresCompletos(ficha);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }
    
}
