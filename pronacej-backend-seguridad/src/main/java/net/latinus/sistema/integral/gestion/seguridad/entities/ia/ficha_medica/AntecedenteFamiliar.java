package net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ai_antecedente_familiar")
@Comment("Antecedentes de familiares con enfermendades del detenido")
@EqualsAndHashCode(of = {"idAntecedenteFamiliar"}, callSuper = true)
public class AntecedenteFamiliar extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del antecedente familiar")
    private Long idAntecedenteFamiliar;


    @JoinColumn(name = "id_catalogo_enfermedad", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con la enfermedad")
    private Catalogo enfermedad;

    @JoinColumn(name = "id_catalogo_parentezco", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo con el parentezco")
    private Catalogo parentezco;


    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha identificacion a la que pertenecen los antecedentes")
    private FichaIdentificacion fichaIdentificacion;
}
