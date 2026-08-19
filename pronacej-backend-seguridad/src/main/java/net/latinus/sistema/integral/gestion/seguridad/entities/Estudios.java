package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.util.Date;

@Entity
@Data
@Table(name = "seg_estudios")
@Comment("Registro de estudios del adolescente")
@EqualsAndHashCode(of = {"idEstudios"}, callSuper = true)

public class Estudios extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal")
    private Long idEstudios;

    @Comment("Fecha de inicio de estudios")
    @Column(name = "fecha_inicio_estudios")
    private Date fechaInicioEstudios;

    @Comment("Ciclo académico actual")
    @Column(name = "ciclo_academico_actual")
    private String cicloAcademicoActual;

    @Comment("Indica si el estudio se realiza mediante convenio con PRONACEJ")
    @Column(name = "convenio_pronacej")
    private Boolean convenioPronacej;

    @Comment("Indica si el estudio se realiza de forma independiente")
    @Column(name = "independiente")
    private Boolean independiente;
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "id_registro_institucion")
    @Comment("Institución educativa")
    private RegistroInstitucion registroInstitucion;
    @ManyToOne

    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación padre")
    private FichaIdentificacion fichaIdentificacion;

    @Override

    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }

}