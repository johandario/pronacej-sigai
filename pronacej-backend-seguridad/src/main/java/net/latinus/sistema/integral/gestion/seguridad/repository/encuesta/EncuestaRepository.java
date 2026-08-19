package net.latinus.sistema.integral.gestion.seguridad.repository.encuesta;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Encuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {
    List<Encuesta> findByRemovido(Boolean removido);
    List<Encuesta> findByCategoriaNemonicoAndRemovido(String tokenIdentificador, Boolean removido);
    Encuesta findByIdEncuestaAndRemovido(Long idEncuesta, Boolean removido);
    Encuesta findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean b);
    Encuesta findByTokenIdentificador(String tokenIdentificador);
}
