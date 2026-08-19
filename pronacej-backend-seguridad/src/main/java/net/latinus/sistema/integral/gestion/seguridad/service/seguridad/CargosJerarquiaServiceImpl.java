package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.CargosJerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CargosJerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.CargosJerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
class CargosJerarquiaServiceImpl implements CargosJerarquiaService {
    
    private JwtProviderService jwtProviderService;
    private CargosJerarquiaRepository cargosJerarquiaRepository;
    private JerarquiaRepository jerarquiaRepository;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<List<CargosJerarquiaDTO>> obtenerCargosJerarquias(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<CargosJerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            //String body = bodyEncriptado.desencriptar(this.rsa, this.aes);

            //catalogo Dto debe de ser el catalogo padre
            //CargosJerarquiaDTO cargosJerarquiaDTO = new Gson().fromJson(body, CargosJerarquiaDTO.class);

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            List<CargosJerarquia> cargosJerarquiaList = this.cargosJerarquiaRepository.findByEmpresaTokenIdentificadorAndRemovidoOrderByIdCargosJerarquiaDesc(
                    empresa.getTokenIdentificador(), false
            );

            List<CargosJerarquiaDTO> cargosJerarquiaDTOList = new ArrayList<>();

            for (CargosJerarquia cargosJerarquia : cargosJerarquiaList) {
                CargosJerarquiaDTO nuevoCargosJerarquiaDTO = new CargosJerarquiaDTO();
                nuevoCargosJerarquiaDTO.setTokenIdentificador(cargosJerarquia.getTokenIdentificador());
                nuevoCargosJerarquiaDTO.setEsJefe(cargosJerarquia.getEsJefe());
                nuevoCargosJerarquiaDTO.setNombre(cargosJerarquia.getNombre());

                // Verificar si la jerarquía existe
                if (cargosJerarquia.getJerarquia() != null) {
                    nuevoCargosJerarquiaDTO.setTokenIdentificadorJerarquia(cargosJerarquia.getJerarquia().getTokenIdentificador());
                    nuevoCargosJerarquiaDTO.setIdJerarquia(cargosJerarquia.getJerarquia().getIdJerarquia());
                } else {
                    nuevoCargosJerarquiaDTO.setTokenIdentificadorJerarquia(null);
                    nuevoCargosJerarquiaDTO.setIdJerarquia(null);
                }

                cargosJerarquiaDTOList.add(nuevoCargosJerarquiaDTO);
            }

            df.llenarRespuestaExitosa("Se han encontrado un total de: " +
                    cargosJerarquiaDTOList.size() + " cargos por jerarquia", cargosJerarquiaDTOList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> obtenerCargosJerarquiasPaginado(HttpServletRequest httpServletRequest,
                                                                                                        BodyEncriptado bodyEncriptado){
        RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();
            Empresa empresa = df2.getData().getEmpresa();

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
                    Sort.by("idCargosJerarquia").descending()
            );

            Page<CargosJerarquia> cargosJerarquiaPage = this.cargosJerarquiaRepository.findByEmpresaIdEmpresaAndRemovido(
                    empresa.getIdEmpresa(), false, pageable
            );

            PaginacionResponse<CargosJerarquiaDTO> paginacionResponse = new PaginacionResponse<>();
            List<CargosJerarquiaDTO> cargosJerarquiaDTOList = new ArrayList<>();
            for (CargosJerarquia cargosJerarquia : cargosJerarquiaPage.toList()) {
                CargosJerarquiaDTO nuevoCargosJerarquiaDTO = new CargosJerarquiaDTO();
                nuevoCargosJerarquiaDTO.setTokenIdentificador(cargosJerarquia.getTokenIdentificador());
                nuevoCargosJerarquiaDTO.setEsJefe(cargosJerarquia.getEsJefe());
                nuevoCargosJerarquiaDTO.setNombre(cargosJerarquia.getNombre());

                // Verificar si la jerarquía existe antes de acceder a sus propiedades
                if (cargosJerarquia.getJerarquia() != null) {
                    nuevoCargosJerarquiaDTO.setTokenIdentificadorJerarquia(cargosJerarquia.getJerarquia().getTokenIdentificador());
                    nuevoCargosJerarquiaDTO.setIdJerarquia(cargosJerarquia.getJerarquia().getIdJerarquia());
                    nuevoCargosJerarquiaDTO.setDepartamento(cargosJerarquia.getJerarquia().getNombre());
                } else {
                    // Si la jerarquía es nula, asignar valores nulos
                    nuevoCargosJerarquiaDTO.setTokenIdentificadorJerarquia(null);
                    nuevoCargosJerarquiaDTO.setIdJerarquia(null);
                    nuevoCargosJerarquiaDTO.setDepartamento(null);
                }

                cargosJerarquiaDTOList.add(nuevoCargosJerarquiaDTO);
            }

            paginacionResponse.setData(cargosJerarquiaDTOList);
            paginacionResponse.setTotalItems((cargosJerarquiaPage.getTotalElements()));
            
            // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
            long totalElementos = cargosJerarquiaPage.getTotalElements(); // Total de cargos en el sistema
            long elementosPaginaActual = cargosJerarquiaDTOList.size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de " + totalElementos + " cargos disponibles en el sistema, mostrando " + elementosPaginaActual + " en esta página. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " cargos del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<CargosJerarquiaDTO> crearCargoJerarquia (HttpServletRequest httpServletRequest,
                                                           BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<CargosJerarquiaDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            CargosJerarquiaDTO cargosJerarquiaDTO = new Gson().fromJson(bodyString, CargosJerarquiaDTO.class);

            cargosJerarquiaDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            CargosJerarquia cargo;
            boolean esEdicion = false;
            
            if (cargosJerarquiaDTO.getEsEdicion()) {
                cargo = this.cargosJerarquiaRepository.findByTokenIdentificadorAndRemovido(cargosJerarquiaDTO.getTokenIdentificador(), false);
                if (cargo == null) {
                    df.setMensaje("El cargo a editar no existe o ya fue eliminado anteriormente");
                    return df;
                }

                esEdicion = true;
                cargo.setFechaEdicion(new Date());
                cargo.setIpEdita(ip);
                cargo.setUsuarioSistemaEdita(usuarioLogin);
            } else {
                cargo = new CargosJerarquia();
                cargo.setFechaCreacion(new Date());
                cargo.setIpCrea(ip);
                cargo.setUsuarioSistemaCrea(usuarioLogin);
                cargo.setEmpresa(df2.getData().getEmpresa());
            }            
            cargo.setEsJefe((cargosJerarquiaDTO.getEsJefe()!=null && cargosJerarquiaDTO.getEsJefe()==true));
            if (cargosJerarquiaDTO.getIdJerarquia() != null) {
                Jerarquia departamento = jerarquiaRepository.findJerarquiaByIdJerarquia(cargosJerarquiaDTO.getIdJerarquia());
                if (departamento != null) {
                    cargo.setJerarquia(departamento);
                }
            } else {
                cargo.setJerarquia(null); // Establecer explícitamente como nulo
            }
           
            cargo.setNombre(cargosJerarquiaDTO.getNombre());

            cargo = this.cargosJerarquiaRepository.save(cargo);
            cargosJerarquiaDTO.setTokenIdentificador(cargo.getTokenIdentificador());

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String accion = esEdicion ? "editó" : "creó";

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se realizo la operación con exito sobre el cargo: " + cargo.getNombre();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se " + accion + " con éxito el cargo " + cargo.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, cargosJerarquiaDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarCargoJerarquia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            CargosJerarquiaDTO cargosJerarquiaDTO = new Gson().fromJson(bodyString, CargosJerarquiaDTO.class);

            CargosJerarquia cargo = this.cargosJerarquiaRepository.findByTokenIdentificadorAndRemovido(
                    cargosJerarquiaDTO.getTokenIdentificador(), false
            );

            if (cargo == null) {
                df.setMensaje("El cargo no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            Date fecha = new Date();
            cargo.setRemovido(true);
            cargo.setIpElimina(ip);
            cargo.setUsuarioSistemaElimina(usuarioSistemaLogin);
            cargo.setFechaEliminacion(fecha);

            this.cargosJerarquiaRepository.save(cargo);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String fechaFormateada = formatearFechaEspanol(fecha);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se ha eliminado con exito del sistema el cargo: " + cargo.getNombre();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito el cargo " + cargo.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> obtenerCargosJerarquiaPorValor(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<CargosJerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación del token JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            // Desencriptar el cuerpo de la solicitud
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            
            // Configuración de paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idCargosJerarquia").descending()
            );

            // Obtener los datos de la base de datos
            String filtroLimpio = paginacionRequest.getFilter() != null ? paginacionRequest.getFilter().trim() : "";
            Page<CargosJerarquia> cargosJerarquia = this.cargosJerarquiaRepository.buscarPorValor(paginacionRequest.getFilter(), pageable);

            // Convertir entidades a DTOs
            PaginacionResponse<CargosJerarquiaDTO> paginacionResponse = new PaginacionResponse<>();
            List<CargosJerarquiaDTO> cargosJerarquiaDTOList = new ArrayList<>();

            for (CargosJerarquia cargo : cargosJerarquia.toList()) {
                CargosJerarquiaDTO cargoDTO = new CargosJerarquiaDTO();
                cargoDTO.setIdCargosJerarquia(cargo.getIdCargosJerarquia());
                cargoDTO.setNombre(cargo.getNombre());
                cargoDTO.setEsJefe(cargo.getEsJefe());

                if (cargo.getJerarquia() != null) {
                    cargoDTO.setIdJerarquia(cargo.getJerarquia().getIdJerarquia());
                    cargoDTO.setTokenIdentificadorJerarquia(cargo.getJerarquia().getNemonico());
                    cargoDTO.setDepartamento(cargo.getJerarquia().getNombre());
                } else {
                    cargoDTO.setIdJerarquia(null);
                    cargoDTO.setTokenIdentificadorJerarquia(null);
                    cargoDTO.setDepartamento(null);
                }

                cargosJerarquiaDTOList.add(cargoDTO);
            }

            paginacionResponse.setData(cargosJerarquiaDTOList);
            paginacionResponse.setTotalItems(cargosJerarquia.getTotalElements());

            // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
            long totalElementos = cargosJerarquia.getTotalElements(); // Total de cargos que coinciden con el filtro
            long elementosPaginaActual = cargosJerarquiaDTOList.size(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se encontraron " + totalElementos + " cargos que coinciden con el filtro '" + paginacionRequest.getFilter() + "', mostrando " + elementosPaginaActual + " en esta página. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " cargos filtrados del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

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
}