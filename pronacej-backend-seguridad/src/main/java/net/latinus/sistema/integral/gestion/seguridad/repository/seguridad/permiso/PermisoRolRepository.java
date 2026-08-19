package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.permiso;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.PermisoRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermisoRolRepository extends JpaRepository<PermisoRol, Long> {

    /**
     * Encontrar todos los registros del padre
     * @param tokenIdentificadorPermisoRolUsuario token identificador del padre (permisoRolUsuario)
     * @param removido valor de removido
     * @return lista de PermisoRol
     */
    List<PermisoRol> findByPermisoRolUsuarioTokenIdentificadorAndRemovido(String tokenIdentificadorPermisoRolUsuario, Boolean removido);
}
