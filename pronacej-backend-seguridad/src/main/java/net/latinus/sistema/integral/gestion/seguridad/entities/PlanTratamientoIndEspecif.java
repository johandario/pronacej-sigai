package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind_especif")
@Comment("Tabla dependiente de Plan de tratamiento individual, para registros de especificaciones")
@EqualsAndHashCode(of = {"idPlanTratIndEspecif"})
public class PlanTratamientoIndEspecif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanTratIndEspecif;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_dimension")
    @Comment("Catalogo con opciones de especificaciones")
    private Catalogo dimension;

    @Comment("Factores de riesgo o necesidades criminogénicas presentes")
    @Column(columnDefinition = "TEXT")
    private String factorRiesgo;

    @Comment("Recursos o factores protectores presentes")
    @Column(columnDefinition = "TEXT")
    private String factorProtector;

    @Comment("Comentario")
    @Column(columnDefinition = "TEXT")
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_espec_factores", nullable = true)
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoInd planTratamientoIndEspecFactores;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_ejec_medidas", nullable = true)
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoInd planTratamientoIndEjecMedidas;

    @ManyToOne
    @JoinColumn(name = "id_plan_tratamiento_unidad_receptora", nullable = true)
    @Comment("Plan de tratamiento al que pertenence")
    private PlanTratamientoInd planTratamientoIndUnidadReceptora;
}
