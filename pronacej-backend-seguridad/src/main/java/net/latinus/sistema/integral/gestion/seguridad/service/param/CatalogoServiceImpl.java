package net.latinus.sistema.integral.gestion.seguridad.service.param;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionConParametrosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Service
@Transactional
@AllArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private EmpresaRepository empresaRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private static final String ORDENAR_POR_NOMBRE = "nombre";
    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<CatalogoDTO>>> crearVariosCatalogosDirecto(
            HttpServletRequest httpServletRequest, List<CatalogoDTO> catalogosDto) {
        RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<CatalogoDTO>>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            String ip = httpServletRequest.getRemoteAddr();

            List<RespuestaPorDefectoAuditoria<CatalogoDTO>> respuestaPorDefectoAuditoriaList = new ArrayList<>();
            for (CatalogoDTO catalogoDTO : catalogosDto) {
                RespuestaPorDefectoAuditoria<CatalogoDTO> respuestaPorDefectoAuditoria = new RespuestaPorDefectoAuditoria<>();
                Catalogo catalogoDb = new Catalogo();
                catalogoDb.setCatalogoPadre(this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificadorPadre(), false));
                catalogoDb.setEmpresa(this.empresaRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificadorEmpresa(), false));
                catalogoDb.setDescripcion(catalogoDTO.getDescripcion());
                catalogoDb.setIpCrea(ip);
                catalogoDb.setNombre(catalogoDTO.getNombre());
                catalogoDb.setCodigoExterno(catalogoDTO.getCodigoExterno());
                catalogoDb.setDescripcion(catalogoDTO.getDescripcion());
                catalogoDb.setNemonico(catalogoDTO.getNemonico());
                catalogoDb = this.catalogoRepository.save(catalogoDb);

                catalogoDTO.setTokenIdentificador(catalogoDb.getTokenIdentificador());

                respuestaPorDefectoAuditoria.llenarRespuestaExitosa("Se ha creado con exito el catalogo: " +
                        catalogoDb.getNombre(), catalogoDTO);

                respuestaPorDefectoAuditoriaList.add(respuestaPorDefectoAuditoria);
            }

            df.llenarRespuestaExitosa("Se han enviado a crear un total de: " +
                    catalogosDto.size() + " catalogo(s)", respuestaPorDefectoAuditoriaList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

@Override
public RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerCatalogoPorNemonicoPadre(HttpServletRequest httpServletRequest,
                                                                                       BodyEncriptado bodyEncriptado) {

    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();

        //catalogo Dto debe de ser el catalogo padre
        CatalogoDTO catalogoDTO = new Gson().fromJson(body, CatalogoDTO.class);

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

        if (Boolean.FALSE.equals(df2.isExito())) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(true);
            return df;
        }

        Empresa empresa = df2.getData().getEmpresa();

        List<Catalogo> catalogoList = this.catalogoRepository.findByCatalogoPadreNemonicoAndEmpresaAndRemovidoOrderByNombre(
                catalogoDTO.getNemonico(), empresa, false

        );

        List<CatalogoDTO> catalogoDTOList = new ArrayList<>();

        for (Catalogo catalogo : catalogoList) {
            CatalogoDTO catalogoDTO1 = this.obtenerCatalogoDTO(catalogo, empresa);
            Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(
                    catalogo, empresa, false
            );
            catalogoDTO1.setTieneHijos(cantidadDeHijos > 0);
            catalogoDTOList.add(catalogoDTO1);
        }

        // Mensaje para el usuario
        String mensajeUsuario = "Se han encontrado un total de: " + catalogoDTOList.size() + " catalogos";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + catalogoDTOList.size() + " catálogos por nemónico padre";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTOList, mensajeAuditoria);

    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

    private CatalogoDTO obtenerCatalogoDTO(Catalogo catalogo, Empresa empresa) {
        if (catalogo == null) {
            return null;
        }

        CatalogoDTO catalogoDTO = new CatalogoDTO();
        catalogoDTO.setIdCatalogo(catalogo.getIdCatalogo());
        catalogoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        catalogoDTO.setNombre(catalogo.getNombre());
        catalogoDTO.setNemonico(catalogo.getNemonico());
        catalogoDTO.setDescripcion(catalogo.getDescripcion());
        catalogoDTO.setTokenIdentificador(catalogo.getTokenIdentificador());
        catalogoDTO.setCodigoExterno(catalogo.getCodigoExterno());

        Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(
                catalogo, empresa, false
        );
        catalogoDTO.setTieneHijos(cantidadDeHijos > 0);

        if (cantidadDeHijos == 0) {
            return catalogoDTO;
        }

        catalogoDTO.setHijos(new ArrayList<>());
        List<Catalogo> hijos = this.catalogoRepository.findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(
                catalogo, empresa, false
        );
        for (Catalogo catalogo1 : hijos) {
            catalogoDTO.getHijos().add(this.obtenerHijo(catalogo1, empresa));
        }

        return catalogoDTO;
    }

    private CatalogoDTO obtenerHijo(Catalogo catalogo, Empresa empresa) {
        if (catalogo == null) {
            return null;
        }

        Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(
                catalogo, empresa, false
        );

        CatalogoDTO catalogoDTO = new CatalogoDTO();
        catalogoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        catalogoDTO.setNombre(catalogo.getNombre());
        catalogoDTO.setNemonico(catalogo.getNemonico());
        catalogoDTO.setDescripcion(catalogo.getDescripcion());
        catalogoDTO.setTokenIdentificador(catalogo.getTokenIdentificador());
        catalogoDTO.setCodigoExterno(catalogo.getCodigoExterno());


        catalogoDTO.setTieneHijos(cantidadDeHijos > 0);

        if (cantidadDeHijos == 0) {
            return catalogoDTO;
        }

        catalogoDTO.setHijos(new ArrayList<>());
        List<Catalogo> hijos = this.catalogoRepository.findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(
                catalogo, empresa, false
        );
        for (Catalogo hijo : hijos) {
            catalogoDTO.setTokenIdentificadorPadre(hijo.getTokenIdentificador());
            catalogoDTO.getHijos().add(this.obtenerHijo(hijo, empresa));
        }

        return catalogoDTO;
    }

