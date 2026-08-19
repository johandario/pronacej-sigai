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
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PertenenciaDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PertenenciaCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PertenenciaDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PertenenciaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
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
public class PertenenciaDocumentoServiceImpl implements PertenenciaDocumentoService {
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private PertenenciaRepository pertenenciaRepository;
    private PertenenciaCarpetaRepository pertenenciaCarpetaRepository;
    private PertenenciaDocumentoRepository pertenenciaDocumentoRepository;

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
            PertenenciaDocumentoDTO pertenenciaDocumentoDTO = new Gson().fromJson(bodyDesencriptado, PertenenciaDocumentoDTO.class);

            Pertenencia pertenencia = this.pertenenciaRepository.findByTokenIdentificadorAndRemovido(
                    pertenenciaDocumentoDTO.getTokenIdentificadorPertenencia(), false
            );

            if (pertenencia == null) {
                df.setMensaje("No existe el registro solicitado");
                return df;
            }

            PertenenciaCarpeta registroCarpeta = this.pertenenciaCarpetaRepository.findFirstByPertenenciaTokenIdentificadorAndRemovido(pertenencia.getTokenIdentificador(), false);

            DocumentoDTO documentoDTO = pertenenciaDocumentoDTO.getDocumentoDTO();
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

            PertenenciaDocumento pertenenciaDocumento = new PertenenciaDocumento();
            pertenenciaDocumento.setDocumento(documento);
            pertenenciaDocumento.setPertenencia(pertenencia);
            pertenenciaDocumento.setCarpeta(carpeta);
            pertenenciaDocumento.setUsuarioSistemaCrea(usuarioSistema);
            pertenenciaDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.pertenenciaDocumentoRepository.save(pertenenciaDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, PertenenciaDocumentosRequest pertenenciaDocumentosRequest) {
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

            Pertenencia pertenencia = this.pertenenciaRepository.findByTokenIdentificadorAndRemovido(
                    pertenenciaDocumentosRequest.getTokenIdentificadorPertenencia(),
                    false
            );

            if (pertenencia == null) {
                df.setMensaje("El registro es inválido");
                return df;
            }

            Pageable pageable = PageRequest.of(pertenenciaDocumentosRequest.getPage(),
                    pertenenciaDocumentosRequest.getSize());
            Page<PertenenciaDocumento> pertenenciaDocumentoPage =
                    this.pertenenciaDocumentoRepository.findByPertenenciaTokenIdentificadorAndRemovido(
                            pertenencia.getTokenIdentificador(),
                            false,
                            pageable
                    );
            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<PertenenciaDocumento> pertenenciaDocumentos = pertenenciaDocumentoPage.toList();


            for (PertenenciaDocumento pertenenciaDocumento : pertenenciaDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();
                Documento documento = pertenenciaDocumento.getDocumento();

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
            paginacionResponse.setTotalItems(pertenenciaDocumentoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " + pertenenciaDocumentoPage.getTotalElements(), paginacionResponse);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PertenenciaDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, PertenenciaDocumentoDTO pertenenciaDocumentoDTO) {
        RespuestaPorDefectoAuditoria<PertenenciaDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = pertenenciaDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = pertenenciaDocumentoDTO.getTokenIdentificadorPertenencia();

            PertenenciaDocumento pertenenciaDocumento = this.
                    pertenenciaDocumentoRepository.findFirstByPertenenciaTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenDetalle,
                            tokenDoc,
                            false
                    );

            if (pertenenciaDocumento == null) {
                df.setMensaje("La relación entre el documento y el registro pertenencia no existe o ya fue eliminada anteriormente");
                return df;
            }

            Documento documento = pertenenciaDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }
            pertenenciaDocumento.setRemovido(true);
            pertenenciaDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            pertenenciaDocumento.setFechaEliminacion(new Date());
            pertenenciaDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.pertenenciaDocumentoRepository.save(pertenenciaDocumento);

            df.llenarRespuestaExitosa("Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " del registro",
                    pertenenciaDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;    }
}
