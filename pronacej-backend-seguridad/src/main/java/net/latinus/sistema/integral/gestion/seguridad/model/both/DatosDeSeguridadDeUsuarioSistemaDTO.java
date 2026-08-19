package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.CambioDeContraseniaRequest;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatosDeSeguridadDeUsuarioSistemaDTO extends CambioDeContraseniaRequest implements Serializable {

    private String passwordActual;

    private Boolean habilitar2DoFactorDeAutenticacion;
    private Boolean cambioDeContraseniaCadaNDias;

    private String tokenIdentificadorDeUsuarioSistema;

    private Integer diasExpiracionContrasenia;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
