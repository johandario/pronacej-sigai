package net.latinus.sistema.integral.gestion.seguridad.service.ubicacion;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.ubicacion.UbicacionJerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion.UbicacionJerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ubicacion.UbicacionJerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio UbicacionJerarquiaService
 * Contiene la lógica empresarial para operaciones de ubicaciones jerárquicas
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UbicacionJerarquiaServiceImpl implements UbicacionJerarquiaService {

    private final JwtProviderService jwtProviderService;
    private final UbicacionJerarquiaRepository ubicacionJerarquiaRepository;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final JerarquiaRepository jerarquiaRepository;
    private final CatalogoRepository catalogoRepository;

    private static final String ORDENAR_POR_NOMBRE = "nombre";

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<UbicacionJerarquiaDTO>> obtenerListaPaginada(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<UbicacionJerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> dfDesencriptado = bodyEncriptado.desencriptarPorEmpresa(
                    this.parametroDelSistemaRepository, null);
            if (!dfDesencriptado.isExito()) {
                df.setMensaje(dfDesencriptado.getMensaje());
                return df;
            }

            String body = dfDesencriptado.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = dfJwt.getData().getEmpresa();
            int pagina = paginacionRequest.getPage() != null ? paginacionRequest.getPage() : 0;
            int tamanio = paginacionRequest.getSize() != null ? paginacionRequest.getSize() : 20;

            Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by(ORDENAR_POR_NOMBRE).ascending());
            Page<UbicacionJerarquia> page = this.ubicacionJerarquiaRepository.findByEmpresaAndRemovido(empresa, false, pageable);

            List<UbicacionJerarquiaDTO> ubicacionesDtoList = page.getContent().stream()
                    .map(this::ubicacionToDTO)
                    .collect(Collectors.toList());

            PaginacionResponse<UbicacionJerarquiaDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(ubicacionesDtoList);
            paginacionResponse.setTotalItems(page.getTotalElements());

            df.llenarRespuestaExitosa("Se han obtenido " + ubicacionesDtoList.size() + " ubicaciones jerárquicas", paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> obtenerListaCompleta(
            HttpServletRequest httpServletRequest) {

        RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = dfJwt.getData().getEmpresa();
            List<UbicacionJerarquia> ubicaciones = this.ubicacionJerarquiaRepository.findByEmpresaAndRemovido(empresa, false);

            List<UbicacionJerarquiaDTO> ubicacionesDtoList = ubicaciones.stream()
                    .map(this::ubicacionToDTO)
                    .collect(Collectors.toList());

            String mensaje = "Se han obtenido un total de: " + ubicacionesDtoList.size() + " ubicaciones jerárquicas";
            df.llenarRespuestaExitosa(mensaje, ubicacionesDtoList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> obtenerPorTokenIdentificador(
            HttpServletRequest httpServletRequest, String tokenIdentificador) {

        RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UbicacionJerarquia ubicacion = this.ubicacionJerarquiaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);

            if (ubicacion == null) {
                df.setMensaje("No se encontró la ubicación jerárquica con el token especificado");
                return df;
            }

            UbicacionJerarquiaDTO ubicacionDTO = this.ubicacionToDTO(ubicacion);

            /*if (ubicacion.getJerarquiaTipo() != null) {
                JerarquiaDTO jerarquiaTipoDTO = new JerarquiaDTO();
                jerarquiaTipoDTO.setTokenIdentificador(ubicacion.getJerarquiaTipo().getTokenIdentificador());
                jerarquiaTipoDTO.setNombre(ubicacion.getJerarquiaTipo().getNombre());
                jerarquiaTipoDTO.setNemonico(ubicacion.getJerarquiaTipo().getNemonico());
                ubicacionDTO.setJerarquiaTipo(jerarquiaTipoDTO);
            }

            if (ubicacion.getJerarquiaCentro() != null) {
                JerarquiaDTO jerarquiaCentroDTO = new JerarquiaDTO();
                jerarquiaCentroDTO.setTokenIdentificador(ubicacion.getJerarquiaCentro().getTokenIdentificador());
                jerarquiaCentroDTO.setNombre(ubicacion.getJerarquiaCentro().getNombre());
                jerarquiaCentroDTO.setNemonico(ubicacion.getJerarquiaCentro().getNemonico());
                ubicacionDTO.setJerarquiaCentro(jerarquiaCentroDTO);
            }

            if (ubicacion.getUbicacionJerarquiaPadre() != null) {
                UbicacionJerarquiaDTO padreDTO = new UbicacionJerarquiaDTO();
                padreDTO.setTokenIdentificador(ubicacion.getUbicacionJerarquiaPadre().getTokenIdentificador());
                padreDTO.setNombre(ubicacion.getUbicacionJerarquiaPadre().getNombre());
                ubicacionDTO.setUbicacionJerarquiaPadre(padreDTO);
            }*/

            df.llenarRespuestaExitosa("Se obtuvo con éxito la ubicación jerárquica: " + ubicacion.getNombre(), ubicacionDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> obtenerHijosPorTokenIdentificadorPadre(
            HttpServletRequest httpServletRequest, String tokenIdentificadorPadre) {

        RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            List<UbicacionJerarquia> hijos = this.ubicacionJerarquiaRepository
                    .findByUbicacionJerarquiaPadre_TokenIdentificadorAndRemovido(tokenIdentificadorPadre, false);

            List<UbicacionJerarquiaDTO> hijosDtoList = hijos.stream()
                    .map(this::ubicacionToDTO)
                    .collect(Collectors.toList());

            String mensaje = "Se han obtenido " + hijosDtoList.size() + " ubicaciones hijas";
            df.llenarRespuestaExitosa(mensaje, hijosDtoList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> obtenerPorTokenIdentificadorJerarquiaCentro(
            HttpServletRequest httpServletRequest, String tokenIdentificadorCentro) {

        RespuestaPorDefectoAuditoria<List<UbicacionJerarquiaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = dfJwt.getData().getEmpresa();

            List<UbicacionJerarquia> ubicaciones = this.ubicacionJerarquiaRepository
                    .findByJerarquiaCentro_TokenIdentificadorAndEmpresaAndRemovido(tokenIdentificadorCentro, empresa, false);

            List<UbicacionJerarquiaDTO> ubicacionesDtoList = ubicaciones.stream()
                    .map(this::ubicacionToDTO)
                    .collect(Collectors.toList());

            String mensaje = "Se han obtenido " + ubicacionesDtoList.size()
                    + " ubicaciones por jerarquía centro";
            df.llenarRespuestaExitosa(mensaje, ubicacionesDtoList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> crearEditar(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> dfDesencriptado = bodyEncriptado.desencriptarPorEmpresa(
                    this.parametroDelSistemaRepository, null);
            if (!dfDesencriptado.isExito()) {
                df.setMensaje(dfDesencriptado.getMensaje());
                return df;
            }

            String body = dfDesencriptado.getData();
            UbicacionJerarquiaDTO ubicacionDTO = new Gson().fromJson(body, UbicacionJerarquiaDTO.class);

            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = dfJwt.getData().getEmpresa();
            String ip = httpServletRequest.getRemoteAddr();

            UbicacionJerarquia ubicacion;

            // Validar si es creación o edición
            if (ubicacionDTO.getTokenIdentificador() != null && !ubicacionDTO.getTokenIdentificador().isEmpty()) {
                // Edición
                ubicacion = this.ubicacionJerarquiaRepository.findByTokenIdentificadorAndRemovido(
                        ubicacionDTO.getTokenIdentificador(), false);
                if (ubicacion == null) {
                    df.setMensaje("No se encontró la ubicación jerárquica a editar");
                    return df;
                }
            } else {
                // Creación
                ubicacion = new UbicacionJerarquia();
                ubicacion.setIpCrea(ip);
            }

            // Asociar Jerarquía Centro
            if (ubicacionDTO.getJerarquiaCentro() != null) {
                Jerarquia jerarquiaCentro = this.jerarquiaRepository.findByTokenIdentificadorAndRemovido(ubicacionDTO.getJerarquiaCentro().getTokenIdentificador(), false);

                if (jerarquiaCentro == null) {
                    df.setMensaje("No se encontró la jerarquía centro especificada");
                    return df;
                }

                ubicacion.setJerarquiaCentro(jerarquiaCentro);
            }

            // Asociar Jerarquia Tipo
            if (ubicacionDTO.getJerarquiaTipo() != null) {
                Jerarquia jerarquiaTipo = this.jerarquiaRepository.findByTokenIdentificadorAndRemovido(ubicacionDTO.getJerarquiaTipo().getTokenIdentificador(), false);

                if (jerarquiaTipo == null) {
                    df.setMensaje("No se encontró la jerarquía tipo especificada");
                    return df;
                }

                ubicacion.setJerarquiaTipo(jerarquiaTipo);
            }

            // Asociar padre en caso de que sea una ubicación hija
            if (ubicacionDTO.getUbicacionJerarquiaPadre() != null) {
                UbicacionJerarquia padre = this.ubicacionJerarquiaRepository.findByTokenIdentificadorAndRemovido(ubicacionDTO.getUbicacionJerarquiaPadre().getTokenIdentificador(), false);

                if (padre == null) {
                    df.setMensaje("No se encontró la jerarquía padre especificada");
                    return df;
                }

                ubicacion.setUbicacionJerarquiaPadre(padre);
            }

            // Asociar catálogo tipo sexo
            if (ubicacionDTO.getTipoSexo() != null) {
                Catalogo tipoSexo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(ubicacionDTO.getTipoSexo().getTokenIdentificador(), false);

                if (tipoSexo == null) {
                    df.setMensaje("No se encontró el catálogo tipo sexo especificado");
                    return df;
                }

                ubicacion.setTipoSexo(tipoSexo);
            }

            // Asociar catálogo atención prioritaria
            if (ubicacionDTO.getAtencionPrioritaria() != null) {
                Catalogo atencionPrioritaria = this.catalogoRepository.findByTokenIdentificadorAndRemovido(ubicacionDTO.getAtencionPrioritaria().getTokenIdentificador(), false);

                if (atencionPrioritaria == null) {
                    df.setMensaje("No se encontró el catálogo atención prioritaria especificado");
                    return df;
                }

                ubicacion.setAtencionPrioritaria(atencionPrioritaria);
            }

            // Asociar catálogo tipo de ubicación
            if (ubicacionDTO.getTipoUbicacion() != null) {
                Catalogo tipoUbicacion = this.catalogoRepository.findByTokenIdentificadorAndRemovido(ubicacionDTO.getTipoUbicacion().getTokenIdentificador(), false);

                if (tipoUbicacion == null) {
                    df.setMensaje("No se encontró el catálogo tipo de ubicación especificado");
                    return df;
                }

                ubicacion.setTipoUbicacion(tipoUbicacion);
            }

            // Asignar valores comunes
            ubicacion.setNombre(ubicacionDTO.getNombre());
            ubicacion.setNombreCorto(ubicacionDTO.getNombreCorto());
            ubicacion.setDescripcion(ubicacionDTO.getDescripcion());
            ubicacion.setRangoInicio(ubicacionDTO.getRangoInicio());
            ubicacion.setRangoFin(ubicacionDTO.getRangoFin());
            ubicacion.setEmpresa(empresa);
            ubicacion.setIpEdita(ip);

            ubicacion = this.ubicacionJerarquiaRepository.save(ubicacion);
            UbicacionJerarquiaDTO resultado = this.ubicacionToDTO(ubicacion);

            String mensaje = ubicacionDTO.getTokenIdentificador() != null ? 
                    "Se ha editado con éxito la ubicación jerárquica: " + ubicacion.getNombre() :
                    "Se ha creado con éxito la ubicación jerárquica: " + ubicacion.getNombre();

            df.llenarRespuestaExitosa(mensaje, resultado);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> eliminar(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<UbicacionJerarquiaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> dfDesencriptado = bodyEncriptado.desencriptarPorEmpresa(
                    this.parametroDelSistemaRepository, null);
            if (!dfDesencriptado.isExito()) {
                df.setMensaje(dfDesencriptado.getMensaje());
                return df;
            }

            String body = dfDesencriptado.getData();
            UbicacionJerarquiaDTO ubicacionDTO = new Gson().fromJson(body, UbicacionJerarquiaDTO.class);

            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            String ip = httpServletRequest.getRemoteAddr();
            UbicacionJerarquia ubicacion = this.ubicacionJerarquiaRepository.findByTokenIdentificadorAndRemovido(
                    ubicacionDTO.getTokenIdentificador(), false);

            if (ubicacion == null) {
                df.setMensaje("No se encontró la ubicación jerárquica a eliminar");
                return df;
            }

            ubicacion.setRemovido(true);
            ubicacion.setIpEdita(ip);
            ubicacion = this.ubicacionJerarquiaRepository.save(ubicacion);

            UbicacionJerarquiaDTO resultado = this.ubicacionToDTO(ubicacion);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito la ubicación jerárquica: " + ubicacion.getNombre(), resultado);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Convierte una entidad UbicacionJerarquia a DTO
     *
     * @param ubicacion entidad UbicacionJerarquia
     * @return UbicacionJerarquiaDTO
     */
    private UbicacionJerarquiaDTO ubicacionToDTO(UbicacionJerarquia ubicacion) {
        UbicacionJerarquiaDTO dto = new UbicacionJerarquiaDTO();
        dto.setTokenIdentificador(ubicacion.getTokenIdentificador());
        dto.setNombre(ubicacion.getNombre());
        dto.setNombreCorto(ubicacion.getNombreCorto());
        dto.setDescripcion(ubicacion.getDescripcion());
        dto.setRangoInicio(ubicacion.getRangoInicio());
        dto.setRangoFin(ubicacion.getRangoFin());

        if (ubicacion.getJerarquiaTipo() != null) {
            dto.setJerarquiaTipo(this.jerarquiaToDTO(ubicacion.getJerarquiaTipo()));
        }

        Jerarquia jerarquiaCentroEfectiva = this.obtenerJerarquiaCentroEfectiva(ubicacion);
        if (jerarquiaCentroEfectiva != null) {
            dto.setJerarquiaCentro(this.jerarquiaToDTO(jerarquiaCentroEfectiva));
        }

        if (ubicacion.getUbicacionJerarquiaPadre() != null) {
            UbicacionJerarquiaDTO padreDTO = new UbicacionJerarquiaDTO();
            padreDTO.setTokenIdentificador(ubicacion.getUbicacionJerarquiaPadre().getTokenIdentificador());
            padreDTO.setNombre(ubicacion.getUbicacionJerarquiaPadre().getNombre());
            dto.setUbicacionJerarquiaPadre(padreDTO);
        }

        if (ubicacion.getTipoSexo() != null) {
            dto.setTipoSexo(this.catalogoToDTO(ubicacion.getTipoSexo()));
        }

        if (ubicacion.getAtencionPrioritaria() != null) {
            dto.setAtencionPrioritaria(this.catalogoToDTO(ubicacion.getAtencionPrioritaria()));
        }

        if (ubicacion.getTipoUbicacion() != null) {
            dto.setTipoUbicacion(this.catalogoToDTO(ubicacion.getTipoUbicacion()));
        }

        return dto;
    }


    private Jerarquia obtenerJerarquiaCentroEfectiva(UbicacionJerarquia ubicacion) {
        Set<String> tokensVisitados = new HashSet<>();
        UbicacionJerarquia actual = ubicacion;

        while (actual != null) {
            String tokenActual = actual.getTokenIdentificador();
            if (tokenActual != null && !tokensVisitados.add(tokenActual)) {
                break;
            }

            if (actual.getJerarquiaCentro() != null) {
                return actual.getJerarquiaCentro();
            }

            actual = actual.getUbicacionJerarquiaPadre();
        }

        return null;
    }

    private JerarquiaDTO jerarquiaToDTO(Jerarquia jerarquia) {
        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setTokenIdentificador(jerarquia.getTokenIdentificador());
        dto.setNemonico(jerarquia.getNemonico());
        dto.setNombre(jerarquia.getNombre());
        return dto;
    }

    private CatalogoDTO catalogoToDTO(Catalogo catalogo) {
        CatalogoDTO dto = new CatalogoDTO();
        dto.setTokenIdentificador(catalogo.getTokenIdentificador());
        dto.setNemonico(catalogo.getNemonico());
        dto.setNombre(catalogo.getNombre());
        return dto;
    }

}






