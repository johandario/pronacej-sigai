package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.exceptions.DesencriptacionExcep;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BodyEncriptado implements Serializable {

    private String body;
    private String llave;
    private List<String> chunkedBody;
    private Boolean logOut = false;

    /**
     * Devuelve un string json con el body descencriptado
     *
     * @param parametroDelSistemaRepository Long idEmpresa.
     * @param empresa                       Empresa.
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> desencriptarPorEmpresa(ParametroDelSistemaRepository parametroDelSistemaRepository,
                                                                       Empresa empresa) {

        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {

            ParametroDelSistema parametroDelSistema = parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, empresa, false
            );

            if (parametroDelSistema == null) {
                df.setMensaje("No se ha encontrado la clave de desencriptación");
                return df;
            }

            String valor = parametroDelSistema.getValor();

            if (valor == null || valor.isBlank()) {
                df.setMensaje("El valor de la clave para desencriptar es nulo o esta vacio");
                return df;
            }

            Aes aes = new Aes();

            String bodyDesencriptado = aes.decrypt(valor, this.body);

            df.llenarRespuestaExitosa("Se ha desencriptado correctamente", bodyDesencriptado);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Devuelve un string json con el body descencriptado con rsa
     *
     * @param parametroDelSistemaRepository Long idEmpresa.
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> desencriptarRSAPorEmpresa(ParametroDelSistemaRepository parametroDelSistemaRepository) {

        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {

            Page<ParametroDelSistema> parametroDelSistemaPage = parametroDelSistemaRepository.findByNemonicoAndRemovido(
                    EtiquetaNemonico.PARAM_RSA_CLAVE_PRIVADA_BACKEND, false,
                    PageRequest.of(0, 2, Sort.by("idParametroDelSistema").descending())
            );

            if (!parametroDelSistemaPage.hasContent()) {
                df.setMensaje("No se ha encontrado la clave de desencriptación");
                return df;
            }

            ParametroDelSistema parametroDelSistema = parametroDelSistemaPage.toList().getFirst();

            String valor = parametroDelSistema.getValor();

            if (valor == null || valor.isBlank()) {
                df.setMensaje("El valor de la clave para desencriptar es nulo o esta vacio");
                return df;
            }

            RSA rsa = new RSA(parametroDelSistemaRepository);

            String bodyDesencriptado = rsa.decryptBase64(this.chunkedBody, valor);

            df.llenarRespuestaExitosa("Se ha desencriptado correctamente", bodyDesencriptado);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Devuelve un objeto BodyEncriptado
     *
     * @param data      string que se va a encriptar con aes.
     * @param idEmpresa Long id de la empresa que se va a usar para la encriptacion
     * @param rsa       RSA objeto
     * @param aes       Aes objeto
     * @return BodyEncriptado
     */
    public static BodyEncriptado crearBodyEncriptado(String data, Long idEmpresa, RSA rsa, Aes aes) throws Exception {
        String claveAesTemp = new Date().getTime() + "_" + idEmpresa;

        RespuestaPorDefectoAuditoria<String> df = rsa.encriptarPorEmpresa(claveAesTemp, idEmpresa);
        if (!df.isExito()) {
            throw new DesencriptacionExcep(df.getMensaje());
        }

        String claveAesEncriptada = df.getData();
        String bodyEncriptadoAes = aes.encrypt(claveAesTemp, data);
        BodyEncriptado bodyEncriptado = new BodyEncriptado();
        bodyEncriptado.setBody(bodyEncriptadoAes);
        bodyEncriptado.setLlave(claveAesEncriptada);

        return bodyEncriptado;

    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}
