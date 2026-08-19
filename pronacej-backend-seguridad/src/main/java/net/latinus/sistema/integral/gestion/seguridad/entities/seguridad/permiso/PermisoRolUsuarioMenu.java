package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Menu;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioMenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Data
@Table(name = "seg_permiso_rol_usuario_menu")
@EqualsAndHashCode(of = {"idPermisoRolUsuarioMenu"}, callSuper = true)
@Comment("Tabla de permisos por rol/usuario")
public class PermisoRolUsuarioMenu extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de auditorias de acciones del sistema")
    private Long idPermisoRolUsuarioMenu;

    @JoinColumn(name = "id_menu")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del menú para permitir acceso")
    private Menu menu;

    @OneToMany(mappedBy = "permisoRolUsuarioMenu")
    @JsonManagedReference
    @SQLRestriction("removido = false")
    private List<PermisoRolUsuarioMenuAccion> acciones;

    @JoinColumn(name = "id_permiso_rol_usuario")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del permisoRolUsuario")
    @JsonBackReference
    private PermisoRolUsuario permisoRolUsuario;

    public PermisoRolUsuarioMenuDTO convertirADTO() {
        PermisoRolUsuarioMenuDTO dto = new PermisoRolUsuarioMenuDTO();
        dto.setTokenMenu(menu.getTokenIdentificador());
        dto.setNemonicoMenu(menu.getNemonico());
        dto.setAcciones(acciones.stream().map(PermisoRolUsuarioMenuAccion::convertirADTO).toList());
        dto.setTokenIdentificador(this.getTokenIdentificador());
        return dto;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
