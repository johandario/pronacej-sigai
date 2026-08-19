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
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.DatosFamiliaresDocumentosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.DatosFamiliaresDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.DatosFamiliaresCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.DatosFamiliaresRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
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
public class DatosFamiliaresDocumentoServiceImpl implements DatosFamiliaresDocumentoService {
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private DatosFamiliaresRepository datosFamiliaresRepository;
    private DatosFamiliaresDocumentoRepository datosFamiliaresDocumentoRepository;
    private DatosFamiliaresCarpetaRepository datosFamiliaresCarpetaRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private CarpetaRepository carpetaRepository;
    private CarpetaService carpetaService;

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
            DatosFamiliaresDocumentoDTO datosFamiliaresDocumentoDTO = new Gson().fromJson(bodyDesencriptado, DatosFamiliaresDocumentoDTO.class);

            DatosFamiliares datosFamiliares = this.datosFamiliaresRepository.findByTokenIdentificadorAndRemovido(
                    datosFamiliaresDocumentoDTO.getTokenIdentificadorDatosFamiliares(), false
            );

            if (datosFamiliares == null) {
                df.setMensaje("No existe el registro solicitado");
                return df;
            }

            DatosFamiliaresCarpeta registroCarpeta = this.datosFamiliaresCarpetaRepository.findFirstByDatosFamiliaresTokenIdentificadorAndRemovido(
                    datosFamiliares.getTokenIdentificador(), false
            );

            DocumentoDTO documentoDTO = datosFamiliaresDocumentoDTO.getDocumentoDTO();
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

