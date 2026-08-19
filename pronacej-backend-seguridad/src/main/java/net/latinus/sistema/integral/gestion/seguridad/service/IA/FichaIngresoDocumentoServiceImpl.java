package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.FichaIngresoDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIngresoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIngresoDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIngresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
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
public class FichaIngresoDocumentoServiceImpl implements FichaIngresoDocumentoService {

    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private FichaIngresoCarpetaRepository fichaIngresoCarpetaRepository;
    private FichaIngresoDocumentoRepository fichaIngresoDocumentoRepository;
    private FichaIngresoRepository fichaIngresoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;


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
            FichaIngresoDocumentoDTO fichaIngresoDocumentoDTO = new Gson().fromJson(bodyDesencriptado, FichaIngresoDocumentoDTO.class);

            String nemonicoIngreso = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_INGRESO;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaIngreso = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(fichaIngresoDocumentoDTO.getTokenFichaIdentificacion(), nemonicoIngreso, false);

//            FichaIngreso fichaIngreso = this.fichaIngresoRepository.
//                    findByTokenIdentificadorAndRemovido(fichaIngresoDocumentoDTO.getTokenIdentificadorFichaIngreso(), false);
            DocumentoDTO documentoDTO = fichaIngresoDocumentoDTO.getDocumentoDTO();

            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();

            if (fichaIdentificacionCarpetaIngreso == null) {
                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
                return df;
            }
//
//            FichaIngresoCarpeta fichaIngresoCarpeta = this.fichaIngresoCarpetaRepository.
//                    findFirstByFichaIngresoTokenIdentificadorAndRemovido(fichaIngreso.getTokenIdentificador(), false);

//
//
//            if (fichaIngresoCarpeta == null) {
//                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
//                return df;
//            }

            Carpeta carpeta = fichaIdentificacionCarpetaIngreso.getCarpeta();

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

            FichaIngresoDocumento fichaIngresoDocumento = new FichaIngresoDocumento();
//            fichaIngresoDocumento.setFichaIngreso(fichaIngreso);
            fichaIngresoDocumento.setCarpeta(carpeta);
            fichaIngresoDocumento.setDocumento(documento);
            fichaIngresoDocumento.setUsuarioSistemaCrea(usuarioSistema);
            fichaIngresoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.fichaIngresoDocumentoRepository.save(fichaIngresoDocumento);

            // Obtener información para los mensajes
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacionCarpetaIngreso.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacionCarpetaIngreso.getFichaIdentificacion());

            // Mensaje para el usuario
            String mensajeUsuario = "Se ha subido con exito el documento con nombre: " + documento.getNombreReal();

            // Mensaje para auditoría
            String mensajeAuditoria = "Se subió el documento '" + documento.getNombreReal() + 
                    "' para la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, documentoDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, FichaIngresoDocumentoRequest fichaIngresoDocumentosRequest) {
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

            String nemonicoPertenencia = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_INGRESO;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaIngreso = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIngresoDocumentosRequest.getTokenIdentificador(), nemonicoPertenencia, false);

//            FichaIngreso fichaIngreso = this.fichaIngresoRepository.
//                    findByTokenIdentificadorAndRemovido(fichaIngresoDocumentosRequest.getTokenIdentificadorFichaIngreso(), false);
//
//            if (fichaIngreso == null) {
//                df.setMensaje("La ficha de ingreso no fue encontrada o ya fue eliminada anteriormente");
//                return df;
//            }

            Pageable pageable = PageRequest.of(fichaIngresoDocumentosRequest.getPage(),
                    fichaIngresoDocumentosRequest.getSize());

            Page<Documento> fichaIngresoDocumentoPage =
                    this.documentoRepository.findByCarpetaTokenIdentificadorAndRemovido(
                            fichaIdentificacionCarpetaIngreso.getCarpeta().getTokenIdentificador(),
                            false,
                            pageable
                    );

            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<Documento> fichaIngresoDocumentos = fichaIngresoDocumentoPage.toList();

            for (Documento documento : fichaIngresoDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();

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
            paginacionResponse.setTotalItems(fichaIngresoDocumentoPage.getTotalElements());

            // Obtener información para los mensajes
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacionCarpetaIngreso.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacionCarpetaIngreso.getFichaIdentificacion());

            // Mensaje para el usuario
            String mensajeUsuario = "Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " + fichaIngresoDocumentoPage.getTotalElements();

            // Mensaje para auditoría
            String mensajeAuditoria = "Se consultaron " + fichaIngresoDocumentoPage.getTotalElements() + 
                    " documentos de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIngresoDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, FichaIngresoDocumentoDTO fichaIngresoDocumentoDTO) {
        RespuestaPorDefectoAuditoria<FichaIngresoDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try{

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

            String tokenDoc = fichaIngresoDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = fichaIngresoDocumentoDTO.getTokenIdentificadorFichaIngreso();

            FichaIngresoDocumento fichaIngresoDocumento = this.fichaIngresoDocumentoRepository.
                    findFirstByFichaIngresoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(tokenDetalle,tokenDoc,false);

            if (fichaIngresoDocumento == null) {
                df.setMensaje("El documento no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            Documento documento = fichaIngresoDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }

            fichaIngresoDocumento.setRemovido(true);
            fichaIngresoDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            fichaIngresoDocumento.setFechaEliminacion(new Date());
            fichaIngresoDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.fichaIngresoDocumentoRepository.save(fichaIngresoDocumento);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " del registro";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó la relación del documento '" + documento.getNombreReal() + 
                    "' del registro";

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIngresoDocumentoDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;

    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentosFichaIngreso(HttpServletRequest httpServletRequest,
                                                                                                        FichaIngresoDocumentoRequest fichaIngresoDocumentosRequest) {
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

            String nemonicoPertenencia = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_INGRESO;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaIngreso = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIngresoDocumentosRequest.getTokenIdentificador(), nemonicoPertenencia, false);

            if(fichaIdentificacionCarpetaIngreso==null){
                return df;
            }

            Pageable pageable = PageRequest.of(fichaIngresoDocumentosRequest.getPage(),
                    fichaIngresoDocumentosRequest.getSize());

            Page<Documento> fichaIngresoDocumentoPage =
                    this.documentoRepository.findByCarpetaTokenIdentificadorAndRemovido(
                            fichaIdentificacionCarpetaIngreso.getCarpeta().getTokenIdentificador(),
                            false,
                            pageable
                    );

            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<Documento> fichaIngresoDocumentos = fichaIngresoDocumentoPage.toList();

            for (Documento documento : fichaIngresoDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();

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
            paginacionResponse.setTotalItems(fichaIngresoDocumentoPage.getTotalElements());

            // Obtener información para los mensajes
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacionCarpetaIngreso.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacionCarpetaIngreso.getFichaIdentificacion());

            // Mensaje para el usuario
            String mensajeUsuario = "Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " + fichaIngresoDocumentoPage.getTotalElements();

            // Mensaje para auditoría
            String mensajeAuditoria = "Se consultaron " + fichaIngresoDocumentoPage.getTotalElements() + 
                    " documentos de ficha de ingreso de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        }catch (Exception ex) {
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
}