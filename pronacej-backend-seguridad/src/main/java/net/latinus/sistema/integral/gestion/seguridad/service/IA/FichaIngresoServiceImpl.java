package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.HistoricoFichaIdentificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
@AllArgsConstructor
public class FichaIngresoServiceImpl implements FichaIngresoService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIngresoRepository fichaIngresoRepository;
    private JerarquiaRepository jerarquiaRepository;
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private PersonaRelacionadaRepository personaRelacionadaRepository;
    private DatosHijoIngresadoRepository datosHijoIngresadoRepository;
    private FichaIdentificacionPersonaRelacionadaRepository fichaIdentificacionPersonaRelacionadaRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private FichaIngresoCarpetaRepository fichaIngresoCarpetaRepository;

    private HistoricoFichaIdentificacionService historicoFichaIdentificacionService;
    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIngresoDTO>> obtenerFichasIngreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIngresoDTO>> df = new RespuestaPorDefectoAuditoria<>();
        
        try {
            
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            String orderBy ="idFichaIngreso";

            if(!ObjectUtils.isEmpty(paginacionRequest.getSort())) {
                orderBy = paginacionRequest.getSort();
                if(orderBy.equals("centro")) {
                    orderBy = "centro.nombre";
                }

                if(orderBy.equals("nombreSeguro")) {
                    orderBy = "seguroSalud.nombre";
                }


            }

            Sort.Direction sortDirection = Sort.Direction.DESC;

            if(!ObjectUtils.isEmpty(paginacionRequest.getDirection())) {
                sortDirection = "desc".equalsIgnoreCase(paginacionRequest.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            }

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by(sortDirection,orderBy)
            );


            Page<FichaIngreso> fichaIngresoPage = this.fichaIngresoRepository.buscarPorCentroOSeguro(
                    empresa.getIdEmpresa(), paginacionRequest.getFilter() , paginacionRequest.getTokenIdentificador(), pageable);
            
            PaginacionResponse<FichaIngresoDTO> paginacionResponse = new PaginacionResponse<>();
            List<FichaIngresoDTO> fichaIngresoDTOList = new ArrayList<>();
            for (FichaIngreso fichaIngreso : fichaIngresoPage.toList()) {
                FichaIngresoDTO fichaIngresoDTO = new FichaIngresoDTO();
                fichaIngresoDTO.setTokenIdentificador(fichaIngreso.getTokenIdentificador());
                fichaIngresoDTO.setTokenIdentificadorEmpresa(fichaIngreso.getEmpresa().getTokenIdentificador());
                // Para saber el tipo de centro obtenemos el padre de la jerarquia del centro, ya que esto funciona como niveles
                JerarquiaDTO tipoCentro = new JerarquiaDTO();
                tipoCentro.setNombre(fichaIngreso.getCentro().getJerarquiaPadre().getNombre());
                tipoCentro.setNemonico(fichaIngreso.getCentro().getJerarquiaPadre().getNemonico());
                JerarquiaDTO centro = new JerarquiaDTO();
                centro.setId(fichaIngreso.getCentro().getIdJerarquia());
                centro.setNombre(fichaIngreso.getCentro().getNombre());
                centro.setUbigeo(fichaIngreso.getCentro().getUbigeo());
                centro.setJerarquiaPadre(tipoCentro);
                if(!ObjectUtils.isEmpty(fichaIngreso.getCentro())){
                    centro.setGenero(catalogoToDTO(fichaIngreso.getCentro().getGenero()));
                    centro.setDireccion(fichaIngreso.getCentro().getDireccion());

                }
                
                fichaIngresoDTO.setFechaIngreso(fichaIngreso.getFechaIngreso());
                fichaIngresoDTO.setFechaCreacion(fichaIngreso.getFechaCreacion());
                fichaIngresoDTO.setCentro(centro);
                fichaIngresoDTO.setAtencionSalud(fichaIngreso.getAtencionSalud());
                fichaIngresoDTO.setAtencionSalud(fichaIngreso.getAtencionSalud());
                fichaIngresoDTO.setMotivo(fichaIngreso.getMotivo());
                fichaIngresoDTO.setObservaciones(fichaIngreso.getObservaciones());
                fichaIngresoDTO.setResponsableInscripcion(fichaIngreso.getResponsableInscripcion());
                fichaIngresoDTO.setCaracteristicasParticulares(fichaIngreso.getCaracteristicasParticulares());
                if(fichaIngreso.getProgramaDerivado()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorProgramaDerivado(fichaIngreso.getProgramaDerivado().getTokenIdentificador());
                }
                if(fichaIngreso.getTutor()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorTutor(fichaIngreso.getTutor().getTokenIdentificador());
                }
                fichaIngresoDTO.setLesiones(fichaIngreso.getLesiones());
                fichaIngresoDTO.setEspecificarZonaLesiones(fichaIngreso.getEspecificarZonaLesiones());
                fichaIngresoDTO.setMoretones(fichaIngreso.getMoretones());
                fichaIngresoDTO.setEspecificarZonaMoretones(fichaIngreso.getEspecificarZonaMoretones());
                fichaIngresoDTO.setCicatrices(fichaIngreso.getCicatrices());
                fichaIngresoDTO.setEspecificarZonaCicatrices(fichaIngreso.getEspecificarZonaCicatrices());
                fichaIngresoDTO.setTatuajes(fichaIngreso.getTatuajes());
                fichaIngresoDTO.setEspecificarZonaTatuajes(fichaIngreso.getEspecificarZonaTatuajes());
                fichaIngresoDTO.setPiercing(fichaIngreso.getPiercing());
                fichaIngresoDTO.setEspecificarZonaPiercing(fichaIngreso.getEspecificarZonaPiercing());
                fichaIngresoDTO.setOtros(fichaIngreso.getOtros());
                fichaIngresoDTO.setEspecificarZonaOtros(fichaIngreso.getEspecificarZonaOtros());
                fichaIngresoDTO.setVictimaAgresion(fichaIngreso.getVictimaAgresion());
                fichaIngresoDTO.setEspecificarAgresion(fichaIngreso.getEspecificarAgresion());
                if(fichaIngreso.getSeguroSalud()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorSeguroSalud(fichaIngreso.getSeguroSalud().getTokenIdentificador());
                    fichaIngresoDTO.setNombreSeguro(fichaIngreso.getSeguroSalud().getNombre());
                }
                if(fichaIngreso.getFormaCabeza()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorFormaCabeza(fichaIngreso.getFormaCabeza().getTokenIdentificador());
                }
                if(fichaIngreso.getFormaNariz()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorFormaNariz(fichaIngreso.getFormaNariz().getTokenIdentificador());
                }
                if(fichaIngreso.getFormaLabios()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorFormaLabios(fichaIngreso.getFormaLabios().getTokenIdentificador());
                }
                if(fichaIngreso.getFormaCuerpo()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorFormaCuerpo(fichaIngreso.getFormaCuerpo().getTokenIdentificador());
                }
                if(fichaIngreso.getAnomaliaOjos()!=null) {
                    fichaIngresoDTO.setTokenIdentificadorAnomaliaOjos(fichaIngreso.getAnomaliaOjos().getTokenIdentificador());
                }
                fichaIngresoDTO.setEsEmbarazada(fichaIngreso.getEsEmbarazada());
                fichaIngresoDTO.setMesesEmbarazo(fichaIngreso.getMesesEmbarazo());
                fichaIngresoDTO.setIngresaConHijo(fichaIngreso.getIngresaConHijo());
                
                if (Boolean.TRUE.equals(fichaIngreso.getIngresaConHijo())) {
                    DatosHijoIngresado datosHijoIngresado = datosHijoIngresadoRepository.findByFichaIngresoTokenIdentificador(fichaIngreso.getTokenIdentificador());
                    if (datosHijoIngresado != null) {
                        PersonaRelacionada personaRelacionada = personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(datosHijoIngresado.getPersonaRelacionada().getTokenIdentificador(), Boolean.FALSE);
                        
                        if (personaRelacionada != null) { 
                            DatosHijoIngresadoDTO datosHijoIngresadoDTO = new DatosHijoIngresadoDTO();

                            // Datos personales
                            datosHijoIngresadoDTO.setHijoApellidoPaterno(personaRelacionada.getPrimerApellido());
                            datosHijoIngresadoDTO.setHijoApellidoMaterno(personaRelacionada.getSegundoApellido());
                            datosHijoIngresadoDTO.setHijoPrimerNombre(personaRelacionada.getPrimerNombre());
                            datosHijoIngresadoDTO.setHijoSegundoNombre(personaRelacionada.getSegundoNombre());
                            datosHijoIngresadoDTO.setHijoFechaNacimiento(personaRelacionada.getFechaNacimiento());
                            datosHijoIngresadoDTO.setHijoDNI(personaRelacionada.getIdentificacion());

                            // Datos de agresión
                            datosHijoIngresadoDTO.setHijoVictimaAgresion(datosHijoIngresado.getHijoVictimaAgresion());
                            datosHijoIngresadoDTO.setHijoEspecificarAgresion(datosHijoIngresado.getHijoEspecificarAgresion());

                            // Moretones
                            datosHijoIngresadoDTO.setHijoMoretones(datosHijoIngresado.getHijoMoretones());
                            datosHijoIngresadoDTO.setHijoEspecificarZonaMoretones(datosHijoIngresado.getHijoEspecificarZonaMoretones());

                            // Cicatrices
                            datosHijoIngresadoDTO.setHijoCicatrices(datosHijoIngresado.getHijoCicatrices());
                            datosHijoIngresadoDTO.setHijoEspecificarZonaCicatrices(datosHijoIngresado.getHijoEspecificarZonaCicatrices());

                            // Tatuajes
                            datosHijoIngresadoDTO.setHijoTatuajes(datosHijoIngresado.getHijoTatuajes());
                            datosHijoIngresadoDTO.setHijoEspecificarZonaTatuajes(datosHijoIngresado.getHijoEspecificarZonaTatuajes());

                            // Otros datos
                            datosHijoIngresadoDTO.setHijoOtroEspecificar(datosHijoIngresado.getHijoOtroEspecificar());
                            datosHijoIngresadoDTO.setHijoObservaciones(datosHijoIngresado.getHijoObservaciones());

                            // Token identificador de la ficha de ingreso
                            datosHijoIngresadoDTO.setTokenIdentificadorFichaIngreso(fichaIngreso.getTokenIdentificador());

                            // Token identificador de la persona relacionada
                            datosHijoIngresadoDTO.setTokenIdentificadorPersonaRelacionada(personaRelacionada.getTokenIdentificador());
                            datosHijoIngresadoDTO.setTokenIdentificador(datosHijoIngresado.getTokenIdentificador());

                            if(!ObjectUtils.isEmpty(personaRelacionada.getTipoSexoBiologico())){
                                datosHijoIngresadoDTO.setHijoTipoSexo(personaRelacionada.getTipoSexoBiologico().getTokenIdentificador());
                            }



                            fichaIngresoDTO.setDatosHijoIngresado(datosHijoIngresadoDTO);
                        }
                    }
                }
                if (fichaIngreso.getFichaIdentificacion() != null) {
                    fichaIngresoDTO.setTokenIdentificadorFichaIdentificacion(fichaIngreso.getFichaIdentificacion().getTokenIdentificador());
                }

                FichaIngresoCarpeta fichaIngresoCarpeta = this.fichaIngresoCarpetaRepository.
                        findFirstByFichaIngresoTokenIdentificadorAndRemovido(fichaIngreso.getTokenIdentificador(),false);
                if(fichaIngresoCarpeta!=null){
                    fichaIngresoDTO.setTokenIdentificadorCarpeta(fichaIngresoCarpeta.getTokenIdentificador());
                }
                fichaIngresoDTOList.add(fichaIngresoDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            fichaIngresoDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(fichaIngresoDTOList);
            paginacionResponse.setTotalItems(fichaIngresoPage.getTotalElements());

            // Obtener datos para los mensajes
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            String identificacionUsuario = obtenerIdentificacionUsuarioSistema(usuarioSistema);

            // Mensaje para el usuario - mantener formato original pero agregar nombre y DNI
            String mensajeUsuario = "Se han encontrado un total de: "
                            + fichaIngresoDTOList.size() + " de: " + fichaIngresoPage.getTotalElements() + " fichas de ingreso disponibles. Consulta realizada por: " + nombreUsuarioCompleto + " (" + identificacionUsuario + ")";

            // Mensaje para auditoría - mismo formato que Auth
            String mensajeAuditoria = "Se han encontrado un total de " + fichaIngresoPage.getTotalElements() + " fichas de ingreso del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<FichaIngresoDTO>  crearFichaIngreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaIngresoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            
            FichaIngresoDTO fichaIngresoDTO = new Gson().fromJson(bodyString, FichaIngresoDTO.class);
            
            fichaIngresoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            
            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            
            FichaIngreso fichaIngreso;
            List<FichaIngreso> fichaIngresoList = this.fichaIngresoRepository.findByFichaIdentificacionTokenIdentificadorAndRemovidoAndActivo(
                    fichaIngresoDTO.getTokenIdentificadorFichaIdentificacion(),
                    false, true);

            String nemonicoPertenencia = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_INGRESO;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaIngreso = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(fichaIngresoDTO.getTokenIdentificadorFichaIdentificacion(), nemonicoPertenencia, false);

            if (fichaIdentificacionCarpetaIngreso == null) {

                FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.
                        findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(fichaIngresoDTO.getTokenIdentificadorFichaIdentificacion(), null, false);
                Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();
                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorFichaIdentificacion(), false);


                String nombreCarpetaPrincipal = "Ficha Ingreso";

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                carpetaDTO.setDescripcion("Carpeta de ficha ingreso");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                fichaIdentificacionCarpetaIngreso = new FichaIdentificacionCarpeta();
                fichaIdentificacionCarpetaIngreso.setCarpeta(carpetaGuardadaRecientemente);
                fichaIdentificacionCarpetaIngreso.setFichaIdentificacion(ficha);
                Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoPertenencia, false);
                fichaIdentificacionCarpetaIngreso.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                fichaIdentificacionCarpetaIngreso.setFechaCreacion(new Date());
                fichaIdentificacionCarpetaIngreso.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaIdentificacionCarpetaIngreso.setUsuarioSistemaCrea(bodyJwtValido.getUsuarioSistema());
                this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaIngreso);
            }

            if(fichaIngresoDTO.getEsEdicion()){
                // HACER PARTE DE EDICION BUSCANDO EL REGISTRO DE LA FICHA DE INGRESO POR SU TOKEN IDENTIFICADOR ANDRES, TE DEJO UN EJEMPLO DE ROL PERO AJUSTALO A FICHA DE INGRESO
                fichaIngreso = fichaIngresoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificador(), Boolean.FALSE);
                if (fichaIngreso == null) {
                    df.setMensaje("La ficha de ingreso a editar no existe o ya fue eliminada anteriormente");
                    return df;
                }
                fichaIngreso.setFechaEdicion(new Date());
                fichaIngreso.setIpEdita(ip);
                fichaIngreso.setUsuarioSistemaEdita(usuarioLogin);
            }else{

                /*FichaIdentificacion fichaTemp = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorFichaIdentificacion(), false);
                if (fichaTemp != null) {
                    List<String> estadosValidos = List.of(
                            EtiquetaNemonico.NEMONICO_ESTADO_ADOLESCENTE_LIBRE,
                            EtiquetaNemonico.NEMONICO_ESTADO_ADOLESCENTE_FUGADO
                    );

                    if (!estadosValidos.contains(fichaTemp.getEstado().getNemonico())) {
                        String mensaje = "No es posible realizar el registro del adolescente debido a que el CJDR/SOA: " +
                                fichaTemp.getCentroIngreso().getNombre() +
                                " no ha generado su registro de salida.";
                        df.llenarConDatosDeException(new Exception(mensaje));
                        return df;
                    }
                }*/

                fichaIngreso = new FichaIngreso();

                Jerarquia centro = jerarquiaRepository.findJerarquiaByTokenIdentificador(fichaIngresoDTO.getCentro().getTokenIdentificador());
                fichaIngreso.setCentro(centro);
                fichaIngreso.setFechaCreacion(new Date());
                fichaIngreso.setIpCrea(ip);
                fichaIngreso.setUsuarioSistemaCrea(usuarioLogin);
                fichaIngreso.setEmpresa(empresa);
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);
                fichaIngreso.setFichaIdentificacion(fichaIdentificacion);
                fichaIngreso.setActivo(true);

                if(fichaIdentificacion.getEstado().getNemonico().equals("ESTADO_ADOLESCENTE_LIBRE")){
                    if(centro.getNombre().contains("SOA")){
                        fichaIdentificacion.setCentroIngreso(centro);
                        fichaIdentificacion.setEstado(catalogoRepository.findByNemonicoAndRemovido(
                                "ESTADO_ADOLESCENTE_INGRESADO", Boolean.FALSE
                        ));
                        fichaIdentificacion.setTieneProceso(false);

                        // Llenar histórico de ficha de identificación

                        AuditObject auditObject = AuditObject.builder()
                                .usuarioSistema(df2.getData().getUsuarioSistema())
                                .ip(httpServletRequest.getRemoteAddr())
                                .build();

                        this.historicoFichaIdentificacionService.crearActualizar(
                                fichaIdentificacion,
                                fichaIngresoDTO.getObservaciones(),
                                null,
                                auditObject,
                                false);
                    }
                }

                if(!ObjectUtils.isEmpty(fichaIngresoDTO.getEstadoAdolescente())){
                    fichaIdentificacion.setEstado(catalogoRepository.findByNemonicoAndRemovido(
                            fichaIngresoDTO.getEstadoAdolescente().getNemonico(), Boolean.FALSE
                    ));
                    fichaIdentificacion.setPostEgreso(true);
                    this.fichaIdentificacionRepository.save(fichaIdentificacion);
//                    if(!ObjectUtils.isEmpty(fichaIdentificacion.getCentroIngreso().getJerarquiaPadre()) && fichaIdentificacion.getCentroIngreso().getJerarquiaPadre().getNemonico().equals("CJDR")){
//                        fichaIdentificacion.setCentroIngreso(this.jerarquiaRepository.findJerarquiaByTNemonico("UAPISE"));
//                    }
                }
            }
            fichaIngreso.setObservaciones(fichaIngresoDTO.getObservaciones());
            fichaIngreso.setFechaIngreso(fichaIngresoDTO.getFechaIngreso());
            // CAMPOS MEDIO CERRADO
            fichaIngreso.setAtencionSalud(fichaIngresoDTO.getAtencionSalud());
            fichaIngreso.setMotivo(fichaIngresoDTO.getMotivo());
            Catalogo programaDerivado = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorProgramaDerivado(), Boolean.FALSE);
            fichaIngreso.setProgramaDerivado(programaDerivado);
            fichaIngreso.setLesiones(fichaIngresoDTO.getLesiones());
            fichaIngreso.setEspecificarZonaLesiones(fichaIngresoDTO.getEspecificarZonaLesiones());
            fichaIngreso.setMoretones(fichaIngresoDTO.getMoretones());
            fichaIngreso.setEspecificarZonaMoretones(fichaIngresoDTO.getEspecificarZonaMoretones());
            fichaIngreso.setCicatrices(fichaIngresoDTO.getCicatrices());
            fichaIngreso.setEspecificarZonaCicatrices(fichaIngresoDTO.getEspecificarZonaCicatrices());
            fichaIngreso.setTatuajes(fichaIngresoDTO.getTatuajes());
            fichaIngreso.setEspecificarZonaTatuajes(fichaIngresoDTO.getEspecificarZonaTatuajes());
            fichaIngreso.setPiercing(fichaIngresoDTO.getPiercing());
            fichaIngreso.setEspecificarZonaPiercing(fichaIngresoDTO.getEspecificarZonaPiercing());
            fichaIngreso.setOtros(fichaIngresoDTO.getOtros());
            fichaIngreso.setEspecificarZonaOtros(fichaIngresoDTO.getEspecificarZonaOtros());
            fichaIngreso.setVictimaAgresion(fichaIngresoDTO.getVictimaAgresion());
            fichaIngreso.setEspecificarAgresion(fichaIngresoDTO.getEspecificarAgresion());
            Catalogo seguroSalud = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorSeguroSalud(), Boolean.FALSE);
            fichaIngreso.setSeguroSalud(seguroSalud);
            Catalogo formaCabeza = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorFormaCabeza(), Boolean.FALSE);
            fichaIngreso.setFormaCabeza(formaCabeza);
            Catalogo formaNariz = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorFormaNariz(), Boolean.FALSE);
            fichaIngreso.setFormaNariz(formaNariz);
            Catalogo formaLabios = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorFormaLabios(), Boolean.FALSE);
            fichaIngreso.setFormaLabios(formaLabios);
            Catalogo formaCuerpo = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorFormaCuerpo(), Boolean.FALSE);
            fichaIngreso.setFormaCuerpo(formaCuerpo);
            Catalogo anomaliaOjos = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorAnomaliaOjos(), Boolean.FALSE);
            fichaIngreso.setAnomaliaOjos(anomaliaOjos);
            fichaIngreso.setEsEmbarazada(fichaIngresoDTO.getEsEmbarazada());
            fichaIngreso.setMesesEmbarazo(fichaIngresoDTO.getMesesEmbarazo());
            fichaIngreso.setIngresaConHijo(fichaIngresoDTO.getIngresaConHijo());
            
            // CAMPOS MEDIO ABIERTO
            Catalogo tutor = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIngresoDTO.getTokenIdentificadorTutor(), Boolean.FALSE);
            fichaIngreso.setTutor(tutor);
            fichaIngreso.setCaracteristicasParticulares(fichaIngresoDTO.getCaracteristicasParticulares());
            fichaIngreso.setResponsableInscripcion(fichaIngresoDTO.getResponsableInscripcion());
            
            fichaIngreso = this.fichaIngresoRepository.save(fichaIngreso);
            fichaIngresoDTO.setTokenIdentificador(fichaIngreso.getTokenIdentificador());
            
            if (Boolean.TRUE.equals(fichaIngreso.getIngresaConHijo())) {
                DatosHijoIngresadoDTO datosHijoIngresadoDTO = fichaIngresoDTO.getDatosHijoIngresado();
                if (datosHijoIngresadoDTO != null) {
                    DatosHijoIngresado datosHijo;
                    PersonaRelacionada personaRelacionada;

                    Boolean grabarRelacion = false;

                    if (fichaIngresoDTO.getEsEdicion()) {
                        // Intentar buscar registros existentes
                        personaRelacionada = datosHijoIngresadoDTO.getTokenIdentificadorPersonaRelacionada() != null ? 
                            personaRelacionadaRepository.findByTokenIdentificadorAndRemovido(
                                datosHijoIngresadoDTO.getTokenIdentificadorPersonaRelacionada(), Boolean.FALSE) : null;

                        datosHijo = datosHijoIngresadoDTO.getTokenIdentificador() != null ?
                            datosHijoIngresadoRepository.findByTokenIdentificadorAndRemovido(
                                datosHijoIngresadoDTO.getTokenIdentificador(), Boolean.FALSE) : null;

                        if (personaRelacionada == null) {
                            personaRelacionada = new PersonaRelacionada();
                            personaRelacionada.setFechaCreacion(new Date());
                            personaRelacionada.setIpCrea(ip);
                            personaRelacionada.setUsuarioSistemaCrea(usuarioLogin);
                            personaRelacionada.setEmpresa(empresa);
                            personaRelacionada.setRemovido(false);

                            grabarRelacion = true;

                        } else {
                            personaRelacionada.setFechaEdicion(new Date());
                            personaRelacionada.setIpEdita(ip);
                            personaRelacionada.setUsuarioSistemaEdita(usuarioLogin);
                        }

                        if (datosHijo == null) {
                            datosHijo = new DatosHijoIngresado();
                            datosHijo.setFechaCreacion(new Date());
                            datosHijo.setIpCrea(ip);
                            datosHijo.setUsuarioSistemaCrea(usuarioLogin);
                            datosHijo.setEmpresa(empresa);
                            datosHijo.setRemovido(false);
                        } else {
                            datosHijo.setFechaEdicion(new Date());
                            datosHijo.setIpEdita(ip);
                            datosHijo.setUsuarioSistemaEdita(usuarioLogin);
                        }
                    } else {
                        // Crear nuevos para una nueva ficha
                        personaRelacionada = new PersonaRelacionada();
                        datosHijo = new DatosHijoIngresado();

                        personaRelacionada.setFechaCreacion(new Date());
                        personaRelacionada.setIpCrea(ip);
                        personaRelacionada.setUsuarioSistemaCrea(usuarioLogin);
                        personaRelacionada.setEmpresa(empresa);
                        personaRelacionada.setRemovido(false);

                        datosHijo.setFechaCreacion(new Date());
                        datosHijo.setIpCrea(ip);
                        datosHijo.setUsuarioSistemaCrea(usuarioLogin);
                        datosHijo.setEmpresa(empresa);
                        datosHijo.setRemovido(false);
                    }

                    // Mapear datos comunes para ambos casos (creación o edición)
                    personaRelacionada.setPrimerApellido(datosHijoIngresadoDTO.getHijoApellidoPaterno());
                    personaRelacionada.setSegundoApellido(datosHijoIngresadoDTO.getHijoApellidoMaterno());
                    personaRelacionada.setPrimerNombre(datosHijoIngresadoDTO.getHijoPrimerNombre());
                    personaRelacionada.setSegundoNombre(datosHijoIngresadoDTO.getHijoSegundoNombre());
                    personaRelacionada.setNombres(datosHijoIngresadoDTO.getHijoPrimerNombre() + " " + 
                        datosHijoIngresadoDTO.getHijoSegundoNombre());
                    personaRelacionada.setNombresCompletos(
                            (ObjectUtils.isEmpty(datosHijoIngresadoDTO.getHijoPrimerNombre()) ? "" : datosHijoIngresadoDTO.getHijoPrimerNombre())+" " +
                            (ObjectUtils.isEmpty(datosHijoIngresadoDTO.getHijoSegundoNombre()) ? "" : datosHijoIngresadoDTO.getHijoSegundoNombre())+" " +
                            (ObjectUtils.isEmpty(datosHijoIngresadoDTO.getHijoApellidoPaterno()) ? "" : datosHijoIngresadoDTO.getHijoApellidoPaterno())+" " +
                            (ObjectUtils.isEmpty(datosHijoIngresadoDTO.getHijoApellidoMaterno()) ? "" : datosHijoIngresadoDTO.getHijoApellidoMaterno()));
                    personaRelacionada.setIdentificacion(datosHijoIngresadoDTO.getHijoDNI());
                    personaRelacionada.setFechaNacimiento(datosHijoIngresadoDTO.getHijoFechaNacimiento());
                    Catalogo tipoSexoBiologico = catalogoRepository.findByTokenIdentificadorAndRemovido(
                    datosHijoIngresadoDTO.getHijoTipoSexo(), Boolean.FALSE);
                    personaRelacionada.setTipoSexoBiologico(tipoSexoBiologico);
                    
                    // Setear campos adicionales
                    personaRelacionada.setOcupacion(datosHijoIngresadoDTO.getHijoOcupacion());
                    personaRelacionada.setParentesco(catalogoRepository.findByNemonicoAndRemovido(
                        "PARENTESCO_HIJO", Boolean.FALSE));
                    personaRelacionada.setTelefono(datosHijoIngresadoDTO.getHijoTelefono());
                    personaRelacionada.setEstadoCivil(catalogoRepository.findByNemonicoAndRemovido(
                        datosHijoIngresadoDTO.getHijoEstadoCivil(), Boolean.FALSE));
                    personaRelacionada.setModalidadEstudio(null);

                    personaRelacionada.setNivelEBR(null);

                    personaRelacionada.setNivelSuperior(null);

                    personaRelacionada.setNivelEBA(null);
                    
                    personaRelacionada.setRolesInfluencias(datosHijoIngresadoDTO.getHijoRoles());

                    personaRelacionada = personaRelacionadaRepository.save(personaRelacionada);

                    FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                            fichaIngresoDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);

                    if (fichaIdentificacion == null) {
                        df.setMensaje("La ficha de identificación no existe o fue eliminada");
                        return df;
                    }

                    if(grabarRelacion){
                        FichaIdentificacionPersonaRelacionada fip = new FichaIdentificacionPersonaRelacionada();
                        fip.setIdPersonasRelacionadas(personaRelacionada);
                        fip.setIdFichaIdentificacion(fichaIdentificacion);
                        fip.setRemovido(false);

                        try {
                            this.fichaIdentificacionPersonaRelacionadaRepository.save(fip);
                        } catch (Exception e) {
                            df.setMensaje("Error al guardar la relación entre persona y ficha: " + e.getMessage());
                            return df;
                        }
                    }

                    datosHijo.setFichaIngreso(fichaIngreso);
                    datosHijo.setPersonaRelacionada(personaRelacionada);
                    datosHijo.setHijoVictimaAgresion(datosHijoIngresadoDTO.getHijoVictimaAgresion());
                    datosHijo.setHijoEspecificarAgresion(datosHijoIngresadoDTO.getHijoEspecificarAgresion());
                    datosHijo.setHijoMoretones(datosHijoIngresadoDTO.getHijoMoretones());
                    datosHijo.setHijoEspecificarZonaMoretones(datosHijoIngresadoDTO.getHijoEspecificarZonaMoretones());
                    datosHijo.setHijoCicatrices(datosHijoIngresadoDTO.getHijoCicatrices());
                    datosHijo.setHijoEspecificarZonaCicatrices(datosHijoIngresadoDTO.getHijoEspecificarZonaCicatrices());
                    datosHijo.setHijoTatuajes(datosHijoIngresadoDTO.getHijoTatuajes());
                    datosHijo.setHijoEspecificarZonaTatuajes(datosHijoIngresadoDTO.getHijoEspecificarZonaTatuajes());
                    datosHijo.setHijoOtroEspecificar(datosHijoIngresadoDTO.getHijoOtroEspecificar());
                    datosHijo.setHijoObservaciones(datosHijoIngresadoDTO.getHijoObservaciones());

                    datosHijoIngresadoRepository.save(datosHijo);
                }
            }

            // CREACIÓN DE CARPETA FICHA INGRESO
