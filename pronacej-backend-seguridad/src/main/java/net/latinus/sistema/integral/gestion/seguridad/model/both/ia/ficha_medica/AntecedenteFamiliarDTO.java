package net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

@Data
public class AntecedenteFamiliarDTO {
    private String tokenIdentificador;

    private String tokenIdFichaIdentificacion;

    private CatalogoDTO enfermedad;

    private CatalogoDTO parentesco;
}
