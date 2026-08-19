package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.FuncionarioJerarquiaRol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "seg_permiso_rol_usuario")
@EqualsAndHashCode(of = {"idPermisoRolUsuario"}, callSuper = true)
@Comment("Tabla de permisos por rol/usuario")
public class PermisoRolUsuario extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de auditorias de acciones del sistema")
    private Long idPermisoRolUsuario;

    @JoinColumn(name = "id_empresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    @JoinColumn(name = "id_catalogo_tipo_asignacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Tipo de asignación, si por rol o por colaborador/rol")
    private Catalogo tipoAsignacion;

    @JoinColumn(name = "id_catalogo_tipo_permiso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Tipo de permiso")
    private Catalogo tipoPermiso;

    @JoinColumn(name = "id_funcionario")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Funcionario relacionado a permiso (opcional)")
    private Funcionario funcionario;

    @Comment("Token identificador del que cambia cuando se hace un cambio en el permiso")
    private String tokenVersionPermiso = UUID.randomUUID().toString();

    @OneToMany(mappedBy = "permisoRolUsuario")
    @JsonManagedReference
    @SQLRestriction("removido = false")
    private List<PermisoRolUsuarioMenu> menus;

    @OneToMany(mappedBy = "permisoRolUsuario")
    @JsonManagedReference
    @SQLRestriction("removido = false")
    private List<PermisoRol> roles;

    public PermisoRolUsuarioDTO convertirADTO() {
        PermisoRolUsuarioDTO dto = new PermisoRolUsuarioDTO();
        dto.setMenus(menus.stream().map(PermisoRolUsuarioMenu::convertirADTO).toList());
        dto.setTipoPermiso(tipoPermiso.convertirADTO());
        dto.setTipoAsignacion(tipoAsignacion.convertirADTO());
        dto.setTokenIdentificador(this.getTokenIdentificador());
        if (this.getFuncionario() != null) {
            dto.setFuncionario(funcionario.convertirADTO());
        }
        dto.setFechaCreacion(this.getFechaCreacion());
        dto.setFechaCreacionTexto(FuncionesAyuda.fechaATexto(this.getFechaCreacion(), false, false));
        return dto;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
