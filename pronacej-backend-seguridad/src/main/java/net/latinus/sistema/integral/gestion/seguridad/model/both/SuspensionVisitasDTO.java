package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class SuspensionVisitasDTO extends CamposDTO implements Serializable {
    
    private List<CometimientoInfraccionDTO> cometimientosInfraccion = new ArrayList<>();
    
    private List<String> tokenIdentificadorCausalesSuspensionSeleccionadas = new ArrayList<>();
    
    private Date fechaInicio;
    private Date fechaFin;
    private String oficioDeSancion;
    private String observaciones;
    private String tokenIdentificadorFichaPrincipal;
    
    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
