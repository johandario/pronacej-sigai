package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
public class BodyJWTFront {
    private String identificadorRol;
    private String identificadorEmpresa;
    private String identificadorUsuarioSistema;
    private String identificadorJerarquia;
    private String identificadorRolJerarquia;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
