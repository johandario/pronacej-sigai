package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import java.util.ArrayList;
import java.util.Date;

@Data
public class OrdenMedicaDTO {
    private String tokenIdentificador;
    private String tokenIdEvaluacionMedica;
    private String numeroOrden;
    private Date fechaEmision;
    private String observaciones;
    private ArrayList<OrdenMedicaDetalleDTO> detalles;
}
