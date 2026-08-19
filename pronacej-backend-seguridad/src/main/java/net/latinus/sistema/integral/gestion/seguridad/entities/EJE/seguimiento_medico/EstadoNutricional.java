package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "eje_estado_nutricional")
@Comment("Estado nutricional de la evaluacion medica")
@EqualsAndHashCode(of = {"idEstadoNutricional"}, callSuper = true)
public class EstadoNutricional extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del estado nutricional")
    private Long idEstadoNutricional;

    @JoinColumn(name = "id_catalogo_criterio", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con los criterios de estado nutricional")
    private Catalogo criterio;

    @JoinColumn(name = "id_catalogo_grado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con los grados de estado nutricional")
    private Catalogo grado;

    @JoinColumn(name = "id_evaluacion_medica", referencedColumnName = "idEvaluacionMedica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Evaluacion medica a la que pertenece el estado nutricional")
    private EvaluacionMedica evaluacionMedica;
}
