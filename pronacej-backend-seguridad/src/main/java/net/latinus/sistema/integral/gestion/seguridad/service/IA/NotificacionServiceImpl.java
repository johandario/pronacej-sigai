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
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.Notificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.NotificacionDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.NotificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.NotificacionDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.NotificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.UtilsService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private CarpetaService carpetaService;
    private DocumentoService documentoService;
    private CarpetaRepository carpetaRepository;
    private EmailService emailService;
    private PaginacionService paginacionService;
    private NotificacionRepository notificacionRepository;
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private NotificacionDocumentoRepository notificacionDocumentoRepository;
    private DocumentoRepository documentoRepository;

    private final LogService logService = new LogService(this.getClass());
    private UtilsService utilsService;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<NotificacionDTO>> obtenerNotificacionesPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<NotificacionDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaNotificaciones = notificacionRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            List<NotificacionDTO> notificacionDTOList = new ArrayList<>();
            for (Notificacion notificacion : listaNotificaciones) {

                NotificacionDTO notificacionDTO = new NotificacionDTO();
                notificacionDTO.setIdNotificacion(notificacion.getIdNotificacion());
                notificacionDTO.setRemitente(notificacion.getRemitente());
                notificacionDTO.setDestinatarios(notificacion.getDestinatario());
                notificacionDTO.setCuerpo(notificacion.getCuerpo());
                notificacionDTO.setAsunto(notificacion.getAsunto());
                notificacionDTO.setTipo(notificacion.getTipo().getNemonico());
                notificacionDTO.setMedio(notificacion.getMedio().getNemonico());
                notificacionDTO.setAdolescente(notificacion.getFichaIdentificacion().getIdFichaIdentificacion());
                notificacionDTO.setFechaCreacion(notificacion.getFechaCreacion());
                notificacionDTO.setObservacionesEntrega(notificacion.getObservacionesEntrega());
                notificacionDTO.setEntregado(notificacion.getEntregado());
                notificacionDTO.setFechaEntrega(notificacion.getFechaEntrega());
                notificacionDTO.setTokenIdentificador(notificacion.getTokenIdentificador());

                if (notificacion.getFichaIdentificacion() != null) {
                    notificacionDTO.setTokenFichaIdentificacion(notificacion.getFichaIdentificacion().getTokenIdentificador());
                }

                notificacionDTOList.add(notificacionDTO);
            }

            notificacionDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            notificacionDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            PaginacionResponse<NotificacionDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse = paginacionService.obtenerDatos(notificacionDTOList, paginacionRequest);

            respuesta.llenarRespuestaExitosa("Notificaciones", paginacionResponse);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<NotificacionDTO> enviarNotificacion(HttpServletRequest httpServletRequest,
                                                                            BodyEncriptado bodyEncriptado,
                                                                            MultipartFile[] multipartFiles) {

        RespuestaPorDefectoAuditoria<NotificacionDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
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
            String bodyDesencriptado = df22.getData();
            NotificacionDTO notificacionDTO = new Gson().fromJson(bodyDesencriptado, NotificacionDTO.class);

            this.logService.info(notificacionDTO.toString());

            if(notificacionDTO.getMedio().equals("MEDIO_CORREO")) {
                RespuestaPorDefectoAuditoria<Boolean> df3 = this.emailService.enviarCorreo(
                        Arrays.asList(notificacionDTO.getDestinatarios().split(",")),
                        notificacionDTO.getAsunto(),
                        notificacionDTO.getCuerpo(),
                        empresa.getTokenIdentificador(),
                        "text/html",
                        multipartFiles
                );

                if (!df3.isExito()) {
                    respuesta.setMensaje(df3.getMensaje());
                    respuesta.setMensajeErrorReal(df3.getMensajeErrorReal());
                    return respuesta;
                }
            }

            List<DocumentoDTO> documentoDTOList = notificacionDTO.getDocumentoDTOList();

            //Creacion de la noticiacion
            Notificacion notificacion = new Notificacion();
            notificacion.setAsunto(notificacionDTO.getAsunto());
            notificacion.setCuerpo(notificacionDTO.getCuerpo());

            Catalogo catalogoMedio = catalogoRepository.findByNemonicoAndRemovido(notificacionDTO.getMedio(), false);
            notificacion.setMedio(catalogoMedio);

            Catalogo catalogoTipo = catalogoRepository.findByNemonicoAndRemovido(notificacionDTO.getTipo(), false);
            notificacion.setTipo(catalogoTipo);

            notificacion.setDestinatario(notificacionDTO.getDestinatarios());
            notificacion.setRemitente(notificacionDTO.getRemitente());
            notificacion.setEmpresa(empresa);

            FichaIdentificacion ficha = fichaIdentificacionRepository.findByIdFichaIdentificacion(notificacionDTO.getAdolescente());

            if (ficha == null || ficha.getTokenIdentificador() == null) {
                respuesta.setMensaje("Se recibio una ficha de identificación inválida");
                return respuesta;
            }

            notificacion.setFichaIdentificacion(ficha);

            notificacion.setObservacionesEntrega(notificacionDTO.getObservacionesEntrega());
            notificacion.setEntregado(notificacionDTO.getEntregado());
            notificacion.setFechaEntrega(notificacionDTO.getFechaEntrega());
            Notificacion notificacionCreada = this.notificacionRepository.save(notificacion);

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            String nemonicoCarpeta = EtiquetaNemonico.CARPETA_GESTION_ADOLES_NOTIFICACIONES;
            Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage = this.fichaIdentificacionCarpetaRepository.
                    findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            ficha.getTokenIdentificador(),
                            nemonicoCarpeta,
                            false,
                            pageable
                    );

            if (fichaIdentificacionCarpetaPage.isEmpty()) {
                respuesta.setMensaje("No se ha creado una carpeta para guardar las notificaciones que pertenezca a la ficha de identificación solicitada");
                return respuesta;
            }

            if (fichaIdentificacionCarpetaPage.getTotalElements() > 1) {
                this.logService.warn("La ficha de identificacion: " +
                        ficha.getTokenIdentificador() + " tiene mas de una carpeta: " +
                        nemonicoCarpeta
                );
            }

            FichaIdentificacionCarpeta fichaIdentificacionCarpeta = fichaIdentificacionCarpetaPage.toList().get(0);

            Carpeta carpeta = fichaIdentificacionCarpeta.getCarpeta();

            String idNodo = carpeta.getIdentificadorAlfresco();

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

                    NotificacionDocumento notificacionDocumento = new NotificacionDocumento();
                    notificacionDocumento.setNotificacion(notificacionCreada);
                    notificacionDocumento.setCarpeta(carpeta);
                    notificacionDocumento.setDocumento(documento);
                    notificacionDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                    notificacionDocumento.setUsuarioSistemaCrea(usuarioSistema);
                    notificacionDocumentoRepository.save(notificacionDocumento);
                }
            }

            notificacionDTO.setTokenIdentificador(notificacion.getTokenIdentificador());

            respuesta.llenarRespuestaExitosa("Se ha enviado una notificación a los emails: "
                    + notificacionDTO.getDestinatarios().toString() + " correctamente", notificacionDTO);


        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
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
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

            Page<NotificacionDocumento> documentosPage;


            documentosPage = this.notificacionDocumentoRepository.findByNotificacionTokenIdentificadorAndRemovido(
                    paginacionRequest.getTokenIdentificador(),
                    false,
                    pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (NotificacionDocumento notDoc : documentosPage.toList()) {
                Documento documento = notDoc.getDocumento();
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
}
