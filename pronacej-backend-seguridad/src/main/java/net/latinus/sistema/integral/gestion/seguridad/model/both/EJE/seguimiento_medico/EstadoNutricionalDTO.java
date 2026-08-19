package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

@Data
public class EstadoNutricionalDTO {
    private String tokenIdentificador;

    private String tokenIdEvaluacionMedica;

    private CatalogoDTO criterio;

    private CatalogoDTO grado;
}
