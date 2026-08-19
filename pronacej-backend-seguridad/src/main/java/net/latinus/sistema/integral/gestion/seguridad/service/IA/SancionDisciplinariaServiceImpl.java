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
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.SancionDisciplinariaCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.SancionDisciplinariaDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.SancionDisciplinariaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class SancionDisciplinariaServiceImpl implements SancionDisciplinariaService{

    private final JwtProviderService jwtProviderService;
    private final SancionDisciplinariaRepository sancionRepository;
    private final FichaIdentificacionRepository fichaRepository;
    private final PaginacionService paginacionService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private CatalogoRepository catalogoRepository;
    private JerarquiaRepository jerarquiaRepository;
    private SancionDisciplinariaDocumentoRepository sancionDisciplinariaDocumentoRepository;
    private SancionDisciplinariaCarpetaRepository sancionDisciplinariaCarpetaRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SancionDisciplinariaDTO>> obtenerListadoPorToken(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<SancionDisciplinariaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> jwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!jwt.isExito()) {
                df.setMensaje(jwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar cuerpo
            RespuestaPorDefectoAuditoria<String> desencriptado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!desencriptado.isExito()) {
                df.setMensaje(desencriptado.getMensaje());
                return df;
            }

            String body = desencriptado.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            // Parámetros
            String tokenIdentificador = paginacionRequest.getTokenIdentificador();
            String filtro = paginacionRequest.getFilter();
            int page = paginacionRequest.getPage() != null ? paginacionRequest.getPage() : 0;
            int size = paginacionRequest.getSize() != null ? paginacionRequest.getSize() : 10;

            Pageable pageable = PageRequest.of(page, size, Sort.by("idSancionDisciplinaria").descending());

            Page<SancionDisciplinaria> paginaSanciones;

            if (tokenIdentificador != null && !tokenIdentificador.isEmpty()) {
                if (filtro != null && !filtro.trim().isEmpty()) {
                    paginaSanciones = sancionRepository.buscarPorTokenYFiltro(tokenIdentificador, filtro, pageable);
                } else {
                    paginaSanciones = sancionRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(tokenIdentificador, false, pageable);
                    System.out.println(" Total sanciones encontradas sin filtro: " + paginaSanciones.getTotalElements());
                }
            } else {
                paginaSanciones = Page.empty(); // o lanzar excepción si lo deseas
            }

            List<SancionDisciplinariaDTO> listaDTO = paginaSanciones.getContent().stream()
                    .map(this::entidadADto)
                    .collect(Collectors.toList());

            PaginacionResponse<SancionDisciplinariaDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(listaDTO);
            paginacionResponse.setTotalItems(paginaSanciones.getTotalElements());

            df.llenarRespuestaExitosa("Sanciones encontradas", paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> crearSancion(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            // Verifica token del usuario
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyDecifrado = df22.getData();
            SancionDisciplinariaDTO dto = new Gson().fromJson(bodyDecifrado, SancionDisciplinariaDTO.class);

            // Buscar si ya existe (modo edición)
            SancionDisciplinaria sancionExistente = sancionRepository
                    .findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

            // Validar existencia en edición
            if (dto.getEsEdicion() && sancionExistente == null) {
                df.setMensaje("No existe la sanción disciplinaria solicitada.");
                return df;
            }

            // CREAR o EDITAR
            SancionDisciplinaria sancion;
            if (!dto.getEsEdicion()) {
                sancion = new SancionDisciplinaria();
                sancion.setFechaCreacion(new Date());
                sancion.setTokenIdentificador(UUID.randomUUID().toString());
            } else {
                sancion = sancionExistente;
                sancion.setFechaEdicion(new Date());
            }

            // Validar y asociar ficha
            FichaIdentificacion ficha = fichaRepository
                    .findById(dto.getFichaIdentificacion().getIdFichaIdentificacion()).orElse(null);
            if (ficha == null) {
                df.setMensaje("No se encontró la ficha de identificación.");
                return df;
            }

            // Asignar campos simples
            sancion.setFechaInicio(dto.getFechaInicio());
            sancion.setFechaFin(dto.getFechaFin());
            sancion.setFechaRegistro(dto.getFechaRegistro());
            sancion.setNroResolucion(dto.getNroResolucion());
            sancion.setFalta(dto.getFalta());
            sancion.setSancion(dto.getSancion());
            sancion.setObservacion(dto.getObservacion());
            sancion.setMotivo(dto.getMotivo());

            // Asignar relaciones
            sancion.setFichaIdentificacion(ficha);
            sancion.setCentro(jerarquiaRepository.findByTokenIdentificadorAndRemovido(dto.getCentro().getTokenIdentificador(), false));
            sancion.setPrograma(jerarquiaRepository.findByTokenIdentificadorAndRemovido(dto.getPrograma().getTokenIdentificador(), false));
//            sancion.setAmbiente(jerarquiaRepository.findByTokenIdentificadorAndRemovido(dto.getAmbiente().getTokenIdentificador(), false));
            if (dto.getAmbiente() != null && dto.getAmbiente().getTokenIdentificador() != null) {
                sancion.setAmbiente(jerarquiaRepository.findByTokenIdentificadorAndRemovido(dto.getAmbiente().getTokenIdentificador(), false));
            } else {
                sancion.setAmbiente(null);
            }
//            sancion.setMotivoSancion(catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getMotivoSancion().getTokenIdentificador(), false));
            sancion.setTipificacionFalta(catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTipificacionFalta().getTokenIdentificador(), false));

            // Guardar entidad
            sancionRepository.save(sancion);

            // Armar respuesta
            String nombreCompleto = String.join(" ",
                    Optional.ofNullable(ficha.getNombres()).orElse(""),
                    Optional.ofNullable(ficha.getApellidoPaterno()).orElse(""),
                    Optional.ofNullable(ficha.getApellidoMaterno()).orElse("")).trim();

            dto.setNombreAdolescente(nombreCompleto);
            dto.setTokenIdentificador(sancion.getTokenIdentificador());

            if (!dto.getEsEdicion()) {
                df.llenarRespuestaExitosa(
                        "Se creó con éxito la sanción disciplinaria de: " + nombreCompleto,
                        dto,
                        "Sanción registrada correctamente para: " + nombreCompleto);
            } else {
                df.llenarRespuestaExitosa(
                        "Se editó con éxito la sanción disciplinaria de: " + nombreCompleto,
                        dto,
                        "Sanción editada correctamente para: " + nombreCompleto);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSancion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            // Desencriptar body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();
            SancionDisciplinariaDTO sancionDTO = new Gson().fromJson(bodyString, SancionDisciplinariaDTO.class);

            // Buscar sanción
            SancionDisciplinaria sancion = this.sancionRepository.findByTokenIdentificadorAndRemovido(
                    sancionDTO.getTokenIdentificador(), false
            );

            if (sancion == null) {
                df.setMensaje("La sanción no fue encontrada o ya fue eliminada anteriormente.");
                return df;
            }

            // Eliminar lógicamente
            Date fecha = new Date();
            sancion.setRemovido(true);
            sancion.setIpElimina(ip);
            sancion.setUsuarioSistemaElimina(usuarioSistemaLogin);
            sancion.setFechaEliminacion(fecha);
            this.sancionRepository.save(sancion);

            // Recuperar nombre del adolescente
            String nombreCompleto = "";
            FichaIdentificacion ficha = sancion.getFichaIdentificacion();
            if (ficha != null) {
                nombreCompleto =
                        (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                                (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                                (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "");
            }

            df.llenarRespuestaExitosa(
                    "Se ha eliminado con éxito la sanción disciplinaria del adolescente: " + nombreCompleto.trim(),
                    true
            );
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> obtenerSancionPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<SancionDisciplinariaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            // Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Buscar entidad por token
            SancionDisciplinaria sancion = this.sancionRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if (sancion == null) {
                df.setMensaje("No existe la sanción disciplinaria solicitada.");
                return df;
            }

            // Convertir a DTO
            SancionDisciplinariaDTO dto = entidadADto(sancion);

            // Asignar nombre del adolescente si existe ficha
            if (sancion.getFichaIdentificacion() != null) {
                FichaIdentificacion ficha = sancion.getFichaIdentificacion();
                String nombreCompleto =
                        (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                                (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                                (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "");
                dto.setNombreAdolescente(nombreCompleto.trim());
            }

            df.llenarRespuestaExitosa("Se ha encontrado el registro:", dto);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }



    @Override
    public RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest,
                                                                        BodyEncriptado bodyEncriptado,
                                                                        MultipartFile[] multipartFiles) {
        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación de token
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

            // Desencriptar
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }

            String bodyDesencriptado = df22.getData();
            SancionDisciplinariaDTO dto = new Gson().fromJson(bodyDesencriptado, SancionDisciplinariaDTO.class);
            SancionDisciplinaria sancion = this.sancionRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
            if (sancion == null) {
                respuesta.setMensaje("No existe la sanción disciplinaria solicitada.");
                return respuesta;
            }

            // Buscar carpeta ya creada
            SancionDisciplinariaCarpeta registroCarpeta = this.sancionDisciplinariaCarpetaRepository.findFirstBySancionDisciplinariaTokenIdentificadorAndRemovido(sancion.getTokenIdentificador(), false);

            List<DocumentoDTO> documentoDTOList = dto.getDocumentoDTOList();
            String nombresCompletos = obtenerNombresCompletos(sancion.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(sancion.getFichaIdentificacion());

            if (registroCarpeta == null) {
                FichaIdentificacion ficha = sancion.getFichaIdentificacion();

                FichaIdentificacionCarpeta carpetaPadre = this.fichaIdentificacionCarpetaRepository
                        .findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                                ficha.getTokenIdentificador(), null, false);

                if (carpetaPadre == null) {
                    respuesta.setMensaje("No se encontró la carpeta principal de la ficha.");
                    return respuesta;
                }

                // Buscar o crear carpeta padre de sanciones
                String nemonicoSancion = EtiquetaNemonico.CARPETA_GESTION_ADOLES_SANCION_DISCIPLINARIA;
                FichaIdentificacionCarpeta carpetaSanciones = this.fichaIdentificacionCarpetaRepository
                        .findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                                ficha.getTokenIdentificador(), nemonicoSancion, false);

                Carpeta carpetaPadreSanciones;
                if (carpetaSanciones == null) {

                    CarpetaDTO carpetaDTO = new CarpetaDTO();
                    carpetaDTO.setNombreCliente("Sanciones disciplinarias");
                    carpetaDTO.setDescripcion("Carpeta de sanciones disciplinarias");

                    CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                    carpetaPadreDTO.setTokenIdentificador(carpetaPadre.getCarpeta().getTokenIdentificador());
                    carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                    RespuestaPorDefectoAuditoria<CarpetaDTO> carpetaResp = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);
                    if (!carpetaResp.isExito()) {
                        respuesta.setMensaje("Error al crear carpeta padre de sanciones.");
                        return respuesta;
                    }

                    Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                            carpetaResp.getData().getTokenIdentificador(), false);

                    carpetaSanciones = new FichaIdentificacionCarpeta();
                    carpetaSanciones.setCarpeta(carpetaGuardada);
                    carpetaSanciones.setFichaIdentificacion(ficha);
                    carpetaSanciones.setTipoDeGestionDeAdolescente(this.catalogoRepository.findByNemonicoAndRemovido(nemonicoSancion, false));
                    carpetaSanciones.setFechaCreacion(new Date());
                    carpetaSanciones.setIpCrea(httpServletRequest.getRemoteAddr());
                    carpetaSanciones.setUsuarioSistemaCrea(usuarioSistema);
                    this.fichaIdentificacionCarpetaRepository.save(carpetaSanciones);

                    carpetaPadreSanciones = carpetaGuardada;
                } else {
                    carpetaPadreSanciones = carpetaSanciones.getCarpeta();
                }

                // Crear carpeta individual de sanción
                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente("sancion_" + sancion.getTokenIdentificador());
                carpetaDTO.setDescripcion("Documentos de la sanción disciplinaria");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadreSanciones.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);
                if (!respuestaCarpeta.isExito()) {
                    respuesta.setMensaje("Error al crear carpeta de sanción.");
                    return respuesta;
                }

                Carpeta carpetaFinal = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                        respuestaCarpeta.getData().getTokenIdentificador(), false);

                registroCarpeta = new SancionDisciplinariaCarpeta();
                registroCarpeta.setCarpeta(carpetaFinal);
                registroCarpeta.setSancionDisciplinaria(sancion); // puedes cambiar el nombre si es necesario
                registroCarpeta.setUsuarioSistemaCrea(usuarioSistema);
                registroCarpeta.setFechaCreacion(new Date());
                registroCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
                this.sancionDisciplinariaCarpetaRepository.save(registroCarpeta);
            }

            Carpeta carpeta = registroCarpeta.getCarpeta();
            String idNodo = carpeta.getIdentificadorAlfresco();

//            for (int i = 0; i < multipartFiles.length; i++) {
//                MultipartFile file = multipartFiles[i];
//                DocumentoDTO docDTO = documentoDTOList.get(i);
//
//                var respuestaDoc = this.documentoService.subirDocumentoAlfresco(httpServletRequest, idNodo, file, docDTO);
//                if (!respuestaDoc.isExito()) {
//                    respuesta.setMensaje("Error al subir documento: " + respuestaDoc.getMensaje());
//                    return respuesta;
//                }
//
//                Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
//                        respuestaDoc.getData().getTokenIdentificador(), false);
//
//                SancionDisciplinariaDocumento relacion = new SancionDisciplinariaDocumento();
//                relacion.setCarpeta(carpeta);
//                relacion.setDocumento(documento);
//                relacion.setSancionDisciplinaria(sancion); // puedes cambiar el nombre si usas otro campo
//                relacion.setUsuarioSistemaCrea(usuarioSistema);
//                relacion.setIpCrea(httpServletRequest.getRemoteAddr());
//                this.sancionDisciplinariaDocumentoRepository.save(relacion);
//            }
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

                    SancionDisciplinariaDocumento sancionDisciplinariaDocumento = new SancionDisciplinariaDocumento();
                    sancionDisciplinariaDocumento.setDocumento(documento);
                    sancionDisciplinariaDocumento.setSancionDisciplinaria(sancion);
                    sancionDisciplinariaDocumento.setCarpeta(carpeta);
                    sancionDisciplinariaDocumento.setUsuarioSistemaCrea(usuarioSistema);
                    sancionDisciplinariaDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                    this.sancionDisciplinariaDocumentoRepository.save(sancionDisciplinariaDocumento);
                }
            }

            respuesta.llenarRespuestaExitosa("Documentos subidos correctamente a la sanción disciplinaria.", true);
        } catch (Exception e) {
            respuesta.llenarConDatosDeException(e);
        }

        return respuesta;
    }


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            var df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            var df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            PaginacionRequest paginacionRequest = new Gson().fromJson(df22.getData(), PaginacionRequest.class);
            Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

            Page<SancionDisciplinariaDocumento> page = this.sancionDisciplinariaDocumentoRepository
                    .findBySancionDisciplinariaTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false, pageable);

            List<DocumentoDTO> lista = new ArrayList<>();
            for (SancionDisciplinariaDocumento doc : page.getContent()) {
                Documento documento = doc.getDocumento();
                DocumentoDTO dto = new DocumentoDTO();

                dto.setTokenIdentificador(documento.getTokenIdentificador());
                dto.setNombre(documento.getNombreReal());
                dto.setDescripcion(documento.getDescripcion());
                dto.setFechaCreacion(documento.getFechaCreacion());
                dto.setMimeType(documento.getMimeType());
                dto.setTamanioBytes(documento.getTamanioByteDocumento());
                dto.setTipoDocumentoSistema(documento.getTipoDeDocumentoSistema().convertirADTO());
                dto.setTipoDeDocumentoSistemaOtro(documento.getTipoDeDocumentoSistemaOtro());
                lista.add(dto);
            }

            PaginacionResponse<DocumentoDTO> resp = new PaginacionResponse<>();
            resp.setData(lista);
            resp.setTotalItems(page.getTotalElements());

            df.llenarRespuestaExitosa("Documentos encontrados", resp, "Total documentos: " + page.getTotalElements());
        } catch (Exception e) {
            df.llenarConDatosDeException(e);
        }

        return df;
    }

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


    private SancionDisciplinariaDTO entidadADto(SancionDisciplinaria sancion) {
        SancionDisciplinariaDTO dto = new SancionDisciplinariaDTO();

        dto.setIdSancionDisciplinaria(sancion.getIdSancionDisciplinaria());
        dto.setFechaInicio(sancion.getFechaInicio());
        dto.setFechaFin(sancion.getFechaFin());
        dto.setFechaRegistro(sancion.getFechaRegistro());
        dto.setNroResolucion(sancion.getNroResolucion());
        dto.setFalta(sancion.getFalta());
        dto.setSancion(sancion.getSancion());
        dto.setObservacion(sancion.getObservacion());
        dto.setEsEdicion(true); // o según tu lógica
        dto.setTokenIdentificador(sancion.getTokenIdentificador());
        dto.setMotivo(sancion.getMotivo());

        // Mapea los objetos anidados correctamente
//        if (sancion.getMotivoSancion() != null) {
//            dto.setMotivoSancion(entidadADtoCatalogo(sancion.getMotivoSancion()));
//            dto.setNombreMotivo(sancion.getMotivoSancion().getNombre());
//        }

        if (sancion.getTipificacionFalta() != null) {
            dto.setTipificacionFalta(entidadADtoCatalogo(sancion.getTipificacionFalta()));
            dto.setNombreTipificacion(sancion.getTipificacionFalta().getNombre());
        }

        if (sancion.getFichaIdentificacion() != null) {
            FichaIdentificacion ficha = sancion.getFichaIdentificacion();
            dto.setFichaIdentificacion(new SancionDisciplinariaDTO().getFichaIdentificacion()); // Ajusta con DTO real
            dto.setNombreAdolescente((ficha.getNombres() + " " + ficha.getApellidoPaterno() + " " + ficha.getApellidoMaterno()).trim());
        }

        if (sancion.getPrograma() != null) {
            dto.setPrograma(entidadADtoJerarquia(sancion.getPrograma()));
        }

        if (sancion.getAmbiente() != null) {
            dto.setAmbiente(entidadADtoJerarquia(sancion.getAmbiente()));
        }

        if (sancion.getCentro() != null) {
            dto.setCentro(entidadADtoJerarquia(sancion.getCentro()));
        }
        if (sancion.getFichaIdentificacion() != null) {
            FichaIdentificacion ficha = sancion.getFichaIdentificacion();

            FichaIdentificacionDTO fichaDTO = new FichaIdentificacionDTO();
            fichaDTO.setIdFichaIdentificacion(ficha.getIdFichaIdentificacion());
            fichaDTO.setTokenIdentificador(ficha.getTokenIdentificador());
            fichaDTO.setNombres(ficha.getNombres());
            fichaDTO.setApellidoPaterno(ficha.getApellidoPaterno());
            fichaDTO.setApellidoMaterno(ficha.getApellidoMaterno());
            fichaDTO.setNumeroIdentificacion(ficha.getNumeroIdentificacion());

            dto.setFichaIdentificacion(fichaDTO);
        }

        return dto;
    }


    private SancionDisciplinaria dtoAEntidad(SancionDisciplinariaDTO dto) {
        SancionDisciplinaria entidad = new SancionDisciplinaria();
        entidad.setIdSancionDisciplinaria(dto.getIdSancionDisciplinaria());
        entidad.setFechaInicio(dto.getFechaInicio());
        entidad.setFechaFin(dto.getFechaFin());
        entidad.setFechaRegistro(dto.getFechaRegistro());
        entidad.setNroResolucion(dto.getNroResolucion());
        entidad.setFalta(dto.getFalta());
        entidad.setSancion(dto.getSancion());
        entidad.setObservacion(dto.getObservacion());
        entidad.setMotivo(dto.getMotivo());

        if (dto.getFichaIdentificacion() != null) {
            FichaIdentificacion ficha = fichaRepository.findByIdFichaIdentificacion(dto.getFichaIdentificacion().getIdFichaIdentificacion());
            entidad.setFichaIdentificacion(ficha);
        }
        return entidad;
    }


    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }

    private static CatalogoDTO entidadADtoCatalogo(Catalogo entidad) {
        if (entidad == null) return null;

        CatalogoDTO dto = new CatalogoDTO();
        dto.setIdCatalogo(entidad.getIdCatalogo());
        dto.setNombre(entidad.getNombre());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setNemonico(entidad.getNemonico());
        dto.setCodigoExterno(entidad.getCodigoExterno());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }


    private Jerarquia dtoAEntidadJerarquia(JerarquiaDTO dto) {
        if (dto == null) return null;
        return this.jerarquiaRepository.findJerarquiaByTokenIdentificador(dto.getTokenIdentificador());
    }

    private static  JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
        if (entidad == null) return null;
        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setId(entidad.getIdJerarquia());
        dto.setNombre(entidad.getNombre());
        dto.setNemonico(entidad.getNemonico());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }

}
