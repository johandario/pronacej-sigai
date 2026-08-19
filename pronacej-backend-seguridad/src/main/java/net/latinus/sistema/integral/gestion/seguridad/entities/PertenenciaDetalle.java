package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_pertenencia_detalle")
@Comment("Detalle de gestión de pertenencias")
@EqualsAndHashCode(of = {"idPertenenciaDetalle"}, callSuper = true)
public class PertenenciaDetalle extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPertenenciaDetalle;

    @Comment("Nombre del item")
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_tipo")
    @Comment("Nombre del tipo de item (prenda/artículo=")
    private Catalogo tipo;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado")
    @Comment("Estado del item (prenda/artículo=")
    private Catalogo estado;

    @Comment("Cantidad de items")
    private Integer cantidad;

    @Comment("Comentario de item de entrega/recepción")
    private String observacion;

    @ManyToOne
    @JoinColumn(name = "id_pertenencia_egreso")
    @Comment("Encabezado al que pertence el detalle")
    private Pertenencia pertenenciaEgreso;

    @ManyToOne
    @JoinColumn(name = "id_pertenencia_ingreso")
    @Comment("Encabezado al que pertence el detalle")
    private Pertenencia pertenenciaIngreso;

    @ManyToOne
    @JoinColumn(name = "id_pertenencia_salida_ingreso")
    @Comment("Encabezado al que pertence el detalle")
    private Pertenencia pertenenciaSalidaIngreso;
}
