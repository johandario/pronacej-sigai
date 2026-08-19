package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.PasswordUserSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordUserSistemaRepository extends JpaRepository<PasswordUserSistema, Long> {

    /**
     * Devuelve una lista de contraseñas pertenecientes a un usuario en especifico
     *
     * @param idUsuarioSistema Long id del usuario del sistema.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<PasswordUserSistema>
     */
    List<PasswordUserSistema> findByUsuarioSistemaIdUsuarioSistemaAndRemovido(
            Long idUsuarioSistema, Boolean removido
    );


    /**
     * Devuelve una lista de contraseñas pertenecientes a un usuario en especifico
     *
     * @param idUsuarioSistema Long id del usuario del sistema.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<PasswordUserSistema>
     */
    List<PasswordUserSistema> findByUsuarioSistemaIdUsuarioSistemaAndRemovidoOrderByIdPasswordDesc(
            Long idUsuarioSistema, Boolean removido
    );
}
