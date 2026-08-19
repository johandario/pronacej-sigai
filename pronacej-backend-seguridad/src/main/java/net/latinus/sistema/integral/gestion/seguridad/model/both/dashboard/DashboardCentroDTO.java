package net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;

/**
 * DTO que representa un centro (Jerarquía hija con padre SOA, CJDR o UAPISE)
 * disponible para seleccionar en el dashboard.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DashboardCentroDTO extends CamposDTO implements Serializable {

    /** Nombre del centro (eg. "Centro Juvenil Santa Margarita"). */
    private String nombre;

    /** Nemónico del tipo de centro padre: 'SOA', 'CJDR' o 'UAPISE'. */
    private String nemonicoPadre;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}

