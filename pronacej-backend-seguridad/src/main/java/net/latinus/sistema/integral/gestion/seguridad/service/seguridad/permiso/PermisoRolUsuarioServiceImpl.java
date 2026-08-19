package net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioMenuAccionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioMenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioNombresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.permiso.*;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PermisoRolUsuarioServiceImpl implements PermisoRolUsuarioService {
    private final PermisoRolUsuarioRepository permisoRolUsuarioRepository;
    private final PermisoRolUsuarioMenuRepository permisoRolUsuarioMenuRepository;
    private final PermisoRolUsuarioMenuAccionRepository permisoRolUsuarioMenuAccionRepository;

    /* EXTERNOS A LA LÓGICA DEL SERVICIO */
    private final FuncionarioJerarquiaRolRepository funcionarioJerarquiaRolRepository;
    private final RolRepository rolRepository;
    private final MenuRepository menuRepository;
    private final CatalogoRepository catalogoRepository;
    private final JwtProviderService jwtProviderService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final FichaIdentificacionRepository fichaIdentificacionRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PermisoRolRepository permisoRolRepository;
    private final HistoricoFichaIdentificacionRepository historicoFichaIdentificacionRepository;

    private static final boolean VALIDA_PERMISO_BASE = true;
    private final boolean VALIDA_HISTORICO = true;
    private final boolean VALIDA_JORNADA = true;
    private final boolean EXCLUSION_JORNADA = true;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PermisoRolUsuarioNombresDTO>> obtenerPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PermisoRolUsuarioNombresDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize()
                    //Sort.by("fechaCreacion").descending()
            );

            String valorBusqueda = (paginacionRequest.getFilter() != null ? paginacionRequest.getFilter().toLowerCase() : "");

            Page<PermisoRolUsuarioNombresDTO> permisosPage
                    = this.permisoRolUsuarioRepository
                    .obtenerPermisosPorTokenEmpresaYRemovido(
                        df2.getData().getEmpresa().getTokenIdentificador(), false, valorBusqueda, pageable
                    );



            PaginacionResponse<PermisoRolUsuarioNombresDTO> paginacionResponse = new PaginacionResponse<>();

            paginacionResponse.setData(permisosPage.toList());
            paginacionResponse.setTotalItems(permisosPage.getTotalElements());

            long totalElementos = permisosPage.getTotalElements(); // Total de roles que coinciden con el filtro
            long elementosPaginaActual = permisosPage.getSize(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se encontraron " + totalElementos + " roles que coinciden con el filtro '" + paginacionRequest.getFilter() + "', mostrando " + elementosPaginaActual + " en esta página";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " roles filtrados del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception e) {
            df.llenarConDatosDeException(e);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> obtenerPermisosPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            PermisoRolUsuario permisoRolUsuario
                    = this.permisoRolUsuarioRepository
                    .findByTokenIdentificadorAndEmpresaTokenIdentificadorAndRemovido(
                            tokenIdentificador,
                            df2.getData().getEmpresa().getTokenIdentificador(),
                            false
                    )
                    .orElseThrow(() -> new RuntimeException("El rol no existe o ya fue eliminado anteriormente."));

            PermisoRolUsuarioDTO permisoRolUsuarioDTO = permisoRolUsuario.convertirADTO();

            List<RolDTO> roles = new ArrayList<>();
            for (PermisoRol permisoRol : permisoRolUsuario.getRoles()) {
                RolDTO rolDTO = permisoRol.getRol().convertirADTO();
                roles.add(rolDTO);
            }
            permisoRolUsuarioDTO.setRoles(roles);

            df.llenarRespuestaExitosa("Se obtuvo con éxito el permiso", permisoRolUsuarioDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> crearEditarPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String bodyStringDesencriptado = df22.getData();
            PermisoRolUsuarioDTO permisoRolUsuarioDTO = new Gson().fromJson(bodyStringDesencriptado, PermisoRolUsuarioDTO.class);

            PermisoRolUsuario permisoRolUsuario;
            if (permisoRolUsuarioDTO.getEsEdicion()) {
                permisoRolUsuario
                        = this.permisoRolUsuarioRepository
                        .findByTokenIdentificadorAndEmpresaTokenIdentificadorAndRemovido(
                                permisoRolUsuarioDTO.getTokenIdentificador(),
                                df2.getData().getEmpresa().getTokenIdentificador(),
                                false
                        )
                        .orElseThrow(() -> new RuntimeException("El rol no existe o ya fue eliminado anteriormente."));

                if (permisoRolUsuarioDTO.getTipoAsignacion() != null) {
                    Catalogo tipoAsignacion = this.catalogoRepository.findByNemonicoAndRemovido(permisoRolUsuarioDTO.getTipoAsignacion().getNemonico(), false);
                    permisoRolUsuario.setTipoAsignacion(tipoAsignacion);
                }
                if (permisoRolUsuarioDTO.getTipoPermiso() != null) {
                    Catalogo tipoPermiso = this.catalogoRepository.findByNemonicoAndRemovido(permisoRolUsuarioDTO.getTipoPermiso().getNemonico(), false);
                    permisoRolUsuario.setTipoPermiso(tipoPermiso);
                }

                if (permisoRolUsuarioDTO.getFuncionario() != null) {
                    Funcionario funcionario = this.funcionarioRepository.findByTokenIdentificadorAndRemovido(permisoRolUsuarioDTO.getFuncionario().getTokenIdentificador(), false);
                    permisoRolUsuario.setFuncionario(funcionario);
                } else {
                    permisoRolUsuario.setFuncionario(null);
                }

                permisoRolUsuario.setTokenVersionPermiso(UUID.randomUUID().toString());
                permisoRolUsuario = this.permisoRolUsuarioRepository.save(permisoRolUsuario);

                // Eliminar roles
                List<PermisoRol> permisoRolList = this.permisoRolRepository.findByPermisoRolUsuarioTokenIdentificadorAndRemovido(permisoRolUsuario.getTokenIdentificador(), false);
                permisoRolList.forEach(permisoRol -> permisoRol.setRemovido(true));
                this.permisoRolRepository.saveAll(permisoRolList);

                for (RolDTO dto : permisoRolUsuarioDTO.getRoles()) {
                    Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
                    PermisoRol permisoRol = new PermisoRol();
                    permisoRol.setRol(rol);
                    permisoRol.setPermisoRolUsuario(permisoRolUsuario);
                    this.permisoRolRepository.save(permisoRol);
                }

                // Eliminar menús
                List<PermisoRolUsuarioMenu> permisoMenusList = this.permisoRolUsuarioMenuRepository.findByPermisoRolUsuarioTokenIdentificadorAndRemovido(permisoRolUsuario.getTokenIdentificador(), false);
                permisoMenusList.forEach(permisoMenu -> permisoMenu.setRemovido(true));
                this.permisoRolUsuarioMenuRepository.saveAll(permisoMenusList);

                //  mapear permisoRolUsuarioMenu, asociar permisoRolUsuario y guardar, asignar a variable
                //  mapear permisoRolUsuarioMenuAccion, asociar permisoRolUsuarioMenu y guardar
                for (PermisoRolUsuarioMenuDTO menu : permisoRolUsuarioDTO.getMenus()) {
                    PermisoRolUsuarioMenu permisoRolUsuarioMenu = new PermisoRolUsuarioMenu();

                    permisoRolUsuarioMenu.setMenu(
                            this.menuRepository.findByTokenIdentificadorAndRemovido(
                                    menu.getTokenMenu(), false
                            )
                    );

                    permisoRolUsuarioMenu.setPermisoRolUsuario(permisoRolUsuario);
                    permisoRolUsuarioMenu = this.permisoRolUsuarioMenuRepository.save(permisoRolUsuarioMenu);

                    for (PermisoRolUsuarioMenuAccionDTO accion : menu.getAcciones()) {
                        PermisoRolUsuarioMenuAccion permisoRolUsuarioMenuAccion = new PermisoRolUsuarioMenuAccion();

                        permisoRolUsuarioMenuAccion.setAccion(
                                this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                                        accion.getTokenCatalogoAccion(), false
                                )
                        );

                        permisoRolUsuarioMenuAccion.setActivo(accion.getActivo());
                        permisoRolUsuarioMenuAccion.setPermisoRolUsuarioMenu(permisoRolUsuarioMenu);
                        this.permisoRolUsuarioMenuAccionRepository.save(permisoRolUsuarioMenuAccion);
                    }
                }

                df.llenarRespuestaExitosa("Se ha editado con éxito el permiso", permisoRolUsuarioDTO);
            } else {

                permisoRolUsuario = new PermisoRolUsuario();
                if (permisoRolUsuarioDTO.getTipoAsignacion() != null) {
                    Catalogo tipoAsignacion = this.catalogoRepository.findByNemonicoAndRemovido(permisoRolUsuarioDTO.getTipoAsignacion().getNemonico(), false);
                    permisoRolUsuario.setTipoAsignacion(tipoAsignacion);
                }
                if (permisoRolUsuarioDTO.getTipoPermiso() != null) {
                    Catalogo tipoPermiso = this.catalogoRepository.findByNemonicoAndRemovido(permisoRolUsuarioDTO.getTipoPermiso().getNemonico(), false);
                    permisoRolUsuario.setTipoPermiso(tipoPermiso);
                }
                permisoRolUsuario.setEmpresa(df2.getData().getEmpresa());

                if (permisoRolUsuarioDTO.getFuncionario() != null) {
                    Funcionario funcionario = this.funcionarioRepository.findByTokenIdentificadorAndRemovido(permisoRolUsuarioDTO.getFuncionario().getTokenIdentificador(), false);
                    permisoRolUsuario.setFuncionario(funcionario);
                }

                permisoRolUsuario = this.permisoRolUsuarioRepository.save(permisoRolUsuario);

                for (RolDTO dto : permisoRolUsuarioDTO.getRoles()) {
                    Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
                    PermisoRol permisoRol = new PermisoRol();
                    permisoRol.setRol(rol);
                    permisoRol.setPermisoRolUsuario(permisoRolUsuario);
                    this.permisoRolRepository.save(permisoRol);
                }

                for (PermisoRolUsuarioMenuDTO menu : permisoRolUsuarioDTO.getMenus()) {
                    PermisoRolUsuarioMenu permisoRolUsuarioMenu = new PermisoRolUsuarioMenu();

                    permisoRolUsuarioMenu.setMenu(
                            this.menuRepository.findByTokenIdentificadorAndRemovido(
                                    menu.getTokenMenu(), false
                            )
                    );

                    permisoRolUsuarioMenu.setPermisoRolUsuario(permisoRolUsuario);
                    permisoRolUsuarioMenu = this.permisoRolUsuarioMenuRepository.save(permisoRolUsuarioMenu);

                    for (PermisoRolUsuarioMenuAccionDTO accion : menu.getAcciones()) {
                        PermisoRolUsuarioMenuAccion permisoRolUsuarioMenuAccion = new PermisoRolUsuarioMenuAccion();

                        permisoRolUsuarioMenuAccion.setAccion(
                                this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                                        accion.getTokenCatalogoAccion(), false
                                )
                        );

                        permisoRolUsuarioMenuAccion.setActivo(accion.getActivo());
                        permisoRolUsuarioMenuAccion.setPermisoRolUsuarioMenu(permisoRolUsuarioMenu);
                        this.permisoRolUsuarioMenuAccionRepository.save(permisoRolUsuarioMenuAccion);
                    }
                }

                df.llenarRespuestaExitosa("Se ha creado con éxito el permiso", permisoRolUsuarioDTO);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> eliminarPermisos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String bodyStringDesencriptado = df22.getData();
            PermisoRolUsuarioNombresDTO permisoRolUsuarioDTO = new Gson().fromJson(bodyStringDesencriptado, PermisoRolUsuarioNombresDTO.class);

            PermisoRolUsuario permisoRolUsuario
                        = this.permisoRolUsuarioRepository
                        .findByTokenIdentificadorAndEmpresaTokenIdentificadorAndRemovido(
                                permisoRolUsuarioDTO.getTokenIdentificador(),
                                df2.getData().getEmpresa().getTokenIdentificador(),
                                false
                        )
                        .orElseThrow(() -> new RuntimeException("El rol no existe o ya fue eliminado anteriormente."));

            permisoRolUsuario.setRemovido(true);
            permisoRolUsuario.setIpElimina(httpServletRequest.getRemoteAddr());
            permisoRolUsuario.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());
            permisoRolUsuario.setFechaEliminacion(new Date());
            this.permisoRolUsuarioRepository.save(permisoRolUsuario);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito el permiso", permisoRolUsuario.convertirADTO());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> obtenerPermisosUsuarioPorTokenFicha(HttpServletRequest httpServletRequest, String tokenFichaIdentificacion) {
        RespuestaPorDefectoAuditoria<PermisoRolUsuarioDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Funcionario funcionario = bodyJwtValido.getUsuarioSistema().getFuncionario();
            Rol rol = bodyJwtValido.getRol();

            // Obtener permisos por rol y rol/funcionario
            List<PermisoRolUsuario> permisoRolUsuarioList
                    = this.permisoRolUsuarioRepository.obtenerPorFuncionarioYRolYNemonicoTipoPermisoRemovido(
                            funcionario.getTokenIdentificador(),
                            rol.getTokenIdentificador(),
                            EtiquetaNemonico.NEMONICO_PERMISO_TIPO_PERMISO_ESTANDAR,
                            false
            );

            // Convertir lista a dto
            List<PermisoRolUsuarioDTO> permisosBase = permisoRolUsuarioList.stream().map(PermisoRolUsuario::convertirADTO).collect(Collectors.toList());

            // Hacer merge de permisos base
            PermisoRolUsuarioDTO permisoRolUsuarioDTO = this.mergePermisos(permisosBase);

            if (tokenFichaIdentificacion != null && !tokenFichaIdentificacion.isEmpty()) {
                this.establecerPermisosAgregarFicha(permisoRolUsuarioDTO, tokenFichaIdentificacion, bodyJwtValido.getJerarquia().getTokenIdentificador());
            }

            df.llenarRespuestaExitosa("Se han encontrado los permisos del usuario", permisoRolUsuarioDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public <T extends CamposDTO> void validarPermisoLista(
            List<T> lista,
            String tokenFichaIdentificacion,
            BodyJwtValido bodyJwtValido
    ) {
        if (lista == null || lista.isEmpty()) {
            return;
        }

        HistoricoFichaIdentificacion historico = obtenerHistoricoActivo(
                tokenFichaIdentificacion,
                bodyJwtValido
        );
        if (historico == null) {
            return;
        }

        Map<String, Boolean> accionesBase = obtenerAccionesPermiso(
                bodyJwtValido,
                EtiquetaNemonico.NEMONICO_PERMISO_TIPO_PERMISO_ESTANDAR
        );

        Map<String, Boolean> accionesExclusion = obtenerAccionesPermiso(
                bodyJwtValido,
                EtiquetaNemonico.NEMONICO_PERMISO_TIPO_EXCLUSION_JORNADA
        );

        LocalDateTime fechaInicio = FuncionesAyuda.toLocalDateTime(historico.getFechaInicio()).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime fechaActual = LocalDateTime.now();

        lista.forEach(objeto ->
                aplicarControles(objeto, fechaInicio, fechaActual, accionesBase, accionesExclusion)
        );
    }

    @Override
    public <T extends CamposDTO> boolean validarPermisoObjetoYAccion(T objeto, String tokenFichaIdentificacion, BodyJwtValido bodyJwtValido, String nemonicoAccion) {
        HistoricoFichaIdentificacion historico = obtenerHistoricoActivo(
                tokenFichaIdentificacion,
                bodyJwtValido
        );

        if (historico == null) {
            return false;
        }

        Map<String, Boolean> accionesBase = obtenerAccionesPermiso(
                bodyJwtValido,
                EtiquetaNemonico.NEMONICO_PERMISO_TIPO_PERMISO_ESTANDAR
        );

        Map<String, Boolean> accionesExclusion = obtenerAccionesPermiso(
                bodyJwtValido,
                EtiquetaNemonico.NEMONICO_PERMISO_TIPO_EXCLUSION_JORNADA
        );

        LocalDateTime fechaInicio = FuncionesAyuda.toLocalDateTime(historico.getFechaInicio());
        LocalDateTime fechaActual = LocalDateTime.now();

        aplicarControles(
                objeto,
                fechaInicio,
                fechaActual,
                accionesBase,
                accionesExclusion
        );

        return estaAccionPermitida(objeto, nemonicoAccion);
    }

    private void establecerPermisosAgregarFicha(PermisoRolUsuarioDTO permisoRolUsuarioDTO, String tokenFichaIdentificacion, String tokenCentroActual) {
        if (permisoRolUsuarioDTO == null || permisoRolUsuarioDTO.getMenus() == null || permisoRolUsuarioDTO.getMenus().isEmpty()) return;

        FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(tokenFichaIdentificacion, false);

        if (ficha == null) return;

        String tokenCentroFicha = ficha.getCentroIngreso().getTokenIdentificador();

        if (!tokenCentroActual.equals(tokenCentroFicha)) return;

        // BLOQUE PARA AÑADIR ACCIÓN DE AGREGAR POR MÓDULO
        permisoRolUsuarioDTO.getMenus().forEach(menu -> {
            PermisoRolUsuarioMenuAccionDTO dto = new PermisoRolUsuarioMenuAccionDTO();
            Catalogo catalogoAccion = new Catalogo();
            catalogoAccion.setNemonico(EtiquetaNemonico.ACCIONES_MENU_PERMISO_AGREGAR);
            dto.setNemonicoCatalogoAccion(catalogoAccion.getNemonico());
            dto.setActivo(true);
            menu.setAcciones(new ArrayList<>(menu.getAcciones()));
            menu.getAcciones().add(dto);
        });

    }

    private HistoricoFichaIdentificacion obtenerHistoricoActivo(
            String tokenFichaIdentificacion,
            BodyJwtValido bodyJwtValido
    ) {
        String tokenCentroActual = bodyJwtValido.getJerarquia().getTokenIdentificador();

        return historicoFichaIdentificacionRepository
                .findByFichaIdentificacionTokenIdentificadorAndCentroTokenIdentificadorAndActivoAndRemovido(
                        tokenFichaIdentificacion,
                        tokenCentroActual,
                        true,
                        false
                )
                .orElse(null);
    }

    private Map<String, Boolean> obtenerAccionesPermiso(
            BodyJwtValido bodyJwtValido,
            String nemonicoTipoPermiso
    ) {
        Funcionario funcionario = bodyJwtValido.getUsuarioSistema().getFuncionario();
        Rol rol = bodyJwtValido.getRol();
        String nemonicoMenu = bodyJwtValido.getNemonicoMenu();

        List<PermisoRolUsuarioMenuDTO> permisos = permisoRolUsuarioMenuRepository
                .obtenerPorFuncionarioYRolYNemonicoTipoPermisoYNemonicoMenuYRemovido(
                        funcionario.getTokenIdentificador(),
                        rol.getTokenIdentificador(),
                        nemonicoTipoPermiso,
                        nemonicoMenu,
                        false
                )
                .stream()
                .map(PermisoRolUsuarioMenu::convertirADTO)
                .toList();

        List<PermisoRolUsuarioMenuDTO> permisosMerge = mergeMenus(permisos);

        if (permisosMerge.isEmpty()) {
            return Map.of();
        }

        return permisosMerge.getFirst()
                .getAcciones()
                .stream()
                .collect(Collectors.toMap(
                        PermisoRolUsuarioMenuAccionDTO::getNemonicoCatalogoAccion,
                        PermisoRolUsuarioMenuAccionDTO::getActivo
                ));
    }

    private <T extends CamposDTO> void aplicarControles(
            T objeto,
            LocalDateTime fechaInicio,
            LocalDateTime fechaActual,
            Map<String, Boolean> accionesBase,
            Map<String, Boolean> accionesExclusion
    ) {
        LocalDateTime fechaCreacion = FuncionesAyuda.toLocalDateTime(objeto.getFechaCreacion());

        // Caso 1: anterior a inicio → no aplica
        if (!fechaCreacion.isAfter(fechaInicio)) {
            return;
        }

        LocalDateTime inicioDia = fechaCreacion.toLocalDate().atStartOfDay();
        LocalDateTime finDia = fechaCreacion.toLocalDate().atTime(23, 59, 59);

        boolean estaEnDiaCreacion =
                !fechaActual.isBefore(inicioDia) &&
                        !fechaActual.isAfter(finDia);

        objeto.setControlesMap(
                estaEnDiaCreacion ? accionesBase : accionesExclusion
        );
    }

    private boolean estaAccionPermitida(
            CamposDTO objeto,
            String nemonicoAccion
    ) {
        if (objeto.getControlesMap() == null || objeto.getControlesMap().isEmpty()) {
            return false;
        }

        return Boolean.TRUE.equals(
                objeto.getControlesMap().get(nemonicoAccion)
        );
    }

    private PermisoRolUsuarioMenuDTO obtenerPermisosMerge(
            String tokenFuncionario,
            String tokenRol,
            String nemonicoTipoPermiso,
            String nemonicoMenu
    ) {
        return permisoRolUsuarioMenuRepository
                .obtenerPorFuncionarioYRolYNemonicoTipoPermisoYNemonicoMenuYRemovido(
                        tokenFuncionario,
                        tokenRol,
                        nemonicoTipoPermiso,
                        nemonicoMenu,
                        false
                )
                .stream()
                .map(PermisoRolUsuarioMenu::convertirADTO)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        this::mergeMenus
                )).getFirst();
    }

    private PermisoRolUsuarioDTO mergePermisos(
            List<PermisoRolUsuarioDTO> permisos) {

        if (permisos == null || permisos.isEmpty()) {
            return null;
        }

        // Tomamos el primero como base
        PermisoRolUsuarioDTO base = permisos.get(0);

        // Flatten de todos los menús
        List<PermisoRolUsuarioMenuDTO> todosLosMenus =
                permisos.stream()
                        .filter(p -> p.getMenus() != null)
                        .flatMap(p -> p.getMenus().stream())
                        .toList();

        // Merge de menús (usa la función ya definida)
        List<PermisoRolUsuarioMenuDTO> menusMergeados =
                mergeMenus(todosLosMenus);

        // Setear el resultado
        base.setMenus(menusMergeados);

        return base;
    }

    private List<PermisoRolUsuarioMenuDTO> mergeMenus(
            List<PermisoRolUsuarioMenuDTO> menus) {

        Map<String, PermisoRolUsuarioMenuDTO> menuMap = new HashMap<>();

        for (PermisoRolUsuarioMenuDTO menu : menus) {
            menuMap.merge(
                    menu.getTokenMenu(),
                    menu,
                    (existente, nuevo) -> {
                        existente.setAcciones(
                                mergeAcciones(existente.getAcciones(), nuevo.getAcciones())
                        );
                        return existente;
                    }
            );
        }

        return new ArrayList<>(menuMap.values());
    }

    private List<PermisoRolUsuarioMenuAccionDTO> mergeAcciones(
            List<PermisoRolUsuarioMenuAccionDTO> acciones1,
            List<PermisoRolUsuarioMenuAccionDTO> acciones2) {

        Map<String, PermisoRolUsuarioMenuAccionDTO> accionMap = new HashMap<>();

        Stream.concat(
                acciones1.stream(),
                acciones2.stream()
        ).forEach(accion ->
                accionMap.merge(
                        accion.getTokenCatalogoAccion(),
                        accion,
                        (existente, nuevo) -> {
                            existente.setActivo(
                                    Boolean.TRUE.equals(existente.getActivo())
                                            || Boolean.TRUE.equals(nuevo.getActivo())
                            );
                            return existente;
                        }
                )
        );

        return new ArrayList<>(accionMap.values());
    }

    private Date finDelDia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

}
