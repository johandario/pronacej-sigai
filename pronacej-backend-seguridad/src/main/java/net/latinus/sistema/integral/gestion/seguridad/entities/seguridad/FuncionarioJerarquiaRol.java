package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.CargosJerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import jakarta.persistence.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@Table(
        name = "funcionario_jerarquia_rol",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_funcionario_jerarquia_rol",
                columnNames = {"id_funcionario","id_jerarquia","id_rol"}
        )
)
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class FuncionarioJerarquiaRol extends EntidadBase {

    @Id @GeneratedValue(strategy=IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="id_funcionario", nullable=false)
    private Funcionario funcionario;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="id_jerarquia", nullable=false)
    private Jerarquia jerarquia;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_rol", nullable = true)
    private Rol rol;


    @Column(name="fecha_asignacion", nullable=false)
    private LocalDate fechaAsignacion = LocalDate.now();

}
