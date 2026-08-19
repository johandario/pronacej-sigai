package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.DetalleRecetaDTO;

import java.util.ArrayList;
import java.util.Date;

@Data
public class RecetaDTO {
    private String tokenIdentificador;

    private String tokenIdEvaluacionMedica;

    private String numeroReceta;

    private Date fechaEmision;

    private String observaciones;

    private CatalogoDTO especialidad;

    private ArrayList<DetalleRecetaDTO> detalles;
}
