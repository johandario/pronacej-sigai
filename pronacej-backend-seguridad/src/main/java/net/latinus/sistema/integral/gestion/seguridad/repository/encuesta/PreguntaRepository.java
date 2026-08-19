package net.latinus.sistema.integral.gestion.seguridad.repository.encuesta;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Pregunta;
import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Seccion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    Pregunta findByIdPreguntaAndRemovido(Long idPregunta, Boolean removido);
    List<Pregunta> findByRemovido(boolean removido);
    List<Pregunta> findBySeccionAndRemovido(Seccion seccion, boolean b);
}
