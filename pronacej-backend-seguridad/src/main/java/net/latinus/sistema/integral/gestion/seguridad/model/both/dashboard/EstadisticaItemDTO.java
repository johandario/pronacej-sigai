package net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;

/**
 * DTO genérico y reutilizable para cualquier estadística agregada del dashboard.
 * Cada instancia representa una categoría (etiqueta) y su cantidad de registros.
 * Diseñado para alimentar gráficos de barras, pie charts, líneas, etc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaItemDTO implements Serializable {

    /** Etiqueta legible del ítem: edad, nombre de sexo, nombre de país, nombre de departamento, etc. */
    private String etiqueta;

    /** Cantidad de adolescentes en esta categoría. */
    private Long cantidad;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}

