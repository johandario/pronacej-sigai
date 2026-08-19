package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.EvaluacionDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.HistoricoEntradaSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.InformeFinalAbiertoDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.InformeFinalAbiertoMedidasRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.InformeFinalAbiertoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.HistoricoFichaIdentificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InformeFinalAbiertoServiceImpl implements InformeFinalAbiertoService {
    private PaginacionService paginacionService;

    private InformeFinalAbiertoRepository informeFinalAbiertoRepository;
    private InformeFinalAbiertoMedidasRepository informeFinalAbiertoMedidasRepository;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private HistoricoEntradaSalidaRepository historicoEntradaSalidaRepository;
    private InformeFinalAbiertoDocumentoRepository informeFinalAbiertoDocumentoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;

    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAbiertoDTO>> obtenerInformes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeFinalAbiertoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            List<InformeFinalAbierto> informeFinalAbiertos = this.informeFinalAbiertoRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            PaginacionResponse<InformeFinalAbiertoDTO> paginacionResponse;
            List<InformeFinalAbiertoDTO> informeFinalAbiertoDTOS = new ArrayList<>();

            for (InformeFinalAbierto informe : informeFinalAbiertos) {

                InformeFinalAbiertoDTO dto = entidadADto(informe);

                dto.setFechaCreacion(informe.getFechaCreacion());
                dto.setTokenIdentificador(informe.getTokenIdentificador());
                dto.setIdFichaIdentificacion(informe.getFichaIdentificacion().getIdFichaIdentificacion());
                informeFinalAbiertoDTOS.add(dto);
            }

            informeFinalAbiertoDTOS.sort(
                    Comparator.comparing(InformeFinalAbiertoDTO::getFechaCreacion).reversed()
            );

            paginacionResponse = paginacionService.obtenerDatos(informeFinalAbiertoDTOS, paginacionRequest);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + informeFinalAbiertoDTOS.size() + " de: " + informeFinalAbiertos.size() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO> crearInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            InformeFinalAbiertoDTO informeDTO = new Gson().fromJson(bodyDecifrado, InformeFinalAbiertoDTO.class);
            InformeFinalAbierto informeFinalAbierto;

            //Es Borrador
            if (informeDTO.getEsEdicion()) {
                informeFinalAbierto = this.informeFinalAbiertoRepository.findByTokenIdentificadorAndRemovido(informeDTO.getTokenIdentificador(), false);

                //Se eliminan las medidas del borrador para volverlas a crear posteriormente
                List<InformeFinalAbiertoMedidas> medidas = informeFinalAbiertoMedidasRepository.findByInformeFinalAbiertoTokenIdentificadorAndRemovido(informeFinalAbierto.getTokenIdentificador(), false);
                informeFinalAbiertoMedidasRepository.deleteAll(medidas);
            }

            informeFinalAbierto = dtoAEntidad(informeDTO);


            if (informeDTO.getCompletado()) {
                informeFinalAbierto.setCompletado(true);
                informeFinalAbierto.setFechaFinalizacion(new Date());
            } else
                informeFinalAbierto.setCompletado(false);

            if (!informeDTO.getEsEdicion()) {
                informeFinalAbierto.setFechaCreacion(new Date());
                informeFinalAbierto.setIpCrea(httpServletRequest.getRemoteAddr());
                informeFinalAbierto.setEmpresa(df2.getData().getEmpresa());
                informeFinalAbierto.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());

                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(informeDTO.getTokenFichaIdenticacion(), false);
                informeFinalAbierto.setFichaIdentificacion(ficha);

                informeFinalAbierto = this.informeFinalAbiertoRepository.save(informeFinalAbierto);


                HistoricoEntradaSalida historico = new HistoricoEntradaSalida();
                if (ficha != null) {
                    historico.setNumeroIdentificacion(ficha.getNumeroIdentificacion());
                    historico.setFichaIdentificacion(ficha);
                } else {
                    throw new IllegalArgumentException("Ficha de identificación no encontrada para la fuga.");
                }
                historico.setFechaEntrada(new Date());
                historico.setRegistroActivo(true);
                historico.setInformeFinalAbierto(informeFinalAbierto);
                historico.setFechaSalida(informeFinalAbierto.getFechaCreacion());
                historico.setMotivoSalida(this.catalogoRepository.findByNemonicoAndRemovido("SALIDA_INFORME_FINAL", false));
                this.historicoEntradaSalidaRepository.save(historico);

                df.llenarRespuestaExitosa("Se ha creado con éxito el informe.", entidadADto(informeFinalAbierto));
            } else {
                informeFinalAbierto.setFechaEdicion(new Date());
                informeFinalAbierto.setIpEdita(httpServletRequest.getRemoteAddr());
                informeFinalAbierto.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                informeFinalAbierto = this.informeFinalAbiertoRepository.save(informeFinalAbierto);

                df.llenarRespuestaExitosa("Se ha editado con éxito el informe.", entidadADto(informeFinalAbierto));
            }

            if (informeDTO.getCompletado()) {
                // Modificar ficha de identificación -> tieneProceso = false
                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(informeDTO.getTokenFichaIdenticacion(), false);
                ficha.setTieneProceso(false);
                fichaIdentificacionRepository.save(ficha);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO> eliminarInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InformeFinalAbiertoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            InformeFinalAbiertoDTO informeDTO = new Gson().fromJson(bodyDecifrado, InformeFinalAbiertoDTO.class);

            InformeFinalAbierto InformeEncontrado = this.informeFinalAbiertoRepository.findByTokenIdentificadorAndRemovido(informeDTO.getTokenIdentificador(), false);

            if (InformeEncontrado == null) {
                df.setMensaje("No existe el registro buscado.");
                return df;
            }

            InformeFinalAbierto informe = dtoAEntidad(informeDTO);
            informe.setRemovido(true);
            informe.setFechaEliminacion(new Date());
            informe.setIpElimina(httpServletRequest.getRemoteAddr());
            informe.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(informeDTO.getTokenFichaIdenticacion(), false);
            informe.setFichaIdentificacion(ficha);

            this.informeFinalAbiertoRepository.save(informe);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito el informe.", entidadADto(informe));


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest,
                                                                 MultipartFile[] multipartFiles,
                                                                 BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
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
            InformeFinalAbiertoDTO informeDTO = new Gson().fromJson(bodyDesencriptado, InformeFinalAbiertoDTO.class);

            InformeFinalAbierto informe = this.informeFinalAbiertoRepository.findByTokenIdentificadorAndRemovido(
                    informeDTO.getTokenIdentificador(), false
            );

            if (informe == null) {
                respuesta.setMensaje("No existe el registro solicitado");
                return respuesta;
            }

            FichaIdentificacion fichaIdentificacion = informe.getFichaIdentificacion();

            if (fichaIdentificacion == null || fichaIdentificacion.getTokenIdentificador() == null) {
                respuesta.setMensaje("Se recibio una ficha de identificación inválida");
                return respuesta;
            }

            Catalogo catalogoCarpeta = catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.CARPETA_GESTION_ADOLES_INFORME_FINAL, false);

            if (catalogoCarpeta == null) {
                respuesta.setMensaje("No existe el catalogo de la carpeta especificada");
                return respuesta;
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            String nemonicoCarpeta = catalogoCarpeta.getNemonico();
            Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage = this.fichaIdentificacionCarpetaRepository.
                    findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIdentificacion.getTokenIdentificador(),
                            nemonicoCarpeta,
                            false,
                            pageable
                    );

            if (fichaIdentificacionCarpetaPage.isEmpty()) {
                respuesta.setMensaje("No se ha creado una carpeta para guardar las evaluaciones que pertenezca a la ficha de identificación solicitada");
                return respuesta;
            }

            if (fichaIdentificacionCarpetaPage.getTotalElements() > 1) {
                this.logService.warn("La ficha de identificacion: " +
                        fichaIdentificacion.getTokenIdentificador() + " tiene mas de una carpeta: " +
                        nemonicoCarpeta
                );
            }

            FichaIdentificacionCarpeta fichaIdentificacionCarpeta = fichaIdentificacionCarpetaPage.toList().get(0);

            Carpeta carpeta = fichaIdentificacionCarpeta.getCarpeta();

            String idNodo = carpeta.getIdentificadorAlfresco();
            List<DocumentoDTO> documentoDTOList = informeDTO.getDocumentoDTOList();


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

                    InformeFinalAbiertoDocumento informeDocumento = new InformeFinalAbiertoDocumento();
                    informeDocumento.setInformeFinalAbierto(informe);
                    informeDocumento.setCarpeta(carpeta);
                    informeDocumento.setDocumento(documento);
                    informeDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                    informeDocumento.setUsuarioSistemaCrea(usuarioSistema);

                    informeFinalAbiertoDocumentoRepository.save(informeDocumento);
                }
            }

            respuesta.llenarRespuestaExitosa("Se ha subido con éxito los documentos", true);

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

            Page<InformeFinalAbiertoDocumento> documentosPage;


            documentosPage = this.informeFinalAbiertoDocumentoRepository.findByInformeFinalAbiertoTokenIdentificadorAndRemovido(
                    paginacionRequest.getTokenIdentificador(),
                    false,
                    pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (InformeFinalAbiertoDocumento infDoc : documentosPage.toList()) {
                Documento documento = infDoc.getDocumento();
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

    private InformeFinalAbierto dtoAEntidad(InformeFinalAbiertoDTO dto) {
        if (dto == null) return null;

        InformeFinalAbierto informe = this.informeFinalAbiertoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        InformeFinalAbierto entidad = Objects.requireNonNullElseGet(informe, InformeFinalAbierto::new);

        entidad.setFortalecimientoDerechos(dto.getFortalecimientoDerechos());
        entidad.setArea(dto.getArea());
        entidad.setFortalecimientoFamiliar(dto.getFortalecimientoFamiliar());
        entidad.setIntervencion(dto.getIntervencion());
        entidad.setEnfoque(dto.getEnfoque());
        entidad.setCultural(dto.getCultural());
        entidad.setResponsabilidad(dto.getResponsabilidad());
        entidad.setConciencia(dto.getConciencia());
        entidad.setCompletado(dto.getCompletado());
        entidad.setFechaFinalizacion(dto.getFechaFinalizacion());

        if (dto.getMedidasList() != null) {
            List<InformeFinalAbiertoMedidas> medidas = dto.getMedidasList().stream()
                    .map(this::medidasDtoAEntidad)
                    .collect(Collectors.toList());
            medidas.forEach(item -> item.setInformeFinalAbierto(entidad));
            entidad.setMedidasList(medidas);
        }

        entidad.setValoracionRiesgo(dto.getValoracionRiesgo());
        entidad.setConclusionesRecomendaciones(dto.getConclusionesRecomendaciones());

        return entidad;
    }

    private InformeFinalAbiertoMedidas medidasDtoAEntidad(InformeFinalAbiertoMedidasDTO dto) {
        if (dto == null) return null;

        InformeFinalAbiertoMedidas medida = this.informeFinalAbiertoMedidasRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        InformeFinalAbiertoMedidas entidad = Objects.requireNonNullElseGet(medida, InformeFinalAbiertoMedidas::new);

        entidad.setMedidaAccesoria(dto.getMedidaAccesoria());
        entidad.setAccion(dto.getAccion());
        entidad.setObjetivo(dto.getObjetivo());
        entidad.setAnalisisCualitativo(dto.getAnalisisCualitativo());

        return entidad;
    }

    private InformeFinalAbiertoDTO entidadADto(InformeFinalAbierto entidad) {
        if (entidad == null) return null;


        InformeFinalAbiertoDTO dto = new InformeFinalAbiertoDTO();
        dto.setIdInformeFinalAbierto(entidad.getIdInformeFinalAbierto());
        dto.setFortalecimientoDerechos(entidad.getFortalecimientoDerechos());
        dto.setArea(entidad.getArea());
        dto.setFortalecimientoFamiliar(entidad.getFortalecimientoFamiliar());
        dto.setIntervencion(entidad.getIntervencion());
        dto.setEnfoque(entidad.getEnfoque());
        dto.setCultural(entidad.getCultural());
        dto.setResponsabilidad(entidad.getResponsabilidad());
        dto.setConciencia(entidad.getConciencia());
        dto.setCompletado(entidad.getCompletado());
        dto.setFechaFinalizacion(entidad.getFechaFinalizacion());

        if (entidad.getMedidasList() != null) {
            List<InformeFinalAbiertoMedidasDTO> medidas = entidad.getMedidasList().stream()
                    .map(this::medidasEntidadADto)
                    .collect(Collectors.toList());
            dto.setMedidasList(medidas);
        }

        dto.setValoracionRiesgo(entidad.getValoracionRiesgo());
        dto.setConclusionesRecomendaciones(entidad.getConclusionesRecomendaciones());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());

        return dto;
    }

    private InformeFinalAbiertoMedidasDTO medidasEntidadADto(InformeFinalAbiertoMedidas entidad) {
        if (entidad == null) return null;

        InformeFinalAbiertoMedidasDTO dto = new InformeFinalAbiertoMedidasDTO();

        dto.setIdInformeFinalAbiertoMedidas(entidad.getIdInformeFinalAbiertoMedidas());
        dto.setMedidaAccesoria(entidad.getMedidaAccesoria());
        dto.setAccion(entidad.getAccion());
        dto.setObjetivo(entidad.getObjetivo());
        dto.setAnalisisCualitativo(entidad.getAnalisisCualitativo());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());

        return dto;
    }
}
