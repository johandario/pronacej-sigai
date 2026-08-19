package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.AuditoriaServicioRest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaServicioRestRepository extends JpaRepository<AuditoriaServicioRest, Long> {

    /**
     * Devuelve un objeto AuditoriaAccionesSistema por el token identificador y removido
     *
     * @param tokenIdentificador String token identificador.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return AuditoriaAccionesSistema
     */
    AuditoriaServicioRest findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
