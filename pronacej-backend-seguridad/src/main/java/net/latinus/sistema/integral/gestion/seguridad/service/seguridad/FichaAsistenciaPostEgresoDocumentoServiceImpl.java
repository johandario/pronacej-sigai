package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaAsistenciaPostEgresoDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaAsistenciaPostEgresoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaAsistenciaPostEgresoDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class FichaAsistenciaPostEgresoDocumentoServiceImpl implements FichaAsistenciaPostEgresoDocumentoService {
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private FichaAsistenciaPostEgresoRepository fichaAsistenciaPostEgresoRepository;
    private FichaAsistenciaPostEgresoCarpetaRepository fichaAsistenciaPostEgresoCarpetaRepository;
    private FichaAsistenciaPostEgresoDocumentoRepository fichaAsistenciaPostEgresoDocumentoRepository;

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
            FichaAsistenciaPostEgresoDocumentoDTO fichaAsistenciaPostEgresoDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaAsistenciaPostEgresoDocumentoDTO.class);

            FichaAsistenciaPostEgreso fichaAsistenciaPostEgreso = this.fichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(
                    fichaAsistenciaPostEgresoDocumentoDTO.getTokenIdentificadorFichaAsistenciaPostEgreso(), false
            );

            if (fichaAsistenciaPostEgreso == null) {
                df.setMensaje("No existe el registro solicitado");
                return df;
            }

            FichaAsistenciaPostEgresoCarpeta fichaAsistenciaPostEgresoCarpeta = this.fichaAsistenciaPostEgresoCarpetaRepository.findFirstByFichaAsistenciaPostEgresoTokenIdentificadorAndRemovido(fichaAsistenciaPostEgreso.getTokenIdentificador(), false);

            DocumentoDTO documentoDTO = fichaAsistenciaPostEgresoDocumentoDTO.getDocumentoDTO();
            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();
            if (fichaAsistenciaPostEgresoCarpeta == null) {
                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
                return df;
            }

            Carpeta carpeta = fichaAsistenciaPostEgresoCarpeta.getCarpeta();

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

            FichaAsistenciaPostEgresoDocumento fichaAsistenciaPostEgresoDocumento = new FichaAsistenciaPostEgresoDocumento();
            fichaAsistenciaPostEgresoDocumento.setDocumento(documento);
            fichaAsistenciaPostEgresoDocumento.setFichaAsistenciaPostEgreso(fichaAsistenciaPostEgreso);
            fichaAsistenciaPostEgresoDocumento.setCarpeta(carpeta);
            fichaAsistenciaPostEgresoDocumento.setUsuarioSistemaCrea(usuarioSistema);
            fichaAsistenciaPostEgresoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.fichaAsistenciaPostEgresoDocumentoRepository.save(fichaAsistenciaPostEgresoDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, FichaAsistenciaPostEgresoDocumentosRequest fichaAsistenciaPostEgresoDocumentosRequest) {
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

            FichaAsistenciaPostEgreso fichaAsistenciaPostEgreso = this.fichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(
                    fichaAsistenciaPostEgresoDocumentosRequest.getTokenIdentificadorFichaAsistenciaPostEgreso(),
                    false
            );

            if (fichaAsistenciaPostEgreso == null) {
                df.setMensaje("El registro es inválido");
                return df;
            }

            Pageable pageable = PageRequest.of(fichaAsistenciaPostEgresoDocumentosRequest.getPage(),
                    fichaAsistenciaPostEgresoDocumentosRequest.getSize());
            Page<FichaAsistenciaPostEgresoDocumento> fichaAsistenciaPostEgresoDocumentoPage =
                    this.fichaAsistenciaPostEgresoDocumentoRepository.findByFichaAsistenciaPostEgresoTokenIdentificadorAndRemovido(
                            fichaAsistenciaPostEgreso.getTokenIdentificador(),
                            false,
                            pageable
                    );
            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<FichaAsistenciaPostEgresoDocumento> fichaAsistenciaPostEgresoDocumentos = fichaAsistenciaPostEgresoDocumentoPage.toList();


            for (FichaAsistenciaPostEgresoDocumento fichaAsistenciaPostEgresoDocumento : fichaAsistenciaPostEgresoDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();
                Documento documento = fichaAsistenciaPostEgresoDocumento.getDocumento();

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
            paginacionResponse.setTotalItems(fichaAsistenciaPostEgresoDocumentoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " + fichaAsistenciaPostEgresoDocumentoPage.getTotalElements(), paginacionResponse);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, FichaAsistenciaPostEgresoDocumentoDTO fichaAsistenciaPostEgresoDocumentoDTO) {
        RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = fichaAsistenciaPostEgresoDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = fichaAsistenciaPostEgresoDocumentoDTO.getTokenIdentificadorFichaAsistenciaPostEgreso();

            FichaAsistenciaPostEgresoDocumento fichaAsistenciaPostEgresoDocumento = this.
                    fichaAsistenciaPostEgresoDocumentoRepository.findFirstByFichaAsistenciaPostEgresoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenDetalle,
                            tokenDoc,
                            false
                    );

            if (fichaAsistenciaPostEgresoDocumento == null) {
                df.setMensaje("La relación entre el documento y el registro pertenencia no existe o ya fue eliminada anteriormente");
                return df;
            }

            Documento documento = fichaAsistenciaPostEgresoDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }
            fichaAsistenciaPostEgresoDocumento.setRemovido(true);
            fichaAsistenciaPostEgresoDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            fichaAsistenciaPostEgresoDocumento.setFechaEliminacion(new Date());
            fichaAsistenciaPostEgresoDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.fichaAsistenciaPostEgresoDocumentoRepository.save(fichaAsistenciaPostEgresoDocumento);

            df.llenarRespuestaExitosa("Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " del registro",
                    fichaAsistenciaPostEgresoDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
