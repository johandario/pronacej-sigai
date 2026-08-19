package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import java.util.ArrayList;

@Data
public class OrdenMedicaDetalleDTO {
    private String tokenIdentificador;
    private EspecialidadProductoDTO especialidadProducto;
    private ArrayList<String> tokensCriteriosEliminar;
}