//
//            String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_INGRESO;
//            FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.
//                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido
//                            (fichaIngreso.getFichaIdentificacion().getTokenIdentificador(), nemonico, false);
//            Carpeta carpetaPadreIngreso= fichaIdentificacionCarpeta.getCarpeta();
//
//            FichaIngresoCarpeta fichaIngresoCarpeta = this.fichaIngresoCarpetaRepository.
//                    findFirstByFichaIngresoTokenIdentificadorAndRemovido(fichaIngreso.getTokenIdentificador(),false);
//
//            if (fichaIngresoCarpeta == null) {
//
//                String nombreCarpeta = "ficha_ingreso_" +fichaIngreso.getIdFichaIngreso();
//
//                CarpetaDTO carpetaDTO = new CarpetaDTO();
//                carpetaDTO.setNombreCliente(nombreCarpeta);
//                carpetaDTO.setDescripcion("Carpeta de ficha ingreso relacionado a: " + fichaIngreso.getTokenIdentificador());
//                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
//                carpetaPadreDTO.setTokenIdentificador(carpetaPadreIngreso.getTokenIdentificador());
//                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);
//
//                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);
//
//                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);
//
//                fichaIngresoCarpeta = new FichaIngresoCarpeta();
//                fichaIngresoCarpeta.setCarpeta(carpetaGuardada);
//                fichaIngresoCarpeta.setFichaIngreso(fichaIngreso);
//                fichaIngresoCarpeta.setFechaCreacion(new Date());
//                fichaIngresoCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
//                fichaIngresoCarpeta.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
//                fichaIngresoCarpeta.setRemovido(false);
//                this.fichaIngresoCarpetaRepository.save(fichaIngresoCarpeta);
//            }



            if(!fichaIngresoDTO.getEsEdicion()){
                for(FichaIngreso ficha: fichaIngresoList){
                    ficha.setActivo(false);
                    if(ObjectUtils.isEmpty(ficha.getFechaInactividad())){
                        ficha.setFechaInactividad(new Date());
                    }
                    this.fichaIngresoRepository.save(ficha);
                }
            }

            // Obtener datos para los mensajes de auditoría
            FichaIdentificacion fichaIdentificacion = fichaIngreso.getFichaIdentificacion();
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String accion = fichaIngresoDTO.getEsEdicion() ? "editó" : "creó";

            // Mensaje para el usuario - mantener formato original
            String mensajeUsuario = "Se " + accion + " con éxito la ficha de ingreso";

            // Mensaje para auditoría - formato como situación educativa laboral
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
            String mensajeAuditoria = "Se " + accion + " con éxito la ficha de ingreso de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIngresoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarFichaIngreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            df.setTokenIdentificadorEmpresa(bodyJwtValido.getEmpresa().getTokenIdentificador());
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            FichaIngresoDTO fichaIngresoDTO = new Gson().fromJson(bodyString, FichaIngresoDTO.class);

            FichaIngreso fichaIngreso = this.fichaIngresoRepository.findByTokenIdentificadorAndRemovido(
                    fichaIngresoDTO.getTokenIdentificador(), false
            );

            if (fichaIngreso == null) {
                df.setMensaje("La ficha de ingreso no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            fichaIngreso.setRemovido(true);
            fichaIngreso.setIpElimina(ip);
            fichaIngreso.setUsuarioSistemaElimina(usuarioSistemaLogin);
            fichaIngreso.setFechaEliminacion(fecha);

            this.fichaIngresoRepository.save(fichaIngreso);

            // Obtener datos para los mensajes de auditoría
            FichaIdentificacion fichaIdentificacion = fichaIngreso.getFichaIdentificacion();
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String fechaFormateada = formatearFechaEspanol(fecha);

            // Mensaje para el usuario - mantener formato original
            String mensajeUsuario = "Se ha eliminado con éxito del sistema a la ficha de ingreso";

            // Mensaje para auditoría - formato como situación educativa laboral
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
            String mensajeAuditoria = "Se eliminó con éxito la ficha de ingreso de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIngresoDTO> obtenerUltimoIngresoValidoPorTokenFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaIngresoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(bodyJwtValido.getEmpresa().getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            FichaIngreso fichaIngresoEncontrada = this.fichaIngresoRepository.obtenerUltimaFichaIngresoValidaPorTokenFichaIdentificacion(paginacionRequest.getTokenIdentificador());

            if (fichaIngresoEncontrada == null) {
                df.setMensaje("No existen fichas de ingreso, primero cree una.");
                return df;
            }
            FichaIngresoDTO fichaIngresoDTO = new FichaIngresoDTO();

            fichaIngresoDTO.setTokenIdentificador(fichaIngresoEncontrada.getTokenIdentificador());
            fichaIngresoDTO.setTokenIdentificadorEmpresa(fichaIngresoEncontrada.getEmpresa().getTokenIdentificador());
            // Para saber el tipo de centro obtenemos el padre de la jerarquia del centro, ya que esto funciona como niveles
            JerarquiaDTO tipoCentro = new JerarquiaDTO();
            tipoCentro.setNombre(fichaIngresoEncontrada.getCentro().getJerarquiaPadre().getNombre());
            JerarquiaDTO centro = new JerarquiaDTO();
            centro.setId(fichaIngresoEncontrada.getCentro().getIdJerarquia());
            centro.setNombre(fichaIngresoEncontrada.getCentro().getNombre());
            centro.setUbigeo(fichaIngresoEncontrada.getCentro().getUbigeo());
            centro.setJerarquiaPadre(tipoCentro);
            centro.setTokenIdentificador(fichaIngresoEncontrada.getCentro().getTokenIdentificador());

            fichaIngresoDTO.setFechaIngreso(fichaIngresoEncontrada.getFechaIngreso());
            fichaIngresoDTO.setFechaCreacion(fichaIngresoEncontrada.getFechaCreacion());
            fichaIngresoDTO.setCentro(centro);
            fichaIngresoDTO.setAtencionSalud(fichaIngresoEncontrada.getAtencionSalud());
            fichaIngresoDTO.setAtencionSalud(fichaIngresoEncontrada.getAtencionSalud());
            fichaIngresoDTO.setMotivo(fichaIngresoEncontrada.getMotivo());
            fichaIngresoDTO.setObservaciones(fichaIngresoEncontrada.getObservaciones());
            fichaIngresoDTO.setResponsableInscripcion(fichaIngresoEncontrada.getResponsableInscripcion());
            fichaIngresoDTO.setCaracteristicasParticulares(fichaIngresoEncontrada.getCaracteristicasParticulares());
            if(fichaIngresoEncontrada.getProgramaDerivado()!=null) {
                fichaIngresoDTO.setTokenIdentificadorProgramaDerivado(fichaIngresoEncontrada.getProgramaDerivado().getTokenIdentificador());
            }
            if(fichaIngresoEncontrada.getTutor()!=null) {
                fichaIngresoDTO.setTokenIdentificadorTutor(fichaIngresoEncontrada.getTutor().getTokenIdentificador());
            }
            fichaIngresoDTO.setMoretones(fichaIngresoEncontrada.getMoretones());
            fichaIngresoDTO.setEspecificarZonaMoretones(fichaIngresoEncontrada.getEspecificarZonaMoretones());
            fichaIngresoDTO.setCicatrices(fichaIngresoEncontrada.getCicatrices());
            fichaIngresoDTO.setEspecificarZonaCicatrices(fichaIngresoEncontrada.getEspecificarZonaCicatrices());
            fichaIngresoDTO.setTatuajes(fichaIngresoEncontrada.getTatuajes());
            fichaIngresoDTO.setEspecificarZonaTatuajes(fichaIngresoEncontrada.getEspecificarZonaTatuajes());
            fichaIngresoDTO.setPiercing(fichaIngresoEncontrada.getPiercing());
            fichaIngresoDTO.setEspecificarZonaPiercing(fichaIngresoEncontrada.getEspecificarZonaPiercing());
            fichaIngresoDTO.setVictimaAgresion(fichaIngresoEncontrada.getVictimaAgresion());
            fichaIngresoDTO.setEspecificarAgresion(fichaIngresoEncontrada.getEspecificarAgresion());
            if(fichaIngresoEncontrada.getFormaCabeza()!=null) {
                fichaIngresoDTO.setTokenIdentificadorFormaCabeza(fichaIngresoEncontrada.getFormaCabeza().getTokenIdentificador());
            }
            if(fichaIngresoEncontrada.getFormaNariz()!=null) {
                fichaIngresoDTO.setTokenIdentificadorFormaNariz(fichaIngresoEncontrada.getFormaNariz().getTokenIdentificador());
            }
            if(fichaIngresoEncontrada.getFormaLabios()!=null) {
                fichaIngresoDTO.setTokenIdentificadorFormaLabios(fichaIngresoEncontrada.getFormaLabios().getTokenIdentificador());
            }
            if(fichaIngresoEncontrada.getFormaCuerpo()!=null) {
                fichaIngresoDTO.setTokenIdentificadorFormaCuerpo(fichaIngresoEncontrada.getFormaCuerpo().getTokenIdentificador());
            }
            if(fichaIngresoEncontrada.getAnomaliaOjos()!=null) {
                fichaIngresoDTO.setTokenIdentificadorAnomaliaOjos(fichaIngresoEncontrada.getAnomaliaOjos().getTokenIdentificador());
            }
            fichaIngresoDTO.setEsEmbarazada(fichaIngresoEncontrada.getEsEmbarazada());
            fichaIngresoDTO.setMesesEmbarazo(fichaIngresoEncontrada.getMesesEmbarazo());
            fichaIngresoDTO.setIngresaConHijo(fichaIngresoEncontrada.getIngresaConHijo());

            // Obtener datos para los mensajes de auditoría
            FichaIdentificacion fichaIdentificacion = fichaIngresoEncontrada.getFichaIdentificacion();
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            String identificacionUsuario = obtenerIdentificacionUsuarioSistema(usuarioSistema);

            // Mensaje para el usuario - mantener formato original pero agregar nombre y DNI
            String mensajeUsuario = "Se encontró la siguiente ficha. Consulta realizada por: " + nombreUsuarioCompleto + " (" + identificacionUsuario + ")";

            // Mensaje para auditoría - formato como situación educativa laboral
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
            String mensajeAuditoria = "Se obtuvo con éxito la ficha de ingreso de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIngresoDTO, mensajeAuditoria);
        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> actualizarFicha(HttpServletRequest httpServletRequest, FichaIdentificacionDTO fichaIdentificacionDTO) {
        return null;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> removerFicha(HttpServletRequest httpServletRequest, FichaIdentificacionDTO fichaIdentificacionDTO) {
        return null;
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

    public void crearCarpeta(FichaIngreso fichaIngreso, String tokenIdentificadorFichaIdentificacion, HttpServletRequest httpServletRequest,
                              UsuarioSistema usuarioSistema) {
        String nemonicoIngreso = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_INGRESO;
        FichaIdentificacionCarpeta fichaIdentificacionCarpetaIngreso = this.fichaIdentificacionCarpetaRepository.
                findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(tokenIdentificadorFichaIdentificacion, nemonicoIngreso, false);

        if (fichaIdentificacionCarpetaIngreso == null) {

            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(tokenIdentificadorFichaIdentificacion, null, false);
            Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();
            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(tokenIdentificadorFichaIdentificacion, false);


            String nombreCarpetaPrincipal = "Ficha Ingreso";

            CarpetaDTO carpetaDTO = new CarpetaDTO();
            carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
            carpetaDTO.setDescripcion("Carpeta de ficha ingreso");
            CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
            carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
            carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

            this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

            Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

            fichaIdentificacionCarpetaIngreso = new FichaIdentificacionCarpeta();
            fichaIdentificacionCarpetaIngreso.setCarpeta(carpetaGuardadaRecientemente);
            fichaIdentificacionCarpetaIngreso.setFichaIdentificacion(ficha);
            Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoIngreso, false);
            fichaIdentificacionCarpetaIngreso.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
            fichaIdentificacionCarpetaIngreso.setFechaCreacion(new Date());
            fichaIdentificacionCarpetaIngreso.setIpCrea(httpServletRequest.getRemoteAddr());
            fichaIdentificacionCarpetaIngreso.setUsuarioSistemaCrea(usuarioSistema);
            this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaIngreso);
        }

//        FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.
//                findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido
//                        (fichaIngreso.getFichaIdentificacion().getTokenIdentificador(), nemonicoIngreso, false);
//        Carpeta carpetaPadreIngreso= fichaIdentificacionCarpeta.getCarpeta();
//
//        FichaIngresoCarpeta fichaIngresoCarpeta = this.fichaIngresoCarpetaRepository.
//                findFirstByFichaIngresoTokenIdentificadorAndRemovido(fichaIngreso.getTokenIdentificador(),false);
//
//        if (fichaIngresoCarpeta == null) {
//
//            String nombreCarpeta = "ing_reg_" +fichaIngreso.getTokenIdentificador();
//
//            CarpetaDTO carpetaDTO = new CarpetaDTO();
//            carpetaDTO.setNombreCliente(nombreCarpeta);
//            carpetaDTO.setDescripcion("Carpeta de ficha ingreso relacionado a: " + fichaIngreso.getTokenIdentificador());
//            CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
//            carpetaPadreDTO.setTokenIdentificador(carpetaPadreIngreso.getTokenIdentificador());
//            carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);
//
//            this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);
//
//            Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);
//
//            fichaIngresoCarpeta = new FichaIngresoCarpeta();
//            fichaIngresoCarpeta.setCarpeta(carpetaGuardada);
//            fichaIngresoCarpeta.setFichaIngreso(fichaIngreso);
//            fichaIngresoCarpeta.setFechaCreacion(new Date());
//            fichaIngresoCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
//            fichaIngresoCarpeta.setUsuarioSistemaCrea(usuarioSistema);
//            fichaIngresoCarpeta.setRemovido(false);
//            this.fichaIngresoCarpetaRepository.save(fichaIngresoCarpeta);
//        }
    }

    /**
     * Formatea una fecha al español en el formato: "viernes, 30 de mayo del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "fecha no disponible";
        }

        try {
            // Configurar el locale para español
            Locale localeEspanol = new Locale("es", "ES");

            // Crear el formato personalizado
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", localeEspanol);

            return formatoCompleto.format(fecha);
        } catch (Exception e) {
            // En caso de error, devolver un formato simple
            SimpleDateFormat formatoSimple = new SimpleDateFormat("dd/MM/yyyy");
            return formatoSimple.format(fecha);
        }
    }

    /**
     * Método auxiliar para obtener nombres completos de una ficha de identificación
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
     * Método auxiliar para obtener nombres completos de un UsuarioSistema
     */
    private String obtenerNombreCompletoUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de un UsuarioSistema
     */
    private String obtenerIdentificacionUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        String identificacion = "N/A";

        if (usuario.getNumeroDeDocumento() != null && !usuario.getNumeroDeDocumento().trim().isEmpty()) {
            identificacion = usuario.getNumeroDeDocumento();
        }
        else if (usuario.getUserName() != null && !usuario.getUserName().trim().isEmpty()) {
            identificacion = usuario.getUserName();
        }
        else {
            String nombresCompletos = obtenerNombreCompletoUsuarioSistema(usuario);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }
}