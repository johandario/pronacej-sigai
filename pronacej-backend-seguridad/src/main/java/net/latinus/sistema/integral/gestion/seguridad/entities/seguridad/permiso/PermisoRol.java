package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "seg_permiso_rol")
@EqualsAndHashCode(of = {"idPermisoRol"}, callSuper = true)
@Comment("Tabla de permisos por roles")
public class PermisoRol extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de auditorias de acciones del sistema")
    private Long idPermisoRol;

    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del rol")
    private Rol rol;

    @JoinColumn(name = "id_permiso_rol_usuario")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del permisoRolUsuario")
    @JsonBackReference
    private PermisoRolUsuario permisoRolUsuario;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
