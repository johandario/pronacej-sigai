package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.AuditoriaAccionesSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.AuditoriaServicioRestDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionAuditoriasAccionesRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.AuditoriaAccionesSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.AuditoriaServicioRestRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.MenuRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AuditoriaAccionesSistemaServiceImpl implements AuditoriaAccionesSistemaService {

    private AuditoriaAccionesSistemaRepository auditoriaAccionesSistemaRepository;

    private AuditoriaServicioRestRepository auditoriaServicioRestRepository;

    private JwtProviderService jwtProviderService;

    private final LogService logService = new LogService(this.getClass());

    private MenuRepository menuRepository;

    private CatalogoRepository catalogoRepository;

    private EmpresaRepository empresaRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    //Se espera que venga en los headers el token de la empresa y el nemonico del menu
    @Override
    public <T> RespuestaPorDefectoAuditoria<Boolean> guardarAccionRequestEncriptado(HttpServletRequest httpServletRequest,
                                                                                    String jsonRequest,
                                                                                    RespuestaPorDefectoAuditoria<T> respuesta,
                                                                                    Date fechaInicio, String nemonicoAccion) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtAppNoValidarSesion(httpServletRequest);

            if (!df2.isExito()) {
                this.logService.warn("Accion del usuario con un jwt inválido");
            }

            //Se asume que se va a ejecutar la funcion al terminar una accion
            Date fechaFin = new Date();


            AuditoriaServicioRest auditoriaServicioRest = new AuditoriaServicioRest();
            auditoriaServicioRest.setJsonRequest(jsonRequest);
            auditoriaServicioRest.setFechaRequest(fechaInicio);
            auditoriaServicioRest.setFechaResponse(fechaFin);
            auditoriaServicioRest.setJsonResponse(respuesta.toString());

            AuditoriaAccionesSistema auditoriaAccionesSistema = new AuditoriaAccionesSistema();


            auditoriaAccionesSistema.setFechaInicioAccion(fechaInicio);
            auditoriaAccionesSistema.setFechaFinAccion(fechaFin);


            String ip = httpServletRequest != null ? httpServletRequest.getRemoteAddr() : null;
            auditoriaAccionesSistema.setIpCrea(ip);
            auditoriaServicioRest.setIpCrea(ip);

            BodyJwtValido bodyJWT = df2.getData();
            if (bodyJWT != null) {
                Rol rol = bodyJWT.getRol();
                UsuarioSistema usuarioSistema = bodyJWT.getUsuarioSistema();

                auditoriaAccionesSistema.setUsuarioQueRealizaLaAccion(usuarioSistema);
                auditoriaAccionesSistema.setRol(rol);
                auditoriaAccionesSistema.setUsuarioSistemaCrea(usuarioSistema);

                //Datos del servicio
                auditoriaServicioRest.setUsuarioSistemaCrea(usuarioSistema);

            }

            String tokenIdentificadorEmpresa = respuesta.getTokenIdentificadorEmpresa();
            if (httpServletRequest != null) {
                if (tokenIdentificadorEmpresa == null || tokenIdentificadorEmpresa.isBlank()) {
                    tokenIdentificadorEmpresa = httpServletRequest.getHeader(EtiquetaNemonico.HEAD_TOKEN_EMPRESA);
                }
                Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificadorEmpresa, false);

                //Si la empresa es nula y si solo hay una entonces se toma esa
                if (empresa == null) {
                    List<Empresa> empresaList = this.empresaRepository.findByRemovido(false);
                    if (empresaList.size() == 1) {
                        empresa = empresaList.get(0);
                        tokenIdentificadorEmpresa = empresa.getTokenIdentificador();
                    }

                }
                Catalogo accion = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                        nemonicoAccion, tokenIdentificadorEmpresa, false
                );

                auditoriaAccionesSistema.setAccion(accion);
                auditoriaAccionesSistema.setEmpresa(empresa);

                try {
                    auditoriaServicioRest.setEmpresa(empresa);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                auditoriaServicioRest.setAccept(httpServletRequest.getHeader(HttpHeaders.ACCEPT));
                auditoriaServicioRest.setHeaderAuthorization(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
                auditoriaServicioRest.setAcceptLanguage(httpServletRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
                String contentLength = httpServletRequest.getHeader(HttpHeaders.CONTENT_LENGTH);
                if (contentLength != null) {
                    auditoriaServicioRest.setContentLength(Integer.valueOf(contentLength));
                }
                auditoriaServicioRest.setContentType(httpServletRequest.getHeader(HttpHeaders.CONTENT_TYPE));
                auditoriaServicioRest.setHost(httpServletRequest.getHeader(HttpHeaders.HOST));
                auditoriaServicioRest.setOrigin(httpServletRequest.getHeader(HttpHeaders.ORIGIN));

                auditoriaServicioRest.setPlatform(httpServletRequest.getHeader("sec-ch-ua-platform"));
                auditoriaServicioRest.setReferer(httpServletRequest.getHeader(HttpHeaders.REFERER));
                auditoriaServicioRest.setTipoDeMetodo(httpServletRequest.getMethod());
                auditoriaServicioRest.setUrl(httpServletRequest.getRequestURL().toString());
                auditoriaServicioRest.setUserAgent(httpServletRequest.getHeader(HttpHeaders.USER_AGENT));

                Pageable pageable = PageRequest.of(0, 2, Sort.by("idMenu").descending());
                String headerMenu = httpServletRequest.getHeader(EtiquetaNemonico.HEAD_NEMONICO_MENU);
                Page<Menu> menuPage = this.menuRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(headerMenu,
                        tokenIdentificadorEmpresa, false, pageable);


                List<Menu> menuList = menuPage.toList();
                if (menuList.size() > 1) {
                    this.logService.info("Se encontro mas de un menu con el nemonico: " + headerMenu + " de la empresa con token: " + tokenIdentificadorEmpresa);
                }
                Menu menu = menuList.isEmpty() ? null : menuList.getFirst();
                if (menu != null && menu.getRealizaAuditoria() != null && !menu.getRealizaAuditoria()) {
                    String msg = "El menu: " + menu.getTitulo() + " no realiza auditoria";
                    this.logService.warn(msg);
                    df.setMensaje(msg);
                    return df;
                }

                auditoriaAccionesSistema.setMenu(menu);

                Enumeration<String> names = httpServletRequest.getHeaderNames();

                if (names != null) {
                    JSONObject jsonObject = new JSONObject();
                    while (names.hasMoreElements()) {
                        String name = (String) names.nextElement();
                        Enumeration<String> values = httpServletRequest.getHeaders(name);
                        if (values != null) {
                            while (values.hasMoreElements()) {
                                String value = (String) values.nextElement();
                                jsonObject.put(name, value);
                            }
                        }
                    }
                    auditoriaServicioRest.setHeadersJson(jsonObject.toString());
                }


            }
            
            String mensajeAuditoria = respuesta.getMensajeAuditoria();
            auditoriaServicioRest = this.auditoriaServicioRestRepository.save(auditoriaServicioRest);
            auditoriaAccionesSistema.setAuditoriaServicioRest(auditoriaServicioRest);
            
            if (mensajeAuditoria == null || mensajeAuditoria.isBlank()){
                auditoriaAccionesSistema.setDescripcion(respuesta.getMensaje());
            }else {
                auditoriaAccionesSistema.setDescripcion(mensajeAuditoria);
            }
            
            auditoriaAccionesSistema = this.auditoriaAccionesSistemaRepository.save(auditoriaAccionesSistema);

            df.llenarRespuestaExitosa("Se creo con exito la auditoria", true, "");
            this.logService.info("Se guardo una auditoria no: " + auditoriaAccionesSistema.getIdAuditoriaAccionesSistema());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<AuditoriaAccionesSistemaDTO>> obtenerPorFiltros(HttpServletRequest httpServletRequest,
                                                                                                           BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<AuditoriaAccionesSistemaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();

            Empresa empresa = bodyJwtValido.getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            UsuarioSistema usuarioSistemaLogin = bodyJwtValido.getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyStringDesencriptado = df22.getData();
            PaginacionAuditoriasAccionesRequest paginacionAuditoriasAccionesRequest = new Gson().fromJson(bodyStringDesencriptado, PaginacionAuditoriasAccionesRequest.class);

            Date fechaInicio = paginacionAuditoriasAccionesRequest.getFechaInicio();
            Date fechaFin = paginacionAuditoriasAccionesRequest.getFechaFin();
            String tokenRol = paginacionAuditoriasAccionesRequest.getTokenIdentificadorRol();
            String tokenAccion = paginacionAuditoriasAccionesRequest.getTokenIdentificadorAccion();
            String tokenMenu = paginacionAuditoriasAccionesRequest.getTokenIdentificadorMenu();
            String userName = paginacionAuditoriasAccionesRequest.getUserName();

            List<AuditoriaAccionesSistema> auditoriaAccionesSistemaList;
            Long totalItems;

            //Emitiendo el reporte
            if (paginacionAuditoriasAccionesRequest.isEmitirReporte()) {

                ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository
                        .findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(empresa.getTokenIdentificador(),
                                EtiquetaNemonico.DIAS_MAX_ENTRE_FECHAS_FILTRO, false);

                if (parametroDelSistema == null) {
                    df.setMensaje("No se encontro el filtro máximo de días, consulta a tu administrador");
                    return df;
                }

                Long dias = Long.valueOf(parametroDelSistema.getValor());

                if (fechaInicio == null) {
                    df.setMensaje("La fecha de inicio es requerida para emitir el reporte");
                    return df;
                }

                if (fechaFin == null) {
                    df.setMensaje("La fecha de fin es requerida para emitir el reporte");
                    return df;
                }

                Long timeInicio = fechaInicio.getTime();
                Long timeFin = fechaFin.getTime();

                Long diferenciaDeFechas = timeFin - timeInicio;

                if (diferenciaDeFechas < 0) {
                    df.setMensaje("La fecha de inicio no debe de ser mayor a la fecha fin");
                    return df;
                }

                Long difereniaEndDias = (((diferenciaDeFechas / 1000) / 60) / 60) / 24;

                if (difereniaEndDias > dias) {
                    df.setMensaje("Para la obtención del reporte solo se permite que el rango entre fechas sea como máximo: "
                            + dias + " días, rango de fechas enviado: " + difereniaEndDias + " días.");
                    return df;
                }

                List<AuditoriaAccionesSistema> auditoriaAccionesSistemaList1 = this.auditoriaAccionesSistemaRepository
                        .encontrarPorFiltrosAppTodos(
                                fechaInicio, fechaFin, userName, tokenRol, tokenAccion, tokenMenu);
                totalItems = Long.valueOf(auditoriaAccionesSistemaList1.size());
                auditoriaAccionesSistemaList = auditoriaAccionesSistemaList1;

            } else {
                Integer size = paginacionAuditoriasAccionesRequest.getSize();
                Integer page = paginacionAuditoriasAccionesRequest.getPage();

                String id = "id_auditoria_acciones_sistema";
                //String id = "idAuditoriaAccionesSistema";

                Pageable pageable = PageRequest.of(page, size, Sort.by(id).descending());

                Page<AuditoriaAccionesSistema> auditoriaAccionesSistemaPage = this.auditoriaAccionesSistemaRepository
                        .encontrarPorFiltrosApp2(
                                fechaInicio, fechaFin, userName, tokenRol, tokenAccion, tokenMenu, pageable);

                totalItems = auditoriaAccionesSistemaPage.getTotalElements();
                auditoriaAccionesSistemaList = auditoriaAccionesSistemaPage.toList();
            }


            List<AuditoriaAccionesSistemaDTO> auditoriaAccionesSistemaDTOList =
                    this.construirAuditoriasDTO(auditoriaAccionesSistemaList);

            PaginacionResponse<AuditoriaAccionesSistemaDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setTotalItems(totalItems);
            paginacionResponse.setData(auditoriaAccionesSistemaDTOList);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " +
                    auditoriaAccionesSistemaDTOList.size() + " auditorias de acciones en el sistema de un total de: " +
                    totalItems + " consulta realizada por: " +
                    usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                    + "(" + usuarioSistemaLogin.getTokenIdentificador() + ")", paginacionResponse, "");

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private List<AuditoriaAccionesSistemaDTO> construirAuditoriasDTO(List<AuditoriaAccionesSistema> auditoriaAccionesSistemas) {
        List<AuditoriaAccionesSistemaDTO> auditoriaAccionesSistemaDTOList = new ArrayList<>();

        for (AuditoriaAccionesSistema auditoriaAccionesSistema : auditoriaAccionesSistemas) {
            AuditoriaServicioRest auditoriaServicioRest = auditoriaAccionesSistema.getAuditoriaServicioRest();


            //Datos de auditoria rest
            AuditoriaServicioRestDTO auditoriaServicioRestDTO = new AuditoriaServicioRestDTO();
            auditoriaServicioRestDTO.setAccept(auditoriaServicioRest.getAccept());
            auditoriaServicioRestDTO.setHost(auditoriaServicioRest.getHost());
            auditoriaServicioRestDTO.setFechaRequest(auditoriaServicioRest.getFechaRequest());
            auditoriaServicioRestDTO.setContentType(auditoriaServicioRest.getContentType());
            auditoriaServicioRestDTO.setContentLength(auditoriaServicioRest.getContentLength());
            auditoriaServicioRestDTO.setAcceptLanguage(auditoriaServicioRest.getAcceptLanguage());
            auditoriaServicioRestDTO.setOrigin(auditoriaServicioRest.getOrigin());
            auditoriaServicioRestDTO.setUrl(auditoriaServicioRest.getUrl());
            auditoriaServicioRestDTO.setReferer(auditoriaServicioRest.getReferer());
            auditoriaServicioRestDTO.setFechaResponse(auditoriaServicioRest.getFechaResponse());
            auditoriaServicioRestDTO.setPlatform(auditoriaServicioRest.getPlatform());
            auditoriaServicioRestDTO.setHeadersJson(auditoriaServicioRest.getHeadersJson());
            auditoriaServicioRestDTO.setJsonRequest(auditoriaServicioRest.getJsonRequest());
            auditoriaServicioRestDTO.setJsonResponse(auditoriaServicioRest.getJsonResponse());
            auditoriaServicioRestDTO.setFechaCreacion(auditoriaServicioRest.getFechaCreacion());
            auditoriaServicioRestDTO.setTokenIdentificador(auditoriaServicioRest.getTokenIdentificador());
            auditoriaServicioRestDTO.setTipoDeMetodo(auditoriaServicioRest.getTipoDeMetodo());
            auditoriaServicioRestDTO.setUserAgent(auditoriaServicioRest.getUserAgent());


            //Datos de auditoria de acciones
            AuditoriaAccionesSistemaDTO auditoriaAccionesSistemaDTO = new AuditoriaAccionesSistemaDTO();

            Catalogo accion = auditoriaAccionesSistema.getAccion();
            UsuarioSistema usuarioQueRealizaLaAccion = auditoriaAccionesSistema.getUsuarioQueRealizaLaAccion();
            Menu menu = auditoriaAccionesSistema.getMenu();
            Rol rol = auditoriaAccionesSistema.getRol();

            auditoriaAccionesSistemaDTO.setAuditoriaServicioRestDTO(auditoriaServicioRestDTO);
            auditoriaAccionesSistemaDTO.setNombreAccion(accion != null ? accion.getNombre() : null);
            auditoriaAccionesSistemaDTO.setFechaFinAccion(auditoriaAccionesSistema.getFechaFinAccion());
            auditoriaAccionesSistemaDTO.setFechaInicioAccion(auditoriaAccionesSistema.getFechaInicioAccion());
            auditoriaAccionesSistemaDTO.setTokenIdentificadorAccion(accion != null ? accion.getTokenIdentificador() : null);
            auditoriaAccionesSistemaDTO.setEmailUsuarioQueRealizaLaAccion(usuarioQueRealizaLaAccion != null ? usuarioQueRealizaLaAccion.getEmail() : null);
            auditoriaAccionesSistemaDTO.setNombreRol(rol != null ? rol.getNombre() : null);
            auditoriaAccionesSistemaDTO.setNombreUsuarioQueRealizaLaAccion(usuarioQueRealizaLaAccion != null ? usuarioQueRealizaLaAccion.getNombres() : null);
            auditoriaAccionesSistemaDTO.setTokenIdentificadorMenu(menu != null ? menu.getTokenIdentificador() : null);
            auditoriaAccionesSistemaDTO.setTokenIdentificador(auditoriaAccionesSistema.getTokenIdentificador());
            auditoriaAccionesSistemaDTO.setTokenIdentificadorRol(rol != null ? rol.getTokenIdentificador() : null);
            auditoriaAccionesSistemaDTO.setTokenIdentificadorUsuarioQueRealizaLaAccion(usuarioQueRealizaLaAccion != null ?
                    usuarioQueRealizaLaAccion.getTokenIdentificador() : null);
            auditoriaAccionesSistemaDTO.setUserNameUsuarioQueRealizaLaAccion(usuarioQueRealizaLaAccion != null ?
                    usuarioQueRealizaLaAccion.getUserName() : null);
            auditoriaAccionesSistemaDTO.setDescripcion(auditoriaAccionesSistema.getDescripcion());

            if (menu != null) {
                auditoriaAccionesSistemaDTO.setNombreMenu(menu.getTitulo());

                Menu menuPadre = menu.getMenuPadre();
                if (menuPadre != null) {
                    auditoriaAccionesSistemaDTO.setModulo(menuPadre.getTitulo());
                }
            }

            auditoriaAccionesSistemaDTOList.add(auditoriaAccionesSistemaDTO);
        }

        return auditoriaAccionesSistemaDTOList;
    }
}
