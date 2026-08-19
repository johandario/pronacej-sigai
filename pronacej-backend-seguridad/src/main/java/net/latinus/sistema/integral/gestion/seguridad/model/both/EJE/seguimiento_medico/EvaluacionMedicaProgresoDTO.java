package net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

import java.util.ArrayList;
import java.util.Date;

@Data
public class EvaluacionMedicaProgresoDTO {

    private String tokenIdentificador;

    private String tokenIdFichaMedica;

    private Date fecha;

    private CatalogoDTO estadoNutricional;

    private CatalogoDTO tipoEvaluacionProgreso;

    private ArrayList<CriterioEvaluacionMedicaProgresoDTO> criteriosEvaluacionProgresoAsociados;

    private ArrayList<String> tokensCriteriosEliminar;

    private String tokenIdentificadorFichaIdentificacion;

    private CatalogoDTO tipoDesnutricion;

    // Atributos específicos
    private String grado;
    private String peso;
    private String talla;
    private String imc;
    private String impresionDiagnostico;
    private String manejoTerapeutico;

    // Atributos booleanos
    private Boolean clinicamenteSano;
    private Boolean enfermo;
}
