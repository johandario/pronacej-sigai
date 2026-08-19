package net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "eje_orden_medica_detalle")
@Comment("Detalle de orden médica")
@EqualsAndHashCode(of = {"idOrdenMedicaDetalle"}, callSuper = true)
public class OrdenMedicaDetalle extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del detalle de la orden")
    private Long idOrdenMedicaDetalle;

    @JoinColumn(name = "id_especialidad_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Especialidad/Producto asociado")
    private EspecialidadProducto especialidadProducto;

    @JoinColumn(name = "id_orden_medica")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Orden a la que pertenece este detalle")
    private OrdenMedica ordenMedica;
}
