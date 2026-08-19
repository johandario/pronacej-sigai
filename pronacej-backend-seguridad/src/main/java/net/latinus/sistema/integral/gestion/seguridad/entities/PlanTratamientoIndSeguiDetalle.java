package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind_segui_detalle")
@Comment("Detalle de Seguimiento de Plan de tratamiento individual")
@EqualsAndHashCode(of = {"idPlanTratamientoIndSeguiDetalle"}, callSuper = true)
public class PlanTratamientoIndSeguiDetalle extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanTratamientoIndSeguiDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento_ind_interv")
    @Comment("Intervencion")
    private PlanTratamientoIndInterv planTratamientoIndInterv;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_frecuencia")
    @Comment("Frecuencia")
    private Catalogo frecuencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo_frecuencia_participacion")
    @Comment("Frecuencia de participación")
    private Catalogo frecuenciaParticipacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo_situacion_actual")
    @Comment("Situación actual")
    private Catalogo situacionActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo_actitud")
    @Comment("Actitud de participación")
    private Catalogo actitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo_aprovechamiento")
    @Comment("Aprovechamiento de participación")
    private Catalogo aprovechamiento;

    @Comment("Fecha de Inicio")
    private Date fechaInicio;

    @Comment("Fecha de edicion")
    private Date fechaFin;

    @Comment("Observaciones de seguimiento")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("Indicador deficiente")
    private Boolean indicadorDeficiente;

    @Comment("Indicador en proceso")
    private Boolean indicadorEnProceso;

    @Comment("Indicador logrado")
    private Boolean indicadorLogrado;

    @Comment("Análisis de seguimiento")
    @Column(columnDefinition = "TEXT")
    private String analisis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento_ind_segui_objetivo")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoIndSegui planTratamientoIndObjetivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento_ind_segui_no_criminogeno")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoIndSegui planTratamientoIndNoCriminogeno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento_ind_segui_diferenciada")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoIndSegui planTratamientoIndDiferenciada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento_ind_segui_medidas")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoIndSegui planTratamientoMedidas;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
