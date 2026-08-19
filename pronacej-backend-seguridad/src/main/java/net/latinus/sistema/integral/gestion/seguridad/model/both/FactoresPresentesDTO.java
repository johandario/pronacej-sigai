package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class FactoresPresentesDTO extends CamposDTO implements Serializable {
    
    // Identificador de la ficha principal
    private String tokenIdentificadorFichaIdentificacion;
    
    // Factores protectores del adolescente
    private String factoresProtectores;
    
    // Factores de riesgo del adolescente
    private String factoresRiesgo;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}