package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "eje_detalle_receta")
@Comment("Detalle de receta, incluye medicamento, dosis, frecuencia, etc.")
@EqualsAndHashCode(of = {"idDetalleReceta"}, callSuper = true)
public class DetalleReceta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del detalle de la receta")
    private Long idDetalleReceta;

    @Comment("Medicamento prescrito en el detalle de la receta")
    private String medicamento;

    @Comment("Dosis del medicamento prescrito")
    private String dosis;

    @Comment("Frecuencia de consumo del medicamento")
    private String frecuencia;

    @Comment("Indicaciones del medicamento")
    @Column(columnDefinition = "TEXT")
    private String indicaciones;

    @Comment("Concentración del medicamento (ej. 500 mg)")
    private String concentracion;

    @JoinColumn(name = "id_medicamento_completo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Medicamento relacionado al detalle de la receta")
    private Medicamento medicamentoCompleto;

    @JoinColumn(name = "id_catalogo_forma_farmaceutica", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con la forma farmacéutica del medicamento")
    private Catalogo formaFarmaceutica;

    @JoinColumn(name = "id_receta", referencedColumnName = "idReceta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Receta a la que pertenece este detalle")
    private Receta receta;
}
