package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

import java.util.ArrayList;

@Data
public class DetalleRecetaDTO {
    private String tokenIdentificador;

    private String medicamento;

    private String dosis;

    private String frecuencia;

    private String indicaciones;

    private String concentracion;

    private MedicamentoDTO medicamentoCompleto;

    private CatalogoDTO formaFarmaceutica;

    private ArrayList<String> tokensCriteriosEliminar;
}
