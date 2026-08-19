package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

import java.util.ArrayList;
import java.util.Date;

@Data
public class EvaluacionMedicaDTO {
    private String tokenIdentificador;

    private String tokenIdFichaMedica;

    private Date fecha;

    private String talla;

    private String peso;

    private String numReferencia;

    private String recomendacion;

    private CatalogoDTO etapa;

    private CatalogoDTO tipoEvaluacion;

    private CatalogoDTO motivoConsulta;

    private ArrayList<CriterioEvaluacionMedicaSeguimientoDTO> criteriosAsociadosSeguimiento;

    private ArrayList<String> tokensCriteriosEliminar;

    private RecetaDTO receta;

    private String doctorAtencion;
    private String lugarAtencion;
}
