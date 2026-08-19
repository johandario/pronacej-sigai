package net.latinus.sistema.integral.gestion.seguridad.service;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;

import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.CorreoTemplate;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.request.EnvioEmailRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CorreoTemplateRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@AllArgsConstructor
@Service
public class EmailService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private CorreoTemplateRepository correoTemplateRepository;

    private EmpresaRepository empresaRepository;

    private JwtProviderService jwtProviderService;

    /**
     * Envia un correo con el contenido deseado
     *
     * @param httpServletRequest List<String> Lista de receptores de correo.
     * @param envioEmailRequest  EnvioEmailRequest
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    public RespuestaPorDefectoAuditoria<Boolean> enviarCorreoPrueba(HttpServletRequest httpServletRequest,
                                                                    EnvioEmailRequest envioEmailRequest) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try{
            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if(!df2.isExito()){
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                return df;
            }

            return this.enviarCorreo(
                    envioEmailRequest.getEmailsTo(), envioEmailRequest.getRazon(), envioEmailRequest.getContenido()
                    , envioEmailRequest.getTokenEmpresa(), envioEmailRequest.getTipo(),envioEmailRequest.getMultipartFiles()
            );
        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Envia un correo con el contenido deseado
     *
     * @param emailsTo       List<String> Lista de receptores de correo.
     * @param razon          String razon del correo.
     * @param contenido      String contenido del correo.
     * @param tokenEmpresa   String token identificador de la empresa.
     * @param tipo           String tipo de body a enviar en el correo ejemplo "text/html"
     * @param multipartFiles MultipartFile[] objetos archivos para adjuntar en el correo
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    public RespuestaPorDefectoAuditoria<Boolean> enviarCorreo(List<String> emailsTo, String razon, String contenido
            , String tokenEmpresa, String tipo, MultipartFile[] multipartFiles) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenEmpresa, false);

            if (empresa == null) {
                df.setMensaje("La empresa no existe");
                return df;
            }

            ParametroDelSistema paramAuth = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_AUTH, false
            );

            if (paramAuth == null || paramAuth.getValor() == null || paramAuth.getValor().isEmpty()) {
                df.setMensaje("No se ha establecido un auth para el envio de correo");
                return df;
            }

            ParametroDelSistema paramTLS = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_TLS_ENABLE, false
            );

            if (paramTLS == null || paramTLS.getValor() == null || paramTLS.getValor().isEmpty()) {
                df.setMensaje("No se ha establecido un param tls para el envio de correo");
                return df;
            }

            ParametroDelSistema paramSSL = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_SSL_ENABLE, false
            );

            ParametroDelSistema paramHOST = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_HOST, false
            );

            if (paramHOST == null || paramHOST.getValor() == null || paramHOST.getValor().isEmpty()) {
                df.setMensaje("No se ha establecido un host para el envio de correo");
                return df;
            }

            ParametroDelSistema paramPORT = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_PORT, false
            );

            if (paramPORT == null || paramPORT.getValor() == null || paramPORT.getValor().isEmpty()) {
                df.setMensaje("No se ha establecido un puerto para el envio de correo");
                return df;
            }

            ParametroDelSistema paramDEBUG = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_DEBUG, false
            );

            if (paramDEBUG == null || paramDEBUG.getValor() == null || paramDEBUG.getValor().isEmpty()) {
                df.setMensaje("No se ha establecido un debug para el envio de correo");
                return df;
            }

            ParametroDelSistema paramUserName = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_USERNAME, false
            );

            if (paramUserName == null || paramUserName.getValor() == null || paramUserName.getValor().isEmpty()) {
                df.setMensaje("No se ha establecido un username para el envio de correo");
                return df;
            }

            ParametroDelSistema paramPassword = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                    tokenEmpresa, EtiquetaNemonico.PARAM_ENVIO_DE_CORREO_SISTEMA_PASSWORD, false
            );

            if (paramPassword == null || paramPassword.getValor() == null || paramPassword.getValor().isEmpty()) {
                df.setMensaje("No se ha establecido un password para el envio de correo");
                return df;
            }

            String username = paramUserName.getValor();
            String password = paramPassword.getValor();

            df = this.enviarCorreoHelp(
                    paramAuth.getValor(),
                    (paramSSL != null) ? paramSSL.getValor() : null,
                    paramTLS.getValor(),
                    paramHOST.getValor(),
                    paramPORT.getValor(),
                    paramDEBUG.getValor(),
                    username,
                    password,
                    emailsTo, razon, contenido,
                    tipo, multipartFiles
            );


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Envia un correo con el contenido deseado y las configuraciones proporcionadas
     *
     * @param smtpAuth        string smtp activo.
     * @param sslEnable       string ssl activo.
     * @param startTTlsEnable String tls activo.
     * @param host            String host del correo electronico.
     * @param port            String puerto del servidor de correo.
     * @param username        String username del correo electronico
     * @param password        String password para el envio de correo
     * @param emailsTo        List<String> lista de destinatarios de correos electronicos
     * @param razon           String razon del correo electronico
     * @param contenido       String contenido del correo electronico
     * @param tipo            String tipo de body a enviar en el correo ejemplo "text/html"
     * @param multipartFiles  MultipartFile[] objetos archivos para adjuntar en el correo
     * @return RespuestaPorDefectoAuditoria<Boolean>
     * @oaram debug String debug activo del envio de correo
     */
    private RespuestaPorDefectoAuditoria<Boolean> enviarCorreoHelp(
            String smtpAuth, String sslEnable, String startTTlsEnable, String host,
            String port, String debug, String username, String password,
            List<String> emailsTo, String razon, String contenido,
            String tipo, MultipartFile[] multipartFiles
    ) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            Properties props = new Properties();

            if (sslEnable != null && !sslEnable.isEmpty()) {
                props.put("mail.smtp.ssl.enable", smtpAuth);
            }

            props.put("mail.smtp.auth", smtpAuth);
            props.put("mail.smtp.ssl.checkserveridentity", true);
            props.put("mail.smtp.starttls.enable", startTTlsEnable);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.debug", debug);
            props.put("mail.mime.charset", "utf-8");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(username));
            InternetAddress[] recipientAddress = new InternetAddress[emailsTo.size()];
            for (int i = 0; emailsTo.size() > i; i++) {
                recipientAddress[i] = new InternetAddress(emailsTo.get(i));
            }

            msg.setRecipients(Message.RecipientType.TO,
                    recipientAddress);

            msg.setSubject(razon);
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(contenido, tipo);
            multipart.addBodyPart(messageBodyPart);

            if (multipartFiles != null) {
                for (MultipartFile multipartFile : multipartFiles) {
                    MimeBodyPart attachment = new MimeBodyPart();
                    ByteArrayDataSource ds = new ByteArrayDataSource(multipartFile.getInputStream(),
                            multipartFile.getContentType());
                    attachment.setDataHandler(new DataHandler(ds));
                    attachment.setFileName(multipartFile.getOriginalFilename());

                    multipart.addBodyPart(attachment);
                }
            }

            msg.setContent(multipart, tipo);
            Transport.send(msg);

            df.llenarRespuestaExitosa("Se ha enviado un correo exitoso a: " +
                    emailsTo, true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Envia un correo con el contenido de un template
     *
     * @param emailsTo               List<String> lista de correos receptores.
     * @param nemonicoCorreoTemplate String nemonico del template de correo a usar.
     * @param valores                Map<String, String> mapa que contiene los valores a ser reemplazados en el correo a enviar.
     * @param tokenEmpresa           String token identificador de la empresa.
     * @return RespuestaPorDefectoAuditoria<Boolean>
     */
    public RespuestaPorDefectoAuditoria<Boolean> enviarCorreoConTemplate(String nemonicoCorreoTemplate,
                                                                         List<String> emailsTo,
                                                                         String tokenEmpresa,
                                                                         Map<String, String> valores) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            CorreoTemplate correosTemplate = this.correoTemplateRepository.findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
                    nemonicoCorreoTemplate, tokenEmpresa, false
            );

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(
                    tokenEmpresa, false
            );

            if (correosTemplate == null) {
                df.setMensaje("El correo template no existe");
                return df;
            }

            String stringHtmlCorreo = correosTemplate.getCorreoString();

            if (stringHtmlCorreo == null || stringHtmlCorreo.isEmpty()) {
                df.setMensaje("El contenido html de la plantilla de correo es inválido");
                return df;
            }

            if (valores != null) {
                valores.put("[NOMBRE_APLICACION]", "PROGRAMA NACIONAL DE CENTROS JUVENILES");
                valores.put("[FECHA_ENVIO_CORREO]", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                if (empresa != null) {
                    valores.put("[COLOR_PRIMARIO]", empresa.getColorPrimarioHex());
                    valores.put("[COLOR_TERCIARIO]", empresa.getColorSecundarioHex());
                    valores.put("[COLOR_SECUNDARIO]", empresa.getColorSecundarioHex());
                    valores.put("[NOMBRE_EMPRESA]", empresa.getNombre());

                    valores.put("[URL_EMPRESA]", empresa.getUrlPagina());
                    valores.put("[URL_LOGO_EMPRESA]", empresa.getUrlLogo());

                }
                FuncionesAyuda funcionesAyuda = new FuncionesAyuda();
                for (var entry : valores.entrySet()) {
                    stringHtmlCorreo = stringHtmlCorreo.replace(
                            entry.getKey(), funcionesAyuda.reemplarTildesPorCaracterEspecial(entry.getValue())
                    );
                }

                stringHtmlCorreo = funcionesAyuda.reemplarTildesPorCaracterEspecial(stringHtmlCorreo);
            }

            df = this.enviarCorreo(emailsTo, correosTemplate.getRazon(), stringHtmlCorreo, tokenEmpresa, "text/html", null);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /*
    private RespuestaPorDefectoAuditoria<Boolean> enviarCorreoAngusEmail( String smtpAuth, String startTTlsEnable, String host,
                                                                          String port, String debug, String username, String password,
                                                                          List<String> emailsTo, String razon, String contenido,
                                                                          String tipo, MultipartFile[] multipartFiles){
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try{

            Properties props = new Properties();

            props.put("mail.smtp.auth", smtpAuth);
            props.put("mail.smtp.ssl.checkserveridentity", true);
            props.put("mail.smtp.starttls.enable", startTTlsEnable);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.debug", debug);
            props.put("mail.mime.charset", "utf-8");
            props.put("mail.smtp.ssl.trust", "sandbox.smtp.mailtrap.io");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
     */
}
