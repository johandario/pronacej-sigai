package net.latinus.sistema.integral.gestion.seguridad.service.salida;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatriz;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.Reforzamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.ReforzamientoDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.SesionReforzamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.ReforzamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RelacionEgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.SesionReforzamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ExpedienteMatrizRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.ReforzamientoDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.ReforzamientoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.SesionReforzamientoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

@Service
@Transactional
@AllArgsConstructor
public class RelacionEgresoServiceImpl implements RelacionEgresoService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private ReforzamientoRepository reforzamientoRepository;
    private SesionReforzamientoRepository sesionReforzamientoRepository;
    private ReforzamientoDocumentoRepository reforzamientoDocumentoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private ExpedienteMatrizRepository expedienteMatrizRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private CatalogoRepository catalogoRepository;
    private DocumentoRepository documentoRepository;
    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;
    private DocumentoService documentoService;
    private JerarquiaRepository jerarquiaRepository;

    private final LogService logService = new LogService(this.getClass());

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<RelacionEgresoDTO>> obtenerAdolescentes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<RelacionEgresoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

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

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var parametroDias = parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(empresa.getTokenIdentificador(), EtiquetaNemonico.PARAM_DIAS_DIFERENCIA_FIN_SENTENCIA, false);

            LocalDate fechaLimiteLocal = LocalDate.now().plusDays(Long.parseLong(parametroDias.getValor()));
            Date fechaLimite = Date.from(fechaLimiteLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Jerarquia centro = jerarquiaRepository.findByTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            var listaExpedientes = expedienteMatrizRepository.findExpedientesConSentenciaPorCumplirse(fechaLimite, paginacionRequest.getTokenIdentificador());

            List<RelacionEgresoDTO> relacionEgresoDTOList = new ArrayList<>();

            for (ExpedienteMatriz expediente : listaExpedientes) {

                RelacionEgresoDTO relacionEgresoDTO = new RelacionEgresoDTO();
                relacionEgresoDTO.setNumExpediente(expediente.getNumExpediente());
                relacionEgresoDTO.setNombres(expediente.getFichaIdentificacion().getNombres());
                relacionEgresoDTO.setApellidoPaterno(expediente.getFichaIdentificacion().getApellidoPaterno());
                relacionEgresoDTO.setApellidoMaterno(expediente.getFichaIdentificacion().getApellidoMaterno());
                relacionEgresoDTO.setTipoDocumento(expediente.getFichaIdentificacion().getTipoIdentificacion().getNombre());
                relacionEgresoDTO.setNumDocumento(expediente.getFichaIdentificacion().getNumeroIdentificacion());
                relacionEgresoDTO.setTokenExpediente(expediente.getTokenIdentificador());
                relacionEgresoDTO.setTokenFichaIdentificacion(expediente.getFichaIdentificacion().getTokenIdentificador());

                relacionEgresoDTOList.add(relacionEgresoDTO);
            }

            relacionEgresoDTOList.sort((a, b) -> b.getNumExpediente().compareTo(a.getNumExpediente()));

            PaginacionResponse<RelacionEgresoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse = paginacionService.obtenerDatos(relacionEgresoDTOList, paginacionRequest);

            respuesta.llenarRespuestaExitosa("Adolescentes", paginacionResponse);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ReforzamientoDTO>> obtenerReforzamientos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<ReforzamientoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaReforzamientos = reforzamientoRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            List<ReforzamientoDTO> reforzamientoDTOList = new ArrayList<>();
            FichaIdentificacion fichaIdentificacion = null;

            for (Reforzamiento reforzamiento : listaReforzamientos) {
                if (fichaIdentificacion == null) {
                    fichaIdentificacion = reforzamiento.getFichaIdentificacion();
                }

                ReforzamientoDTO reforzamientoDTO = new ReforzamientoDTO();
                reforzamientoDTO.setIdReforzamiento(reforzamiento.getIdReforzamiento());
                reforzamientoDTO.setPlanVida(reforzamiento.getPlanVida());
                reforzamientoDTO.setTokenFichaIdentificacion(reforzamiento.getFichaIdentificacion().getTokenIdentificador());
                reforzamientoDTO.setIdFichaIdentificacion(reforzamiento.getFichaIdentificacion().getIdFichaIdentificacion());

                List<SesionReforzamiento> sesiones = sesionReforzamientoRepository.findByReforzamientoTokenIdentificadorAndRemovido(reforzamiento.getTokenIdentificador(), false);

                // Ordenar las sesiones por fechaCreacion de forma descendente (la más reciente primero)
                SesionReforzamiento ultimaSesion = sesiones.stream()
                        .max(Comparator.comparing(SesionReforzamiento::getFechaSesion))
                        .orElse(null); // Devuelve null si no hay sesiones

                if (ultimaSesion != null) {
                    reforzamientoDTO.setFechaUltimaSesion(ultimaSesion.getFechaSesion());
                    reforzamientoDTO.setTipoUltimaSesion(ultimaSesion.getTipoSesion().getNombre());
                    reforzamientoDTO.setResponsableUltimaSesion(ultimaSesion.getNombreResponsable());
                    reforzamientoDTO.setObservacionesUltimaSesion(ultimaSesion.getObservaciones());
                    reforzamientoDTO.setFechaUltimaSesionFormateada(FuncionesAyuda.fechaATexto(ultimaSesion.getFechaSesion(), false, false, "dd-MM-yyyy"));
                }

                reforzamientoDTO.setNumeroSesiones(sesiones.size());
                reforzamientoDTO.setFechaCreacion(reforzamiento.getFechaCreacion());
                reforzamientoDTO.setTokenIdentificador(reforzamiento.getTokenIdentificador());
                reforzamientoDTO.setFechaCreacionFormateada(FuncionesAyuda.fechaATexto(reforzamiento.getFechaCreacion(), true, false));

                if (reforzamiento.getFichaIdentificacion() != null) {
                    reforzamientoDTO.setTokenFichaIdentificacion(reforzamiento.getFichaIdentificacion().getTokenIdentificador());
                }

                reforzamientoDTOList.add(reforzamientoDTO);
            }

            reforzamientoDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            reforzamientoDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            PaginacionResponse<ReforzamientoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse = paginacionService.obtenerDatos(reforzamientoDTOList, paginacionRequest);

            // Mensajes con nombres completos y DNI
            String nombresCompletos = "N/A";
            String identificacionPersona = "N/A";

            if (fichaIdentificacion != null) {
                nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
            }

            // Mensaje para el usuario
            String mensajeUsuario = "Reforzamientos de " + nombresCompletos + " (" + identificacionPersona + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + paginacionResponse.getTotalItems() + " actividades de reforzamiento de la persona con identificación: " + identificacionPersona;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ReforzamientoDTO> obtenerReforzamientoPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<ReforzamientoDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ReforzamientoDTO reforzamientoDTO = new Gson().fromJson(body, ReforzamientoDTO.class);

            Reforzamiento reforzamiento = reforzamientoRepository.findByTokenIdentificadorAndRemovido(reforzamientoDTO.getTokenIdentificador(), false);

            reforzamientoDTO = new ReforzamientoDTO();
            reforzamientoDTO.setIdReforzamiento(reforzamiento.getIdReforzamiento());
            reforzamientoDTO.setPlanVida(reforzamiento.getPlanVida());
            reforzamientoDTO.setTokenFichaIdentificacion(reforzamiento.getFichaIdentificacion().getTokenIdentificador());
            reforzamientoDTO.setFechaCreacion(reforzamiento.getFechaCreacion());
            reforzamientoDTO.setTokenIdentificador(reforzamiento.getTokenIdentificador());
            reforzamientoDTO.setSesiones(new ArrayList<>());

            List<SesionReforzamiento> sesiones = sesionReforzamientoRepository.findByReforzamientoTokenIdentificadorAndRemovido(reforzamiento.getTokenIdentificador(), false);

            for (SesionReforzamiento sesion : sesiones) {

                SesionReforzamientoDTO sesionDTO = new SesionReforzamientoDTO();
                sesionDTO.setIdSesionReforzamiento(sesion.getIdSesionReforzamiento());
                sesionDTO.setTokenReforzamiento(reforzamiento.getTokenIdentificador());
                sesionDTO.setFechaSesion(sesion.getFechaSesion());
                sesionDTO.setNemonicoTipoSesion(sesion.getTipoSesion().getNemonico());
                sesionDTO.setNombretipoSesion(sesion.getTipoSesion().getNombre());
                sesionDTO.setNombreResponsable(sesion.getNombreResponsable());
                sesionDTO.setObservaciones(sesion.getObservaciones());
                sesionDTO.setArchivo(sesion.getDocumento().getNombreReal());
                sesionDTO.setFechaCreacion(sesion.getFechaCreacion());
                sesionDTO.setTokenIdentificador(sesion.getTokenIdentificador());

                reforzamientoDTO.getSesiones().add(sesionDTO);
            }

            // Mensajes con nombres completos y DNI
            FichaIdentificacion fichaIdentificacion = reforzamiento.getFichaIdentificacion();
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Mensaje para el usuario
            String mensajeUsuario = "Reforzamiento de " + nombresCompletos + " (" + identificacionPersona + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se ha obtenido el detalle de la actividad de reforzamiento de la persona con identificación: " + identificacionPersona;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, reforzamientoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> crearReforzamiento(HttpServletRequest httpServletRequest,
                                                                    MultipartFile[] multipartFiles,
                                                                    MultipartFile[] constancias,
                                                                    BodyEncriptado bodyEncriptado) {

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
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ReforzamientoDTO reforzamientoDTO = new Gson().fromJson(body, ReforzamientoDTO.class);

            var fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(reforzamientoDTO.getTokenFichaIdentificacion(), false);

            if (fichaIdentificacion == null || fichaIdentificacion.getTokenIdentificador() == null) {
                respuesta.setMensaje("Se recibio una ficha de identificación inválida");
                return respuesta;
            }

            Catalogo catalogoCarpeta = catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.CARPETA_GESTION_ADOLES_ACTIVIDADES_REFORZAMIENTO, false);

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

            List<DocumentoDTO> documentoDTOList = reforzamientoDTO.getReforzamientoDocumentoDTO().getDocumentoDTOList();

            String idNodo = carpeta.getIdentificadorAlfresco();

            Reforzamiento reforzamiento = new Reforzamiento();
            reforzamiento.setPlanVida(reforzamientoDTO.getPlanVida());
            reforzamiento.setFichaIdentificacion(fichaIdentificacion);

            reforzamiento.setIpCrea(httpServletRequest.getRemoteAddr());
            reforzamiento.setUsuarioSistemaCrea(usuarioSistema);

            var reforzamientoCreado = reforzamientoRepository.save(reforzamiento);

            int archivoIndex = 0; // Para asignar archivos solo a nuevas sesiones
            Catalogo tipoDocumentoSesion = catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.NEMONICO_TIPO_DOCUMENTO_CONSTANCIA_SESION_REFORZAMIENTO, false);

            StringBuilder detallesSesiones = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            for (SesionReforzamientoDTO sesionDTO : reforzamientoDTO.getSesiones()) {

                SesionReforzamiento sesionReforzamiento = new SesionReforzamiento();
                sesionReforzamiento.setReforzamiento(reforzamientoCreado);
                sesionReforzamiento.setFechaSesion(sesionDTO.getFechaSesion());

                Catalogo tipoSesion = catalogoRepository.findByNemonicoAndRemovido(sesionDTO.getNemonicoTipoSesion(), false);
                sesionReforzamiento.setTipoSesion(tipoSesion);

                sesionReforzamiento.setNombreResponsable(sesionDTO.getNombreResponsable());
                sesionReforzamiento.setObservaciones(sesionDTO.getObservaciones());

                sesionReforzamiento.setIpCrea(httpServletRequest.getRemoteAddr());
                sesionReforzamiento.setUsuarioSistemaCrea(usuarioSistema);

                // Agregar detalles de la sesión para auditoría
                if (detallesSesiones.length() > 0) {
                    detallesSesiones.append(", ");
                }
                detallesSesiones.append("Fecha: ")
                        .append(dateFormat.format(sesionDTO.getFechaSesion()))
                        .append(" - Tipo: ")
                        .append(sesionDTO.getNombretipoSesion())
                        .append(" - Responsable: ")
                        .append(sesionDTO.getNombreResponsable());

                if (sesionDTO.getIdSesionReforzamiento() == null && constancias != null && archivoIndex < constancias.length) {
                    MultipartFile archivoConstancia = constancias[archivoIndex];
                    archivoIndex++;

                    DocumentoDTO documentoDTO = sesionDTO.getDocumentoDTO();
                    CatalogoDTO catalogoDTO = new CatalogoDTO();
                    catalogoDTO.setTokenIdentificador(tipoDocumentoSesion.getTokenIdentificador());
                    documentoDTO.setTipoDocumentoSistema(catalogoDTO);

                    RespuestaPorDefectoAuditoria<DocumentoDTO> respuestaDocumento = this.documentoService.subirDocumentoAlfresco(
                            httpServletRequest, idNodo, archivoConstancia, documentoDTO);

                    if (!respuestaDocumento.isExito()) {
                        respuesta.setMensaje(respuestaDocumento.getMensaje());
                        respuesta.setMensajeErrorReal(respuestaDocumento.getMensajeErrorReal());
                        return respuesta;
                    }

                    documentoDTO = respuestaDocumento.getData();
                    Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                            documentoDTO.getTokenIdentificador(), false);

                    sesionReforzamiento.setCarpeta(carpeta);
                    sesionReforzamiento.setDocumento(documento);
                }

                sesionReforzamientoRepository.save(sesionReforzamiento);
            }

            Boolean ingresoActa = false;

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

                    ReforzamientoDocumento reforzamientoDocumento = new ReforzamientoDocumento();
                    reforzamientoDocumento.setCarpeta(carpeta);
                    reforzamientoDocumento.setReforzamiento(reforzamientoCreado);
                    reforzamientoDocumento.setDocumento(documento);
                    reforzamientoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                    reforzamientoDocumento.setUsuarioSistemaCrea(usuarioSistema);
                    reforzamientoDocumentoRepository.save(reforzamientoDocumento);
                }
            }

            // Mensajes con nombres completos y DNI
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Obtener información adicional para auditoría
            String fechaCreacion = dateFormat.format(reforzamientoCreado.getFechaCreacion());
            String planVida = reforzamientoCreado.getPlanVida() ? "Sí" : "No";
            int numeroSesiones = reforzamientoDTO.getSesiones().size();

            // Mensaje para el usuario
            String mensajeUsuario = "Reforzamiento Creado para " + nombresCompletos + " (" + identificacionPersona + ")";

            // Mensaje para auditoría (incluye detalles de sesiones)
            String mensajeAuditoria = "Se creó con éxito la actividad de reforzamiento del " + fechaCreacion +
                    ", plan de vida: " + planVida + ", " + numeroSesiones + " sesiones de la persona con identificación: " + identificacionPersona;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarReforzamiento(HttpServletRequest httpServletRequest,
                                                                         MultipartFile[] constancias,
                                                                         BodyEncriptado bodyEncriptado) {

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
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ReforzamientoDTO reforzamientoDTO = new Gson().fromJson(body, ReforzamientoDTO.class);

            Reforzamiento reforzamiento = reforzamientoRepository.findByTokenIdentificadorAndRemovido(reforzamientoDTO.getTokenIdentificador(), false);

            if (reforzamiento == null) {
                respuesta.setMensaje("No se encontró el reforzamiento especificado");
                return respuesta;
            }

            var fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(reforzamiento.getFichaIdentificacion().getTokenIdentificador(), false);

            Catalogo catalogoCarpeta = catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.CARPETA_GESTION_ADOLES_ACTIVIDADES_REFORZAMIENTO, false);

            if (catalogoCarpeta == null) {
                respuesta.setMensaje("No existe el catalogo de la carpeta especificada");
                return respuesta;
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            String nemonicoCarpeta = catalogoCarpeta.getNemonico();
            Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage = this.fichaIdentificacionCarpetaRepository.
                    findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                            reforzamiento.getFichaIdentificacion().getTokenIdentificador(),
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


            reforzamiento.setPlanVida(reforzamientoDTO.getPlanVida());

            reforzamiento.setIpEdita(httpServletRequest.getRemoteAddr());
            reforzamiento.setUsuarioSistemaEdita(usuarioSistema);

            reforzamientoRepository.save(reforzamiento);

            int archivoIndex = 0; // Para asignar archivos solo a nuevas sesiones
            Catalogo tipoDocumentoSesion = catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.NEMONICO_TIPO_DOCUMENTO_CONSTANCIA_SESION_REFORZAMIENTO, false);

            StringBuilder detallesSesionesNuevas = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            for (SesionReforzamientoDTO sesionDTO : reforzamientoDTO.getSesiones()) {

                if (sesionDTO.getTokenIdentificador() != null)
                    continue;

                SesionReforzamiento sesionReforzamiento = new SesionReforzamiento();
                sesionReforzamiento.setReforzamiento(reforzamiento);
                sesionReforzamiento.setFechaSesion(sesionDTO.getFechaSesion());

                Catalogo tipoSesion = catalogoRepository.findByNemonicoAndRemovido(sesionDTO.getNemonicoTipoSesion(), false);
                sesionReforzamiento.setTipoSesion(tipoSesion);

                sesionReforzamiento.setNombreResponsable(sesionDTO.getNombreResponsable());
                sesionReforzamiento.setObservaciones(sesionDTO.getObservaciones());

                sesionReforzamiento.setIpCrea(httpServletRequest.getRemoteAddr());
                sesionReforzamiento.setUsuarioSistemaCrea(usuarioSistema);

                // Agregar detalles de las nuevas sesiones para auditoría
                if (detallesSesionesNuevas.length() > 0) {
                    detallesSesionesNuevas.append(", ");
                }
                detallesSesionesNuevas.append("Fecha: ")
                        .append(dateFormat.format(sesionDTO.getFechaSesion()))
                        .append(" - Tipo: ")
                        .append(sesionDTO.getNombretipoSesion())
                        .append(" - Responsable: ")
                        .append(sesionDTO.getNombreResponsable());

                if (sesionDTO.getIdSesionReforzamiento() == null && constancias != null && archivoIndex < constancias.length) {
                    MultipartFile archivoConstancia = constancias[archivoIndex];
                    archivoIndex++;

                    DocumentoDTO documentoDTO = sesionDTO.getDocumentoDTO();
                    CatalogoDTO catalogoDTO = new CatalogoDTO();
                    catalogoDTO.setTokenIdentificador(tipoDocumentoSesion.getTokenIdentificador());
                    documentoDTO.setTipoDocumentoSistema(catalogoDTO);

                    RespuestaPorDefectoAuditoria<DocumentoDTO> respuestaDocumento = this.documentoService.subirDocumentoAlfresco(
                            httpServletRequest, idNodo, archivoConstancia, documentoDTO);

                    if (!respuestaDocumento.isExito()) {
                        respuesta.setMensaje(respuestaDocumento.getMensaje());
                        respuesta.setMensajeErrorReal(respuestaDocumento.getMensajeErrorReal());
                        return respuesta;
                    }

                    documentoDTO = respuestaDocumento.getData();
                    Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                            documentoDTO.getTokenIdentificador(), false);

                    sesionReforzamiento.setCarpeta(carpeta);
                    sesionReforzamiento.setDocumento(documento);
                }

                sesionReforzamientoRepository.save(sesionReforzamiento);
            }

            // Mensajes con nombres completos y DNI
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Obtener información adicional para auditoría
            String fechaCreacion = dateFormat.format(reforzamiento.getFechaCreacion());
            String planVida = reforzamiento.getPlanVida() ? "Sí" : "No";

            // Obtener número total de sesiones actual
            List<SesionReforzamiento> sesionesTotales = sesionReforzamientoRepository.findByReforzamientoTokenIdentificadorAndRemovido(reforzamiento.getTokenIdentificador(), false);
            int numeroSesiones = sesionesTotales.size();

            // Mensaje para el usuario
            String mensajeUsuario = "Reforzamiento actualizado para " + nombresCompletos + " (" + identificacionPersona + ")";

            // Mensaje para auditoría (incluye detalles de nuevas sesiones si las hay)
            String mensajeAuditoria = "Se editó con éxito la actividad de reforzamiento del " + fechaCreacion +
                    ", plan de vida: " + planVida + ", " + numeroSesiones + " sesiones de la persona con identificación: " + identificacionPersona;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> removerReforzamiento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

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
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ReforzamientoDTO reforzamientoDTO = new Gson().fromJson(body, ReforzamientoDTO.class);

            Reforzamiento reforzamiento = reforzamientoRepository.findByTokenIdentificadorAndRemovido(reforzamientoDTO.getTokenIdentificador(), false);

            // Obtener sesiones antes de eliminar para el mensaje de auditoría
            List<SesionReforzamiento> sesiones = sesionReforzamientoRepository.findByReforzamientoTokenIdentificadorAndRemovido(reforzamiento.getTokenIdentificador(), false);

            StringBuilder detallesSesiones = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            for (SesionReforzamiento sesion : sesiones) {
                if (detallesSesiones.length() > 0) {
                    detallesSesiones.append(", ");
                }
                detallesSesiones.append("Fecha: ")
                        .append(dateFormat.format(sesion.getFechaSesion()))
                        .append(" - Tipo: ")
                        .append(sesion.getTipoSesion().getNombre())
                        .append(" - Responsable: ")
                        .append(sesion.getNombreResponsable());
            }

            reforzamiento.setRemovido(true);

            reforzamiento.setIpElimina(httpServletRequest.getRemoteAddr());
            reforzamiento.setUsuarioSistemaElimina(usuarioSistema);

            reforzamientoRepository.save(reforzamiento);

            // Mensajes con nombres completos y DNI
            FichaIdentificacion fichaIdentificacion = reforzamiento.getFichaIdentificacion();
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Obtener información adicional para auditoría
            String fechaCreacion = dateFormat.format(reforzamiento.getFechaCreacion());
            String planVida = reforzamiento.getPlanVida() ? "Sí" : "No";
            int numeroSesiones = sesiones.size();

            // Mensaje para el usuario
            String mensajeUsuario = "Removido reforzamiento de " + nombresCompletos + " (" + identificacionPersona + ")";

            // Mensaje para auditoría (incluye detalles de sesiones eliminadas)
            String mensajeAuditoria = "Se eliminó con éxito la actividad de reforzamiento del " + fechaCreacion +
                    ", plan de vida: " + planVida + ", " + numeroSesiones + " sesiones de la persona con identificación: " + identificacionPersona;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

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

            Reforzamiento reforzamiento = reforzamientoRepository.findByTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            List<ReforzamientoDocumento> documentosReforzamiento = this.reforzamientoDocumentoRepository
                    .findByReforzamientoTokenIdentificadorAndRemovido(
                            reforzamiento.getTokenIdentificador(),
                            false);

            List<SesionReforzamiento> sesiones = sesionReforzamientoRepository.findByReforzamientoTokenIdentificadorAndRemovido(reforzamiento.getTokenIdentificador(), false);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (ReforzamientoDocumento refDoc : documentosReforzamiento) {
                Documento documento = refDoc.getDocumento();

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

            for (SesionReforzamiento sesion : sesiones) {
                Documento documento = sesion.getDocumento();

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

            documentoList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<DocumentoDTO> paginacionResponse = paginacionService.obtenerDatos(documentoList, paginacionRequest);

            // Mensajes con nombres completos y DNI
            FichaIdentificacion fichaIdentificacion = reforzamiento.getFichaIdentificacion();
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Mensaje para el usuario
            String mensajeUsuario = "Se han encontrado " + documentoList.size() + " documentos, de un total de " + paginacionResponse.getTotalItems() +
                    " de " + nombresCompletos + " (" + identificacionPersona + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han obtenido los documentos de la actividad de reforzamiento de la persona con identificación: " + identificacionPersona;

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
        } else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getNumeroIdentificacion();
        } else {
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }
}