package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatriz;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.HistoricoEntradaSalida;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamientoDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.HistoricoEntradaSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ActaExternamientoDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ActaExternamientoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ExpedienteMatrizRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.HistoricoFichaIdentificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
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
public class ActaExternamientoServiceImpl implements ActaExternamientoService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private CatalogoRepository catalogoRepository;
    private DocumentoRepository documentoRepository;
    private ActaExternamientoRepository actaExternamientoRepository;
    private ActaExternamientoDocumentoRepository actaExternamientoDocumentoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private ExpedienteMatrizRepository expedienteMatrizRepository;
    private PaginacionService paginacionService;
    private JwtProviderService jwtProviderService;
    private DocumentoService documentoService;
    private HistoricoEntradaSalidaRepository historicoEntradaSalidaRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ActaExternamientoDTO>> obtenerActasExternamiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<ActaExternamientoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaActas = actaExternamientoRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            List<ActaExternamientoDTO> actaDTOList = new ArrayList<>();
            for (ActaExternamiento acta : listaActas) {

                ActaExternamientoDTO actaDTO = new ActaExternamientoDTO();
                actaDTO.setIdActaExternamiento(acta.getIdActaExternamiento());
                actaDTO.setFechaRegistro(acta.getFechaRegistro());
                actaDTO.setIngreso(acta.getIngreso());
                actaDTO.setInstitucion(acta.getInstitucion());
                actaDTO.setAutorizacion(acta.getAutorizacion());
                actaDTO.setTipoDocumento(acta.getTipoDocumento().getNombre());
                actaDTO.setNemonicoTipoDocumento(acta.getTipoDocumento().getNemonico());
                actaDTO.setNumeroDocumento(acta.getNumeroDocumento());
                actaDTO.setResolucion(acta.getResolucion());
                actaDTO.setDomicilio(acta.getDomicilio());
                actaDTO.setMandatoDetencion(acta.getMandatoDetencion());
                actaDTO.setRetiroSolo(acta.getRetiroSolo());
                actaDTO.setImpreso(acta.getImpreso());
                actaDTO.setFirmado(acta.getFirmado());
                actaDTO.setFamiliares(acta.getFamiliares());
                actaDTO.setParentescos(acta.getParentescos());
                actaDTO.setIdentificaciones(acta.getIdentificaciones());
                actaDTO.setDirecciones(acta.getDirecciones());
                actaDTO.setTelefonos(acta.getTelefonos());
                actaDTO.setObservaciones(acta.getObservaciones());
                actaDTO.setTokenFichaIdentificacion(acta.getFichaIdentificacion().getTokenIdentificador());
                actaDTO.setTokenExpedienteMatriz(acta.getExpedienteMatriz().getTokenIdentificador());
                actaDTO.setNumeroExpedienteMatriz(acta.getExpedienteMatriz().getNumExpediente());
                actaDTO.setFechaCreacion(acta.getFechaCreacion());
                actaDTO.setTokenIdentificador(acta.getTokenIdentificador());
                actaDTO.setIsComplete(acta.getIsComplete());

                actaDTOList.add(actaDTO);
            }

            actaDTOList.sort((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()));

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            actaDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            PaginacionResponse<ActaExternamientoDTO> paginacionResponse = paginacionService.obtenerDatos(actaDTOList, paginacionRequest);

            respuesta.llenarRespuestaExitosa("Se han encontrado un total de: " + actaDTOList.size() + " elementos disponibles.",
                    paginacionResponse);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> crearActaExternamiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ActaExternamientoDTO actaDTO = new Gson().fromJson(body, ActaExternamientoDTO.class);

            ActaExternamiento actaExternamiento = new ActaExternamiento();

            actaExternamiento.setFechaRegistro(actaDTO.getFechaRegistro());
            actaExternamiento.setIngreso(actaDTO.getIngreso());
            actaExternamiento.setInstitucion(actaDTO.getInstitucion());

            Catalogo tipoDocumento = catalogoRepository.findByNemonicoAndRemovido(actaDTO.getNemonicoTipoDocumento(), false);

            actaExternamiento.setAutorizacion(actaDTO.getAutorizacion());
            actaExternamiento.setTipoDocumento(tipoDocumento);
            actaExternamiento.setNumeroDocumento(actaDTO.getNumeroDocumento());
            actaExternamiento.setResolucion(actaDTO.getResolucion());
            actaExternamiento.setDomicilio(actaDTO.getDomicilio());
            actaExternamiento.setMandatoDetencion(actaDTO.getMandatoDetencion());
            actaExternamiento.setRetiroSolo(actaDTO.getRetiroSolo());
            actaExternamiento.setFamiliares(actaDTO.getFamiliares());
            actaExternamiento.setParentescos(actaDTO.getParentescos());
            actaExternamiento.setIdentificaciones(actaDTO.getIdentificaciones());
            actaExternamiento.setDirecciones(actaDTO.getDirecciones());
            actaExternamiento.setTelefonos(actaDTO.getTelefonos());
            actaExternamiento.setObservaciones(actaDTO.getObservaciones());

            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(actaDTO.getTokenFichaIdentificacion(), false);
            actaExternamiento.setFichaIdentificacion(fichaIdentificacion);

            ExpedienteMatriz expedienteMatriz = expedienteMatrizRepository.findByTokenIdentificadorAndRemovido(actaDTO.getTokenExpedienteMatriz(), false);
            actaExternamiento.setExpedienteMatriz(expedienteMatriz);


            HistoricoEntradaSalida historico = new HistoricoEntradaSalida();
            if (fichaIdentificacion != null) {
                historico.setNumeroIdentificacion(fichaIdentificacion.getNumeroIdentificacion());
                historico.setFichaIdentificacion(fichaIdentificacion);
            } else {
                throw new IllegalArgumentException("Ficha de identificación no encontrada para la fuga.");
            }
            historico.setFechaEntrada(new Date());
            historico.setRegistroActivo(true);
            historico.setExternamiento(actaExternamiento);
            historico.setFechaSalida(actaExternamiento.getFechaRegistro());
            historico.setMotivoSalida(this.catalogoRepository.findByNemonicoAndRemovido("SALIDA_EXTERNAMIENTO", false));
            this.historicoEntradaSalidaRepository.save(historico);
            actaExternamientoRepository.save(actaExternamiento);

            // Modificar ficha de identificación -> tieneProceso = false
            fichaIdentificacion.setTieneProceso(false);
            fichaIdentificacionRepository.save(fichaIdentificacion);

            respuesta.llenarRespuestaExitosa("Creado", true);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarActaExternamiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ActaExternamientoDTO actaDTO = new Gson().fromJson(body, ActaExternamientoDTO.class);

            ActaExternamiento actaExternamiento = actaExternamientoRepository.findByIdActaExternamientoAndRemovido(actaDTO.getIdActaExternamiento(), false);

            if (actaExternamiento == null) {
                respuesta.setMensaje("No se encontró el acta de externamiento.");
                return respuesta;
            }

            actaExternamiento.setIngreso(actaDTO.getIngreso());
            actaExternamiento.setInstitucion(actaDTO.getInstitucion());

            Catalogo tipoDocumento = catalogoRepository.findByNemonicoAndRemovido(actaDTO.getNemonicoTipoDocumento(), false);

            actaExternamiento.setAutorizacion(actaDTO.getAutorizacion());
            actaExternamiento.setTipoDocumento(tipoDocumento);
            actaExternamiento.setNumeroDocumento(actaDTO.getNumeroDocumento());
            actaExternamiento.setNumeroDocumento(actaDTO.getNumeroDocumento());
            actaExternamiento.setResolucion(actaDTO.getResolucion());
            actaExternamiento.setDomicilio(actaDTO.getDomicilio());
            actaExternamiento.setMandatoDetencion(actaDTO.getMandatoDetencion());
            actaExternamiento.setRetiroSolo(actaDTO.getRetiroSolo());
            actaExternamiento.setFamiliares(actaDTO.getFamiliares());
            actaExternamiento.setParentescos(actaDTO.getParentescos());
            actaExternamiento.setIdentificaciones(actaDTO.getIdentificaciones());
            actaExternamiento.setDirecciones(actaDTO.getDirecciones());
            actaExternamiento.setTelefonos(actaDTO.getTelefonos());
            actaExternamiento.setImpreso(actaDTO.getImpreso());
            actaExternamiento.setFirmado(actaDTO.getFirmado());
            actaExternamiento.setObservaciones(actaDTO.getObservaciones());

            actaExternamientoRepository.save(actaExternamiento);

            respuesta.llenarRespuestaExitosa("Actualizado", true);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> subirActaFirmada(HttpServletRequest httpServletRequest, MultipartFile multipartFile, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(df2.getLogOut());
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ActaExternamientoDTO actaExternamientoDTO = new Gson().fromJson(body, ActaExternamientoDTO.class);

            ActaExternamiento actaExternamiento = actaExternamientoRepository.findByIdActaExternamientoAndRemovido(actaExternamientoDTO.getIdActaExternamiento(), false);

            if (actaExternamiento == null || actaExternamiento.getTokenIdentificador() == null) {
                respuesta.setMensaje("El acta de externamiento no existe o ha sido removida");
                return respuesta;
            }

            FichaIdentificacion fichaIdentificacion = actaExternamiento.getFichaIdentificacion();

            if (fichaIdentificacion == null || fichaIdentificacion.getTokenIdentificador() == null) {
                respuesta.setMensaje("Se recibio una ficha de identificación inválida");
                return respuesta;
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            String nemonicoCarpeta = EtiquetaNemonico.CARPETA_GESTION_ADOLES_LEGAL;
            Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage = this.fichaIdentificacionCarpetaRepository.
                    findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            fichaIdentificacion.getTokenIdentificador(),
                            nemonicoCarpeta,
                            false,
                            pageable
                    );

            if (fichaIdentificacionCarpetaPage.isEmpty()) {
                respuesta.setMensaje("No se ha creado una carpeta para guardar los informes que pertenezca a la ficha de identificación solicitada");
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
            DocumentoDTO documentoDTO = actaExternamientoDTO.getActaExternamientoDocumentoDTO().getDocumentoDTO();
            RespuestaPorDefectoAuditoria<DocumentoDTO> respuestaDocumento = this.documentoService.subirDocumentoAlfresco(
                    httpServletRequest, idNodo, multipartFile, documentoDTO
            );

            if (!respuestaDocumento.isExito()) {
                respuesta.setMensaje(respuestaDocumento.getMensaje());
                respuesta.setMensajeErrorReal(respuestaDocumento.getMensajeErrorReal());
                return respuesta;
            }

            documentoDTO = respuestaDocumento.getData();
            Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                    documentoDTO.getTokenIdentificador(), false
            );

            ActaExternamientoDocumento actaExternamientoDocumento = new ActaExternamientoDocumento();
            actaExternamientoDocumento.setActaExternamiento(actaExternamiento);
            actaExternamientoDocumento.setCarpeta(carpeta);
            actaExternamientoDocumento.setDocumento(documento);
            actaExternamientoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            actaExternamientoDocumento.setUsuarioSistemaCrea(usuarioSistema);
            actaExternamientoDocumentoRepository.save(actaExternamientoDocumento);

            actaExternamiento.setFirmado(true);
            actaExternamientoRepository.save(actaExternamiento);

            respuesta.llenarRespuestaExitosa("Se ha subido correctamente el documento.", true);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarActaExternamiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ActaExternamientoDTO actaDTO = new Gson().fromJson(body, ActaExternamientoDTO.class);

            ActaExternamiento actaExternamiento = actaExternamientoRepository.findByIdActaExternamientoAndRemovido(actaDTO.getIdActaExternamiento(), false);

            if (actaExternamiento == null) {
                respuesta.setMensaje("No se encontró el acta de externamiento.");
                return respuesta;
            }

            actaExternamiento.setRemovido(true);

            actaExternamientoRepository.save(actaExternamiento);

            respuesta.llenarRespuestaExitosa("Removido", true);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }


}
