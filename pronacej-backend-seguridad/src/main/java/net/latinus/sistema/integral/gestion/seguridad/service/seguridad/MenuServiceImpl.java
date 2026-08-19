package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.NavigationFuseResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FuncionarioRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.MenuEmpresaRolRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.MenuRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
@AllArgsConstructor
public class MenuServiceImpl implements MenuService {

    private MenuRepository menuRepository;

    private MenuEmpresaRolRepository menuEmpresaRolRepository;

    private JwtProviderService jwtProviderService;

    private final LogService logService = new LogService(this.getClass());

    private FuncionarioRepository funcionarioRepository;

    @Override
    public RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<MenuDTO>>> crearOEditarMenus(HttpServletRequest httpServletRequest,
                                                                                                       List<MenuDTO> menuDTOList) {

        RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<MenuDTO>>> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            List<RespuestaPorDefectoAuditoria<MenuDTO>> list = new ArrayList<>();
            for (MenuDTO menuDTO : menuDTOList) {
                RespuestaPorDefectoAuditoria<MenuDTO> df3 =
                        this.crearOEditarMenu(httpServletRequest.getRemoteAddr(),
                                null, menuDTO);
                list.add(df3);
            }

            df.llenarRespuestaExitosa("Se han creado un total del: " +
                    list.size() + " menus", list);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<MenuDTO> crearOEditarMenu(String ip, UsuarioSistema usuarioSistema,
                                                                  MenuDTO menuDTO) {

        RespuestaPorDefectoAuditoria<MenuDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            Menu menu = this.verificarCreacionEdicion(ip, usuarioSistema, menuDTO);

            if (menu == null) {
                df.setMensaje("El menu a editar no esta disponible o fue eliminado anteriormente");
                return df;
            }
            List<MenuDTO> hijos = menuDTO.getChildren();
            menu.setEsPadre(hijos != null && !hijos.isEmpty());
            if (hijos != null) {
                for (MenuDTO hijo : hijos) {
                    Menu hijoMenu = this.crearOEditarEnDB(ip, usuarioSistema, hijo);
                    hijoMenu.setMenuPadre(menu);
                    hijo.setTokenIdentificador(hijoMenu.getTokenIdentificador());
                }
            }

            this.menuRepository.save(menu);
            menuDTO.setTokenIdentificador(menu.getTokenIdentificador());

            df.llenarRespuestaExitosa("Se creo con exito el menu: " +
                    menuDTO.getTitle(), menuDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<NavigationFuseResponse> crearMenuPorJwtApp(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<NavigationFuseResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // TODO CAMBIAR LOGICA DE MENUS POR ROLES
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            df.setTokenIdentificadorEmpresa(df2.getTokenIdentificadorEmpresa());

            BodyJwtValido bodyJwtValido = df2.getData();

            Rol rolJerarquia = bodyJwtValido.getRolJerarquia();
            Jerarquia jerarquiaSeleccionada = bodyJwtValido.getJerarquia();

            //Creando la lista de menu por el rol y por la empresa
            RespuestaPorDefectoAuditoria<List<MenuDTO>> df3 = this.crearMenuPorRolYEmpresa(
                    rolJerarquia, df2.getData().getEmpresa(), true
            );

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                return df;
            }



            RespuestaPorDefectoAuditoria<List<MenuDTO>> df4 = this.crearMenuPorRolYEmpresa(
                    rolJerarquia, df2.getData().getEmpresa(), false
            );

            if (!df4.isExito()) {
                df.setMensaje(df4.getMensaje());
                return df;
            }

            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovidoAndBloqueado(
                    usuarioSistema.getNumeroDeDocumento(),
                    false,
                    false
            );

            List<MenuDTO> listMenuCompact = df3.getData();
            List<MenuDTO> listMenuDefault = df4.getData();


            if (!ObjectUtils.isEmpty(jerarquiaSeleccionada)) {
                Jerarquia jerarquia = jerarquiaSeleccionada;
                JerarquiaDTO jerarquiaDTO = new JerarquiaDTO();
                Jerarquia jerarquiaPadre = jerarquia.getJerarquiaPadre();
                jerarquiaDTO.setNombre(jerarquia.getNombre());
                jerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
                jerarquiaDTO.setNemonico(jerarquiaPadre.getNemonico());
                jerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
//                listMenuDefault.removeIf(menu -> "Configuración".equals(menu.getTitle()));
//                listMenuDefault.removeIf(menu -> "Seguridad".equals(menu.getTitle()));
                if (!ObjectUtils.isEmpty(jerarquiaDTO.getNemonico()) && jerarquiaDTO.getNemonico().equals("SOA")) {
                    listMenuDefault.removeIf(menu -> "MENU_FLUJO_BANDEJA_ENTRADA".equals(menu.getNemonico()));
                    listMenuDefault.removeIf(menu -> "MENU_FLUJO_BANDEJA_SALIDA".equals(menu.getNemonico()));


                    Optional<MenuDTO> menuNuevoOpt = listMenuDefault.stream()
                            .filter(menu -> "MENU_NOTIFICACIONES".equals(menu.getNemonico()))
                            .findFirst();
                    if (menuNuevoOpt.isPresent()) {
                        MenuDTO menuNuevo = menuNuevoOpt.get();
                        List<String> nemonicosAEliminar = Arrays.asList(
                                "MENU_REGISTRO_INGRESO",
                                "MENU_FLUJO_INSTANCIA"
                        );
                        eliminarHijos(menuNuevo, nemonicosAEliminar);
                        reemplazarMenuEnLista(listMenuDefault, menuNuevo);
                        reemplazarMenuEnLista(listMenuCompact, menuNuevo);
                    }

                    Optional<MenuDTO> menuConfOpt = listMenuDefault.stream()
                            .filter(menu -> "Configuración".equals(menu.getTitle()))
                            .findFirst();
                    if (menuConfOpt.isPresent()) {
                        MenuDTO menuNuevo = menuConfOpt.get();
                        List<String> nemonicosAEliminar = Arrays.asList(
                                "MENU_FLUJO_PROCESOS"
                        );
                        eliminarHijos(menuNuevo, nemonicosAEliminar);
                        reemplazarMenuEnLista(listMenuDefault, menuNuevo);
                        reemplazarMenuEnLista(listMenuCompact, menuNuevo);
                    }
                }else if(!ObjectUtils.isEmpty(jerarquiaDTO.getNemonico()) && jerarquiaDTO.getNemonico().equals("UAPISE")){
                    Optional<MenuDTO> menuNuevoOpt = listMenuDefault.stream()
                            .filter(menu -> "MENU_NOTIFICACIONES".equals(menu.getNemonico()))
                            .findFirst();
                    if (menuNuevoOpt.isPresent()) {
                        MenuDTO menuNuevo = menuNuevoOpt.get();
                        List<String> nemonicosAEliminar = Arrays.asList(
                                "MENU_REGISTRO_INGRESO",
                                "MENU_FLUJO_INSTANCIA",
                                "MENU_REGISTRO_SALIDA"
                        );
                        eliminarHijos(menuNuevo, nemonicosAEliminar);
                        reemplazarMenuEnLista(listMenuDefault, menuNuevo);
                        reemplazarMenuEnLista(listMenuCompact, menuNuevo);
                    }
                }

                if(jerarquia.getEsOficinaCentral()){
                    listMenuDefault.removeIf(menu -> "Configuración".equals(menu.getTitle()));
                    listMenuDefault.removeIf(menu -> "Seguridad".equals(menu.getTitle()));
                }
            }

            NavigationFuseResponse navigationFuseResponse = new NavigationFuseResponse();
            navigationFuseResponse.setCompact(listMenuCompact);
            navigationFuseResponse.setPorDefecto(listMenuDefault);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + listMenuCompact.size() + " menus para el usuario", navigationFuseResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private Menu crearOEditarEnDB(String ip, UsuarioSistema usuarioSistema,
                                  MenuDTO menuDTO) {

        Menu menu = this.verificarCreacionEdicion(ip, usuarioSistema, menuDTO);
        if (menu == null) {
            return menu;
        }
        menu = this.menuRepository.save(menu);

        if (menuDTO.getChildren() == null || menuDTO.getChildren().isEmpty()) {
            return menu;
        }

        menu.setEsPadre(true);

        //Creando los hijos
        for (MenuDTO menuDTO1 : menuDTO.getChildren()) {
            Menu menuHijo = this.crearOEditarEnDB(ip, usuarioSistema, menuDTO1);
            menuHijo.setMenuPadre(menu);
            this.menuRepository.save(menuHijo);
        }

        return menu;
    }

    private Menu verificarCreacionEdicion(String ip, UsuarioSistema usuarioSistema,
                                          MenuDTO menuDTO) {
        Menu menu;

        if (menuDTO.getEsEdicion()) {
            menu = this.menuRepository.findByTokenIdentificadorAndRemovido(menuDTO.getTokenIdentificador(),
                    false);
            if (menu == null) {
                return menu;
            }

            menu.setFechaEdicion(new Date());
            menu.setIpEdita(ip);
            menu.setUsuarioSistemaEdita(usuarioSistema);
        } else {
            menu = new Menu();
            menu.setIpCrea(ip);
            menu.setUsuarioSistemaCrea(usuarioSistema);
        }

        menu.setIcono(menuDTO.getIcon());
        menu.setLink(menuDTO.getLink());
        menu.setMostrarEnElFront(menuDTO.getMostrarEnFront());
        menu.setSubtitulo(menuDTO.getSubtitle());
        menu.setTipo(menuDTO.getType());
        menu.setTitulo(menuDTO.getTitle());
        menu.setTooltip(menuDTO.getTooltip());
        menu.setNemonico(menuDTO.getNemonico());
        menu.setMenuPadre(this.menuRepository.findByTokenIdentificadorAndRemovido(menuDTO.getTokenIdentificadorPadre(), false));

        return menu;
    }

    @Override
    public List<MenuDTO> obtenerMenusDeMenusPadres(List<Menu> menuPadres, Boolean esCompact, Boolean mostrarEnElFront, Rol rol,
                                                   Empresa empresa) {
        List<MenuDTO> menuDTOList = new ArrayList<>();
        for (Menu menu : menuPadres) {
            MenuDTO menuDTO = new MenuDTO();
            menuDTO.setId(menu.getTokenIdentificador());
            String tipo = esCompact ? menu.getTipo().equals("group") ? "aside" : menu.getTipo() : menu.getTipo();

            menuDTO.setType(tipo);
            menuDTO.setIcon(menu.getIcono());
            menuDTO.setTitle(menu.getTitulo());
            menuDTO.setSubtitle(menu.getSubtitulo());
            menuDTO.setTooltip(menu.getTooltip());
            menuDTO.setLink(menu.getLink());
            menuDTO.setTokenIdentificador(menu.getTokenIdentificador());
            menuDTO.setTokenIdentificadorEmpresa(menu.getEmpresa().getTokenIdentificador());
            menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());
            menuDTO.setNemonico(menu.getNemonico());

            boolean tieneHijoSeguro = false;  // Variable para verificar si hay al menos un hijo seguro
            boolean mostrarMenuBasicSinHijos = false; // Variable para verificar si un menu tipo basic sin hijos se debe agregar al menu en base a los permisos

            if (menu.getEsPadre()) {
                menuDTO.setChildren(new ArrayList<>());
                List<Menu> hijos;
                if (mostrarEnElFront != null) {

                    hijos = this.menuRepository.findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovidoOrderByOrden(
                            mostrarEnElFront, menu.getIdMenu(), false
                    );
                    for (Menu hijo : hijos) {
                        MenuEmpresaRol hijoSeguro = this.menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndMenuIdMenuAndRemovido(empresa.getIdEmpresa(), rol.getIdRol(), hijo.getIdMenu(), Boolean.FALSE);
                        if (hijoSeguro != null) {
                            //Si existe hijoSeguro quiere decir que el rol que pide el menu tiene permiso a el por lo tanto agrega ese menu como hijo
                            menuDTO.getChildren().add(this.obtenerMenusHijos(hijo, menu, esCompact, mostrarEnElFront, rol, empresa));
                            tieneHijoSeguro = true;  // Se encontró al menos un hijo seguro
                        }

                    }
                } else {
                    // CASO EN EL QUE SE LLAMA DESDE OBTENER TODOS LOS MENUS ESTE NO VERIFICARIA PERMISO
                    hijos = this.menuRepository.findByMenuPadreIdMenuAndRemovidoOrderByOrden(menu.getIdMenu(), false);
                    for (Menu hijo : hijos) {
                        menuDTO.getChildren().add(this.obtenerMenusHijos(hijo, menu, esCompact, mostrarEnElFront, null, null));
                    }
                    tieneHijoSeguro = true;  // Se añaden todos los hijos, así que este menú debe incluirse
                }

            } else {
                // CASO MENUS BASIC QUE NO TIENEN HIJOS DEBEN VERIFICAR PERMISO
                if (menu.getTipo().equals("basic")) {
                    MenuEmpresaRol permisoMenuBasicSinHijos = this.menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndMenuIdMenuAndRemovido(empresa.getIdEmpresa(), rol.getIdRol(), menu.getIdMenu(), Boolean.FALSE);
                    if (permisoMenuBasicSinHijos != null) {
                        mostrarMenuBasicSinHijos = true;
                    }
                }
            }

            // Solo agregar el menú a la lista si tiene al menos un hijo seguro o no se está verificando mostrarEnElFront o es un basic en menu principal (como inicio)
            // Actualizacion el unico menu basico que se presenta por default es inicio, los demas deben verificar permisos
            if (tieneHijoSeguro || mostrarEnElFront == null || "Inicio".equals(menu.getTitulo()) || mostrarMenuBasicSinHijos) {
                menuDTOList.add(menuDTO);
            }
        }

        return menuDTOList;
    }

    public List<MenuDTO> obtenerTodosLosMenuHijosDeUnaArregloDePadres(List<Menu> menuPadres, Boolean esCompact, Boolean mostrarEnElFront) {
        List<MenuDTO> menuDTOList = new ArrayList<>();
        for (Menu menu : menuPadres) {
            MenuDTO menuDTO = new MenuDTO();
            menuDTO.setId(menu.getTokenIdentificador());
            String tipo = esCompact ? menu.getTipo().equals("group") ? "aside" : menu.getTipo() : menu.getTipo();

            menuDTO.setType(tipo);
            menuDTO.setIcon(menu.getIcono());
            menuDTO.setTitle(menu.getTitulo());
            menuDTO.setSubtitle(menu.getSubtitulo());
            menuDTO.setTooltip(menu.getTooltip());
            menuDTO.setLink(menu.getLink());
            menuDTO.setTokenIdentificador(menu.getTokenIdentificador());
            menuDTO.setTokenIdentificadorEmpresa(menu.getEmpresa().getTokenIdentificador());
            menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());
            menuDTO.setNemonico(menu.getNemonico());

            if (menu.getEsPadre()) {
                menuDTO.setChildren(new ArrayList<>());
                List<Menu> hijos;
                if (mostrarEnElFront != null) {
                    hijos = this.menuRepository.findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovidoOrderByOrden(
                            mostrarEnElFront, menu.getIdMenu(), false
                    );
                } else {
                    hijos = this.menuRepository.findByMenuPadreIdMenuAndRemovidoOrderByOrden(menu.getIdMenu(), false);
                }

                for (Menu hijo : hijos) {
                    menuDTO.getChildren().add(this.obtenerMenusHijos(hijo, menu, esCompact, mostrarEnElFront));
                }
            }

            menuDTOList.add(menuDTO);
        }

        return menuDTOList;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> crearMenuPorRolYEmpresa(Rol rol, Empresa empresa, Boolean esCompact) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // TODO CAMBIAR LOGICA DE MENUS POR ROLES
            List<Menu> menuPadresPrincipales = this.menuRepository.findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovidoOrderByOrden(
                    true, null, false
            );

            List<MenuDTO> list;
            if (rol != null && rol.getEsSuperRol() != null && rol.getEsSuperRol()) {
                list = this.obtenerTodosLosMenuHijosDeUnaArregloDePadres(menuPadresPrincipales, esCompact, true);
            } else {
                list = this.obtenerMenusDeMenusPadres(menuPadresPrincipales, esCompact, true, rol, empresa);
            }
            df.llenarRespuestaExitosa("Se han encontrado un total de:" +
                    list.size() + " menus principales", list);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerTodosLosMenu(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            Rol rol = bodyJwtValido.getRol();

            List<Menu> listMenuPrincipales = this.menuRepository.
                    findByEmpresaTokenIdentificadorAndMenuPadreAndRemovidoOrderByOrden(
                            empresa.getTokenIdentificador(), null, false
                    );


            List<MenuDTO> list = this.obtenerMenusDeMenusPadres(listMenuPrincipales,
                    false, null, rol, empresa);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " +
                    list.size() + " menus disponibles", list);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusPermisos(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            Rol rol = bodyJwtValido.getRol();

            List<Menu> listaMenus = this.menuRepository.
                    findByEmpresaTokenIdentificadorAndMostrarEnPermisosAndRemovidoOrderByOrdenAsc(
                            empresa.getTokenIdentificador(), true, false
                    );


            List<MenuDTO> list = this.construirArbolMenus(listaMenus);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " +
                    list.size() + " menus disponibles", list);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusPorEmpresa(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            List<Menu> listaMenusBasicPorEmpresa = this.menuRepository.
                    findByEmpresaTokenIdentificadorAndTipoAndRemovidoOrderByIdMenuDesc(
                            empresa.getTokenIdentificador(), "basic", false
                    );

            List<MenuDTO> menuDTOList = new ArrayList<>();

            for (Menu menu : listaMenusBasicPorEmpresa) {
                MenuDTO menuDTO = new MenuDTO();
                menuDTO.setId(menu.getTokenIdentificador());
                menuDTO.setType(menu.getTipo());
                menuDTO.setIcon(menu.getIcono());
                menuDTO.setTitle(menu.getTitulo());
                menuDTO.setSubtitle(menu.getSubtitulo());
                menuDTO.setTooltip(menu.getTooltip());
                menuDTO.setLink(menu.getLink());
                menuDTO.setTokenIdentificador(menu.getTokenIdentificador());
                menuDTO.setTokenIdentificadorEmpresa(menu.getEmpresa().getTokenIdentificador());
                menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());

                menuDTOList.add(menuDTO);
            }

            df.llenarRespuestaExitosa("Se han encontrado un total de " +
                    menuDTOList.size() + " menus basicos para la empresa " + empresa.getNombre(), menuDTOList);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private MenuDTO obtenerMenusHijos(Menu menu, Menu menuPadre, Boolean esCompact, Boolean mostrarEnElFront) {
        if (menu == null) {
            return null;
        }

        String id = (menuPadre != null ? menuPadre.getTokenIdentificador() + "." : "") + menu.getTokenIdentificador();
        MenuDTO menuDTO = new MenuDTO();
        menuDTO.setId(id);
        String tipo = esCompact ? menu.getTipo().equals("group") ? "aside" : menu.getTipo() : menu.getTipo();
        menuDTO.setType(tipo);
        menuDTO.setIcon(menu.getIcono());
        menuDTO.setTitle(menu.getTitulo());
        menuDTO.setSubtitle(menu.getSubtitulo());
        menuDTO.setLink(menu.getLink());
        menuDTO.setTokenIdentificador(menu.getTokenIdentificador());
        menuDTO.setTokenIdentificadorEmpresa(menu.getEmpresa().getTokenIdentificador());
        menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());
        menuDTO.setNemonico(menu.getNemonico());

        List<Menu> hijos;
        if (mostrarEnElFront != null) {
            hijos = this.menuRepository.findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovidoOrderByOrden(
                    mostrarEnElFront, menu.getIdMenu(), false
            );
        } else {
            hijos = this.menuRepository.findByMenuPadreIdMenuAndRemovidoOrderByOrden(menu.getIdMenu(), false);
        }

        if (hijos == null || hijos.isEmpty()) {
            return menuDTO;
        }

        menuDTO.setChildren(new ArrayList<>());
        for (Menu hijo : hijos) {
            menuDTO.getChildren().add(obtenerMenusHijos(hijo, menu, esCompact, mostrarEnElFront));
        }

        return menuDTO;
    }


    private MenuDTO obtenerMenusHijos(Menu menu, Menu menuPadre, Boolean esCompact, Boolean mostrarEnElFront, Rol rol, Empresa empresa) {
        if (menu == null) {
            return null;
        }

        String id = (menuPadre != null ? menuPadre.getTokenIdentificador() + "." : "") + menu.getTokenIdentificador();
        MenuDTO menuDTO = new MenuDTO();
        menuDTO.setId(id);
        String tipo = esCompact ? menu.getTipo().equals("group") ? "aside" : menu.getTipo() : menu.getTipo();
        menuDTO.setType(tipo);
        menuDTO.setIcon(menu.getIcono());
        menuDTO.setTitle(menu.getTitulo());
        menuDTO.setSubtitle(menu.getSubtitulo());
        menuDTO.setLink(menu.getLink());
        menuDTO.setTokenIdentificador(menu.getTokenIdentificador());
        menuDTO.setTokenIdentificadorEmpresa(menu.getEmpresa().getTokenIdentificador());
        menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());

        if(!ObjectUtils.isEmpty(menu.getNemonico())) {
            menuDTO.setNemonico(menu.getNemonico());
        }

        List<Menu> hijos;
        if (mostrarEnElFront != null) {
            hijos = this.menuRepository.findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovidoOrderByOrden(
                    mostrarEnElFront, menu.getIdMenu(), false
            );
        } else {
            hijos = this.menuRepository.findByMenuPadreIdMenuAndRemovidoOrderByOrden(menu.getIdMenu(), false);
        }

        if (hijos == null || hijos.isEmpty()) {
            return menuDTO;
        }

        menuDTO.setChildren(new ArrayList<>());

        if (mostrarEnElFront != null) {
            for (Menu hijo : hijos) {
                MenuEmpresaRol hijoSeguro = this.menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndMenuIdMenuAndRemovidoAndBloqueado(empresa.getIdEmpresa(), rol.getIdRol(), hijo.getIdMenu(), Boolean.FALSE, Boolean.FALSE);
                if (hijoSeguro != null) {
                    //Si existe hijoSeguro quiere decir que el rol que pide el menu tiene permiso a el por lo tanto agrega ese menu como hijo
                    menuDTO.getChildren().add(this.obtenerMenusHijos(hijo, menu, esCompact, mostrarEnElFront, rol, empresa));
                }
            }
        } else {
            for (Menu hijo : hijos) {
                menuDTO.getChildren().add(obtenerMenusHijos(hijo, menu, esCompact, mostrarEnElFront, null, null));
            }
        }


        return menuDTO;
    }

