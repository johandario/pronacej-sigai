package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.apache.commons.validator.EmailValidator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FuncionarioDTO extends CamposDTO implements Serializable, CheckCamposRequeridos {
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String tokenIdentificadorTipoDeDocumento;
    private String numeroDeDocumento;
    private String numeroDeCelular;
    private String logo;
    private Long idDepartamento;
    private String departamento;
    private String tokenIdentificadorDepartamento;
    private Long idCargo;
    private String cargo;
    private Boolean cargoSuperRol;
    private String tokenIdentificadorCargo;
    private Boolean bloqueado = false;
    private Boolean removido;

    private List<FuncionarioJerarquiaRolDTO> asignaciones;

    private Long numeroCentros;

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

        if (this.email == null || this.email.isEmpty() || !EmailValidator.getInstance().isValid(this.email)) {
            resp.setMensaje("El email es inválido");
            return resp;
        }

        if (this.numeroDeDocumento == null || this.numeroDeDocumento.isEmpty()) {
            resp.setMensaje("El número de documento es requerido");
            return resp;
        }

        resp.llenarRespuestaExitosa("Datos requeridos válidos", true);

        return resp;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
