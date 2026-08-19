package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.PertenenciaCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedicaCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedicaDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaMedicaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FichaMedicaDocumentoServiceImpl implements FichaMedicaDocumentoService {
    private final JwtProviderService jwtProviderService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    private final DocumentoService documentoService;
    private final DocumentoRepository documentoRepository;
    private final FichaMedicaRepository fichaMedicaRepository;
    private final FichaMedicaCarpetaRepository fichaMedicaCarpetaRepository;
    private final FichaMedicaDocumentoRepository fichaMedicaDocumentoRepository;

    private final FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private final CarpetaService carpetaService;
    private final CarpetaRepository carpetaRepository;

    private final CatalogoRepository catalogoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, MultipartFile multipartFile) {
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            FichaMedicaDocumentoDTO fichaMedicaDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaMedicaDocumentoDTO.class);

            FichaMedica fichaMedica = this.fichaMedicaRepository.findByTokenIdentificadorAndRemovido(
                    fichaMedicaDocumentoDTO.getTokenIdentificadorFichaMedica(), false
            );

            if (fichaMedica == null) {
                df.setMensaje("No existe el registro solicitado");
                return df;
            }

            this.crearCarpetaPrincipal(httpServletRequest, fichaMedica, usuarioSistema);

            FichaMedicaCarpeta registroCarpeta = this.fichaMedicaCarpetaRepository.findFirstByFichaMedicaTokenIdentificadorAndRemovido(fichaMedica.getTokenIdentificador(), false);

            DocumentoDTO documentoDTO = fichaMedicaDocumentoDTO.getDocumentoDTO();
            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();
            if (registroCarpeta == null) {
                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
                return df;
            }

            Carpeta carpeta = registroCarpeta.getCarpeta();

            String idNode = carpeta.getIdentificadorAlfresco();
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

            documentoDTO = df3.getData();
            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    documentoDTO.getTokenIdentificador(), false
            );

            FichaMedicaDocumento fichaMedicaDocumento = new FichaMedicaDocumento();
            fichaMedicaDocumento.setDocumento(documento);
            fichaMedicaDocumento.setFichaMedica(fichaMedica);
            fichaMedicaDocumento.setCarpeta(carpeta);
            fichaMedicaDocumento.setUsuarioSistemaCrea(usuarioSistema);
            fichaMedicaDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.fichaMedicaDocumentoRepository.save(fichaMedicaDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, FichaMedicaDocumentosRequest fichaMedicaDocumentosRequest) {
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

            FichaMedica fichaMedica = this.fichaMedicaRepository.findByTokenIdentificadorAndRemovido(
                    fichaMedicaDocumentosRequest.getTokenIdentificadorFichaMedica(),
                    false
            );

            if (fichaMedica == null) {
                df.setMensaje("El registro es inválido");
                return df;
            }

            Pageable pageable = PageRequest.of(fichaMedicaDocumentosRequest.getPage(),
                    fichaMedicaDocumentosRequest.getSize());
            Page<FichaMedicaDocumento> fichaMedicaDocumentoPage;
            if (fichaMedicaDocumentosRequest.getTextoBuscar() != null && !fichaMedicaDocumentosRequest.getTextoBuscar().isEmpty()) {
                String textoABuscar = fichaMedicaDocumentosRequest.getTextoBuscar().trim().toLowerCase();
                fichaMedicaDocumentoPage = this.fichaMedicaDocumentoRepository.obtenerDocumentosConFiltro(
                        fichaMedica.getTokenIdentificador(),
                        textoABuscar,
                        false,
                        pageable
                );
            } else {
                fichaMedicaDocumentoPage = this.fichaMedicaDocumentoRepository.findByFichaMedicaTokenIdentificadorAndRemovidoOrderByFechaCreacionDesc(
                        fichaMedica.getTokenIdentificador(),
                        false,
                        pageable
                );
            }
            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<FichaMedicaDocumento> fichaMedicaDocumentos = fichaMedicaDocumentoPage.toList();


            for (FichaMedicaDocumento fichaMedicaDocumento : fichaMedicaDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();
                Documento documento = fichaMedicaDocumento.getDocumento();

                documentoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
                Catalogo tipoDeDocumentoSistema = documento.getTipoDeDocumentoSistema();
                CatalogoDTO tipoDeDocumentoSistemaDTO = tipoDeDocumentoSistema.convertirADTO();

                documentoDTO.setTipoDocumentoSistema(tipoDeDocumentoSistemaDTO);
                documentoDTO.setNombre(documento.getNombreReal());
                documentoDTO.setTokenIdentificador(documento.getTokenIdentificador());
                documentoDTO.setDescripcion(documento.getDescripcion());
                documentoDTO.setFechaCreacion(documento.getFechaCreacion());
                documentoDTO.setMimeType(documento.getMimeType());
                documentoDTO.setTamanioBytes(documento.getTamanioByteDocumento());
                documentoDTO.setTipoDeDocumentoSistemaOtro(documento.getTipoDeDocumentoSistemaOtro());

                documentoList.add(documentoDTO);
            }

            PaginacionResponse paginacionResponse = new PaginacionResponse();
            paginacionResponse.setData(documentoList);
            paginacionResponse.setTotalItems(fichaMedicaDocumentoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " + fichaMedicaDocumentoPage.getTotalElements(), paginacionResponse);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaMedicaDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, FichaMedicaDocumentoDTO fichaMedicaDocumentoDTO) {
        RespuestaPorDefectoAuditoria<FichaMedicaDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = fichaMedicaDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = fichaMedicaDocumentoDTO.getTokenIdentificadorFichaMedica();

            FichaMedicaDocumento fichaMedicaDocumento = this.
                    fichaMedicaDocumentoRepository.findFirstByFichaMedicaTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenDetalle,
                            tokenDoc,
                            false
                    );

            if (fichaMedicaDocumento == null) {
                df.setMensaje("La relación entre el documento y el registro fichaMedica no existe o ya fue eliminada anteriormente");
                return df;
            }

            Documento documento = fichaMedicaDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }
            fichaMedicaDocumento.setRemovido(true);
            fichaMedicaDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            fichaMedicaDocumento.setFechaEliminacion(new Date());
            fichaMedicaDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.fichaMedicaDocumentoRepository.save(fichaMedicaDocumento);

            df.llenarRespuestaExitosa("Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " del registro",
                    fichaMedicaDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private void crearCarpetaPrincipal(HttpServletRequest httpServletRequest, FichaMedica fichaMedica, UsuarioSistema usuarioSistema) {
        FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(fichaMedica.getFichaIdentificacion().getTokenIdentificador(), null, false);
        Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

        String nemonicoPertenencia = EtiquetaNemonico.CARPETA_GESTION_HISTORIA_CLINICA;
        FichaIdentificacionCarpeta fichaIdentificacionCarpetaFichaMedica = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(fichaMedica.getFichaIdentificacion().getTokenIdentificador(), nemonicoPertenencia, false);

        if (fichaIdentificacionCarpetaFichaMedica == null) {
            String nombreCarpetaPrincipal = "Historia Clínica";

            CarpetaDTO carpetaDTO = new CarpetaDTO();
            carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
            carpetaDTO.setDescripcion("Carpeta de documentos en historia clínica");
            CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
            carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
            carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

            this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

            Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

            fichaIdentificacionCarpetaFichaMedica = new FichaIdentificacionCarpeta();
            fichaIdentificacionCarpetaFichaMedica.setCarpeta(carpetaGuardadaRecientemente);
            fichaIdentificacionCarpetaFichaMedica.setFichaIdentificacion(fichaMedica.getFichaIdentificacion());
            Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoPertenencia, false);
            fichaIdentificacionCarpetaFichaMedica.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
            fichaIdentificacionCarpetaFichaMedica.setFechaCreacion(new Date());
            fichaIdentificacionCarpetaFichaMedica.setIpCrea(httpServletRequest.getRemoteAddr());
            fichaIdentificacionCarpetaFichaMedica.setUsuarioSistemaCrea(usuarioSistema);
            fichaIdentificacionCarpetaFichaMedica = this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaFichaMedica);
        }

        Carpeta carpetaPadreFichaMedica = fichaIdentificacionCarpetaFichaMedica.getCarpeta();

        String pattern = "yyyy-MM-dd-HH:mm:ss";
        DateFormat fecha = new SimpleDateFormat(pattern);
        String nombreCarpeta = fecha.format(new Date());

        CarpetaDTO carpetaDTO = new CarpetaDTO();
        carpetaDTO.setNombreCliente(nombreCarpeta);
        carpetaDTO.setDescripcion("Carpeta de historia clínica en fecha " + nombreCarpeta);
        CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
        carpetaPadreDTO.setTokenIdentificador(carpetaPadreFichaMedica.getTokenIdentificador());
        carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

        this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

        Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

        FichaMedicaCarpeta carpetaDetalle = new FichaMedicaCarpeta();
        carpetaDetalle.setCarpeta(carpetaGuardada);
        carpetaDetalle.setFichaMedica(fichaMedica);
        carpetaDetalle.setFechaCreacion(new Date());
        carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
        carpetaDetalle.setUsuarioSistemaCrea(usuarioSistema);
        this.fichaMedicaCarpetaRepository.save(carpetaDetalle);
    }
}
