package net.latinus.sistema.integral.gestion.seguridad.service.IA;

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
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.ExpedienteMatrizDetalleDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ExpedienteMatrizDetalleCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ExpedienteMatrizDetalleDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ExpedienteMatrizDetalleRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
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
public class ExpedienteMatrizDetalleDocumentoServiceImpl implements ExpedienteMatrizDetalleDocumentoService {
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private ExpedienteMatrizDetalleRepository expedienteMatrizDetalleRepository;
    private ExpedienteMatrizDetalleCarpetaRepository expedienteMatrizDetalleCarpetaRepository;
    private ExpedienteMatrizDetalleDocumentoRepository expedienteMatrizDetalleDocumentoRepository;

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
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();

            ExpedienteMatrizDetalleDocumentoDTO expedienteMatrizDetalleDocumentoDTO = new Gson().fromJson(bodyDesencriptado, ExpedienteMatrizDetalleDocumentoDTO.class);

            ExpedienteMatrizDetalle expedienteMatrizDetalle = this.expedienteMatrizDetalleRepository.findByTokenIdentificadorAndRemovido(
                    expedienteMatrizDetalleDocumentoDTO.getTokenIdentificadorExpedienteDetalle(), false
            );

            if (expedienteMatrizDetalle == null) {
                df.setMensaje("No existe el detalle solicitado");
                return df;
            }

            ExpedienteMatrizDetalleCarpeta detalleCarpeta = this.expedienteMatrizDetalleCarpetaRepository.findFirstByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(expedienteMatrizDetalle.getTokenIdentificador(), false);

            DocumentoDTO documentoDTO = expedienteMatrizDetalleDocumentoDTO.getDocumentoDTO();
            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();
            if (detalleCarpeta == null) {
                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
                return df;
            }

            Carpeta carpeta = detalleCarpeta.getCarpeta();

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

            ExpedienteMatrizDetalleDocumento expedienteMatrizDetalleDocumento = new ExpedienteMatrizDetalleDocumento();
            expedienteMatrizDetalleDocumento.setDocumento(documento);
            expedienteMatrizDetalleDocumento.setExpedienteMatrizDetalle(expedienteMatrizDetalle);
            expedienteMatrizDetalleDocumento.setCarpeta(carpeta);
            expedienteMatrizDetalleDocumento.setUsuarioSistemaCrea(usuarioSistema);
            expedienteMatrizDetalleDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.expedienteMatrizDetalleDocumentoRepository.save(expedienteMatrizDetalleDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, ExpedienteMatrizDetalleDocumentosRequest expedienteMatrizDetalleDocumentosRequest) {

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

                ExpedienteMatrizDetalle expedienteDetalle = this.expedienteMatrizDetalleRepository.findByTokenIdentificadorAndRemovido(
                        expedienteMatrizDetalleDocumentosRequest.getTokenIdentificadorExpedienteDetalle(),
                        false
                );

                if (expedienteDetalle == null) {
                    df.setMensaje("El detalle es inválido");
                    return df;
                }

                Pageable pageable = PageRequest.of(expedienteMatrizDetalleDocumentosRequest.getPage(),
                        expedienteMatrizDetalleDocumentosRequest.getSize());
                Page<ExpedienteMatrizDetalleDocumento> expedienteMatrizDetalleDocumentoPage =
                        this.expedienteMatrizDetalleDocumentoRepository.findByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(
                                expedienteDetalle.getTokenIdentificador(),
                                false,
                                pageable
                        );
                List<DocumentoDTO> documentoList = new ArrayList<>();
                List<ExpedienteMatrizDetalleDocumento> expedienteMatrizDetalleDocumentos = expedienteMatrizDetalleDocumentoPage.toList();


                for (ExpedienteMatrizDetalleDocumento expedienteMatrizDetalleDocumento : expedienteMatrizDetalleDocumentos) {
                    DocumentoDTO documentoDTO = new DocumentoDTO();
                    Documento documento = expedienteMatrizDetalleDocumento.getDocumento();

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
                paginacionResponse.setTotalItems(expedienteMatrizDetalleDocumentoPage.getTotalElements());

                df.llenarRespuestaExitosa("Se han encontrado: " +
                        documentoList.size() + " documentos, de un total de: " + expedienteMatrizDetalleDocumentoPage.getTotalElements(), paginacionResponse);


            } catch (Exception ex) {
                df.llenarConDatosDeException(ex);
            }

            return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDocumentoDTO> eliminarRelacionConDocumento(
            HttpServletRequest httpServletRequest, ExpedienteMatrizDetalleDocumentoDTO expedienteMatrizDetalleDocumentoDTO
    ) {
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = expedienteMatrizDetalleDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = expedienteMatrizDetalleDocumentoDTO.getTokenIdentificadorExpedienteDetalle();

            ExpedienteMatrizDetalleDocumento expedienteDetalleDocumento = this.
                    expedienteMatrizDetalleDocumentoRepository.findFirstByExpedienteMatrizDetalleTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenDetalle,
                            tokenDoc,
                            false
                    );

            if (expedienteDetalleDocumento == null) {
                df.setMensaje("La relación entre el documento y el detalle no existe o ya fue eliminada anteriormente");
                return df;
            }

            Documento documento = expedienteDetalleDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }
            expedienteDetalleDocumento.setRemovido(true);
            expedienteDetalleDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            expedienteDetalleDocumento.setFechaEliminacion(new Date());
            expedienteDetalleDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.expedienteMatrizDetalleDocumentoRepository.save(expedienteDetalleDocumento);

            df.llenarRespuestaExitosa("Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " del detalle",
                    expedienteMatrizDetalleDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
