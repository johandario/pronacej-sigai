package net.latinus.sistema.integral.gestion.seguridad.repository.encuesta;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Pregunta;
import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {
    Respuesta findByIdRespuestaAndRemovido(Long idRespuesta, Boolean removido);
    List<Respuesta> findByRemovido(boolean removido);
    List<Respuesta> findByPreguntaAndRemovido(Pregunta pregunta, boolean b);
}
