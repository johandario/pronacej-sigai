package net.latinus.sistema.integral.gestion.seguridad.service.param;

import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.*;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FuncionarioRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.PlantillaFormularioRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.PlantillaVariableRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.CargosJerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;

@Service
@AllArgsConstructor
public class PlantillaFormularioServiceImpl implements PlantillaFormularioService {
    private FuncionarioRepository funcionarioRepository;
    private CatalogoRepository catalogoRepository;
    private JerarquiaRepository jerarquiaRepository;
    private CargosJerarquiaRepository cargosJerarquiaRepository;
    private PlantillaFormularioRepository plantillaFormularioRepository;
    private PlantillaVariableRepository plantillaVariableRepository;
    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;
    private ParametroDelSistemaService parametroDelSistemaService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> crearPlantillaFormulario(HttpServletRequest httpServletRequest, PlantillaFormularioDTO plantillaFormularioDTO) {
        RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuario = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            PlantillaFormulario plantillaFormulario;
            boolean esEdicion = plantillaFormularioDTO.getEsEdicion();

            if (!esEdicion) {
                plantillaFormulario = plantillaFormularioRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(plantillaFormularioDTO.getNemonico(), empresa.getTokenIdentificador(), false);

                if (plantillaFormulario != null) {
                    df.setMensaje("Ya existe una plantilla asignada a ese formulario.");
                    return df;
                }

                plantillaFormulario = new PlantillaFormulario();
                plantillaFormulario.setEmpresa(empresa);
                plantillaFormulario.setFechaCreacion(new Date());
                plantillaFormulario.setUsuarioSistemaCrea(usuario);
                plantillaFormulario.setIpCrea(ip);
                plantillaFormulario.setNemonico(plantillaFormularioDTO.getNemonico());
                Catalogo formularioRelacionado = catalogoRepository.findByTokenIdentificadorAndRemovido(plantillaFormularioDTO.getTokenIdentificadorFormularioRelacionado(), Boolean.FALSE);
                plantillaFormulario.setFormularioRelacionado(formularioRelacionado);
                Catalogo estadoActivo = catalogoRepository.findByNemonicoAndRemovido("ESTADO_ACTIVO", Boolean.FALSE);
                plantillaFormulario.setEstado(estadoActivo);
                List<PlantillaFormulario> listaFormulariosRepetidos = plantillaFormularioRepository.findByNemonicoAndEmpresaIdEmpresaAndRemovido(plantillaFormularioDTO.getNemonico(), empresa.getIdEmpresa(), Boolean.FALSE);
                for (PlantillaFormulario pf : listaFormulariosRepetidos) {
                    // LAS PLANTILLAS ANTIGUAS PASAN A SER INACTIVAS
                    Catalogo estadoInactivo = catalogoRepository.findByNemonicoAndRemovido("ESTADO_INACTIVO", Boolean.FALSE);
                    pf.setEstado(estadoInactivo);
                    plantillaFormularioRepository.save(pf);
                }
            } else {
                plantillaFormulario = plantillaFormularioRepository.findByTokenIdentificadorAndRemovido(plantillaFormularioDTO.getTokenIdentificador(), Boolean.FALSE);
                if (plantillaFormulario == null) {
                    df.setMensaje("La plantilla a editar no existe o ya fue eliminada anteriormente");
                    return df;
                }
                plantillaFormulario.setFechaEdicion(new Date());
                plantillaFormulario.setUsuarioSistemaEdita(usuario);
                plantillaFormulario.setIpEdita(ip);
            }

            plantillaFormulario.setRazon(plantillaFormularioDTO.getRazon());
            plantillaFormulario.setDescripcion(plantillaFormularioDTO.getDescripcion());
            plantillaFormulario.setFormularioString(plantillaFormularioDTO.getFormularioString());
            plantillaFormulario.setContenidoHtml(plantillaFormularioDTO.getContenidoHtml());

            plantillaFormularioRepository.save(plantillaFormulario);

            for (PlantillaVariableDTO plantillaVariableDTO : plantillaFormularioDTO.getListaVariables()) {
                if ("".equals(plantillaVariableDTO.getTokenIdentificador())) {
                    // Solo cuando son nuevas se crean
                    PlantillaVariable plantillaVariable = new PlantillaVariable();
                    plantillaVariable.setNombre(plantillaVariableDTO.getNombre());
                    plantillaVariable.setClave(plantillaVariableDTO.getClave());
                    plantillaVariable.setIpCrea(ip);
                    plantillaVariable.setUsuarioSistemaCrea(usuario);
                    plantillaVariable.setEmpresa(empresa);
                    plantillaVariable.setFechaCreacion(new Date());
                    plantillaVariable.setPlantillaFormulario(plantillaFormulario);
                    plantillaVariableRepository.save(plantillaVariable);
                }
            }

            // Cuando se eliminan variables que ya existían al editar la plantilla se envían en listaVariablesEliminar
            for (PlantillaVariableDTO plantillaVariableEliminarDTO : plantillaFormularioDTO.getListaVariablesEliminar()) {
                PlantillaVariable plantillaVariableEliminar = plantillaVariableRepository.findByTokenIdentificadorAndRemovido(plantillaVariableEliminarDTO.getTokenIdentificador(), Boolean.FALSE);
                if (plantillaVariableEliminar != null) {
                    plantillaVariableEliminar.setFechaEliminacion(new Date());
                    plantillaVariableEliminar.setUsuarioSistemaElimina(usuario);
                    plantillaVariableEliminar.setIpElimina(ip);
                    plantillaVariableEliminar.setRemovido(Boolean.TRUE);
                    plantillaVariableRepository.save(plantillaVariableEliminar);
                }
            }

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuario);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String accion = esEdicion ? "editó" : "creó";

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Se ha " + (esEdicion ? "editado" : "creado") + " con éxito la plantilla: " + plantillaFormulario.getNemonico();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se " + accion + " con éxito la plantilla de formulario " + plantillaFormulario.getNemonico() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, plantillaFormularioDTO, mensajeAuditoria);
            return df;

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> eliminarPlantillaFormulario(HttpServletRequest httpServletRequest, PlantillaFormularioDTO plantillaFormularioDTO) {
        RespuestaPorDefectoAuditoria<PlantillaFormularioDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuario = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            PlantillaFormulario plantillaFormulario = this.plantillaFormularioRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(plantillaFormularioDTO.getNemonico(), plantillaFormularioDTO.getTokenIdentificadorEmpresa(), Boolean.FALSE);

            plantillaFormulario.setUsuarioSistemaElimina(usuario);
            plantillaFormulario.setFechaEliminacion(new Date());
            plantillaFormulario.setIpElimina(ip);
            plantillaFormulario.setRemovido(true);
            plantillaFormulario = this.plantillaFormularioRepository.save(plantillaFormulario);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuario);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Se ha eliminado con éxito la plantilla formulario con nemónico: " + plantillaFormulario.getNemonico();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito la plantilla de formulario " + plantillaFormulario.getNemonico() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, plantillaFormularioDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaFormularioDTO>> obtenerPlantillasFormulario(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaFormularioDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioConsultante = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            Empresa empresa = df2.getData().getEmpresa();

            List<PlantillaFormulario> plantillasFormulario = this.plantillaFormularioRepository.findByEmpresaIdEmpresaAndRemovido(empresa.getIdEmpresa(), Boolean.FALSE);

            List<PlantillaFormularioDTO> plantillaFormularioDTOList = new ArrayList<>();

            for (PlantillaFormulario plantillaFormulario : plantillasFormulario) {
                PlantillaFormularioDTO plantillaFormularioDTO = new PlantillaFormularioDTO();
                plantillaFormularioDTO.setTokenIdentificador(plantillaFormulario.getTokenIdentificador());
                plantillaFormularioDTO.setDescripcion(plantillaFormulario.getDescripcion());
                plantillaFormularioDTO.setNemonico(plantillaFormulario.getNemonico());
                plantillaFormularioDTO.setRazon(plantillaFormulario.getRazon());
                plantillaFormularioDTO.setFormularioString(plantillaFormulario.getFormularioString());
                plantillaFormularioDTO.setContenidoHtml(plantillaFormulario.getContenidoHtml());
                plantillaFormularioDTO.setFechaCreacion(plantillaFormulario.getFechaCreacion());
                if (plantillaFormulario.getFormularioRelacionado() != null) {
                    plantillaFormularioDTO.setTokenIdentificadorFormularioRelacionado(plantillaFormulario.getFormularioRelacionado().getTokenIdentificador());
                }
                if (plantillaFormulario.getEmpresa() != null) {
                    plantillaFormularioDTO.setTokenIdentificadorEmpresa(plantillaFormulario.getEmpresa().getTokenIdentificador());
                }
                List<PlantillaVariableDTO> plantillasVariablesDTO = new ArrayList<>();

                //LISTA VARIABLES
                List<PlantillaVariable> plantillasVariables = this.plantillaVariableRepository.findByPlantillaFormularioIdPlantillaFormularioAndRemovido(plantillaFormulario.getIdPlantillaFormulario(), Boolean.FALSE);
                for (PlantillaVariable plantillaVariable : plantillasVariables) {
                    PlantillaVariableDTO plantillaVariableDTO = new PlantillaVariableDTO();
                    plantillaVariableDTO.setClave(plantillaVariable.getClave());
                    plantillaVariableDTO.setNombre(plantillaVariable.getNombre());
                    plantillaVariableDTO.setTokenIdentificador(plantillaVariable.getTokenIdentificador());
                    plantillasVariablesDTO.add(plantillaVariableDTO);
                }
                plantillaFormularioDTO.setListaVariables(plantillasVariablesDTO);
                plantillaFormularioDTOList.add(plantillaFormularioDTO);
            }

            plantillaFormularioDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<PlantillaFormularioDTO> paginacionResponse = paginacionService.obtenerDatos(plantillaFormularioDTOList, paginacionRequest);

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Plantillas Formulario. Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                  " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                  "(" + usuarioConsultante.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + plantillaFormularioDTOList.size() + " plantillas de formulario del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> obtenerFuncionariosPorValor(HttpServletRequest httpServletRequest, String valor, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FuncionarioDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioConsultante = df2.getData().getUsuarioSistema();

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

            Page<Funcionario> funcionarios = this.funcionarioRepository.buscarPorValor(valor, pageable);

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
                funcionarioDTOList.add(funcionarioDTO);
            }

            paginacionResponse.setData(funcionarioDTOList);
            paginacionResponse.setTotalItems(funcionarios.getTotalElements());

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Se han encontrado un total de: " + funcionarioDTOList.size() + " de: " + funcionarios.getTotalElements() + " elementos disponibles. Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                  " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                  "(" + usuarioConsultante.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + funcionarios.getTotalElements() + " funcionarios filtrados del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    // Método para buscar plantillas de formulario por valor (corregir el error del controlador)
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaFormularioDTO>> buscarPlantillasFormularioPorValor(HttpServletRequest httpServletRequest, String valor, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaFormularioDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioConsultante = df2.getData().getUsuarioSistema();
            Empresa empresa = df2.getData().getEmpresa();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            
            // Aquí iría la lógica de búsqueda por valor en plantillas de formulario
            // Por ahora uso todas las plantillas como ejemplo
            List<PlantillaFormulario> plantillasFormulario = this.plantillaFormularioRepository.findByEmpresaIdEmpresaAndRemovido(empresa.getIdEmpresa(), Boolean.FALSE);
            
            // Filtrar por el valor de búsqueda
            List<PlantillaFormulario> plantillasFiltradas = plantillasFormulario.stream()
                .filter(p -> p.getNemonico().toLowerCase().contains(valor.toLowerCase()) || 
                           p.getDescripcion().toLowerCase().contains(valor.toLowerCase()) ||
                           p.getRazon().toLowerCase().contains(valor.toLowerCase()))
                .toList();

            List<PlantillaFormularioDTO> plantillaFormularioDTOList = new ArrayList<>();

            for (PlantillaFormulario plantillaFormulario : plantillasFiltradas) {
                PlantillaFormularioDTO plantillaFormularioDTO = new PlantillaFormularioDTO();
                plantillaFormularioDTO.setTokenIdentificador(plantillaFormulario.getTokenIdentificador());
                plantillaFormularioDTO.setDescripcion(plantillaFormulario.getDescripcion());
                plantillaFormularioDTO.setNemonico(plantillaFormulario.getNemonico());
                plantillaFormularioDTO.setRazon(plantillaFormulario.getRazon());
                plantillaFormularioDTO.setFechaCreacion(plantillaFormulario.getFechaCreacion());
                if (plantillaFormulario.getEmpresa() != null) {
                    plantillaFormularioDTO.setTokenIdentificadorEmpresa(plantillaFormulario.getEmpresa().getTokenIdentificador());
                }
                plantillaFormularioDTOList.add(plantillaFormularioDTO);
            }

            PaginacionResponse<PlantillaFormularioDTO> paginacionResponse = paginacionService.obtenerDatos(plantillaFormularioDTOList, paginacionRequest);

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Se encontraron " + plantillasFiltradas.size() + " plantillas que coinciden con '" + valor + "'. Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                  " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                  "(" + usuarioConsultante.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + plantillasFiltradas.size() + " plantillas de formulario filtradas del sistema";

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