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
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PlanTratamientoIndSeguiAbiertoDocumentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.*;
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
@AllArgsConstructor
public class PlanTratamientoIndSeguiAbiertoDocumentoServiceImpl implements PlanTratamientoIndSeguiAbiertoDocumentoService {
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;    
    private PlanTratamientoIndRepository planTratamientoIndRepository;
    private PlanTratamientoIndCarpetaRepository planTratamientoIndCarpetaRepository;
    private PlanTratamientoIndSeguiAbiertoRepository planTratamientoIndSeguiAbiertoRepository;
    private PlanTratamientoIndSeguiAbiertoCarpetaRepository planTratamientoIndSeguiAbiertoCarpetaRepository;
    private PlanTratamientoIndSeguiAbiertoDocumentoRepository planTratamientoIndSeguiAbiertoDocumentoRepository;

    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    
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
            PlanTratamientoIndSeguiAbiertoDocumentoDTO planTratamientoIndSeguiAbiertoDocumentoDTO = new Gson().fromJson(bodyDesencriptado, PlanTratamientoIndSeguiAbiertoDocumentoDTO.class);

            PlanTratamientoIndSeguiAbierto planTratamientoIndSeguiAbierto = this.planTratamientoIndSeguiAbiertoRepository.findByTokenIdentificadorAndRemovido(
                    planTratamientoIndSeguiAbiertoDocumentoDTO.getTokenIdentificadorFichaSeguimientoAbierto(), false
            );

            if (planTratamientoIndSeguiAbierto == null) {
                df.setMensaje("No existe el registro solicitado");
                return df;
            }

            // Obtener Detalle relacionado a ficha de seguimiento
            PlanTratamientoIndInterv  planTratamientoIndInterv = planTratamientoIndSeguiAbierto.getPlanTratamientoIndInterv();

            // Obtener PlanTratamiento de detalle
            PlanTratamientoInd planTratamientoInd;
            if (planTratamientoIndInterv.getPlanTratamientoIndDiferenciada() != null) {
                planTratamientoInd = planTratamientoIndInterv.getPlanTratamientoIndDiferenciada();
            } else if (planTratamientoIndInterv.getPlanTratamientoIndObjetivo() != null) {
                planTratamientoInd = planTratamientoIndInterv.getPlanTratamientoIndObjetivo();
            } else if (planTratamientoIndInterv.getPlanTratamientoIndNoCriminogeno() != null) {
                planTratamientoInd = planTratamientoIndInterv.getPlanTratamientoIndNoCriminogeno();
            } else if (planTratamientoIndInterv.getPlanTratamientoMedidas() != null) {
                planTratamientoInd = planTratamientoIndInterv.getPlanTratamientoMedidas();
            } else {
                df.setMensaje("No se encontró un plan de tratamiento, no es posible crear carpeta principal");
                return df;
            }

            // CREACIÓN DE CARPETA PRINCIPAL DE PTI EN CASO DE QUE NO EXISTA

            String nemonicoPertenencia = EtiquetaNemonico.CARPETA_GESTION_ADOLES_PTI;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPlan = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(planTratamientoInd.getFichaIdentificacion().getTokenIdentificador(), nemonicoPertenencia, false);
            Carpeta carpetaPadreFichaIdentificacion = fichaIdentificacionCarpetaPlan.getCarpeta();

            PlanTratamientoIndCarpeta carpetaPlanEncontrada = this.planTratamientoIndCarpetaRepository.findFirstByPlanTratamientoIndTokenIdentificadorAndRemovido(planTratamientoInd.getTokenIdentificador(), false);

            // En caso de que no exista carpeta de plan, crear
            if (carpetaPlanEncontrada == null) {
                String pattern = "yyyy-MM-dd-HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                String fechaFormateada = fecha.format(planTratamientoInd.getFechaCreacion());

                String nombreCarpeta = "plan_" + fechaFormateada + "_" + planTratamientoInd.getTipoCentro();

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de plan de tratamiento individual relacionado a : " + planTratamientoInd.getTokenIdentificador());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadreFichaIdentificacion.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                PlanTratamientoIndCarpeta carpetaDetalle = new PlanTratamientoIndCarpeta();
                carpetaDetalle.setCarpeta(carpetaGuardada);
                carpetaDetalle.setPlanTratamientoInd(planTratamientoInd);
                carpetaDetalle.setFechaCreacion(new Date());
                carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                carpetaPlanEncontrada = this.planTratamientoIndCarpetaRepository.save(carpetaDetalle);
            }

            PlanTratamientoIndSeguiAbiertoCarpeta registroCarpeta = null;

            Carpeta carpetaPadrePlan = carpetaPlanEncontrada.getCarpeta();

            // CREACIÓN DE CARPETA DE FICHA DE SEGUIMIENTO EN CASO DE QUE NO EXISTA

            PlanTratamientoIndSeguiAbiertoCarpeta carpetaFichaEncontrada = this.planTratamientoIndSeguiAbiertoCarpetaRepository.findFirstByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndRemovido(planTratamientoIndSeguiAbierto.getTokenIdentificador(), false);

