package net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ai_ingreso_centro_juvenil")
@Comment("Centros juveniles/rehabilitacion/tutelares donde ha estado el ingresado")
@EqualsAndHashCode(of = {"idIngresoCentroJuvenil"}, callSuper = true)
public class IngresoCentroJuvenil extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del centro")
    private Long idIngresoCentroJuvenil;

    @Comment("Nombre del centro")
    private String centro;

    @Comment("Fecha de ingreso al centro")
    private Date fechaIngreso;
    @Comment("Fecha de egreso del centro")
    private Date fechaEgreso;

    @JoinColumn(name = "id_catalogo_motivo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catalogo motivo por el que ingresó al centro")
    private Catalogo motivo;


    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha identificacion a la que pertenecen los antecedentes")
    private FichaIdentificacion fichaIdentificacion;

}
