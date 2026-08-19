package net.latinus.sistema.integral.gestion.seguridad.service.documentos;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.EntryNodeResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.ErrorBody;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.NodeResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;

@Service
@Transactional
@AllArgsConstructor
public class DocumentoServiceImpl implements DocumentoService {

    private CatalogoRepository catalogoRepository;

    private AlfrescoService alfrescoService;
    private EmpresaRepository empresaRepository;
    
    private CarpetaRepository carpetaRepository;

    private DocumentoRepository documentoRepository;

    private JwtProviderService jwtProviderService;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumentoAlfresco(
            HttpServletRequest httpServletRequest, String idNodoAlfresco,
            MultipartFile multipartFile, DocumentoDTO documentoDTO) {

        RespuestaPorDefectoAuditoria<DocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String tokenEmpresa = empresa.getTokenIdentificador();

            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            if (documentoDTO == null) {
                df.setMensaje("Se debe de enviar un body del documento.");
                return df;
            }

            CatalogoDTO tipoDeDocumentoSistemaDTO = documentoDTO.getTipoDocumentoSistema();

            if (tipoDeDocumentoSistemaDTO == null) {
                df.setMensaje("El tipo de documento del sistema es requerido");
                return df;
            }

            Catalogo tipoDeDocumentoSistema = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    tipoDeDocumentoSistemaDTO.getTokenIdentificador(), false
            );

            if (tipoDeDocumentoSistema == null) {
                df.setMensaje("El tipo de documento del sistema con nombre: " + tipoDeDocumentoSistemaDTO.getNombre() + " no exite");
                return df;
            }
            Documento documento;
            
            Carpeta carpeta = this.carpetaRepository.findByIdentificadorAlfrescoAndRemovido(idNodoAlfresco,
                    Boolean.FALSE);

            if (documentoDTO.getEsEdicion() != null && documentoDTO.getEsEdicion()) {
                documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                        documentoDTO.getTokenIdentificador(), false
                );

                if (documento == null) {
                    df.setMensaje("El documento no se puede editar debido a que no existe");
                    return df;
                }

                documento.setUsuarioSistemaEdita(usuarioSistema);
                documento.setIpEdita(httpServletRequest.getRemoteAddr());
                documento.setFechaEdicion(new Date());

                //Editando el nombre del antiguo archivo
                Long date = new Date().getTime();
                String nuevoNombre = "edicion_" + documento.getTokenIdentificador() + "_" + date + "_" + documento.getNombreReal();
                RespuestaPorDefectoAuditoria<NodeResponse> df33 = this.alfrescoService.actualizarMetadataNodo(tokenEmpresa,
                        documento.getIdentificadorAlfresco(), nuevoNombre,
                        "Edicion del documento: " + documento.getTokenIdentificador(), null);

                if (!df33.isExito()) {
                    df.setMensaje(df33.getMensaje());
                    return df;
                }

                ErrorBody error2 = df33.getData().getError();

