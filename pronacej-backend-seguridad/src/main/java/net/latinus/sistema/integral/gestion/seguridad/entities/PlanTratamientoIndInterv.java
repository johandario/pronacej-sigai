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
@Table(name = "ia_plan_tratamiento_ind_interv")
@Comment("Tabla dependiente de Plan de tratamiento individual, para registros de intervención")
@EqualsAndHashCode(of = {"idPlanTratIndInterv"}, callSuper = true)
public class PlanTratamientoIndInterv extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanTratIndInterv;

    @Comment("Versión")
    private String version;

    @Comment("Es reajuste")
    private Boolean reajuste;

    @Comment("Se encuentra activo")
    private Boolean activo;

    @Comment("Fundamentación de reajuste")
    @Column(columnDefinition = "TEXT")
    private String fundamentacionReajuste;

    @Comment("Fecha de reajuste")
    private Date fechaReajuste;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_dimension")
    @Comment("Dimensiones")
    private Catalogo dimension;

    @Comment("Objetivos")
    @Column(columnDefinition = "TEXT")
    private String objetivo;

    @Comment("Actividad o programa")
    @Column(columnDefinition = "TEXT")
    private String actividadPrograma;

    @Comment("Equipo o profesional responsable")
    @Column(columnDefinition = "TEXT")
    private String equipoResponsable;

    @Comment("Tiempo estimado en días")
    @Column(columnDefinition = "TEXT")
    private String tiempoEstimado;

    @Comment("Número de atenciones individuales")
    @Column(columnDefinition = "TEXT")
    private String numAtencionIndividual;

    @Comment("Número de atenciones grupales")
    @Column(columnDefinition = "TEXT")
    private String numAtencionGrupal;

    @Comment("Lugar")
    @Column(columnDefinition = "TEXT")
    private String lugar;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_modalidad")
    @Comment("Modalidad")
    private Catalogo modalidad;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_frecuencia")
    @Comment("Frecuencia")
    private Catalogo frecuencia;

    @Comment("Descripción de plan")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_ind_objetivo")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoInd planTratamientoIndObjetivo;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_ind_no_criminogeno")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoInd planTratamientoIndNoCriminogeno;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_ind_diferenciada")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoInd planTratamientoIndDiferenciada;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_ind_medidas")
    @JsonBackReference
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoInd planTratamientoMedidas;

    @Comment("Fecha de Inicio")
    private Date fechaInicio;

    @Comment("Fecha de edicion")
    private Date fechaFin;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
