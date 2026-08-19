package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.permiso;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.PermisoRolUsuarioMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermisoRolUsuarioMenuRepository extends JpaRepository<PermisoRolUsuarioMenu, Long> {
    List<PermisoRolUsuarioMenu> findByPermisoRolUsuarioTokenIdentificadorAndRemovido(String tokenIdentificadorPermisoRolUsuario, Boolean removido);

    @Query(value = """
        select distinct sprum.* from seg_permiso_rol_usuario spru
        left join seg_permiso_rol spr on spr.id_permiso_rol_usuario = spru.id_permiso_rol_usuario and spr.removido = :removido
        left join seg_funcionario sf on sf.id_funcionario = spru.id_funcionario
        left join seg_rol sr on sr.id_rol = spr.rol_id_rol
        left join seg_permiso_rol_usuario_menu sprum on sprum.id_permiso_rol_usuario = spru.id_permiso_rol_usuario
        left join seg_menu sm on sm.id_menu = sprum.id_menu
        left join par_catalogo pc on pc.id_catalogo = spru.id_catalogo_tipo_permiso
        where spru.removido = :removido
          --and spr.removido = :removido
          and sprum.removido = :removido
          and (spru.id_funcionario is null or sf.token_identificador = :tokenFuncionario)
          and (
                (sr.id_rol is not null and sr.token_identificador = :tokenRol)
                or (sr.id_rol is null and sf.token_identificador = :tokenFuncionario)
          )
          and sm.nemonico = :nemonicoMenu
          and pc.nemonico = :nemonicoTipoPermiso
    """, nativeQuery = true)
    List<PermisoRolUsuarioMenu> obtenerPorFuncionarioYRolYNemonicoTipoPermisoYNemonicoMenuYRemovido(
            String tokenFuncionario,
            String tokenRol,
            String nemonicoTipoPermiso,
            String nemonicoMenu,
            boolean removido
    );
}
