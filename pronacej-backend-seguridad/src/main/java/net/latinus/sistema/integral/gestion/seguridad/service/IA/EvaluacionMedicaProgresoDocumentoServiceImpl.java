package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionMedicaProgreso;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionMedicaProgresoCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionMedicaProgresoDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.EvaluacionMedicaProgresoDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.EvaluacionMedicaProgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
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
public class EvaluacionMedicaProgresoDocumentoServiceImpl implements EvaluacionMedicaProgresoDocumentoService{

    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;

    private EvaluacionMedicaProgresoCarpetaRepository evaluacionMedicaProgresoCarpetaRepository;
    private EvaluacionMedicaProgresoDocumentoRepository evaluacionMedicaProgresoDocumentoRepository;
    private EvaluacionMedicaProgresoRepository evaluacionMedicaProgresoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, MultipartFile multipartFile) {
        RespuestaPorDefectoAuditoria<DocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{
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
            EvaluacionMedicaProgresoDocumentoDTO docDTO = new Gson().fromJson(bodyDesencriptado, EvaluacionMedicaProgresoDocumentoDTO.class);

            // Buscar la EvaluacionMedicaProgreso basada en el token proporcionado
            EvaluacionMedicaProgreso evaluacion = this.evaluacionMedicaProgresoRepository
                    .findByTokenIdentificadorAndRemovido(docDTO.getTokenIdentificadorEvaluacionMedicaProgreso(), false);

            if (evaluacion == null) {
                df.setMensaje("La evaluación médica de progreso no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            EvaluacionMedicaProgresoCarpeta evaluacionMedicaProgresoCarpeta = this.evaluacionMedicaProgresoCarpetaRepository.
                    findFirstByEvaluacionMedicaProgresoTokenIdentificadorAndRemovido(evaluacion.getTokenIdentificador(), false);

            DocumentoDTO documentoDTO = docDTO.getDocumentoDTO();
            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();

            if (evaluacionMedicaProgresoCarpeta == null) {
                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
                return df;
            }

            Carpeta carpeta = evaluacionMedicaProgresoCarpeta.getCarpeta();

            String idNode = carpeta.getIdentificadorAlfresco();
            RespuestaPorDefectoAuditoria<DocumentoDTO> df3 = this.documentoService.subirDocumentoAlfresco(
                    httpServletRequest,
                    idNode,
                    multipartFile,
                    documentoDTO
            );

            documentoDTO = df3.getData();
            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    documentoDTO.getTokenIdentificador(), false
            );


            EvaluacionMedicaProgresoDocumento evaluacionDoc = new EvaluacionMedicaProgresoDocumento();
            evaluacionDoc.setEvaluacionMedicaProgreso(evaluacion);
            evaluacionDoc.setCarpeta(carpeta);
            evaluacionDoc.setDocumento(documento);
            evaluacionDoc.setUsuarioSistemaCrea(usuarioSistema);
            evaluacionDoc.setIpCrea(httpServletRequest.getRemoteAddr());
            this.evaluacionMedicaProgresoDocumentoRepository.save(evaluacionDoc);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest,
                                                                                            EvaluacionMedicaProgresoDocumentoRequest evaluacionMedicaProgresoDocumentosRequest) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{
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

            EvaluacionMedicaProgreso evaluacion = this.evaluacionMedicaProgresoRepository
                    .findByTokenIdentificadorAndRemovido(evaluacionMedicaProgresoDocumentosRequest.getTokenIdentificadorEvaluacionMedicaProgreso(), false);

            if (evaluacion == null) {
                df.setMensaje("La evaluación médica de progreso no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            Pageable pageable = PageRequest.of(evaluacionMedicaProgresoDocumentosRequest.getPage(), evaluacionMedicaProgresoDocumentosRequest.getSize());

            Page<EvaluacionMedicaProgresoDocumento> documentosPage = this.evaluacionMedicaProgresoDocumentoRepository
                    .findByEvaluacionMedicaProgresoTokenIdentificadorAndRemovido(evaluacion.getTokenIdentificador(), false, pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (EvaluacionMedicaProgresoDocumento empDoc : documentosPage.toList()) {
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

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest,
                                                                                                           EvaluacionMedicaProgresoDocumentoDTO evalMedDocDTO) {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try{
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(df2.getLogOut());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            String tokenDoc = evalMedDocDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenEvaluacion = evalMedDocDTO.getTokenIdentificadorEvaluacionMedicaProgreso();

            EvaluacionMedicaProgresoDocumento evalMedDoc = this.evaluacionMedicaProgresoDocumentoRepository
                    .findFirstByEvaluacionMedicaProgresoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenEvaluacion, tokenDoc, false
                    );

            if (evalMedDoc == null) {
                df.setMensaje("El documento no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            Documento documento = evalMedDoc.getDocumento();
            if (documento == null) {
                df.setMensaje("El registro no presenta el documento requerido");
                return df;
            }

            evalMedDoc.setRemovido(true);
            evalMedDoc.setIpElimina(httpServletRequest.getRemoteAddr());
            evalMedDoc.setFechaEliminacion(new Date());
            evalMedDoc.setUsuarioSistemaElimina(usuarioSistema);
            this.evaluacionMedicaProgresoDocumentoRepository.save(evalMedDoc);

            df.llenarRespuestaExitosa("Se eliminó correctamente el documento: "
                    + documento.getNombreReal() + " del registro", evalMedDocDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
