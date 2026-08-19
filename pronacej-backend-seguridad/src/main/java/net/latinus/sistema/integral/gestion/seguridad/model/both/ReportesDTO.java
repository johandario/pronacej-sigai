package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReportesDTO extends CamposDTO implements Serializable {

    private String nemonicoTipoSexo; // Nemonico del tipo de sexo (Ej. "TIPO_SEXO_MASCULINO", "TIPO_SEXO_FEMENINO")
    private String tokenIdentificadorCentro;
    private String nemonicoCentro;

}
