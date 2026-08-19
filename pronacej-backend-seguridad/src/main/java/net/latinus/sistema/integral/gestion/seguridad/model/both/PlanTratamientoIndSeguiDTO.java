package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPlanTratamientoIndSegui"}, callSuper = true)
public class PlanTratamientoIndSeguiDTO extends CamposDTO implements Serializable {
    private Long idPlanTratamientoIndSegui;
    private CatalogoDTO periodoTiempo;
    private String nombrePeriodoTiempo;
    private String programa;
    private String resumen;
    private String estadoSalud;
    private String observaciones;
    private String recomendaciones;
    private Date fechaInicio;
    private Date fechaFin;
    private String fecInicio;
    private String fecFin;
    private List<PlanTratamientoIndSeguiDetalleDTO> intervObjetivos;
    private List<PlanTratamientoIndSeguiDetalleDTO> intervNoCriminogenos;
    private List<PlanTratamientoIndSeguiDetalleDTO> intervDiferenciada;
    private List<PlanTratamientoIndSeguiDetalleDTO> intervMedidas;
    private String tokenPadre;
    private Long idPlanTratamiento;
}
