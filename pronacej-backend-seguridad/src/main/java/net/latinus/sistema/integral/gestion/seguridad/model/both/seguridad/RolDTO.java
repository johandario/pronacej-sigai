package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CheckCamposRequeridos;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class RolDTO extends CamposDTO implements Serializable, CheckCamposRequeridos {

    private String nombre;
    private String codigo;
    private String descripcion;
    private Boolean esSuperRol = false;
    private Boolean esRolPorDefecto = false;
    private Integer diasExpiracionPassword;

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> chequearValoresRequeridos() {
        RespuestaPorDefectoAuditoria<Boolean> resp = new RespuestaPorDefectoAuditoria<>();

        if (this.nombre == null || this.nombre.isEmpty()) {
            resp.setMensaje("El nombre es requerido");
            return resp;
        }

        if (this.codigo == null || this.codigo.isEmpty()) {
            resp.setMensaje("El codigo es requerido");
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
