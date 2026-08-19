package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RecaptchaV3Response;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RecaptchaService {

    @Value("${urlRecaptchaV3}")
    private String urlRecaptchaV3;

    @Autowired
    private ParametroDelSistemaService parametroDelSistemaService;

    private LogService logService = new LogService(RecaptchaService.class);

    /**
     * Devuelve un objeto RespuestaPorDefectoAuditoria<Boolean>
     *
     * @param tokenRecaptchav3 String token del recaptcha generado por el front.
     *
     * @return RespuestaPorDefectoAuditoria<Boolean> true si paso, false si no
     */
    public RespuestaPorDefectoAuditoria<Boolean> verificarRecaptchaV3(String tokenRecaptchav3, Long idEmpresa) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            ParametroDelSistema parametroDelSistemaRecaptchaActivo = this.parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                    EtiquetaNemonico.PARAM_RECAPTCHA_V3_ACTIVO, idEmpresa
            );

            if (parametroDelSistemaRecaptchaActivo == null) {
                df.setMensaje("No se pudo encontrar el parametro de sistema de uso del servicio de recaptcha v3, contacta a tu administrador");
                return df;
            }

            if (parametroDelSistemaRecaptchaActivo.getValor() == null) {
                df.setMensaje("No se pudo determinar el valor del parametro de sistema de uso del servicio de recaptcha v3, contacta a tu administrador");
                return df;
            }

            if (parametroDelSistemaRecaptchaActivo.getValor().equals("false")) {
                this.logService.warn("Recaptcha v3 desactivado");
                df.llenarRespuestaExitosa("No hay necesidad de verificar el recaptcha v3 debido a que este esta desactivado", true);
                return df;
            }

            ParametroDelSistema parametroDelSistemaRecaptchaSecretKey = this.parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                    EtiquetaNemonico.PARAM_RECAPTCHA_V3_SECRET_KEY, idEmpresa
            );

            if (parametroDelSistemaRecaptchaSecretKey == null) {
                df.setMensaje("No se pudo determinar el valor del secret key de uso del servicio de recaptcha v3, contacta a tu administrador");
                return df;
            }

            ParametroDelSistema parametroDelSistemaRecaptchaScoreMin = this.parametroDelSistemaService.encontrarPorNemonicoYEmpresa(
                    EtiquetaNemonico.PARAM_RECAPTCHA_V3_SCORE_MIN, idEmpresa
            );

            if (parametroDelSistemaRecaptchaScoreMin == null) {
                df.setMensaje("No se pudo encontrar el score mínimo para el uso del servicio de recaptcha v3, contacta a tu administrador");
                return df;
            }

            String secretKey = parametroDelSistemaRecaptchaSecretKey.getValor();
            Float scoreMin = Float.valueOf(parametroDelSistemaRecaptchaScoreMin.getValor());

            RestTemplate restTemplate = new RestTemplate();
            String urlCompleta = this.urlRecaptchaV3 + "?secret=" + secretKey
                    + "&response=" + tokenRecaptchav3;

            ResponseEntity<String> response = restTemplate.exchange(urlCompleta, HttpMethod.POST,
                    HttpEntity.EMPTY, String.class);
            this.logService.info("recaptcha v3 response: " + response);
            df.setMensajeErrorReal(response.getBody());
            RecaptchaV3Response recaptchaV3Response = new Gson().fromJson(response.getBody(), RecaptchaV3Response.class);

            if(!recaptchaV3Response.getSuccess()){
                df.setMensaje(response.getBody());
                return df;
            }

            if (recaptchaV3Response.getScore() < scoreMin) {
                df.setMensaje("No se pudo comprobar que sea una persona");
                return df;
            }

            df.llenarRespuestaExitosa("Score del recaptcha v3 fue: " +
                    recaptchaV3Response.getScore() + "el cual es suficiente para pasar", true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
