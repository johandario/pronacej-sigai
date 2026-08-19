package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionTipoDeDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.FichaIdentificacionTipoDeDocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.TipoDeArchivoSeccionFichaPrincipal;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionTipoDeDocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class FichaIdentificacionTipoDeDocumentoServiceImpl implements FichaIdentificacionTipoDeDocumentoService {

    private JwtProviderService jwtProviderService;
    private FichaIdentificacionTipoDeDocumentoRepository fichaIdentificacionTipoDeDocumentoRepository;
    private CatalogoRepository catalogoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>>
    obtenerTiposDeDocumentosDeUnaSeccionDeLaFichaPrincipal(HttpServletRequest httpServletRequest, String nemonicoSeccion) {

        RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            // Obtener el catálogo de la sección para obtener su nombre
            Catalogo seccionCatalogo = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoSeccion, false);
            String nombreSeccion = seccionCatalogo != null ? seccionCatalogo.getNombre() : nemonicoSeccion;

            List<FichaIdentificacionTipoDeDocumento> fichaIdentificacionTipoDeDocumentoList =
                    this.fichaIdentificacionTipoDeDocumentoRepository.findBySeccionFichaDeIdentificacionNemonicoAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
                            nemonicoSeccion, false,
                            false,
                            false
                    );
            List<FichaIdentificacionTipoDeDocumentoDTO> fichaIdentificacionTipoDeDocumentoDTOList = new ArrayList<>();

            for (FichaIdentificacionTipoDeDocumento fichaIdentificacionTipoDeDocumento : fichaIdentificacionTipoDeDocumentoList) {
                fichaIdentificacionTipoDeDocumentoDTOList.add(fichaIdentificacionTipoDeDocumento.convertirADTO());
            }

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se obtuvieron con éxito " + fichaIdentificacionTipoDeDocumentoDTOList.size() + " tipos de documentos asociados a la sección: " + nombreSeccion + ". Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + fichaIdentificacionTipoDeDocumentoDTOList.size() + " tipos de documentos de ficha de identificación";

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIdentificacionTipoDeDocumentoDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<TipoDeArchivoSeccionFichaPrincipal>>
    obtenerSeccionDefichaPrincipalConTotalDeTipoDeDocumentos(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<TipoDeArchivoSeccionFichaPrincipal>>
                df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            List<Catalogo> seccionesFichaPrincipal = this.catalogoRepository.
                    findByCatalogoPadreNemonicoAndEmpresaAndRemovidoOrderByNombre(
                            EtiquetaNemonico.SECCIONES_FICHA_DE_IDENTIFICACION,
                            empresa,
                            false
                    );

            if (seccionesFichaPrincipal.isEmpty()) {
                df.setMensaje("No se han configurado las secciones de la ficha de identificación: " +
                        EtiquetaNemonico.SECCIONES_FICHA_DE_IDENTIFICACION);
                return df;
            }

            List<TipoDeArchivoSeccionFichaPrincipal> tipoDeArchivoSeccionFichaPrincipals
                    = new ArrayList<>();

            for (Catalogo seccionFichaPrincipal : seccionesFichaPrincipal) {
                TipoDeArchivoSeccionFichaPrincipal tipoDeArchivoSeccionFichaPrincipal = new TipoDeArchivoSeccionFichaPrincipal();
                tipoDeArchivoSeccionFichaPrincipal.setSeccionFichaPPL(seccionFichaPrincipal.convertirADTO());
                tipoDeArchivoSeccionFichaPrincipal.setCantidadDeTipoDedocumentos(
                        this.fichaIdentificacionTipoDeDocumentoRepository.
                                countBySeccionFichaDeIdentificacionAndEmpresaAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
                                        seccionFichaPrincipal, empresa, false,
                                        false, false
                                )
                );
                FichaIdentificacionTipoDeDocumento fichaIdentificacionTipoDeDocumento =
                        this.fichaIdentificacionTipoDeDocumentoRepository.
                                findFirstBySeccionFichaDeIdentificacionAndEmpresaAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
                                        seccionFichaPrincipal, empresa, false,
                                        false, false
                                );
                if (fichaIdentificacionTipoDeDocumento != null) {
                    tipoDeArchivoSeccionFichaPrincipal.setFechaDeCreacion(fichaIdentificacionTipoDeDocumento.getFechaCreacion());
                }

                tipoDeArchivoSeccionFichaPrincipals.add(tipoDeArchivoSeccionFichaPrincipal);
            }

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se obtuvieron con éxito " + tipoDeArchivoSeccionFichaPrincipals.size() + " secciones de ficha principal con sus respectivos documentos. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + tipoDeArchivoSeccionFichaPrincipals.size() + " secciones de ficha principal del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, tipoDeArchivoSeccionFichaPrincipals, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>> obtenerPorSeccionFichaPrincipal(
            HttpServletRequest httpServletRequest, String tokenSeccionFichaPrinicipal) {

        RespuestaPorDefectoAuditoria<List<FichaIdentificacionTipoDeDocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            List<FichaIdentificacionTipoDeDocumento> fichaIdentificacionTipoDeDocumentoList =
                    this.fichaIdentificacionTipoDeDocumentoRepository.
                            findBySeccionFichaDeIdentificacionTokenIdentificadorAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
                                    tokenSeccionFichaPrinicipal,
                                    false, false, false
                            );

            List<FichaIdentificacionTipoDeDocumentoDTO> fichaIdentificacionTipoDeDocumentoDTOList =
                    new ArrayList<>();

            for (FichaIdentificacionTipoDeDocumento fichaIdentificacionTipoDeDocumento : fichaIdentificacionTipoDeDocumentoList) {
                fichaIdentificacionTipoDeDocumentoDTOList.add(fichaIdentificacionTipoDeDocumento.convertirADTO());
            }

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se obtuvieron con éxito " + fichaIdentificacionTipoDeDocumentoDTOList.size() + " relaciones entre sección y tipo de documento. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + fichaIdentificacionTipoDeDocumentoDTOList.size() + " relaciones sección-documento del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIdentificacionTipoDeDocumentoDTOList, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> crear(
            HttpServletRequest httpServletRequest,
            FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO) {

        RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            System.out.println(fichaIdentificacionTipoDeDocumentoDTO.toString());

            CatalogoDTO tipoDeDocumentoDTO = fichaIdentificacionTipoDeDocumentoDTO.getTipoArchivoSistemaDTO();

            if (tipoDeDocumentoDTO == null) {
                df.setMensaje("No se recibio un tipo de documento");
                return df;
            }

            Catalogo tipoDeDocumentoSistema = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    tipoDeDocumentoDTO.getTokenIdentificador(),
                    false
            );

            if (tipoDeDocumentoSistema == null) {
                df.setMensaje("El tipo de documento enviado es inválido");
                return df;
            }

            CatalogoDTO seccionFichaDTO = fichaIdentificacionTipoDeDocumentoDTO.getSeccionFichaDeIdentificacionDTO();

            if (seccionFichaDTO == null) {
                df.setMensaje("No se recibio una seccion de ficha principal");
                return df;
            }

            Catalogo seccionFichaPrincipal = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    seccionFichaDTO.getTokenIdentificador(),
                    false
            );

            if (seccionFichaPrincipal == null) {
                df.setMensaje("La sección de ficha principal es inválida");
                return df;
            }

            FichaIdentificacionTipoDeDocumento fichaIdentificacionTipoDeDocumento =
                    this.fichaIdentificacionTipoDeDocumentoRepository.
                            findFirstBySeccionFichaDeIdentificacionAndTipoArchivoSistemaAndEmpresaAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
                                    seccionFichaPrincipal,
                                    tipoDeDocumentoSistema,
                                    empresa,
                                    false,
                                    false,
                                    false
                            );

            if (fichaIdentificacionTipoDeDocumento != null) {
                df.setMensaje("Ya existe una relación con la sección: " +
                        seccionFichaPrincipal.getNombre() + " y el tipo de documento: " +
                        tipoDeDocumentoSistema.getNombre());
                return df;
            }

            FichaIdentificacionTipoDeDocumento fichaIdentificacionTipoDeDocumento1 = new FichaIdentificacionTipoDeDocumento();
            fichaIdentificacionTipoDeDocumento1.setSeccionFichaDeIdentificacion(seccionFichaPrincipal);
            fichaIdentificacionTipoDeDocumento1.setTipoArchivoSistema(tipoDeDocumentoSistema);
            fichaIdentificacionTipoDeDocumento1.setEmpresa(empresa);
            fichaIdentificacionTipoDeDocumento1.setRequerido(
                    fichaIdentificacionTipoDeDocumentoDTO.getRequerido()
            );

            fichaIdentificacionTipoDeDocumento1.setUsuarioSistemaCrea(bodyJwtValido.getUsuarioSistema());
            fichaIdentificacionTipoDeDocumento1.setIpCrea(httpServletRequest.getRemoteAddr());
            fichaIdentificacionTipoDeDocumento1 = this.fichaIdentificacionTipoDeDocumentoRepository.save(
                    fichaIdentificacionTipoDeDocumento1
            );

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se creó con éxito el tipo de documento: " +
                    tipoDeDocumentoSistema.getNombre() + " para la sección: " +
                    seccionFichaPrincipal.getNombre();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se creó con éxito la relación tipo de documento " + tipoDeDocumentoSistema.getNombre() + " - sección " + seccionFichaPrincipal.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIdentificacionTipoDeDocumento1.convertirADTO(), mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> editar(
            HttpServletRequest httpServletRequest,
            FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO) {

        RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            CatalogoDTO tipoDeDocumentoDTO = fichaIdentificacionTipoDeDocumentoDTO.getTipoArchivoSistemaDTO();

            if (tipoDeDocumentoDTO == null) {
                df.setMensaje("No se recibio un tipo de documento");
                return df;
            }

            Catalogo tipoDeDocumentoSistema = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    tipoDeDocumentoDTO.getTokenIdentificador(),
                    false
            );

            if (tipoDeDocumentoSistema == null) {
                df.setMensaje("El tipo de documento enviado es inválido");
                return df;
            }

            CatalogoDTO seccionFichaDTO = fichaIdentificacionTipoDeDocumentoDTO.getSeccionFichaDeIdentificacionDTO();

            if (seccionFichaDTO == null) {
                df.setMensaje("No se recibio una seccion de ficha principal");
                return df;
            }

            Catalogo seccionFichaPrincipal = this.catalogoRepository.findByTokenIdentificadorAndRemovido(
                    seccionFichaDTO.getTokenIdentificador(),
                    false
            );

            if (seccionFichaPrincipal == null) {
                df.setMensaje("La sección de ficha principal es inválida");
                return df;
            }

            FichaIdentificacionTipoDeDocumento fichaIdentificacionTipoDeDocumento =
                    this.fichaIdentificacionTipoDeDocumentoRepository.
                            findByTokenIdentificadorAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
                                    fichaIdentificacionTipoDeDocumentoDTO.getTokenIdentificador(),
                                    false,
                                    false,
                                    false
                            );

            if (fichaIdentificacionTipoDeDocumento == null) {
                df.setMensaje("El objeto a editar no existe o fue eliminado anteriormente");
                return df;
            }

            fichaIdentificacionTipoDeDocumento.setSeccionFichaDeIdentificacion(seccionFichaPrincipal);
            fichaIdentificacionTipoDeDocumento.setTipoArchivoSistema(tipoDeDocumentoSistema);

            fichaIdentificacionTipoDeDocumento.setRequerido(
                    fichaIdentificacionTipoDeDocumentoDTO.getRequerido()
            );

            fichaIdentificacionTipoDeDocumento.setUsuarioSistemaEdita(bodyJwtValido.getUsuarioSistema());
            fichaIdentificacionTipoDeDocumento.setIpEdita(httpServletRequest.getRemoteAddr());
            fichaIdentificacionTipoDeDocumento.setFechaEdicion(new Date());

            fichaIdentificacionTipoDeDocumento = this.fichaIdentificacionTipoDeDocumentoRepository.save(
                    fichaIdentificacionTipoDeDocumento
            );

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se actualizó con éxito el tipo de documento: " +
                    tipoDeDocumentoSistema.getNombre() + " para la sección: " +
                    seccionFichaPrincipal.getNombre();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se editó con éxito la relación tipo de documento " + tipoDeDocumentoSistema.getNombre() + " - sección " + seccionFichaPrincipal.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIdentificacionTipoDeDocumento.convertirADTO(), mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> eliminar(
            HttpServletRequest httpServletRequest,
            FichaIdentificacionTipoDeDocumentoDTO fichaIdentificacionTipoDeDocumentoDTO) {

        RespuestaPorDefectoAuditoria<FichaIdentificacionTipoDeDocumentoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            FichaIdentificacionTipoDeDocumento fichaIdentificacionTipoDeDocumento =
                    this.fichaIdentificacionTipoDeDocumentoRepository.
                            findByTokenIdentificadorAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
                                    fichaIdentificacionTipoDeDocumentoDTO.getTokenIdentificador(),
                                    false,
                                    false,
                                    false
                            );

            if (fichaIdentificacionTipoDeDocumento == null) {
                df.setMensaje("El objeto a eliminar no existe o fue eliminado anteriormente");
                return df;
            }

            Catalogo tipoDeDocumento = fichaIdentificacionTipoDeDocumento.getTipoArchivoSistema();
            Catalogo seccion = fichaIdentificacionTipoDeDocumento.getSeccionFichaDeIdentificacion();
            
            fichaIdentificacionTipoDeDocumento.setRemovido(true);
            Date fechaEliminacion = new Date();
            fichaIdentificacionTipoDeDocumento.setFechaEliminacion(fechaEliminacion);
            fichaIdentificacionTipoDeDocumento.setUsuarioSistemaElimina(bodyJwtValido.getUsuarioSistema());

            fichaIdentificacionTipoDeDocumento = this.fichaIdentificacionTipoDeDocumentoRepository.save(
                    fichaIdentificacionTipoDeDocumento
            );

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            String fechaFormateada = formatearFechaEspanol(fechaEliminacion);

            String nombreTipoDocumento = tipoDeDocumento != null ? tipoDeDocumento.getNombre() : "N/A";
            String nombreSeccion = seccion != null ? seccion.getNombre() : "N/A";

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se eliminó con éxito el tipo de documento: " + nombreTipoDocumento + " de la sección: " + nombreSeccion;

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se eliminó con éxito la relación tipo de documento " + nombreTipoDocumento + " - sección " + nombreSeccion + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, fichaIdentificacionTipoDeDocumento.convertirADTO(), mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
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
}