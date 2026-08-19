package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaDeIdentificacionDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.DocumentoDTOFichaPrincipal;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaDeIdentificacionDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaPrincipalDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaPrincipalDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaDeIdentificacionDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
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
public class FichaIdentificacionDocumentoServiceImpl implements FichaIdentificacionDocumentoService {

    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final LogService logService = new LogService(this.getClass());

    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private FichaDeIdentificacionDocumentoRepository fichaDeIdentificacionDocumentoRepository;
    private CatalogoRepository catalogoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTOFichaPrincipal> subirDocumento(HttpServletRequest httpServletRequest,
                                                                                   FichaPrincipalDocumentoDTO fichaPrincipalDocumentoDTO,
                                                                                   MultipartFile multipartFile) {
        RespuestaPorDefectoAuditoria<DocumentoDTOFichaPrincipal> df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                    fichaPrincipalDocumentoDTO.getTokenIdentificadorFichaPrincipal(), false
            );

            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no existe o fue eliminada anteriormente");
                return df;
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage = this.fichaIdentificacionCarpetaRepository.
                    findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIdentificacion.getTokenIdentificador(),
                            EtiquetaNemonico.CARPETA_GESTION_ADOLES_DOC_PERSONALES,
                            false,
                            pageable
                    );
            DocumentoDTOFichaPrincipal documentoDTOFichaPrincipal = fichaPrincipalDocumentoDTO.getDocumentoDTO();
            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTOFichaPrincipal.getNombre();
            if (fichaIdentificacionCarpetaPage.isEmpty()) {
                df.setMensaje(fallo + ", debido a que no existe la carpeta de documentos personales");
                return df;
            }

            if (fichaIdentificacionCarpetaPage.getTotalElements() > 1) {
                this.logService.warn("Existe mas de una carpeta de : " + EtiquetaNemonico.CARPETA_GESTION_ADOLES_DOC_PERSONALES +
                        " asociada a la ficha de identificacion: "
                        + fichaIdentificacion.getTokenIdentificador());
            }

            CatalogoDTO tipoDocFichaDTO = documentoDTOFichaPrincipal.getTipoDeDocumentoFicha();

            if (tipoDocFichaDTO == null) {
                df.setMensaje("No se recibio un tipo de documento por la ficha principal");
                return df;
            }
            Catalogo tipoDeDocumentoFicha = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    tipoDocFichaDTO.getTokenIdentificador(), false
            );

            if (tipoDeDocumentoFicha == null) {
                df.setMensaje("El tipo de documento por la ficha principal es inválido");
                return df;
            }

            FichaIdentificacionCarpeta fichaIdentificacionCarpeta = fichaIdentificacionCarpetaPage.toList().get(0);
            Carpeta carpeta = fichaIdentificacionCarpeta.getCarpeta();

            String idNode = carpeta.getIdentificadorAlfresco();
            RespuestaPorDefectoAuditoria<DocumentoDTO> df3 = this.documentoService.subirDocumentoAlfresco(
                    httpServletRequest,
                    idNode,
                    multipartFile,
                    documentoDTOFichaPrincipal
            );

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }

            DocumentoDTO documentoAlfrescoDTO = df3.getData();
            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    documentoAlfrescoDTO.getTokenIdentificador(), false
            );

            documentoDTOFichaPrincipal.setTokenIdentificador(documentoAlfrescoDTO.getTokenIdentificador());

            FichaDeIdentificacionDocumento fichaDeIdentificacionDocumento = new FichaDeIdentificacionDocumento();
            fichaDeIdentificacionDocumento.setDocumento(documento);
            fichaDeIdentificacionDocumento.setFichaIdentificacion(fichaIdentificacion);
            fichaDeIdentificacionDocumento.setCarpeta(carpeta);
            fichaDeIdentificacionDocumento.setUsuarioSistemaCrea(usuarioSistema);
            fichaDeIdentificacionDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            fichaDeIdentificacionDocumento.setTipoDeDocumentoFicha(tipoDeDocumentoFicha);
            this.fichaDeIdentificacionDocumentoRepository.save(fichaDeIdentificacionDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTOFichaPrincipal);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaDeIdentificacionDocumentoDTO> editarDocumento(HttpServletRequest httpServletRequest,
                                                                                           FichaDeIdentificacionDocumentoDTO fichaDeIdentificacionDocumentoDTO,
                                                                                           MultipartFile multipartFile) {

        RespuestaPorDefectoAuditoria<FichaDeIdentificacionDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            FichaIdentificacionDTO fichaIdentificacionDTO = fichaDeIdentificacionDocumentoDTO.getFichaIdentificacionDTO();

            if (fichaIdentificacionDTO == null) {
                df.setMensaje("No se recibio la ficha principal");
                return df;
            }

            FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                    fichaIdentificacionDTO.getTokenIdentificador(), false
            );

            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no existe o fue eliminada anteriormente");
                return df;
            }


            FichaDeIdentificacionDocumento fichaDeIdentificacionDocumento = this.fichaDeIdentificacionDocumentoRepository.
                    findByTokenIdentificadorAndRemovido(fichaDeIdentificacionDocumentoDTO.getTokenIdentificador(), false);

            if (fichaDeIdentificacionDocumento == null) {
                df.setMensaje("No puedes continuar con la edición debido a que no existe un registro de este documento subido anteriormente o posiblemente este ya fue borrado");
                return df;
            }

            FichaIdentificacion fichaIdentificacion2 = fichaDeIdentificacionDocumento.getFichaIdentificacion();

            if (!fichaIdentificacion2.getIdFichaIdentificacion().equals(fichaIdentificacion.getIdFichaIdentificacion())) {
                df.setMensaje("No puedes continuar con la edición debido a que: " +
                        "el documento no permite a la ficha principal creada inicialmente");
                return df;
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage = this.fichaIdentificacionCarpetaRepository.
                    findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIdentificacion.getTokenIdentificador(),
                            EtiquetaNemonico.CARPETA_GESTION_ADOLES_DOC_PERSONALES,
                            false,
                            pageable
                    );

            DocumentoDTO documentoDTO = fichaDeIdentificacionDocumentoDTO.getDocumentoDTO();
            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();

            if (fichaIdentificacionCarpetaPage.isEmpty()) {
                df.setMensaje(fallo + ", debido a que no existe la carpeta de documentos personales");
                return df;
            }

            if (fichaIdentificacionCarpetaPage.getTotalElements() > 1) {
                this.logService.warn("Existe mas de una carpeta de : " + EtiquetaNemonico.CARPETA_GESTION_ADOLES_DOC_PERSONALES +
                        " asociada a la ficha de identificacion: "
                        + fichaIdentificacion.getTokenIdentificador());
            }

            CatalogoDTO tipoDocFichaDTO = fichaDeIdentificacionDocumentoDTO.getTipoDeDocumentoFichaDeIdentificacion();

            if (tipoDocFichaDTO == null) {
                df.setMensaje("No se recibio un tipo de documento por la ficha principal");
                return df;
            }
            Catalogo tipoDeDocumentoFicha = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    tipoDocFichaDTO.getTokenIdentificador(), false
            );

            if (tipoDeDocumentoFicha == null) {
                df.setMensaje("El tipo de documento por la ficha principal es inválido");
                return df;
            }

            if (multipartFile != null && !multipartFile.isEmpty()) {
                FichaIdentificacionCarpeta fichaIdentificacionCarpeta = fichaIdentificacionCarpetaPage.toList().get(0);
                Carpeta carpeta = fichaIdentificacionCarpeta.getCarpeta();

                String idNode = carpeta.getIdentificadorAlfresco();
                documentoDTO.setEsEdicion(true);
                RespuestaPorDefectoAuditoria<DocumentoDTO> df3 = this.documentoService.subirDocumentoAlfresco(
                        httpServletRequest,
                        idNode,
                        multipartFile,
                        documentoDTO
                );

                if (!df3.isExito()) {
                    df.setMensaje(df3.getMensaje());
                    df.setMensajeErrorReal(df3.getMensajeErrorReal());
                    return df;
                }

                DocumentoDTO documentoAlfrescoDTO = df3.getData();
                Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                        documentoAlfrescoDTO.getTokenIdentificador(), false
                );

                documentoDTO.setTokenIdentificador(documentoAlfrescoDTO.getTokenIdentificador());

                fichaDeIdentificacionDocumento.setDocumento(documento);
                fichaDeIdentificacionDocumento.setCarpeta(carpeta);

            }

            fichaDeIdentificacionDocumento.setFichaIdentificacion(fichaIdentificacion);
            fichaDeIdentificacionDocumento.setUsuarioSistemaEdita(usuarioSistema);
            fichaDeIdentificacionDocumento.setIpEdita(httpServletRequest.getRemoteAddr());
            fichaDeIdentificacionDocumento.setFechaEdicion(new Date());
            fichaDeIdentificacionDocumento.setTipoDeDocumentoFicha(tipoDeDocumentoFicha);
            this.fichaDeIdentificacionDocumentoRepository.save(fichaDeIdentificacionDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documentoDTO.getNombre(), fichaDeIdentificacionDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaDeIdentificacionDocumentoDTO>>
    obtenerDocumentosDeLaFichaDeIdentificacion(
            HttpServletRequest httpServletRequest, FichaPrincipalDocumentosRequest fichaPrincipalDocumentosRequest) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaDeIdentificacionDocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

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

            FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                    fichaPrincipalDocumentosRequest.getTokenIdentificadorFichaIdentificacion(),
                    false
            );

            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación principal es inválida o fue removida anteriormente");
                return df;
            }

            Pageable pageable = PageRequest.of(fichaPrincipalDocumentosRequest.getPage(),
                    fichaPrincipalDocumentosRequest.getSize(), Sort.by("id_ficha_identificacion_documento").descending());
            Page<FichaDeIdentificacionDocumento> fichaDeIdentificacionDocumentoPage =
                    this.fichaDeIdentificacionDocumentoRepository.encontrarPorFichaIdentificacionYFiltroBuscar(
                            fichaIdentificacion.getTokenIdentificador(),
                            fichaPrincipalDocumentosRequest.getTextoBuscar(),
                            pageable
                    );
            List<FichaDeIdentificacionDocumentoDTO> documentoList = new ArrayList<>();
            List<FichaDeIdentificacionDocumento> fichaDeIdentificacionDocumentos = fichaDeIdentificacionDocumentoPage.toList();


            for (FichaDeIdentificacionDocumento fichaDeIdentificacionDocumento : fichaDeIdentificacionDocumentos) {
                FichaDeIdentificacionDocumentoDTO fichaDeIdentificacionDocumentoDTO = new FichaDeIdentificacionDocumentoDTO();
                Documento documento = fichaDeIdentificacionDocumento.getDocumento();

                fichaDeIdentificacionDocumentoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
                Catalogo tipoDeDocumentoSistema = documento.getTipoDeDocumentoSistema();
                Catalogo tipoDeDocumentoFicha = fichaDeIdentificacionDocumento.getTipoDeDocumentoFicha();
                if (tipoDeDocumentoFicha != null) {
                    fichaDeIdentificacionDocumentoDTO.setTipoDeDocumentoFichaDeIdentificacion(tipoDeDocumentoFicha.convertirADTO());
                }
                CatalogoDTO tipoDeDocumentoSistemaDTO = tipoDeDocumentoSistema.convertirADTO();

                fichaDeIdentificacionDocumentoDTO.setTokenIdentificador(fichaDeIdentificacionDocumento.getTokenIdentificador());
                fichaDeIdentificacionDocumentoDTO.setFechaCreacion(documento.getFechaCreacion());
                fichaDeIdentificacionDocumentoDTO.setDocumentoDTO(documento.convertirADTO());
                fichaDeIdentificacionDocumentoDTO.setFichaIdentificacionDTO(fichaIdentificacion.convertirADTO());

                documentoList.add(fichaDeIdentificacionDocumentoDTO);
            }

            PaginacionResponse<FichaDeIdentificacionDocumentoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(documentoList);
            paginacionResponse.setTotalItems(fichaDeIdentificacionDocumentoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " +
                    fichaDeIdentificacionDocumentoPage.getTotalElements(), paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionDocumentoDTO> eliminarRelacionConDocumento(
            HttpServletRequest httpServletRequest, FichaIdentificacionDocumentoDTO fichaIdentificacionDocumentoDTO
    ) {
        RespuestaPorDefectoAuditoria<FichaIdentificacionDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = fichaIdentificacionDocumentoDTO.getTokenIdentificadorDocumento();
            String tokenFicha = fichaIdentificacionDocumentoDTO.getTokenIdentificadorFichaIdentificacion();

            Pageable pageable = PageRequest.of(0, 2, Sort.by("idFichaIdentificacionDocumento").descending());
            Page<FichaDeIdentificacionDocumento> fichaDeIdentificacionDocumentoPage = this.
                    fichaDeIdentificacionDocumentoRepository.findByFichaIdentificacionTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenFicha,
                            tokenDoc,
                            false, pageable
                    );

            if (!fichaDeIdentificacionDocumentoPage.hasContent()) {
                df.setMensaje("La relación entre el documento y la ficha de identificación no existe o ya fue eliminada anteriormente");
                return df;
            }

            if (fichaDeIdentificacionDocumentoPage.getTotalElements() > 1) {
                this.logService.warn("Existe mas de una relación entre el documento: "
                        + tokenDoc + " y la ficha: " + tokenFicha);
            }

            FichaDeIdentificacionDocumento fichaDeIdentificacionDocumento = fichaDeIdentificacionDocumentoPage.toList().get(0);
            Documento documento = fichaDeIdentificacionDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("La ficha de identificacion no presenta el documento requerido");
                return df;
            }
            fichaDeIdentificacionDocumento.setRemovido(true);
            fichaDeIdentificacionDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            fichaDeIdentificacionDocumento.setFechaEliminacion(new Date());
            fichaDeIdentificacionDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.fichaDeIdentificacionDocumentoRepository.save(fichaDeIdentificacionDocumento);

            df.llenarRespuestaExitosa("Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " de la ficha de identificación",
                    fichaIdentificacionDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
