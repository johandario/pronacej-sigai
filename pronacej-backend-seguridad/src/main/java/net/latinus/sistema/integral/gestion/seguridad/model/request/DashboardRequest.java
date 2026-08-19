package net.latinus.sistema.integral.gestion.seguridad.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;

/**
 * Cuerpo de la solicitud para el endpoint de estadísticas del dashboard.
 * <p>
 * Si {@code tokenCentro} es {@code null} o vacío, el servicio devuelve
 * estadísticas agregadas sobre todos los centros de la empresa (obtenida del JWT).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DashboardRequest extends CamposDTO implements Serializable {

    /**
     * Token identificador del centro (Jerarquía) seleccionado.
     * {@code null} = todos los centros de la empresa.
     */
    private String tokenCentro;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}