                if (error2 != null && error2.getErrorKey() != null) {
                    df.setMensaje("Error edicion: " + error2.getBriefSummary());
                    return df;
                }

            } else {
                documento = new Documento();
                documento.setUsuarioSistemaCrea(usuarioSistema);
                documento.setIpCrea(httpServletRequest.getRemoteAddr());
            }
            
            documento.setCarpeta(carpeta);
            documento.setTipoDeDocumentoSistema(tipoDeDocumentoSistema);

            if (tipoDeDocumentoSistema.getNemonico().equals(EtiquetaNemonico.NEMONICO_TIPO_DOCUMENTO_SISTEMA_OTROS)) {
                documento.setTipoDeDocumentoSistemaOtro(documentoDTO.getTipoDeDocumentoSistemaOtro());
            }

            Resource resource = multipartFile.getResource();
            RespuestaPorDefectoAuditoria<NodeResponse> df3 = this.alfrescoService.subirArchivo(tokenEmpresa,
                    idNodoAlfresco,
                    resource, "Subida de documento: " + documentoDTO.getNombre(),
                    documentoDTO.getDescripcion(), null);

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                return df;
            }

            ErrorBody error = df3.getData().getError();

            if (error != null && error.getErrorKey() != null) {
                df.setMensaje(error.getBriefSummary());
                return df;
            }

            EntryNodeResponse entryNodeResponse = df3.getData().getEntry();

            //Guardando los datos del archivo subido a alfresco
            documento.setIdentificadorAlfresco(entryNodeResponse.getId());
            documento.setMimeType(multipartFile.getContentType());
            documento.setNombreReal(resource.getFilename());
            documento.setTamanioByteDocumento(resource.contentLength());
            documento.setEmpresa(
                    empresa
            );
            documento.setDescripcion(documentoDTO.getDescripcion());

            this.documentoRepository.save(documento);

            documentoDTO.setTokenIdentificador(documento.getTokenIdentificador());
            documentoDTO.setFechaCreacion(documento.getFechaCreacion());
            documentoDTO.setNombre(documento.getNombreReal());
            documentoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String textofinal = "subio";
            if (documentoDTO.getEsEdicion() != null && documentoDTO.getEsEdicion()) {
                textofinal = "actualizo";
            }
            df.llenarRespuestaExitosa("Se " + textofinal + " con éxito el documento: "
                    + documentoDTO.getNombre(), documentoDTO);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Resource> obtenerDocumentoFisico(HttpServletRequest httpServletRequest, String tokenIdentificadorDocumento) {
        RespuestaPorDefectoAuditoria<Resource> df = new RespuestaPorDefectoAuditoria<>();

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

            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    tokenIdentificadorDocumento, false
            );

            if (documento == null) {
                df.setMensaje("El documento es inválido o ya fue borrado anteriormente");
                return df;
            }

            String idNodoAlfresco = documento.getIdentificadorAlfresco();

            RespuestaPorDefectoAuditoria<Resource> df3 = this.alfrescoService.obtenerDocumento(
                    empresa.getTokenIdentificador(),
                    idNodoAlfresco
            );

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            df.llenarRespuestaExitosa("Se ha obtenido con exito el documento: "
                    + documento.getNombreReal(), (Resource) df3.getData());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTO> eliminarDocumento(HttpServletRequest httpServletRequest,
                                                                        String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if (documento == null) {
                df.setMensaje("El documento no existe o fue eliminado anteriormente");
                return df;
            }

            String idNodoAlfresco = documento.getIdentificadorAlfresco();

            if (idNodoAlfresco == null || idNodoAlfresco.isEmpty()) {
                df.setMensaje("El documento tiene un identificador de alfresco inválido");
                return df;
            }
            String nombreRemovido = "removido_" + documento.getTokenIdentificador() + "_" + documento.getNombreReal();
            RespuestaPorDefectoAuditoria<NodeResponse> df3 = this.alfrescoService.actualizarMetadataNodo(
                    empresa.getTokenIdentificador(), idNodoAlfresco,
                    nombreRemovido, "Removiendo el archivo: " + documento.getNombreReal(),
                    "El archivo: " +
                            documento.getNombreReal() + " no se remueve del servidor de alfresco solo cambia el nombre"
            );

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            NodeResponse nodeResponse = df3.getData();

            if (nodeResponse == null || nodeResponse.getEntry() == null || nodeResponse.getEntry().getId().isEmpty()) {
                df.setMensaje("No se pudo realizar la operación correctamente en alfresco");
                return df;
            }

            documento.setRemovido(true);
            documento.setFechaEliminacion(new Date());
            documento.setIpElimina(httpServletRequest.getRemoteAddr());
            documento.setUsuarioSistemaElimina(usuarioSistema);

            this.documentoRepository.save(documento);

            df.llenarRespuestaExitosa("Se ha elimanado correctamente el documento con nombre: " +
                    documento.getNombreReal(), documento.convertirADTO());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTO> actualizardocumento(HttpServletRequest httpServletRequest,
                                                                          MultipartFile multipartFile, DocumentoDTO documentoDTO) {

        RespuestaPorDefectoAuditoria<DocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    documentoDTO.getTokenIdentificador(), false
            );

            if (documento == null) {
                df.setMensaje("El documento a actualizar no existe o se elimino anteriormente");
                return df;
            }
            String idNodo = documento.getIdentificadorAlfresco();

            RespuestaPorDefectoAuditoria<NodeResponse> df3 = this.alfrescoService.getMetadaData(
                    empresa.getTokenIdentificador(), idNodo);
            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            NodeResponse nodeResponse = df3.getData();

            ErrorBody errorBody = nodeResponse.getError();
            if (errorBody != null && errorBody.getErrorKey() != null) {
                df.setMensaje("Ha ocurrido el siguiente error: " + errorBody.getBriefSummary());
                df.setMensajeErrorReal(errorBody.toString());
                return df;
            }

            idNodo = nodeResponse.getEntry().getParentId();

            df = this.subirDocumentoAlfresco(httpServletRequest, idNodo, multipartFile,
                    documentoDTO);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

}
