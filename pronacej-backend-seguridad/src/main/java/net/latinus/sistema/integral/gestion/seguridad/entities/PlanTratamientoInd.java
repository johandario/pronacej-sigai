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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind")
@Comment("Encabezado de Plan de tratamiento individual")
@EqualsAndHashCode(of = {"idPlanTratamiento"}, callSuper = true)
public class PlanTratamientoInd extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanTratamiento;

    @Comment("Estado de pti")
    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado")
    private Catalogo estado;

    @Comment("Instrumentos y técnicas utilizadas")
    @Column(columnDefinition = "TEXT")
    private String instTecnicas;

    @OneToMany(mappedBy = "planTratamientoIndEspecFactores", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Especificación de factores de riesgo y protectores")
    private List<PlanTratamientoIndEspecif> especFactores;

    @OneToMany(mappedBy = "planTratamientoIndEjecMedidas", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Especificación de factores de riesgo y protectores")
    private List<PlanTratamientoIndEspecif> ejecMedidas;

    @OneToMany(mappedBy = "planTratamientoIndUnidadReceptora", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Especificación de factores de riesgo y protectores")
    private List<PlanTratamientoIndEspecif> unidadReceptora;

    @Comment("Factores de riesgo no criminógenos")
    @Column(columnDefinition = "TEXT")
    private String factRiesgoNoCrimin;

    @Comment("Valoración del riesgo de violencia o reinicidencia inicial global (alto/medio/bajo)")
    private String valRiesgo;

    @Comment("Hipótesis explicativa de la conducta infractora")
    @Column(columnDefinition = "TEXT")
    private String hipotExplicativa;

    @Comment("Intensidad de la intervención o tratamiento")
    @Column(columnDefinition = "TEXT")
    private String intensidadIntervTrat;

    @Comment("Tipo de centro cerrado/abierto")
    private String tipoCentro;

    @Comment("En caso de ser soa, si pertenece a Libertad, Comunitario, Amonestacion")
    private String tipoAbierto;

    @Comment("Si el plan ha sido completado")
    private Boolean completada = false;

    @OneToMany(mappedBy = "planTratamientoIndObjetivo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Comment("Objetivos de la intervención o tratamiento")
    private List<PlanTratamientoIndInterv> intervObjetivos;

    @OneToMany(mappedBy = "planTratamientoIndNoCriminogeno", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Comment("Intervención sobre factores no criminogenos o desarrollo integral")
    private List<PlanTratamientoIndInterv> intervNoCriminogenos;

    @OneToMany(mappedBy = "planTratamientoIndDiferenciada", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Comment("Intervención diferenciada")
    private List<PlanTratamientoIndInterv> intervDiferenciada;

    @OneToMany(mappedBy = "planTratamientoMedidas", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Comment("Medidas socioeducativas")
    private List<PlanTratamientoIndInterv> intervMedidas;

    @ManyToOne
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación padre")
    private FichaIdentificacion fichaIdentificacion;

    @ManyToOne
    @JoinColumn(name = "id_expediente_matriz_detalle")
    @Comment("Expediente matriz detalle vinculado al pti")
    private ExpedienteMatrizDetalle expedienteMatrizDetalle;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
