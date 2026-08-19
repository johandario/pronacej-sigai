package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "eje_diagnostico")
@Comment("Diagnostico de evaluacion medica")
@EqualsAndHashCode(of = {"idDiagnostico"}, callSuper = true)
public class Diagnostico extends EntidadBase  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del diagnostico")
    private Long idDiagnostico;

    @Comment("Codigo del diagnostico")
    private String codDiagnostico;

    @Comment("Diagnostico")
    private String diagnostico;

    @JoinColumn(name = "id_catalogo_tipo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con tipos de diagnostico")
    private Catalogo tipoDiagnostico;

    @Column(columnDefinition = "TEXT")
    @Comment("tratamiento del diagnostico")
    private String tratamiento;

    @Column(columnDefinition = "TEXT")
    @Comment("indicaciones del diagnostico")
    private String indicaciones;

    @Column(columnDefinition = "TEXT")
    @Comment("examenes del diagnostico")
    private String examenes;

    @Column(columnDefinition = "TEXT")
    @Comment("medicamentos del diagnostico")
    private String medicamentos;

    @JoinColumn(name = "id_evaluacion_medica", referencedColumnName = "idEvaluacionMedica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Evaluacion medica a la que pertenece el diagnostico")
    private EvaluacionMedica evaluacionMedica;
}