    @Override
    public RespuestaPorDefectoAuditoria<MenuDTO> editarTituloYRealizaAuditoria(HttpServletRequest httpServletRequest,
                                                                               MenuDTO menuDTO) {

        RespuestaPorDefectoAuditoria<MenuDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Menu menu = this.menuRepository.findByTokenIdentificadorAndRemovido(
                    menuDTO.getTokenIdentificador(), false
            );

            if (menu == null) {
                df.setMensaje("El menu a editar no exite o ya fue eliminado anteriormente");
                return df;
            }

            menu.setTitulo(menuDTO.getTitle());
            menu.setRealizaAuditoria(menuDTO.getRealizaAuditoria());
            this.menuRepository.save(menu);

            df.llenarRespuestaExitosa("Se ha editado el titulo y la acción de realizar auditoria del menu: " +
                    menu.getTitulo(), menuDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusAccesiblesPorRol(Long idEmpresa, Rol rol) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            List<MenuEmpresaRol> listaRelaciones = menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndRemovido(idEmpresa, rol.getIdRol(), Boolean.FALSE);
            List<MenuDTO> listaMenusAccesibles = new ArrayList<>();
            for (MenuEmpresaRol mer : listaRelaciones) {
                MenuDTO menuDTO = new MenuDTO();
                menuDTO.setId(mer.getMenu().getTokenIdentificador());
                menuDTO.setTokenIdentificador(mer.getMenu().getTokenIdentificador());
                menuDTO.setTitle(mer.getMenu().getTitulo());
                menuDTO.setSubtitle(mer.getMenu().getSubtitulo());
                menuDTO.setType(mer.getMenu().getTipo());
                menuDTO.setIcon(mer.getMenu().getIcono());
                menuDTO.setMostrarEnFront(mer.getMenu().getMostrarEnElFront());
                listaMenusAccesibles.add(menuDTO);
            }

            df.llenarRespuestaExitosa("Se obtuvieron con éxito " + listaMenusAccesibles.size() + " permisos para el rol " + rol.getNombre(), listaMenusAccesibles);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuDTO>> obtenerMenusPadres(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<MenuDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            List<Menu> listaMenusGroupPorEmpresa = this.menuRepository.
                    findByEmpresaTokenIdentificadorAndTipoAndRemovidoOrderByIdMenuDesc(
                            empresa.getTokenIdentificador(), "group", false
                    );

            List<Menu> listaMenusCollapsablePorEmpresa = this.menuRepository.
                    findByEmpresaTokenIdentificadorAndTipoAndRemovidoOrderByIdMenuDesc(
                            empresa.getTokenIdentificador(), "collapsable", false
                    );

            List<MenuDTO> menuDTOList = new ArrayList<>();

            for (Menu menu : listaMenusGroupPorEmpresa) {
                MenuDTO menuDTO = new MenuDTO();
                menuDTO.setId(menu.getTokenIdentificador());
                menuDTO.setType(menu.getTipo());
                menuDTO.setIcon(menu.getIcono());
                menuDTO.setTitle(menu.getTitulo());
                menuDTO.setSubtitle(menu.getSubtitulo());
                menuDTO.setTooltip(menu.getTooltip());
                menuDTO.setLink(menu.getLink());
                menuDTO.setTokenIdentificador(menu.getTokenIdentificador());
                menuDTO.setTokenIdentificadorEmpresa(menu.getEmpresa().getTokenIdentificador());
                menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());

                menuDTOList.add(menuDTO);
            }

            for (Menu menu : listaMenusCollapsablePorEmpresa) {
                MenuDTO menuDTO = new MenuDTO();
                menuDTO.setId(menu.getTokenIdentificador());
                menuDTO.setType(menu.getTipo());
                menuDTO.setIcon(menu.getIcono());
                menuDTO.setTitle(menu.getTitulo());
                menuDTO.setSubtitle(menu.getSubtitulo());
                menuDTO.setTooltip(menu.getTooltip());
                menuDTO.setLink(menu.getLink());
                menuDTO.setTokenIdentificador(menu.getTokenIdentificador());
                menuDTO.setTokenIdentificadorEmpresa(menu.getEmpresa().getTokenIdentificador());
                menuDTO.setRealizaAuditoria(menu.getRealizaAuditoria());

                menuDTOList.add(menuDTO);
            }

            df.llenarRespuestaExitosa("Se han encontrado un total de " +
                    menuDTOList.size() + " menus padres para la empresa " + empresa.getNombre(), menuDTOList);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private void eliminarHijos(MenuDTO menuNuevo, List<String> nemonicosAEliminar) {
        if (menuNuevo == null || menuNuevo.getChildren() == null) {
            return; // No hay hijos para procesar
        }

        List<MenuDTO> children = menuNuevo.getChildren();


        List<MenuDTO> childrenFiltrados = new ArrayList<>();

        for (MenuDTO child : children) {
            String nemonico = child.getNemonico();
            if (nemonico == null || !nemonicosAEliminar.contains(nemonico)) {
                childrenFiltrados.add(child);
            }
        }

        menuNuevo.setChildren(childrenFiltrados);
    }

    private void reemplazarMenuEnLista(List<MenuDTO> listMenuDefault, MenuDTO menu) {
        for (int i = 0; i < listMenuDefault.size(); i++) {
            MenuDTO menuOriginal = listMenuDefault.get(i);
            if (menuOriginal.getId().equals(menu.getId())) { // Asegúrate de que 'id' es único
                listMenuDefault.set(i, menu);
                break; // Salir del bucle una vez encontrado y reemplazado
            }
        }
    }

    private List<MenuDTO> construirArbolMenus(List<Menu> menus) {

        // 1. Mapa id → DTO
        Map<Long, MenuDTO> menuMap = new HashMap<>();

        for (Menu menu : menus) {
            menuMap.put(menu.getIdMenu(), toDtoBase(menu));
        }

        // 2. Relacionar hijos con padres
        List<MenuDTO> raiz = new ArrayList<>();

        for (Menu menu : menus) {
            MenuDTO dtoActual = menuMap.get(menu.getIdMenu());

            if (menu.getMenuPadre() != null) {
                Long idPadre = menu.getMenuPadre().getIdMenu();
                MenuDTO dtoPadre = menuMap.get(idPadre);
                if (dtoPadre != null) dtoPadre.getChildren().add(dtoActual);
                else raiz.add(dtoActual);
            } else {
                // Menú raíz
                raiz.add(dtoActual);
            }
        }

        return raiz;
    }

    private MenuDTO toDtoBase(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getIdMenu().toString());
        dto.setTitle(menu.getTitulo());
        dto.setSubtitle(menu.getSubtitulo());
        dto.setType(menu.getTipo());
        dto.setIcon(menu.getIcono());
        dto.setChildren(new ArrayList<>());
        dto.setTokenIdentificador(menu.getTokenIdentificador());
        dto.setMostrarAccionesPermisos(menu.getMostrarAccionesPermisos());
        return dto;
    }
}
