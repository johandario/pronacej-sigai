package net.latinus.sistema.integral.gestion.seguridad.repository.fuga;

import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoFugaRepository extends JpaRepository<EventoFuga, Long> {

    Page<EventoFuga> findByRemovido(Boolean removido, Pageable pageable);
    EventoFuga findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    List<EventoFuga> findByTokenFichaIdentificacionIdFichaIdentificacionAndRemovido(Long idFichaIdentificacion, Boolean removido);
    EventoFuga findFirstByOrderByIdFugaDesc();

}
