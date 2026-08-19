package net.latinus.sistema.integral.gestion.seguridad.service.general;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.informe.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.CampoInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.InformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.PlantillaInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.ValorInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.informe.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;

@Service
@Transactional
@AllArgsConstructor
public class InformeServiceImpl implements InformeService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private InformeRepository informeRepository;
    private PlantillaInformeRepository plantillaInformeRepository;
    private CampoRepository campoRepository;
    private ValorRepository valorRepository;
    private CatalogoRepository catalogoRepository;
    private DocumentoRepository documentoRepository;
    private InformeDocumentoRepository informeDocumentoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private JerarquiaRepository jerarquiaRepository;
    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;
    private DocumentoService documentoService;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> obtenerInformes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            var listaInformes = informeRepository.findByRemovido(false);

            List<InformeDTO> informeDTOList = new ArrayList<>();
            for (Informe informe : listaInformes) {

                InformeDTO informeListaDTO = new InformeDTO();
                informeListaDTO.setIdInforme(informe.getIdInforme());
                informeListaDTO.setFechaRegistro(informe.getFechaRegistro());
                informeListaDTO.setAsignado(informe.getFichaIdentificacion().getNombres() + " " + informe.getFichaIdentificacion().getApellidoPaterno() + " " + informe.getFichaIdentificacion().getApellidoMaterno());
                informeListaDTO.setIdFichaIdentificacion(informe.getFichaIdentificacion().getIdFichaIdentificacion());
                informeListaDTO.setImpreso(informe.getImpreso());
                informeListaDTO.setFirmado(informe.getFirmado());
                informeListaDTO.setIdPlantillaInforme(informe.getPlantillaInforme().getIdPlantillaInforme());
                informeListaDTO.setFechaCreacion(informe.getFechaCreacion());
                informeListaDTO.setTokenIdentificador(informe.getTokenIdentificador());
                informeListaDTO.setIdInformePadre(
                        informe.getInformePadre() != null ? informe.getInformePadre().getIdInforme() : null
                );
                informeListaDTO.setTipo(informe.getPlantillaInforme().getNombre());

                informeDTOList.add(informeListaDTO);
            }

            informeDTOList.sort((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()));

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            PaginacionResponse<InformeDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse = paginacionService.obtenerDatos(informeDTOList, paginacionRequest);

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Informes";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + informeDTOList.size() + " informes del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> obtenerInformesPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<InformeDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaInformes = informeRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            List<InformeDTO> informeDTOList = new ArrayList<>();
            for (Informe informe : listaInformes) {

                InformeDTO informeListaDTO = new InformeDTO();
                informeListaDTO.setIdInforme(informe.getIdInforme());
                informeListaDTO.setFechaRegistro(informe.getFechaRegistro());
                informeListaDTO.setAsignado(informe.getFichaIdentificacion().getNombres() + " " + informe.getFichaIdentificacion().getApellidoPaterno() + " " + informe.getFichaIdentificacion().getApellidoMaterno());
                informeListaDTO.setImpreso(informe.getImpreso());
                informeListaDTO.setFirmado(informe.getFirmado());
                informeListaDTO.setIdFichaIdentificacion(informe.getFichaIdentificacion().getIdFichaIdentificacion());
                informeListaDTO.setIdPlantillaInforme(informe.getPlantillaInforme().getIdPlantillaInforme());
                informeListaDTO.setFechaCreacion(informe.getFechaCreacion());
                informeListaDTO.setTokenIdentificador(informe.getTokenIdentificador());
                informeListaDTO.setIdInformePadre(
                        informe.getInformePadre() != null ? informe.getInformePadre().getIdInforme() : null
                );
                informeListaDTO.setTipo(informe.getPlantillaInforme().getNombre());

                informeDTOList.add(informeListaDTO);
            }

            informeDTOList.sort((a, b) -> b.getFechaRegistro().compareTo(a.getFechaRegistro()));

            PaginacionResponse<InformeDTO> paginacionResponse = paginacionService.obtenerDatos(informeDTOList, paginacionRequest);

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Informes";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + informeDTOList.size() + " informes por token del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<InformeDTO> obtenerInformePorId(HttpServletRequest httpServletRequest, InformeDTO informeDTO) {

        RespuestaPorDefectoAuditoria<InformeDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            var informe = informeRepository.findByIdInformeAndRemovido(informeDTO.getIdInforme(), false);

            var informeJson = new Gson().toJson(informe);
            informeDTO = new Gson().fromJson(informeJson, InformeDTO.class);

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Informe";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se obtuvo un informe específico del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, informeDTO, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Transactional
    @Override
    public RespuestaPorDefectoAuditoria<InformeDTO> crearInforme(HttpServletRequest httpServletRequest, InformeDTO informeDTO) {
        RespuestaPorDefectoAuditoria<InformeDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioResponsable = df2.getData().getUsuarioSistema();

            // Convertir DTO a entidad Informe
            Informe informe = new Informe();

            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByIdFichaIdentificacion(informeDTO.getIdFichaIdentificacion());
            informe.setFichaIdentificacion(fichaIdentificacion);

            PlantillaInforme plantillaInforme = plantillaInformeRepository.findByIdPlantillaInformeAndRemovido(informeDTO.getIdPlantillaInforme(), false);
            informe.setPlantillaInforme(plantillaInforme);

            if (informeDTO.getImpreso() != null)
                informe.setImpreso(informeDTO.getImpreso());

            // Guardar Informe en la base de datos
            Informe informeCreado = informeRepository.save(informe);

            // Construir nombre del adolescente a partir de la ficha
            String nombreCompletoAdolescente = obtenerNombresCompletos(fichaIdentificacion);

            // Asignar el nombre al DTO
            informeDTO.setNombreAdolescente(nombreCompletoAdolescente.trim());

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioResponsable);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Se creó con éxito el informe " + nombreCompletoAdolescente.trim();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se creó con éxito el informe de " + nombreCompletoAdolescente + 
                                    " (" + identificacionPersona + ") del " + fechaFormateada + 
                                    " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, informeDTO, mensajeAuditoria);

            // Guardar valores del informe
            for (ValorInformeDTO valorDTO : informeDTO.getValores()) {
                ValorInforme valor = new ValorInforme();
                valor.setInforme(informeCreado);
                valor.setValor(valorDTO.getValor());

                CampoInforme campo = campoRepository.findByIdCampoAndRemovido(valorDTO.getIdCampo(), false);
                valor.setCampoInforme(campo);

                valorRepository.save(valor);
            }
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Transactional
    @Override
    public RespuestaPorDefectoAuditoria<InformeDTO> crearInformePorToken(HttpServletRequest httpServletRequest, InformeDTO informeDTO) {

        RespuestaPorDefectoAuditoria<InformeDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioResponsable = null;
            if (dfJwt.isExito()) {
                usuarioResponsable = dfJwt.getData().getUsuarioSistema();
            }

            // Convertir DTO a entidad Informe
            Informe informe = new Informe();

            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(informeDTO.getTokenFichaIdentificacion(), false);
            informe.setFichaIdentificacion(fichaIdentificacion);

            PlantillaInforme plantillaInforme = plantillaInformeRepository.findByCatalogoNemonicoAndRemovido(informeDTO.getNemonicoPlantillaInforme(), false);
            informe.setPlantillaInforme(plantillaInforme);

            if (informeDTO.getImpreso() != null)
                informe.setImpreso(informeDTO.getImpreso());

            // Guardar Encuesta en la base de datos
            Informe informeCreado = informeRepository.save(informe);

            // Iterar sobre las secciones y preguntas para guardarlas
            for (ValorInformeDTO valorDTO : informeDTO.getValores()) {
                ValorInforme valor = new ValorInforme();
                valor.setInforme(informeCreado);
                valor.setValor(valorDTO.getValor());

                CampoInforme campo = campoRepository.findByIdCampoAndRemovido(valorDTO.getIdCampo(), false);
                valor.setCampoInforme(campo);

                valorRepository.save(valor);
            }

            informeCreado = informeRepository.findByIdInformeAndRemovido(informe.getIdInforme(), false);

            InformeDTO informeCreadoDTO = new InformeDTO();
            informeCreadoDTO.setIdInforme(informeCreado.getIdInforme());
            informeCreadoDTO.setFechaRegistro(informeCreado.getFechaRegistro());
            informeCreadoDTO.setAsignado(informeCreado.getFichaIdentificacion().getNombres() + " " + informeCreado.getFichaIdentificacion().getApellidoPaterno() + " " + informeCreado.getFichaIdentificacion().getApellidoMaterno());
            informeCreadoDTO.setImpreso(informeCreado.getImpreso());
            informeCreadoDTO.setFirmado(informeCreado.getFirmado());
            informeCreadoDTO.setIdFichaIdentificacion(informeCreado.getFichaIdentificacion().getIdFichaIdentificacion());
            informeCreadoDTO.setIdPlantillaInforme(informeCreado.getPlantillaInforme().getIdPlantillaInforme());
            informeCreadoDTO.setIdInformePadre(
                    informeCreado.getInformePadre() != null ? informeCreado.getInformePadre().getIdInforme() : null
            );
            informeCreadoDTO.setTipo(informeCreado.getPlantillaInforme().getNombre());

            // Obtener datos para el mensaje
            String nombreCompletoAdolescente = obtenerNombresCompletos(fichaIdentificacion);
            String nombreUsuarioResponsable = usuarioResponsable != null ? obtenerNombreCompletoUsuarioSistema(usuarioResponsable) : "N/A";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Informe Creado";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se creó con éxito el informe por token de " + nombreCompletoAdolescente + 
                                    " (" + identificacionPersona + ") del " + fechaFormateada + 
                                    " por el usuario " + nombreUsuarioResponsable;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, informeCreadoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Transactional
    @Override
    public RespuestaPorDefectoAuditoria<InformeDTO> actualizarInforme(HttpServletRequest httpServletRequest, InformeDTO informeDTO) {

        RespuestaPorDefectoAuditoria<InformeDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioResponsable = null;
            if (dfJwt.isExito()) {
                usuarioResponsable = dfJwt.getData().getUsuarioSistema();
            }

            Informe informe = informeRepository.findByIdInformeAndRemovido(informeDTO.getIdInforme(), false);

            if (informeDTO.getImpreso() != null || informeDTO.getFirmado() != null) {
                if (informeDTO.getImpreso() != null)
                    informe.setImpreso(informeDTO.getImpreso());

                if (informeDTO.getFirmado() != null)
                    informe.setFirmado(informeDTO.getFirmado());

                informeRepository.save(informe);
            }
            if (informeDTO.getValores() != null) {
                for (ValorInformeDTO valorDTO : informeDTO.getValores()) {
                    ValorInforme valor = valorRepository.findByInforme_IdInformeAndCampoInforme_IdCampoAndRemovido(informeDTO.getIdInforme(), valorDTO.getIdCampo(), false);
                    valor.setValor(valorDTO.getValor());
                    valorRepository.save(valor);
                }
            }

            // Obtener datos para el mensaje
            String nombreCompletoAdolescente = obtenerNombresCompletos(informe.getFichaIdentificacion());
            String nombreUsuarioResponsable = usuarioResponsable != null ? obtenerNombreCompletoUsuarioSistema(usuarioResponsable) : "N/A";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String identificacionPersona = obtenerIdentificacionPersona(informe.getFichaIdentificacion());

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Actualizado";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se actualizó con éxito el informe de " + nombreCompletoAdolescente + 
                                    " (" + identificacionPersona + ") del " + fechaFormateada + 
                                    " por el usuario " + nombreUsuarioResponsable;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, informeDTO, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Transactional
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> subirInformeFirmado(HttpServletRequest httpServletRequest, MultipartFile multipartFile, BodyEncriptado bodyEncriptado) {

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
            InformeDTO informeDTO = new Gson().fromJson(body, InformeDTO.class);

            Informe informe = informeRepository.findByIdInformeAndRemovido(informeDTO.getIdInforme(), false);

            if (informe == null || informe.getTokenIdentificador() == null) {
                respuesta.setMensaje("El informe no existe o ha sido removido");
                return respuesta;
            }

            FichaIdentificacion fichaIdentificacion = informe.getFichaIdentificacion();

            if (fichaIdentificacion == null || fichaIdentificacion.getTokenIdentificador() == null) {
                respuesta.setMensaje("Se recibio una ficha de identificación inválida");
                return respuesta;
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("idFichaIdentificacionCarpeta").descending());
            String nemonicoCarpeta = EtiquetaNemonico.CARPETA_GESTION_ADOLES_INFORMES;
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
            DocumentoDTO documentoDTO = informeDTO.getInformeDocumentoDTO().getDocumentoDTO();
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

            InformeDocumento informeDocumento = new InformeDocumento();
            informeDocumento.setInforme(informe);
            informeDocumento.setCarpeta(carpeta);
            informeDocumento.setDocumento(documento);
            informeDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
            informeDocumento.setUsuarioSistemaCrea(usuarioSistema);
            informeDocumentoRepository.save(informeDocumento);

            informe.setFirmado(true);
            informeRepository.save(informe);

            // Obtener datos para el mensaje
            String nombreCompletoAdolescente = obtenerNombresCompletos(fichaIdentificacion);
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Se ha subido correctamente el documento.";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se subió con éxito el informe firmado de " + nombreCompletoAdolescente + 
                                    " (" + identificacionPersona + ") del " + fechaFormateada + 
                                    " por el usuario " + nombreUsuarioResponsable;

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
            UsuarioSistema usuarioConsultante = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

            Page<InformeDocumento> documentosPage;

            documentosPage = this.informeDocumentoRepository.findByInformeTokenIdentificadorAndRemovido(
                    paginacionRequest.getTokenIdentificador(),
                    false,
                    pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (InformeDocumento infDoc : documentosPage.toList()) {
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

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Se han encontrado " + documentoList.size() + " documentos, de un total de " + documentosPage.getTotalElements() +
                                  ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                  " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                  "(" + usuarioConsultante.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + documentosPage.getTotalElements() + " documentos del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> removerInforme(HttpServletRequest httpServletRequest, InformeDTO informeDTO) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioResponsable = null;
            if (dfJwt.isExito()) {
                usuarioResponsable = dfJwt.getData().getUsuarioSistema();
            }

            Informe informe = informeRepository.findByIdInformeAndRemovido(informeDTO.getIdInforme(), false);
            informe.setRemovido(true);

            informeRepository.save(informe);

            // Obtener datos para el mensaje
            String nombreCompletoAdolescente = obtenerNombresCompletos(informe.getFichaIdentificacion());
            String nombreUsuarioResponsable = usuarioResponsable != null ? obtenerNombreCompletoUsuarioSistema(usuarioResponsable) : "N/A";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);
            String identificacionPersona = obtenerIdentificacionPersona(informe.getFichaIdentificacion());

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Informe removido";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito el informe de " + nombreCompletoAdolescente + 
                                    " (" + identificacionPersona + ") del " + fechaFormateada + 
                                    " por el usuario " + nombreUsuarioResponsable;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaInformeDTO>> obtenerListaPlantillasInforme(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<PlantillaInformeDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaPlantillas = plantillaInformeRepository.findByRemovido(false);

            List<PlantillaInformeDTO> plantillasDTOList = new ArrayList<>();
            for (PlantillaInforme plantilla : listaPlantillas) {

                PlantillaInformeDTO plantillaDTO = new PlantillaInformeDTO();
                plantillaDTO.setIdPlantillaInforme(plantilla.getIdPlantillaInforme());
                plantillaDTO.setNombre(plantilla.getNombre());
                plantillaDTO.setDescripcion(plantilla.getDescripcion());
                plantillaDTO.setNemonico(plantilla.getCatalogo().getNemonico());
                plantillaDTO.setNemonicoCentro(plantilla.getTipoCentro().getNemonico());
                plantillaDTO.setTipoCentro(plantilla.getTipoCentro().getNombre());
                plantillaDTO.setFechaCreacion(plantilla.getFechaCreacion());
                plantillaDTO.setTokenIdentificador(plantilla.getTokenIdentificador());

                plantillasDTOList.add(plantillaDTO);
            }

            plantillasDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<PlantillaInformeDTO> paginacionResponse = paginacionService.obtenerDatos(plantillasDTOList, paginacionRequest);

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Plantillas";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + plantillasDTOList.size() + " plantillas de informe del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<PlantillaInformeDTO>> obtenerPlantillasInforme(HttpServletRequest httpServletRequest, String tokenCentro) {

        RespuestaPorDefectoAuditoria<List<PlantillaInformeDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            List<PlantillaInforme> listaPlantillas;
            if (tokenCentro != null) {
                Jerarquia centro = jerarquiaRepository.findByTokenIdentificadorAndRemovido(tokenCentro, false);

                listaPlantillas = plantillaInformeRepository.findByTipoCentroNemonicoAndRemovido(EtiquetaNemonico.TIPO_CENTRO_TODOS, false);

                if (centro.getNombre().contains("SOA"))
                    listaPlantillas.addAll(plantillaInformeRepository.findByTipoCentroNemonicoAndRemovido(EtiquetaNemonico.TIPO_CENTRO_SOA, false));

                else if (centro.getNombre().contains("CJDR"))
                    listaPlantillas.addAll(plantillaInformeRepository.findByTipoCentroNemonicoAndRemovido(EtiquetaNemonico.TIPO_CENTRO_CJDR, false));

                else if (centro.getNombre().contains("UAPISE"))
                    listaPlantillas.addAll(plantillaInformeRepository.findByTipoCentroNemonicoAndRemovido(EtiquetaNemonico.TIPO_CENTRO_UAPISE, false));
            } else
                listaPlantillas = plantillaInformeRepository.findByRemovido(false);

            List<PlantillaInformeDTO> plantillasDTOList = new ArrayList<>();
            for (PlantillaInforme plantilla : listaPlantillas) {

                PlantillaInformeDTO plantillaDTO = new PlantillaInformeDTO();
                plantillaDTO.setIdPlantillaInforme(plantilla.getIdPlantillaInforme());
                plantillaDTO.setNombre(plantilla.getNombre());
                plantillaDTO.setDescripcion(plantilla.getDescripcion());
                plantillaDTO.setNemonico(plantilla.getCatalogo().getNemonico());
                plantillaDTO.setNemonicoCentro(plantilla.getTipoCentro().getNemonico());
                plantillaDTO.setTipoCentro(plantilla.getTipoCentro().getNombre());
                plantillaDTO.setTokenIdentificador(plantilla.getTokenIdentificador());

                plantillasDTOList.add(plantillaDTO);
            }

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Plantillas";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + plantillasDTOList.size() + " plantillas de informe del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, plantillasDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlantillaInformeDTO> obtenerPlantillaInformePorId(HttpServletRequest
                                                                                                  httpServletRequest, PlantillaInformeDTO plantillaInformeDTO) {
        return null;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlantillaInformeDTO> crearPlantillaInforme(HttpServletRequest
                                                                                           httpServletRequest, PlantillaInformeDTO plantillaInformeDTO) {

        RespuestaPorDefectoAuditoria<PlantillaInformeDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            String nemonico = "PLANTILLA_" + plantillaInformeDTO.getNombre().toUpperCase().replace(" ", "_");

            Catalogo catalogo;

            catalogo = catalogoRepository.findByNemonicoAndRemovido(nemonico, false);

            if (catalogo != null) {
                respuesta.setMensaje("Ya existe una plantilla asignada a ese formulario.");
                return respuesta;
            }

            catalogo = new Catalogo();
            catalogo.setUsuarioSistemaCrea(usuarioSistema);
            catalogo.setEmpresa(empresa);
            catalogo.setDescripcion("Plantilla de Informe: " + plantillaInformeDTO.getNombre());
            catalogo.setIpCrea(ip);
            catalogo.setNombre(plantillaInformeDTO.getNombre());
            catalogo.setNemonico(nemonico);
            catalogo.setFechaCreacion(new Date());
            catalogo.setCatalogoPadre(this.catalogoRepository.findByNemonicoAndRemovido(EtiquetaNemonico.PLANTILLAS_INFORME, false));
            catalogo = this.catalogoRepository.save(catalogo);

            // Convertir DTO a entidad PlantillaInforme
            PlantillaInforme plantilla = new PlantillaInforme();
            plantilla.setNombre(plantillaInformeDTO.getNombre());
            plantilla.setDescripcion(plantillaInformeDTO.getDescripcion());
            plantilla.setCatalogo(catalogo);

            Catalogo tipoCentro = catalogoRepository.findByNemonicoAndRemovido(plantillaInformeDTO.getNemonicoCentro(), false);
            plantilla.setTipoCentro(tipoCentro);

            // Guardar Encuesta en la base de datos
            PlantillaInforme plantillaCreada = plantillaInformeRepository.save(plantilla);

            // Iterar sobre las secciones y preguntas para guardarlas
            for (CampoInformeDTO campoDTO : plantillaInformeDTO.getCampos()) {
                CampoInforme campo = new CampoInforme();
                campo.setPlantillaInforme(plantillaCreada);
                campo.setEtiqueta(campoDTO.getEtiqueta());

                Catalogo catalogoTipo = catalogoRepository.findByNemonicoAndRemovido(campoDTO.getTipo(), false);
                campo.setTipo(catalogoTipo);

                campoRepository.save(campo);
            }

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Plantilla Informe creado";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se creó con éxito la plantilla de informe " + plantilla.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, new PlantillaInformeDTO(), mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarPlantillaInforme(HttpServletRequest httpServletRequest,
                                                                            BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioResponsable = null;
            if (dfJwt.isExito()) {
                usuarioResponsable = dfJwt.getData().getUsuarioSistema();
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PlantillaInformeDTO plantillaInformeDTO = new Gson().fromJson(body, PlantillaInformeDTO.class);

            PlantillaInforme plantillaAnterior = plantillaInformeRepository.findByTokenIdentificadorAndRemovido(plantillaInformeDTO.getTokenIdentificador(), false);

            Catalogo tipoCentro = catalogoRepository.findByNemonicoAndRemovido(plantillaInformeDTO.getNemonicoCentro(), false);

            Catalogo catalogo = plantillaAnterior.getCatalogo();

            catalogo.setNombre(plantillaInformeDTO.getNombre());
            catalogo.setDescripcion("Plantilla de Informe: " + plantillaInformeDTO.getNombre());
            catalogoRepository.save(catalogo);

            // Convertir DTO a entidad PlantillaInforme
            PlantillaInforme plantilla = new PlantillaInforme();
            plantilla.setNombre(plantillaInformeDTO.getNombre());
            plantilla.setDescripcion(plantillaInformeDTO.getDescripcion());
            plantilla.setCatalogo(catalogo);
            plantilla.setVersion(plantillaAnterior.getVersion() + 1);
            plantilla.setTipoCentro(tipoCentro);

            // Guardar Plantilla en la base de datos
            PlantillaInforme plantillaCreada = plantillaInformeRepository.save(plantilla);

            // Iterar sobre las secciones y preguntas para guardarlas
            for (CampoInformeDTO campoDTO : plantillaInformeDTO.getCampos()) {
                CampoInforme campo = new CampoInforme();
                campo.setPlantillaInforme(plantillaCreada);
                campo.setEtiqueta(campoDTO.getEtiqueta());

                Catalogo catalogoTipo = catalogoRepository.findByNemonicoAndRemovido(campoDTO.getTipo(), false);
                campo.setTipo(catalogoTipo);

                campoRepository.save(campo);
            }

            plantillaAnterior.setRemovido(true);
            plantillaInformeRepository.save(plantillaAnterior);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = usuarioResponsable != null ? obtenerNombreCompletoUsuarioSistema(usuarioResponsable) : "N/A";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Actualizado";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se actualizó con éxito la plantilla de informe " + plantilla.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> removerPlantillaInforme(HttpServletRequest
                                                                                 httpServletRequest, PlantillaInformeDTO plantillaInformeDTO) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioResponsable = null;
            if (dfJwt.isExito()) {
                usuarioResponsable = dfJwt.getData().getUsuarioSistema();
            }

            PlantillaInforme plantilla = plantillaInformeRepository.findByIdPlantillaInformeAndRemovido(plantillaInformeDTO.getIdPlantillaInforme(), false);
            plantilla.setRemovido(true);

            plantillaInformeRepository.save(plantilla);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = usuarioResponsable != null ? obtenerNombreCompletoUsuarioSistema(usuarioResponsable) : "N/A";
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje para el usuario (mantener formato original)
            String mensajeUsuario = "Plantilla removida";

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito la plantilla de informe " + plantilla.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> obtenerCamposPorIdPlantilla(HttpServletRequest
                                                                                                   httpServletRequest, PlantillaInformeDTO plantillaInformeDTO) {

        RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            var listaCampos = campoRepository.findByPlantillaInforme_IdPlantillaInformeAndRemovido(plantillaInformeDTO.getIdPlantillaInforme(), false);

            List<CampoInformeDTO> campoDTOList = new ArrayList<>();

            for (CampoInforme campo : listaCampos) {

                CampoInformeDTO campoDTO = new CampoInformeDTO();
                campoDTO.setIdCampo(campo.getIdCampo());
                campoDTO.setEtiqueta(campo.getEtiqueta());
                campoDTO.setTipo(campo.getTipo().getNemonico());

                campoDTOList.add(campoDTO);
            }

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Campos";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + campoDTOList.size() + " campos de plantilla del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, campoDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> obtenerCamposPorIdInforme(HttpServletRequest
                                                                                                 httpServletRequest, InformeDTO informeDTO) {

        RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            var listaCampos = campoRepository.findByPlantillaInforme_IdPlantillaInformeAndRemovido(informeDTO.getIdPlantillaInforme(), false);

            List<CampoInformeDTO> campoDTOList = new ArrayList<>();

            for (CampoInforme campo : listaCampos) {

                CampoInformeDTO campoDTO = new CampoInformeDTO();
                campoDTO.setIdCampo(campo.getIdCampo());
                campoDTO.setEtiqueta(campo.getEtiqueta());
                campoDTO.setTipo(campo.getTipo().getNemonico());

                ValorInforme valor = valorRepository.findByInforme_IdInformeAndCampoInforme_IdCampoAndRemovido(informeDTO.getIdInforme(), campo.getIdCampo(), false);
                campoDTO.setValor(valor.getValor());

                campoDTOList.add(campoDTO);
            }

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Campos";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + campoDTOList.size() + " campos de informe del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, campoDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> obtenerCamposPorNemonico(HttpServletRequest
                                                                                                httpServletRequest, String nemonico) {

        RespuestaPorDefectoAuditoria<List<CampoInformeDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener datos del JWT para auditoría
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            UsuarioSistema usuarioConsultante = null;
            if (dfJwt.isExito()) {
                usuarioConsultante = dfJwt.getData().getUsuarioSistema();
            }

            var catalogo = catalogoRepository.findByNemonicoAndRemovido(nemonico, false);
            var plantilla = plantillaInformeRepository.findByCatalogo_IdCatalogoAndRemovido(catalogo.getIdCatalogo(), false);
            var listaCampos = campoRepository.findByPlantillaInforme_IdPlantillaInformeAndRemovido(plantilla.getIdPlantillaInforme(), false);

            List<CampoInformeDTO> campoDTOList = new ArrayList<>();

            for (CampoInforme campo : listaCampos) {

                CampoInformeDTO campoDTO = new CampoInformeDTO();
                campoDTO.setIdCampo(campo.getIdCampo());
                campoDTO.setEtiqueta(campo.getEtiqueta());
                campoDTO.setTipo(campo.getTipo().getNemonico());

                campoDTOList.add(campoDTO);
            }

            // Mensaje para el usuario (mantener formato original + información del consultante)
            String mensajeUsuario = "Campos";
            if (usuarioConsultante != null) {
                mensajeUsuario += ". Consulta realizada por: " + usuarioConsultante.getUserName() + 
                                " con identificación: " + usuarioConsultante.getNumeroDeDocumento() + 
                                "(" + usuarioConsultante.getTokenIdentificador() + ")";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + campoDTOList.size() + " campos por nemónico del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, campoDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    /**
     * Formatea una fecha al español en el formato: "viernes, 30 de mayo del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "fecha no disponible";
        }

        try {
            // Configurar el locale para español
            Locale localeEspanol = new Locale("es", "ES");

            // Crear el formato personalizado
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", localeEspanol);

            return formatoCompleto.format(fecha);
        } catch (Exception e) {
            // En caso de error, devolver un formato simple
            SimpleDateFormat formatoSimple = new SimpleDateFormat("dd/MM/yyyy");
            return formatoSimple.format(fecha);
        }
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
     * Método auxiliar para obtener nombres completos de un UsuarioSistema
     */
    private String obtenerNombreCompletoUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
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