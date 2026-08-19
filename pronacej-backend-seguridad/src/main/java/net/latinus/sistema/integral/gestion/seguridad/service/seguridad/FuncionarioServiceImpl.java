package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.FuncionarioJerarquiaRol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;

@Service
@AllArgsConstructor
public class FuncionarioServiceImpl implements FuncionarioService {
    private FuncionarioRepository funcionarioRepository;

    private UsuarioSistemaRepository usuarioSistemaRepository;

    private CatalogoRepository catalogoRepository;

    private JerarquiaRepository jerarquiaRepository;

    private CargosJerarquiaRepository cargosJerarquiaRepository;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaService parametroDelSistemaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private FuncionarioJerarquiaRolRepository asignacionRepo;

    private RolRepository rolRepository;

    @Override
    public RespuestaPorDefectoAuditoria<FuncionarioDTO> crearFuncionario(HttpServletRequest httpServletRequest, FuncionarioDTO funcionarioDTO) {
        RespuestaPorDefectoAuditoria<FuncionarioDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            Funcionario funcionariosTempDocumento = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovido(funcionarioDTO.getNumeroDeDocumento(), Boolean.FALSE);

            if (funcionariosTempDocumento != null && !funcionarioDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el mismo número de documento.");
                return df;
            }

            List<Funcionario> funcionariosTempEmail = this.funcionarioRepository.findByEmailAndRemovido(funcionarioDTO.getEmail(), Boolean.FALSE);

            if (!funcionariosTempEmail.isEmpty() && !funcionarioDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el mismo email.");
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();

            Jerarquia jerarquia = bodyJwtValido.getJerarquia();

            Rol rolJeraquia = bodyJwtValido.getRolJerarquia();

            //df = this.crearFuncionario(funcionarioDTO, null, httpServletRequest.getRemoteAddr());

            Funcionario funcionario;
            if (funcionariosTempDocumento == null && !funcionarioDTO.getEsEdicion()) {
                funcionario = new Funcionario();
                funcionario.setNombres(funcionarioDTO.getNombres());
                funcionario.setApellidos(funcionarioDTO.getApellidos());
                funcionario.setEmail(funcionarioDTO.getEmail());
                funcionario.setTelefono(funcionarioDTO.getTelefono());
                funcionario.setNumeroDeDocumento(funcionarioDTO.getNumeroDeDocumento());
                funcionario.setNumeroDeCelular(funcionarioDTO.getNumeroDeCelular());
                Catalogo tipoDeDocumento = catalogoRepository.findByTokenIdentificadorAndRemovido(funcionarioDTO.getTokenIdentificadorTipoDeDocumento(), Boolean.FALSE);
                funcionario.setTipoDeDocumento(tipoDeDocumento);
                Jerarquia departamento = jerarquiaRepository.findJerarquiaByIdJerarquia(funcionarioDTO.getIdDepartamento());
                funcionario.setDepartamento(departamento);
                CargosJerarquia cargo = cargosJerarquiaRepository.findCargosJerarquiaByTokenIdentificador(funcionarioDTO.getTokenIdentificadorCargo());
                funcionario.setCargo(cargo);
                funcionario = this.funcionarioRepository.save(funcionario);
                persistirAsignaciones(funcionario, funcionarioDTO.getAsignaciones());
                // Obtener datos para el mensaje
                String nombresCompletos = obtenerNombresCompletos(funcionario);
                String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
                Date fechaAccion = new Date();
                String fechaFormateada = formatearFechaEspanol(fechaAccion);

                // Mensaje para el usuario
                String mensajeUsuario = "Se ha creado con éxito el funcionario " + funcionario.getNumeroDeDocumento();

                // Mensaje para auditoría
                String mensajeAuditoria = "Se creó con éxito el funcionario " + nombresCompletos +
                        " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

                df.llenarRespuestaExitosa(mensajeUsuario, funcionarioDTO, mensajeAuditoria);
                return df;

            } else {
                funcionario = funcionariosTempDocumento;
                funcionario.setNombres(funcionarioDTO.getNombres());
                funcionario.setApellidos(funcionarioDTO.getApellidos());
                funcionario.setEmail(funcionarioDTO.getEmail());
                funcionario.setTelefono(funcionarioDTO.getTelefono());
                funcionario.setNumeroDeDocumento(funcionarioDTO.getNumeroDeDocumento());
                funcionario.setNumeroDeCelular(funcionarioDTO.getNumeroDeCelular());
                Catalogo tipoDeDocumento = catalogoRepository.findByTokenIdentificadorAndRemovido(funcionarioDTO.getTokenIdentificadorTipoDeDocumento(), Boolean.FALSE);
                funcionario.setTipoDeDocumento(tipoDeDocumento);
                CargosJerarquia cargo = cargosJerarquiaRepository.findCargosJerarquiaByTokenIdentificador(funcionarioDTO.getTokenIdentificadorCargo());
                funcionario.setCargo(cargo);
                Jerarquia departamento = jerarquiaRepository.findJerarquiaByIdJerarquia(funcionarioDTO.getIdDepartamento());
                funcionario.setDepartamento(departamento);
                funcionario.setRemovido(false);

                funcionario = this.funcionarioRepository.save(funcionario);
                persistirAsignaciones(funcionario, funcionarioDTO.getAsignaciones());
                // Obtener datos para el mensaje
                String nombresCompletos = obtenerNombresCompletos(funcionario);
                String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
                Date fechaAccion = new Date();
                String fechaFormateada = formatearFechaEspanol(fechaAccion);

                // Mensaje para el usuario
                String mensajeUsuario = "Se ha modificado con éxito el funcionario";

                // Mensaje para auditoría
                String mensajeAuditoria = "Se editó con éxito el funcionario " + nombresCompletos +
                        " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

                df.llenarRespuestaExitosa(mensajeUsuario, funcionarioDTO, mensajeAuditoria);
                return df;

            }
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    public RespuestaPorDefectoAuditoria<FuncionarioDTO> crearFuncionarioCargaMasiva(FuncionarioDTO funcionarioDTO) {
        RespuestaPorDefectoAuditoria<FuncionarioDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            List<Funcionario> funcionariosTemp = this.funcionarioRepository.findByNumeroDeDocumento(funcionarioDTO.getNumeroDeDocumento());

            Funcionario funcionariosTempDocumento = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovido(funcionarioDTO.getNumeroDeDocumento(), Boolean.FALSE);

            if (funcionariosTempDocumento != null && !funcionarioDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el mismo número de documento.");
                return df;
            }

            List<Funcionario> funcionariosTempEmail = this.funcionarioRepository.findByEmailAndRemovido(funcionarioDTO.getEmail(), Boolean.FALSE);

            if (!funcionariosTempEmail.isEmpty() && !funcionarioDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un usuario con el mismo email.");
                return df;
            }

            Funcionario funcionario;
            if (funcionariosTemp.isEmpty() && !funcionarioDTO.getEsEdicion()) {
                funcionario = new Funcionario();
                funcionario.setNombres(funcionarioDTO.getNombres());
                funcionario.setApellidos(funcionarioDTO.getApellidos());
                funcionario.setEmail(funcionarioDTO.getEmail());
                funcionario.setTelefono(funcionarioDTO.getTelefono());
                funcionario.setNumeroDeDocumento(funcionarioDTO.getNumeroDeDocumento());
                funcionario.setNumeroDeCelular(funcionarioDTO.getNumeroDeCelular());
                Catalogo tipoDeDocumento = catalogoRepository.findByTokenIdentificadorAndRemovido(funcionarioDTO.getTokenIdentificadorTipoDeDocumento(), Boolean.FALSE);
                funcionario.setTipoDeDocumento(tipoDeDocumento);
                Jerarquia departamento = jerarquiaRepository.findJerarquiaByIdJerarquia(funcionarioDTO.getIdDepartamento());
                funcionario.setDepartamento(departamento);
                CargosJerarquia cargo = cargosJerarquiaRepository.findCargosJerarquiaByTokenIdentificador(funcionarioDTO.getTokenIdentificadorCargo());
                funcionario.setCargo(cargo);
                funcionario = this.funcionarioRepository.save(funcionario);

                // Obtener datos para el mensaje
                String nombresCompletos = obtenerNombresCompletos(funcionario);

                // Mensaje para el usuario
                String mensajeUsuario = "Se ha creado con éxito el funcionario " + funcionario.getNumeroDeDocumento();

                // Mensaje para auditoría
                String mensajeAuditoria = "Se creó con éxito el funcionario " + nombresCompletos + " mediante carga masiva";

                df.llenarRespuestaExitosa(mensajeUsuario, funcionarioDTO, mensajeAuditoria);
                return df;

            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FuncionarioDTO> eliminarFuncionario(HttpServletRequest httpServletRequest, FuncionarioDTO funcionarioDTO) {
        RespuestaPorDefectoAuditoria<FuncionarioDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovido(funcionarioDTO.getNumeroDeDocumento(), Boolean.FALSE);

            //df = this.crearFuncionario(funcionarioDTO, null, httpServletRequest.getRemoteAddr());

            // Obtener datos para el mensaje antes de marcar como removido
            String nombresCompletos = obtenerNombresCompletos(funcionario);
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            funcionario.setRemovido(true);
            funcionario = this.funcionarioRepository.save(funcionario);

            UsuarioSistema usuario = this.usuarioSistemaRepository.findByNumeroDeDocumentoAndRemovido(funcionarioDTO.getNumeroDeDocumento(), Boolean.FALSE);
            if (usuario != null) {
                usuario.setRemovido(Boolean.TRUE);
                usuarioSistemaRepository.save(usuario);
            }

            // Mensaje para el usuario
            String mensajeUsuario = "Se ha eliminado con éxito el funcionario";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito el funcionario " + nombresCompletos +
                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, funcionarioDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> obtenerFuncionarios(HttpServletRequest httpServletRequest,
                                                                                                BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idFuncionario").descending()
            );

            Page<Funcionario> funcionarios = this.funcionarioRepository.findByRemovido(false, pageable);

            PaginacionResponse<FuncionarioDTO> paginacionResponse = new PaginacionResponse<>();
            List<FuncionarioDTO> funcionarioDTOList = new ArrayList<>();

            for (Funcionario funcionario : funcionarios.toList()) {
                FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
                funcionarioDTO.setNombres(funcionario.getNombres());
                funcionarioDTO.setApellidos(funcionario.getApellidos());
                funcionarioDTO.setEmail(funcionario.getEmail());
                funcionarioDTO.setTelefono(funcionario.getTelefono());
                funcionarioDTO.setNumeroDeCelular(funcionario.getNumeroDeCelular());
                funcionarioDTO.setNumeroDeDocumento(funcionario.getNumeroDeDocumento());
                funcionarioDTO.setFechaCreacion(funcionario.getFechaCreacion());
                if (funcionario.getTipoDeDocumento() != null) {
                    funcionarioDTO.setTokenIdentificadorTipoDeDocumento(funcionario.getTipoDeDocumento().getTokenIdentificador());
                }
                if (funcionario.getCargo() != null) {
                    funcionarioDTO.setTokenIdentificadorCargo(funcionario.getCargo().getTokenIdentificador());
                    funcionarioDTO.setCargo(funcionario.getCargo().getNombre());
                    funcionarioDTO.setIdCargo(funcionario.getCargo().getIdCargosJerarquia());
                }
                if (funcionario.getDepartamento() != null) {
                    funcionarioDTO.setTokenIdentificadorDepartamento(funcionario.getDepartamento().getTokenIdentificador());
                    funcionarioDTO.setDepartamento(funcionario.getDepartamento().getNombre());
                    funcionarioDTO.setIdDepartamento(funcionario.getDepartamento().getIdJerarquia());
                } else {
                    List<FuncionarioJerarquiaRol> asignaciones = this.asignacionRepo.findByFuncionario_TokenIdentificadorAndRemovidoFalse(funcionario.getTokenIdentificador());
                    if (!asignaciones.isEmpty()) {
                        FuncionarioJerarquiaRol funcionarioJerarquiaRol = asignaciones.get(0);
                        funcionarioDTO.setTokenIdentificadorDepartamento(funcionarioJerarquiaRol.getJerarquia().getTokenIdentificador());
                        funcionarioDTO.setDepartamento(funcionarioJerarquiaRol.getJerarquia().getNombre());
                        funcionarioDTO.setIdDepartamento(funcionarioJerarquiaRol.getJerarquia().getIdJerarquia());
                    }
                }

                List<FuncionarioJerarquiaRol> asigns = asignacionRepo
                        .findByFuncionario_TokenIdentificadorAndRemovidoFalse(funcionario.getTokenIdentificador());

                List<FuncionarioJerarquiaRolDTO> asignacionesDto = asigns.stream().map(asig -> {
                    FuncionarioJerarquiaRolDTO dto = new FuncionarioJerarquiaRolDTO();
                    dto.setTokenIdentificadorJerarquia(asig.getJerarquia().getTokenIdentificador());
                    dto.setJerarquia(asig.getJerarquia().getNombre());
                    if (!ObjectUtils.isEmpty(asig.getRol())) {
                        dto.setTokenIdentificadorRol(asig.getRol().getTokenIdentificador());
                        dto.setRol(asig.getRol().getNombre());
                    }


//                    dto.setTokenIdentificadorCargo(asig.getCargo().getTokenIdentificador());
//                    dto.setCargo(asig.getCargo().getNombre());
                    return dto;
                }).toList();
                funcionarioDTO.setAsignaciones(asignacionesDto);
                funcionarioDTOList.add((funcionarioDTO));
            }

            paginacionResponse.setData(funcionarioDTOList);
            paginacionResponse.setTotalItems(funcionarios.getTotalElements());

            // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
            long totalElementos = funcionarios.getTotalElements(); // Total de funcionarios en el sistema
            long elementosPaginaActual = funcionarioDTOList.size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de: " + totalElementos + " de: " + totalElementos + " elementos disponibles";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de: " + totalElementos + " funcionarios";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<FuncionarioDTO>> obtenerFuncionariosSinPaginacion(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<FuncionarioDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            List<Funcionario> funcionarios = this.funcionarioRepository.obtenerPorRemovido(false);

            List<FuncionarioDTO> funcionarioDTOList = new ArrayList<>();

            for (Funcionario funcionario : funcionarios) {
                FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
                funcionarioDTO.setNombres(funcionario.getNombres());
                funcionarioDTO.setApellidos(funcionario.getApellidos());
                funcionarioDTO.setEmail(funcionario.getEmail());
                funcionarioDTO.setTelefono(funcionario.getTelefono());
                funcionarioDTO.setNumeroDeCelular(funcionario.getNumeroDeCelular());
                funcionarioDTO.setNumeroDeDocumento(funcionario.getNumeroDeDocumento());
                funcionarioDTO.setFechaCreacion(funcionario.getFechaCreacion());
                funcionarioDTO.setTokenIdentificador(funcionario.getTokenIdentificador());
                if (funcionario.getTipoDeDocumento() != null) {
                    funcionarioDTO.setTokenIdentificadorTipoDeDocumento(funcionario.getTipoDeDocumento().getTokenIdentificador());
                }
                if (funcionario.getCargo() != null) {
                    funcionarioDTO.setTokenIdentificadorCargo(funcionario.getCargo().getTokenIdentificador());
                    funcionarioDTO.setCargo(funcionario.getCargo().getNombre());
                    funcionarioDTO.setIdCargo(funcionario.getCargo().getIdCargosJerarquia());
                }
                if (funcionario.getDepartamento() != null) {
                    funcionarioDTO.setTokenIdentificadorDepartamento(funcionario.getDepartamento().getTokenIdentificador());
                    funcionarioDTO.setDepartamento(funcionario.getDepartamento().getNombre());
                    funcionarioDTO.setIdDepartamento(funcionario.getDepartamento().getIdJerarquia());
                } else {
                    List<FuncionarioJerarquiaRol> asignaciones = this.asignacionRepo.findByFuncionario_TokenIdentificadorAndRemovidoFalse(funcionario.getTokenIdentificador());
                    if (!asignaciones.isEmpty()) {
                        FuncionarioJerarquiaRol funcionarioJerarquiaRol = asignaciones.get(0);
                        funcionarioDTO.setTokenIdentificadorDepartamento(funcionarioJerarquiaRol.getJerarquia().getTokenIdentificador());
                        funcionarioDTO.setDepartamento(funcionarioJerarquiaRol.getJerarquia().getNombre());
                        funcionarioDTO.setIdDepartamento(funcionarioJerarquiaRol.getJerarquia().getIdJerarquia());
                    }
                }

                List<FuncionarioJerarquiaRol> asigns = asignacionRepo
                        .findByFuncionario_TokenIdentificadorAndRemovidoFalse(funcionario.getTokenIdentificador());

                List<FuncionarioJerarquiaRolDTO> asignacionesDto = asigns.stream().map(asig -> {
                    FuncionarioJerarquiaRolDTO dto = new FuncionarioJerarquiaRolDTO();
                    dto.setTokenIdentificadorJerarquia(asig.getJerarquia().getTokenIdentificador());
                    dto.setJerarquia(asig.getJerarquia().getNombre());
                    dto.setTokenIdentificador(asig.getTokenIdentificador());
                    if (!ObjectUtils.isEmpty(asig.getRol())) {
                        dto.setTokenIdentificadorRol(asig.getRol().getTokenIdentificador());
                        dto.setRol(asig.getRol().getNombre());
                    }


//                    dto.setTokenIdentificadorCargo(asig.getCargo().getTokenIdentificador());
//                    dto.setCargo(asig.getCargo().getNombre());
                    return dto;
                }).toList();
                funcionarioDTO.setAsignaciones(asignacionesDto);
                funcionarioDTOList.add((funcionarioDTO));
            }

            //funcionarioDTOList.sort(Comparator.comparing(FuncionarioDTO::getNombres));
            funcionarioDTOList.sort(
                    Comparator.comparing(
                            f -> f.getNombres()
                                    .replaceAll("\\s+", "")
                                    .toLowerCase()
                    )
            );

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de: " + funcionarioDTOList.size() + " elementos disponibles";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de: " + funcionarioDTOList.size() + " funcionarios";

            df.llenarRespuestaExitosa(mensajeUsuario, funcionarioDTOList, mensajeAuditoria);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> obtenerFuncionariosPorValor(HttpServletRequest httpServletRequest, String valor,
                                                                                                        BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idFuncionario").descending()
            );

            Page<Funcionario> funcionarios = this.funcionarioRepository.buscarPorValor(paginacionRequest.getFilter(), pageable);

            PaginacionResponse<FuncionarioDTO> paginacionResponse = new PaginacionResponse<>();
            List<FuncionarioDTO> funcionarioDTOList = new ArrayList<>();

            for (Funcionario funcionario : funcionarios.toList()) {
                FuncionarioDTO funcionarioDTO = new FuncionarioDTO();
                funcionarioDTO.setNombres(funcionario.getNombres());
                funcionarioDTO.setApellidos(funcionario.getApellidos());
                funcionarioDTO.setEmail(funcionario.getEmail());
                funcionarioDTO.setTelefono(funcionario.getTelefono());
                funcionarioDTO.setNumeroDeCelular(funcionario.getNumeroDeCelular());
                funcionarioDTO.setNumeroDeDocumento(funcionario.getNumeroDeDocumento());
                funcionarioDTO.setFechaCreacion(funcionario.getFechaCreacion());
                if (funcionario.getTipoDeDocumento() != null) {
                    funcionarioDTO.setTokenIdentificadorTipoDeDocumento(funcionario.getTipoDeDocumento().getTokenIdentificador());
                }
                if (funcionario.getCargo() != null) {
                    funcionarioDTO.setTokenIdentificadorCargo(funcionario.getCargo().getTokenIdentificador());
                    funcionarioDTO.setCargo(funcionario.getCargo().getNombre());
                    funcionarioDTO.setIdCargo(funcionario.getCargo().getIdCargosJerarquia());
                }
                if (funcionario.getDepartamento() != null) {
                    funcionarioDTO.setTokenIdentificadorDepartamento(funcionario.getDepartamento().getTokenIdentificador());
                    funcionarioDTO.setDepartamento(funcionario.getDepartamento().getNombre());
                    funcionarioDTO.setIdDepartamento(funcionario.getDepartamento().getIdJerarquia());
                } else {
                    List<FuncionarioJerarquiaRol> asignaciones = this.asignacionRepo.findByFuncionario_TokenIdentificadorAndRemovidoFalse(funcionario.getTokenIdentificador());
                    if (!asignaciones.isEmpty()) {
                        FuncionarioJerarquiaRol funcionarioJerarquiaRol = asignaciones.get(0);
                        funcionarioDTO.setTokenIdentificadorDepartamento(funcionarioJerarquiaRol.getJerarquia().getTokenIdentificador());
                        funcionarioDTO.setDepartamento(funcionarioJerarquiaRol.getJerarquia().getNombre());
                        funcionarioDTO.setIdDepartamento(funcionarioJerarquiaRol.getJerarquia().getIdJerarquia());
                    }
                }

                List<FuncionarioJerarquiaRol> asigns = asignacionRepo
                        .findByFuncionario_TokenIdentificadorAndRemovidoFalse(funcionario.getTokenIdentificador());

                List<FuncionarioJerarquiaRolDTO> asignacionesDto = asigns.stream().map(asig -> {
                    FuncionarioJerarquiaRolDTO dto = new FuncionarioJerarquiaRolDTO();
                    dto.setTokenIdentificadorJerarquia(asig.getJerarquia().getTokenIdentificador());
                    dto.setJerarquia(asig.getJerarquia().getNombre());
                    if (!ObjectUtils.isEmpty(asig.getRol())) {
                        dto.setTokenIdentificadorRol(asig.getRol().getTokenIdentificador());
                        dto.setRol(asig.getRol().getNombre());
                    }


//                    dto.setTokenIdentificadorCargo(asig.getCargo().getTokenIdentificador());
//                    dto.setCargo(asig.getCargo().getNombre());
                    return dto;
                }).toList();
                funcionarioDTO.setAsignaciones(asignacionesDto);
                funcionarioDTOList.add((funcionarioDTO));
            }

            paginacionResponse.setData(funcionarioDTOList);
            paginacionResponse.setTotalItems(funcionarios.getTotalElements());

            // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
            long totalElementos = funcionarios.getTotalElements(); // Total de funcionarios que coinciden con el filtro
            long elementosPaginaActual = funcionarioDTOList.size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se encontraron " + totalElementos + " funcionarios que coinciden con el filtro '" + paginacionRequest.getFilter() + "', mostrando " + elementosPaginaActual + " en esta página";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de: " + totalElementos + " funcionarios filtrados";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FuncionarioDTO> obtenerFuncionarioDelUsuario(HttpServletRequest httpServletRequest) {

        RespuestaPorDefectoAuditoria<FuncionarioDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuario = df2.getData().getUsuarioSistema();

            Jerarquia jerarquiaActual = df2.getData().getJerarquia();

            Rol rol = df2.getData().getRol();

            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovido(usuario.getNumeroDeDocumento(), false);

            FuncionarioDTO funcionarioDTO = new FuncionarioDTO();

            funcionarioDTO.setNombres(funcionario.getNombres());
            funcionarioDTO.setApellidos(funcionario.getApellidos());
            funcionarioDTO.setEmail(funcionario.getEmail());
            funcionarioDTO.setTelefono(funcionario.getTelefono());
            funcionarioDTO.setNumeroDeCelular(funcionario.getNumeroDeCelular());
            funcionarioDTO.setNumeroDeDocumento(funcionario.getNumeroDeDocumento());
            funcionarioDTO.setFechaCreacion(funcionario.getFechaCreacion());
            funcionarioDTO.setDepartamento(jerarquiaActual.getNombre());
            funcionarioDTO.setTokenIdentificadorDepartamento(jerarquiaActual.getTokenIdentificador());
            //funcionarioDTO.setCargo(funcionario.getCargo().getNombre());
            funcionarioDTO.setCargo(rol.getNombre());
            funcionarioDTO.setCargoSuperRol(rol.getEsSuperRol());
            funcionarioDTO.setIdDepartamento(jerarquiaActual.getIdJerarquia());

            if (funcionario.getAsignaciones() != null && !funcionario.getAsignaciones().isEmpty()) {
                funcionarioDTO.setNumeroCentros((long) funcionario.getAsignaciones().size());
            }

            // Obtener datos para el mensaje
            String nombresCompletos = obtenerNombresCompletos(funcionario);

            // Mensaje para el usuario
            String mensajeUsuario = "Funcionario: ";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se obtuvo con éxito la información del funcionario " + nombresCompletos;

            df.llenarRespuestaExitosa(mensajeUsuario, funcionarioDTO, mensajeAuditoria);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorFuncionarios(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuario = df2.getData().getUsuarioSistema();

            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovido(usuario.getNumeroDeDocumento(), false);

            List<JerarquiaDTO> jerarquiaDTOList = new ArrayList<>();

            for (FuncionarioJerarquiaRol jerarquiaRol : funcionario.getAsignaciones()) {
                if (!jerarquiaRol.getRemovido()) {
                    Jerarquia jerarquia = jerarquiaRol.getJerarquia();
                    JerarquiaDTO nuevaJerarquiaDTO = new JerarquiaDTO();
                    nuevaJerarquiaDTO.setId(jerarquia.getIdJerarquia());
                    if (jerarquia.getJerarquiaPadre() != null) {
                        nuevaJerarquiaDTO.setIdJerarquiaPadre(jerarquia.getJerarquiaPadre().getIdJerarquia());
                    }
                    nuevaJerarquiaDTO.setNombre(jerarquia.getNombre());
                    nuevaJerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
                    nuevaJerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
                    nuevaJerarquiaDTO.setEmpresa(jerarquia.getEmpresa().getIdEmpresa());

                    jerarquiaDTOList.add(nuevaJerarquiaDTO);
                }

            }

            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvieron con éxito " + jerarquiaDTOList.size() + " jerarquías por nemónico padre";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + jerarquiaDTOList.size() + " jerarquías del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
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
     * Método auxiliar para obtener nombres completos de un funcionario
     */
    private String obtenerNombresCompletos(Funcionario funcionario) {
        if (funcionario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (funcionario.getNombres() != null && !funcionario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(funcionario.getNombres());
        }
        if (funcionario.getApellidos() != null && !funcionario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(funcionario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
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

    private void persistirAsignaciones(
            Funcionario funcionario,
            List<FuncionarioJerarquiaRolDTO> asignacionesDto
    ) {
        if (asignacionesDto == null) return;

        // 1) Marcar como removidas las que el front ha quitado
        List<FuncionarioJerarquiaRol> existentes = asignacionRepo
                .findByFuncionario_TokenIdentificadorAndRemovidoFalse(
                        funcionario.getTokenIdentificador()
                );

        Set<String> jerarquiasFront = asignacionesDto.stream()
                .map(FuncionarioJerarquiaRolDTO::getTokenIdentificadorJerarquia)
                .collect(Collectors.toSet());

        for (FuncionarioJerarquiaRol asig : existentes) {
            String tokJer = asig.getJerarquia().getTokenIdentificador();
            if (!jerarquiasFront.contains(tokJer)) {
                asig.setRemovido(true);
                asig.setFechaEliminacion(new Date());
                asignacionRepo.save(asig);
            }
        }

        String tokFunc = funcionario.getTokenIdentificador();
        for (FuncionarioJerarquiaRolDTO dto : asignacionesDto) {
            String tokJer = dto.getTokenIdentificadorJerarquia();
            String tokRol = dto.getTokenIdentificadorRol();  // puede ser null

            // 2) Buscar asignación por funcionario+jerarquía (sin filtrar removido)
            Optional<FuncionarioJerarquiaRol> opt = asignacionRepo
                    .findByFuncionario_TokenIdentificadorAndJerarquia_TokenIdentificador(
                            tokFunc, tokJer
                    );

            // 3) Cargar la jerarquía (siempre obligatoria)
            Jerarquia jer = jerarquiaRepository
                    .findByTokenIdentificadorAndRemovido(tokJer, false);
            if (jer == null) {
                throw new IllegalStateException(
                        "Jerarquía no encontrada: " + tokJer
                );
            }

            // 4) Si viene un tokenRol, lo cargamos; si no, dejamos rol en null
            Rol rol = null;
            if (tokRol != null) {
                rol = rolRepository
                        .findByTokenIdentificadorAndRemovido(tokRol, false);
                if (rol == null) {
                    throw new IllegalStateException(
                            "Rol no encontrado: " + tokRol
                    );
                }
            }

            if (opt.isPresent()) {
                // 5a) Ya existe: solo actualizamos rol si vino en el DTO
                FuncionarioJerarquiaRol exist = opt.get();
                if (rol != null) {
                    exist.setRol(rol);
                }
                exist.setRemovido(false);
                exist.setFechaEdicion(new Date());
                asignacionRepo.save(exist);

            } else {
                // 5b) No existe: creamos nuevo (rol puede quedar null)
                FuncionarioJerarquiaRol nuevo = new FuncionarioJerarquiaRol();
                nuevo.setFuncionario(funcionario);
                nuevo.setJerarquia(jer);
                if (rol != null) {
                    nuevo.setRol(rol);
                }
                nuevo.setRemovido(false);
                nuevo.setFechaCreacion(new Date());
                asignacionRepo.save(nuevo);
            }
        }
    }
}