@Override
public RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> obtenerCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

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
                Sort.by(ORDENAR_POR_NOMBRE).ascending()
        );

        Page<Catalogo> catalogosPage = this.catalogoRepository.findByCatalogoPadreIsNullAndRemovido(false, pageable);
        PaginacionResponse<CatalogoDTO> paginacionResponse = new PaginacionResponse<>();

        // Convertir la lista de entidades Catalogo a DTOs
        List<CatalogoDTO> catalogoDTOs = catalogosPage.toList().stream()
                // .sorted(Comparator.comparing(Catalogo::getNombre))
                .map(catalogo -> {
                    CatalogoDTO dto = new CatalogoDTO();
                    dto.setTokenIdentificador(catalogo.getTokenIdentificador());
                    dto.setNombre(catalogo.getNombre());
                    dto.setDescripcion(catalogo.getDescripcion());
                    dto.setNemonico(catalogo.getNemonico());
                    dto.setFechaCreacion(catalogo.getFechaCreacion());
                    return dto;
                })
                .toList();

        paginacionResponse.setData(catalogoDTOs);
        paginacionResponse.setTotalItems(catalogosPage.getTotalElements());

        // CORREGIDO: Usar el total de elementos de la paginación en lugar del tamaño de la página actual
        long totalElementos = catalogosPage.getTotalElements(); // Total de catálogos en el sistema

        // Mensaje para el usuario
        String mensajeUsuario = "Catálogos obtenidos con éxito.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + totalElementos + " catálogos";

        df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> obtenerSubCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();
        PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
        String tokenPadre = paginacionRequest.getTokenIdentificador();

        Pageable pageable = PageRequest.of(
                paginacionRequest.getPage(),
                paginacionRequest.getSize(),
                Sort.by(ORDENAR_POR_NOMBRE).ascending()
        );

        Page<Catalogo> catalogosPage = this.catalogoRepository.findByCatalogoPadre_TokenIdentificadorAndRemovido(tokenPadre, false, pageable);
        PaginacionResponse<CatalogoDTO> paginacionResponse = new PaginacionResponse<>();

        // Convertir la lista de entidades Catalogo a DTOs
        List<CatalogoDTO> catalogoDTOs = catalogosPage.stream()
                // .sorted(Comparator.comparing(Catalogo::getNombre))
                .map(catalogo -> {
                    CatalogoDTO dto = new CatalogoDTO();
                    dto.setTokenIdentificador(catalogo.getTokenIdentificador());
                    dto.setNombre(catalogo.getNombre());
                    dto.setDescripcion(catalogo.getDescripcion());
                    dto.setCodigoExterno(catalogo.getCodigoExterno());
                    dto.setNemonico(catalogo.getNemonico());
                    dto.setFechaCreacion(catalogo.getFechaCreacion());
                    dto.setTokenIdentificadorPadre(catalogo.getCatalogoPadre().getTokenIdentificador());
                    return dto;
                })
                .toList();

        paginacionResponse.setData(catalogoDTOs);
        paginacionResponse.setTotalItems(catalogosPage.getTotalElements());

        // CORREGIDO: Usar el total de elementos de la paginación
        long totalElementos = catalogosPage.getTotalElements();

        // Mensaje para el usuario
        String mensajeUsuario = "Sub catálogos obtenidos con éxito.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + totalElementos + " sub catálogos";

        df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerSubCatalogosPorNemonicoPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();

        String nemonicoPadre = new Gson().fromJson(body, String.class);
        List<Catalogo> catalogos = this.catalogoRepository.findByCatalogoPadre_NemonicoAndRemovido(nemonicoPadre, false);
        List<CatalogoDTO> catalogoDTOs = catalogos.stream()
                .map(catalogo -> {
                    CatalogoDTO dto = new CatalogoDTO();
                    dto.setTokenIdentificador(catalogo.getTokenIdentificador());
                    dto.setNombre(catalogo.getNombre());
                    dto.setCodigoExterno(catalogo.getCodigoExterno());
                    dto.setDescripcion(catalogo.getDescripcion());
                    dto.setNemonico(catalogo.getNemonico());
                    dto.setTokenIdentificadorPadre(
                            catalogo.getCatalogoPadre() == null ? null :
                            catalogo.getCatalogoPadre().getTokenIdentificador()
                    );
                    dto.setFechaCreacion(catalogo.getFechaCreacion());
                    return dto;
                })
                .collect(Collectors.toList());

        // Mensaje para el usuario
        String mensajeUsuario = "Catálogos obtenidos con éxito.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + catalogoDTOs.size() + " catálogos por nemónico padre";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTOs, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> buscarCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();
        PaginacionConParametrosRequest paginacionRequest = new Gson().fromJson(body, PaginacionConParametrosRequest.class);
        String nombre = paginacionRequest.getNombre();

        Pageable pageable = PageRequest.of(
                paginacionRequest.getPage(),
                paginacionRequest.getSize(),
                Sort.by(ORDENAR_POR_NOMBRE).ascending()
        );

        Page<Catalogo> catalogosPage = this.catalogoRepository.findByNombreContainingIgnoreCaseAndCatalogoPadreIsNullAndRemovido(nombre, false, pageable);
        PaginacionResponse<CatalogoDTO> paginacionResponse = new PaginacionResponse<>();

        // Convertir la lista de entidades Catalogo a DTOs
        List<CatalogoDTO> catalogoDTOs = catalogosPage.stream()
                // .sorted(Comparator.comparing(Catalogo::getNombre))
                .map(catalogo -> {
                    CatalogoDTO dto = new CatalogoDTO();
                    dto.setTokenIdentificador(catalogo.getTokenIdentificador());
                    dto.setNombre(catalogo.getNombre());
                    dto.setDescripcion(catalogo.getDescripcion());
                    dto.setCodigoExterno(catalogo.getCodigoExterno());
                    dto.setNemonico(catalogo.getNemonico());
                    dto.setFechaCreacion(catalogo.getFechaCreacion());
                    return dto;
                })
                .toList();

        paginacionResponse.setData(catalogoDTOs);
        paginacionResponse.setTotalItems(catalogosPage.getTotalElements());

        // CORREGIDO: Usar el total de elementos de la paginación
        long totalElementos = catalogosPage.getTotalElements();

        // Mensaje para el usuario
        String mensajeUsuario = "Búsqueda realizada con éxito.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + totalElementos + " catálogos filtrados";

        df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> buscarSubCatalogos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();
        PaginacionConParametrosRequest paginacionRequest = new Gson().fromJson(body, PaginacionConParametrosRequest.class);
        String nombre = paginacionRequest.getNombre();
        String token = paginacionRequest.getTokenIdentificador();

        Pageable pageable = PageRequest.of(
                paginacionRequest.getPage(),
                paginacionRequest.getSize(),
                Sort.by(ORDENAR_POR_NOMBRE).ascending()
        );

        Page<Catalogo> catalogosPage = this.catalogoRepository.findByCatalogoPadre_TokenIdentificadorAndNombreContainingIgnoreCaseAndRemovido(token, nombre, false, pageable);
        PaginacionResponse<CatalogoDTO> paginacionResponse = new PaginacionResponse<>();

        // Convertir la lista de entidades Catalogo a DTOs
        List<CatalogoDTO> catalogoDTOs = catalogosPage.stream()
                // .sorted(Comparator.comparing(Catalogo::getNombre))
                .map(catalogo -> {
                    CatalogoDTO dto = new CatalogoDTO();
                    dto.setTokenIdentificador(catalogo.getTokenIdentificador());
                    dto.setNombre(catalogo.getNombre());
                    dto.setDescripcion(catalogo.getDescripcion());
                    dto.setCodigoExterno(catalogo.getCodigoExterno());
                    dto.setNemonico(catalogo.getNemonico());
                    dto.setFechaCreacion(catalogo.getFechaCreacion());
                    dto.setTokenIdentificadorPadre(catalogo.getCatalogoPadre().getTokenIdentificador());
                    return dto;
                })
                .toList();

        paginacionResponse.setData(catalogoDTOs);
        paginacionResponse.setTotalItems(catalogosPage.getTotalElements());

        // CORREGIDO: Usar el total de elementos de la paginación
        long totalElementos = catalogosPage.getTotalElements();

        // Mensaje para el usuario
        String mensajeUsuario = "Búsqueda realizada con éxito.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + totalElementos + " sub catálogos filtrados";

        df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<CatalogoDTO> actualizarCatalogo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<CatalogoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
        df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();
        CatalogoDTO catalogoDTO = new Gson().fromJson(body, CatalogoDTO.class);

        String ip = httpServletRequest.getRemoteAddr();

        Catalogo catalogoDb = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);
        if (catalogoDb == null) {
            df.setMensaje("El catálogo con el token proporcionado no existe.");
            df.setExito(false);
            return df;
        }

        Catalogo catalogoNemonicoRepetido = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                catalogoDTO.getNemonico(),
                empresa.getTokenIdentificador(),
                false
        );

        if (catalogoNemonicoRepetido != null &&
                !catalogoNemonicoRepetido.getIdCatalogo().equals(catalogoDb.getIdCatalogo())) {
            df.setMensaje("Ya existe un catalogo con el nemonico: " + catalogoDTO.getNemonico() + " intenta enviando uno diferente para continuar");
            return df;
        }

        Date fecha = new Date();
        catalogoDb.setUsuarioSistemaEdita(usuarioSistema);
        catalogoDb.setNombre(catalogoDTO.getNombre());
        catalogoDb.setDescripcion(catalogoDTO.getDescripcion());
        catalogoDb.setCodigoExterno(catalogoDTO.getCodigoExterno());
        catalogoDb.setNemonico(catalogoDTO.getNemonico());
        catalogoDb.setIpEdita(ip);
        catalogoDb.setFechaEdicion(fecha);

        this.catalogoRepository.save(catalogoDb);

        catalogoDTO.setTokenIdentificador(catalogoDb.getTokenIdentificador());

        // Obtener datos para el mensaje
        String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
        String fechaFormateada = formatearFechaEspanol(fecha);

        // Mensaje para el usuario
        String mensajeUsuario = "Se ha actualizado con éxito el catálogo: " + catalogoDb.getNombre();

        // Mensaje para auditoría
        String mensajeAuditoria = "Se editó con éxito el catálogo " + catalogoDb.getNombre() + 
                                " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTO, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<CatalogoDTO> eliminarCatalogo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<CatalogoDTO> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(true);
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();
        CatalogoDTO catalogoDTO = new Gson().fromJson(body, CatalogoDTO.class);

        Catalogo catalogoDb = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);
        if (catalogoDb == null) {
            df.setMensaje("El catálogo con el token proporcionado no existe.");
            df.setExito(false);
            return df;
        }

        // Obtener datos para el mensaje antes de marcar como removido
        String nombreCatalogo = catalogoDb.getNombre();
        String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistema);

        // Marcar como removido
        Date fecha = new Date();
        catalogoDb.setRemovido(true);
        catalogoDb.setIpElimina(httpServletRequest.getRemoteAddr());
        catalogoDb.setFechaEliminacion(fecha);
        catalogoDb.setUsuarioSistemaElimina(usuarioSistema);
        this.catalogoRepository.save(catalogoDb);

        String fechaFormateada = formatearFechaEspanol(fecha);

        // Mensaje para el usuario
        String mensajeUsuario = "El catálogo ha sido eliminado con éxito.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se eliminó con éxito el catálogo " + nombreCatalogo + 
                                " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTO, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<CatalogoDTO> crearCatalogo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
    RespuestaPorDefectoAuditoria<CatalogoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            df.setMensaje(df22.getMensaje());
            return df;
        }
        String body = df22.getData();
        CatalogoDTO catalogoDTO = new Gson().fromJson(body, CatalogoDTO.class);

        String ip = httpServletRequest.getRemoteAddr();

        Catalogo catalogoNemonicoRepetido = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                catalogoDTO.getNemonico(),
                empresa.getTokenIdentificador(),
                false
        );

        if (catalogoNemonicoRepetido != null) {
            df.setMensaje("Ya existe un catalogo con el nemonico: " + catalogoDTO.getNemonico() + " intenta enviando uno diferente para continuar");
            return df;
        }

        Date fecha = new Date();
        Catalogo catalogoDb = new Catalogo();

        catalogoDb.setUsuarioSistemaCrea(usuarioSistema);

        catalogoDb.setEmpresa(empresa);
        catalogoDb.setDescripcion(catalogoDTO.getDescripcion());
        catalogoDb.setIpCrea(ip);
        catalogoDb.setNombre(catalogoDTO.getNombre());
        catalogoDb.setCodigoExterno(catalogoDTO.getCodigoExterno());

        catalogoDb.setFechaCreacion(fecha);

        Catalogo catalogoPadre = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificadorPadre(), false);

        // Si padre es nulo no se guarda
        if (catalogoPadre != null) {
            catalogoDb.setCatalogoPadre(catalogoPadre);
        }

        // Si el campo nemonico es nulo o está vacío se genera uno automáticamente
        if (catalogoDTO.getNemonico() == null || catalogoDTO.getNemonico().isBlank()) {
            String prefijo = catalogoPadre != null ? catalogoPadre.getNemonico() : "";
            catalogoDb.setNemonico(FuncionesAyuda.crearNemonico(prefijo, catalogoDTO.getNombre()));
        } else {
            catalogoDb.setNemonico(catalogoDTO.getNemonico());
        }

        catalogoDb = this.catalogoRepository.save(catalogoDb);

        this.catalogoRepository.save(catalogoDb);

        catalogoDTO.setTokenIdentificador(catalogoDb.getTokenIdentificador());

        // Obtener datos para el mensaje
        String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
        String fechaFormateada = formatearFechaEspanol(fecha);

        // Mensaje para el usuario
        String mensajeUsuario = "El catálogo se ha creado con exito";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se creó con éxito el catálogo " + catalogoDb.getNombre() + 
                                " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTO, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<CatalogoDTO> obtenerUnCatalogo(HttpServletRequest httpServletRequest,
                                                                   String nemonico) {

    RespuestaPorDefectoAuditoria<CatalogoDTO> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(true);
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();

        Pageable pageable = PageRequest.of(0, 3, Sort.by("idCatalogo").ascending());
        Page<Catalogo> catalogoPageable = this.catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                nemonico, empresa.getTokenIdentificador(), false, pageable
        );

        if (catalogoPageable.isEmpty()) {
            df.setMensaje("No se encontro el catalogo con nemonico: " + nemonico);
            return df;
        }

        if (catalogoPageable.getTotalElements() > 1) {
            this.logService.warn("Se ha encontrado más de un valor de catalogo con el nemonico: " + nemonico);
        }

        Catalogo catalogo = catalogoPageable.toList().get(0);
        CatalogoDTO catalogoDTO = new CatalogoDTO();
        catalogoDTO.setNombre(catalogo.getNombre());
        catalogoDTO.setDescripcion(catalogo.getDescripcion());
        catalogoDTO.setTokenIdentificador(catalogo.getTokenIdentificador());
        catalogoDTO.setNemonico(catalogo.getNemonico());
        catalogoDTO.setFechaCreacion(catalogo.getFechaCreacion());
        catalogoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        // Mensaje para el usuario
        String mensajeUsuario = "Se ha encontrado con éxito el catalogo con nemonico: " + nemonico;

        // Mensaje para auditoría
        String mensajeAuditoria = "Se obtuvo con éxito el catálogo " + catalogo.getNombre() + " por nemónico";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTO, mensajeAuditoria);

    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

    @Override
    public RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerTotales(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);

        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            return df;
        }

        //List<Catalogo> catalogosPadres = this.catalogoRepository.findByCatalogoPadreAndRemovidoOrderByIdCatalogo(null, false );

        return df;
    }

