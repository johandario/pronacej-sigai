package net.latinus.sistema.integral.gestion.seguridad.entities.salida;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "sal_reforzamiento")
@Comment("Tabla de actividades de reforzamiento")
@EqualsAndHashCode(of = {"idReforzamiento"}, callSuper = true)
public class Reforzamiento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reforzamiento")
    @Comment("Identificador único del reforzamiento")
    private Long idReforzamiento;

    @Comment("Bandera para validar si tiene plan de vida")
    @Column(name = "plan_vida", nullable = false)
    private Boolean planVida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion", nullable = false)
    @Comment("Adolescente al que pertenece el reforzamiento")
    private FichaIdentificacion fichaIdentificacion;
}
