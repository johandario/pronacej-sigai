package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.HistorialDeFotosFichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.HistorialDeFotosFichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.HistorialDeFotosFichaIdentificacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.HistorialDeFotosFichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class HistorialDeFotosFichaIdentificacionServiceImpl implements HistorialDeFotosFichaIdentificacionService {

    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private CatalogoRepository catalogoRepository;
    private HistorialDeFotosFichaIdentificacionRepository historialDeFotosFichaIdentificacionRepository;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO>
    subirArchivoAlHistorial(HttpServletRequest httpServletRequest, MultipartFile multipartFile,
                            HistorialDeFotosFichaIdentificacionDTO historialDeFotosFichaIdentificacionDTO) {

        RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(df2.getLogOut());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            FichaIdentificacionDTO fichaIdentificacionDTO = historialDeFotosFichaIdentificacionDTO.getFichaIdentificacionDTO();

            if (fichaIdentificacionDTO == null || fichaIdentificacionDTO.getTokenIdentificador().isEmpty() ||
                    fichaIdentificacionDTO.getTokenIdentificador() == null) {
                df.setMensaje("Se recibio una ficha de identificación inválida");
                return df;
            }

            FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                    fichaIdentificacionDTO.getTokenIdentificador(),
                    false
            );

            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no existe");
                return df;
            }

            CatalogoDTO tipoDTO = historialDeFotosFichaIdentificacionDTO.getTipo();

            Catalogo tipo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    tipoDTO.getTokenIdentificador(), false
            );

            if (tipo == null) {
                df.setMensaje("El tipo de arhcivo del historial es inválido");
                return df;
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            String nemonicoCarpeta = EtiquetaNemonico.CARPETA_GESTION_ADOLES_IMAGENES;
            Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage = this.fichaIdentificacionCarpetaRepository.
                    findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIdentificacion.getTokenIdentificador(),
                            nemonicoCarpeta,
                            false,
                            pageable
                    );

            if (fichaIdentificacionCarpetaPage.isEmpty()) {
                df.setMensaje("No se ha creado una carpeta para las depositar las imagenes que pertenezca a la ficha de identificación solicitada");
                return df;
            }

            if (fichaIdentificacionCarpetaPage.getTotalElements() > 1) {
                this.logService.warn("La ficha de identificacion: " +
                        fichaIdentificacion.getTokenIdentificador() + " tiene mas de una carpeta: " +
                        nemonicoCarpeta
                );
            }

            FichaIdentificacionCarpeta fichaIdentificacionCarpeta = fichaIdentificacionCarpetaPage.toList().get(0);

            Carpeta carpeta = fichaIdentificacionCarpeta.getCarpeta();

            String idNodo = carpeta.getIdentificadorAlfresco();
            DocumentoDTO documentoDTO = historialDeFotosFichaIdentificacionDTO.getDocumentoDTO();
            RespuestaPorDefectoAuditoria<DocumentoDTO> df4 = this.documentoService.subirDocumentoAlfresco(
                    httpServletRequest, idNodo, multipartFile, documentoDTO
            );

            if (!df4.isExito()) {
                df.setMensaje(df4.getMensaje());
                df.setMensajeErrorReal(df4.getMensajeErrorReal());
                return df;
            }

            documentoDTO = df4.getData();
            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    documentoDTO.getTokenIdentificador(), false
            );

            HistorialDeFotosFichaIdentificacion historialDeFotosFichaIdentificacion = new HistorialDeFotosFichaIdentificacion();
            historialDeFotosFichaIdentificacion.setFichaIdentificacion(fichaIdentificacion);
            historialDeFotosFichaIdentificacion.setTipo(tipo);
            historialDeFotosFichaIdentificacion.setCarpeta(carpeta);
            historialDeFotosFichaIdentificacion.setDocumento(documento);
            historialDeFotosFichaIdentificacion.setIpCrea(httpServletRequest.getRemoteAddr());
            historialDeFotosFichaIdentificacion.setUsuarioSistemaCrea(usuarioSistema);
            this.historialDeFotosFichaIdentificacionRepository.save(historialDeFotosFichaIdentificacion);

            df.llenarRespuestaExitosa("Se ha subido correctamente al documento.",
                    historialDeFotosFichaIdentificacion.convertirADTO());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>> obtener(
            HttpServletRequest httpServletRequest, HistorialDeFotosFichaIdentificacionRequest historialDeFotosFichaIdentificacionRequest
    ) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(df2.getLogOut());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            Pageable pageable = PageRequest.of(
                    historialDeFotosFichaIdentificacionRequest.getPage(),
                    historialDeFotosFichaIdentificacionRequest.getSize(),
                    Sort.by("id_historial_foto_ficha_identificacion").descending()
            );

            Page<HistorialDeFotosFichaIdentificacion> historialDeFotosFichaIdentificacionPage =
                    this.historialDeFotosFichaIdentificacionRepository.encontrarPorFiltroDeBusqueda(
                            historialDeFotosFichaIdentificacionRequest.getFiltroBusqueda(),
                            historialDeFotosFichaIdentificacionRequest.getTokenIdentificadorFichaDeIdentificacion(),
                            pageable
                    );

            List<HistorialDeFotosFichaIdentificacionDTO> historialDeFotosFichaIdentificacionDTOList = new ArrayList<>();

            List<HistorialDeFotosFichaIdentificacion> historialDeFotosFichaIdentificacionList = historialDeFotosFichaIdentificacionPage.toList();

            for (HistorialDeFotosFichaIdentificacion historialDeFotosFichaIdentificacion : historialDeFotosFichaIdentificacionList) {
                historialDeFotosFichaIdentificacionDTOList.add(
                        historialDeFotosFichaIdentificacion.convertirADTO()
                );
            }

            Long totalElements = historialDeFotosFichaIdentificacionPage.getTotalElements();

            PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setTotalItems(totalElements);
            paginacionResponse.setData(historialDeFotosFichaIdentificacionDTOList);

            df.llenarRespuestaExitosa("Se han encontrado: "
                    + historialDeFotosFichaIdentificacionDTOList.size() + " historiales de fotos de un total de: "
                    + totalElements, paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> eliminarRelacionConElDocumento(
            HttpServletRequest httpServletRequest, HistorialDeFotosFichaIdentificacionDTO historialDeFotosFichaIdentificacionDTO
    ) {

        RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(df2.getLogOut());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            HistorialDeFotosFichaIdentificacion historialDeFotosFichaIdentificacion =
                    this.historialDeFotosFichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                            historialDeFotosFichaIdentificacionDTO.getTokenIdentificador(),
                            false
                    );

            if (historialDeFotosFichaIdentificacion == null) {
                df.setMensaje("El historial no existe o fue eliminado anteriormente");
                return df;
            }

            historialDeFotosFichaIdentificacion.setRemovido(true);
            historialDeFotosFichaIdentificacion.setFechaEliminacion(new Date());
            historialDeFotosFichaIdentificacion.setIpElimina(httpServletRequest.getRemoteAddr());
            historialDeFotosFichaIdentificacion.setUsuarioSistemaElimina(usuarioSistema);
            this.historialDeFotosFichaIdentificacionRepository.save(
                    historialDeFotosFichaIdentificacion
            );

            df.llenarRespuestaExitosa("Se elimino la relación con exito",
                    historialDeFotosFichaIdentificacion.convertirADTO());
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> obtenerFotoPerfil
            (HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<HistorialDeFotosFichaIdentificacionDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(df2.getLogOut());
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            String tokenIdentificador = new Gson().fromJson(bodyString, String.class);

            HistorialDeFotosFichaIdentificacion historialDeFotosFichaIdentificacion =
                    this.historialDeFotosFichaIdentificacionRepository.
                            findFirstByFichaIdentificacionTokenIdentificadorAndTipoNemonicoAndRemovidoOrderByFechaCreacionDesc(
                            tokenIdentificador,
                            "NEMONICO_TIPO_HISTORIAL_ARCHIVOS_FRONTAL",
                            false
                    );

            if(historialDeFotosFichaIdentificacion!=null){
                df.llenarRespuestaExitosa("Foto perfil encontrada", historialDeFotosFichaIdentificacion.convertirADTO());
            }else{
                df.llenarRespuestaExitosa("Foto perfil no encontrada", new HistorialDeFotosFichaIdentificacionDTO());
            }

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
