package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.JerarquiasPorNemonicosPadreRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FuncionarioRepository;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
@AllArgsConstructor
public class JerarquiaServiceImpl implements JerarquiaService {
    
    private JwtProviderService jwtProviderService;
    private JerarquiaRepository jerarquiaRepository;
    private FuncionarioRepository funcionarioRepository;
    private EmpresaRepository empresaRepository;
    private CatalogoRepository catalogoRepository;
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquias(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        
        try {
            var listaJerarquias = jerarquiaRepository.findByNoMostrarEnFrontAndRemovido(Boolean.FALSE, Boolean.FALSE);

            List<JerarquiaDTO> jerarquiaDTOList = new ArrayList<>();
            for (Jerarquia jerarquia : listaJerarquias) {
                JerarquiaDTO jerarquiaDTO = new JerarquiaDTO();
                jerarquiaDTO.setId(jerarquia.getIdJerarquia());
                if(jerarquia.getJerarquiaPadre()!=null){
                    jerarquiaDTO.setIdJerarquiaPadre(jerarquia.getJerarquiaPadre().getIdJerarquia());
                } 
                if(jerarquia.getGenero()!=null){
                    jerarquiaDTO.setTokenIdentificadorGenero(jerarquia.getGenero().getTokenIdentificador());
                }
                jerarquiaDTO.setDireccion(jerarquia.getDireccion());
                jerarquiaDTO.setNombre(jerarquia.getNombre());
                jerarquiaDTO.setEmpresa(jerarquia.getEmpresa().getIdEmpresa());
                jerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());

                jerarquiaDTOList.add(jerarquiaDTO);
            }

            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvieron con éxito " + jerarquiaDTOList.size() + " jerarquías";
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + jerarquiaDTOList.size() + " jerarquías del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTOList, mensajeAuditoria);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorNemonicoPadre(HttpServletRequest httpServletRequest,
                                                                                                BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            JerarquiaDTO jerarquiaDTO = new Gson().fromJson(body, JerarquiaDTO.class);
            
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            
            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            
            Empresa empresa = df2.getData().getEmpresa();
            
            List<Jerarquia> jerarquiaList = jerarquiaRepository.findByJerarquiaPadreNemonicoAndEmpresaTokenIdentificadorAndRemovidoOrderByIdJerarquiaDesc(
                    jerarquiaDTO.getNemonico(), empresa.getTokenIdentificador(), Boolean.FALSE
            );

            List<JerarquiaDTO> jerarquiaDTOList = new ArrayList<>();
            
            for (Jerarquia jerarquia : jerarquiaList) {
                JerarquiaDTO nuevaJerarquiaDTO = new JerarquiaDTO();
                nuevaJerarquiaDTO.setId(jerarquia.getIdJerarquia());
                if(jerarquia.getJerarquiaPadre()!=null){
                    nuevaJerarquiaDTO.setIdJerarquiaPadre(jerarquia.getJerarquiaPadre().getIdJerarquia());
                }            
                nuevaJerarquiaDTO.setNombre(jerarquia.getNombre());
                nuevaJerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
                nuevaJerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
                nuevaJerarquiaDTO.setEmpresa(jerarquia.getEmpresa().getIdEmpresa());

                jerarquiaDTOList.add(nuevaJerarquiaDTO);
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

    @Override
    public RespuestaPorDefectoAuditoria<Map<String, List<JerarquiaDTO>>> obtenerJerarquiasPorNemonicoPadreLista(HttpServletRequest httpServletRequest,
                                                                                                                  BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Map<String, List<JerarquiaDTO>>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            JerarquiasPorNemonicosPadreRequest request = new Gson().fromJson(body, JerarquiasPorNemonicosPadreRequest.class);
            List<String> nemonicosPadre = normalizarNemonicosPadre(request != null ? request.getNemonicosPadre() : null);

            Map<String, List<JerarquiaDTO>> jerarquiasPorNemonico = new LinkedHashMap<>();
            for (String nemonico : nemonicosPadre) {
                jerarquiasPorNemonico.put(nemonico, new ArrayList<>());
            }

            if (nemonicosPadre.isEmpty()) {
                df.llenarRespuestaExitosa("No se enviaron nemónicos padre válidos", jerarquiasPorNemonico,
                        "Consulta de jerarquías por lista de nemónicos sin valores válidos");
                return df;
            }

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            List<Jerarquia> jerarquiaList = jerarquiaRepository.findByJerarquiaPadreNemonicoInAndEmpresaTokenIdentificadorAndRemovidoOrderByIdJerarquiaDesc(
                    nemonicosPadre, empresa.getTokenIdentificador(), Boolean.FALSE
            );

            for (Jerarquia jerarquia : jerarquiaList) {
                if (jerarquia.getJerarquiaPadre() == null) {
                    continue;
                }
                String nemonicoPadre = normalizarNemonico(jerarquia.getJerarquiaPadre().getNemonico());
                if (nemonicoPadre.isEmpty()) {
                    continue;
                }

                List<JerarquiaDTO> jerarquias = jerarquiasPorNemonico.computeIfAbsent(nemonicoPadre, k -> new ArrayList<>());

                JerarquiaDTO nuevaJerarquiaDTO = new JerarquiaDTO();
                nuevaJerarquiaDTO.setId(jerarquia.getIdJerarquia());
                if (jerarquia.getJerarquiaPadre() != null) {
                    nuevaJerarquiaDTO.setIdJerarquiaPadre(jerarquia.getJerarquiaPadre().getIdJerarquia());
                }
                nuevaJerarquiaDTO.setNombre(jerarquia.getNombre());
                nuevaJerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
                nuevaJerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
                nuevaJerarquiaDTO.setEmpresa(jerarquia.getEmpresa().getIdEmpresa());
                nuevaJerarquiaDTO.setDireccion(jerarquia.getDireccion());

                jerarquias.add(nuevaJerarquiaDTO);
            }

            int totalJerarquias = jerarquiasPorNemonico.values().stream().mapToInt(List::size).sum();

            String mensajeUsuario = "Se obtuvieron con éxito " + totalJerarquias + " jerarquías para " + nemonicosPadre.size() + " nemónicos padre";
            String mensajeAuditoria = "Se consultaron jerarquías por lista de nemónicos padre. Nemónicos: " + nemonicosPadre.size()
                    + ", jerarquías encontradas: " + totalJerarquias;

            df.llenarRespuestaExitosa(mensajeUsuario, jerarquiasPorNemonico, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorNemonicoPadreCompleto(HttpServletRequest httpServletRequest,
                                                                                                       BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            JerarquiaDTO jerarquiaDTO = new Gson().fromJson(body, JerarquiaDTO.class);
            
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            
            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            
            Empresa empresa = df2.getData().getEmpresa();
            
            List<Jerarquia> jerarquiaList = jerarquiaRepository.findByJerarquiaPadreNemonicoAndEmpresaTokenIdentificadorAndRemovidoOrderByIdJerarquiaDesc(
                    jerarquiaDTO.getNemonico(), empresa.getTokenIdentificador(), Boolean.FALSE
            );

            List<JerarquiaDTO> jerarquiaDTOList = new ArrayList<>();
            
            for (Jerarquia jerarquia : jerarquiaList) {
                JerarquiaDTO nuevaJerarquiaDTO = construirJerarquiaConHijos(jerarquia, empresa);
                jerarquiaDTOList.add(nuevaJerarquiaDTO);
            }

            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvieron con éxito " + jerarquiaDTOList.size() + " jerarquías por nemónico padre con estructura completa";
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + jerarquiaDTOList.size() + " jerarquías del sistema con hijos anidados";

            df.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTOList, mensajeAuditoria);

            } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    /**
     * Método privado auxiliar que construye un JerarquiaDTO con todos sus hijos anidados recursivamente
     * @param jerarquia Entidad Jerarquia
     * @param empresa Empresa del usuario
     * @return JerarquiaDTO con estructura jerárquica completa
     */
    private JerarquiaDTO construirJerarquiaConHijos(Jerarquia jerarquia, Empresa empresa) {
        JerarquiaDTO jerarquiaDTO = new JerarquiaDTO();
        jerarquiaDTO.setId(jerarquia.getIdJerarquia());
        if(jerarquia.getJerarquiaPadre()!=null){
            jerarquiaDTO.setIdJerarquiaPadre(jerarquia.getJerarquiaPadre().getIdJerarquia());
        }            
        jerarquiaDTO.setNombre(jerarquia.getNombre());
        jerarquiaDTO.setNemonico(jerarquia.getNemonico());
        jerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
        jerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
        jerarquiaDTO.setDireccion(jerarquia.getDireccion());
        jerarquiaDTO.setEmpresa(jerarquia.getEmpresa().getIdEmpresa());
        if(jerarquia.getGenero()!=null){
            jerarquiaDTO.setTokenIdentificadorGenero(jerarquia.getGenero().getTokenIdentificador());
        }
        jerarquiaDTO.setEsOficinaCentral(jerarquia.getEsOficinaCentral());
        
        // Buscar todos los hijos de esta jerarquía
        List<Jerarquia> hijos = jerarquiaRepository.findByJerarquiaPadreIdJerarquiaAndEmpresaIdEmpresaAndRemovido(
                jerarquia.getIdJerarquia(), empresa.getIdEmpresa(), Boolean.FALSE
        );
        
        // Construir de forma recursiva cada hijo
        List<JerarquiaDTO> hijosDTO = new ArrayList<>();
        for (Jerarquia hijo : hijos) {
            JerarquiaDTO hijoDTO = construirJerarquiaConHijos(hijo, empresa);
            hijosDTO.add(hijoDTO);
        }
        
        jerarquiaDTO.setHijos(hijosDTO);
        
        return jerarquiaDTO;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<JerarquiaDTO> obtenerJerarquiaPorNumeroDeDocumento(HttpServletRequest httpServletRequest) {

        RespuestaPorDefectoAuditoria<JerarquiaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if(!df2.isExito()){
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovidoAndBloqueado(
                    usuarioSistema.getNumeroDeDocumento(),
                    false,
                    false
            );

            Jerarquia jerarquiaSeleccionada = bodyJwtValido.getJerarquia();
            
            Jerarquia jerarquia = jerarquiaSeleccionada;
            Jerarquia jerarquiaPadre = jerarquia.getJerarquiaPadre();
            JerarquiaDTO jerarquiaPadreDTO = new JerarquiaDTO();
            
            if (jerarquiaPadre != null) {
                jerarquiaPadreDTO.setNemonico(jerarquiaPadre.getNemonico());
            }
            
            JerarquiaDTO jerarquiaDTO = new JerarquiaDTO();
            jerarquiaDTO.setNombre(jerarquia.getNombre());
            jerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
            jerarquiaDTO.setNemonico(jerarquia.getNemonico());
            jerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
            jerarquiaDTO.setJerarquiaPadre(jerarquiaPadreDTO);
            jerarquiaDTO.setGenero(catalogoToDTO(jerarquia.getGenero()));
            jerarquiaDTO.setEsOficinaCentral(jerarquia.getEsOficinaCentral());
            jerarquiaDTO.setDireccion(jerarquia.getDireccion());

            String nombreJerarquia = jerarquia.getNombre() != null ? jerarquia.getNombre() : "Jerarquía sin nombre";
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvo con éxito la jerarquía: " + nombreJerarquia;
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se ha encontrado la jerarquía del funcionario: " + nombreJerarquia;
            
            df.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTO, mensajeAuditoria);

        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<JerarquiaDTO> crearJerarquia(HttpServletRequest httpServletRequest, JerarquiaDTO jerarquiaDTO) {
        RespuestaPorDefectoAuditoria<JerarquiaDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        
        try {
            // Obtener información del usuario para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            String responsable = "Usuario no identificado";
            if (jwtResponse.isExito() && jwtResponse.getData() != null && jwtResponse.getData().getUsuarioSistema() != null) {
                responsable = obtenerNombreCompletoUsuarioSistema(jwtResponse.getData().getUsuarioSistema());
            }
            
            Jerarquia jerarquia = new Jerarquia();
            if(jerarquiaDTO.getIdJerarquiaPadre() != 0) {
                Jerarquia jerarquiaPadre = jerarquiaRepository.findJerarquiaByIdJerarquia(jerarquiaDTO.getIdJerarquiaPadre());
                jerarquia.setJerarquiaPadre(jerarquiaPadre);

                if ("UAPISE".equalsIgnoreCase(jerarquiaPadre.getNemonico())) {
                    jerarquia.setEsOficinaCentral(true);
                }
            }                 
            jerarquia.setNombre(jerarquiaDTO.getNombre());        
            if(jerarquiaDTO.getEmpresa()!= null) {
                Empresa empresa = empresaRepository.findByIdEmpresaAndRemovido(jerarquiaDTO.getEmpresa(), Boolean.FALSE);
                jerarquia.setEmpresa(empresa);
            }
            jerarquia.setDireccion(jerarquiaDTO.getDireccion());
            jerarquia.setUbigeo(jerarquiaDTO.getDireccion());
            if(jerarquiaDTO.getTokenIdentificadorGenero()!=null){
                Catalogo catalogo = catalogoRepository.findByTokenIdentificadorAndRemovido(jerarquiaDTO.getTokenIdentificadorGenero(), Boolean.FALSE);
                jerarquia.setGenero(catalogo);
            }
            jerarquia = jerarquiaRepository.save(jerarquia);

            String nombreJerarquia = jerarquiaDTO.getNombre() != null ? jerarquiaDTO.getNombre() : "Jerarquía sin nombre";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se creó con éxito la jerarquía: " + nombreJerarquia;
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se creó con éxito la jerarquía " + nombreJerarquia + 
                                    " del " + fechaFormateada + " por el usuario " + responsable;
            
            respuesta.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTO, mensajeAuditoria);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<JerarquiaDTO> actualizarJerarquia(HttpServletRequest httpServletRequest, JerarquiaDTO jerarquiaDTO) {
        RespuestaPorDefectoAuditoria<JerarquiaDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        
        try {
            // Obtener información del usuario para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            String responsable = "Usuario no identificado";
            if (jwtResponse.isExito() && jwtResponse.getData() != null && jwtResponse.getData().getUsuarioSistema() != null) {
                responsable = obtenerNombreCompletoUsuarioSistema(jwtResponse.getData().getUsuarioSistema());
            }
            
            Jerarquia jerarquia = jerarquiaRepository.findJerarquiaByIdJerarquia(jerarquiaDTO.getId());
            if(jerarquiaDTO.getIdJerarquiaPadre() != 0) {
                Jerarquia jerarquiaPadre = jerarquiaRepository.findJerarquiaByIdJerarquia(jerarquiaDTO.getIdJerarquiaPadre());
                jerarquia.setJerarquiaPadre(jerarquiaPadre);
            }                 
            jerarquia.setNombre(jerarquiaDTO.getNombre());
            if(jerarquiaDTO.getEmpresa()!= null) {
                Empresa empresa = empresaRepository.findByIdEmpresaAndRemovido(jerarquiaDTO.getEmpresa(), Boolean.FALSE);
                jerarquia.setEmpresa(empresa);
            } 
            jerarquia.setDireccion(jerarquiaDTO.getDireccion());
            if(jerarquiaDTO.getTokenIdentificadorGenero()!=null){
                Catalogo catalogo = catalogoRepository.findByTokenIdentificadorAndRemovido(jerarquiaDTO.getTokenIdentificadorGenero(), Boolean.FALSE);
                jerarquia.setGenero(catalogo);
            }
            jerarquia = jerarquiaRepository.save(jerarquia);
            
            String nombreJerarquia = jerarquiaDTO.getNombre() != null ? jerarquiaDTO.getNombre() : "Jerarquía sin nombre";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se actualizó con éxito la jerarquía: " + nombreJerarquia;
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se editó con éxito la jerarquía " + nombreJerarquia + 
                                    " del " + fechaFormateada + " por el usuario " + responsable;
            
            respuesta.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTO, mensajeAuditoria);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        
        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<JerarquiaDTO> removerJerarquia(HttpServletRequest httpServletRequest, JerarquiaDTO jerarquiaDTO) {
        RespuestaPorDefectoAuditoria<JerarquiaDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        
        try {
            // Obtener información del usuario para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            String responsable = "Usuario no identificado";
            if (jwtResponse.isExito() && jwtResponse.getData() != null && jwtResponse.getData().getUsuarioSistema() != null) {
                responsable = obtenerNombreCompletoUsuarioSistema(jwtResponse.getData().getUsuarioSistema());
            }
            
            Jerarquia jerarquia = jerarquiaRepository.findJerarquiaByIdJerarquia(jerarquiaDTO.getId());
            String nombreJerarquia = jerarquia.getNombre() != null ? jerarquia.getNombre() : "Jerarquía sin nombre";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            
            jerarquia.setRemovido(true);
            jerarquiaRepository.save(jerarquia);
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la jerarquía: " + nombreJerarquia;
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito la jerarquía " + nombreJerarquia + 
                                    " del " + fechaFormateada + " por el usuario " + responsable;
            
            respuesta.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTO, mensajeAuditoria);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        
        return respuesta;
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

    @Override
    public RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorJerarquiaPadreFuncionario(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            Empresa empresa = bodyJwtValido.getEmpresa();
            Jerarquia jerarquiaActual = bodyJwtValido.getJerarquia();

            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovidoAndBloqueado(
                    usuarioSistema.getNumeroDeDocumento(),
                    false,
                    false
            );
            if (funcionario == null) {
                df.setMensaje("El usuario no tiene un funcionario asociado o el funcionario está bloqueado/removido");
                return df;
            }

//            Jerarquia jerarquia = funcionario.getDepartamento();

            if (jerarquiaActual == null) {
                df.setMensaje("El funcionario no tiene un departamento asignado.");
                return df;
            }
            Jerarquia jerarquiaPadre = jerarquiaActual.getJerarquiaPadre();

            String nemonicoPadre = jerarquiaPadre.getNemonico();
            if (nemonicoPadre == null || nemonicoPadre.isEmpty()) {
                df.setMensaje("La jerarquía padre no tiene nemónico. No se puede filtrar.");
                return df;
            }

            List<Jerarquia> jerarquiaList = new ArrayList<>();

            if(nemonicoPadre.equals("UAPISE")){

                List<Jerarquia> second = this.jerarquiaRepository
                        .findByJerarquiaPadreNemonicoInAndEmpresaTokenIdentificadorAndRemovidoOrderByIdJerarquiaDesc(
                                List.of("CJDR", "SOA"), empresa.getTokenIdentificador(), false
                        );
                Map<Long, Jerarquia> map = new LinkedHashMap<>();
                second.forEach(j -> map.putIfAbsent(j.getIdJerarquia(), j)); // evita sobrescribir

                jerarquiaList = new ArrayList<>(map.values());

            }else{
                jerarquiaList = this.jerarquiaRepository
                        .findByJerarquiaPadreNemonicoAndEmpresaTokenIdentificadorAndRemovidoOrderByIdJerarquiaDesc(
                                nemonicoPadre,
                                empresa.getTokenIdentificador(),
                                false
                        );
            }



            List<JerarquiaDTO> jerarquiaDTOList = new ArrayList<>();
            for (Jerarquia j : jerarquiaList) {
                JerarquiaDTO nuevaJerarquiaDTO = new JerarquiaDTO();
                nuevaJerarquiaDTO.setId(j.getIdJerarquia());
                if (j.getJerarquiaPadre() != null) {
                    nuevaJerarquiaDTO.setIdJerarquiaPadre(j.getJerarquiaPadre().getIdJerarquia());
                }
                nuevaJerarquiaDTO.setTokenIdentificador(j.getTokenIdentificador());
                nuevaJerarquiaDTO.setNombre(j.getNombre());
                nuevaJerarquiaDTO.setNemonico(j.getNemonico());
                nuevaJerarquiaDTO.setUbigeo(j.getUbigeo());
                if (j.getEmpresa() != null) {
                    nuevaJerarquiaDTO.setEmpresa(j.getEmpresa().getIdEmpresa());
                }
                nuevaJerarquiaDTO.setDireccion(j.getDireccion());
                if (!ObjectUtils.isEmpty(j.getGenero())) {
                    CatalogoDTO generoDTO = catalogoToDTO(j.getGenero());
                    nuevaJerarquiaDTO.setGenero(generoDTO);
                    nuevaJerarquiaDTO.setTokenIdentificadorGenero(generoDTO.getTokenIdentificador());
                }

                jerarquiaDTOList.add(nuevaJerarquiaDTO);
            }

            jerarquiaDTOList.sort(Comparator.comparing(JerarquiaDTO::getNombre));

            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvieron con éxito " + jerarquiaDTOList.size() + " jerarquías del funcionario";
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + jerarquiaDTOList.size() + " jerarquías del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> obtenerJerarquiasPorTokenPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<JerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            JerarquiaDTO jerarquiaDTO = new Gson().fromJson(body, JerarquiaDTO.class);

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            List<Jerarquia> jerarquiaList = jerarquiaRepository.findByJerarquiaPadreTokenIdentificadorAndRemovido(
                    jerarquiaDTO.getTokenIdentificador(),  Boolean.FALSE
            );

            List<JerarquiaDTO> jerarquiaDTOList = new ArrayList<>();

            for (Jerarquia jerarquia : jerarquiaList) {
                JerarquiaDTO nuevaJerarquiaDTO = new JerarquiaDTO();
                nuevaJerarquiaDTO.setId(jerarquia.getIdJerarquia());
                if(jerarquia.getJerarquiaPadre()!=null){
                    nuevaJerarquiaDTO.setIdJerarquiaPadre(jerarquia.getJerarquiaPadre().getIdJerarquia());
                }
                nuevaJerarquiaDTO.setNombre(jerarquia.getNombre());
                nuevaJerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
                nuevaJerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
                nuevaJerarquiaDTO.setEmpresa(jerarquia.getEmpresa().getIdEmpresa());

                jerarquiaDTOList.add(nuevaJerarquiaDTO);
            }

            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvieron con éxito " + jerarquiaDTOList.size() + " jerarquías por token padre";
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + jerarquiaDTOList.size() + " jerarquías del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, jerarquiaDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<FichaCentroEstadisticaDTO>> obtenerEstadisticasFichasPorCentro(HttpServletRequest httpServletRequest,
                                                                                                            BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<FichaCentroEstadisticaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            Empresa empresa = df2.getData().getEmpresa();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ReportesDTO reportesDTO = new Gson().fromJson(body, ReportesDTO.class);

            List<Object[]> resultados = null;

            resultados = jerarquiaRepository.countFichasPorCentro(empresa.getIdEmpresa(),reportesDTO.getNemonicoTipoSexo(),
                    reportesDTO.getTokenIdentificadorCentro(), reportesDTO.getNemonicoCentro());

            List<FichaCentroEstadisticaDTO> estadisticas = resultados.stream()
                    .map(obj -> new FichaCentroEstadisticaDTO(
                            (String) obj[0],
                            ((Number) obj[1]).intValue()
                    ))
                    .collect(Collectors.toList());

            // Mensaje para el usuario
            String mensajeUsuario = "Se generaron con éxito las estadísticas de " + estadisticas.size() + " centros";
            
            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + estadisticas.size() + " estadísticas de centros del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, estadisticas, mensajeAuditoria);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
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

        String resultado = nombreCompleto.length() > 0 ? nombreCompleto.toString() : "Usuario sin nombre";
        
        // Agregar DNI si está disponible
        if (usuario.getNumeroDeDocumento() != null && !usuario.getNumeroDeDocumento().trim().isEmpty()) {
            resultado += " (" + usuario.getNumeroDeDocumento() + ")";
        }

        return resultado;
    }

    private List<String> normalizarNemonicosPadre(List<String> nemonicosEntrada) {
        if (nemonicosEntrada == null || nemonicosEntrada.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> nemonicosNormalizados = new LinkedHashSet<>();
        for (String nemonico : nemonicosEntrada) {
            String normalizado = normalizarNemonico(nemonico);
            if (!normalizado.isEmpty()) {
                nemonicosNormalizados.add(normalizado);
            }
        }

        return new ArrayList<>(nemonicosNormalizados);
    }

    private String normalizarNemonico(String nemonico) {
        if (nemonico == null) {
            return "";
        }
        return nemonico.trim().toUpperCase(Locale.ROOT);
    }
}