            if (carpetaFichaEncontrada == null) {
                String pattern = "yyyy-MM-dd-HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                String fechaFormateada = fecha.format(planTratamientoIndSeguiAbierto.getFechaCreacion());

                String nombreCarpeta = "ficha_seguimiento_" + fechaFormateada;

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de ficha de seguimiento de pti");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePlan.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);
                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                PlanTratamientoIndSeguiAbiertoCarpeta carpetaDetalle = new PlanTratamientoIndSeguiAbiertoCarpeta();
                carpetaDetalle.setCarpeta(carpetaGuardada);
                carpetaDetalle.setPlanTratamientoIndSeguiAbierto(planTratamientoIndSeguiAbierto);
                carpetaDetalle.setFechaCreacion(new Date());
                carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                registroCarpeta = this.planTratamientoIndSeguiAbiertoCarpetaRepository.save(carpetaDetalle);
                this.planTratamientoIndSeguiAbiertoCarpetaRepository.flush();
            }

            if (registroCarpeta == null) {
                registroCarpeta = this.planTratamientoIndSeguiAbiertoCarpetaRepository.findFirstByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndRemovido(planTratamientoIndSeguiAbierto.getTokenIdentificador(), false);
            }

            DocumentoDTO documentoDTO = planTratamientoIndSeguiAbiertoDocumentoDTO.getDocumentoDTO();
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

            PlanTratamientoIndSeguiAbiertoDocumento planTratamientoIndSeguiAbiertoDocumento = new PlanTratamientoIndSeguiAbiertoDocumento();
            planTratamientoIndSeguiAbiertoDocumento.setDocumento(documento);
            planTratamientoIndSeguiAbiertoDocumento.setPlanTratamientoIndSeguiAbierto(planTratamientoIndSeguiAbierto);
            planTratamientoIndSeguiAbiertoDocumento.setCarpeta(carpeta);
            planTratamientoIndSeguiAbiertoDocumento.setUsuarioSistemaCrea(usuarioSistema);
            planTratamientoIndSeguiAbiertoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.planTratamientoIndSeguiAbiertoDocumentoRepository.save(planTratamientoIndSeguiAbiertoDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, PlanTratamientoIndSeguiAbiertoDocumentoRequest planTratamientoIndSeguiAbiertoDocumentoRequest) {
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

            PlanTratamientoIndSeguiAbierto planTratamientoIndSeguiAbierto = this.planTratamientoIndSeguiAbiertoRepository.findByTokenIdentificadorAndRemovido(
                    planTratamientoIndSeguiAbiertoDocumentoRequest.getTokenIdentificadorFichaSeguimientoAbierto(),
                    false
            );

            if (planTratamientoIndSeguiAbierto == null) {
                df.setMensaje("El registro es inválido");
                return df;
            }

            Pageable pageable = PageRequest.of(planTratamientoIndSeguiAbiertoDocumentoRequest.getPage(),
                    planTratamientoIndSeguiAbiertoDocumentoRequest.getSize());
            Page<PlanTratamientoIndSeguiAbiertoDocumento> planTratamientoIndSeguiAbiertoDocumentoPage =
                    this.planTratamientoIndSeguiAbiertoDocumentoRepository.findByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndRemovido(
                            planTratamientoIndSeguiAbierto.getTokenIdentificador(),
                            false,
                            pageable
                    );
            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<PlanTratamientoIndSeguiAbiertoDocumento> planTratamientoIndSeguiAbiertoDocumentos = planTratamientoIndSeguiAbiertoDocumentoPage.toList();


            for (PlanTratamientoIndSeguiAbiertoDocumento planTratamientoIndSeguiAbiertoDocumento : planTratamientoIndSeguiAbiertoDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();
                Documento documento = planTratamientoIndSeguiAbiertoDocumento.getDocumento();

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
            paginacionResponse.setTotalItems(planTratamientoIndSeguiAbiertoDocumentoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " + planTratamientoIndSeguiAbiertoDocumentoPage.getTotalElements(), paginacionResponse);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, PlanTratamientoIndSeguiAbiertoDocumentoDTO planTratamientoIndSeguiAbiertoDocumentoDTO) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = planTratamientoIndSeguiAbiertoDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = planTratamientoIndSeguiAbiertoDocumentoDTO.getTokenIdentificadorFichaSeguimientoAbierto();

            PlanTratamientoIndSeguiAbiertoDocumento planTratamientoIndSeguiAbiertoDocumento = this.
                    planTratamientoIndSeguiAbiertoDocumentoRepository.findFirstByPlanTratamientoIndSeguiAbiertoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenDetalle,
                            tokenDoc,
                            false
                    );

            if (planTratamientoIndSeguiAbiertoDocumento == null) {
                df.setMensaje("La relación entre el documento y el registro PlanTratamientoIndSeguiAbierto no existe o ya fue eliminada anteriormente");
                return df;
            }

            Documento documento = planTratamientoIndSeguiAbiertoDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }
            planTratamientoIndSeguiAbiertoDocumento.setRemovido(true);
            planTratamientoIndSeguiAbiertoDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            planTratamientoIndSeguiAbiertoDocumento.setFechaEliminacion(new Date());
            planTratamientoIndSeguiAbiertoDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.planTratamientoIndSeguiAbiertoDocumentoRepository.save(planTratamientoIndSeguiAbiertoDocumento);

            df.llenarRespuestaExitosa("Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " del registro",
                    planTratamientoIndSeguiAbiertoDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
