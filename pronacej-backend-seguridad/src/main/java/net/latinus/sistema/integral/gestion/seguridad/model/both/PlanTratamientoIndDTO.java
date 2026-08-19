package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPlanTratamiento"}, callSuper = true)
public class PlanTratamientoIndDTO extends CamposDTO implements Serializable {
    private Long idPlanTratamiento;
    private CatalogoDTO estado;
    private String nombreEstado;
    private String instTecnicas;
    private List<PlanTratamientoIndEspecifDTO> especFactores;
    private List<PlanTratamientoIndEspecifDTO> ejecMedidas;
    private List<PlanTratamientoIndEspecifDTO> unidadReceptora;
    private String factRiesgoNoCrimin;
    private String valRiesgo;
    private String hipotExplicativa;
    private String intensidadIntervTrat;
    private String tipoCentro;
    private String tipoAbierto;
    private Boolean completada;
    private Boolean esActivo;
    private List<PlanTratamientoIndIntervDTO> intervObjetivos;
    private List<PlanTratamientoIndIntervDTO> intervNoCriminogenos;
    private List<PlanTratamientoIndIntervDTO> intervDiferenciada;
    private List<PlanTratamientoIndIntervDTO> intervMedidas;
    private String tokenPadre;
    private Long idFichaIdentificacion;
    private String tokenExpedienteMatrizDetalle;
    private String tokenFichaIdentificacion;
    private List<CatalogoDTO> medidasAccesorias = new ArrayList<>();
}
