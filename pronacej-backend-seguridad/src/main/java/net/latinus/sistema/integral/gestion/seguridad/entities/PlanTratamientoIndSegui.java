package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import java.util.List;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind_segui")
@Comment("Encabezado de Seguimiento de Plan de tratamiento individual")
@EqualsAndHashCode(of = {"idPlanTratamientoIndSegui"}, callSuper = true)
public class PlanTratamientoIndSegui extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanTratamientoIndSegui;

    @Comment("Periodo del seguimiento")
    @ManyToOne
    @JoinColumn(name = "id_catalogo_periodo")
    private Catalogo periodoTiempo;

    @Comment("Programa")
    @Column(columnDefinition = "TEXT")
    private String programa;

    @Comment("Resumen")
    @Column(columnDefinition = "TEXT")
    private String resumen;

    @Comment("Estado de salud actual")
    @Column(columnDefinition = "TEXT")
    private String estadoSalud;

    @Comment("Observaciones")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("Estado de salud actual")
    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @Comment("Fecha de Inicio")
    private Date fechaInicio;

    @Comment("Fecha de edicion")
    private Date fechaFin;

    @OneToMany(mappedBy = "planTratamientoIndObjetivo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Comment("Objetivos de la intervención o tratamiento")
    private List<PlanTratamientoIndSeguiDetalle> intervObjetivos;

    @OneToMany(mappedBy = "planTratamientoIndNoCriminogeno", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Comment("Intervención sobre factores no criminogenos o desarrollo integral")
    private List<PlanTratamientoIndSeguiDetalle> intervNoCriminogenos;

    @OneToMany(mappedBy = "planTratamientoIndDiferenciada", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Comment("Intervención diferenciada")
    private List<PlanTratamientoIndSeguiDetalle> intervDiferenciada;

    @OneToMany(mappedBy = "planTratamientoMedidas", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Comment("Medidas socioeducativas")
    private List<PlanTratamientoIndSeguiDetalle> intervMedidas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación padre")
    private FichaIdentificacion fichaIdentificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento_ind")
    @Comment("Ficha de identificación padre")
    private PlanTratamientoInd planTratamientoInd;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