            DatosFamiliaresDocumento datosFamiliaresDocumento = new DatosFamiliaresDocumento();
            datosFamiliaresDocumento.setDocumento(documento);
            datosFamiliaresDocumento.setDatosFamiliares(datosFamiliares);
            datosFamiliaresDocumento.setCarpeta(carpeta);
            datosFamiliaresDocumento.setUsuarioSistemaCrea(usuarioSistema);
            datosFamiliaresDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.datosFamiliaresDocumentoRepository.save(datosFamiliaresDocumento);

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(datosFamiliares.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se ha subido con éxito el documento con nombre: " + documento.getNombreReal();

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(datosFamiliares.getFichaIdentificacion());
            String mensajeAuditoria = "Se subió con éxito el documento '" + documento.getNombreReal() + "' para los datos familiares de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, documentoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, DatosFamiliaresDocumentosRequest datosFamiliaresDocumentosRequest) {
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

            DatosFamiliares datosFamiliares = this.datosFamiliaresRepository.findByTokenIdentificadorAndRemovido(
                    datosFamiliaresDocumentosRequest.getTokenIdentificadorDatosFamiliares(),
                    false
            );

            if (datosFamiliares == null) {
                df.setMensaje("El registro es inválido");
                return df;
            }

            Pageable pageable = PageRequest.of(datosFamiliaresDocumentosRequest.getPage(),
                    datosFamiliaresDocumentosRequest.getSize());
            Page<DatosFamiliaresDocumento> datosFamiliaresDocumentoPage =
                    this.datosFamiliaresDocumentoRepository.findByDatosFamiliaresTokenIdentificadorAndRemovido(
                            datosFamiliares.getTokenIdentificador(),
                            false,
                            pageable
                    );
            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<DatosFamiliaresDocumento> datosFamiliaresDocumentos = datosFamiliaresDocumentoPage.toList();

            for (DatosFamiliaresDocumento datosFamiliaresDocumento : datosFamiliaresDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();
                Documento documento = datosFamiliaresDocumento.getDocumento();

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
            paginacionResponse.setTotalItems(datosFamiliaresDocumentoPage.getTotalElements());

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(datosFamiliares.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se han encontrado un total de " + datosFamiliaresDocumentoPage.getTotalElements() + " documentos";

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(datosFamiliares.getFichaIdentificacion());
            String mensajeAuditoria = "Se obtuvieron con éxito " + datosFamiliaresDocumentoPage.getTotalElements() + " documentos de datos familiares de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DatosFamiliaresDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, DatosFamiliaresDocumentoDTO datosFamiliaresDocumentoDTO) {
        RespuestaPorDefectoAuditoria<DatosFamiliaresDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = datosFamiliaresDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = datosFamiliaresDocumentoDTO.getTokenIdentificadorDatosFamiliares();

            DatosFamiliaresDocumento datosFamiliaresDocumento = this.
                    datosFamiliaresDocumentoRepository.findFirstByDatosFamiliaresTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenDetalle,
                            tokenDoc,
                            false
                    );

            if (datosFamiliaresDocumento == null) {
                df.setMensaje("La relación entre el documento y el registro de datos familiares no existe o ya fue eliminada anteriormente");
                return df;
            }

            Documento documento = datosFamiliaresDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(datosFamiliaresDocumento.getDatosFamiliares().getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(datosFamiliaresDocumento.getDatosFamiliares().getFichaIdentificacion());

            datosFamiliaresDocumento.setRemovido(true);
            datosFamiliaresDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            datosFamiliaresDocumento.setFechaEliminacion(new Date());
            datosFamiliaresDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.datosFamiliaresDocumentoRepository.save(datosFamiliaresDocumento);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó correctamente el documento: " + documento.getNombreReal() + " del registro";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito el documento '" + documento.getNombreReal() + "' de los datos familiares de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, datosFamiliaresDocumentoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DocumentoDTO> subirDocumentoFichaPsicoSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, MultipartFile multipartFile) {
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
            DatosFamiliaresDocumentoDTO datosFamiliaresDocumentoDTO = new Gson().fromJson(bodyDesencriptado, DatosFamiliaresDocumentoDTO.class);

            String nemonicoPsicosocial = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_PSICOSOCIAL;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPsicosocial = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(datosFamiliaresDocumentoDTO.getTokenFichaIdentificacion(), nemonicoPsicosocial, false);

            DocumentoDTO documentoDTO = datosFamiliaresDocumentoDTO.getDocumentoDTO();

            String fallo = "No se pudo guardar el documento con nombre: " + documentoDTO.getNombre();

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(datosFamiliaresDocumentoDTO.getTokenFichaIdentificacion(), false);

            if (fichaIdentificacionCarpetaPsicosocial == null) {

                FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.
                        findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(datosFamiliaresDocumentoDTO.getTokenFichaIdentificacion(), null, false);
                Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

                String nombreCarpetaPrincipal = "Ficha Ingreso";

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                carpetaDTO.setDescripcion("Carpeta de ficha ingreso");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                fichaIdentificacionCarpetaPsicosocial = new FichaIdentificacionCarpeta();
                fichaIdentificacionCarpetaPsicosocial.setCarpeta(carpetaGuardadaRecientemente);
                fichaIdentificacionCarpetaPsicosocial.setFichaIdentificacion(ficha);
                Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoPsicosocial, false);
                fichaIdentificacionCarpetaPsicosocial.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                fichaIdentificacionCarpetaPsicosocial.setFechaCreacion(new Date());
                fichaIdentificacionCarpetaPsicosocial.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaIdentificacionCarpetaPsicosocial.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaPsicosocial);

//                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
//                return df;
            }

            Carpeta carpeta = fichaIdentificacionCarpetaPsicosocial.getCarpeta();

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

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(ficha);
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se ha subido con éxito el documento con nombre: " + documento.getNombreReal();

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(ficha);
            String mensajeAuditoria = "Se subió con éxito el documento '" + documento.getNombreReal() + "' a la ficha psicosocial de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, documentoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentosFichaPsicoSocial(HttpServletRequest httpServletRequest,
                                                                                                            DatosFamiliaresDocumentosRequest datosFamiliaresDocumentosRequest) {
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

            String nemonicoPsicosocial = EtiquetaNemonico.CARPETA_GESTION_ADOLES_FICHA_PSICOSOCIAL;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPsicosocial = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(datosFamiliaresDocumentosRequest.getTokenFichaIdentificacion(), nemonicoPsicosocial, false);

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(datosFamiliaresDocumentosRequest.getTokenFichaIdentificacion(), false);

            if (fichaIdentificacionCarpetaPsicosocial == null) {

                FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.
                        findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(datosFamiliaresDocumentosRequest.getTokenFichaIdentificacion(), null, false);
                Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

                String nombreCarpetaPrincipal = "Ficha Ingreso";

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                carpetaDTO.setDescripcion("Carpeta de ficha ingreso");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                fichaIdentificacionCarpetaPsicosocial = new FichaIdentificacionCarpeta();
                fichaIdentificacionCarpetaPsicosocial.setCarpeta(carpetaGuardadaRecientemente);
                fichaIdentificacionCarpetaPsicosocial.setFichaIdentificacion(ficha);
                Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoPsicosocial, false);
                fichaIdentificacionCarpetaPsicosocial.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                fichaIdentificacionCarpetaPsicosocial.setFechaCreacion(new Date());
                fichaIdentificacionCarpetaPsicosocial.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaIdentificacionCarpetaPsicosocial.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaPsicosocial);

//                df.setMensaje(fallo + ", debido a que no existe la carpeta.");
//                return df;
            }

            Pageable pageable = PageRequest.of(datosFamiliaresDocumentosRequest.getPage(),
                    datosFamiliaresDocumentosRequest.getSize());

            Page<Documento> fichaIngresoDocumentoPage =
                    this.documentoRepository.findByCarpetaTokenIdentificadorAndRemovido(
                            fichaIdentificacionCarpetaPsicosocial.getCarpeta().getTokenIdentificador(),
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

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(ficha);
            
            // Mensaje para el usuario
            String mensajeUsuario = "Se han encontrado un total de " + fichaIngresoDocumentoPage.getTotalElements() + " documentos";

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(ficha);
            String mensajeAuditoria = "Se obtuvieron con éxito " + fichaIngresoDocumentoPage.getTotalElements() + " documentos de la ficha psicosocial de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
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
