package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ReseteoDePassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReseteoDePasswordRepository extends JpaRepository<ReseteoDePassword, Long> {

    /**
     * Devuelve un objeto ReseteoDePassword por el token identificador y removido
     *
     * @param tokenIdentificador String token identificador.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ReseteoDePassword
     */
    ReseteoDePassword findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Devuelve un objeto ReseteoDePassword por el token identificador y removido
     *
     * @param tokenIdentificador String token identificador.
     * @param estadoNemonico String nemonico del estado
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ReseteoDePassword
     */
    ReseteoDePassword findByTokenIdentificadorAndEstadoNemonicoAndRemovido(String tokenIdentificador,
                                                                           String estadoNemonico, Boolean removido);
}
