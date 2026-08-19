package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.permiso;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.PermisoRolUsuarioMenuAccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermisoRolUsuarioMenuAccionRepository extends JpaRepository<PermisoRolUsuarioMenuAccion, Long> {

    List<PermisoRolUsuarioMenuAccion> findByPermisoRolUsuarioMenu_Menu_NemonicoAndPermisoRolUsuarioMenu_PermisoRolUsuario_TokenIdentificadorAndRemovido(String nemonicoMenu, String tokenIdentificadorPermisoRolUsuario, Boolean removido);



}