@Override
public RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerCatalogosPrincipales(HttpServletRequest httpServletRequest) {

    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(df2.getLogOut());
            df.setSinAcceso(df2.getSinAcceso());
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();
        df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        List<Catalogo> catalogoList = this.catalogoRepository.findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(
                null,
                empresa, false
        );

        List<CatalogoDTO> catalogoDTOList = new ArrayList<>();
        for (Catalogo catalogo : catalogoList) {
            CatalogoDTO catalogoDTO = catalogo.convertirADTO();
            Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(catalogo,
                    empresa, false);
            catalogoDTO.setTieneHijos(cantidadDeHijos > 0);
            catalogoDTOList.add(catalogoDTO);
        }

        // Mensaje para el usuario
        String mensajeUsuario = "Se han obtenido con exito un total de: " + catalogoDTOList.size() + " catalogos principales.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + catalogoDTOList.size() + " catálogos principales";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTOList, mensajeAuditoria);

    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> obtenerCatalogosHijos(HttpServletRequest httpServletRequest,
                                                                                           PaginacionRequest paginacionRequest) {
    RespuestaPorDefectoAuditoria<PaginacionResponse<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(df2.getLogOut());
            df.setSinAcceso(df2.getSinAcceso());
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();
        df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        Catalogo catalogoPadre = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                paginacionRequest.getTokenIdentificador(), false
        );

        if (catalogoPadre == null) {
            df.setMensaje("El catalogo solicitado no existe o ya fue eliminado anteriormente");
            return df;
        }

        Empresa empresa1 = catalogoPadre.getEmpresa();
        if (empresa1 == null) {
            df.setMensaje("El catalogo no presenta una empresa");
            return df;
        }

        if (!empresa1.getIdEmpresa().equals(empresa.getIdEmpresa())) {
            df.setMensaje("El catalogo consultado pertenece a otra empresa");
            return df;
        }

        Pageable pageable = PageRequest.of(
                paginacionRequest.getPage(),
                paginacionRequest.getSize()
        );
        Page<Catalogo> catalogoHijosPage = this.catalogoRepository.findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(
                catalogoPadre, empresa1, false,
                pageable
        );

        List<CatalogoDTO> catalogoDTOList = new ArrayList<>();

        for (Catalogo catalogo : catalogoHijosPage.toList()) {
            catalogoDTOList.add(catalogo.convertirADTO());
        }

        PaginacionResponse<CatalogoDTO> paginacionResponse = new PaginacionResponse<>();
        paginacionResponse.setData(catalogoDTOList);
        paginacionResponse.setTotalItems(catalogoHijosPage.getTotalElements());

        // CORREGIDO: Usar el total de elementos de la paginación
        long totalElementos = catalogoHijosPage.getTotalElements();

        // Mensaje para el usuario
        String mensajeUsuario = "Se han encontrado un total de: " + catalogoDTOList.size() + " catalogo(s)";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + totalElementos + " catálogos hijos";

        df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<CatalogoDTO> obtenerCatalogoPorToken(HttpServletRequest httpServletRequest,
                                                                         String tokenIdentificador) {

    RespuestaPorDefectoAuditoria<CatalogoDTO> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(df2.getLogOut());
            df.setSinAcceso(df2.getSinAcceso());
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();
        df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        Catalogo catalogo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                tokenIdentificador, false
        );

        if (catalogo == null) {
            df.setMensaje("El catalogo consultado no existe");
            return df;
        }
        CatalogoDTO catalogoDTO = catalogo.convertirADTO();
        Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(
                catalogo, empresa, false
        );
        catalogoDTO.setTieneHijos(cantidadDeHijos > 0);

        // Mensaje para el usuario
        String mensajeUsuario = "Se encontro con éxito el catalogo: " + catalogo.getNombre();

        // Mensaje para auditoría
        String mensajeAuditoria = "Se obtuvo con éxito el catálogo " + catalogo.getNombre() + " por token";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTO, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }
    return df;
}

