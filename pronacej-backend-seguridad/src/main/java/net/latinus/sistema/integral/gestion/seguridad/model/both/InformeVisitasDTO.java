package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class InformeVisitasDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorPersonaRelacionada;
    private String tokenIdentificadorTipoAutorizacion;
    private Date fechaInicio;
    private Date fechaFin;
    private String causalesRestriccion;
    private String observaciones;
    private String tokenIdentificadorFichaPrincipal;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
