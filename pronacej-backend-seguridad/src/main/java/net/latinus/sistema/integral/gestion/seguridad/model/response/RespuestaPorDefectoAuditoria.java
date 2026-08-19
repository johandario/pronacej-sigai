package net.latinus.sistema.integral.gestion.seguridad.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.junit.platform.commons.util.ExceptionUtils;
import java.util.Base64;

@Data
@EqualsAndHashCode(callSuper = true)
public class RespuestaPorDefectoAuditoria<T> extends RespuestaPorDefecto<T> {

    private String mensajeErrorReal;
    private String tokenIdentificadorEmpresa;
    private Boolean logOut = false;


    /**
     * Llena el objeto con los datos obtenidos en la exception
     *
     * @param ex Objeto Exception.
     * @return void
     */
    @Override
    public void llenarConDatosDeException(Exception ex) {
        super.llenarConDatosDeException(ex);
        this.mensajeErrorReal = ExceptionUtils.readStackTrace(ex);
    }

    /**
     * LLena los datos con una respuesta exito
     *
     * @param mensaje Mensaje final que se le envia al usuario.
     * @param data    data final enviada al usuario.
     * @return void.
     */
    @Override
    public void llenarRespuestaExitosa(String mensaje, T data, String mensajeAuditoria) {
        super.llenarRespuestaExitosa(mensaje, data, mensajeAuditoria);
        this.mensajeErrorReal = null;
    }
    
    /**
     * LLena los datos con una respuesta exito
     *
     * @param mensaje Mensaje final que se le envia al usuario.
     * @param data    data final enviada al usuario.
     * @return void.
     */
    @Override
    public void llenarRespuestaExitosa(String mensaje, T data) {
        super.llenarRespuestaExitosa(mensaje, data);
        this.mensajeErrorReal = null;
    }

    /**
     * Devuelve un objeto RespuestaPorDefecto
     *
     * @return RespuestaPorDefecto
     */
    public RespuestaPorDefecto<T> transformarARespuestaPorDefecto() {
        RespuestaPorDefecto<T> df = new RespuestaPorDefecto<>();

        df.setMensaje(this.getMensaje());
        df.setData(this.getData());
        df.setExito(this.isExito());
        df.setTitulo(this.getTitulo());
        df.setCodigoEstado(this.getCodigoEstado());
        df.setSinAcceso(this.getSinAcceso());
        return df;
    }

    /**
     * Devuelve un objeto RespuestaPorDefecto<String>
     *
     * @return RespuestaPorDefecto<String>
     */
    public RespuestaPorDefecto<String> transformarARespuestaPorDefectoDataStringBase64() {
        RespuestaPorDefecto<String> df = new RespuestaPorDefecto<>();

        df.setMensaje(this.getMensaje());
        df.setData(this.getData() != null ? new String(Base64.getEncoder().encode(this.getData().toString().getBytes())) : null);
        df.setExito(this.isExito());
        df.setTitulo(this.getTitulo());
        df.setCodigoEstado(this.getCodigoEstado());
        df.setSinAcceso(this.getSinAcceso());
        return df;
    }

    /**
     * Devuelve un objeto BodyEncriptado con la encriptacion
     *
     * @param parametroDelSistemaRepository ParametroDelSistemaRepository.
     * @param empresa Empresa
     * @return BodyEncriptado
     */
    public BodyEncriptado transFormarEnbodyEncriptado(ParametroDelSistemaRepository parametroDelSistemaRepository,
                                                      Empresa empresa) throws Exception {

        BodyEncriptado bodyEncriptado = new BodyEncriptado();
        ParametroDelSistema parametroDelSistema = parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                EtiquetaNemonico.PARAM_AES_CLAVE, empresa, false
        );

        if (parametroDelSistema == null) {
            throw new Exception("El parametro del sistema con nemonico: " + EtiquetaNemonico.PARAM_AES_CLAVE);
        }

        String valor = parametroDelSistema.getValor();

        if (valor == null || valor.isBlank()) {
            throw new Exception("El valor de la clave para desencriptar es nulo o esta vacio");
        }

        Aes aes = new Aes();

        String bodyEncriptadoString = aes.encrypt(valor, this.toString());
        bodyEncriptado.setLlave("");
        bodyEncriptado.setBody(bodyEncriptadoString);

        return bodyEncriptado;
    }

    public BodyEncriptado crearBodyEncriptadoHelp(Long idEmpresa, RSA rsa, Aes aes) throws Exception {
        BodyEncriptado body = BodyEncriptado.crearBodyEncriptado(this.transformarARespuestaPorDefecto().toString(), idEmpresa, rsa, aes);
        body.setLogOut(this.logOut);
        return body;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
