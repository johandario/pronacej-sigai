package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class InformeEgresoPIIDTO extends CamposDTO implements Serializable {
    
    // Identificadores de relación
    private String tokenIdentificadorFichaIdentificacion;
    private String tokenIdentificadorInformeSeguimientoPII;
    
    // Campos de motivo de ingreso
    private String motivoIngresoPII;
    
    // Campos de descripción del plan de tratamiento
    private String descripcionPsicologicaPlanTratamiento;
    private String descripcionSocialPlanTratamiento;
    private String descripcionConductualPlanTratamiento;
    private String descripcionFamiliarPlanTratamiento;
    private String descripcionNivelRiesgoPlanTratamiento;
    
    // Campos de evolución del plan de tratamiento
    private String descripcionEvolucionPsicologicaPlanTratamiento;
    private String descripcionEvolucionSocialPlanTratamiento;
    private String descripcionEvolucionConductualPlanTratamiento;
    private String descripcionEvolucionFamiliarPlanTratamiento;
    private String descripcionEvolucionNivelRiesgoPlanTratamiento;
    
    // Campos de conclusiones y recomendaciones
    private String conclusiones;
    private String recomendaciones;
    
    // Campos de usuario
    private String nombreCompletoUsuarioCreacion;
    private Boolean esVisualizacion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}