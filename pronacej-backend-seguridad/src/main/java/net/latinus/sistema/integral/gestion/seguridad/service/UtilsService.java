package net.latinus.sistema.integral.gestion.seguridad.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlantillaFormulario;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlantillaVariable;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.informe.Informe;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.CampoInformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.informe.InformeDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.GeneracionPdfRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.informe.InformeRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.PlantillaFormularioRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.PlantillaVariableRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.general.InformeService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UtilsService {

    private JwtProviderService jwtProviderService;
    private InformeService informeService;
    private final Aes aes = new Aes();
    ;
    private RSA rsa;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private PlantillaFormularioRepository plantillaFormularioRepository;
    private PlantillaVariableRepository plantillaVariableRepository;
    private InformeRepository informeRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private CatalogoRepository catalogoRepository;
    private CarpetaRepository carpetaRepository;
    private CarpetaService carpetaService;

    private final LogService logService = new LogService(this.getClass());


    /**
     * Obten Response
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param body               String datos que se van a encriptar.
     * @return RespuestaPorDefectoAuditoria<BodyEncriptado>
     */
    public RespuestaPorDefectoAuditoria<BodyEncriptado> crearBodyEncriptado(HttpServletRequest httpServletRequest, String body,
                                                                            String nemonicoClaveRSAPublica) {
        RespuestaPorDefectoAuditoria<BodyEncriptado> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            String claveAesDinamica = new Date().getTime() + "_pruebas";

            BodyEncriptado bodyEncriptado = new BodyEncriptado();

            String bodyStringEncriptadoAes = aes.encrypt(claveAesDinamica, body);
            bodyEncriptado.setBody(bodyStringEncriptadoAes);

            Page<ParametroDelSistema> parametroDelSistemaPage = this.parametroDelSistemaRepository.findByNemonicoAndRemovidoAndEmpresaIdEmpresa(
                    nemonicoClaveRSAPublica, false, null, PageRequest.of(0, 3)
            );

            if (parametroDelSistemaPage.isEmpty()) {
                df.setMensaje("No existe la clave para encriptar");
                return df;
            }

            ParametroDelSistema parametroDelSistema = parametroDelSistemaPage.toList().get(0);

            //RespuestaPorDefectoAuditoria<String> respRsaEncriptacion = rsa.encriptarPorEmpresa(claveAesDinamica, this.idEmpresa);
            String llaveEncriptada = rsa.Encrypt(claveAesDinamica, parametroDelSistema.getValor());

            bodyEncriptado.setLlave(llaveEncriptada);
            bodyEncriptado.setLogOut(false);

            df.llenarRespuestaExitosa("Se encripto con éxito", bodyEncriptado);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    /**
     * Obten un string desencriptado
     *
     * @param httpServletRequest      HttpServletRequest datos del request.
     * @param bodyEncriptado          BodyEncriptado datos encriptados.
     * @param nemonicoClaveRSaPrivada String nemonico de la clave privada RSA.
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> desecriptarBodyEncriptado(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado,
                                                                          String nemonicoClaveRSaPrivada) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Page<ParametroDelSistema> parametroDelSistemaPage = this.parametroDelSistemaRepository.findByNemonicoAndRemovidoAndEmpresaIdEmpresa(
                    nemonicoClaveRSaPrivada, false, null, PageRequest.of(0, 3)
            );

            if (parametroDelSistemaPage.isEmpty()) {
                df.setMensaje("No existe la clave para desencriptar");
                return df;
            }

            ParametroDelSistema parametroDelSistema = parametroDelSistemaPage.toList().get(0);

            String claveAes = this.rsa.Decrypt(bodyEncriptado.getLlave(), parametroDelSistema.getValor());
            String body = this.aes.decrypt(claveAes, bodyEncriptado.getBody());

            df.llenarRespuestaExitosa("Se desencripto con éxito", body);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    /**
     * Encripta un texto directamente con AES
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param textoClaro         String texto a encriptar.
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> encriptarConAes(HttpServletRequest httpServletRequest,
                                                                String textoClaro) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null, false
            );

            if (parametroDelSistema == null) {
                df.setMensaje("No existe la clave para desencriptar");
                return df;
            }

            String claveAes = parametroDelSistema.getValor();

            String body = this.aes.encrypt(claveAes, textoClaro);

            df.llenarRespuestaExitosa("Se encripto con éxito", body);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Encripta un texto directamente con AES
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param textoEncriptado    String texto encriptado.
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> desencriptarConAes(HttpServletRequest httpServletRequest,
                                                                   String textoEncriptado) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null, false
            );

            if (parametroDelSistema == null) {
                df.setMensaje("No existe la clave para desencriptar");
                return df;
            }

            String claveAes = parametroDelSistema.getValor();

            String body = this.aes.decrypt(claveAes, textoEncriptado);

            df.llenarRespuestaExitosa("Se desencripto con éxito", body);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Remueve todos los caracteres especiales de una cadena de texto
     *
     * @param texto string caracteres especiales.
     * @return String
     */
    public String quitarCaracteresEspeciales(String texto) {
        return texto.replaceAll("[^a-zA-Z0-9\\s+]", "");
    }

    /**
     * Genera el PDF de acuerdo a la plantilla
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @param bodyEncriptado     Objeto encriptado.
     * @return RespuestaPorDefectoAuditoria<byte [ ]>
     */
    public RespuestaPorDefectoAuditoria<byte[]> generarPdfFormulario(HttpServletRequest httpServletRequest,
                                                                     BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<byte[]> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            GeneracionPdfRequest request = new Gson().fromJson(bodyDesencriptado, GeneracionPdfRequest.class);

            PlantillaFormulario plantillaFormulario = this.plantillaFormularioRepository.findByNemonicoAndRemovido(/*"PLANTILLA_" + */request.getNemonico(), false);

            if (plantillaFormulario == null) {
                df.setMensaje("No se ha encontrado la plantilla: PLANTILLA_" + request.getNemonico());
                return df;
            }

            String htmlPlantilla = plantillaFormulario.getFormularioString();
            List<PlantillaVariable> variables = plantillaVariableRepository.findByPlantillaFormularioIdPlantillaFormularioAndRemovido(plantillaFormulario.getIdPlantillaFormulario(), false);

            htmlPlantilla = procesarSeccionesOpcionales(htmlPlantilla, request.getVariables(), variables);

            for (PlantillaVariable var : variables) {
                String clave = var.getClave();
                String valor = request.getVariables().get(clave);

                if (clave.contains("TABLA")) {
                    String tablaHtml = generarTablaHtml(valor);
                    //htmlPlantilla = htmlPlantilla.replaceAll(Pattern.quote(clave), tablaHtml);
                    htmlPlantilla = htmlPlantilla.replaceAll(Pattern.quote(clave), Matcher.quoteReplacement(tablaHtml));
                } else if (clave.equals("[INFORME]")) {
                    String informeHtml = generarInformeHtml(httpServletRequest, valor);
                    //htmlPlantilla = htmlPlantilla.replaceAll(Pattern.quote(clave), informeHtml);
                    htmlPlantilla = htmlPlantilla.replaceAll(Pattern.quote(clave), Matcher.quoteReplacement(informeHtml));
                } else {
                    //htmlPlantilla = htmlPlantilla.replaceAll(Pattern.quote(clave), valor != null ? escapeHtml(valor) : "");
                    htmlPlantilla = htmlPlantilla.replaceAll(
                            Pattern.quote(clave),
                            Matcher.quoteReplacement(valor != null ? escapeHtml(valor) : "")
                    );
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlPlantilla);
            renderer.layout();
            renderer.createPDF(out);
            out.close();
            renderer.finishPDF();

            df.llenarRespuestaExitosa("Exitoso", out.toByteArray());

            //System.out.println("Archivo generado (HTML -> PDF): " + out);

            return df;

        } catch (
                Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /*private String generarTablaHtml(String tablaJson) {

        Map<String, Object> tabla = new Gson().fromJson(tablaJson, new TypeToken<Map<String, Object>>() {
        }.getType());

        List<String> encabezados = (List<String>) tabla.get("encabezados");
        List<Map<String, Object>> filas = (List<Map<String, Object>>) tabla.get("filas");

        if (encabezados == null || encabezados.isEmpty() || filas == null || filas.isEmpty()) {
            return "<p>No hay datos disponibles</p>";
        }

        // Construir el HTML
        StringBuilder tablaHtml = new StringBuilder();
        tablaHtml.append("<table border='1' style='border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; font-size: 14px;'>");

// Encabezados
        tablaHtml.append("<thead>")
                .append("<tr style='background-color: #f2f2f2; border-bottom: 2px solid #ddd;'>");
        for (String header : encabezados) {
            tablaHtml.append("<th style='padding: 12px; text-align: left; color: #333; font-weight: bold;'>")
                    .append(header)
                    .append("</th>");
        }
        tablaHtml.append("</tr>")
                .append("</thead>");

// Filas
        tablaHtml.append("<tbody>");
        boolean isOddRow = true; // Para alternar colores de fila
        for (Map<String, Object> row : filas) {
            String backgroundColor = isOddRow ? "#ffffff" : "#f9f9f9";
            tablaHtml.append("<tr style='background-color: ").append(backgroundColor).append("; border-bottom: 1px solid #ddd;'>");
            for (String header : encabezados) {
                tablaHtml.append("<td style='padding: 12px; color: #555;'>")
                        .append(row.getOrDefault(header, ""))
                        .append("</td>");
            }
            tablaHtml.append("</tr>");
            isOddRow = !isOddRow; // Alternar color
        }
        tablaHtml.append("</tbody>");

        tablaHtml.append("</table>");
        return tablaHtml.toString();
    }*/

    private String generarTablaHtml(String tablaJson) {
        Map<String, Object> tabla = new Gson().fromJson(tablaJson, new TypeToken<Map<String, Object>>() {
        }.getType());

        List<String> encabezados = (List<String>) tabla.get("encabezados");
        List<Map<String, Object>> filas = (List<Map<String, Object>>) tabla.get("filas");

        if (encabezados == null || encabezados.isEmpty() || filas == null || filas.isEmpty()) {
            return "<p>No hay datos disponibles</p>";
        }

        // Construir el HTML
        StringBuilder tablaHtml = new StringBuilder();
        tablaHtml.append("<table border='1' style='border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; font-size: 12px;'>");

        // Encabezados
        tablaHtml.append("<thead><tr style='background-color: #f2f2f2; border-bottom: 2px solid #ddd;'>");
        for (String header : encabezados) {
            tablaHtml.append("<th style='padding: 12px; text-align: left; color: #333; font-weight: bold;'>")
                    .append(header)
                    .append("</th>");
        }
        tablaHtml.append("</tr></thead>");

        // Filas
        tablaHtml.append("<tbody>");
        boolean isOddRow = true; // Alternar colores de fila
        for (Map<String, Object> row : filas) {
            String backgroundColor = isOddRow ? "#ffffff" : "#f9f9f9";
            tablaHtml.append("<tr style='background-color: ").append(backgroundColor).append("; border-bottom: 1px solid #ddd;'>");

            // Obtener valores en el orden en que aparecen (independiente de los nombres de los encabezados)
            List<Object> valores = new ArrayList<>(row.values());

            // Iterar hasta el número de columnas esperadas
            for (int i = 0; i < encabezados.size(); i++) {
                String valor = (i < valores.size()) ? String.valueOf(valores.get(i)) : ""; // Evita IndexOutOfBounds
                tablaHtml.append("<td style='padding: 12px; color: #555;'>")
                        .append(escapeHtml(valor))
                        .append("</td>");
            }
            tablaHtml.append("</tr>");
            isOddRow = !isOddRow; // Alternar color
        }
        tablaHtml.append("</tbody>");
        tablaHtml.append("</table>");

        return tablaHtml.toString();
    }


    private String generarInformeHtml(HttpServletRequest httpServletRequest, String idInforme) {

        Informe informe = informeRepository.findByIdInformeAndRemovido(Long.parseLong(idInforme), false);

        InformeDTO informeDTO = new InformeDTO();
        informeDTO.setIdInforme(informe.getIdInforme());
        informeDTO.setIdPlantillaInforme(informe.getPlantillaInforme().getIdPlantillaInforme());

        var campos = informeService.obtenerCamposPorIdInforme(httpServletRequest, informeDTO).getData();

        if (campos == null) {
            return "<p>No se encontró el informe</p>";
        }

        StringBuilder informeHtml = new StringBuilder();

        for (CampoInformeDTO campo : campos) {
            informeHtml.append("<div class=\"dynamic-item\">")
                    .append("<div> <strong>").append(campo.getEtiqueta()).append("</strong> </div><br />");

            // Agregar el valor normal del campo
            informeHtml.append("<div style=\"text-align: justify;\">")
                    .append(campo.getValor() != null ? escapeHtml(campo.getValor()) : "")
                    .append("</div>");

            if (EtiquetaNemonico.INFORME_CAMPO_BINARIO.equals(campo.getTipo())) {
//                // Generar los radio buttons
//                informeHtml.append("<div>")
//                        .append("<input type=\"radio\" value=\"Si\"")
//                        .append("Si".equals(campo.getValor()) ? " checked=\"checked\"" : "").append("></input>")
//                        .append("<label>Acepto</label>")
//                        .append("<input type=\"radio\" value=\"No\"")
//                        .append(!"Si".equals(campo.getValor()) ? " checked=\"checked\"" : "").append("></input>")
//                        .append("<label>No Acepto</label>")
//                        .append("</div>");

                // Agregar el disclaimer debajo de los radio buttons
                informeHtml.append("<br /><br /><div class=\"disclaimer\">")
                        .append("<p>* En caso de aceptar, el/la agresado/a o padre/madre o apoderado accede a brindar la siguiente información:</p>")
                        .append("<p>Dirección, Teléfono fijo, Teléfono celular, Correo electrónico</p>")
                        .append("</div>");
            }
            informeHtml.append("</div>").append("<br /><br /><br />");
        }
        return informeHtml.toString();
    }

    private String procesarSeccionesOpcionales(String html, Map<String, String> variablesRequest, List<PlantillaVariable> variables) {
        for (PlantillaVariable variable : variables) {
            String clave = variable.getClave();
            String valor = variablesRequest.get(clave);

            if (valor == null || valor.trim().isEmpty()) {
                String regex = "(?s)<!--\\s*OPCIONAL:" + Pattern.quote(clave) + "\\s*-->.*?<!--\\s*/OPCIONAL:" + Pattern.quote(clave) + "\\s*-->";
                html = html.replaceAll(regex, "");
            }
        }
        return html;
    }


    /**
     * Actualiza las carpetas faltantes de Alfresco
     *
     * @param httpServletRequest HttpServletRequest datos del request.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    public RespuestaPorDefectoAuditoria<Boolean> actualizarCarpetasAlfresco(HttpServletRequest httpServletRequest) {
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

            List<FichaIdentificacionCarpeta> carpetasPadre = fichaIdentificacionCarpetaRepository.findAllCarpetasPadres();

            List<Catalogo> subcarpetasEsperadas = this.catalogoRepository.findByCatalogoPadreNemonicoAndEmpresaTokenIdentificadorAndRemovidoOrderByIdCatalogoDesc(
                    EtiquetaNemonico.CARPETA_GESTION_ADOLESCENTE,
                    empresa.getTokenIdentificador(),
                    false
            );

            for (FichaIdentificacionCarpeta carpetaPadre : carpetasPadre) {
                List<FichaIdentificacionCarpeta> subcarpetasActuales = fichaIdentificacionCarpetaRepository.
                        findSubcarpetasByCarpetaPadre(carpetaPadre.getFichaIdentificacion().getTokenIdentificador());

                for (Catalogo catalogo : subcarpetasEsperadas) {
                    boolean existe = subcarpetasActuales.stream().anyMatch(c ->
                            Objects.equals(c.getTipoDeGestionDeAdolescente().getTokenIdentificador(), catalogo.getTokenIdentificador()));

                    if (!existe) {

                        String nombreEsperado = quitarCaracteresEspeciales(catalogo.getNombre()).replace(" ", "-");
                        this.logService.info("nombreEsperado: " + nombreEsperado + ", getIdFichaIdentificacionCarpeta: " + carpetaPadre.getIdFichaIdentificacionCarpeta());
                        CarpetaDTO nuevaCarpeta = new CarpetaDTO();
                        nuevaCarpeta.setNombreCliente(nombreEsperado);
                        nuevaCarpeta.setDescripcion(catalogo.getDescripcion());

                        CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                        carpetaPadreDTO.setTokenIdentificador(carpetaPadre.getCarpeta().getTokenIdentificador());
                        nuevaCarpeta.setCarpetaDTOPadre(carpetaPadreDTO);

                        RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = carpetaService.crearCarpeta(httpServletRequest, true, nuevaCarpeta);
                        if (respuestaCarpeta.isExito()) {
                            nuevaCarpeta = respuestaCarpeta.getData();

                            FichaIdentificacionCarpeta fichaIdentificacionCarpetaHijo = new FichaIdentificacionCarpeta();
                            fichaIdentificacionCarpetaHijo.setCarpeta(
                                    this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                                            nuevaCarpeta.getTokenIdentificador(), false
                                    )
                            );
                            fichaIdentificacionCarpetaHijo.setFichaIdentificacion(carpetaPadre.getFichaIdentificacion());
                            fichaIdentificacionCarpetaHijo.setTipoDeGestionDeAdolescente(catalogo);
                            fichaIdentificacionCarpetaHijo.setIpCrea(httpServletRequest.getRemoteAddr());
                            fichaIdentificacionCarpetaHijo.setUsuarioSistemaCrea(usuarioSistema);
                            this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaHijo);
                        } else
                            this.logService.warn("Error al crear carpeta: " + respuesta);
                    }
                }
            }

            respuesta.llenarRespuestaExitosa("Carpetas Actualizadas", true);

        } catch (
                Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    public CatalogoDTO entidadADtoCatalogo(Catalogo entidad) {
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

    public JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
        if (entidad == null) return null;

        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setId(entidad.getIdJerarquia());
        dto.setNombre(entidad.getNombre());
        dto.setNemonico(entidad.getNemonico());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }

    private String escapeHtml(String input) {
        if (input == null) return "";

        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br/>");
    }
}
