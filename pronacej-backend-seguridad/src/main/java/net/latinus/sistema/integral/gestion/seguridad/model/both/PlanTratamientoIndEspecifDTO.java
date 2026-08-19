package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanTratamientoIndEspecifDTO implements Serializable {
    private Long idPlanTratIndEspecif;
    private CatalogoDTO dimension;
    private String factorRiesgo;
    private String factorProtector;
    private String comentario;
}
