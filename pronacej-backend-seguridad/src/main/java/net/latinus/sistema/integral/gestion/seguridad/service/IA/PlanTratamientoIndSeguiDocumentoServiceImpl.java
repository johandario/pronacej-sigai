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
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.PlanTratamientoIndSeguiDocumentoRequest;
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
public class PlanTratamientoIndSeguiDocumentoServiceImpl implements PlanTratamientoIndSeguiDocumentoService {
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;    
    private PlanTratamientoIndRepository planTratamientoIndRepository;
    private PlanTratamientoIndCarpetaRepository planTratamientoIndCarpetaRepository;
    private PlanTratamientoIndSeguiRepository planTratamientoIndSeguiRepository;
    private PlanTratamientoIndSeguiCarpetaRepository planTratamientoIndSeguiCarpetaRepository;
    private PlanTratamientoIndSeguiDocumentoRepository planTratamientoIndSeguiDocumentoRepository;

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
            PlanTratamientoIndSeguiDocumentoDTO planTratamientoIndSeguiDocumentoDTO = new Gson().fromJson(bodyDesencriptado, PlanTratamientoIndSeguiDocumentoDTO.class);

            PlanTratamientoIndSegui planTratamientoIndSegui = this.planTratamientoIndSeguiRepository.findByTokenIdentificadorAndRemovido(
                    planTratamientoIndSeguiDocumentoDTO.getTokenIdentificadorSeguimiento(), false
            );

            if (planTratamientoIndSegui == null) {
                df.setMensaje("No existe el registro solicitado");
                return df;
            }

