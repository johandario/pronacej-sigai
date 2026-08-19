package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CheckCamposRequeridos;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FuncionarioJerarquiaRolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.apache.commons.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UsuarioSistemaDTO extends CamposDTO implements Serializable, CheckCamposRequeridos {

    private String nombres;
    private String apellidos;
    private String userName;
    private String email;
    private String telefono;
    private String tokenIdentificadorTipoDeDocumento;
    private String numeroDeDocumento;
    private String numeroDeCelular;
    private String logo;
    private String password;
    private Boolean bloqueado = false;

    private List<FuncionarioJerarquiaRolDTO> asignaciones;

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> chequearValoresRequeridos() {
        RespuestaPorDefectoAuditoria<Boolean> resp = new RespuestaPorDefectoAuditoria<>();

        if (this.nombres == null || this.nombres.isEmpty()) {
            resp.setMensaje("El nombre es requerido");
            return resp;
        }

        if (this.apellidos == null || this.apellidos.isEmpty()) {
            resp.setMensaje("El apellido es requerido");
            return resp;
        }

        if (this.userName == null || this.userName.isEmpty()) {
            resp.setMensaje("El username es requerido");
            return resp;
        }

        if (this.email == null || this.email.isEmpty() || !EmailValidator.getInstance().isValid(this.email)) {
            resp.setMensaje("El email es inválido");
            return resp;
        }

        if (this.numeroDeDocumento == null || this.numeroDeDocumento.isEmpty()) {
            resp.setMensaje("El número de documento es requerido");
            return resp;
        }

        if (this.password == null || this.password.isEmpty()) {
            resp.setMensaje("El password es requerido");
            return resp;
        }

        resp.llenarRespuestaExitosa("Datos requeridos válidos", true);

        return resp;
    }

    @Override
    public String toString() {
        try {
            Gson gson = new GsonBuilder().setDateFormat(EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER).create();
            return gson.toJson(this);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Logger logger = LoggerFactory.getLogger(e.getClass());
            logger.error(e.toString());
            return "";
        }
    }
}
