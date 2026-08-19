package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_persona_relacionada_enfermedad")
@Comment("Enfermedad relacionada a la persona relacionada a la ficha")
@EqualsAndHashCode(of = {"idPersonasRelacionadaEnfermedad"}, callSuper = true)
public class PersonaRelacionadaEnfermedad extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la enfermedad relacionada")
    private Long idPersonasRelacionadaEnfermedad;

    @ManyToOne
    @JoinColumn(name = "id_persona_relacionada", referencedColumnName = "idPersonasRelacionadas")
    private PersonaRelacionada idPersonasRelacionadas;

    @ManyToOne
    @JoinColumn(name = "id_ficha_medica", referencedColumnName = "idFichaMedica")
    @Comment("Id de la ficha médica")
    private FichaMedica idFichaMedica;

    @ManyToOne
    @JoinColumn(name = "id_tipo_enfermedad", referencedColumnName = "idCatalogo")
    private Catalogo tipoEnfermedad;

    @ManyToOne
    @JoinColumn(name = "id_clasificacion_enfermedad")
    private ClasificacionEnfermedad clasificacionEnfermedad;

    @ManyToOne
    @JoinColumn(name = "id_tipo_parentesco")
    private Catalogo tipoParentesco;

    @ManyToOne
    @JoinColumn(name = "id_sexo_parentesco")
    private Catalogo sexoParentesco;

    @Column(columnDefinition = "TEXT")
    @Comment("Detalle de la enfermedad")
    private String detalle;

    @Comment("Se detalla si la enfermedad se encuentra presente")
    private Boolean enfermedadActual;

}
