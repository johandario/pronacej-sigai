package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "eje_criterio_evaluacion_medica_seguimiento")
@Comment("Criterios Asociados a la Evaluacion de Seguimiento")
@EqualsAndHashCode(of = {"idCriterioEvaluacionMedicaSeguimiento"}, callSuper = true)
public class CriterioEvaluacionMedicaSeguimiento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del criterio de la evaluacion medica de seguimiento")
    private Long idCriterioEvaluacionMedicaSeguimiento;

    @JoinColumn(name = "id_tipo_evaluacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo del tipo de evaluacion")
    private Catalogo tipoEvaluacion;

    @JoinColumn(name = "id_tipo_criterion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo del tipo de criterio de evaluacion")
    private Catalogo criterioEvaluacion;

    @Comment("Descripcion de los criterios escogidos")
    private String descripcion;

    @JoinColumn(name = "id_evaluacion_medica", referencedColumnName = "idEvaluacionMedica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Evaluacion medica de seguimiento del detenido")
    private EvaluacionMedica evaluacionMedica;
}
