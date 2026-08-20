package net.latinus.sistema.integral.gestion.seguridad.service.general;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.SeguimientoConductual;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.SeguimientoPsicologico;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.general.EvaluacionDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.EvaluacionDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.encuesta.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.SeguimientoConductualRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.SeguimientoPsicologicoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class EncuestaServiceImpl implements EncuestaService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private EncuestaRepository encuestaRepository;
    private SeccionRepository seccionRepository;
    private PreguntaRepository preguntaRepository;
    private RespuestaRepository respuestaRepository;
    private CatalogoRepository catalogoRepository;
    private EncabezadoRepository encabezadoRepository;
    private ContestacionRepository contestacionRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;
    private DocumentoService documentoService;
    private SeguimientoPsicologicoRepository psicologicoRepository;
    private SeguimientoConductualRepository conductualRepository;
    private DocumentoRepository documentoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private EvaluacionDocumentoRepository evaluacionDocumentoRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EncuestaDTO>> obtenerListaEncuestas(HttpServletRequest httpServletRequest,
                                                                                               BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncuestaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaEncuestas = encuestaRepository.findByRemovido(false);

            List<EncuestaDTO> encuestaDTOList = new ArrayList<>();
            for (Encuesta encuesta : listaEncuestas) {
                EncuestaDTO encuestaDTO = new EncuestaDTO();
                encuestaDTO.setIdEncuesta(encuesta.getIdEncuesta());
                encuestaDTO.setNombre(encuesta.getNombre());
                encuestaDTO.setDescripcion(encuesta.getDescripcion());
                encuestaDTO.setSeccionesOrdenadas(encuesta.getSeccionesOrdenadas());
                encuestaDTO.setTokenIdentificador(encuesta.getTokenIdentificador());
                encuestaDTO.setNemonico(encuesta.getCatalogo().getNemonico());
                encuestaDTO.setNemonicoCentro(encuesta.getTipoCentro().getNemonico());
                encuestaDTO.setTipoCentro(encuesta.getTipoCentro().getNombre());
                encuestaDTO.setFechaCreacion(encuesta.getFechaCreacion());

                encuestaDTOList.add(encuestaDTO);
            }

            encuestaDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<EncuestaDTO> paginacionResponse = paginacionService.obtenerDatos(encuestaDTOList, paginacionRequest);

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + paginacionResponse.getTotalItems() + " encuestas disponibles";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se consultó la lista de encuestas. Total encontrado: " + paginacionResponse.getTotalItems() + " registros";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<EncuestaDTO>> obtenerEncuestas(HttpServletRequest httpServletRequest,
                                                                            BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<List<EncuestaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            EncuestaDTO request = new Gson().fromJson(body, EncuestaDTO.class);

            List<Encuesta> listaEncuestas;
            if (request.getNemonicoCategoria() != null)
                listaEncuestas = encuestaRepository.findByCategoriaNemonicoAndRemovido(request.getNemonicoCategoria(), false);
            else
                listaEncuestas = encuestaRepository.findByRemovido(false);

            List<EncuestaDTO> encuestaDTOList = new ArrayList<>();
            for (Encuesta encuesta : listaEncuestas) {
                EncuestaDTO encuestaDTO = new EncuestaDTO();
                encuestaDTO.setIdEncuesta(encuesta.getIdEncuesta());
                encuestaDTO.setNombre(encuesta.getNombre());
                encuestaDTO.setDescripcion(encuesta.getDescripcion());
                encuestaDTO.setSeccionesOrdenadas(encuesta.getSeccionesOrdenadas());
                encuestaDTO.setTokenIdentificador(encuesta.getTokenIdentificador());
                encuestaDTO.setNemonico(encuesta.getCatalogo().getNemonico());
                encuestaDTO.setTipoCentro(encuesta.getTipoCentro().getNombre());
                encuestaDTO.setNemonicoCentro(encuesta.getTipoCentro().getNemonico());
                encuestaDTO.setCategoria(encuesta.getCategoria().getNombre());
                encuestaDTO.setNemonicoCategoria(encuesta.getCategoria().getNemonico());

                encuestaDTOList.add(encuestaDTO);
            }

            // Obtener el nombre de la categoría para el mensaje de auditoría
            String nombreCategoria = null;
            if (request.getNemonicoCategoria() != null) {
                Catalogo catalogoCategoria = catalogoRepository.findByNemonicoAndRemovido(request.getNemonicoCategoria(), false);
                if (catalogoCategoria != null) {
                    nombreCategoria = catalogoCategoria.getNombre();
                }
            }

            // Mensaje para el usuario
            String filtro = nombreCategoria != null ? 
                " filtradas por categoría " + nombreCategoria : "";
            String mensajeUsuario = "Obteniendo " + encuestaDTOList.size() + " encuestas" + filtro;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se consultaron las encuestas disponibles. Total encontrado: " + 
                encuestaDTOList.size() + " registros" + 
                (nombreCategoria != null ? 
                    ". Filtro aplicado por categoría: " + nombreCategoria : "");

            respuesta.llenarRespuestaExitosa(mensajeUsuario, encuestaDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
}

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> obtenerEvaluacionesPorFichaIdentificacion(HttpServletRequest httpServletRequest,
                                                                                                                     BodyEncriptado bodyEncriptado, String nemonicoCategoria) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaEncabezados = this.encabezadoRepository.findByEncuestaCategoriaNemonicoAndFichaIdentificacionTokenIdentificadorAndRemovido(nemonicoCategoria,
                    paginacionRequest.getTokenIdentificador(), false);

            List<EncabezadoDTO> encabezadoDTOList = new ArrayList<>();
            for (Encabezado encabezado : listaEncabezados) {

                EncabezadoDTO encabezadoDTO = new EncabezadoDTO();
                encabezadoDTO.setIdEncabezado(encabezado.getIdEncabezado());
                encabezadoDTO.setNombre(encabezado.getNombre());
                encabezadoDTO.setDescripcion(encabezado.getDescripcion());
                encabezadoDTO.setValorTotal(encabezado.getValorTotal());
                encabezadoDTO.setEncuesta(encabezado.getEncuesta().getTokenIdentificador());
                encabezadoDTO.setFichaIdentificacion(encabezado.getFichaIdentificacion().getTokenIdentificador());
                encabezadoDTO.setFechaCreacion(encabezado.getFechaCreacion());
                encabezadoDTO.setTokenIdentificador(encabezado.getTokenIdentificador());
                encabezadoDTO.setFechaCompletacion(encabezado.getFechaCompletacion());
                encabezadoDTO.setCompletada(encabezado.getCompletada());
                encabezadoDTOList.add(encabezadoDTO);
            }

            encabezadoDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<EncabezadoDTO> paginacionResponse = paginacionService.obtenerDatos(encabezadoDTOList, paginacionRequest);

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletosPorToken(paginacionRequest.getTokenIdentificador());

            // Obtener nombre de la categoría para mensajes más descriptivos
            Catalogo catalogoCategoria = catalogoRepository.findByNemonicoAndRemovido(nemonicoCategoria, false);
            String nombreCategoria = catalogoCategoria != null ? catalogoCategoria.getNombre() : "evaluaciones";

            // Determinar texto específico basado en el nemónico de categoría
            String tipoEvaluacionTexto = determinarTipoEvaluacionPorNemonico(nemonicoCategoria);

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + paginacionResponse.getTotalItems() + 
                " " + tipoEvaluacionTexto + " para " + nombresCompletos;

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersonaPorToken(paginacionRequest.getTokenIdentificador());
            String mensajeAuditoria = "Se consultaron las " + tipoEvaluacionTexto + 
                " de la persona con identificación: " + identificacionPersona + 
                ". Total encontrado: " + paginacionResponse.getTotalItems() + " registros";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    /**
     * Determina el texto del tipo de evaluación basado en el nemónico de categoría
     */
    private String determinarTipoEvaluacionPorNemonico(String nemonicoCategoria) {
        switch (nemonicoCategoria) {
            case "CATEGORIA_PSICOLOGICA":
                return "evaluaciones psicológicas";
            case "CATEGORIA_PRUEBA_PSICOLOGICA":
                return "pruebas psicológicas";
            case "CATEGORIA_RIESGO":
                return "evaluaciones de nivel de riesgo";
            case "CATEGORIA_GENERAL":
            default:
                return "evaluaciones conductuales";
        }
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> obtenerEvaluacionesPorNemonicoEncuesta(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String nemonicoEncuesta) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar token JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }

            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            // Buscar la encuesta en el catálogo
            Catalogo catalogoEncuesta = catalogoRepository.findByNemonicoAndRemovido(nemonicoEncuesta, false);
            if (catalogoEncuesta == null) {
                respuesta.setMensaje("No se encuentra el catálogo de la encuesta");
                respuesta.setLogOut(true);
                return respuesta;
            }

            // Configurar paginación y ordenamiento
            String direction = paginacionRequest.getDirection();
            Sort.Direction sortDirection = (direction != null && !direction.isEmpty()) ?
                    Sort.Direction.fromString(direction) :
                    Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by(sortDirection, paginacionRequest.getSort() != null ? paginacionRequest.getSort() : "fechaCreacion")
            );

            // Filtrar por nombre y descripción si se proporciona un filtro
            String filter = paginacionRequest.getFilter();
            Page<Encabezado> listaEncabezados;

            if (filter != null && !filter.isEmpty()) {
                listaEncabezados = this.encabezadoRepository.buscarPorFiltros(
                        catalogoEncuesta.getIdCatalogo(),
                        paginacionRequest.getTokenIdentificador(),
                        "%" + filter.toLowerCase() + "%", // Filtro por nombre
                        "%" + filter.toLowerCase() + "%", // Filtro por descripción
                        pageable
                );
            } else {
                listaEncabezados = this.encabezadoRepository.findByEncuestaCatalogoIdCatalogoAndFichaIdentificacionTokenIdentificadorAndRemovido(
                        catalogoEncuesta.getIdCatalogo(),
                        paginacionRequest.getTokenIdentificador(),
                        false,
                        pageable
                );
            }

            List<EncabezadoDTO> encabezadoDTOList = new ArrayList<>();

            for (Encabezado encabezado : listaEncabezados) {
                EncabezadoDTO encabezadoDTO = new EncabezadoDTO();
                encabezadoDTO.setIdEncabezado(encabezado.getIdEncabezado());
                encabezadoDTO.setNombre(encabezado.getNombre());
                encabezadoDTO.setDescripcion(encabezado.getDescripcion());
                encabezadoDTO.setValorTotal(encabezado.getValorTotal());
                encabezadoDTO.setEncuesta(encabezado.getEncuesta().getTokenIdentificador());
                encabezadoDTO.setFichaIdentificacion(encabezado.getFichaIdentificacion().getTokenIdentificador());
                encabezadoDTO.setFechaCreacion(encabezado.getFechaCreacion());
                encabezadoDTO.setTokenIdentificador(encabezado.getTokenIdentificador());
                encabezadoDTO.setFechaCompletacion(encabezado.getFechaCompletacion());
                encabezadoDTO.setCompletada(encabezado.getCompletada());

                encabezadoDTOList.add(encabezadoDTO);
            }

            encabezadoDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            // Construir la respuesta paginada
            PaginacionResponse<EncabezadoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(encabezadoDTOList);
            paginacionResponse.setTotalItems(listaEncabezados.getTotalElements());

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletosPorToken(paginacionRequest.getTokenIdentificador());

            // Mensaje para el usuario
            String filtroTexto = (filter != null && !filter.isEmpty()) ? " con filtro '" + filter + "'" : "";
            String mensajeUsuario = "Obteniendo " + paginacionResponse.getTotalItems() + 
                " evaluaciones de " + catalogoEncuesta.getNombre() + filtroTexto + " para " + nombresCompletos;

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersonaPorToken(paginacionRequest.getTokenIdentificador());
            String mensajeAuditoria = "Se consultaron las evaluaciones de encuesta " + catalogoEncuesta.getNombre() + 
                " para la persona con identificación: " + identificacionPersona + 
                ". Total encontrado: " + paginacionResponse.getTotalItems() + " registros" + 
                (filter != null && !filter.isEmpty() ? ". Filtro aplicado: " + filter : "");

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> obtenerEvaluacionesPorNemonicoCategoria(HttpServletRequest httpServletRequest,
                                                                                                                   BodyEncriptado bodyEncriptado, String nemonicoCentro, List<String> nemonicosCategoria) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<EncabezadoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            List<EncabezadoDTO> encabezadoDTOList = new ArrayList<>();

            // Variables para auditoría
            String tipoEvaluacionTexto = "evaluaciones";
            List<String> nombresCategoriasEncontradas = new ArrayList<>();

            for (String nemonicoCategoria : nemonicosCategoria) {

                Catalogo catalogoCategoria = catalogoRepository.findByNemonicoAndRemovido(nemonicoCategoria, false);

                if (catalogoCategoria == null) {
                    respuesta.setMensaje("No se encuentra el catálogo de la categoría: " + nemonicoCategoria);
                    respuesta.setLogOut(true);
                    return respuesta;
                }

                // Agregar nombre de categoría para auditoría
                nombresCategoriasEncontradas.add(catalogoCategoria.getNombre());

                var listaEncabezados = this.encabezadoRepository.findByEncuestaCategoriaNemonicoAndEncuestaTipoCentroNemonicoAndFichaIdentificacionTokenIdentificadorAndRemovido
                        (catalogoCategoria.getNemonico(), nemonicoCentro, paginacionRequest.getTokenIdentificador(), false);

                for (Encabezado encabezado : listaEncabezados) {
                    EncabezadoDTO encabezadoDTO = new EncabezadoDTO();
                    encabezadoDTO.setIdEncabezado(encabezado.getIdEncabezado());
                    encabezadoDTO.setNombre(encabezado.getNombre());
                    encabezadoDTO.setDescripcion(encabezado.getDescripcion());
                    encabezadoDTO.setValorTotal(encabezado.getValorTotal());
                    encabezadoDTO.setEncuesta(encabezado.getEncuesta().getTokenIdentificador());
                    encabezadoDTO.setFichaIdentificacion(encabezado.getFichaIdentificacion().getTokenIdentificador());
                    encabezadoDTO.setFechaCreacion(encabezado.getFechaCreacion());
                    encabezadoDTO.setTokenIdentificador(encabezado.getTokenIdentificador());
                    encabezadoDTO.setFechaCompletacion(encabezado.getFechaCompletacion());
                    encabezadoDTO.setCompletada(encabezado.getCompletada());
                    encabezadoDTOList.add(encabezadoDTO);
                }
            }

            // Determinar el tipo de evaluación basado en las categorías
            tipoEvaluacionTexto = determinarTipoEvaluacionPorCategorias(nemonicosCategoria);

            encabezadoDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<EncabezadoDTO> paginacionResponse = paginacionService.obtenerDatos(encabezadoDTOList, paginacionRequest);

            // Obtener información contextual para los mensajes
            String nombresCompletos = obtenerNombresCompletosPorToken(paginacionRequest.getTokenIdentificador());
            String identificacionPersona = obtenerIdentificacionPersonaPorToken(paginacionRequest.getTokenIdentificador());

            // Obtener nombre del tipo de centro
            Catalogo catalogoCentro = catalogoRepository.findByNemonicoAndRemovido(nemonicoCentro, false);
            String nombreTipoCentro = catalogoCentro != null ? catalogoCentro.getNombre() : "centro";

            // Construir texto de categorías para los mensajes
            String categoriasTexto = String.join(", ", nombresCategoriasEncontradas);

            // Mensaje para el usuario
            String filtroTexto = paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty() 
                ? " con filtro '" + paginacionRequest.getFilter() + "'" : "";
            String mensajeUsuario = "Obteniendo " + paginacionResponse.getTotalItems() + 
                " " + tipoEvaluacionTexto + filtroTexto + " para " + nombresCompletos + 
                " en " + nombreTipoCentro;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se consultaron las " + tipoEvaluacionTexto + 
                " de categorías [" + categoriasTexto + "] para la persona con identificación: " + identificacionPersona + 
                ". Tipo de centro: " + nombreTipoCentro + 
                ". Total encontrado: " + paginacionResponse.getTotalItems() + " registros.";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }

    /**
     * Determina el texto del tipo de evaluación basado en las categorías proporcionadas
     */
    private String determinarTipoEvaluacionPorCategorias(List<String> nemonicosCategoria) {
        if (nemonicosCategoria == null || nemonicosCategoria.isEmpty()) {
            return "evaluaciones";
        }

        // Verificar si contiene categorías específicas
        boolean tienePsicologica = nemonicosCategoria.contains("CATEGORIA_PSICOLOGICA");
        boolean tienePruebaPsicologica = nemonicosCategoria.contains("CATEGORIA_PRUEBA_PSICOLOGICA");
        boolean tieneRiesgo = nemonicosCategoria.contains("CATEGORIA_RIESGO");
        boolean tieneGeneral = nemonicosCategoria.contains("CATEGORIA_GENERAL");

        // Determinar el texto basado en las categorías presentes
        if (tienePruebaPsicologica && nemonicosCategoria.size() == 1) {
            return "pruebas psicológicas";
        } else if (tienePsicologica && nemonicosCategoria.size() == 1) {
            return "evaluaciones psicológicas";
        } else if (tieneRiesgo && nemonicosCategoria.size() == 1) {
            return "evaluaciones de nivel de riesgo";
        } else if (tieneGeneral && nemonicosCategoria.size() == 1) {
            return "evaluaciones conductuales";
        } else if (nemonicosCategoria.size() > 1) {
            // Si hay múltiples categorías, usar texto genérico
            return "evaluaciones de múltiples categorías";
        } else {
            // Fallback para categorías no reconocidas
            return "evaluaciones de categoría " + nemonicosCategoria.get(0);
        }
    }

    @Override
    public RespuestaPorDefectoAuditoria<EncuestaDTO> obtenerEncuestaPorTokenEncuesta(HttpServletRequest
                                                                                             httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<EncuestaDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            EncabezadoDTO encabezadoDTO = new Gson().fromJson(body, EncabezadoDTO.class);

            List<Contestacion> contestaciones = new ArrayList<>();
            Encabezado encabezado = encabezadoRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getTokenIdentificador(), false);
            if (encabezado != null) {
                contestaciones = contestacionRepository.findByEncabezadoIdEncabezadoAndRemovido(encabezado.getIdEncabezado(), false);
            }

            Encuesta encuesta = encuestaRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getEncuesta(), false);
            EncuestaDTO encuestaDTO = new EncuestaDTO();
            encuestaDTO.setIdEncuesta(encuesta.getIdEncuesta());
            encuestaDTO.setNombre(encuesta.getNombre());
            encuestaDTO.setDescripcion(encuesta.getDescripcion());
            encuestaDTO.setCategoria(encuesta.getCategoria().getNombre());
            encuestaDTO.setNemonicoCategoria(encuesta.getCategoria().getNemonico());
            encuestaDTO.setSeccionesOrdenadas(encuesta.getSeccionesOrdenadas());
            encuestaDTO.setTokenIdentificador(encuesta.getTokenIdentificador());
            encuestaDTO.setNemonicoCentro(encuesta.getTipoCentro().getNemonico());
            encuestaDTO.setTipoCentro(encuesta.getTipoCentro().getNombre());

            List<Seccion> secciones = seccionRepository.findByEncuestaAndRemovido(encuesta, false);

            // Ordenar secciones si es necesario
            if (encuesta.getSeccionesOrdenadas()) {
                secciones.sort(Comparator.comparingInt(Seccion::getOrden));
            }

            for (Seccion seccion : secciones) {
                SeccionDTO seccionDTO = new SeccionDTO();
                seccionDTO.setNombre(seccion.getNombre());
                seccionDTO.setOrden(seccion.getOrden());
                seccionDTO.setPreguntasOrdenadas(seccion.getPreguntasOrdenadas());
                seccionDTO.setTienePuntuacion(seccion.getTienePuntuacion());
                seccionDTO.setIdSeccion(seccion.getIdSeccion());

                List<Pregunta> preguntas = preguntaRepository.findBySeccionAndRemovido(seccion, false);

                // Ordenar preguntas si es necesario
                if (seccion.getPreguntasOrdenadas()) {
                    preguntas.sort(Comparator.comparingInt(Pregunta::getOrden));
                }

                for (Pregunta pregunta : preguntas) {
                    PreguntaDTO preguntaDTO = new PreguntaDTO();
                    preguntaDTO.setTexto(pregunta.getTexto());
                    preguntaDTO.setCategoria(pregunta.getCategoria().getNemonico());
                    preguntaDTO.setIdPregunta(pregunta.getIdPregunta());
                    preguntaDTO.setTieneObservaciones(pregunta.getTieneObservaciones());
                    preguntaDTO.setPermiteDocumentos(pregunta.getPermiteDocumentos());
                    preguntaDTO.setOrden(pregunta.getOrden());
                    preguntaDTO.setRequerido(pregunta.getRequerido());
                    preguntaDTO.setRespuestasOrdenadas(pregunta.getRespuestasOrdenadas());

                    List<Respuesta> respuestas = respuestaRepository.findByPreguntaAndRemovido(pregunta, false);

                    // Ordenar respuestas si es necesario
                    if (pregunta.getRespuestasOrdenadas()) {
                        respuestas.sort(Comparator.comparingInt(Respuesta::getOrden));
                    }

                    for (Respuesta respuesta_0 : respuestas) {
                        RespuestaDTO respuestaDTO = new RespuestaDTO();
                        respuestaDTO.setIdRespuesta(respuesta_0.getIdRespuesta());
                        respuestaDTO.setRespuesta(respuesta_0.getRespuesta());
                        respuestaDTO.setOrden(respuesta_0.getOrden());
                        respuestaDTO.setRespuestaCorrecta(respuesta_0.getRespuestaCorrecta());
                        respuestaDTO.setValorRespuesta(respuesta_0.getValorRespuesta());
                        preguntaDTO.getRespuestas().add(respuestaDTO);
                    }

                    if (contestaciones != null && !contestaciones.isEmpty()) {
                        var listaContestaciones = contestaciones.stream()
                                .filter(contestacion -> contestacion.getPregunta().getIdPregunta().equals(pregunta.getIdPregunta()))
                                .toList();

                        for (Contestacion contestacion : listaContestaciones) {
                            ContestacionDTO contestacionDTO = new ContestacionDTO();
                            contestacionDTO.setIdContestacion(contestacion.getIdContestacion());
                            contestacionDTO.setIdPregunta(contestacion.getPregunta().getIdPregunta());
                            if (contestacion.getRespuesta() != null)
                                contestacionDTO.setIdRespuesta(contestacion.getRespuesta().getIdRespuesta());
                            contestacionDTO.setContestacion(contestacion.getContestacion());
                            contestacionDTO.setObservacion(contestacion.getObservacion());
                            contestacionDTO.setCritico(Boolean.TRUE.equals(contestacion.getCritico()));

                            preguntaDTO.getContestaciones().add(contestacionDTO);
                        }
                    }

                    seccionDTO.getPreguntas().add(preguntaDTO);
                }
                encuestaDTO.getSecciones().add(seccionDTO);
            }

            // Mensaje para el usuario
            String mensajeUsuario = "Plantilla de " + encuesta.getCategoria().getNombre().toLowerCase() + " cargada correctamente";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se consultó la plantilla de " + encuesta.getCategoria().getNombre().toLowerCase() + 
                " '" + encuesta.getNombre() + "' con " + encuestaDTO.getSecciones().size() + " secciones y " + 
                encuestaDTO.getSecciones().stream().mapToInt(s -> s.getPreguntas().size()).sum() + " preguntas";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, encuestaDTO, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EncuestaDTO> obtenerEvaluacionPorTokenEncabezado(HttpServletRequest
                                                                                                 httpServletRequest, BodyEncriptado bodyEncriptado) {

    RespuestaPorDefectoAuditoria<EncuestaDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

    try {
        RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
        if (!df22.isExito()) {
            respuesta.setMensaje(df22.getMensaje());
            return respuesta;
        }
        String body = df22.getData();
        EncabezadoDTO encabezadoDTO = new Gson().fromJson(body, EncabezadoDTO.class);

        Encabezado encabezado = encabezadoRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getTokenIdentificador(), false);
        List<Contestacion> contestaciones = contestacionRepository.findByEncabezadoIdEncabezadoAndRemovido(encabezado.getIdEncabezado(), false);

        Encuesta encuesta = encuestaRepository.findByTokenIdentificador(encabezado.getEncuesta().getTokenIdentificador());
        EncuestaDTO encuestaDTO = new EncuestaDTO();
        encuestaDTO.setIdEncuesta(encuesta.getIdEncuesta());
        encuestaDTO.setNombre(encuesta.getNombre());
        encuestaDTO.setDescripcion(encuesta.getDescripcion());
        encuestaDTO.setSeccionesOrdenadas(encuesta.getSeccionesOrdenadas());
        encuestaDTO.setTokenIdentificador(encuesta.getTokenIdentificador());
        encuestaDTO.setNemonico(encuesta.getCatalogo().getNemonico());
        encuestaDTO.setTipoCentro(encuesta.getTipoCentro().getNombre());
        encuestaDTO.setNemonicoCentro(encuesta.getTipoCentro().getNemonico());
        encuestaDTO.setCategoria(encuesta.getCategoria().getNombre());
        encuestaDTO.setNemonicoCategoria(encuesta.getCategoria().getNemonico());
        encuestaDTO.setAdolescente(
                encabezado.getFichaIdentificacion().getNombres() + " " +
                        encabezado.getFichaIdentificacion().getApellidoPaterno() + " " +
                        encabezado.getFichaIdentificacion().getApellidoMaterno());
        encuestaDTO.setDniAdolescente(encabezado.getFichaIdentificacion().getNumeroIdentificacion());
        encuestaDTO.setCompletada(encabezado.getCompletada());
        encuestaDTO.setJustificacionValoracion(encabezado.getJustificacionValoracion());
        encuestaDTO.setFechaValoracion(encabezado.getFechaValoracion());
        if (encabezado.getValoracionFinal() != null) {
            encuestaDTO.setTokenIdentificadorValoracionFinal(encabezado.getValoracionFinal().getTokenIdentificador());
            encuestaDTO.setNombreValoracionFinal(encabezado.getValoracionFinal().getNombre());
        }

        // Datos de cabecera para informe SAVRY (PDF I. Datos generales)
        var ficha = encabezado.getFichaIdentificacion();
        if (ficha != null) {
            encuestaDTO.setFechaNacimientoAdolescente(ficha.getFechaNacimiento());
            encuestaDTO.setEdadAdolescente(calcularEdadAdolescente(ficha.getFechaNacimiento(), ficha.getEdad()));
            if (ficha.getCentroIngreso() != null) {
                encuestaDTO.setEstablecimiento(ficha.getCentroIngreso().getNombre());
            }
        }
        encuestaDTO.setCorrelativo(encabezado.getIdEncabezado());
        encuestaDTO.setFechaRegistro(encabezado.getFechaCreacion());
        encuestaDTO.setFechaEvaluacion(
                encabezado.getFechaCompletacion() != null
                        ? encabezado.getFechaCompletacion()
                        : encabezado.getFechaValoracion());
        encuestaDTO.setEvaluador(resolverNombreEvaluador(encabezado));

        List<Seccion> secciones = seccionRepository.findByEncuestaAndRemovido(encuesta, false);

        // Ordenar secciones si es necesario
        if (encuesta.getSeccionesOrdenadas()) {
            secciones.sort(Comparator.comparingInt(Seccion::getOrden));
        }

        for (Seccion seccion : secciones) {
            SeccionDTO seccionDTO = new SeccionDTO();
            seccionDTO.setNombre(seccion.getNombre());
            seccionDTO.setOrden(seccion.getOrden());
            seccionDTO.setPreguntasOrdenadas(seccion.getPreguntasOrdenadas());
            seccionDTO.setTienePuntuacion(seccion.getTienePuntuacion());
            seccionDTO.setIdSeccion(seccion.getIdSeccion());

            List<Pregunta> preguntas = preguntaRepository.findBySeccionAndRemovido(seccion, false);

            // Ordenar preguntas si es necesario
            if (seccion.getPreguntasOrdenadas()) {
                preguntas.sort(Comparator.comparingInt(Pregunta::getOrden));
            }

            for (Pregunta pregunta : preguntas) {
                PreguntaDTO preguntaDTO = new PreguntaDTO();
                preguntaDTO.setTexto(pregunta.getTexto());
                preguntaDTO.setCategoria(pregunta.getCategoria().getNemonico());
                preguntaDTO.setIdPregunta(pregunta.getIdPregunta());
                preguntaDTO.setTieneObservaciones(pregunta.getTieneObservaciones());
                preguntaDTO.setPermiteDocumentos(pregunta.getPermiteDocumentos());
                preguntaDTO.setOrden(pregunta.getOrden());
                preguntaDTO.setRequerido(pregunta.getRequerido());
                preguntaDTO.setRespuestasOrdenadas(pregunta.getRespuestasOrdenadas());

                List<Respuesta> respuestas = respuestaRepository.findByPreguntaAndRemovido(pregunta, false);

                // Ordenar respuestas si es necesario
                if (pregunta.getRespuestasOrdenadas()) {
                    respuestas.sort(Comparator.comparingInt(Respuesta::getOrden));
                }

                for (Respuesta respuesta_0 : respuestas) {
                    RespuestaDTO respuestaDTO = new RespuestaDTO();
                    respuestaDTO.setIdRespuesta(respuesta_0.getIdRespuesta());
                    respuestaDTO.setRespuesta(respuesta_0.getRespuesta());
                    respuestaDTO.setOrden(respuesta_0.getOrden());
                    respuestaDTO.setRespuestaCorrecta(respuesta_0.getRespuestaCorrecta());
                    respuestaDTO.setValorRespuesta(respuesta_0.getValorRespuesta());

                    preguntaDTO.getRespuestas().add(respuestaDTO);
                }

                var listaContestaciones = contestaciones.stream()
                        .filter(contestacion -> contestacion.getPregunta().getIdPregunta().equals(pregunta.getIdPregunta()))
                        .toList();

                for (Contestacion contestacion : listaContestaciones) {
                    ContestacionDTO contestacionDTO = new ContestacionDTO();
                    contestacionDTO.setIdContestacion(contestacion.getIdContestacion());
                    contestacionDTO.setIdPregunta(contestacion.getPregunta().getIdPregunta());
                    if (contestacion.getRespuesta() != null)
                        contestacionDTO.setIdRespuesta(contestacion.getRespuesta().getIdRespuesta());
                    contestacionDTO.setContestacion(contestacion.getContestacion());
                    contestacionDTO.setObservacion(contestacion.getObservacion());
                    contestacionDTO.setCritico(Boolean.TRUE.equals(contestacion.getCritico()));

                    preguntaDTO.getContestaciones().add(contestacionDTO);
                }

                seccionDTO.getPreguntas().add(preguntaDTO);
            }
            encuestaDTO.getSecciones().add(seccionDTO);
        }

        // Obtener nombres completos para los mensajes
        String nombresCompletos = obtenerNombresCompletos(encabezado.getFichaIdentificacion());

        // Mensaje para el usuario
        String estadoTexto = encabezado.getCompletada() ? "completada" : "en borrador";
        String mensajeUsuario = "Evaluación " + encuesta.getCategoria().getNombre().toLowerCase() + " " + estadoTexto + " de " + nombresCompletos;

        // Mensaje para auditoría
        String identificacionPersona = obtenerIdentificacionPersona(encabezado.getFichaIdentificacion());
        String mensajeAuditoria = "Se consultó la evaluación " + encuesta.getCategoria().getNombre().toLowerCase() + 
            " '" + encuesta.getNombre() + "' de la persona con identificación: " + identificacionPersona + 
            ". Estado: " + estadoTexto + ". Total de contestaciones: " + contestaciones.size();

        respuesta.llenarRespuestaExitosa(mensajeUsuario, encuestaDTO, mensajeAuditoria);

    } catch (Exception ex) {
        respuesta.llenarConDatosDeException(ex);
    }

    return respuesta;
}

    @Override
    public RespuestaPorDefectoAuditoria<EncuestaDTO> obtenerEncuestaPorId(HttpServletRequest
                                                                                  httpServletRequest, EncuestaDTO encuestaDTO) {
        RespuestaPorDefectoAuditoria<EncuestaDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        var encuesta = encuestaRepository.findByIdEncuestaAndRemovido(encuestaDTO.getIdEncuesta(), false);

        encuestaDTO = new EncuestaDTO();
        encuestaDTO.setIdEncuesta(encuesta.getIdEncuesta());
        encuestaDTO.setNombre(encuesta.getNombre());
        encuestaDTO.setDescripcion(encuesta.getDescripcion());
        encuestaDTO.setSeccionesOrdenadas(encuesta.getSeccionesOrdenadas());
        /*Secciones*/


        respuesta.llenarRespuestaExitosa("Encuesta", encuestaDTO);

        return respuesta;
    }

    @Transactional
    @Override
    public RespuestaPorDefectoAuditoria<EncuestaDTO> crearEncuesta(HttpServletRequest httpServletRequest, EncuestaDTO encuestaDTO) {

        RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

        BodyJwtValido bodyJwtValido = df2.getData();
        Empresa empresa = bodyJwtValido.getEmpresa();
        UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
        String ip = httpServletRequest.getRemoteAddr();

        Catalogo tipoCentro = catalogoRepository.findByNemonicoAndRemovido(encuestaDTO.getNemonicoCentro(), false);
        Catalogo categoriaEncuesta = catalogoRepository.findByNemonicoAndRemovido(encuestaDTO.getNemonicoCategoria(), false);
        Catalogo catalogo = new Catalogo();

        catalogo.setUsuarioSistemaCrea(usuarioSistema);

        catalogo.setEmpresa(empresa);
        catalogo.setDescripcion("Plantilla de Encuesta: " + encuestaDTO.getNombre());
        catalogo.setIpCrea(ip);
        catalogo.setNombre(encuestaDTO.getNombre());
        catalogo.setNemonico("ENCUESTA_" + encuestaDTO.getNombre().toUpperCase().replace(" ", "_") + "_" + tipoCentro.getNombre().toUpperCase());
        catalogo.setFechaCreacion(new Date());
        catalogo.setCatalogoPadre(this.catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.PLANTILLAS_ENCUESTA, false));
        catalogo = this.catalogoRepository.save(catalogo);

        // Convertir DTO a entidad Encuesta
        Encuesta encuesta = new Encuesta();
        encuesta.setNombre(encuestaDTO.getNombre());
        encuesta.setDescripcion(encuestaDTO.getDescripcion());
        encuesta.setSeccionesOrdenadas(encuestaDTO.getSeccionesOrdenadas());
        encuesta.setCatalogo(catalogo);
        encuesta.setTipoCentro(tipoCentro);
        encuesta.setCategoria(categoriaEncuesta);

        // Guardar Encuesta en la base de datos
        Encuesta encuestaCreada = encuestaRepository.save(encuesta);

        int totalSecciones = 0;
        int totalPreguntas = 0;
        int totalRespuestas = 0;

        // Iterar sobre las secciones y preguntas para guardarlas
        for (SeccionDTO seccionDTO : encuestaDTO.getSecciones()) {
            Seccion seccion = new Seccion();
            seccion.setNombre(seccionDTO.getNombre());
            seccion.setOrden(seccionDTO.getOrden());
            seccion.setPreguntasOrdenadas(seccionDTO.getPreguntasOrdenadas());
            seccion.setTienePuntuacion(seccionDTO.getTienePuntuacion());
            seccion.setEncuesta(encuestaCreada);

            // Guardar sección en la base de datos
            Seccion seccionGuardada = seccionRepository.save(seccion);
            totalSecciones++;

            // Guardar preguntas de esta sección
            for (PreguntaDTO preguntaDTO : seccionDTO.getPreguntas()) {
                Pregunta pregunta = new Pregunta();
                pregunta.setTexto(preguntaDTO.getTexto());
                pregunta.setOrden(preguntaDTO.getOrden());
                pregunta.setRequerido(preguntaDTO.getRequerido());
                pregunta.setRespuestasOrdenadas(preguntaDTO.getRespuestasOrdenadas());
                pregunta.setTieneObservaciones(preguntaDTO.getTieneObservaciones());
                pregunta.setPermiteDocumentos(preguntaDTO.getPermiteDocumentos());
                pregunta.setSeccion(seccionGuardada);

                Catalogo categoria = catalogoRepository.findByNemonicoAndRemovido(preguntaDTO.getCategoria(), false);
                pregunta.setCategoria(categoria);

                // Guardar pregunta en la base de datos
                Pregunta preguntaGuardada = preguntaRepository.save(pregunta);
                totalPreguntas++;

                // Guardar respuestas de esta pregunta
                for (RespuestaDTO respuestaDTO : preguntaDTO.getRespuestas()) {
                    Respuesta respuesta = new Respuesta();
                    respuesta.setRespuesta(respuestaDTO.getRespuesta());
                    respuesta.setValorRespuesta(respuestaDTO.getValorRespuesta());
                    respuesta.setOrden(respuestaDTO.getOrden());
                    respuesta.setRespuestaCorrecta(respuestaDTO.getRespuestaCorrecta());
                    respuesta.setPregunta(preguntaGuardada);

                    // Guardar respuesta en la base de datos
                    respuestaRepository.save(respuesta);
                    totalRespuestas++;
                }
            }
        }

        RespuestaPorDefectoAuditoria<EncuestaDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        // Mensaje para el usuario
        String mensajeUsuario = "Se creó con éxito la encuesta '" + encuestaDTO.getNombre() + "' para " + tipoCentro.getNombre();

        // Mensaje para auditoría
        String mensajeAuditoria = "Se creó la encuesta '" + encuestaDTO.getNombre() + "' de categoría " + 
            categoriaEncuesta.getNombre() + " para tipo de centro " + tipoCentro.getNombre() + 
            ". Estructura creada: " + totalSecciones + " secciones, " + totalPreguntas + 
            " preguntas y " + totalRespuestas + " opciones de respuesta. Nemónico asignado: " + catalogo.getNemonico();

        respuesta.llenarRespuestaExitosa(mensajeUsuario, encuestaDTO, mensajeAuditoria);

        return respuesta;
    }

    @Transactional
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> crearEvaluacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            EncabezadoDTO encabezadoDTO = new Gson().fromJson(body, EncabezadoDTO.class);

            Encuesta encuesta = encuestaRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getEncuesta(), false);
            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getFichaIdentificacion(), false);
            Encabezado encabezado;

            boolean esEdicion = false;
            boolean soloValoracion = Boolean.TRUE.equals(encabezadoDTO.getSoloValoracion());

            if (encabezadoDTO.getTokenIdentificador() != null) {
                encabezado = encabezadoRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getTokenIdentificador(), false);
                esEdicion = true;

                if (!soloValoracion) {
                    //Se eliminan las contestaciones del borrador para volverlas a crear posteriormente
                    List<Contestacion> contestaciones = contestacionRepository.findByEncabezadoIdEncabezadoAndRemovido(encabezado.getIdEncabezado(), false);
                    contestacionRepository.deleteAll(contestaciones);
                }
            } else {
                encabezado = new Encabezado();
                encabezado.setNombre(encuesta.getNombre());
                encabezado.setDescripcion(encuesta.getDescripcion());
                encabezado.setEncuesta(encuesta);
                encabezado.setValorTotal(0.0);
                encabezado.setFichaIdentificacion(fichaIdentificacion);
            }

            if (soloValoracion) {
                String errorValoracion = aplicarValoracionFinal(encabezado, encabezadoDTO, true);
                if (errorValoracion != null) {
                    respuesta.setMensaje(errorValoracion);
                    return respuesta;
                }
                encabezado.setCompletada(true);
                if (encabezado.getFechaCompletacion() == null) {
                    encabezado.setFechaCompletacion(new Date());
                }
                encabezadoRepository.save(encabezado);

                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion != null ? fichaIdentificacion : encabezado.getFichaIdentificacion());
                String mensajeUsuario = "Se actualizó con éxito la valoración final de " + encabezado.getNombre() + " para " + nombresCompletos;
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion != null ? fichaIdentificacion : encabezado.getFichaIdentificacion());
                String mensajeAuditoria = "Se revaloró la evaluación de " + encabezado.getNombre() +
                        " para la persona con identificación: " + identificacionPersona;
                respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);
                return respuesta;
            }

            if (encabezadoDTO.getCompletada()) {
                encabezado.setCompletada(true);
                encabezado.setFechaCompletacion(new Date());
                // Valoración final obligatoria SOLO para SAVRY (no HCR-20 / DASH / ERASOR).
                if (esEncuestaSavry(encuesta)) {
                    String errorValoracion = aplicarValoracionFinal(encabezado, encabezadoDTO, true);
                    if (errorValoracion != null) {
                        respuesta.setMensaje(errorValoracion);
                        return respuesta;
                    }
                } else if (encabezadoDTO.getTokenIdentificadorValoracionFinal() != null
                        || (encabezadoDTO.getJustificacionValoracion() != null
                        && !encabezadoDTO.getJustificacionValoracion().isBlank())) {
                    aplicarValoracionFinal(encabezado, encabezadoDTO, false);
                }
            } else {
                encabezado.setCompletada(false);
            }

            encabezadoRepository.save(encabezado);

            if (encabezadoDTO.getContestaciones() != null) {
                for (ContestacionDTO contestacionDTO : encabezadoDTO.getContestaciones()) {
                    Contestacion contestacion = new Contestacion();

                    contestacion.setEncabezado(encabezado);
                    contestacion.setContestacion(contestacionDTO.getContestacion());
                    contestacion.setObservacion(contestacionDTO.getObservacion());
                    contestacion.setCritico(Boolean.TRUE.equals(contestacionDTO.getCritico()));

                    Pregunta pregunta = preguntaRepository.findByIdPreguntaAndRemovido(contestacionDTO.getIdPregunta(), false);
                    contestacion.setPregunta(pregunta);

                    if (contestacionDTO.getIdRespuesta() != null) {
                        Respuesta respuestaEncuesta = respuestaRepository.findByIdRespuestaAndRemovido(contestacionDTO.getIdRespuesta(), false);
                        contestacion.setRespuesta(respuestaEncuesta);
                    }

                    contestacionRepository.save(contestacion);
                }
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);

            // Mensaje para el usuario
            String accion = esEdicion ? "actualizó" : "creó";
            String estado = encabezadoDTO.getCompletada() ? "completada" : "como borrador";
            String mensajeUsuario = "Se " + accion + " con éxito la evaluación " + estado + " de " + encuesta.getNombre() + " para " + nombresCompletos;

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
            int totalContestaciones = encabezadoDTO.getContestaciones() != null ? encabezadoDTO.getContestaciones().size() : 0;
            String mensajeAuditoria = "Se " + accion + " la evaluación de " + encuesta.getNombre() +
                " para la persona con identificación: " + identificacionPersona +
                ". Estado: " + estado + ". Total de contestaciones: " + totalContestaciones;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    /**
     * True solo para encuesta SAVRY (Valoración de riesgo y violencia),
     * no para HCR-20 / DASH / ERASOR aunque compartan CATEGORIA_RIESGO.
     */
    private boolean esEncuestaSavry(Encuesta encuesta) {
        if (encuesta == null || encuesta.getCatalogo() == null || encuesta.getCatalogo().getNemonico() == null) {
            return false;
        }
        String nemonico = encuesta.getCatalogo().getNemonico().toUpperCase();
        return nemonico.contains("FACTORES_DE_RIESGO") || nemonico.contains("SAVRY");
    }

    /**
     * Aplica valoración final SAVRY al encabezado.
     * @return mensaje de error o null si OK
     */
    private String aplicarValoracionFinal(Encabezado encabezado, EncabezadoDTO encabezadoDTO, boolean obligatoria) {
        String tokenValoracion = encabezadoDTO.getTokenIdentificadorValoracionFinal();
        String justificacion = encabezadoDTO.getJustificacionValoracion();

        if (obligatoria) {
            if (tokenValoracion == null || tokenValoracion.isBlank() || "0".equals(tokenValoracion)) {
                return "Debe seleccionar el nivel de riesgo final (Bajo, Medio o Alto).";
            }
            if (justificacion == null || justificacion.isBlank()) {
                return "Debe ingresar la justificación de la valoración final.";
            }
        }

        if (tokenValoracion != null && !tokenValoracion.isBlank() && !"0".equals(tokenValoracion)) {
            Catalogo valoracion = catalogoRepository.findByTokenIdentificadorAndRemovido(tokenValoracion, false);
            if (valoracion == null) {
                return "El nivel de riesgo final seleccionado no es válido.";
            }
            encabezado.setValoracionFinal(valoracion);
            encabezado.setFechaValoracion(new Date());
        }

        if (justificacion != null) {
            encabezado.setJustificacionValoracion(justificacion.trim());
        }

        return null;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarEncuesta(HttpServletRequest httpServletRequest,
                                                                    BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            EncuestaDTO encuestaDTO = new Gson().fromJson(body, EncuestaDTO.class);

            Encuesta encuestaAnterior = encuestaRepository.findByTokenIdentificadorAndRemovido(encuestaDTO.getTokenIdentificador(), false);

            Catalogo tipoCentro = catalogoRepository.findByNemonicoAndRemovido(encuestaDTO.getNemonicoCentro(), false);
            Catalogo categoriaEncuesta = catalogoRepository.findByNemonicoAndRemovido(encuestaDTO.getNemonicoCategoria(), false);

            Catalogo catalogo = encuestaAnterior.getCatalogo();

            catalogo.setNombre(encuestaDTO.getNombre());
            catalogo.setDescripcion("Plantilla de Encuesta: " + encuestaDTO.getNombre());
            catalogoRepository.save(catalogo);

            // Convertir DTO a entidad Encuesta
            Encuesta encuesta = new Encuesta();
            encuesta.setNombre(encuestaDTO.getNombre());
            encuesta.setDescripcion(encuestaDTO.getDescripcion());
            encuesta.setSeccionesOrdenadas(encuestaDTO.getSeccionesOrdenadas());
            encuesta.setCatalogo(catalogo);
            encuesta.setTipoCentro(tipoCentro);
            encuesta.setCategoria(categoriaEncuesta);
            encuesta.setVersion(encuestaAnterior.getVersion() + 1);

            // Guardar Encuesta en la base de datos
            Encuesta encuestaCreada = encuestaRepository.save(encuesta);

            int totalSecciones = 0;
            int totalPreguntas = 0;
            int totalRespuestas = 0;

            // Iterar sobre las secciones y preguntas para guardarlas
            for (SeccionDTO seccionDTO : encuestaDTO.getSecciones()) {
                Seccion seccion = new Seccion();
                seccion.setNombre(seccionDTO.getNombre());
                seccion.setOrden(seccionDTO.getOrden());
                seccion.setPreguntasOrdenadas(seccionDTO.getPreguntasOrdenadas());
                seccion.setTienePuntuacion(seccionDTO.getTienePuntuacion());
                seccion.setEncuesta(encuestaCreada);

                // Guardar sección en la base de datos
                Seccion seccionGuardada = seccionRepository.save(seccion);
                totalSecciones++;

                // Guardar preguntas de esta sección
                for (PreguntaDTO preguntaDTO : seccionDTO.getPreguntas()) {
                    Pregunta pregunta = new Pregunta();
                    pregunta.setTexto(preguntaDTO.getTexto());
                    pregunta.setOrden(preguntaDTO.getOrden());
                    pregunta.setRequerido(preguntaDTO.getRequerido());
                    pregunta.setRespuestasOrdenadas(preguntaDTO.getRespuestasOrdenadas());
                    pregunta.setTieneObservaciones(preguntaDTO.getTieneObservaciones());
                    pregunta.setPermiteDocumentos(preguntaDTO.getPermiteDocumentos());
                    pregunta.setSeccion(seccionGuardada);

                    Catalogo categoria = catalogoRepository.findByNemonicoAndRemovido(preguntaDTO.getCategoria(), false);
                    pregunta.setCategoria(categoria);

                    // Guardar pregunta en la base de datos
                    Pregunta preguntaGuardada = preguntaRepository.save(pregunta);
                    totalPreguntas++;

                    // Guardar respuestas de esta pregunta
                    for (RespuestaDTO respuestaDTO : preguntaDTO.getRespuestas()) {
                        Respuesta opcion = new Respuesta();
                        opcion.setRespuesta(respuestaDTO.getRespuesta());
                        opcion.setValorRespuesta(respuestaDTO.getValorRespuesta());
                        opcion.setOrden(respuestaDTO.getOrden());
                        opcion.setRespuestaCorrecta(respuestaDTO.getRespuestaCorrecta());
                        opcion.setPregunta(preguntaGuardada);

                        // Guardar respuesta en la base de datos
                        respuestaRepository.save(opcion);
                        totalRespuestas++;
                    }
                }
            }

            encuestaAnterior.setRemovido(true);
            encuestaRepository.save(encuestaAnterior);

            // Mensaje para el usuario
            String mensajeUsuario = "Se actualizó con éxito la encuesta '" + encuestaDTO.getNombre() + "'";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se actualizó la encuesta '" + encuestaDTO.getNombre() + "' de categoría " + 
                categoriaEncuesta.getNombre() + " para tipo de centro " + tipoCentro.getNombre() + 
                ". Nueva versión: " + encuesta.getVersion() + ". Estructura actualizada: " + totalSecciones + 
                " secciones, " + totalPreguntas + " preguntas y " + totalRespuestas + " opciones de respuesta. " +
                "La versión anterior fue marcada como removida";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> removerEncuesta(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            EncuestaDTO encuestaDTO = new Gson().fromJson(body, EncuestaDTO.class);

            Encuesta encuesta = encuestaRepository.findByTokenIdentificadorAndRemovido(encuestaDTO.getTokenIdentificador(), false);

            // Obtener información antes de eliminar
            String nombreEncuesta = encuesta.getNombre();
            String nombreCategoria = encuesta.getCategoria().getNombre();
            String nombreTipoCentro = encuesta.getTipoCentro().getNombre();
            String nemonico = encuesta.getCatalogo().getNemonico();

            encuesta.setRemovido(true);
            encuestaRepository.save(encuesta);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la encuesta '" + nombreEncuesta + "'";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó la encuesta '" + nombreEncuesta + "' de categoría " + 
                nombreCategoria + " para tipo de centro " + nombreTipoCentro + 
                ". Nemónico: " + nemonico + ". La encuesta fue marcada como removida";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> removerEvaluacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            EncabezadoDTO encabezadoDTO = new Gson().fromJson(body, EncabezadoDTO.class);

            List<SeguimientoConductual> seguimientoConductuales = conductualRepository.findByEvaluacionTokenIdentificadorAndRemovido(encabezadoDTO.getTokenIdentificador(), false);

            for (SeguimientoConductual conductual : seguimientoConductuales) {
                conductual.setRemovido(true);
                conductualRepository.save(conductual);
            }

            List<SeguimientoPsicologico> seguimientoPsicologicos = psicologicoRepository.findByEvaluacionTokenIdentificadorAndRemovido(encabezadoDTO.getTokenIdentificador(), false);

            for (SeguimientoPsicologico psicologico : seguimientoPsicologicos) {
                psicologico.setRemovido(true);
                psicologicoRepository.save(psicologico);
            }

            Encabezado encabezado = encabezadoRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getTokenIdentificador(), false);

            // Obtener nombres completos para los mensajes antes de eliminar
            String nombresCompletos = obtenerNombresCompletos(encabezado.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(encabezado.getFichaIdentificacion());
            String nombreEncuesta = encabezado.getEncuesta().getNombre();

            encabezado.setRemovido(true);
            encabezadoRepository.save(encabezado);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito la evaluación de " + nombreEncuesta + " de " + nombresCompletos;

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó la evaluación de " + nombreEncuesta + 
                " de la persona con identificación: " + identificacionPersona + 
                ". También se eliminaron " + seguimientoConductuales.size() + " seguimientos conductuales y " + 
                seguimientoPsicologicos.size() + " seguimientos psicológicos asociados";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> subirDocumento(HttpServletRequest httpServletRequest,
                                                                MultipartFile[] multipartFiles,
                                                                BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(df2.getLogOut());
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            EncabezadoDTO encabezadoDTO = new Gson().fromJson(body, EncabezadoDTO.class);

            Encabezado encabezado = encabezadoRepository.findByTokenIdentificadorAndRemovido(encabezadoDTO.getTokenIdentificador(), false);

            if (encabezado == null || encabezado.getTokenIdentificador() == null) {
                respuesta.setMensaje("El informe no existe o ha sido removido");
                return respuesta;
            }

            FichaIdentificacion fichaIdentificacion = encabezado.getFichaIdentificacion();

            if (fichaIdentificacion == null || fichaIdentificacion.getTokenIdentificador() == null) {
                respuesta.setMensaje("Se recibio una ficha de identificación inválida");
                return respuesta;
            }

            if (encabezadoDTO.getEvaluacionDocumentoDTO() == null
                    || encabezadoDTO.getEvaluacionDocumentoDTO().getNemonicoCarpeta() == null
                    || encabezadoDTO.getEvaluacionDocumentoDTO().getNemonicoCarpeta().isBlank()) {
                respuesta.setMensaje("No se recibió la carpeta destino del documento");
                return respuesta;
            }

            String nemonicoCarpetaSolicitado = encabezadoDTO.getEvaluacionDocumentoDTO().getNemonicoCarpeta();
            Catalogo catalogoCarpeta = catalogoRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                    nemonicoCarpetaSolicitado, empresa.getTokenIdentificador(), false);

            if (catalogoCarpeta == null) {
                catalogoCarpeta = catalogoRepository.findByNemonicoAndRemovido(nemonicoCarpetaSolicitado, false);
            }

            if (catalogoCarpeta == null) {
                respuesta.setMensaje("No existe el catalogo de la carpeta especificada: " + nemonicoCarpetaSolicitado);
                return respuesta;
            }

            String nemonicoCarpeta = catalogoCarpeta.getNemonico();
            FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository
                    .findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIdentificacion.getTokenIdentificador(),
                            nemonicoCarpeta,
                            false
                    );

            if (fichaIdentificacionCarpeta == null) {
                fichaIdentificacionCarpeta = this.crearCarpetaEvaluacionSiNoExiste(
                        httpServletRequest,
                        fichaIdentificacion,
                        catalogoCarpeta,
                        usuarioSistema,
                        respuesta
                );
                if (fichaIdentificacionCarpeta == null) {
                    return respuesta;
                }
            }

            Carpeta carpeta = fichaIdentificacionCarpeta.getCarpeta();
            if (carpeta == null) {
                respuesta.setMensaje("La carpeta destino no tiene un identificador de Alfresco válido.");
                return respuesta;
            }

            // Tras recrear Alfresco, los UUID en BD pueden quedar huérfanos (EntityNotFound).
            RespuestaPorDefectoAuditoria<Carpeta> carpetaAsegurada = this.carpetaService
                    .asegurarExistenciaEnAlfresco(empresa.getTokenIdentificador(), carpeta);
            if (!carpetaAsegurada.isExito() || carpetaAsegurada.getData() == null
                    || carpetaAsegurada.getData().getIdentificadorAlfresco() == null
                    || carpetaAsegurada.getData().getIdentificadorAlfresco().isBlank()) {
                respuesta.setMensaje(carpetaAsegurada.getMensaje() != null
                        ? carpetaAsegurada.getMensaje()
                        : "La carpeta destino no tiene un identificador de Alfresco válido.");
                respuesta.setMensajeErrorReal(carpetaAsegurada.getMensajeErrorReal());
                return respuesta;
            }
            carpeta = carpetaAsegurada.getData();

            String idNodo = carpeta.getIdentificadorAlfresco();
            List<DocumentoDTO> documentoDTOList = encabezadoDTO.getEvaluacionDocumentoDTO().getDocumentoDTOList();

            if (documentoDTOList != null && !documentoDTOList.isEmpty()) {
                for (int i = 0; multipartFiles.length > i; i++) {

                    MultipartFile multipartFile = multipartFiles[i];
                    DocumentoDTO documentoDTO = documentoDTOList.get(i);

                    RespuestaPorDefectoAuditoria<DocumentoDTO> respuestaDocumento = this.documentoService.subirDocumentoAlfresco(httpServletRequest,
                            idNodo, multipartFile, documentoDTO);

                    if (!respuestaDocumento.isExito()) {
                        respuesta.setMensaje(respuestaDocumento.getMensaje());
                        respuesta.setMensajeErrorReal(respuestaDocumento.getMensajeErrorReal());
                        return respuesta;
                    }

                    documentoDTO = respuestaDocumento.getData();
                    Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                            documentoDTO.getTokenIdentificador(), false
                    );

                    EvaluacionDocumento evaluacionDocumento = new EvaluacionDocumento();
                    evaluacionDocumento.setEncabezado(encabezado);
                    evaluacionDocumento.setCatalogoCarpeta(catalogoCarpeta);
                    evaluacionDocumento.setCarpeta(carpeta);
                    evaluacionDocumento.setDocumento(documento);
                    evaluacionDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                    evaluacionDocumento.setUsuarioSistemaCrea(usuarioSistema);
                    evaluacionDocumentoRepository.save(evaluacionDocumento);
                }
            }

            respuesta.llenarRespuestaExitosa("Se ha subido correctamente el documento.", true);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    private FichaIdentificacionCarpeta crearCarpetaEvaluacionSiNoExiste(HttpServletRequest httpServletRequest,
                                                                       FichaIdentificacion fichaIdentificacion,
                                                                       Catalogo catalogoCarpeta,
                                                                       UsuarioSistema usuarioSistema,
                                                                       RespuestaPorDefectoAuditoria<Boolean> respuesta) {
        FichaIdentificacionCarpeta carpetaPadre = this.fichaIdentificacionCarpetaRepository
                .findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                        fichaIdentificacion.getTokenIdentificador(), null, false);

        if (carpetaPadre == null || carpetaPadre.getCarpeta() == null) {
            respuesta.setMensaje("No se encontró la carpeta principal de la ficha de identificación.");
            return null;
        }

        String tokenEmpresa = null;
        if (fichaIdentificacion.getEmpresa() != null) {
            tokenEmpresa = fichaIdentificacion.getEmpresa().getTokenIdentificador();
        } else if (carpetaPadre.getCarpeta().getEmpresa() != null) {
            tokenEmpresa = carpetaPadre.getCarpeta().getEmpresa().getTokenIdentificador();
        }
        if (tokenEmpresa == null || tokenEmpresa.isBlank()) {
            respuesta.setMensaje("No se pudo determinar la empresa para recrear carpetas en Alfresco.");
            return null;
        }

        RespuestaPorDefectoAuditoria<Carpeta> padreAsegurado = this.carpetaService.asegurarExistenciaEnAlfresco(
                tokenEmpresa, carpetaPadre.getCarpeta());
        if (!padreAsegurado.isExito() || padreAsegurado.getData() == null) {
            respuesta.setMensaje("No se pudo asegurar la carpeta principal en Alfresco"
                    + (padreAsegurado.getMensaje() != null ? ": " + padreAsegurado.getMensaje() : "."));
            respuesta.setMensajeErrorReal(padreAsegurado.getMensajeErrorReal());
            return null;
        }

        String nombreCarpeta = catalogoCarpeta.getNombre() != null && !catalogoCarpeta.getNombre().isBlank()
                ? catalogoCarpeta.getNombre().replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]", "").trim().replace(" ", "-")
                : catalogoCarpeta.getNemonico();

        CarpetaDTO carpetaDTO = new CarpetaDTO();
        carpetaDTO.setNombreCliente(nombreCarpeta);
        carpetaDTO.setDescripcion(catalogoCarpeta.getDescripcion() != null
                ? catalogoCarpeta.getDescripcion()
                : "Carpeta de documentos de " + nombreCarpeta);

        CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
        carpetaPadreDTO.setTokenIdentificador(padreAsegurado.getData().getTokenIdentificador());
        carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

        RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(
                httpServletRequest, true, carpetaDTO);
        if (!respuestaCarpeta.isExito() || respuestaCarpeta.getData() == null) {
            respuesta.setMensaje("No se pudo crear la carpeta para guardar los documentos"
                    + (respuestaCarpeta.getMensaje() != null ? ": " + respuestaCarpeta.getMensaje() : "."));
            respuesta.setMensajeErrorReal(respuestaCarpeta.getMensajeErrorReal());
            return null;
        }

        Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                respuestaCarpeta.getData().getTokenIdentificador(), false);
        if (carpetaGuardada == null) {
            respuesta.setMensaje("La carpeta se creó en Alfresco pero no se encontró en la base de datos.");
            return null;
        }

        FichaIdentificacionCarpeta fichaIdentificacionCarpeta = new FichaIdentificacionCarpeta();
        fichaIdentificacionCarpeta.setCarpeta(carpetaGuardada);
        fichaIdentificacionCarpeta.setFichaIdentificacion(fichaIdentificacion);
        fichaIdentificacionCarpeta.setTipoDeGestionDeAdolescente(catalogoCarpeta);
        fichaIdentificacionCarpeta.setFechaCreacion(new Date());
        fichaIdentificacionCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
        fichaIdentificacionCarpeta.setUsuarioSistemaCrea(usuarioSistema);
        this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpeta);

        this.logService.info("Se creó la carpeta " + catalogoCarpeta.getNemonico()
                + " para la ficha " + fichaIdentificacion.getTokenIdentificador());

        return fichaIdentificacionCarpeta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            EvaluacionDocumentoRequest evaluacionDocumentoRequest = new Gson().fromJson(body, EvaluacionDocumentoRequest.class);

            Pageable pageable = PageRequest.of(evaluacionDocumentoRequest.getPage(), evaluacionDocumentoRequest.getSize());

            Page<EvaluacionDocumento> documentosPage;

            if (evaluacionDocumentoRequest.getTokenEvaluacion() != null)
                documentosPage = this.evaluacionDocumentoRepository
                        .findByEncabezadoTokenIdentificadorAndRemovido(
                                evaluacionDocumentoRequest.getTokenEvaluacion(),
                                false,
                                pageable);
            else
                documentosPage = this.evaluacionDocumentoRepository
                        .findByEncabezadoFichaIdentificacionTokenIdentificadorAndCatalogoCarpetaNemonicoAndRemovido(
                                evaluacionDocumentoRequest.getTokenIdentificador(),
                                evaluacionDocumentoRequest.getNemonicoCarpeta(),
                                false,
                                pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (EvaluacionDocumento empDoc : documentosPage.toList()) {
                Documento documento = empDoc.getDocumento();
                DocumentoDTO documentoDTO = new DocumentoDTO();
                // Asigna campos al DTO según sea necesario
                Catalogo tipoDeDocumentoSistema = documento.getTipoDeDocumentoSistema();
                CatalogoDTO tipoDeDocumentoSistemaDTO = tipoDeDocumentoSistema.convertirADTO();

                documentoDTO.setTipoDocumentoSistema(tipoDeDocumentoSistemaDTO);
                documentoDTO.setTokenIdentificador(documento.getTokenIdentificador());
                documentoDTO.setNombre(documento.getNombreReal());
                documentoDTO.setDescripcion(documento.getDescripcion());
                documentoDTO.setFechaCreacion(documento.getFechaCreacion());
                documentoDTO.setMimeType(documento.getMimeType());
                documentoDTO.setTamanioBytes(documento.getTamanioByteDocumento());
                documentoDTO.setTipoDeDocumentoSistemaOtro(documento.getTipoDeDocumentoSistemaOtro());
                documentoList.add(documentoDTO);
            }

            PaginacionResponse<DocumentoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(documentoList);
            paginacionResponse.setTotalItems(documentosPage.getTotalElements());

            df.llenarRespuestaExitosa(
                    "Se han encontrado " + documentoList.size() + " documentos, de un total de " + documentosPage.getTotalElements(),
                    paginacionResponse
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    /**
     * Método auxiliar para obtener nombres completos de una ficha
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
     * Método auxiliar para obtener nombres completos por token de ficha
     */
    private String obtenerNombresCompletosPorToken(String tokenFicha) {
        try {
            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(tokenFicha, false);
            return obtenerNombresCompletos(fichaIdentificacion);
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * Método auxiliar para obtener identificación de persona por token de ficha
     */
    private String obtenerIdentificacionPersonaPorToken(String tokenFicha) {
        try {
            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(tokenFicha, false);
            return obtenerIdentificacionPersona(fichaIdentificacion);
        } catch (Exception e) {
            return "N/A";
        }
    }

    /** Edad actual a partir de F. nac.; si no hay fecha, usa la edad almacenada en ficha. */
    private Integer calcularEdadAdolescente(Date fechaNacimiento, Integer edadFicha) {
        if (fechaNacimiento != null) {
            LocalDate nacimiento = new Date(fechaNacimiento.getTime())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            int edad = Period.between(nacimiento, LocalDate.now()).getYears();
            return Math.max(edad, 0);
        }
        return edadFicha;
    }

    /** Nombre del usuario de login que registró la evaluación (nombres + apellidos; si no, userName). */
    private String resolverNombreEvaluador(Encabezado encabezado) {
        UsuarioSistema usuario = encabezado.getUsuarioSistemaCrea();
        if (usuario == null) {
            usuario = encabezado.getUsuarioSistemaEdita();
        }
        if (usuario == null) {
            return null;
        }
        String nombres = usuario.getNombres() != null ? usuario.getNombres().trim() : "";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos().trim() : "";
        String completo = (nombres + " " + apellidos).trim();
        if (!completo.isEmpty()) {
            return completo;
        }
        return usuario.getUserName();
    }
}
