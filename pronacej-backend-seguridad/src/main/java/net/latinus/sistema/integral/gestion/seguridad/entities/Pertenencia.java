package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.util.List;

@Entity
@Data
@Table(name = "ia_pertenencia")
@Comment("Encabezado de gestión de pertenencias")
@EqualsAndHashCode(of = {"idPertenencia"}, callSuper = true)
public class Pertenencia extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPertenencia;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado")
    @Comment("Estado del registro de gestión de pertenencias")
    private Catalogo estado;

    @Comment("Comentario general de entrega")
    private String comentarioEgresos;

    @Comment("Comentario general de recepción")
    private String comentarioIngresos;

    @Comment("Comentario general de entrega al salir del centro")
    private String comentarioSalidaEgresos;

    @Comment("Comentario general de recepción al salir del centro")
    private String comentarioSalidaIngresos;

    @OneToMany(mappedBy = "pertenenciaEgreso", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Lista de pertenencias que entrega el centro")
    private List<PertenenciaDetalle> detalleEgresos;

    @OneToMany(mappedBy = "pertenenciaIngreso", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Lista de pertenencias que recibe el centro")
    private List<PertenenciaDetalle> detalleIngresos;

    @OneToMany(mappedBy = "pertenenciaSalidaIngreso", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Lista de pertenencias que se retira al adolescente al salir del centro")
    private List<PertenenciaDetalle> detalleSalidaIngresos;

    @JoinColumn(name = "id_ficha_ingreso", referencedColumnName = "idFichaIngreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha de identificación asociada al expediente")
    private FichaIngreso fichaIngreso;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha de identificación asociada al expediente")
    private FichaIdentificacion fichaIdentificacion;

}