            // Obtener el plan de tratamiento
            PlanTratamientoInd planTratamientoInd = planTratamientoIndSegui.getPlanTratamientoInd();
            if (planTratamientoInd == null) {
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

            PlanTratamientoIndSeguiCarpeta registroCarpeta = null;

            Carpeta carpetaPadrePlan = carpetaPlanEncontrada.getCarpeta();

            // CREACIÓN DE CARPETA DE FICHA DE SEGUIMIENTO EN CASO DE QUE NO EXISTA

            PlanTratamientoIndSeguiCarpeta carpetaFichaEncontrada = this.planTratamientoIndSeguiCarpetaRepository.findFirstByPlanTratamientoIndSeguiTokenIdentificadorAndRemovido(planTratamientoIndSegui.getTokenIdentificador(), false);

            if (carpetaFichaEncontrada == null) {
                String pattern = "yyyy-MM-dd-HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                String fechaFormateada = fecha.format(planTratamientoIndSegui.getFechaCreacion());

                String nombreCarpeta = "seguimiento_pti_" + fechaFormateada;

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de seguimiento de pti");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePlan.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);
                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                PlanTratamientoIndSeguiCarpeta carpetaDetalle = new PlanTratamientoIndSeguiCarpeta();
                carpetaDetalle.setCarpeta(carpetaGuardada);
                carpetaDetalle.setPlanTratamientoIndSegui(planTratamientoIndSegui);
                carpetaDetalle.setFechaCreacion(new Date());
                carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                registroCarpeta = this.planTratamientoIndSeguiCarpetaRepository.save(carpetaDetalle);
                this.planTratamientoIndSeguiCarpetaRepository.flush();
            }

            if (registroCarpeta == null) {
                registroCarpeta = this.planTratamientoIndSeguiCarpetaRepository.findFirstByPlanTratamientoIndSeguiTokenIdentificadorAndRemovido(planTratamientoIndSegui.getTokenIdentificador(), false);
            }

            DocumentoDTO documentoDTO = planTratamientoIndSeguiDocumentoDTO.getDocumentoDTO();
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

            PlanTratamientoIndSeguiDocumento planTratamientoIndSeguiDocumento = new PlanTratamientoIndSeguiDocumento();
            planTratamientoIndSeguiDocumento.setDocumento(documento);
            planTratamientoIndSeguiDocumento.setPlanTratamientoIndSegui(planTratamientoIndSegui);
            planTratamientoIndSeguiDocumento.setCarpeta(carpeta);
            planTratamientoIndSeguiDocumento.setUsuarioSistemaCrea(usuarioSistema);
            planTratamientoIndSeguiDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            this.planTratamientoIndSeguiDocumentoRepository.save(planTratamientoIndSeguiDocumento);

            df.llenarRespuestaExitosa("Se ha subido con exito el documento con nombre: "
                    + documento.getNombreReal(), documentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, PlanTratamientoIndSeguiDocumentoRequest planTratamientoIndSeguiDocumentoRequest) {
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

            PlanTratamientoIndSegui planTratamientoIndSegui = this.planTratamientoIndSeguiRepository.findByTokenIdentificadorAndRemovido(
                    planTratamientoIndSeguiDocumentoRequest.getTokenIdentificadorSeguimiento(),
                    false
            );

            if (planTratamientoIndSegui == null) {
                df.setMensaje("El registro es inválido");
                return df;
            }

            Pageable pageable = PageRequest.of(planTratamientoIndSeguiDocumentoRequest.getPage(),
                    planTratamientoIndSeguiDocumentoRequest.getSize());
            Page<PlanTratamientoIndSeguiDocumento> planTratamientoIndSeguiDocumentoPage =
                    this.planTratamientoIndSeguiDocumentoRepository.findByPlanTratamientoIndSeguiTokenIdentificadorAndRemovido(
                            planTratamientoIndSegui.getTokenIdentificador(),
                            false,
                            pageable
                    );
            List<DocumentoDTO> documentoList = new ArrayList<>();
            List<PlanTratamientoIndSeguiDocumento> planTratamientoIndSeguiDocumentos = planTratamientoIndSeguiDocumentoPage.toList();


            for (PlanTratamientoIndSeguiDocumento planTratamientoIndSeguiDocumento : planTratamientoIndSeguiDocumentos) {
                DocumentoDTO documentoDTO = new DocumentoDTO();
                Documento documento = planTratamientoIndSeguiDocumento.getDocumento();

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
            paginacionResponse.setTotalItems(planTratamientoIndSeguiDocumentoPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado: " +
                    documentoList.size() + " documentos, de un total de: " + planTratamientoIndSeguiDocumentoPage.getTotalElements(), paginacionResponse);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDocumentoDTO> eliminarRelacionConDocumento(HttpServletRequest httpServletRequest, PlanTratamientoIndSeguiDocumentoDTO planTratamientoIndSeguiDocumentoDTO) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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

            String tokenDoc = planTratamientoIndSeguiDocumentoDTO.getDocumentoDTO().getTokenIdentificador();
            String tokenDetalle = planTratamientoIndSeguiDocumentoDTO.getTokenIdentificadorSeguimiento();

            PlanTratamientoIndSeguiDocumento planTratamientoIndSeguiDocumento = this.
                    planTratamientoIndSeguiDocumentoRepository.findFirstByPlanTratamientoIndSeguiTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
                            tokenDetalle,
                            tokenDoc,
                            false
                    );

            if (planTratamientoIndSeguiDocumento == null) {
                df.setMensaje("La relación entre el documento y el registro PlanTratamientoIndSegui no existe o ya fue eliminada anteriormente");
                return df;
            }

            Documento documento = planTratamientoIndSeguiDocumento.getDocumento();
            if (documento == null) {
                df.setMensaje("El detalle no presenta el documento requerido");
                return df;
            }
            planTratamientoIndSeguiDocumento.setRemovido(true);
            planTratamientoIndSeguiDocumento.setIpElimina(httpServletRequest.getRemoteAddr());
            planTratamientoIndSeguiDocumento.setFechaEliminacion(new Date());
            planTratamientoIndSeguiDocumento.setUsuarioSistemaElimina(usuarioSistema);
            this.planTratamientoIndSeguiDocumentoRepository.save(planTratamientoIndSeguiDocumento);

            df.llenarRespuestaExitosa("Se eliminado correctamente el documento: " +
                            documento.getNombreReal() + " del registro",
                    planTratamientoIndSeguiDocumentoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
