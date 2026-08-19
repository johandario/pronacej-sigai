package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.util.Date;

@Entity
@Data
@Table(name = "seg_trabajo_laboral")
@Comment("Registro de trabajo laboral del adolescente")
@EqualsAndHashCode(of = {"idTrabajoLaboral"}, callSuper = true)
public class TrabajoLaboral extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal")
    private Long idTrabajoLaboral;

    @Comment("Fecha de ingreso laboral")
    @Column(name = "fecha_ingreso_laboral")
    private Date fechaIngresoLaboral;

    @Comment("Cargo laboral")
    @Column(name = "cargo_laboral")
    private String cargoLaboral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro_institucion")
    @Comment("Institución donde labora")
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
