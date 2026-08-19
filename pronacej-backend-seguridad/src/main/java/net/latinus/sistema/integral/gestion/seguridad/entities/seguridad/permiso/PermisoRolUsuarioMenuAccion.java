package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioMenuAccionDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "seg_permiso_rol_usuario_menu_accion")
@EqualsAndHashCode(of = {"idPermisoRolUsuarioMenuAccion"}, callSuper = true)
@Comment("Tabla de acciones que puede tener un PermisoRolUsuarioMenu")
public class PermisoRolUsuarioMenuAccion extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de auditorias de acciones del sistema")
    private Long idPermisoRolUsuarioMenuAccion;

    @JoinColumn(name = "id_catalogo_accion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Catalogo accion;

    @Comment("Estado de la acción")
    private Boolean activo;

    @JoinColumn(name = "id_permiso_rol_usuario_menu")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del permisoRolUsuarioMenu")
    @JsonBackReference
    private PermisoRolUsuarioMenu permisoRolUsuarioMenu;

    public PermisoRolUsuarioMenuAccionDTO convertirADTO() {
        PermisoRolUsuarioMenuAccionDTO dto = new PermisoRolUsuarioMenuAccionDTO();
        dto.setTokenCatalogoAccion(this.accion.getTokenIdentificador());
        dto.setNemonicoCatalogoAccion(this.accion.getNemonico());
        dto.setTokenIdentificador(this.getTokenIdentificador());
        dto.setActivo(this.activo);
        return dto;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