@Override
public RespuestaPorDefectoAuditoria<List<CatalogoDTO>>
obtenerHijos(HttpServletRequest httpServletRequest, String tokenIdentificador) {

    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(df2.getLogOut());
            df.setSinAcceso(df2.getSinAcceso());
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();
        df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        Catalogo catalogoPadre = this.catalogoRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);

        if (catalogoPadre == null) {
            df.setMensaje("El catalogo padre no existe o fue eliminado anteriormente");
            return df;
        }

        List<Catalogo> catalogoList = this.catalogoRepository.findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(
                catalogoPadre,
                empresa, false
        );

        List<CatalogoDTO> catalogoDTOList = new ArrayList<>();

        for (Catalogo catalogo : catalogoList) {
            CatalogoDTO catalogoDTO = catalogo.convertirADTO();
            Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(catalogo,
                    empresa, false);
            catalogoDTO.setTieneHijos(cantidadDeHijos > 0);
            catalogoDTOList.add(catalogoDTO);

        }

        // Mensaje para el usuario
        String mensajeUsuario = "Se han obtenido con exito un total de: " + catalogoDTOList.size() + " catalogos principales.";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + catalogoDTOList.size() + " catálogos hijos";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTOList, mensajeAuditoria);

    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

@Override
public RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerDescendencia(HttpServletRequest httpServletRequest,
                                                                           String tokenIdentificador) {

    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {
        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(df2.getLogOut());
            df.setSinAcceso(df2.getSinAcceso());
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();
        df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        Catalogo catalogoUltimoHijo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador,
                false);
        if (catalogoUltimoHijo == null) {
            df.setMensaje("No se ha encontro el catalogo solicitado o posiblemente este ya fue eliminado");
            return df;
        }

        List<Catalogo> catalogoListDescendencia = new ArrayList<>();
        catalogoListDescendencia.add(catalogoUltimoHijo);
        Catalogo catalogoPadre = catalogoUltimoHijo.getCatalogoPadre();
        while (catalogoPadre != null) {
            catalogoListDescendencia.add(catalogoPadre);
            catalogoPadre = catalogoPadre.getCatalogoPadre();
        }

        Collections.reverse(catalogoListDescendencia);

        List<CatalogoDTO> catalogoDTOList = catalogoListDescendencia.stream().map(
                (cat) -> {
                    CatalogoDTO catalogoDTO = cat.convertirADTO();
                    Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(
                            cat,
                            empresa, false
                    );
                    catalogoDTO.setTieneHijos(cantidadDeHijos > 0);

                    return catalogoDTO;
                }
        ).toList();

        // Mensaje para el usuario
        String mensajeUsuario = "Se han encontrado un total de: " + catalogoDTOList.size() + " descendientes";

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + catalogoDTOList.size() + " catálogos descendientes";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTOList, mensajeAuditoria);

    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }
    return df;
}

