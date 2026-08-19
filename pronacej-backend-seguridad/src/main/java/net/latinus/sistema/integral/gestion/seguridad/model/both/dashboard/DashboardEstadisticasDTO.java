package net.latinus.sistema.integral.gestion.seguridad.model.both.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO contenedor de todas las estadísticas del dashboard para un centro dado.
 * <p>
 * Escalabilidad: para agregar un nuevo criterio basta con añadir un nuevo campo
 * {@code List<EstadisticaItemDTO>} y poblarlo en el servicio. No requiere cambios
 * en el controlador ni en la firma del endpoint.
 * </p>
 * Cada lista es compatible con gráficos de barras, pie chart, líneas, etc.
 * en el frontend: las etiquetas van al eje X / labels y las cantidades a la serie.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardEstadisticasDTO implements Serializable {

    /** Distribución de adolescentes por delito. */
    private List<EstadisticaItemDTO> porDelito = new ArrayList<>();

    /** Distribución de adolescentes por edad calculada (años). */
    private List<EstadisticaItemDTO> porEdad = new ArrayList<>();

    /** Distribución de adolescentes por tipo de sexo. */
    private List<EstadisticaItemDTO> porSexo = new ArrayList<>();

    /** Distribución de adolescentes por nacionalidad (gentilicio del país de nacimiento). */
    private List<EstadisticaItemDTO> porNacionalidad = new ArrayList<>();

    /** Distribución de adolescentes por departamento. */
    private List<EstadisticaItemDTO> porDepartamento = new ArrayList<>();

    /** Distribución de adolescentes por días de internación. */
    private List<EstadisticaItemDTO> porDiasInternacion = new ArrayList<>();

    /** Distribución de adolescentes por tipo de enfermedad. */
    private List<EstadisticaItemDTO> porTipoEnfermedad = new ArrayList<>();

    /** Distribución de adolescentes por grado de instrucción. */
    private List<EstadisticaItemDTO> porGradoInstruccion = new ArrayList<>();

    /** Distribución de adolescentes por número de hijos. */
    private List<EstadisticaItemDTO> porNumeroHijos = new ArrayList<>();

    /** Distribución de adolescentes por número de centros. */
    private List<EstadisticaItemDTO> porNumeroCentros = new ArrayList<>();

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}

