package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.permiso;

import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.PermisoRolUsuario;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.permiso.PermisoRolUsuarioNombresDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRolUsuarioRepository extends JpaRepository<PermisoRolUsuario, Long> {
    /**
     * Devuelve un objeto Page con los objetos PermisoRolUsuario por token identificador de empresa y booleano removido
     */
    Page<PermisoRolUsuario> findByEmpresaTokenIdentificadorAndRemovido(String tokenIdentificadorEmpresa, Boolean removido, Pageable pageable);

    @Query(value = """
        select
          trim(concat_ws(' ',
            trim(sf.apellidos),
            trim(sf.nombres)
          )) as nombreFuncionario,
          pc1.nombre as tipoAsignacion,
          pc2.nombre as tipoPermiso,
          --to_char(spru.fecha_creacion, 'DD/MM/YYYY HH24:MI') as fechaCreacion,
          spru.fecha_creacion fechaCreacion,
          spru.token_identificador,
          string_agg(distinct sr.nombre, ', ' order by sr.nombre) as nombreRoles
        from seg_permiso_rol_usuario spru
        left join seg_permiso_rol spr
          on spr.id_permiso_rol_usuario = spru.id_permiso_rol_usuario and spr.removido = :removido
        left join seg_funcionario sf
          on sf.id_funcionario = spru.id_funcionario
        join par_catalogo pc1
          on pc1.id_catalogo = spru.id_catalogo_tipo_asignacion
        join par_catalogo pc2
          on pc2.id_catalogo = spru.id_catalogo_tipo_permiso
        left join seg_rol sr
          on sr.id_rol = spr.rol_id_rol
        where (
          :valorBusqueda is null
          or upper(trim(concat_ws(' ',
                trim(sf.apellidos),
                trim(sf.nombres)
             ))) ilike concat('%', :valorBusqueda, '%')
          or pc1.nombre ilike concat('%', :valorBusqueda, '%')
          or pc2.nombre ilike concat('%', :valorBusqueda, '%')
          or to_char(spru.fecha_creacion, 'DD/MM/YYYY HH24:MI')
               ilike concat('%', :valorBusqueda, '%')
          or exists (
              select 1
              from seg_permiso_rol spr2
              join seg_rol sr2 on sr2.id_rol = spr2.rol_id_rol
              where spr2.id_permiso_rol_usuario = spru.id_permiso_rol_usuario
                and sr2.nombre ilike concat('%', :valorBusqueda, '%')
          )
        )
        and spru.removido = :removido
        group by
          sf.apellidos,
          sf.nombres,
          pc1.nombre,
          pc2.nombre,
          spru.fecha_creacion,
          spru.token_identificador
        order by spru.fecha_creacion desc
    """, nativeQuery = true)
    Page<PermisoRolUsuarioNombresDTO> obtenerPermisosPorTokenEmpresaYRemovido(String tokenIdentificadorEmpresa, Boolean removido, String valorBusqueda, Pageable pageable);

    /**
     * Devuelve un objeto PermisoRolUsuario por el token identificador, token identificador de empresa y booleano removido
     */
    Optional<PermisoRolUsuario> findByTokenIdentificadorAndEmpresaTokenIdentificadorAndRemovido(String tokenIdentificador, String tokenIdentificadorEmpresa, Boolean removido);

    /**
     * Devuelve un objeto List<PermisoRolUsuario> por el token identificador de funcionario, token identificador de rol, nemonico de tipo de permiso y booleano removido
     *
     * @param tokenFuncionario      token identificador de funcionario
     * @param tokenRol              token identificador de rol
     * @param nemonicoTipoPermiso   nemonico de tipo de permiso
     * @param removido              estado de eliminado lógico
     * @return                      lista de objetos PermisoRolUsuario
     */
    @Query(value = """
        select distinct spru.* from seg_permiso_rol_usuario spru
        left join seg_permiso_rol spr on spr.id_permiso_rol_usuario = spru.id_permiso_rol_usuario and spr.removido = :removido
        left join seg_funcionario sf on sf.id_funcionario = spru.id_funcionario
        left join seg_rol sr on sr.id_rol = spr.rol_id_rol
        left join par_catalogo pc on pc.id_catalogo = spru.id_catalogo_tipo_permiso
        where spru.removido = :removido
          and (spru.id_funcionario is null or sf.token_identificador = :tokenFuncionario)
          and (
                (sr.id_rol is not null and sr.token_identificador = :tokenRol)
                or (sr.id_rol is null and sf.token_identificador = :tokenFuncionario)
          )
          and pc.nemonico = :nemonicoTipoPermiso
    """, nativeQuery = true)
    List<PermisoRolUsuario> obtenerPorFuncionarioYRolYNemonicoTipoPermisoRemovido(
            String tokenFuncionario,
            String tokenRol,
            String nemonicoTipoPermiso,
            boolean removido
    );
//    @Query("""
//        SELECT pru
//        FROM PermisoRol pr
//        JOIN pr.permisoRolUsuario pru
//        LEFT JOIN pru.funcionario f
//        JOIN pr.rol r
//        JOIN pru.tipoPermiso tp
//        WHERE r.tokenIdentificador = :tokenRol
//            AND pru.removido = :removido
//            AND (pru.funcionario is null or f.tokenIdentificador = :tokenFuncionario)
//            AND tp.nemonico = :nemonicoTipoPermiso
//    """)
//    List<PermisoRolUsuario> obtenerPorFuncionarioYRolYNemonicoTipoPermisoRemovido(
//            String tokenFuncionario,
//            String tokenRol,
//            String nemonicoTipoPermiso,
//            boolean removido
//    );


}