@Override
public RespuestaPorDefectoAuditoria<List<CatalogoDTO>> obtenerTodosPorString(HttpServletRequest httpServletRequest,
                                                                             String stringFiltro) {

    RespuestaPorDefectoAuditoria<List<CatalogoDTO>> df = new RespuestaPorDefectoAuditoria<>();

    try {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
        if (!df2.isExito()) {
            df.setMensaje(df2.getMensaje());
            df.setLogOut(df2.getLogOut());
            df.setSinAcceso(df2.getSinAcceso());
            return df;
        }

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();
        df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

        List<Catalogo> listCatalogoFiltro =
                this.catalogoRepository.obtenerPorFiltro(
                        stringFiltro.toLowerCase(),
                        empresa.getIdEmpresa()
                );

        List<CatalogoDTO> catalogoDTOList = new ArrayList<>();
        for (Catalogo catalogo : listCatalogoFiltro) {
            CatalogoDTO catalogoDTO = catalogo.convertirADTO();
            Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(
                    catalogo, empresa, false
            );

            catalogoDTO.setTieneHijos(cantidadDeHijos > 0);
            catalogoDTOList.add(catalogoDTO);
        }

        // Mensaje para el usuario
        String mensajeUsuario = "Se han encontrado un total de: " + catalogoDTOList.size() + " con el filtro: " + stringFiltro;

        // Mensaje para auditoría
        String mensajeAuditoria = "Se han encontrado un total de: " + catalogoDTOList.size() + " catálogos por filtro";

        df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTOList, mensajeAuditoria);
    } catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

    return df;
}

    @Override
    public RespuestaPorDefectoAuditoria<List<Long>> borrarCatalogosQueNoTenganHijos(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<Long>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                return df;
            }

            String tokenEmpresa = httpServletRequest.getHeader(EtiquetaNemonico.HEAD_TOKEN_EMPRESA);

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenEmpresa, false);

            if (empresa == null) {
                df.setMensaje("No se envio un token de empresa válido o la empresa ya fue eliminada anteriormente.");
                return df;
            }

            List<Catalogo> catalogoList = this.catalogoRepository.findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(
                    null, empresa, false
            );

            List<Long> longList = new ArrayList<>();
            for (Catalogo catalogo : catalogoList) {
                Long cantidadDeHijos = this.catalogoRepository.countByCatalogoPadreAndEmpresaAndRemovido(
                        catalogo, empresa, false
                );

                //Si el catalogo no tiene hijos el catalogo se elimina
                if (cantidadDeHijos == 0) {
                    catalogo.setRemovido(true);
                    catalogo.setFechaEliminacion(new Date());
                    catalogo.setIpElimina(httpServletRequest.getRemoteAddr());
                    this.catalogoRepository.save(catalogo);
                    longList.add(catalogo.getIdCatalogo());
                }
            }

            df.llenarRespuestaExitosa("Se han eliminado de manera lógica: " +
                            longList.size() + " catalogos, se envia el arreglo con el id de los catalogos eliminados",
                    longList);

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
