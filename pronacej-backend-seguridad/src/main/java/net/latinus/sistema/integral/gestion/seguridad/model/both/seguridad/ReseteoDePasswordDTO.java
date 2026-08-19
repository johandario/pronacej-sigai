package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReseteoDePasswordDTO extends CamposDTO {

    private CatalogoDTO estadoDTO;
    private UsuarioSistemaEmpresaRolDTO usuarioSistemaEmpresaRolDTO;
    private EmpresaDTO empresaDTO;
    private PasswordUserSistemaDTO passwordUserSistemaDTO;

    private String recaptchaV3;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
